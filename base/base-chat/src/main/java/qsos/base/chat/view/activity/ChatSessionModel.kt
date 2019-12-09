package qsos.base.chat.view.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import qsos.base.chat.R
import qsos.base.chat.data.entity.EnumChatMessageType
import qsos.base.chat.data.entity.MChatMessageAudio
import qsos.base.chat.service.IMessageService
import qsos.base.chat.utils.AudioUtils
import qsos.core.file.RxImageConverters
import qsos.core.file.RxImagePicker
import qsos.core.file.Sources
import qsos.core.lib.utils.dialog.AbsBottomDialog
import qsos.core.lib.utils.dialog.BottomDialog
import qsos.core.lib.utils.dialog.BottomDialogUtils
import qsos.core.lib.utils.file.FileUtils
import qsos.core.player.PlayerConfigHelper
import qsos.core.player.audio.AudioPlayerHelper
import qsos.core.player.data.PreAudioEntity
import qsos.lib.base.callback.OnTListener
import qsos.lib.base.utils.ToastUtils
import qsos.lib.netservice.file.HttpFileEntity
import java.io.File
import java.util.*
import kotlin.collections.HashMap

/**
 * @author : 华清松
 * 聊天会话功能接口
 */
class ChatSessionModel(private val activity: Activity) : IChatSessionModel {

    /**文件上传大小限制*/
    private val mUpdateLimit = 5 * 1000 * 1000
    private val mPlayList: MutableLiveData<HashMap<String, AudioPlayerHelper?>> = MutableLiveData()

    init {
        mPlayList.value = HashMap()
    }

    override fun clickTextMessage(
            view: View, message: IMessageService.Message,
            back: (action: Int) -> Unit
    ) {
        val popup = PopupMenu(activity, view)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.chat_message_item_click, popup.menu)
        popup.menu.removeGroup(R.id.menu_message_2)
        popup.setOnMenuItemClickListener { item ->
            back.invoke(item.itemId)
            true
        }
        popup.show()
    }

    override fun longClickTextMessage(
            view: View, message: IMessageService.Message,
            back: (action: Int) -> Unit
    ) {
        val popup = PopupMenu(activity, view)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.chat_message_item_click, popup.menu)
        popup.menu.removeGroup(R.id.menu_message_1)
        when {
            message.sendUserId == ChatMainActivity.mLoginUser.value?.userId -> {
                popup.menu.removeItem(R.id.menu_message_reply)
                if (message.readNum >= 2) {
                    popup.menu.removeItem(R.id.menu_message_cancel)
                }
            }
            message.sendUserId != ChatMainActivity.mLoginUser.value?.userId -> {
                popup.menu.removeItem(R.id.menu_message_cancel)
            }
        }
        popup.setOnMenuItemClickListener { item ->
            back.invoke(item.itemId)
            true
        }
        popup.show()
    }

    override fun resendMessage(message: IMessageService.Message, back: (file: HttpFileEntity?) -> Unit) {
        var needUpdate = false
        var file: File? = null
        when (message.content.getContentType()) {
            EnumChatMessageType.IMAGE.contentType, EnumChatMessageType.VIDEO.contentType,
            EnumChatMessageType.AUDIO.contentType, EnumChatMessageType.FILE.contentType -> {
                val url = message.content.fields["url"] as String
                file = File(url)
                needUpdate = file.exists()
            }
            EnumChatMessageType.TEXT.contentType, EnumChatMessageType.LINK.contentType,
            EnumChatMessageType.CARD.contentType, EnumChatMessageType.LOCATION.contentType -> {
                needUpdate = false
            }
        }
        if (needUpdate) {
            val mHttpFileEntity = HttpFileEntity(url = null, path = file!!.absolutePath, filename = file.name)
            mHttpFileEntity.adjoin = message
            back.invoke(mHttpFileEntity)
        } else {
            back.invoke(null)
        }
    }

    override fun playAudio(view: View, stateView: ImageView, data: MChatMessageAudio) {
        var mAudioPlayerHelper: AudioPlayerHelper? = mPlayList.value!![data.url]
        if (mAudioPlayerHelper == null) {
            /**停止其它播放*/
            stopAudioPlay()
            mAudioPlayerHelper = PlayerConfigHelper.previewAudio(
                    context = activity, position = 0,
                    list = arrayListOf(
                            PreAudioEntity(
                                    name = data.name,
                                    desc = data.name,
                                    path = data.url
                            )
                    ),
                    onPlayerListener = object : OnTListener<AudioPlayerHelper.State> {
                        override fun back(t: AudioPlayerHelper.State) {
                            stateView.setImageDrawable(AppCompatResources.getDrawable(activity, when (t) {
                                AudioPlayerHelper.State.STOP -> {
                                    R.drawable.icon_play
                                }
                                AudioPlayerHelper.State.ERROR -> {
                                    ToastUtils.showToast(activity, "播放错误")
                                    R.drawable.icon_play
                                }
                                else -> {
                                    R.drawable.icon_pause
                                }
                            }))
                        }
                    }
            )
            mPlayList.value!![data.url] = mAudioPlayerHelper
        } else {
            /**停止当前音频播放*/
            mPlayList.value!!.remove(data.url)
            mAudioPlayerHelper.stop()
        }
    }

    override fun stopAudioPlay() {
        mPlayList.value!!.values.forEach {
            it?.stop()
        }
        mPlayList.value!!.clear()
    }

    override fun sendFileMessage(
            context: Context, fm: FragmentManager, fileType: Int,
            back: (type: EnumChatMessageType, files: ArrayList<HttpFileEntity>) -> Unit
    ) {
        when (fileType) {
            0 -> {
                RxImagePicker.with(fm).takeImage(type = Sources.DEVICE)
                        .flatMap {
                            RxImageConverters.uriToFileObservable(context, it, FileUtils.createImageFile())
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            FileUtils.zipFileByLuBan(context, arrayListOf(it), object : OnTListener<List<File>> {
                                override fun back(t: List<File>) {
                                    val files = arrayListOf<HttpFileEntity>()
                                    t.forEach { f ->
                                        val file = HttpFileEntity(url = null, path = f.absolutePath, filename = f.name)
                                        file.adjoin = f.length()
                                        files.add(file)
                                    }
                                    back.invoke(EnumChatMessageType.IMAGE, files)
                                }
                            })
                        }
            }
            1 -> {
                RxImagePicker.with(fm).takeFiles(arrayOf("image/*")).flatMap {
                    val files = arrayListOf<File>()
                    it.forEachIndexed { index, uri ->
                        if (index < 10) {
                            RxImageConverters.uriToFile(context, uri, null)?.let { f ->
                                files.add(f)
                            }
                        }
                    }
                    Observable.just(files)
                }.observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            FileUtils.zipFileByLuBan(context, it, object : OnTListener<List<File>> {
                                override fun back(t: List<File>) {
                                    val files = arrayListOf<HttpFileEntity>()
                                    t.forEach { f ->
                                        val file = HttpFileEntity(url = null, path = f.absolutePath, filename = f.name)
                                        file.adjoin = f.length()
                                        if (f.length() <= mUpdateLimit) {
                                            files.add(file)
                                        } else {
                                            ToastUtils.showToast(context, "文件【${f.name}】大小超过限制，不允许上传")
                                        }
                                    }
                                    back.invoke(EnumChatMessageType.IMAGE, files)
                                }
                            })
                        }
            }
            2 -> {
                RxImagePicker.with(fm).takeVideo()
                        .flatMap {
                            RxImageConverters.uriToFileObservable(context, it, FileUtils.createVideoFile())
                        }.observeOn(AndroidSchedulers.mainThread()).subscribe {
                            val files = arrayListOf<HttpFileEntity>()
                            if (it.length() <= mUpdateLimit) {
                                val file = HttpFileEntity(url = null, path = it.absolutePath, filename = it.name)
                                file.adjoin = it.length()
                                files.add(file)
                            } else {
                                ToastUtils.showToast(context, "文件【${it.name}】大小超过限制，不允许上传")
                            }
                            back.invoke(EnumChatMessageType.VIDEO, files)
                        }
            }
            3 -> {
                BottomDialogUtils.showCustomerView(context, R.layout.audio_dialog, object : BottomDialog.ViewListener {
                    @SuppressLint("CheckResult")
                    override fun bindView(dialog: AbsBottomDialog) {
                        AudioUtils.record(
                                dialog.findViewById<ImageView>(R.id.audio_action),
                                dialog.findViewById(R.id.audio_state),
                                object : OnTListener<Int> {
                                    override fun back(t: Int) {
                                        if (t < 0) {
                                            dialog.dismiss()
                                        }
                                    }
                                }
                        ).observeOn(AndroidSchedulers.mainThread()).subscribe(
                                {
                                    val file = File(it.audioPath)
                                    if (file.exists()) {
                                        val fileEntity = HttpFileEntity(url = null, path = file.absolutePath, filename = file.name)
                                        fileEntity.adjoin = it.recordTime + 0L
                                        back.invoke(EnumChatMessageType.AUDIO, arrayListOf(fileEntity))
                                    } else {
                                        ToastUtils.showToast(context, "文件不存在")
                                    }
                                },
                                {
                                    it.printStackTrace()
                                }
                        )
                    }
                })
            }
            else -> {
                RxImagePicker.with(fm).takeFiles(
                        arrayOf("application/vnd.ms-powerpoint", "application/pdf", "application/msword")
                ).flatMap {
                    val files = arrayListOf<File>()
                    it.forEachIndexed { index, uri ->
                        if (index < 2) {
                            RxImageConverters.uriToFile(context, uri, null)?.let { f ->
                                if (f.length() <= mUpdateLimit) {
                                    files.add(f)
                                } else {
                                    ToastUtils.showToast(context, "文件【${f.name}】大小超过限制，不允许上传")
                                }
                            }
                        } else {
                            ToastUtils.showToast(context, "一次最多可上传1个文件")
                        }
                    }
                    Observable.just(files)
                }.observeOn(AndroidSchedulers.mainThread()).subscribe {
                    val files = arrayListOf<HttpFileEntity>()
                    it.forEach { f ->
                        val file = HttpFileEntity(url = null, path = f.absolutePath, filename = f.name)
                        file.adjoin = f.length()
                        files.add(file)
                    }
                    back.invoke(EnumChatMessageType.FILE, files)
                }
            }
        }
    }

}
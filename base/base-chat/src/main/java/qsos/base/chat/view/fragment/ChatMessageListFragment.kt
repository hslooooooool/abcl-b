package qsos.base.chat.view.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import com.noober.menu.FloatMenu
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_chat_message.*
import kotlinx.android.synthetic.main.item_message_audio.view.*
import qsos.base.chat.R
import qsos.base.chat.data.entity.*
import qsos.base.chat.data.model.DefChatMessageModelIml
import qsos.base.chat.data.model.IChatModel
import qsos.base.chat.utils.AudioUtils
import qsos.base.chat.view.activity.ChatMainActivity
import qsos.base.chat.view.adapter.ChatMessageAdapter
import qsos.base.chat.view.holder.ItemChatMessageBaseViewHolder
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
import qsos.lib.base.base.fragment.BaseFragment
import qsos.lib.base.callback.OnListItemClickListener
import qsos.lib.base.callback.OnTListener
import qsos.lib.base.utils.BaseUtils
import qsos.lib.base.utils.DateUtils
import qsos.lib.base.utils.LogUtil
import qsos.lib.base.utils.ToastUtils
import qsos.lib.netservice.file.FileRepository
import qsos.lib.netservice.file.HttpFileEntity
import qsos.lib.netservice.file.IFileModel
import java.io.File
import java.util.*
import kotlin.collections.HashMap

/**
 * @author : 华清松
 * 聊天页面
 */
class ChatMessageListFragment(
        private val mSession: ChatSession,
        override val layoutId: Int = R.layout.fragment_chat_message,
        override val reload: Boolean = false
) : BaseFragment(), IChatFragment {

    private var mFileModel: IFileModel? = null
    private var mMessageAdapter: ChatMessageAdapter? = null
    private var mChatMessageModel: IChatModel.IMessage? = null
    private var mLinearLayoutManager: LinearLayoutManager? = null
    private val mPlayList: HashMap<String, AudioPlayerHelper?> = HashMap()
    private val mMessageData: MutableLiveData<ArrayList<MChatMessage>> = MutableLiveData()

    override fun initData(savedInstanceState: Bundle?) {
        mChatMessageModel = DefChatMessageModelIml()
        mFileModel = FileRepository(mChatMessageModel!!.mJob)
        mMessageData.value = arrayListOf()
    }

    override fun initView(view: View) {

        mMessageAdapter = ChatMessageAdapter(mSession, mMessageData.value!!, object : OnListItemClickListener {
            override fun onItemClick(view: View, position: Int, obj: Any?) {
                preOnItemClick(view, position, obj)
            }

            override fun onItemLongClick(view: View, position: Int, obj: Any?) {
                preOnItemLongClick(view, position, obj)
            }

        })
        mLinearLayoutManager = LinearLayoutManager(mContext)
        mLinearLayoutManager!!.stackFromEnd = false
        mLinearLayoutManager!!.reverseLayout = false
        chat_message_list.layoutManager = mLinearLayoutManager
        chat_message_list.adapter = mMessageAdapter

        mChatMessageModel?.mDataOfChatMessageList?.observe(this, Observer {
            mMessageData.value?.clear()
            it.data?.let { messages ->
                mMessageData.value!!.addAll(messages)
                mMessageAdapter?.notifyDataSetChanged()
                mLinearLayoutManager?.scrollToPosition(mMessageData.value!!.size - 1)
            }
        })

        mMessageData.observe(this, Observer {
            mMessageAdapter?.notifyDataSetChanged()
        })

        chat_message_send.setOnClickListener {
            sendTextMessage()
        }
        chat_message_audio.setOnClickListener {
            takeAudio()
        }
        chat_message_phone.setOnClickListener {
            takePhoto()
        }
        chat_message_album.setOnClickListener {
            takeAlbum()
        }
        chat_message_video.setOnClickListener {
            takeVideo()
        }
        chat_message_file.setOnClickListener {
            takeFile()
        }

        base_title_bar.findViewById<TextView>(R.id.base_title_bar_title)?.text = ""
        base_title_bar.findViewById<View>(R.id.base_title_bar_icon_left)?.setOnClickListener {
            ARouter.getInstance().build("/CHAT/MAIN")
                    .withTransition(R.anim.activity_out_center, R.anim.activity_in_center)
                    .navigation()
            activity?.finish()
        }

        getData()

    }

    override fun getData() {
        mChatMessageModel?.getMessageListBySessionId(sessionId = mSession.sessionId)
    }

    override fun onDestroy() {
        mChatMessageModel?.clear()
        super.onDestroy()
    }

    override fun sendTextMessage() {
        val content = chat_message_edit.text.toString().trim()
        if (TextUtils.isEmpty(content)) {
            chat_message_edit.hint = "请输入内容"
        } else {
            val map = HashMap<String, Any?>()
            map["contentType"] = MChatMessageType.TEXT.contentType
            map["contentDesc"] = content
            map["content"] = content
            val message = MChatMessage(
                    user = ChatMainActivity.mLoginUser.value!!,
                    createTime = DateUtils.format(date = Date()),
                    message = ChatMessage(
                            sessionId = mSession.sessionId,
                            content = ChatContent(
                                    fields = map
                            )
                    )
            )
            message.sendStatus = MChatSendStatus.SENDING
            val hashCode = message.hashCode()
            message.hashCode = hashCode

            mMessageData.value!!.add(message)
            mMessageAdapter?.notifyDataSetChanged()
            mLinearLayoutManager?.scrollToPosition(mMessageData.value!!.size - 1)

            mChatMessageModel?.sendMessage(
                    message = message,
                    failed = { msg, result ->
                        ToastUtils.showToast(mContext, msg)
                        notifySendMessage(result)
                    },
                    success = { result ->
                        notifySendMessage(result)
                    }
            )

            /**发送后更新视图*/
            BaseUtils.closeKeyBord(mContext, chat_message_edit)
            chat_message_edit.setText("")
            chat_message_edit.clearFocus()
        }
    }

    override fun sendFileMessage(type: MChatMessageType, files: ArrayList<HttpFileEntity>) {
        files.forEach { file ->
            val map = HashMap<String, Any?>()
            map["contentType"] = type.contentType
            map["contentDesc"] = "文件"
            map["name"] = file.filename
            map["url"] = file.path
            when (type) {
                MChatMessageType.AUDIO -> {
                    map["length"] = file.adjoin as Int
                }
                else -> {
                }
            }
            val message = MChatMessage(
                    user = ChatMainActivity.mLoginUser.value!!,
                    createTime = DateUtils.format(date = Date()),
                    message = ChatMessage(
                            sessionId = mSession.sessionId,
                            content = ChatContent(
                                    fields = map
                            )
                    )
            )
            message.sendStatus = MChatSendStatus.SENDING
            val hashCode = message.hashCode()
            message.hashCode = hashCode
            mMessageData.value!!.add(message)
            file.adjoin = message
        }

        mMessageAdapter?.notifyDataSetChanged()
        mLinearLayoutManager?.scrollToPosition(mMessageData.value!!.size - 1)

        uploadFile(files)
    }

    override fun notifySendMessage(result: MChatMessage) {
        mMessageData.value?.find {
            it.hashCode == result.hashCode
        }?.sendStatus = result.sendStatus
        mMessageAdapter?.notifyDataSetChanged()
    }

    override fun onPause() {
        stopAudioPlay()
        super.onPause()
    }

    override fun takeAudio() {
        BottomDialogUtils.showCustomerView(mContext, R.layout.audio_dialog, object : BottomDialog.ViewListener {
            override fun bindView(dialog: AbsBottomDialog) {
                AudioUtils.record(dialog).observeOn(AndroidSchedulers.mainThread()).subscribe({
                    val file = File(it.audioPath)
                    if (file.exists()) {
                        val fileEntity = HttpFileEntity(url = null, path = file.absolutePath, filename = file.name)
                        fileEntity.adjoin = it.recordTime
                        sendFileMessage(MChatMessageType.AUDIO, arrayListOf(
                                fileEntity
                        ))
                    } else {
                        ToastUtils.showToast(mContext, "文件不存在")
                    }
                }, {
                    it.printStackTrace()
                }).takeUnless {
                    (context as AppCompatActivity).isFinishing
                }
            }
        })
    }

    override fun takePhoto() {
        RxImagePicker.with((context as FragmentActivity).supportFragmentManager).takeImage(type = Sources.DEVICE)
                .flatMap {
                    RxImageConverters.uriToFileObservable(mContext, it, FileUtils.createImageFile())
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val file = HttpFileEntity(url = null, path = it.absolutePath, filename = it.name)
                    sendFileMessage(MChatMessageType.IMAGE, arrayListOf(file))
                }.takeUnless {
                    activity!!.isFinishing
                }
    }

    override fun takeAlbum() {
        RxImagePicker.with((context as FragmentActivity).supportFragmentManager)
                .takeFiles(arrayOf("image/*")).flatMap {
                    val files = arrayListOf<File>()
                    it.forEachIndexed { index, uri ->
                        if (index < 10) {
                            RxImageConverters.uriToFile(mContext, uri, FileUtils.createImageFile())?.let { f ->
                                files.add(f)
                            }
                        } else {
                            ToastUtils.showToast(mContext, "一次最多可上传9张")
                        }
                    }
                    Observable.just(files)
                }.observeOn(AndroidSchedulers.mainThread()).subscribe {
                    val files = arrayListOf<HttpFileEntity>()
                    it.forEach { f ->
                        files.add(HttpFileEntity(url = null, path = f.absolutePath, filename = f.name))
                    }
                    sendFileMessage(MChatMessageType.IMAGE, files)
                }.takeUnless {
                    activity!!.isFinishing
                }
    }

    override fun takeVideo() {
        RxImagePicker.with((context as FragmentActivity).supportFragmentManager).takeVideo().flatMap {
            RxImageConverters.uriToFileObservable(mContext, it, FileUtils.createVideoFile())
        }.observeOn(AndroidSchedulers.mainThread()).subscribe {
            val file = HttpFileEntity(url = null, path = it.absolutePath, filename = it.name)
            sendFileMessage(MChatMessageType.VIDEO, arrayListOf(file))
        }.takeUnless {
            activity!!.isFinishing
        }
    }

    override fun takeFile() {
        RxImagePicker.with((context as FragmentActivity).supportFragmentManager).takeFiles(
                arrayOf("application/vnd.ms-powerpoint", "application/pdf", "application/msword")
        ).flatMap {
            val files = arrayListOf<File>()
            it.forEachIndexed { index, uri ->
                if (index < 2) {
                    RxImageConverters.uriToFile(mContext, uri, FileUtils.createImageFile())?.let { f ->
                        files.add(f)
                    }
                } else {
                    ToastUtils.showToast(mContext, "一次最多可上传1个文件")
                }
            }
            Observable.just(files)
        }.observeOn(AndroidSchedulers.mainThread()).subscribe {
            val files = arrayListOf<HttpFileEntity>()
            it.forEach { f ->
                files.add(HttpFileEntity(url = null, path = f.absolutePath, filename = f.name))
            }
            sendFileMessage(MChatMessageType.FILE, files)
        }.takeUnless {
            activity!!.isFinishing
        }
    }

    override fun uploadFile(files: ArrayList<HttpFileEntity>) {
        if (!mMessageData.value.isNullOrEmpty() && files.isNotEmpty()) {
            val file = files[0]

            uploadFileMessage(file, MBaseChatMessageFile.UpLoadState.LOADING)

            mFileModel?.uploadFile(file, object : OnTListener<HttpFileEntity> {
                override fun back(t: HttpFileEntity) {
                    if (t.loadSuccess) {
                        LogUtil.i("上传文件成功>>>>>" + t.filename)

                        uploadFileMessage(t, MBaseChatMessageFile.UpLoadState.SUCCESS)

                        mChatMessageModel?.sendMessage(
                                message = t.adjoin as MChatMessage,
                                failed = { msg, result ->
                                    ToastUtils.showToast(mContext, msg)
                                    notifySendMessage(result)
                                },
                                success = { result ->
                                    notifySendMessage(result)
                                    files.removeAt(0)
                                    if (files.isNotEmpty()) {
                                        uploadFile(files)
                                    }
                                })
                    } else {
                        LogUtil.i("上传文件>>>>>" + t.progress)
                    }
                }
            })
        }
    }

    private fun uploadFileMessage(file: HttpFileEntity, state: MBaseChatMessageFile.UpLoadState) {
        val hashCode = (file.adjoin as MChatMessage).hashCode!!
        var position: Int? = null
        for ((index, msg) in mMessageData.value!!.withIndex()) {
            if (msg.hashCode == hashCode) {
                mMessageData.value!![index].message.content.fields["uploadState"] = state
                position = index
                break
            }
        }
        position?.let {
            mMessageAdapter?.notifyItemChanged(it, ItemChatMessageBaseViewHolder.UpdateType.UPLOAD_STATE)
        }
    }

    override fun playAudio(view: View, data: MChatMessageAudio) {
        var mAudioPlayerHelper: AudioPlayerHelper? = mPlayList[data.url]
        if (mAudioPlayerHelper == null) {
            /**停止其它播放*/
            stopAudioPlay()
            mAudioPlayerHelper = PlayerConfigHelper.previewAudio(
                    context = mContext, position = 0,
                    list = arrayListOf(
                            PreAudioEntity(
                                    name = data.name,
                                    desc = data.name,
                                    path = data.url
                            )
                    ),
                    onPlayerListener = object : OnTListener<AudioPlayerHelper.State> {
                        override fun back(t: AudioPlayerHelper.State) {
                            view.apply {
                                this.item_message_audio_state.setImageDrawable(AppCompatResources.getDrawable(mContext, when (t) {
                                    AudioPlayerHelper.State.STOP -> {
                                        R.drawable.icon_play
                                    }
                                    AudioPlayerHelper.State.ERROR -> {
                                        ToastUtils.showToast(mContext, "播放错误")
                                        R.drawable.icon_play
                                    }
                                    else -> {
                                        R.drawable.icon_pause
                                    }
                                }))
                            }

                        }
                    }
            )
            mPlayList[data.url] = mAudioPlayerHelper
        } else {
            /**停止当前音频播放*/
            mPlayList.remove(data.url)
            mAudioPlayerHelper.stop()
        }
    }

    /**停止所有语音播放*/
    private fun stopAudioPlay() {
        mPlayList.values.forEach {
            it?.stop()
        }
    }

    /**列表项点击*/
    private fun preOnItemClick(view: View, position: Int, obj: Any?) {
        when (view.id) {
            R.id.item_message_view_audio -> {
                if (obj is MChatMessage) {
                    if (obj.content is MChatMessageAudio) {
                        playAudio(view, obj.content as MChatMessageAudio)
                    }
                }
            }
            R.id.item_message_cancel_reedit -> {
                if (obj != null && obj is MChatMessage && obj.contentType == MChatMessageType.TEXT.contentType) {
                    val content = obj.content as MChatMessageText
                    chat_message_edit.setText(content.content)
                    obj.message.cancelBack = false
                    notifySendMessage(obj)
                }
            }
        }
    }

    /**列表项长按*/
    private fun preOnItemLongClick(view: View, position: Int, obj: Any?) {
        val floatMenu = FloatMenu(context, view)
        floatMenu.items("撤销")
        when (view.id) {
            R.id.item_message_content -> {
                if (obj != null && obj is MChatMessage) {
                    floatMenu.setOnItemClickListener { _, index ->
                        when (index) {
                            0 -> {
                                mChatMessageModel?.deleteMessage(message = obj, failed = { msg, _ ->
                                    ToastUtils.showToast(context, msg)
                                }, success = { result ->
                                    result.message.cancelBack = true
                                    notifySendMessage(result)
                                })
                            }
                            else -> {
                            }
                        }
                    }
                    floatMenu.show()
                }
            }
        }
    }

    /**检查消息附件上传情况，防止结束此页面时文件上传失败，友情提示*/
    private fun checkMessageFileUploaded(): Boolean {
        var uploaded = true
        for (m in mMessageData.value!!) {
            if (m.content is MBaseChatMessageFile) {
                val file = m.content as MBaseChatMessageFile
                if (file.uploadState != MBaseChatMessageFile.UpLoadState.SUCCESS) {
                    uploaded = false
                }
                break
            }
        }
        return uploaded
    }

}
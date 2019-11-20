package qsos.base.chat.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_chat_message.*
import qsos.base.chat.R
import qsos.base.chat.data.entity.*
import qsos.base.chat.data.model.DefChatMessageModelIml
import qsos.base.chat.data.model.DefChatSessionModelIml
import qsos.base.chat.data.model.IChatModel
import qsos.base.chat.service.DefMessageService
import qsos.base.chat.service.IMessageService
import qsos.base.chat.utils.AudioUtils
import qsos.base.chat.view.fragment.ChatMessageListFragment
import qsos.core.file.RxImageConverters
import qsos.core.file.RxImagePicker
import qsos.core.file.Sources
import qsos.core.lib.utils.dialog.AbsBottomDialog
import qsos.core.lib.utils.dialog.BottomDialog
import qsos.core.lib.utils.dialog.BottomDialogUtils
import qsos.core.lib.utils.file.FileUtils
import qsos.lib.base.base.activity.BaseActivity
import qsos.lib.base.callback.OnTListener
import qsos.lib.base.utils.BaseUtils
import qsos.lib.base.utils.LogUtil
import qsos.lib.base.utils.ToastUtils
import qsos.lib.base.utils.rx.RxBus
import qsos.lib.netservice.file.FileRepository
import qsos.lib.netservice.file.HttpFileEntity
import qsos.lib.netservice.file.IFileModel
import java.io.File
import java.util.*

/**
 * @author : 华清松
 * 聊天会话页面
 */
@Route(group = "CHAT", path = "/CHAT/SESSION")
class ChatSessionActivity(
        override val layoutId: Int = R.layout.activity_chat_message,
        override val reload: Boolean = false
) : BaseActivity(), IChatSessionModel {

    @Autowired(name = "/CHAT/SESSION_ID")
    @JvmField
    var mSessionId: Int? = -1

    private var mChatSessionModel: IChatModel.ISession? = null

    private var mFileModel: IFileModel? = null
    private var mChatMessageModel: IChatModel.IMessage? = null
    private val mMessageList: MutableLiveData<List<IMessageService.Message>> = MutableLiveData()

    override fun initData(savedInstanceState: Bundle?) {
        mChatSessionModel = DefChatSessionModelIml()
        mChatMessageModel = DefChatMessageModelIml()
        mFileModel = FileRepository(mChatMessageModel!!.mJob)
    }

    @SuppressLint("CheckResult")
    override fun initView() {
        if (mSessionId == null || mSessionId!! < 0) {
            ToastUtils.showToastLong(this, "聊天不存在")
            finish()
            return
        }

        RxPermissions(this).request(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
        ).subscribe({ pass ->
            if (!pass) {
                ToastUtils.showToastLong(mContext, "权限开启失败，无法使用此功能")
                finish()
            }
        }, {
            it.printStackTrace()
        })

        chat_message_send.setOnClickListener {
            val content = chat_message_edit.text.toString().trim()
            if (TextUtils.isEmpty(content)) {
                chat_message_edit.hint = "请输入内容"
            } else {

                sendMessage(
                        ChatContent().create(EnumChatMessageType.TEXT.contentType, content)
                                .put("realContent", content)
                )

                BaseUtils.closeKeyBord(mContext, chat_message_edit)
                chat_message_edit.setText("")
                chat_message_edit.clearFocus()
            }
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
            finish()
        }

        RxBus.toFlow(ChatMessageListFragment.ChatMessageListFragmentEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            if (
                                    this.isActive
                                    && it.session.sessionId == mSessionId
                                    && it.type.type > 0
                            ) {
                                notifyEvent(it)
                            }
                        }, {
                    it.printStackTrace()
                })

        getData()
    }

    override fun getData() {
        mChatSessionModel?.getSessionById(
                sessionId = mSessionId!!,
                failed = {
                    ToastUtils.showToast(this, it)
                },
                success = {
                    supportFragmentManager.beginTransaction()
                            .add(
                                    R.id.chat_message_frg,
                                    ChatMessageListFragment(it, DefMessageService(), mMessageList),
                                    "ChatMessageListFragment"
                            )
                            .commit()
                }
        )
    }

    override fun onDestroy() {
        mChatSessionModel?.clear()
        mChatMessageModel?.clear()
        super.onDestroy()
    }

    override fun onBackPressed() {
        ARouter.getInstance().build("/CHAT/MAIN")
                .withTransition(R.anim.activity_out_center, R.anim.activity_in_center)
                .navigation()
        super.onBackPressed()
    }

    override fun takeAudio() {
        BottomDialogUtils.showCustomerView(mContext, R.layout.audio_dialog, object : BottomDialog.ViewListener {
            @SuppressLint("CheckResult")
            override fun bindView(dialog: AbsBottomDialog) {
                AudioUtils.record(dialog).observeOn(AndroidSchedulers.mainThread()).subscribe({
                    val file = File(it.audioPath)
                    if (file.exists()) {
                        val fileEntity = HttpFileEntity(url = null, path = file.absolutePath, filename = file.name)
                        fileEntity.adjoin = it.recordTime
                        sendFileMessage(EnumChatMessageType.AUDIO, arrayListOf(
                                fileEntity
                        ))
                    } else {
                        ToastUtils.showToast(mContext, "文件不存在")
                    }
                }, {
                    it.printStackTrace()
                })
            }
        })
    }

    @SuppressLint("CheckResult")
    override fun takePhoto() {
        RxImagePicker.with(supportFragmentManager).takeImage(type = Sources.DEVICE)
                .flatMap {
                    RxImageConverters.uriToFileObservable(mContext, it, FileUtils.createImageFile())
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val file = HttpFileEntity(url = null, path = it.absolutePath, filename = it.name)
                    sendFileMessage(EnumChatMessageType.IMAGE, arrayListOf(file))
                }
    }

    @SuppressLint("CheckResult")
    override fun takeAlbum() {
        RxImagePicker.with(supportFragmentManager)
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
                    sendFileMessage(EnumChatMessageType.IMAGE, files)
                }
    }

    @SuppressLint("CheckResult")
    override fun takeVideo() {
        RxImagePicker.with(supportFragmentManager).takeVideo().flatMap {
            RxImageConverters.uriToFileObservable(mContext, it, FileUtils.createVideoFile())
        }.observeOn(AndroidSchedulers.mainThread()).subscribe {
            val file = HttpFileEntity(url = null, path = it.absolutePath, filename = it.name)
            sendFileMessage(EnumChatMessageType.VIDEO, arrayListOf(file))
        }
    }

    @SuppressLint("CheckResult")
    override fun takeFile() {
        RxImagePicker.with(supportFragmentManager).takeFiles(
                arrayOf("application/vnd.ms-powerpoint", "application/pdf", "application/msword")
        ).flatMap {
            val files = arrayListOf<File>()
            it.forEachIndexed { index, uri ->
                if (index < 2) {
                    RxImageConverters.uriToFile(mContext, uri, FileUtils.createFileByUri(mContext, uri))?.let { f ->
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
            sendFileMessage(EnumChatMessageType.FILE, files)
        }
    }

    override fun sendMessage(content: ChatContent) {
        RxBus.send(ChatMessageListFragment.ChatMessageListFragmentEvent(
                session = DefMessageService.DefSession(
                        sessionId = mSessionId!!
                ),
                type = ChatMessageListFragment.EnumEvent.SEND,
                data = content
        ))
    }

    override fun sendFileMessage(type: EnumChatMessageType, files: ArrayList<HttpFileEntity>) {
        files.forEach { file ->
            val content = ChatContent().create(type.contentType, file.filename ?: "文件")
                    .put("sessionName", file.filename)
                    .put("url", file.path)
            when (type) {
                EnumChatMessageType.AUDIO -> {
                    content.put("length", file.adjoin as Int)
                }
                else -> {
                }
            }

            sendMessage(content)

            file.adjoin = content.hashCode()
        }

        uploadFile(files)
    }

    override fun uploadFile(files: ArrayList<HttpFileEntity>) {
        if (!mMessageList.value.isNullOrEmpty() && files.isNotEmpty()) {
            val file = files[0]

            uploadFileMessage(file, MBaseChatMessageFile.UpLoadState.LOADING)

            mFileModel?.uploadFile(file, object : OnTListener<HttpFileEntity> {
                override fun back(t: HttpFileEntity) {
                    if (t.loadSuccess) {
                        LogUtil.i("上传文件成功>>>>>" + t.filename)

                        uploadFileMessage(t, MBaseChatMessageFile.UpLoadState.SUCCESS)

                        mChatMessageModel?.sendMessage(
                                message = t.adjoin as ChatMessageBo,
                                failed = { msg, result ->
                                    ToastUtils.showToast(mContext, msg)
                                    if (mActive) {
                                        notifySendMessage(result)
                                    } else {
                                        LogUtil.d("聊天界面", "页面隐藏，缓存数据")
                                        val list = mMessageUpdateCancel.value
                                        list?.add(result)
                                        mMessageUpdateCancel.postValue(list)
                                    }
                                },
                                success = { result ->
                                    if (mActive) {
                                        notifySendMessage(result)
                                        files.removeAt(0)
                                        if (files.isNotEmpty()) {
                                            uploadFile(files)
                                        }
                                    } else {
                                        LogUtil.d("聊天界面", "页面隐藏，缓存数据")
                                        val list = mMessageUpdateCancel.value
                                        list?.add(result)
                                        mMessageUpdateCancel.postValue(list)
                                    }
                                })
                    } else {
                        LogUtil.i("上传文件>>>>>" + t.progress)
                        if (t.progress == -1) {
                            ToastUtils.showToast(mContext, "上传失败")
                            val msg = t.adjoin as ChatMessageBo
                            msg.sendStatus = EnumChatSendStatus.FAILED
                            if (mActive) {
                                notifySendMessage(msg)
                            } else {
                                LogUtil.d("聊天界面", "页面隐藏，缓存数据")
                                val list = mMessageUpdateCancel.value
                                list?.add(msg)
                                mMessageUpdateCancel.postValue(list)
                            }
                        }
                    }
                }
            })
        }
    }

    private fun notifyEvent(event: ChatMessageListFragment.ChatMessageListFragmentEvent) {
        when (event.type) {
            ChatMessageListFragment.EnumEvent.CANCEL -> {
                if (event.data is String) {
                    chat_message_edit.setText(event.data)
                }
            }
            else -> {
            }
        }
    }
}
package qsos.base.chat.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_chat_message.*
import qsos.base.chat.DefMessageService
import qsos.base.chat.R
import qsos.base.chat.data.entity.ChatContent
import qsos.base.chat.data.entity.EnumChatMessageType
import qsos.base.chat.data.entity.EnumChatSendStatus
import qsos.base.chat.data.model.DefChatMessageModelIml
import qsos.base.chat.data.model.DefChatSessionModelIml
import qsos.base.chat.data.model.IChatModel
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
import qsos.lib.base.utils.DateUtils
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
@SuppressLint("CheckResult")
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
    private var mMessageService: IMessageService? = null
    private val mMessageList: MutableLiveData<List<IMessageService.Message>> = MutableLiveData()

    override fun initData(savedInstanceState: Bundle?) {
        mChatSessionModel = DefChatSessionModelIml()
        mChatMessageModel = DefChatMessageModelIml()
        mMessageService = DefMessageService()
        mFileModel = FileRepository(mChatMessageModel!!.mJob)
        mMessageList.value = arrayListOf()
    }

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
                                .put("content", content)
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

        mChatMessageModel?.mDataOfChatMessageList?.observe(this, Observer {
            if (it.code == 200) {
                mMessageList.postValue(it.data)
            } else {
                ToastUtils.showToast(this, it.msg ?: "消息获取失败")
            }
        })

        RxBus.toFlow(IMessageService.MessageReceiveEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    chat_message_edit.setText(it.message.content.getContentDesc())
                }

        getData()
    }

    override fun getData() {
        mChatSessionModel?.getSessionById(
                sessionId = mSessionId!!,
                failed = {
                    ToastUtils.showToast(this, it)
                },
                success = {
                    val fragment = ChatMessageListFragment(it, mMessageService!!, mMessageList)
                    supportFragmentManager.beginTransaction()
                            .add(R.id.chat_message_frg, fragment, "ChatMessageListFragment")
                            .commit()
                    mChatMessageModel?.getMessageListBySessionId(mSessionId!!)
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

    override fun takePhoto() {
        RxImagePicker.with(supportFragmentManager).takeImage(type = Sources.DEVICE)
                .flatMap {
                    RxImageConverters.uriToFileObservable(mContext, it, FileUtils.createImageFile())
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val file = HttpFileEntity(url = null, path = it.absolutePath, filename = it.name)
                    file.adjoin = it.length()
                    sendFileMessage(EnumChatMessageType.IMAGE, arrayListOf(file))
                }
    }

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
                        val file = HttpFileEntity(url = null, path = f.absolutePath, filename = f.name)
                        file.adjoin = f.length()
                        files.add(file)
                    }
                    sendFileMessage(EnumChatMessageType.IMAGE, files)
                }
    }

    override fun takeVideo() {
        RxImagePicker.with(supportFragmentManager).takeVideo().flatMap {
            RxImageConverters.uriToFileObservable(mContext, it, FileUtils.createVideoFile())
        }.observeOn(AndroidSchedulers.mainThread()).subscribe {
            val file = HttpFileEntity(url = null, path = it.absolutePath, filename = it.name)
            file.adjoin = it.length()
            sendFileMessage(EnumChatMessageType.VIDEO, arrayListOf(file))
        }
    }

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
                val file = HttpFileEntity(url = null, path = f.absolutePath, filename = f.name)
                file.adjoin = f.length()
                files.add(file)
            }
            sendFileMessage(EnumChatMessageType.FILE, files)
        }
    }

    override fun sendMessage(content: ChatContent): IMessageService.Message {
        val message = DefMessageService.DefMessage(
                sessionId = mSessionId!!,
                sendUserId = ChatMainActivity.mLoginUser.value!!.userId,
                sendUserName = ChatMainActivity.mLoginUser.value!!.userName,
                sendUserAvatar = ChatMainActivity.mLoginUser.value!!.avatar ?: "",
                timeline = content.hashCode() + Date().hours,
                content = content,
                createTime = DateUtils.getTimeToNow(Date())
        )

        RxBus.send(IMessageService.MessageSendEvent(
                session = DefMessageService.DefSession(sessionId = mSessionId!!),
                message = arrayListOf(message)
        ))

        return message
    }

    override fun sendFileMessage(type: EnumChatMessageType, files: ArrayList<HttpFileEntity>) {
        files.forEach { file ->
            val content = ChatContent().create(type.contentType, file.filename ?: "文件")
                    .put("name", file.filename)
                    .put("url", file.path)
                    .put("length", file.adjoin as Long?)
            file.adjoin = sendMessage(content)
        }

        uploadFile(files)
    }

    override fun uploadFile(files: ArrayList<HttpFileEntity>) {
        if (files.isNotEmpty()) {
            val file = files[0]
            val message = file.adjoin as IMessageService.Message

            message.sendStatus = EnumChatSendStatus.SENDING
            sendFileMessageUpdate(message)

            mFileModel?.uploadFile(file, object : OnTListener<HttpFileEntity> {
                override fun back(t: HttpFileEntity) {
                    if (t.loadSuccess) {
                        LogUtil.i("上传文件成功>>>>>" + t.filename)
                        message.sendStatus = EnumChatSendStatus.SUCCESS
                        sendFileMessageUpdate(message)

                        /**【递归】移除当前已上传文件，传递下一文件*/
                        files.removeAt(0)
                        uploadFile(files)
                    } else {
                        LogUtil.i("上传文件>>>>>" + t.progress)
                        if (t.progress == -1) {
                            ToastUtils.showToast(mContext, "上传失败")
                            message.sendStatus = EnumChatSendStatus.FAILED
                            sendFileMessageUpdate(message)
                        }
                    }
                }
            })
        }
    }

    override fun receiveEvent(type: Int, data: String) {
        when (type) {
            ChatMessageListFragment.EnumEvent.CANCEL.type -> {
                chat_message_edit.setText(data)
            }
        }
    }

    override fun sendFileMessageUpdate(message: IMessageService.Message) {
        RxBus.send(IMessageService.MessageUpdateFileEvent(
                session = DefMessageService.DefSession(sessionId = mSessionId!!),
                message = message
        ))
    }
}
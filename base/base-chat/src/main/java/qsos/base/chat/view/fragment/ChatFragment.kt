package qsos.base.chat.view.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_chat_message.*
import qsos.base.chat.R
import qsos.base.chat.data.entity.*
import qsos.base.chat.data.model.DefChatMessageModelIml
import qsos.base.chat.data.model.IChatModel
import qsos.base.chat.utils.AudioUtils
import qsos.base.chat.view.activity.ChatMainActivity
import qsos.base.chat.view.adapter.ChatMessageAdapter
import qsos.core.file.RxImageConverters
import qsos.core.file.RxImagePicker
import qsos.core.file.Sources
import qsos.core.lib.utils.dialog.AbsBottomDialog
import qsos.core.lib.utils.dialog.BottomDialog
import qsos.core.lib.utils.dialog.BottomDialogUtils
import qsos.core.lib.utils.file.FileUtils
import qsos.lib.base.base.fragment.BaseFragment
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
class ChatFragment(
        private val mSession: ChatSession,
        override val layoutId: Int = R.layout.fragment_chat_message,
        override val reload: Boolean = false
) : BaseFragment(), IChatFragment {

    private var mChatMessageModel: IChatModel.IMessage? = null
    private var mFileModel: IFileModel? = null
    private var mLinearLayoutManager: LinearLayoutManager? = null
    private var mMessageAdapter: ChatMessageAdapter? = null
    private val mMessageData: MutableLiveData<ArrayList<MChatMessage>> = MutableLiveData()

    override fun initData(savedInstanceState: Bundle?) {
        mChatMessageModel = DefChatMessageModelIml()
        mFileModel = FileRepository(mChatMessageModel!!.mJob)
        mMessageData.value = arrayListOf()
    }

    override fun initView(view: View) {

        mMessageAdapter = ChatMessageAdapter(mSession, mMessageData.value!!)

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

    override fun sendFileMessage(type: MChatMessageType, files: List<HttpFileEntity>) {
        files.forEach { file ->
            val map = HashMap<String, Any?>()
            map["contentType"] = type.contentType
            map["contentDesc"] = "文件"
            map["name"] = file.filename
            map["url"] = file.path
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

        uploadFile(0, files)
    }

    override fun notifySendMessage(result: MChatMessage) {
        mMessageData.value?.find {
            it.hashCode == result.hashCode
        }?.sendStatus = result.sendStatus
        mMessageAdapter?.notifyDataSetChanged()
    }

    override fun takeAudio() {
        BottomDialogUtils.showCustomerView(mContext, R.layout.audio_dialog, object : BottomDialog.ViewListener {
            override fun bindView(dialog: AbsBottomDialog) {
                AudioUtils.record(dialog).subscribe({
                    val file = File(it)
                    if (file.exists()) {
                        sendFileMessage(MChatMessageType.AUDIO, arrayListOf(
                                HttpFileEntity(url = null, path = file.absolutePath, filename = file.name)
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
        RxImagePicker.with((context as FragmentActivity).supportFragmentManager).takeFiles(arrayOf("image/*"))
                .flatMap {
                    val files = arrayListOf<File>()
                    it.forEachIndexed { index, uri ->
                        if (index < 9) {
                            RxImageConverters.uriToFile(mContext, uri, FileUtils.createImageFile())?.let { f ->
                                files.add(f)
                            }
                        }
                    }
                    Observable.just(files)
                }
                .subscribe {
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
        RxImagePicker.with((context as FragmentActivity).supportFragmentManager).takeVideo()
                .flatMap {
                    RxImageConverters.uriToFileObservable(mContext, it, FileUtils.createVideoFile())
                }
                .subscribe {
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

    override fun uploadFile(index: Int, files: List<HttpFileEntity>) {
        mFileModel?.uploadFile(files[index], object : OnTListener<HttpFileEntity> {
            override fun back(t: HttpFileEntity) {
                if (t.loadSuccess) {
                    val message = t.adjoin as MChatMessage
                    message.message.content.fields["url"] = t.url
                    mChatMessageModel?.sendMessage(
                            message = message,
                            failed = { msg, result ->
                                ToastUtils.showToast(mContext, msg)
                                notifySendMessage(result)
                            },
                            success = { result ->
                                notifySendMessage(result)
                                if (index < files.size) {
                                    uploadFile(index + 1, files)
                                }
                            })
                } else {
                    LogUtil.i("上传文件>>>>>" + t.progress)
                }
            }

        })
    }
}
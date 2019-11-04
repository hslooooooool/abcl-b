package qsos.base.chat.view.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import kotlinx.android.synthetic.main.fragment_chat_message.*
import qsos.base.chat.R
import qsos.base.chat.data.entity.*
import qsos.base.chat.data.model.DefChatMessageModelIml
import qsos.base.chat.data.model.IChatModel
import qsos.base.chat.view.activity.ChatMainActivity
import qsos.base.chat.view.adapter.ChatMessageAdapter
import qsos.core.file.RxImageConverters
import qsos.core.file.RxImagePicker
import qsos.core.file.Sources
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
        chat_message_file.setOnClickListener {
            takeImage()
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
                    createTime = DateUtils.setTimeFormat(Date()),
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

    override fun sendImageMessage(images: List<HttpFileEntity>) {
        val map = HashMap<String, Any?>()
        map["contentType"] = MChatMessageType.IMAGE.contentType
        map["contentDesc"] = "图片"
        map["name"] = images[0].filename
        map["url"] = images[0].path
        val message = MChatMessage(
                user = ChatMainActivity.mLoginUser.value!!,
                createTime = DateUtils.setTimeFormat(Date()),
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

        mFileModel?.uploadFile(images[0], object : OnTListener<HttpFileEntity> {
            override fun back(t: HttpFileEntity) {
                if (t.loadSuccess) {
                    map["url"] = t.url
                    mChatMessageModel?.sendMessage(
                            message = message,
                            failed = { msg, result ->
                                ToastUtils.showToast(mContext, msg)
                                notifySendMessage(result)
                            },
                            success = { result ->
                                notifySendMessage(result)
                            })
                } else {
                    LogUtil.i("上传图片>>>>>" + t.progress)
                }
            }
        })
    }

    override fun notifySendMessage(result: MChatMessage) {
        mMessageData.value?.find {
            it.hashCode == result.hashCode
        }?.sendStatus = result.sendStatus
        mMessageAdapter?.notifyDataSetChanged()
    }

    override fun takeImage() {
        RxImagePicker.with((context as FragmentActivity).supportFragmentManager).takeImage(Sources.ONE)
                .flatMap {
                    RxImageConverters.uriToFileObservable(mContext, it, FileUtils.createImageFile())
                }
                .subscribe {
                    val file = HttpFileEntity(url = null, path = it.absolutePath, filename = it.name)
                    sendImageMessage(arrayListOf(
                            file
                    ))
                }.takeUnless {
                    activity!!.isFinishing
                }
    }
}
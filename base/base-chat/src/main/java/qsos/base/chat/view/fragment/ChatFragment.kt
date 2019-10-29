package qsos.base.chat.view.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_chat_message.*
import qsos.base.chat.R
import qsos.base.chat.data.entity.*
import qsos.base.chat.data.model.DefChatMessageModelIml
import qsos.base.chat.data.model.IChatModel
import qsos.base.chat.view.activity.ChatMainActivity
import qsos.base.chat.view.adapter.ChatMessageAdapter
import qsos.lib.base.base.fragment.BaseFragment
import qsos.lib.base.utils.BaseUtils
import qsos.lib.base.utils.ToastUtils

/**
 * @author : 华清松
 * 聊天页面
 */
class ChatFragment(
        private val mSession: ChatSession,
        override val layoutId: Int = R.layout.fragment_chat_message,
        override val reload: Boolean = false
) : BaseFragment() {

    private var mChatMessageModel: IChatModel.IMessage? = null
    private var mMessageAdapter: ChatMessageAdapter? = null
    private val mMessageData: MutableLiveData<ArrayList<MChatMessage>> = MutableLiveData()

    override fun initData(savedInstanceState: Bundle?) {
        mChatMessageModel = DefChatMessageModelIml()
        mMessageData.value = arrayListOf()
    }

    override fun initView(view: View) {

        mMessageAdapter = ChatMessageAdapter(mSession, mMessageData.value!!)

        val mLinearLayoutManager = LinearLayoutManager(mContext)
        mLinearLayoutManager.stackFromEnd = true
        mLinearLayoutManager.reverseLayout = true
        chat_message_list.layoutManager = mLinearLayoutManager
        chat_message_list.adapter = mMessageAdapter

        mChatMessageModel?.mDataOfChatMessageList?.observe(this, Observer {
            mMessageData.value?.clear()
            it.data?.let { messages ->
                mMessageData.value?.addAll(messages)
            }
            mMessageAdapter?.notifyDataSetChanged()
        })

        chat_message_send.setOnClickListener {
            val content = chat_message_edit.text.toString().trim()
            if (TextUtils.isEmpty(content)) {
                chat_message_edit.hint = "请输入内容"
            } else {
                val map = HashMap<String, Any?>()
                map["contentType"] = MChatMessageType.TEXT.contentType
                map["content"] = content
                val message = MChatMessage(
                        user = ChatMainActivity.mLoginUser.value!!,
                        createTime = System.currentTimeMillis(),
                        message = ChatMessage(
                                sessionId = mSession.sessionId,
                                content = ChatContent(
                                        fields = map
                                )
                        )
                )
                message.sendStatus = MChatSendStatus.SENDING
                message.readStatus = 0
                val hashCode = message.hashCode()
                message.hashCode = hashCode

                mMessageData.value?.add(0, message)
                mMessageAdapter?.notifyDataSetChanged()
                mChatMessageModel?.sendMessage(
                        message = message,
                        failed = { msg, result ->
                            ToastUtils.showToast(context, msg)
                            notifySendMessage(result)
                        },
                        success = { result ->
                            notifySendMessage(result)
                        }
                )

                /**发送后更新视图*/
                BaseUtils.closeKeyBord(it.context, chat_message_edit)
                chat_message_edit.setText("")
                chat_message_edit.clearFocus()
            }
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

    /**更新发送消息状态*/
    private fun notifySendMessage(result: MChatMessage) {
        mMessageData.value?.find {
            it.hashCode == result.hashCode
        }?.sendStatus = result.sendStatus
        mMessageAdapter?.notifyDataSetChanged()
    }
}
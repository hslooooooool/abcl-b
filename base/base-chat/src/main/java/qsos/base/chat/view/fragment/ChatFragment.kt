package qsos.base.chat.view.fragment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_chat_message.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import qsos.base.chat.R
import qsos.base.chat.data.entity.MChatMessage
import qsos.base.chat.data.model.DefChatModelIml
import qsos.base.chat.data.model.IChatModelConfig
import qsos.base.chat.view.adapter.ChatMessageAdapter
import qsos.lib.base.base.fragment.BaseFragment
import qsos.lib.base.utils.ToastUtils
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 * 聊天页面
 */
class ChatFragment(
        override val layoutId: Int = R.layout.fragment_chat_message,
        override val reload: Boolean = false
) : BaseFragment() {

    val mJob: CoroutineContext = Dispatchers.Main + Job()

    private var mChatMessageModel: IChatModelConfig? = null
    private var mMessageAdapter: ChatMessageAdapter? = null
    private val mMessageData: MutableLiveData<ArrayList<MChatMessage>> = MutableLiveData()

    override fun getData() {
        mChatMessageModel?.getMessageListBySessionId(sessionId = 1)
    }

    override fun initData(savedInstanceState: Bundle?) {
        mChatMessageModel = DefChatModelIml()
        mMessageData.value = arrayListOf()
    }

    override fun initView(view: View) {

        mMessageAdapter = ChatMessageAdapter(mMessageData.value!!)

        chat_message_list.layoutManager = LinearLayoutManager(mContext)
        chat_message_list.adapter = mMessageAdapter

        mChatMessageModel?.mDataOfChatMessageList?.observe(this, Observer {
            ToastUtils.showToast(mContext, it.msg ?: "数据变动")
            mMessageData.value?.clear()
            it.data?.let { messages ->
                mMessageData.value?.addAll(messages)
            }
            mMessageAdapter?.notifyDataSetChanged()
        })

        getData()
    }

    override fun onDestroy() {
        mJob.cancel()
        super.onDestroy()
    }
}
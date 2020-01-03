package vip.qsos.app_chat.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.google.gson.Gson
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_chat_message.*
import kotlinx.android.synthetic.main.item_chat_friend.view.*
import qsos.base.chat.api.IMessageListService
import qsos.base.chat.data.entity.*
import qsos.core.lib.utils.image.ImageLoaderUtils
import qsos.lib.base.base.activity.BaseActivity
import qsos.lib.base.base.adapter.BaseAdapter
import qsos.lib.base.base.adapter.BaseNormalAdapter
import qsos.lib.base.callback.OnListItemClickListener
import qsos.lib.base.callback.OnTListener
import qsos.lib.base.utils.BaseUtils
import qsos.lib.base.utils.DateUtils
import qsos.lib.base.utils.LogUtil
import qsos.lib.base.utils.ToastUtils
import qsos.lib.base.utils.rx.RxBus
import qsos.lib.netservice.file.FileRepository
import qsos.lib.netservice.file.HttpFileEntity
import qsos.lib.netservice.file.IFileModel
import vip.qsos.app_chat.R
import vip.qsos.app_chat.data.entity.ChatGroup
import vip.qsos.app_chat.data.entity.ChatMessage
import vip.qsos.app_chat.data.entity.ChatMessageBo
import vip.qsos.app_chat.data.entity.ChatUser
import vip.qsos.app_chat.data.model.*
import java.util.*
import kotlin.concurrent.timerTask

/**
 * @author : 华清松
 * 聊天会话页面
 */
@SuppressLint("CheckResult", "SetTextI18n")
@Route(group = "CHAT", path = "/CHAT/SESSION")
class ChatSessionActivity(
        override val layoutId: Int = R.layout.activity_chat_message,
        override val reload: Boolean = false
) : BaseActivity(), IChatSessionView {

    @Autowired(name = "/CHAT/GROUP_ID")
    @JvmField
    var mGroupId: Long? = -1L

    private lateinit var mTitle: TextView
    private lateinit var mMenu: TextView

    private lateinit var mChatSessionModel: ChatModel.ISession
    private lateinit var mFileModel: IFileModel
    private lateinit var mChatMessageModel: ChatModel.IMessage
    private lateinit var mMessageListService: IMessageListService
    private val mMessageGroup: MutableLiveData<ChatGroup> = MutableLiveData()
    private lateinit var mChatUserModel: ChatModel.IUser
    private lateinit var mChatGroupModel: ChatModel.IGroup
    private lateinit var mChatUserAdapter: BaseAdapter<ChatUser>
    private val mChatUserList = arrayListOf<ChatUser>()
    private var mPullMessageTimer: Timer? = null

    private lateinit var mModel: ChatSessionModel

    override fun initData(savedInstanceState: Bundle?) {
        mModel = ChatSessionModelImpl(this)
        mChatSessionModel = ChatSessionModelIml()
        mChatMessageModel = ChatMessageModelIml()
        mMessageListService = MessageListService()
        mChatUserModel = ChatUserModelIml()
        mChatGroupModel = ChatGroupModelIml()
        mFileModel = FileRepository(mChatMessageModel.mJob)

        lifecycle.addObserver(mModel)
    }

    override fun initView() {
        if (mGroupId == null || mGroupId!! < 0) {
            ToastUtils.showToastLong(this, "聊天不存在")
            finish()
            return
        }

        RxPermissions(this).request(
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
        ).subscribe({ pass ->
            if (!pass) {
                ToastUtils.showToastLong(mContext, "权限开启失败，无法使用此功能")
                finish()
            }
        }, {
            it.printStackTrace()
        })

        mTitle = base_title_bar.findViewById(R.id.base_title_bar_title)
        mMenu = base_title_bar.findViewById(R.id.base_title_bar_menu_more)
        mMenu.visibility = View.VISIBLE
        mMenu.setOnClickListener {
            chat_message_draw.openDrawer(GravityCompat.END)
        }

        chat_message_srl.setColorSchemeResources(R.color.colorPrimary, R.color.black, R.color.green)
        chat_message_srl.setOnRefreshListener {
            mChatMessageModel.getOldMessageBySessionId(mGroupId!!) {
                chat_message_srl.isRefreshing = false
                if (it.isNotEmpty()) {
                    RxBus.send(IMessageListService.MessageEvent(
                            group = mMessageGroup.value!!,
                            message = it,
                            eventType = IMessageListService.EventType.SHOW_MORE
                    ))
                } else {
                    ToastUtils.showToast(this, "已经到顶")
                }
            }
        }
        chat_message_edit.setOnTouchListener { _, _ ->
            chat_message_rv.scrollToBottom()
            return@setOnTouchListener false
        }
        chat_message_edit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                changeSendStyle(s?.length ?: 0)
            }
        })
        chat_message_send.setOnClickListener {
            val content = chat_message_edit.text.toString().trim()
            if (TextUtils.isEmpty(content)) {
                chat_message_edit.hint = "请输入内容"
                BaseUtils.hideKeyboard(this)
            } else {
                sendMessage(ChatContent().create(EnumChatMessageType.TEXT.contentType, content)
                        .put("content", content), send = true, bottom = true)
                chat_message_edit.setText("")
            }
        }
        chat_message_camera.setOnClickListener {
            takeFile(0)
        }
        chat_message_album.setOnClickListener {
            takeFile(1)
        }
        chat_message_video.setOnClickListener {
            takeFile(2)
        }
        chat_message_voice.setOnClickListener {
            takeFile(3)
        }
        chat_message_file.setOnClickListener {
            takeFile(4)
        }

        base_title_bar.apply {
            this.findViewById<TextView>(R.id.base_title_bar_title)?.text = ""
            this.findViewById<View>(R.id.base_title_bar_icon_left)?.setOnClickListener {
                ARouter.getInstance().build("/APP/MAIN")
                        .withTransition(R.anim.activity_out_center, R.anim.activity_in_center)
                        .navigation()
                finish()
            }
        }

        /**当前所有用户列表，用于添加用户入群*/
        mChatUserAdapter = BaseNormalAdapter(R.layout.item_chat_friend, mChatUserList) { holder, data, _ ->
            ImageLoaderUtils.display(mContext, holder.itemView.item_chat_friend_avatar, data.avatar)
            holder.itemView.item_chat_friend_state.visibility = View.GONE
            holder.itemView.item_chat_friend_name.text = data.name
            holder.itemView.setOnClickListener {
                mChatSessionModel.addUserListToSession(arrayListOf(data.userId), mGroupId!!,
                        failed = {
                            ToastUtils.showToast(this, it)
                        },
                        success = {
                            if (mGroupId == it.id) {
                                ToastUtils.showToast(this, "已添加")
                            } else {
                                /**单聊变群聊，切换到新群聊天*/
                                ARouter.getInstance().build("/CHAT/SESSION")
                                        .withLong("/CHAT/GROUP_ID", it.id)
                                        .navigation()
                            }
                        }
                )
            }
        }
        val mLinearLayoutManager = LinearLayoutManager(mContext)
        chat_message_friends.layoutManager = mLinearLayoutManager
        chat_message_friends.adapter = mChatUserAdapter

        mChatUserModel.mDataOfChatUserList.observe(this, Observer {
            mChatUserList.clear()
            it.data?.let { users ->
                mChatUserList.addAll(users)
            }
            mChatUserAdapter.notifyDataSetChanged()
        })

        getData()
    }

    override fun getData() {
        mChatUserModel.getAllChatUser()

        mChatSessionModel.getGroupById(
                groupId = mGroupId!!,
                failed = {
                    ToastUtils.showToast(this, it)
                },
                success = {
                    mMessageGroup.postValue(it)
                    mChatGroupModel.getGroupById(mGroupId!!) { group ->
                        chat_message_group_info.text = Gson().toJson(group)
                        mTitle.text = group.name
                    }

                    chat_message_rv.initView(
                            group = it, messageListService = mMessageListService,
                            itemClickListener = object : OnListItemClickListener {
                                override fun onItemClick(view: View, position: Int, obj: Any?) {
                                    preOnItemClick(view, obj)
                                }

                                override fun onItemLongClick(view: View, position: Int, obj: Any?) {
                                    preOnItemLongClick(view, obj)
                                }
                            },
                            newMessageNumLimit = 4, lifecycleOwner = this,
                            readNumListener = object : OnTListener<Int> {
                                override fun back(t: Int) {
                                    if (t == 0) {
                                        chat_message_new_message_num.visibility = View.GONE
                                    } else {
                                        chat_message_new_message_num.visibility = View.VISIBLE
                                        chat_message_new_message_num.text = "有${t}条新消息"
                                    }
                                }
                            })
                    chat_message_new_message_num.setOnClickListener {
                        chat_message_rv.scrollToBottom()
                    }
                    pullNewMessage(it)
                }
        )
    }

    override fun onDestroy() {
        mChatSessionModel.clear()
        mChatMessageModel.clear()
        mChatUserModel.clear()
        mChatGroupModel.clear()
        mPullMessageTimer?.cancel()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (!checkHaveSending()) {
            goToHome()
            super.onBackPressed()
        } else {
            val build = AlertDialog.Builder(this)
            build.setTitle("友情提示")
            build.setMessage("您有正在发送消息，是否等待发送完成？退出有可能导致消息发送失败。")
            build.setNegativeButton("退出") { dialog, _ ->
                dialog.dismiss()
                goToHome()
                super.onBackPressed()
            }
            build.setPositiveButton("等待发送") { dialog, _ ->
                dialog.dismiss()
            }
            build.create().show()
        }
    }

    override fun deleteMessage(message: IMessageListService.Message) {
        mMessageListService.revokeMessage(message,
                failed = { msg, _ ->
                    ToastUtils.showToast(mContext, msg)
                },
                success = {
                    it.sendStatus = EnumChatSendStatus.CANCEL_CAN
                    RxBus.send(IMessageListService.MessageEvent(
                            group = mMessageGroup.value!!,
                            message = arrayListOf(it),
                            eventType = IMessageListService.EventType.UPDATE_SHOWED
                    ))
                })
    }

    override fun sendMessage(content: ChatContent, send: Boolean, bottom: Boolean): IMessageListService.Message {
        val message = ChatMessageBo(
                user = ChatModel.mLoginUser.value!!,
                createTime = DateUtils.format(date = Date()),
                message = ChatMessage(
                        sessionId = mGroupId!!,
                        messageId = UUID.randomUUID().hashCode(),
                        content = content
                )
        )

        RxBus.send(IMessageListService.MessageEvent(
                group = mMessageGroup.value!!,
                message = arrayListOf(message),
                eventType = IMessageListService.EventType.SEND
        ))

        return message
    }

    override fun takeFile(fileType: Int) {
        clearEditFocus()
        mModel.sendFileMessage(mContext, supportFragmentManager, fileType) { type, files ->
            sendFileMessage(type, files)
        }
    }

    override fun sendFileMessage(type: EnumChatMessageType, files: ArrayList<HttpFileEntity>) {
        files.forEach { file ->
            val content = ChatContent().create(type.contentType, file.filename ?: "文件")
                    .put("name", file.filename)
                    .put("url", file.path)
                    .put("length", file.adjoin as Long?)
            val message = ChatMessageBo(
                    user = ChatModel.mLoginUser.value!!,
                    createTime = DateUtils.format(date = Date()),
                    message = ChatMessage(
                            sessionId = mGroupId!!,
                            messageId = UUID.randomUUID().hashCode(),
                            content = content
                    )
            )

            RxBus.send(IMessageListService.MessageEvent(
                    group = mMessageGroup.value!!,
                    message = arrayListOf(message),
                    eventType = IMessageListService.EventType.SHOW
            ))

            file.adjoin = message
        }

        uploadFile(files)
    }

    override fun uploadFile(files: ArrayList<HttpFileEntity>) {
        if (files.isNotEmpty()) {
            val file = files[0]
            val message = file.adjoin as IMessageListService.Message

            message.sendStatus = EnumChatSendStatus.SENDING

            RxBus.send(IMessageListService.MessageEvent(
                    group = mMessageGroup.value!!,
                    message = arrayListOf(message),
                    eventType = IMessageListService.EventType.UPDATE_SHOWED
            ))

            mFileModel.uploadFile(file, object : OnTListener<HttpFileEntity> {
                override fun back(t: HttpFileEntity) {
                    if (t.loadSuccess) {
                        LogUtil.i("上传文件成功>>>>>" + t.filename)
                        message.sendStatus = EnumChatSendStatus.SUCCESS
                        message.content.put("url", file.url)
                        message.content.put("avatar", file.avatar)

                        RxBus.send(IMessageListService.MessageEvent(
                                group = mMessageGroup.value!!,
                                message = arrayListOf(message),
                                eventType = IMessageListService.EventType.SEND_SHOWED
                        ))

                        /**【递归】移除当前已上传文件，传递下一文件*/
                        files.removeAt(0)
                        uploadFile(files)
                    } else {
                        LogUtil.i("上传文件>>>>>" + t.progress)
                        if (t.progress == -1) {
                            ToastUtils.showToast(mContext, "上传失败")
                            message.sendStatus = EnumChatSendStatus.FAILED

                            RxBus.send(IMessageListService.MessageEvent(
                                    group = mMessageGroup.value!!,
                                    message = arrayListOf(message),
                                    eventType = IMessageListService.EventType.UPDATE_SHOWED
                            ))
                        }
                    }
                }
            })
        }
    }

    override fun pullNewMessage(group: IMessageListService.Group) {
        /**TODO 往后走Socket*/
        mPullMessageTimer?.cancel()
        mPullMessageTimer = Timer()
        mPullMessageTimer!!.schedule(timerTask {
            mChatMessageModel.getNewMessageBySessionId(mGroupId!!) {
                if (it.isNotEmpty()) {
                    RxBus.send(IMessageListService.MessageEvent(
                            group = mMessageGroup.value!!,
                            message = it,
                            eventType = IMessageListService.EventType.SHOW_NEW)
                    )
                }
            }
        }, 2000L, 500L)
    }

    override fun checkHaveSending(): Boolean {
        var haveSending = false
        chat_message_rv.getMessageList().forEach {
            if (it.sendStatus == EnumChatSendStatus.SENDING) {
                haveSending = true
            }
        }
        return haveSending
    }

    override fun goToHome() {
        ARouter.getInstance().build("/APP/MAIN")
                .withTransition(R.anim.activity_out_center, R.anim.activity_in_center)
                .navigation()
        finish()
    }

    override fun clearEditFocus() {
        chat_message_edit.clearFocus()
        BaseUtils.hideKeyboard(this)
    }

    override fun changeSendStyle(inputNum: Int) {
        if (inputNum > 0) {
            chat_message_send.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
        } else {
            chat_message_send.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))
        }
    }

    /**列表项点击*/
    private fun preOnItemClick(view: View, obj: Any?) {
        when (view.id) {
            R.id.item_message_text -> {
                if (obj != null && obj is IMessageListService.Message) {
                    mModel.clickTextMessage(view, obj) {
                        when (it) {
                            R.id.menu_message_citations -> {
                                chat_message_edit.append(obj.content.getContent())
                            }
                            else -> {
                            }
                        }
                    }
                }
            }
            R.id.item_message_state -> {
                if (obj is IMessageListService.Message) {
                    mModel.resendMessage(obj) {
                        if (it == null) {
                            RxBus.send(IMessageListService.MessageEvent(
                                    group = mMessageGroup.value!!,
                                    message = arrayListOf(obj),
                                    eventType = IMessageListService.EventType.SEND_SHOWED
                            ))
                        } else {
                            uploadFile(arrayListOf(it))
                        }
                    }
                }
            }
            R.id.item_message_view_audio -> {
                if (obj is IMessageListService.Message) {
                    obj.getRealContent<MChatMessageAudio>()?.let {
                        mModel.playAudio(view, view.findViewById(R.id.item_message_audio_state), it)
                    }
                }
            }
            R.id.item_message_cancel_reedit -> {
                if (
                        obj != null && obj is IMessageListService.Message
                        && obj.content.getContentType() == EnumChatMessageType.TEXT.contentType
                ) {
                    obj.getRealContent<MChatMessageText>()?.let {
                        obj.sendStatus = EnumChatSendStatus.CANCEL_OK
                        RxBus.send(IMessageListService.MessageEvent(
                                group = mMessageGroup.value!!,
                                message = arrayListOf(obj),
                                eventType = IMessageListService.EventType.UPDATE_SHOWED
                        ))

                        chat_message_edit.setText(obj.content.getContentDesc())
                    }
                }
            }
        }
    }

    /**列表项长按*/
    private fun preOnItemLongClick(view: View, obj: Any?) {
        when (view.id) {
            R.id.item_message_text -> {
                if (obj != null && obj is IMessageListService.Message) {
                    mModel.longClickTextMessage(view, obj) {
                        when (it) {
                            R.id.menu_message_cancel -> {
                                deleteMessage(obj)
                            }
                            R.id.menu_message_reply -> {
                                chat_message_edit.setText("回复(${obj.sendUserName})：")
                            }
                            R.id.menu_message_copy -> {
                                val mClipData = ClipData.newPlainText(
                                        applicationInfo.nonLocalizedLabel, obj.content.getContent()
                                )
                                (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).primaryClip = mClipData
                            }
                            else -> {
                            }
                        }
                    }
                }
            }
        }
    }

}
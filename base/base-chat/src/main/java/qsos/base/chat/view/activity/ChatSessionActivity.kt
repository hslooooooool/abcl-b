package qsos.base.chat.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Point
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.google.gson.Gson
import com.noober.menu.FloatMenu
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_chat_message.*
import kotlinx.android.synthetic.main.item_chat_friend.view.*
import kotlinx.android.synthetic.main.item_message_audio.view.*
import qsos.base.chat.R
import qsos.base.chat.data.entity.*
import qsos.base.chat.data.model.*
import qsos.base.chat.service.DefMessageService
import qsos.base.chat.service.IMessageService
import qsos.base.chat.utils.AudioUtils
import qsos.core.file.RxImageConverters
import qsos.core.file.RxImagePicker
import qsos.core.file.Sources
import qsos.core.lib.utils.dialog.AbsBottomDialog
import qsos.core.lib.utils.dialog.BottomDialog
import qsos.core.lib.utils.dialog.BottomDialogUtils
import qsos.core.lib.utils.file.FileUtils
import qsos.core.lib.utils.image.ImageLoaderUtils
import qsos.core.player.PlayerConfigHelper
import qsos.core.player.audio.AudioPlayerHelper
import qsos.core.player.data.PreAudioEntity
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
import java.io.File
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

    @Autowired(name = "/CHAT/SESSION_ID")
    @JvmField
    var mSessionId: Int? = -1

    private lateinit var mTitle: TextView
    private lateinit var mMenu: TextView

    private var mChatSessionModel: IChatModel.ISession? = null
    private var mFileModel: IFileModel? = null
    private var mChatMessageModel: IChatModel.IMessage? = null
    private var mMessageService: IMessageService? = null
    private val mMessageSession: MutableLiveData<ChatSession> = MutableLiveData()
    private var mChatUserModel: IChatModel.IUser? = null
    private var mChatGroupModel: IChatModel.IGroup? = null
    private var mChatUserAdapter: BaseAdapter<ChatUser>? = null
    private val mChatUserList = arrayListOf<ChatUser>()
    private var mPullMessageTimer: Timer? = null
    private val mPlayList: HashMap<String, AudioPlayerHelper?> = HashMap()

    override fun initData(savedInstanceState: Bundle?) {
        mChatSessionModel = DefChatSessionModelIml()
        mChatMessageModel = DefChatMessageModelIml()
        mMessageService = DefMessageService()
        mChatUserModel = DefChatUserModelIml()
        mChatGroupModel = DefChatGroupModelIml()
        mFileModel = FileRepository(mChatMessageModel!!.mJob)
    }

    override fun initView() {
        if (mSessionId == null || mSessionId!! < 0) {
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
            mChatMessageModel?.getOldMessageBySessionId(mSessionId!!) {
                chat_message_srl.isRefreshing = false
                if (it.isNotEmpty()) {
                    RxBus.send(IMessageService.MessageEvent(
                            session = mMessageSession.value!!,
                            message = it,
                            eventType = IMessageService.EventType.SHOW_MORE
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
            takePhoto()
        }
        chat_message_album.setOnClickListener {
            takeAlbum()
        }
        chat_message_voice.setOnClickListener {
            takeVoice()
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

        /**当前所有用户列表，用于添加用户入群*/
        mChatUserAdapter = BaseNormalAdapter(R.layout.item_chat_friend, mChatUserList) { holder, data, _ ->
            ImageLoaderUtils.display(mContext, holder.itemView.item_chat_friend_avatar, data.avatar)
            holder.itemView.item_chat_friend_state.visibility = View.GONE
            holder.itemView.item_chat_friend_name.text = data.userName
            holder.itemView.setOnClickListener {
                mChatSessionModel?.addUserListToSession(arrayListOf(data.userId), mSessionId!!,
                        failed = {
                            ToastUtils.showToast(this, it)
                        },
                        success = {
                            if (mSessionId == it.sessionId) {
                                ToastUtils.showToast(this, "已添加")
                            } else {
                                /**单聊变群聊，切换到新群聊天*/
                                ARouter.getInstance().build("/CHAT/SESSION")
                                        .withInt("/CHAT/SESSION_ID", it.sessionId)
                                        .navigation()
                            }
                        }
                )
            }
        }
        val mLinearLayoutManager = LinearLayoutManager(mContext)
        chat_message_friends.layoutManager = mLinearLayoutManager
        chat_message_friends.adapter = mChatUserAdapter

        mChatUserModel?.mDataOfChatUserList?.observe(this, Observer {
            mChatUserList.clear()
            it.data?.let { users ->
                mChatUserList.addAll(users)
            }
            mChatUserAdapter?.notifyDataSetChanged()
        })

        getData()
    }

    override fun getData() {
        mChatUserModel?.getAllChatUser()

        mChatSessionModel?.getSessionById(
                sessionId = mSessionId!!,
                failed = {
                    ToastUtils.showToast(this, it)
                },
                success = {
                    mMessageSession.postValue(it)
                    mChatGroupModel?.getGroupById(mSessionId!!) { group ->
                        chat_message_group_info.text = Gson().toJson(group)
                        mTitle.text = group.name
                    }

                    chat_message_rv.initView(
                            session = it, messageService = mMessageService!!,
                            itemClickListener = mOnListItemClickListener,
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

    override fun onPause() {
        stopAudioPlay()
        super.onPause()
    }

    override fun onDestroy() {
        mChatSessionModel?.clear()
        mChatMessageModel?.clear()
        mChatUserModel?.clear()
        mChatGroupModel?.clear()
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

    override fun takeVoice() {
        clearEditFocus()
        BottomDialogUtils.showCustomerView(mContext, R.layout.audio_dialog, object : BottomDialog.ViewListener {
            override fun bindView(dialog: AbsBottomDialog) {
                AudioUtils.record(dialog).observeOn(AndroidSchedulers.mainThread()).subscribe({
                    val file = File(it.audioPath)
                    if (file.exists()) {
                        val fileEntity = HttpFileEntity(url = null, path = file.absolutePath, filename = file.name)
                        fileEntity.adjoin = it.recordTime + 0L
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
        clearEditFocus()
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
        clearEditFocus()
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
        clearEditFocus()
        RxImagePicker.with(supportFragmentManager).takeVideo().flatMap {
            RxImageConverters.uriToFileObservable(mContext, it, FileUtils.createVideoFile())
        }.observeOn(AndroidSchedulers.mainThread()).subscribe {
            val file = HttpFileEntity(url = null, path = it.absolutePath, filename = it.name)
            file.adjoin = it.length()
            sendFileMessage(EnumChatMessageType.VIDEO, arrayListOf(file))
        }
    }

    override fun takeFile() {
        clearEditFocus()
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

    override fun stopAudioPlay() {
        mPlayList.values.forEach {
            it?.stop()
        }
    }

    override fun deleteMessage(message: IMessageService.Message) {
        mMessageService?.revokeMessage(message,
                failed = { msg, _ ->
                    ToastUtils.showToast(mContext, msg)
                },
                success = {
                    it.sendStatus = EnumChatSendStatus.CANCEL_CAN
                    RxBus.send(IMessageService.MessageEvent(
                            session = mMessageSession.value!!,
                            message = arrayListOf(it),
                            eventType = IMessageService.EventType.UPDATE_SHOWED
                    ))
                })
    }

    override fun sendMessage(content: ChatContent, send: Boolean, bottom: Boolean): IMessageService.Message {
        val message = ChatMessageBo(
                user = ChatMainActivity.mLoginUser.value!!,
                createTime = DateUtils.format(date = Date()),
                message = ChatMessage(
                        sessionId = mSessionId!!,
                        messageId = UUID.randomUUID().hashCode(),
                        content = content
                )
        )

        RxBus.send(IMessageService.MessageEvent(
                session = mMessageSession.value!!,
                message = arrayListOf(message),
                eventType = IMessageService.EventType.SEND
        ))

        return message
    }

    override fun sendFileMessage(type: EnumChatMessageType, files: ArrayList<HttpFileEntity>) {
        files.forEach { file ->
            val content = ChatContent().create(type.contentType, file.filename ?: "文件")
                    .put("name", file.filename)
                    .put("url", file.path)
                    .put("length", file.adjoin as Long?)
            val message = ChatMessageBo(
                    user = ChatMainActivity.mLoginUser.value!!,
                    createTime = DateUtils.format(date = Date()),
                    message = ChatMessage(
                            sessionId = mSessionId!!,
                            messageId = UUID.randomUUID().hashCode(),
                            content = content
                    )
            )

            RxBus.send(IMessageService.MessageEvent(
                    session = mMessageSession.value!!,
                    message = arrayListOf(message),
                    eventType = IMessageService.EventType.SHOW
            ))

            file.adjoin = message
        }

        uploadFile(files)
    }

    override fun uploadFile(files: ArrayList<HttpFileEntity>) {
        if (files.isNotEmpty()) {
            val file = files[0]
            val message = file.adjoin as IMessageService.Message

            message.sendStatus = EnumChatSendStatus.SENDING

            RxBus.send(IMessageService.MessageEvent(
                    session = mMessageSession.value!!,
                    message = arrayListOf(message),
                    eventType = IMessageService.EventType.UPDATE_SHOWED
            ))

            mFileModel?.uploadFile(file, object : OnTListener<HttpFileEntity> {
                override fun back(t: HttpFileEntity) {
                    if (t.loadSuccess) {
                        LogUtil.i("上传文件成功>>>>>" + t.filename)
                        message.sendStatus = EnumChatSendStatus.SUCCESS
                        message.content.put("url", file.url)

                        RxBus.send(IMessageService.MessageEvent(
                                session = mMessageSession.value!!,
                                message = arrayListOf(message),
                                eventType = IMessageService.EventType.SEND_SHOWED
                        ))

                        /**【递归】移除当前已上传文件，传递下一文件*/
                        files.removeAt(0)
                        uploadFile(files)
                    } else {
                        LogUtil.i("上传文件>>>>>" + t.progress)
                        if (t.progress == -1) {
                            ToastUtils.showToast(mContext, "上传失败")
                            message.sendStatus = EnumChatSendStatus.FAILED

                            RxBus.send(IMessageService.MessageEvent(
                                    session = mMessageSession.value!!,
                                    message = arrayListOf(message),
                                    eventType = IMessageService.EventType.UPDATE_SHOWED
                            ))
                        }
                    }
                }
            })
        }
    }

    override fun pullNewMessage(session: IMessageService.Session) {
        /**TODO 往后走Socket*/
        mPullMessageTimer?.cancel()
        mPullMessageTimer = Timer()
        mPullMessageTimer!!.schedule(timerTask {
            mChatMessageModel?.getNewMessageBySessionId(mSessionId!!) {
                if (it.isNotEmpty()) {
                    RxBus.send(IMessageService.MessageEvent(
                            session = mMessageSession.value!!,
                            message = it,
                            eventType = IMessageService.EventType.SHOW_NEW)
                    )
                }
            }
        }, 2000L, 500L)
    }

    /**检测是否有正在发送的消息，友情提示，防止退出后消息未发送*/
    private fun checkHaveSending(): Boolean {
        var haveSending = false
        chat_message_rv.getMessageList().forEach {
            if (it.sendStatus == EnumChatSendStatus.SENDING) {
                haveSending = true
            }
        }
        return haveSending
    }

    /**跳转到首页*/
    private fun goToHome() {
        ARouter.getInstance().build("/CHAT/MAIN")
                .withTransition(R.anim.activity_out_center, R.anim.activity_in_center)
                .navigation()
        finish()
    }

    /**列表项点击监听*/
    private val mOnListItemClickListener = object : OnListItemClickListener {
        override fun onItemClick(view: View, position: Int, obj: Any?) {
            preOnItemClick(view, position, obj)
        }

        override fun onItemLongClick(view: View, position: Int, obj: Any?) {
            preOnItemLongClick(view, position, obj)
        }
    }

    /**列表项点击*/
    private fun preOnItemClick(view: View, position: Int, obj: Any?) {
        when (view.id) {
            R.id.item_message_state -> {
                if (obj is IMessageService.Message) {
                    // TODO 判断消息类型，如果是文本，直接发送，如果是文件，判断上传是否成功，否-重新上传再发送
                    RxBus.send(IMessageService.MessageEvent(
                            session = mMessageSession.value!!,
                            message = arrayListOf(obj),
                            eventType = IMessageService.EventType.SEND_SHOWED
                    ))
                }
            }
            R.id.item_message_view_audio -> {
                if (obj is IMessageService.Message) {
                    obj.getRealContent<MChatMessageAudio>()?.let {
                        playAudio(view, it)
                    }
                }
            }
            R.id.item_message_cancel_reedit -> {
                if (obj != null && obj is IMessageService.Message && obj.content.getContentType() == EnumChatMessageType.TEXT.contentType) {
                    obj.getRealContent<MChatMessageText>()?.let {
                        obj.sendStatus = EnumChatSendStatus.CANCEL_OK
                        RxBus.send(IMessageService.MessageEvent(
                                session = mMessageSession.value!!,
                                message = arrayListOf(obj),
                                eventType = IMessageService.EventType.UPDATE_SHOWED
                        ))

                        chat_message_edit.setText(obj.content.getContentDesc())
                    }
                }
            }
        }
    }

    /**列表项长按*/
    private fun preOnItemLongClick(view: View, position: Int, obj: Any?) {
        when (view.id) {
            R.id.item_message_content -> {
                if (obj != null && obj is IMessageService.Message) {
                    val point = IntArray(2)
                    view.getLocationOnScreen(point)
                    val floatMenu = FloatMenu(this)
                    floatMenu.items("撤销", "其它")
                    floatMenu.setOnItemClickListener { _, index ->
                        when (index) {
                            0 -> {
                                deleteMessage(obj)
                            }
                            else -> {
                            }
                        }
                    }
                    floatMenu.show(Point(point[0], point[1]))
                }
            }
        }
    }

    /**清除输入框焦点并关闭键盘*/
    private fun clearEditFocus() {
        chat_message_edit.clearFocus()
        BaseUtils.hideKeyboard(this)
    }

    /**根据输入字数，修改发送按钮样式*/
    private fun changeSendStyle(inputNum: Int = 0) {
        if (inputNum > 0) {
            chat_message_send.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
        } else {
            chat_message_send.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))
        }
    }
}
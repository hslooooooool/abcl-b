package qsos.base.chat.data.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import qsos.base.chat.data.ApiChatSession
import qsos.base.chat.data.entity.ChatSession
import qsos.base.core.config.BaseConfig
import qsos.core.lib.utils.data.SharedPreUtils
import qsos.lib.base.utils.LogUtil
import qsos.lib.netservice.ApiEngine
import qsos.lib.netservice.expand.retrofitByDef

/**
 * @author : 华清松
 * 消息更新服务
 */
class MessageUpdateService : Service() {
    private val binder = MessageUpdateBinder()
    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        binder.getOnlineSession()
        return super.onStartCommand(intent, flags, startId)
    }

    class MessageUpdateBinder : Binder(), IMessageUpdateService {
        private val mJob = Dispatchers.IO + Job()

        override fun <SESSION> getLocalSession(): List<SESSION> {
            val str = SharedPreUtils.find("SESSION", "")
            return try {
                Gson().fromJson<List<SESSION>>(str, object : TypeToken<List<SESSION>>() {}.type)
            } catch (e: Exception) {
                arrayListOf()
            }
        }

        override fun <SESSION> saveLocalSession(list: List<SESSION>) {
            SharedPreUtils.save("SESSION", Gson().toJson(list))
        }

        @SuppressLint("UseSparseArrays")
        override fun getOnlineSession() {
            CoroutineScope(mJob).retrofitByDef<List<ChatSession>> {
                api = ApiEngine.createService(ApiChatSession::class.java).getSessionListByUserId(userId = BaseConfig.userId)
                onFailed { code, msg, error ->
                    LogUtil.w(msg ?: (error?.message ?: "消息会话获取失败，错误码【$code】"))
                }
                onSuccess { sessionList ->
                    val old = getLocalSession<ChatSession>()
                    val oldMap = HashMap<Int, ChatSession>(old.size)
                    old.forEach {
                        oldMap[it.sessionId] = it
                    }
                    /**需要更新的会话集合*/
                    val haveUpdateSession = HashSet<ChatSession>()
                    sessionList?.forEach {
                        if (oldMap[it.sessionId]?.lastTimeline != it.lastTimeline) {
                            haveUpdateSession.add(it)
                        }
                    }
                    haveUpdateSession.forEach {
                        getNewMessage(it, oldMap[it.sessionId]!!.lastTimeline, 10)
                    }
                }
            }
        }

        override fun <SESSION> getNewMessage(session: SESSION, startTimeline: Int, size: Int) {


        }

        override fun <SESSION, MESSAGE> saveNewMessage(session: SESSION, list: List<MESSAGE>) {


        }
    }

}
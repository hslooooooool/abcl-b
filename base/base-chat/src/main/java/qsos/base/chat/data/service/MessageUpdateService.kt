package qsos.base.chat.data.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class MessageUpdateService : Service(), IMessageUpdateService {
    
    override fun <SESSION> getLocalSession(): List<SESSION> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <SESSION> saveLocalSession(list: List<SESSION>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getOnlineSession() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getNewMessage(startTimeline: Int, size: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <MESSAGE> saveNewMessage(list: List<MESSAGE>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}
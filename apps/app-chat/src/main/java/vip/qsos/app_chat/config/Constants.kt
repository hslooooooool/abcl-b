package vip.qsos.app_chat.config

interface Constants {

    /**自定义消息 Action 类型*/
    interface MessageAction {
        companion object {
            /**下线类型*/
            const val ACTION_999 = "999"
        }
    }

    companion object {
        /**服务端IP地址*/
        var IM_SERVER_HOST = "192.168.1.103"
        /**服务端消息端口*/
        var IM_SERVER_PORT = 23456
    }
}

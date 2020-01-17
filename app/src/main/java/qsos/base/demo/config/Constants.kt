package qsos.base.demo.config

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
        var IM_SERVER_HOST = "192.168.2.103"
        /**服务端消息端口*/
        var IM_SERVER_PORT = 23456
        /**文件上传地址*/
        const val FILE_UPLOAD_URL = "http://192.168.2.103:8085/api/file/upload/file"
    }
}

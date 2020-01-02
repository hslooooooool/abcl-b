package qsos.base.chat.data.entity

/**
 * @author : 华清松
 * 消息内容-位置消息
 * @param name 位置名称
 * @param avatar 位置封面
 * @param lng 位置经度
 * @param lat 位置纬度
 */
data class MChatMessageLocation(
        val name: String,
        val avatar: String,
        val lng: String,
        val lat: String
) : IMessageType {

    fun getRealLng(): Double? {
        return try {
            lng.toDouble()
        } catch (e: Exception) {
            null
        }
    }

    fun getRealLat(): Double? {
        return try {
            lat.toDouble()
        } catch (e: Exception) {
            null
        }
    }

    override var contentDesc: String = name
    override var contentType: Int = EnumChatMessageType.LOCATION.contentType
}
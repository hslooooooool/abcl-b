package vip.qsos.app_chat.data.entity

/**
 * @author : 华清松
 * 好友关系
 * @param applicant 申请用户ID
 * @param friend 好友用户ID
 * @param accept 是否接受
 */
data class ChatFriendBo(
        var applicant: Long,
        var friend: Long,
        var accept: Boolean?
)

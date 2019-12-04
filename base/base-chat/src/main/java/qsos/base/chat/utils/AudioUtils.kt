package qsos.base.chat.utils

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.TextView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import qsos.base.chat.R
import qsos.core.file.audio.*
import qsos.core.lib.utils.dialog.AbsBottomDialog
import kotlin.math.abs

/**
 * @author : 华清松
 * 录音工具
 */
@SuppressLint("SetTextI18n", "CheckResult")
object AudioUtils {
    private var fileObservable: PublishSubject<AudioRecordData>? = null
    /**录音按键左右移动距离*/
    private var moveX = 0F
    /**录音按键上下移动距离*/
    private var moveY = 0F
    private lateinit var mAudioRecordController: AudioRecordController

    /**开启录音
     * @param dialog 录音弹窗
     * @param config 录音配置，限制最小最大录制时长，设置录音格式，支持 AMR 与 WAV 格式
     * */
    fun record(
            dialog: AbsBottomDialog,
            config: AudioRecordConfig = AudioRecordConfig.Builder()
                    .setLimitMinTime(1)
                    .setLimitMaxTime(10)
                    .setAudioFormat(AudioType.AMR)
                    .build()
    ): Observable<AudioRecordData> {
        fileObservable = PublishSubject.create()
        mAudioRecordController = AudioRecordController(config)
        val stateView = dialog.findViewById<TextView>(R.id.audio_state)
        dialog.findViewById<ImageView>(R.id.audio_action).setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                /**按下手指*/
                MotionEvent.ACTION_DOWN -> {
                    moveX = motionEvent.x
                    moveY = motionEvent.y
                    /**开始录音*/
                    mAudioRecordController.start()
                }
                /**抬起手指*/
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    mAudioRecordController.finish()
                }
                /**移动手指*/
                MotionEvent.ACTION_MOVE -> {
                    if (abs(moveX - motionEvent.x) > 100 || abs(moveY - motionEvent.y) > 100) {
                        mAudioRecordController.cancelWant()
                    } else {
                        mAudioRecordController.cancelRefuse()
                    }
                }
            }
            true
        }
        mAudioRecordController.mAudioPublisher.subscribe {
            when (it.recordState) {
                AudioRecordState.PREPARE, AudioRecordState.RECORDING, AudioRecordState.CANCEL_REFUSE -> {
                    stateView.text = "聆听中...\t\t时长:\t\t${it.recordTime} 秒"
                }
                AudioRecordState.CANCEL_WANT -> {
                    stateView.text = "松开可取消录音...\t\t时长:\t\t${it.recordTime} 秒"
                }
                AudioRecordState.CANCEL -> {
                    stateView.text = it.recordState.value
                    fileObservable?.onComplete()
                    fileObservable = null
                    if (dialog.isVisible) dialog.dismiss()
                }
                AudioRecordState.ERROR -> {
                    stateView.text = it.recordState.value
                    fileObservable?.onError(Throwable(it.recordState.value))
                    fileObservable?.onComplete()
                    fileObservable = null
                    if (dialog.isVisible) dialog.dismiss()
                }
                AudioRecordState.FINISH -> {
                    stateView.text = it.recordState.value + "\t\t时长:${it.recordTime} 秒"
                    fileObservable?.onNext(it)
                    fileObservable?.onComplete()
                    fileObservable = null
                    if (dialog.isVisible) dialog.dismiss()
                }
            }
        }
        return fileObservable!!
    }

}
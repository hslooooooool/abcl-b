package qsos.base.chat.utils

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import qsos.core.file.audio.*
import qsos.lib.base.callback.OnTListener
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
            touchView: View,
            stateView: TextView,
            closeListener: OnTListener<Int>,
            config: AudioRecordConfig = AudioRecordConfig.Builder()
                    .setLimitMinTime(1)
                    .setLimitMaxTime(10)
                    .setAudioFormat(AudioType.AMR)
                    .build()
    ): Observable<AudioRecordData> {
        fileObservable = PublishSubject.create()
        mAudioRecordController = AudioRecordController(config)
        touchView.setOnTouchListener { _, motionEvent ->
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
                    closeListener.back(0)
                }
                AudioRecordState.CANCEL_WANT -> {
                    stateView.text = "松开可取消录音...\t\t时长:\t\t${it.recordTime} 秒"
                    closeListener.back(1)
                }
                AudioRecordState.CANCEL -> {
                    fileObservable?.onComplete()
                    fileObservable = null
                    closeListener.back(-1)
                }
                AudioRecordState.ERROR -> {
                    fileObservable?.onError(Throwable(it.recordState.value))
                    fileObservable?.onComplete()
                    fileObservable = null
                    closeListener.back(-2)
                }
                AudioRecordState.FINISH -> {
                    fileObservable?.onNext(it)
                    fileObservable?.onComplete()
                    fileObservable = null
                    closeListener.back(-3)
                }
            }
        }
        return fileObservable!!
    }

}
package qsos.base.demo.config

import android.content.Context
import android.text.TextUtils
import androidx.fragment.app.FragmentActivity
import com.google.gson.Gson
import io.reactivex.Observable
import kotlinx.coroutines.*
import qsos.core.file.RxImageConverters
import qsos.core.file.RxImagePicker
import qsos.core.file.Sources
import qsos.core.form.config.IFormConfig
import qsos.core.form.db.entity.FormValueOfFile
import qsos.core.form.db.entity.FormValueOfLocation
import qsos.core.form.db.entity.FormValueOfUser
import qsos.core.lib.utils.file.FileUtils
import qsos.core.player.PlayerConfigHelper
import qsos.core.player.data.PreAudioEntity
import qsos.core.player.data.PreDocumentEntity
import qsos.core.player.data.PreImageEntity
import qsos.core.player.data.PreVideoEntity
import qsos.lib.base.callback.OnTListener
import qsos.lib.base.utils.LogUtil
import qsos.lib.base.utils.ToastUtils
import qsos.lib.netservice.data.BaseResponse
import qsos.lib.netservice.file.FileRepository
import qsos.lib.netservice.file.HttpFileEntity
import qsos.lib.netservice.file.IFileModel
import timber.log.Timber
import java.io.File
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 * 表单文件操作具体实现
 */
object FormConfig : IFormConfig {

    val mJob: CoroutineContext = Dispatchers.Main + Job()
    private var mFileModel: IFileModel = FileRepository(mJob)

    override fun takeCamera(context: Context, formItemId: Long, onSuccess: (FormValueOfFile) -> Any) {
        Timber.tag("表单文件代理").i("拍照")
        RxImagePicker.with((context as FragmentActivity).supportFragmentManager).takeImage(Sources.DEVICE)
                .flatMap {
                    RxImageConverters.uriToFileObservable(context, it, FileUtils.createImageFile())
                }
                .subscribe {
                    val file = FormValueOfFile(fileId = "0001", fileName = it.name, filePath = it.absolutePath, fileType = it.extension, fileUrl = it.absolutePath, fileCover = it.absolutePath)
                    onSuccess.invoke(file)
                }.takeUnless {
                    context.isFinishing
                }
    }

    override fun takeGallery(context: Context, formItemId: Long, canTakeSize: Int, onSuccess: (List<FormValueOfFile>) -> Any) {
        Timber.tag("表单文件代理").i("图库")
        when (canTakeSize) {
            1 -> {
                RxImagePicker.with((context as FragmentActivity).supportFragmentManager).takeImage(Sources.ONE)
                        .flatMap {
                            RxImageConverters.uriToFileObservable(context, it, FileUtils.createImageFile())
                        }
                        .subscribe {
                            val file = FormValueOfFile(fileId = it.absolutePath, fileName = it.name,
                                    filePath = it.absolutePath, fileType = it.extension,
                                    fileUrl = it.absolutePath, fileCover = it.absolutePath)
                            mFileModel.uploadFile(
                                    Constants.FILE_UPLOAD_URL,
                                    HttpFileEntity(url = null, path = it.absolutePath, filename = it.name),
                                    object : OnTListener<HttpFileEntity> {
                                        override fun back(t: HttpFileEntity) {
                                            if (t.loadSuccess) {
                                                file.fileId = t.avatar
                                                file.fileCover = t.avatar
                                                file.fileUrl = t.url
                                                onSuccess.invoke(arrayListOf(file))
                                            } else {
                                                if (t.progress == -1) {
                                                    ToastUtils.showToast(context, "上传失败")
                                                }
                                            }
                                        }
                                    }
                            )
                        }
            }
            in 2..9 -> {
                RxImagePicker.with((context as FragmentActivity).supportFragmentManager).takeFiles(arrayOf("image/*"))
                        .flatMap {
                            val files = arrayListOf<File>()
                            it.forEachIndexed { index, uri ->
                                if (index < canTakeSize) {
                                    RxImageConverters.uriToFile(context, uri, FileUtils.createImageFile())?.let { f ->
                                        files.add(f)
                                    }
                                }
                            }
                            Observable.just(files)
                        }
                        .subscribe {
                            val files = arrayListOf<FormValueOfFile>()
                            it.forEach { f ->
                                files.add(FormValueOfFile(fileId = "takeGallery${f.absolutePath}",
                                        fileName = f.name, filePath = f.absolutePath,
                                        fileType = f.extension, fileUrl = f.absolutePath,
                                        fileCover = f.absolutePath))
                            }
                            onSuccess.invoke(files)
                            FileRepository(Dispatchers.Main + Job()).uploadFile(
                                    Constants.FILE_UPLOAD_URL,
                                    it.map { file ->
                                        HttpFileEntity(url = null, path = file.absolutePath, filename = file.name)
                                    },
                                    object : OnTListener<BaseResponse<List<HttpFileEntity>>> {
                                        override fun back(t: BaseResponse<List<HttpFileEntity>>) {
                                            LogUtil.i("上传图片>>>>>" + t.code)
                                        }
                                    }
                            )
                        }.takeUnless {
                            context.isFinishing
                        }
            }
            else -> onSuccess(arrayListOf())
        }
    }

    override fun takeVideo(context: Context, formItemId: Long, canTakeSize: Int, onSuccess: (List<FormValueOfFile>) -> Any) {
        Timber.tag("表单文件代理").i("视频")
        RxImagePicker.with((context as FragmentActivity).supportFragmentManager).takeVideo()
                .flatMap {
                    RxImageConverters.uriToFileObservable(context, it, FileUtils.createVideoFile())
                }
                .subscribe {
                    val file = FormValueOfFile(fileId = "takeVideo", fileName = it.name, filePath = it.absolutePath, fileType = it.extension, fileUrl = it.absolutePath, fileCover = it.absolutePath)
                    onSuccess.invoke(arrayListOf(file))
                }.takeUnless {
                    context.isFinishing
                }
    }

    override fun takeAudio(context: Context, formItemId: Long, onSuccess: (FormValueOfFile) -> Any) {
    }

    override fun takeFile(context: Context, formItemId: Long, canTakeSize: Int, mimeTypes: List<String>, onSuccess: (List<FormValueOfFile>) -> Any) {
        Timber.tag("表单文件代理").i("文件")
        RxImagePicker.with((context as FragmentActivity).supportFragmentManager).takeFiles(arrayOf("image/*", "video/*", "text/plain"))
                .flatMap {
                    val files = arrayListOf<File>()
                    it.forEachIndexed { index, uri ->
                        if (index < canTakeSize) {
                            RxImageConverters.uriToFile(context, uri, FileUtils.createFileByUri(context, uri))?.let { f ->
                                files.add(f)
                            }
                        }
                    }
                    Observable.just(files)
                }
                .subscribe {
                    val files = arrayListOf<FormValueOfFile>()
                    it.forEach { f ->
                        files.add(FormValueOfFile(fileId = "takeFile", fileName = f.name, filePath = f.absolutePath, fileType = f.extension, fileUrl = f.absolutePath, fileCover = f.absolutePath))
                    }
                    onSuccess.invoke(files)
                }.takeUnless {
                    context.isFinishing
                }
    }

    override fun takeLocation(context: Context, formItemId: Long, location: FormValueOfLocation?, onSuccess: (FormValueOfLocation) -> Any) {
        Timber.tag("表单位置代理").i("位置，已有位置${Gson().toJson(location)}")
        CoroutineScope(Job()).launch(Dispatchers.Main) {
            val takeLocation = async(Dispatchers.IO) {
                FormValueOfLocation(locName = "测试位置成都市高新区孵化园A区B栋", locX = 30.0000, locY = 104.000000)
            }
            val l = takeLocation.await()
            onSuccess.invoke(l)
        }
    }

    override fun takeUser(context: Context, formItemId: Long, canTakeSize: Int, checkedUsers: List<FormValueOfUser>, onSuccess: (List<FormValueOfUser>) -> Any) {
    }

    override fun previewFile(context: Context, index: Int, formValueOfFiles: List<FormValueOfFile>) {
        Timber.tag("表单文件预览代理").i("文件$index")
        if (formValueOfFiles.isNotEmpty()) {
            val file = formValueOfFiles[index]
            var position = 0
            var positionIsRight = false
            when (val type = FormValueOfFile.getFileTypeByMime(file.fileType)) {
                "IMAGE" -> {
                    val img = formValueOfFiles.filter {
                        !TextUtils.isEmpty(it.filePath)
                    }.filter {
                        type == FormValueOfFile.getFileTypeByMime(it.fileType)
                    }.map { v ->
                        if (v != file && !positionIsRight) {
                            position++
                        } else {
                            positionIsRight = true
                        }
                        PreImageEntity(v.fileName ?: "", v.filePath!!, v.fileName ?: "")
                    }
                    PlayerConfigHelper.previewImage(context, position, img)
                }
                "AUDIO" -> {
                    val audio = formValueOfFiles.filter {
                        !TextUtils.isEmpty(it.filePath)
                    }.filter {
                        type == FormValueOfFile.getFileTypeByMime(it.fileType)
                    }.map { v ->
                        if (v != file && !positionIsRight) {
                            position++
                        } else {
                            positionIsRight = true
                        }
                        PreAudioEntity(v.fileName ?: "", v.filePath!!, v.fileName ?: "")
                    }
                    PlayerConfigHelper.previewAudio(context, position, audio)
                }
                "VIDEO" -> {
                    val video = formValueOfFiles.filter {
                        !TextUtils.isEmpty(it.filePath)
                    }.filter {
                        type == FormValueOfFile.getFileTypeByMime(it.fileType)
                    }.map { v ->
                        if (v != file && !positionIsRight) {
                            position++
                        } else {
                            positionIsRight = true
                        }
                        PreVideoEntity(v.fileName ?: "", v.filePath!!, v.fileName ?: "")
                    }
                    PlayerConfigHelper.previewVideo(context, index, video)
                }
                "FILE" -> {
                    val doc = formValueOfFiles[index]
                    doc.filePath?.let {
                        PlayerConfigHelper.previewDocument(context, PreDocumentEntity(doc.fileName
                                ?: it, it))
                    }
                }
            }
        }
    }

    override fun previewUser(context: Context, index: Int, formValueOfUser: List<FormValueOfUser>) {
        Timber.tag("表单用户预览代理").i("用户$index")
    }
}
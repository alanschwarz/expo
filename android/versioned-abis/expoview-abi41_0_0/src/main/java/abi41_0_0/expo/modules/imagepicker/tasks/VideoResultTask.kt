package abi41_0_0.expo.modules.imagepicker.tasks

import android.content.ContentResolver
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import abi41_0_0.expo.modules.imagepicker.ImagePickerConstants
import abi41_0_0.expo.modules.imagepicker.fileproviders.FileProvider
import abi41_0_0.org.unimodules.core.Promise
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.NullPointerException

class VideoResultTask(private val promise: Promise,
                      private val uri: Uri,
                      private val contentResolver: ContentResolver,
                      private val fileProvider: FileProvider,
                      private val mediaMetadataRetriever: MediaMetadataRetriever)
  :  AsyncTask<Void?, Void?, Void?>() {

  override fun doInBackground(vararg params: Void?): Void? {
    try {
      val outputFile = fileProvider.generateFile()
      saveVideo(outputFile)
      val response = Bundle().apply {
        putString("uri", Uri.fromFile(outputFile).toString())
        putBoolean("cancelled", false)
        putString("type", "video")
        putInt("width", mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)!!.toInt())
        putInt("height", mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)!!.toInt())
        putInt("rotation", mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)!!.toInt())
        putInt("duration", mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toInt())
      }
      promise.resolve(response)
    } catch (e: NullPointerException) {
      promise.reject(ImagePickerConstants.ERR_CAN_NOT_EXTRACT_METADATA, ImagePickerConstants.CAN_NOT_EXTRACT_METADATA_MESSAGE, e)
    } catch (e: IllegalArgumentException) {
      promise.reject(ImagePickerConstants.ERR_CAN_NOT_EXTRACT_METADATA, ImagePickerConstants.CAN_NOT_EXTRACT_METADATA_MESSAGE, e)
    } catch (e: SecurityException) {
      promise.reject(ImagePickerConstants.ERR_CAN_NOT_EXTRACT_METADATA, ImagePickerConstants.CAN_NOT_EXTRACT_METADATA_MESSAGE, e)
    } catch (e: IOException) {
      promise.reject(ImagePickerConstants.ERR_CAN_NOT_SAVE_RESULT, ImagePickerConstants.CAN_NOT_SAVE_RESULT_MESSAGE, e)
    }
    return null
  }

  @Throws(IOException::class)
  private fun saveVideo(outputFile: File) {
    contentResolver.openInputStream(uri)?.use { input ->
      FileOutputStream(outputFile).use { out ->
        val buffer = ByteArray(4096)
        var bytesRead: Int
        while (input.read(buffer).also { bytesRead = it } > 0) {
          out.write(buffer, 0, bytesRead)
        }
      }
    }
  }

}

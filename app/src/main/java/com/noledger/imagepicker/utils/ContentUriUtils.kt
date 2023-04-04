package com.noledger.imagepicker.utils

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.annotation.WorkerThread
import java.io.File
import java.io.FileOutputStream
import java.net.URISyntaxException

object ContentUriUtils {
    /**
     * make sure to use this getFilePath method from worker thread
     */
    @WorkerThread
    @Throws(URISyntaxException::class)
    fun getFilePath(context: Context, fileUri: Uri): String? {
        var uri = fileUri
        var selection: String? = null
        var selectionArgs: Array<String>? = null
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(
                context.applicationContext,
                uri
            )
        ) {
            when {
                isExternalStorageDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
                isDownloadsDocument(uri) -> {
                    val id = DocumentsContract.getDocumentId(uri)
                    uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(id)
                    )
                }
                isMediaDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    if ("image" == type) {
                        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    selection = "_id=?"
                    selectionArgs = arrayOf(
                        split[1]
                    )
                }
            }
        }
        if ("content".equals(uri.scheme, ignoreCase = true)) {
            if (isGooglePhotosUri(uri)) {
                return uri.lastPathSegment
            }
            val projection = arrayOf(
                MediaStore.Images.Media.DATA
            )
            try {
                val cursor =
                    context.contentResolver?.query(uri, projection, selection, selectionArgs, null)
                var path: String? = null
                if (cursor != null) {
                    val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    if (cursor.moveToFirst()) {
                        path = cursor.getString(column_index)
                        cursor.close()
                    }
                }
                return path
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

}


object DocumentUtils {
    fun getFile(mContext: Context?, documentUri: Uri): File {
        val inputStream = mContext?.contentResolver?.openInputStream(documentUri)
        var file = File("")
        inputStream.use { input ->
            file = File(mContext?.cacheDir, System.currentTimeMillis().toString() + ".pdf")
            FileOutputStream(file).use { output ->
                val buffer = ByteArray(4 * 1024) // or other buffer size
                var read: Int = -1
                while (input?.read(buffer).also {
                        if (it != null) {
                            read = it
                        }
                    } != -1) {
                    output.write(buffer, 0, read)
                }
                output.flush()
            }
        }
        return file
    }
}
package com.github.noledger.imagepicker.provider

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.MimeTypeMap
import com.github.noledger.imagepicker.R
import com.github.noledger.imagepicker.ImagePicker
import com.github.noledger.imagepicker.ImagePickerActivity
import com.github.noledger.imagepicker.util.IntentUtils
class GalleryProvider(activity: ImagePickerActivity) :
    BaseProvider(activity) {

    companion object {
        private const val GALLERY_INTENT_REQ_CODE = 4261
    }

    // Mime types restrictions for gallery. By default all mime types are valid
    private val mimeTypes: Array<String>

    init {
        val bundle = activity.intent.extras ?: Bundle()

        // Get MIME types
        mimeTypes = bundle.getStringArray(ImagePicker.EXTRA_MIME_TYPES) ?: emptyArray()
    }

    /**
     * Start Gallery Capture Intent
     */
    fun startIntent() {
        startGalleryIntent()
    }

    /**
     * Start Gallery Intent
     */
    private fun startGalleryIntent() {
        val galleryIntent = IntentUtils.getGalleryIntent(activity, mimeTypes)
        activity.startActivityForResult(galleryIntent, GALLERY_INTENT_REQ_CODE)
    }

    /**
     * Handle Gallery Intent Activity Result
     *
     * @param requestCode It must be {@link GalleryProvider#GALLERY_INTENT_REQ_CODE}
     * @param resultCode For success it should be {@link Activity#RESULT_OK}
     * @param data Result Intent
     */
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == GALLERY_INTENT_REQ_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                handleResult(data)
            } else {
                setResultCancel()
            }
        }
    }

    /**
     * This method will be called when final result fot this provider is enabled.
     */
    private fun handleResult(data: Intent?) {
        val uri = data?.data

        val contentResolver = activity.contentResolver
        val mimeType = contentResolver.getType(uri!!)
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)

        Log.e("LINKUP", "handleResult: $uri")
        Log.e("LINKUP ===>", "handleResult: $extension")
        Log.e("LINKUP ======>", "handleResult: $uri.$extension")
        val uriWithExtension = "$uri.$extension"
        if (uri != null) {
            takePersistableUriPermission(uri)
            activity.setImage(uri)
        } else {
            setError(R.string.error_failed_pick_gallery_image)
        }
    }

    /**
     * Take a persistable URI permission grant that has been offered. Once
     * taken, the permission grant will be remembered across device reboots.
     */
    private fun takePersistableUriPermission(uri: Uri) {
        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
}

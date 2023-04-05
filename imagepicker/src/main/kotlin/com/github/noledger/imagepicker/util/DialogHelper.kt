package com.github.noledger.imagepicker.util

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.github.noledger.imagepicker.R
import com.github.noledger.imagepicker.constant.ImageProvider
import com.github.noledger.imagepicker.listener.DismissListener
import com.github.noledger.imagepicker.listener.ResultListener
import com.google.android.material.bottomsheet.BottomSheetDialog

internal object DialogHelper {

    /**
     * Show Image Provide Picker Dialog. This will streamline the code to pick/capture image
     *
     */
    fun showChooseAppDialog(
        context: Context, listener: ResultListener<ImageProvider>, dismissListener: DismissListener?
    ) {
        val bottomSheetDialog = BottomSheetDialog(context,R.style.CustomBottomSheetDialog)
        val layoutInflater = LayoutInflater.from(context)
        val customView = layoutInflater.inflate(R.layout.dialog_choose_app, null)
        customView.setBackgroundColor(Color.TRANSPARENT)

        customView.findViewById<View>(R.id.lytCameraPick).setOnClickListener {
            listener.onResult(ImageProvider.CAMERA)
            bottomSheetDialog.dismiss()
        }
        customView.findViewById<View>(R.id.lytGalleryPick).setOnClickListener {
            listener.onResult(ImageProvider.GALLERY)
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.setCancelable(true)
        bottomSheetDialog.setOnCancelListener { listener.onResult(null) }
        bottomSheetDialog.setOnDismissListener { dismissListener?.onDismiss() }
        bottomSheetDialog.setContentView(customView)
        bottomSheetDialog.show()
    }
}

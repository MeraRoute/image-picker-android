package com.noledger.imagepicker

import android.app.Activity
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.github.noledger.imagepicker.ImagePicker
import com.github.noledger.imagepicker.util.FileUtil
import com.noledger.imagepicker.databinding.ActivityMainBinding
import com.noledger.imagepicker.utils.ContentUriUtils
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import java.io.File

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    var filePath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpObserver()
    }

    private fun setUpObserver() {
        binding.imgCamera.setOnClickListener {
            ImagePicker.with(this).cropSquare().maxResultSize(1080, 1080).createIntent { intent ->
                startForProfileImageResult.launch(intent)
            }
        }
    }

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data
            when (resultCode) {
                Activity.RESULT_OK -> {
                    try {
                        if (data != null && data.data != null) {
                            //Image Uri will not be null for RESULT_OK
                            val fileUri = data.data!!
                            var filePath = ContentUriUtils.getFilePath(this, fileUri)
                            //                            viewModel.filePath.value = filePath
                            binding.imgPersonImage.setImageURI(fileUri)

                            //image compress using zelory compressor
                            val actualImageFile = filePath?.let { File(it) }


                            lifecycleScope.launchWhenResumed {
                                val compressedImageFile =
                                    actualImageFile?.let {
                                        Compressor.compress(this@MainActivity, it) {
                                            quality(80) // combine with compressor constraint
                                            format(
                                                if (this@MainActivity.let { it1 ->
                                                        FileUtil.getImageExtension(
                                                            it1,
                                                            fileUri
                                                        )
                                                    } == ".png"
                                                ) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                                            )
                                        }
                                    }
                                filePath = compressedImageFile?.absolutePath
                                filePath?.let { Log.e("MainActivity File Path===>", it) }
                            }


                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(this,ImagePicker.getError(data),Toast.LENGTH_SHORT)
                }
            }
        }
}
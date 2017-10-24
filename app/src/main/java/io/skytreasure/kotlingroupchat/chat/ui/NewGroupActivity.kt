package io.skytreasure.kotlingroupchat.chat.ui

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Base64
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_new_group.*

import io.skytreasure.kotlingroupchat.R
import io.skytreasure.kotlingroupchat.chat.ui.adapter.ParticipantsAdapter
import java.io.ByteArrayOutputStream
import java.io.IOException

class NewGroupActivity : AppCompatActivity(), View.OnClickListener {

    var adapter: ParticipantsAdapter? = null
    private var mCropImageUri: Uri? = null
    private var resultUri: Uri? = null

    companion object {


        private const val ASK_MULTIPLE_PERMISSION_REQUEST_CODE: Int = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_group)

        rv_main.layoutManager = LinearLayoutManager(this@NewGroupActivity) as RecyclerView.LayoutManager?
        adapter = ParticipantsAdapter()
        rv_main.adapter = adapter

        iv_profile.setOnClickListener(this@NewGroupActivity)
        btn_creategroup.setOnClickListener(this@NewGroupActivity)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CropImage.startPickImageActivity(this)
            } else {
                Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show()
            }
        }
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (mCropImageUri != null && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // mCurrentFragment.setImageUri(mCropImageUri);
            } else {
                Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show()
            }
        }


        if (requestCode == ASK_MULTIPLE_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            val imageUri = CropImage.getPickImageResultUri(this, data)

            // For API >= 23 we need to check specifically that we have permissions to read external storage,
            // but we don't know if we need to for the URI so the simplest is to try open the stream and see if we get error.
            var requirePermissions = false
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {

                // request permissions and handle the result in onRequestPermissionsResult()
                requirePermissions = true
                mCropImageUri = imageUri
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE)
                }
            } else {

                CropImage.activity(imageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        .setAspectRatio(1, 1)
                        .setInitialCropWindowPaddingRatio(0f)
                        .start(this)

            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                resultUri = result.uri
                val img = "data:"
                val mimeType = getMimeType(resultUri, this) + ";base64,"//data:image/jpeg;base64,
                val s = img + mimeType + getBase64EncodedImage(resultUri, this) as String
                //callProfilePictureApi(s)

                iv_profile.setImageURI(resultUri)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                result.error
            }
        }

    }

    private fun getBase64EncodedImage(uri: Uri?, context: Context): String? {
        return try {
            val bmp = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            val nh = (bmp.height * (512.0 / bmp.width)).toInt()
            val scaledBmp = Bitmap.createScaledBitmap(bmp, 512, nh, true)
            val baos = ByteArrayOutputStream()
            scaledBmp.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val imageBytes = baos.toByteArray()
            Base64.encodeToString(imageBytes, Base64.DEFAULT)
        } catch (exception: IOException) {
            Toast.makeText(context, "Image not found", Toast.LENGTH_LONG).show()
            null
        }

    }


    private fun getMimeType(uri: Uri?, context: Context): String {
        return if (uri?.scheme == ContentResolver.SCHEME_CONTENT) {
            val cr = context.contentResolver
            cr.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString())
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase())
        }
    }

    /*
     * Call from fragment to crop image
     **/

    fun cropImage() {
        if (CropImage.isExplicitCameraPermissionRequired(this@NewGroupActivity)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE)
            }
        } else {
            //CropImage.getPickImageChooserIntent(this)
            CropImage.startPickImageActivity(this@NewGroupActivity)
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_profile -> {
                cropImage()
            }

            R.id.btn_creategroup -> {
                Toast.makeText(this@NewGroupActivity, et_groupname.text, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

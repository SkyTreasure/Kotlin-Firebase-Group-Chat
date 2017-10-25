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
import io.skytreasure.kotlingroupchat.MainActivity
import kotlinx.android.synthetic.main.activity_new_group.*

import io.skytreasure.kotlingroupchat.R
import io.skytreasure.kotlingroupchat.chat.MyChatManager
import io.skytreasure.kotlingroupchat.chat.model.GroupModel
import io.skytreasure.kotlingroupchat.chat.model.UserModel
import io.skytreasure.kotlingroupchat.chat.ui.adapter.ParticipantsAdapter
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.selectedUserList
import io.skytreasure.kotlingroupchat.common.constants.NetworkConstants
import io.skytreasure.kotlingroupchat.common.controller.NotifyMeInterface
import io.skytreasure.kotlingroupchat.common.util.SharedPrefManager
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
        adapter = ParticipantsAdapter(object : NotifyMeInterface {
            override fun handleData(`object`: Any, requestCode: Int?) {
                tv_no_of_participants.setText("" + selectedUserList?.size!! + " Participants")
            }

        })
        rv_main.adapter = adapter

        iv_profile.setOnClickListener(this@NewGroupActivity)
        iv_back.setOnClickListener(this@NewGroupActivity)
        btn_creategroup.setOnClickListener(this@NewGroupActivity)
        tv_no_of_participants.setText("" + selectedUserList?.size!! + " Participants")
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
                createGroup()
            }

            R.id.iv_back -> {
                finish()
            }
        }
    }

    private fun createGroup() {
        var isValid: Boolean = true
        var errorMessage: String = "Validation Error"

        var groupName: String = et_groupname.text.toString()

        if (groupName.isBlank()) {
            isValid = false
            errorMessage = "Group name is blank"
        }
        if (groupName.length!! < 3) {
            isValid = false
            errorMessage = "Group name should be more than 2 characters"
        }

        var groupImage: String = "https://cdn1.iconfinder.com/data/icons/google_jfk_icons_by_carlosjj/128/groups.png"
        var newGroup: GroupModel = GroupModel(groupName, groupImage, group_deleted = false, group = true)
        var adminUserModel: UserModel? = SharedPrefManager.getInstance(this@NewGroupActivity).savedUserModel
        adminUserModel?.admin = true

        var groupMembers: HashMap<String, UserModel> = hashMapOf()


        for (user in selectedUserList!!) {
            groupMembers.put(user.uid!!, user)
        }


        groupMembers.put(adminUserModel?.uid!!, adminUserModel)

        newGroup.members = groupMembers

        MyChatManager.setmContext(this@NewGroupActivity)

        if (isValid) {

            MyChatManager.createGroup(object : NotifyMeInterface {
                override fun handleData(`object`: Any, requestCode: Int?) {
                    Toast.makeText(this@NewGroupActivity, "Group has been created successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@NewGroupActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }

            }, newGroup, NetworkConstants.CREATE_GROUP)
        } else {
            Toast.makeText(this@NewGroupActivity, errorMessage, Toast.LENGTH_SHORT).show()
        }


    }
}

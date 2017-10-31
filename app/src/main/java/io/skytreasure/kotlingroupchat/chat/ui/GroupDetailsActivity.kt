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
import android.text.format.DateFormat
import android.util.Base64
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import io.skytreasure.kotlingroupchat.MainActivity
import kotlinx.android.synthetic.main.activity_new_group.*

import io.skytreasure.kotlingroupchat.R
import io.skytreasure.kotlingroupchat.chat.MyChatManager
import io.skytreasure.kotlingroupchat.chat.model.FileModel
import io.skytreasure.kotlingroupchat.chat.model.GroupModel
import io.skytreasure.kotlingroupchat.chat.model.MessageModel
import io.skytreasure.kotlingroupchat.chat.model.UserModel
import io.skytreasure.kotlingroupchat.chat.ui.adapter.ParticipantsAdapter
import io.skytreasure.kotlingroupchat.common.constants.AppConstants
import io.skytreasure.kotlingroupchat.common.constants.DataConstants
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.sMyGroups
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.selectedUserList
import io.skytreasure.kotlingroupchat.common.constants.FirebaseConstants
import io.skytreasure.kotlingroupchat.common.constants.NetworkConstants
import io.skytreasure.kotlingroupchat.common.controller.NotifyMeInterface
import io.skytreasure.kotlingroupchat.common.util.SharedPrefManager
import io.skytreasure.kotlingroupchat.common.util.loadRoundImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*


class GroupDetailsActivity : AppCompatActivity(), View.OnClickListener {

    var adapter: ParticipantsAdapter? = null
    private var mCropImageUri: Uri? = null
    private var resultUri: Uri? = null
    var storage = FirebaseStorage.getInstance()
    var groupId: String? = ""
    var storageRef: StorageReference? = null

    companion object {


        private const val ASK_MULTIPLE_PERMISSION_REQUEST_CODE: Int = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_group)

        storageRef = storage.getReferenceFromUrl(NetworkConstants.URL_STORAGE_REFERENCE).child(NetworkConstants.FOLDER_STORAGE_IMG)

        rv_main.layoutManager = LinearLayoutManager(this@GroupDetailsActivity) as RecyclerView.LayoutManager?


        groupId = intent.getStringExtra(AppConstants.GROUP_ID)

        if (groupId != null) {
            //Group Details page
            btn_creategroup.text = "Update Group Name"
            selectedUserList?.clear()
            et_groupname.setText(DataConstants.sGroupMap?.get(groupId!!)?.name!!.toString())

            DataConstants.sGroupMap?.get(groupId!!)?.members?.forEach { member ->
                DataConstants.userMap?.get(member.value.uid)!!.admin = member.value.admin
                DataConstants.userMap?.get(member.value.uid)!!.delete_till = member.value.delete_till
                DataConstants.userMap?.get(member.value.uid)!!.unread_group_count = member.value.unread_group_count
                selectedUserList?.add(DataConstants.userMap?.get(member.value.uid)!!)
            }

            loadRoundImage(iv_profile, DataConstants.sGroupMap?.get(groupId!!)?.image_url!!)

            adapter = ParticipantsAdapter(object : NotifyMeInterface {
                override fun handleData(`object`: Any, requestCode: Int?) {
                    tv_no_of_participants.setText("" + selectedUserList?.size!! + " Participants")
                }

            }, AppConstants.DETAILS, groupId!!)
            rv_main.adapter = adapter
            tv_exit_group.visibility = View.VISIBLE
            tv_exit_group.setOnClickListener(this@GroupDetailsActivity)

        } else {
            // Group Creation Page
            btn_creategroup.text = "Create Group"
            adapter = ParticipantsAdapter(object : NotifyMeInterface {
                override fun handleData(`object`: Any, requestCode: Int?) {
                    tv_no_of_participants.setText("" + selectedUserList?.size!! + " Participants")
                }

            }, AppConstants.CREATION, "23")
            rv_main.adapter = adapter
            tv_exit_group.visibility = View.GONE
        }

        iv_profile.setOnClickListener(this@GroupDetailsActivity)
        iv_back.setOnClickListener(this@GroupDetailsActivity)
        btn_creategroup.setOnClickListener(this@GroupDetailsActivity)
        tv_no_of_participants.setText("" + selectedUserList?.size!! + " Participants")
        label_hint.setOnClickListener(this@GroupDetailsActivity)
        label_hint.setText("Add akash.nidhi@interactionone.com to the group")
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
                if (groupId != null) {
                    sendFileFirebase(storageRef, resultUri!!, groupId!!)
                }
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
        if (CropImage.isExplicitCameraPermissionRequired(this@GroupDetailsActivity)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE)
            }
        } else {
            //CropImage.getPickImageChooserIntent(this)
            CropImage.startPickImageActivity(this@GroupDetailsActivity)
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_profile -> {
                cropImage()
            }

            R.id.btn_creategroup -> {
                if (btn_creategroup.text.equals("Create Group")) {
                    createGroup()
                } else {
                    updateName()
                }

            }

            R.id.iv_back -> {
                finish()
            }

            R.id.label_hint -> {
                if (!DataConstants.sGroupMap?.get(groupId!!)?.members?.containsKey("9f19bxizDuYx95PfkBe3N7uamu92")!!) {
                    MyChatManager.addMemberToAGroup(object : NotifyMeInterface {
                        override fun handleData(`object`: Any, requestCode: Int?) {
                            selectedUserList?.add(DataConstants.userMap?.get("9f19bxizDuYx95PfkBe3N7uamu92")!!)
                            /*     adapter = ParticipantsAdapter(object : NotifyMeInterface {
                                     override fun handleData(`object`: Any, requestCode: Int?) {
                                         tv_no_of_participants.setText("" + selectedUserList?.size!! + " Participants")
                                     }

                                 }, AppConstants.CREATION, groupId!!)*/
                            adapter?.notifyDataSetChanged()
                        }

                    }, groupId, DataConstants.userMap?.get("9f19bxizDuYx95PfkBe3N7uamu92"))
                } else {
                    Toast.makeText(this@GroupDetailsActivity, "Akash is already in the group", Toast.LENGTH_LONG).show()
                }
            }


            R.id.tv_exit_group -> {
                MyChatManager.setmContext(this@GroupDetailsActivity)
                MyChatManager.removeMemberFromGroup(object : NotifyMeInterface {
                    override fun handleData(`object`: Any, requestCode: Int?) {

                        DataConstants.sGroupMap?.get(groupId)?.members?.remove(DataConstants.sCurrentUser?.uid)

                        Toast.makeText(this@GroupDetailsActivity, "You have been exited from group", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@GroupDetailsActivity, ViewGroupsActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }

                }, groupId, DataConstants.sCurrentUser?.uid)
            }
        }
    }

    private fun updateName() {
        if (!et_groupname.text.isBlank() && et_groupname.text.length > 2) {
            var mFirebaseDatabaseReference: DatabaseReference? = FirebaseDatabase.getInstance().reference.child(FirebaseConstants.GROUP).child(groupId)
            mFirebaseDatabaseReference?.child(FirebaseConstants.NAME)?.setValue(et_groupname.text.toString())
            Toast.makeText(this@GroupDetailsActivity, "Name Updated successful", Toast.LENGTH_LONG).show()
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
        var adminUserModel: UserModel? = SharedPrefManager.getInstance(this@GroupDetailsActivity).savedUserModel
        adminUserModel?.admin = true

        var groupMembers: HashMap<String, UserModel> = hashMapOf()


        for (user in selectedUserList!!) {
            groupMembers.put(user.uid!!, user)
        }


        groupMembers.put(adminUserModel?.uid!!, adminUserModel)

        newGroup.members = groupMembers

        MyChatManager.setmContext(this@GroupDetailsActivity)

        if (isValid) {

            //sendFileFirebase(storageRef, resultUri!!, groupId!!)

            MyChatManager.createGroup(object : NotifyMeInterface {
                override fun handleData(`object`: Any, requestCode: Int?) {
                    Toast.makeText(this@GroupDetailsActivity, "Group has been created successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@GroupDetailsActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }

            }, newGroup, NetworkConstants.CREATE_GROUP)
        } else {
            Toast.makeText(this@GroupDetailsActivity, errorMessage, Toast.LENGTH_SHORT).show()
        }


    }


    private fun sendFileFirebase(storageReference: StorageReference?, file: File, groupId: String) {
        if (storageReference != null) {
            var mFirebaseDatabaseReference: DatabaseReference? = FirebaseDatabase.getInstance().reference.child(FirebaseConstants.GROUP).child(groupId)
            val uploadTask = storageReference.putFile(Uri.fromFile(file))
            uploadTask.addOnFailureListener { e -> Log.e("", "onFailure sendFileFirebase " + e.message) }.addOnSuccessListener { taskSnapshot ->
                Log.i("", "onSuccess sendFileFirebase")
                val downloadUrl = taskSnapshot.downloadUrl
                mFirebaseDatabaseReference?.child(FirebaseConstants.IMAGE_URL)?.setValue(downloadUrl)
            }
        } else {
            //IS NULL
        }

    }

    fun sendFileFirebase(storageReference: StorageReference?, file: Uri, groupId: String) {
        if (storageReference != null) {

            val name = DateFormat.format("yyyy-MM-dd_hhmmss", Date()).toString()
            val imageGalleryRef = storageReference.child(name + "_gallery")
            val uploadTask = imageGalleryRef.putFile(file)
            uploadTask.addOnFailureListener { e -> Log.e("", "onFailure sendFileFirebase " + e.message) }.addOnSuccessListener { taskSnapshot ->
                Log.i("", "onSuccess sendFileFirebase")
                val downloadUrl = taskSnapshot.downloadUrl
                val fileModel = FileModel("img", downloadUrl!!.toString(), name, "")

                // val chatModel = MessageModel(tfUserModel.getUserId(), ffUserModel.getUserId(), ffUserModel, Calendar.getInstance().time.time.toString() + "", fileModel)
                FirebaseDatabase.getInstance().reference.child(FirebaseConstants.GROUP).
                        child(groupId).child(FirebaseConstants.IMAGE_URL)?.setValue(downloadUrl.toString())
                Toast.makeText(this@GroupDetailsActivity, "Group Image Updated successful", Toast.LENGTH_LONG).show()
            }
        } else {
            //IS NULL
        }

    }

}

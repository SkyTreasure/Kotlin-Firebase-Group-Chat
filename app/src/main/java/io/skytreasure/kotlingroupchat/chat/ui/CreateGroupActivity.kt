package io.skytreasure.kotlingroupchat.chat.ui

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
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
import kotlinx.android.synthetic.main.activity_create_group.*

import io.skytreasure.kotlingroupchat.R
import io.skytreasure.kotlingroupchat.chat.MyChatManager
import io.skytreasure.kotlingroupchat.chat.model.UserModel
import io.skytreasure.kotlingroupchat.chat.ui.adapter.UserListAdapter
import io.skytreasure.kotlingroupchat.chat.ui.adapter.UserSelectionAdapter
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.mapList
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.selectedUserList
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.userList
import io.skytreasure.kotlingroupchat.common.constants.NetworkConstants
import io.skytreasure.kotlingroupchat.common.controller.NotifyMeInterface
import java.io.ByteArrayOutputStream
import java.io.IOException

class CreateGroupActivity : AppCompatActivity(), View.OnClickListener {


    var progressDialog: ProgressDialog? = null
    var adapter: UserListAdapter? = null
    var secondaryAdapter: UserSelectionAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)
        MyChatManager.setmContext(this@CreateGroupActivity)
        progressDialog = ProgressDialog(this)
        progressDialog!!.show()
        MyChatManager.getAllUsersFromFirebase(object : NotifyMeInterface {
            override fun handleData(`asv`: Any, requestCode: Int?) {
                userList = `asv` as ArrayList<UserModel>
                selectedUserList?.clear();
                rv_user_list.layoutManager = LinearLayoutManager(this@CreateGroupActivity) as RecyclerView.LayoutManager?
                adapter = UserListAdapter(this@CreateGroupActivity, userSelectionInterface)
                rv_user_list.adapter = adapter

                rv_selected_user.layoutManager = LinearLayoutManager(this@CreateGroupActivity, LinearLayoutManager.HORIZONTAL, false);
                secondaryAdapter = UserSelectionAdapter(this@CreateGroupActivity, userRemovedFromSelection)
                rv_selected_user.adapter = secondaryAdapter

                progressDialog!!.hide()
            }

        }, NetworkConstants.GET_ALL_USERS_REQUEST)

        tv_next.setOnClickListener(this@CreateGroupActivity)


    }


    private var userSelectionInterface = object : NotifyMeInterface {
        override fun handleData(`object`: Any, requestCode: Int?) {
            when (requestCode) {
                NetworkConstants.USER_REMOVED -> {
                    selectedUserList?.remove(`object` as UserModel)
                    secondaryAdapter?.notifyDataSetChanged()
                }
                NetworkConstants.USER_ADDED -> {
                    selectedUserList?.add(`object` as UserModel)
                    secondaryAdapter?.notifyDataSetChanged()
                }

            }
            if (selectedUserList?.size!! > 0) {
                rv_selected_user.visibility = View.VISIBLE
            } else {
                rv_selected_user.visibility = View.GONE
            }
        }
    }

    private var userRemovedFromSelection = object : NotifyMeInterface {
        override fun handleData(`object`: Any, requestCode: Int?) {
            when (requestCode) {
                NetworkConstants.USER_REMOVED -> {
                    adapter?.resetView(`object` as String)
                }
            }
        }

    }






    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_next -> {
                if (selectedUserList?.size!! > 1 && selectedUserList?.size!! <= 6) {
                    val intent = Intent(this@CreateGroupActivity, NewGroupActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@CreateGroupActivity, "Number of members should be more than 2 and less than 7", Toast.LENGTH_LONG).show()
                }

            }
        }
    }
}

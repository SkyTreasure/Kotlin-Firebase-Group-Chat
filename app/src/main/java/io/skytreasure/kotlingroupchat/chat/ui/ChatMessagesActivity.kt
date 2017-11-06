package io.skytreasure.kotlingroupchat.chat.ui

import android.Manifest
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.format.DateFormat
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask

import io.skytreasure.kotlingroupchat.R
import io.skytreasure.kotlingroupchat.chat.MyChatManager
import io.skytreasure.kotlingroupchat.chat.model.FileModel
import io.skytreasure.kotlingroupchat.chat.model.LocationModel
import io.skytreasure.kotlingroupchat.chat.model.MessageModel
import io.skytreasure.kotlingroupchat.chat.model.UserModel
import io.skytreasure.kotlingroupchat.chat.ui.ViewHolders.ViewHolder
import io.skytreasure.kotlingroupchat.chat.ui.adapter.ChatMessagesRecyclerAdapter
import io.skytreasure.kotlingroupchat.chat.ui.widget.EndlessRecyclerViewScrollListener
import io.skytreasure.kotlingroupchat.chat.ui.widget.InfiniteFirebaseRecyclerAdapter
import io.skytreasure.kotlingroupchat.common.constants.AppConstants
import io.skytreasure.kotlingroupchat.common.constants.DataConstants
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.groupMembersMap
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.sMyGroups
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.sCurrentUser
import io.skytreasure.kotlingroupchat.common.constants.FirebaseConstants
import io.skytreasure.kotlingroupchat.common.constants.NetworkConstants
import io.skytreasure.kotlingroupchat.common.controller.NotifyMeInterface
import io.skytreasure.kotlingroupchat.common.util.MyTextUtil
import io.skytreasure.kotlingroupchat.common.util.MyViewUtils
import kotlinx.android.synthetic.main.activity_chat_messages.*
import java.io.File
import java.util.*
import kotlin.concurrent.schedule

class ChatMessagesActivity : AppCompatActivity(), View.OnClickListener {


    var adapter: ChatMessagesRecyclerAdapter? = null
    var groupId: String? = ""
    var position: Int? = 0
    var progressDialog: ProgressDialog? = null
    var mFirebaseDatabaseReference: DatabaseReference? = null
    var mLinearLayoutManager: LinearLayoutManager? = null
    var storage = FirebaseStorage.getInstance()
    var type: String? = ""

    val IMAGE_GALLERY_REQUEST = 1
    val IMAGE_CAMERA_REQUEST = 2
    val PLACE_PICKER_REQUEST = 3

    var user2: UserModel = UserModel()
    var user2Id: String = ""

    var groupIsPresent: Boolean = false

    //File
    var filePathImageCamera: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_messages)

        progressDialog?.show()

        groupId = intent.getStringExtra(AppConstants.GROUP_ID)
        position = intent.getIntExtra(AppConstants.POSITION, 1)
        type = intent.getStringExtra(AppConstants.CHAT_TYPE)
        tv_loadmore.setOnClickListener(this)

        MyChatManager.setmContext(this@ChatMessagesActivity)

        when (type) {
            AppConstants.GROUP_CHAT -> {

                if (groupId != null) {
                    chat_room_title.text = sMyGroups?.get(position!!)?.name
                    mLinearLayoutManager = LinearLayoutManager(this)
                    mLinearLayoutManager!!.setStackFromEnd(true)
                    //  chat_messages_recycler.layoutManager = mLinearLayoutManager

                    progressDialog?.show()
                    btnSend.setOnClickListener(this)
                    iv_back.setOnClickListener(this)
                    chat_room_title.setOnClickListener(this)
                    iv_attach.setOnClickListener(this)

                    MyChatManager.fetchGroupMembersDetails(object : NotifyMeInterface {
                        override fun handleData(`object`: Any, requestCode: Int?) {
                            readMessagesFromFirebase(groupId!!)
                            getLastMessageAndUpdateUnreadCount()
                        }

                    }, NetworkConstants.FETCH_GROUP_MEMBERS_DETAILS, groupId)
                }


            }

            AppConstants.ONE_ON_ONE_CHAT -> {
                mLinearLayoutManager = LinearLayoutManager(this)
                mLinearLayoutManager!!.setStackFromEnd(true)
                btnSend.setOnClickListener(this)
                iv_back.setOnClickListener(this)
                chat_room_title.setOnClickListener(this)
                iv_attach.setOnClickListener(this)

                btnSend.visibility = View.GONE
                user2Id = intent.getStringExtra(AppConstants.USER_ID)
                var userModel = DataConstants.userMap?.get(user2Id)
                chat_room_title.text = userModel?.name

                checkIfGroupExistsOrNot();

            }
        }


    }

    /**
     * Check if group exists, if exists then download the chat data.
     * If group doesn't exists, then wait till the first message is sent.
     *
     *
     * When first message is sent.
     *
     * Check if user2 node is present or not, if not create user2 node. Then,
     *
     * Create a groupID of these two by calling getHash(uid1,uid2)
     *
     * Create a group with 2 members, group flag set to false. Then add group id in user1, user2 to be true
     *
     * Then add the message under the MESSAGE->GROUPID.
     */
    private fun checkIfGroupExistsOrNot() {
        groupId = MyTextUtil().getHash(sCurrentUser?.uid!!, user2Id)

        MyChatManager.checkIfGroupExists(object : NotifyMeInterface {
            override fun handleData(`object`: Any, requestCode: Int?) {
                if (`object` as Boolean) {
                    //Exists so fetch the data
                    readMessagesFromFirebase(groupId!!)
                    tv_last_seen.visibility = View.VISIBLE
                    user2 = DataConstants.userMap?.get(user2Id)!!
                    if (user2.online!!) {
                        tv_last_seen.setText("Online")
                    } else if (user2.last_seen_online != null) {
                        tv_last_seen.setText(MyTextUtil().getTimestamp(user2.last_seen_online!!.toLong()))
                    } else {
                        tv_last_seen.visibility = View.GONE
                    }
                    groupIsPresent = true
                } else {
                    //Doesn't exists wait till first message is sent (Do nothing)
                    user2 = DataConstants.userMap?.get(user2Id)!!
                    MyChatManager.createOrUpdateUserNode(object : NotifyMeInterface {
                        override fun handleData(`object`: Any, requestCode: Int?) {
                            user2 = `object` as UserModel
                            //createGroupOfTwo(user2, null)
                        }
                    }, user2, NetworkConstants.CREATE_USER_NODE)
                }
            }

        }, groupId!!, NetworkConstants.CHECK_GROUP_EXISTS)


    }


    fun getLastMessageAndUpdateUnreadCount() {
        MyChatManager.fetchLastMessageFromGroup(object : NotifyMeInterface {
            override fun handleData(`object`: Any, requestCode: Int?) {
                var lastMessage: MessageModel? = `object` as MessageModel
                if (lastMessage != null) {
                    MyChatManager.updateUnReadCountLastSeenMessageTimestamp(groupId, lastMessage!!)
                }
            }

        }, NetworkConstants.FETCH_MESSAGES, groupId)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnSend -> {
                var message: String = et_chat.text.toString()
                if (!message.isEmpty()) {
                    sendMessage(message!!)
                }
            }

            R.id.tv_loadmore -> {
                /* newAdapter?.more()
                 tv_loadmore.visibility = View.GONE*/
            }

            R.id.iv_back -> {
                finish()
            }

            R.id.chat_room_title -> {
                val intent = Intent(this@ChatMessagesActivity, GroupDetailsActivity::class.java)
                intent.putExtra(AppConstants.GROUP_ID, groupId)
                startActivity(intent)

            }

            R.id.iv_attach -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Select the Type of attachment")
                        .setItems(R.array.attachment_type, DialogInterface.OnClickListener { dialog, which ->
                            when (which) {
                                0 ->
                                    //Camera
                                    photoCameraIntent()
                                1 ->
                                    //Gallery
                                    photoGalleryIntent()
                                2 ->
                                    //Location
                                    locationPlacesIntent()
                                3 ->
                                    //Delete Chat messages of this group
                                    deleteChatMessagesOfThisGroup()
                            }
                        })


                val resultFL = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                if (resultFL == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(this@ChatMessagesActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            100)
                } else {
                    builder.create().show()
                }
            }
        }
    }

    private fun deleteChatMessagesOfThisGroup() {
        MyChatManager.setmContext(this@ChatMessagesActivity)
        MyChatManager.deleteGroupChat(object : NotifyMeInterface {
            override fun handleData(`object`: Any, requestCode: Int?) {
                if (`object` as Boolean) {

                    Handler().postDelayed({
                        readMessagesFromFirebase(groupId!!)
                    }, 2000)


                }
            }

        }, groupId)
    }


    private fun locationPlacesIntent() {
        try {
            val builder = PlacePicker.IntentBuilder()
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST)
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }

    }

    private fun photoCameraIntent() {
        val nomeFoto = DateFormat.format("yyyy-MM-dd_hhmmss", Date()).toString()
        filePathImageCamera = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), nomeFoto + "camera.jpg")
        val it = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        it.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(filePathImageCamera))
        startActivityForResult(it, IMAGE_CAMERA_REQUEST)
    }

    private fun photoGalleryIntent() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Get Photo From"), IMAGE_GALLERY_REQUEST)
    }

    /**
     * Check if group exists, if exists then download the chat data.
     * If group doesn't exists, then wait till the first message is sent.
     *
     *
     * When first message is sent.
     *
     * Check if user2 node is present or not, if not create user2 node. Then,
     *
     * Create a groupID of these two by calling getHash(uid1,uid2)
     *
     * Create a group with 2 members, group flag set to false. Then add group id in user1, user2 to be true
     *
     * Then add the message under the MESSAGE->GROUPID.
     */
    fun sendMessage(message: String) {

        when (type) {
            AppConstants.ONE_ON_ONE_CHAT -> {
                if (groupIsPresent) {
                    sendMessageToGroup(message)
                } else {
                    /* if (DataConstants.userMap?.containsKey(user2Id)!!) {
                         user2 = DataConstants.userMap?.get(user2Id)!!
                         //createGroupOfTwo(sCurrentUser, user2)
                     } else {
                         //User2 node is not there so create one.
                         user2.email = "sky.wall.treasure@gmail.com"
                         user2.name = "Sky Wall Treasure"
                         user2.image_url = "https://lh6.googleusercontent.com/-x8DMU8TwQWU/AAAAAAAAAAI/AAAAAAAALQA/waA51g0k3GA/s96-c/photo.jpg"
                         user2.uid = user2Id
                         user2.group = hashMapOf()*/


                }
            }

            AppConstants.GROUP_CHAT -> {
                sendMessageToGroup(message)
            }
        }


    }

    private fun createGroupOfTwo(user2: UserModel, message: String?) {

        MyChatManager.createOneOnOneChatGroup(object : NotifyMeInterface {
            override fun handleData(`object`: Any, requestCode: Int?) {
                groupIsPresent = true
                readMessagesFromFirebase(groupId!!)
                btnSend.visibility = View.VISIBLE
                if (message != null) {
                    sendMessageToGroup(message)
                }


            }

        }, user2Id, user2, NetworkConstants.CREATE_ONE_ON_ONE_GROUP)
    }

    private fun sendMessageToGroup(message: String) {
        var cal: Calendar = Calendar.getInstance()
        var read_status_temp: HashMap<String, Boolean> = hashMapOf()

        /*   for (member in groupMembersMap?.get(groupId!!)!!) {
               if (member.uid == sCurrentUser?.uid) {
                   read_status_temp.put(member.uid!!, true)
               } else {
                   read_status_temp.put(member.uid!!, false)
               }
           }*/

        var messageModel: MessageModel? = MessageModel(message, sCurrentUser?.uid, cal.timeInMillis.toString(),
                read_status = read_status_temp)

        MyChatManager.sendMessageToAGroup(object : NotifyMeInterface {

            override fun handleData(`object`: Any, requestCode: Int?) {
                et_chat.setText("")
                Handler().postDelayed({

                    chat_messages_recycler.scrollToPosition(newAdapter?.mSnapshots?.mSnapshots?.count()!!)
                }, 1000)

            }

        }, NetworkConstants.SEND_MESSAGE_REQUEST, groupId, messageModel)
    }

    override fun onStop() {
        super.onStop()
        getLastMessageAndUpdateUnreadCount()
    }


    var newAdapter: InfiniteFirebaseRecyclerAdapter<MessageModel, ViewHolder>? = null

    private fun readMessagesFromFirebase(groupId: String) {
        var currentGroup = DataConstants.sGroupMap?.get(groupId)
        var time = Calendar.getInstance().timeInMillis
        var deleteTill: String = currentGroup?.members?.get(sCurrentUser?.uid)?.delete_till!!
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().reference

        var itemCount: Int = 10

        var ref: Query = mFirebaseDatabaseReference?.child(FirebaseConstants.MESSAGES)
                ?.child(groupId)!!



        newAdapter = object : InfiniteFirebaseRecyclerAdapter<MessageModel, ViewHolder>(MessageModel::class.java, R.layout.item_chat_row, ViewHolder::class.java, ref, itemCount, deleteTill, chat_messages_recycler) {
            override fun populateViewHolder(viewHolder: ViewHolder?, model: MessageModel?, position: Int) {
                val viewHolder = viewHolder as ViewHolder
                val chatMessage = model!!

                if (chatMessage.sender_id.toString() == sCurrentUser?.uid) {
                    viewHolder.llParent.gravity = Gravity.END
                    viewHolder.llChild.background =
                            ContextCompat.getDrawable(this@ChatMessagesActivity, R.drawable.chat_bubble_grey_sender)
                    viewHolder.name.text = "You"
                } else {
                    viewHolder.llParent.gravity = Gravity.START
                    viewHolder.name.text = DataConstants.userMap?.get(chatMessage.sender_id!!)?.name
                    viewHolder.llChild.background = ContextCompat.getDrawable(viewHolder.llParent.context, R.drawable.chat_bubble_grey)
                }
                viewHolder.message.text = chatMessage.message
                try {
                    viewHolder.timestamp.text = MyTextUtil().getTimestamp(chatMessage.timestamp?.toLong()!!)
                } catch (e: Exception) {
                    e.printStackTrace()
                }



                viewHolder.rlName.layoutParams.width = viewHolder.message.layoutParams.width
            }

            override fun getItemCount(): Int {
                return super.getItemCount()
            }

        }


        chat_messages_recycler.setLayoutManager(mLinearLayoutManager)
        //chat_messages_recycler.setAdapter(firebaseAdapter)
        chat_messages_recycler.setAdapter(newAdapter)
        chat_messages_recycler.scrollToPosition(itemCount)
        btnSend.visibility = View.VISIBLE
        progressDialog?.hide()


        chat_messages_recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (IsRecyclerViewAtTop() && newState == RecyclerView.SCROLL_STATE_IDLE) {

                    newAdapter?.more()
                }
            }

            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {


            }
        })


    }

    private fun IsRecyclerViewAtTop(): Boolean {
        return if (chat_messages_recycler.getChildCount() == 0) true else chat_messages_recycler.getChildAt(0).getTop() == 0
    }

    protected override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {

        val storageRef = storage.getReferenceFromUrl(NetworkConstants.URL_STORAGE_REFERENCE).child(NetworkConstants.FOLDER_STORAGE_IMG)

        if (requestCode == IMAGE_GALLERY_REQUEST) {
            if (resultCode == RESULT_OK) {
                val selectedImageUri = data.data
                if (selectedImageUri != null) {
                    sendFileFirebase(storageRef, selectedImageUri)
                } else {
                    //URI IS NULL
                }
            }
        } else if (requestCode == IMAGE_CAMERA_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (filePathImageCamera != null && filePathImageCamera!!.exists()) {
                    val imageCameraRef = storageRef.child(filePathImageCamera!!.getName() + "_camera")
                    sendFileFirebase(imageCameraRef, filePathImageCamera!!)
                } else {
                    //IS NULL
                }
            }
        } else if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                val place = PlacePicker.getPlace(this, data)
                if (place != null) {
                    val latLng = place.latLng
                    val mapModel = LocationModel(latLng.latitude.toString() + "", latLng.longitude.toString() + "")
                    //val chatModel = MessageModel(tfUserModel.getUserId(), ffUserModel.getUserId(), ffUserModel, Calendar.getInstance().time.time.toString() + "", mapModel)
                    // mFirebaseDatabaseReference.child(deedId).child(CHAT_REFERENCE).child(seekerProviderKey).push().setValue(chatModel)
                } else {
                    //PLACE IS NULL
                }
            }
        }

    }


    private fun sendFileFirebase(storageReference: StorageReference?, file: File) {
        if (storageReference != null) {
            val uploadTask = storageReference.putFile(Uri.fromFile(file))
            uploadTask.addOnFailureListener { e -> Log.e("", "onFailure sendFileFirebase " + e.message) }.addOnSuccessListener { taskSnapshot ->
                Log.i("", "onSuccess sendFileFirebase")
                val downloadUrl = taskSnapshot.downloadUrl
                val fileModel = FileModel("img", downloadUrl!!.toString(), file.name, file.length().toString() + "")
                //  val chatModel = MessageModel(tfUserModel.getUserId(), ffUserModel.getUserId(), ffUserModel, Calendar.getInstance().time.time.toString() + "", fileModel)
                //  mFirebaseDatabaseReference.child(deedId).child(CHAT_REFERENCE).child(seekerProviderKey).push().setValue(chatModel)
            }
        } else {
            //IS NULL
        }

    }


    private fun sendFileFirebase(storageReference: StorageReference?, file: Uri) {
        if (storageReference != null) {
            val name = DateFormat.format("yyyy-MM-dd_hhmmss", Date()).toString()
            val imageGalleryRef = storageReference.child(name + "_gallery")
            val uploadTask = imageGalleryRef.putFile(file)
            uploadTask.addOnFailureListener { e -> Log.e("", "onFailure sendFileFirebase " + e.message) }.addOnSuccessListener { taskSnapshot ->
                Log.i("", "onSuccess sendFileFirebase")
                val downloadUrl = taskSnapshot.downloadUrl
                val fileModel = FileModel("img", downloadUrl!!.toString(), name, "")
                //   val chatModel = MessageModel(tfUserModel.getUserId(), ffUserModel.getUserId(), ffUserModel, Calendar.getInstance().time.time.toString() + "", fileModel)
                // mFirebaseDatabaseReference.child(deedId).child(CHAT_REFERENCE).child(seekerProviderKey).push().setValue(chatModel)
            }
        } else {
            //IS NULL
        }

    }

}

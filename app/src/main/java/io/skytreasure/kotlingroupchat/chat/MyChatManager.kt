package io.skytreasure.kotlingroupchat.chat

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.Gson
import io.skytreasure.kotlingroupchat.chat.model.GroupModel
import io.skytreasure.kotlingroupchat.chat.model.MessageModel
import io.skytreasure.kotlingroupchat.chat.model.UserModel
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.sGroupMap
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.groupMembersMap
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.groupMessageMap
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.sCurrentUser
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.userMap
import io.skytreasure.kotlingroupchat.common.constants.FirebaseConstants
import io.skytreasure.kotlingroupchat.common.constants.PrefConstants
import io.skytreasure.kotlingroupchat.common.controller.NotifyMeInterface
import io.skytreasure.kotlingroupchat.common.util.SecurePrefs
import io.skytreasure.kotlingroupchat.common.util.SharedPrefManager
import java.util.*
import com.google.firebase.database.Transaction
import android.databinding.adapters.NumberPickerBindingAdapter.setValue
import android.provider.Settings
import com.google.firebase.messaging.FirebaseMessaging
import io.skytreasure.kotlingroupchat.common.constants.DataConstants
import io.skytreasure.kotlingroupchat.common.constants.NetworkConstants
import io.skytreasure.kotlingroupchat.common.util.MyTextUtil
import io.skytreasure.kotlingroupchat.login.LoginActivity
import kotlin.collections.HashMap


@SuppressLint("StaticFieldLeak")
/**
 * Created by akash on 23/10/17.
 */
object MyChatManager {

    val TAG = "MyChatManager"
    var sMyChatManager: MyChatManager? = null
    var sAuth: FirebaseAuth? = FirebaseAuth.getInstance()
    var sDatabase: FirebaseDatabase? = FirebaseDatabase.getInstance()
    var mAuthListener: FirebaseAuth.AuthStateListener? = null
    var isFirebaseAuthSuccessfull = false
    var firebaseUserId = ""
    var mFirebaseDatabaseReference: DatabaseReference? = FirebaseDatabase.getInstance().reference
    val gson = Gson()
    var mContext: Context? = null
    var mUserRef: DatabaseReference? = mFirebaseDatabaseReference?.child(FirebaseConstants.USERS)
    var mGroupRef: DatabaseReference? = mFirebaseDatabaseReference?.child(FirebaseConstants.GROUP)
    var mMessageRef: DatabaseReference? = mFirebaseDatabaseReference?.child(FirebaseConstants.MESSAGES)

    var groupListener: ValueEventListener? = null


    fun setmContext(mContext: Context) {
        this.mContext = mContext
    }

    fun init(mContext: Context) {
        this.mContext = mContext
        setupFirebaseAuth()
        signInToFirebaseAnonymously()
    }

    /**
     * Setup Firebase Auth and Database
     */
    fun setupFirebaseAuth() {
        if (sAuth == null)
            sAuth = FirebaseAuth.getInstance()
        if (sDatabase == null)
            sDatabase = FirebaseDatabase.getInstance()

        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                firebaseUserId = user.uid
                isFirebaseAuthSuccessfull = false
                signInToFirebaseAnonymously()
            }
        }
    }

    /**
     * Sign in to firebase Anonymously
     */
    fun signInToFirebaseAnonymously() {
        setupFirebaseAuth()
        if (!isFirebaseAuthSuccessfull) {
            sAuth?.signInAnonymously()?.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("", "signInAnonymously", task.exception)
                    isFirebaseAuthSuccessfull = false
                } else {
                    isFirebaseAuthSuccessfull = true
                }
            }?.addOnFailureListener { Log.w("", "signInAnonymously") }
        }

    }

    /*
    * Firebase ref = Firebase(url: "https://<YOUR-FIREBASE-APP>.firebaseio.com");
Firebase userRef = ref.child("user");
Map newUserData = new HashMap();
newUserData.put("age", 30);
newUserData.put("city", "Provo, UT");
userRef.updateChildren(newUserData);
    * */
    //TODO: Update multiple items at once
    /*
    * Firebase ref = new Firebase("https://<YOUR-FIREBASE-APP>.firebaseio.com");
// Generate a new push ID for the new post
Firebase newPostRef = ref.child("posts").push();
String newPostKey = newPostRef.getKey();
// Create the data we want to update
Map newPost = new HashMap();
newPost.put("title", "New Post");
newPost.put("content", "Here is my new post!");
Map updatedUserData = new HashMap();
updatedUserData.put("users/posts/" + newPostKey, true);
updatedUserData.put("posts/" + newPostKey, newPost);
// Do a deep-path update
ref.updateChildren(updatedUserData, new Firebase.CompletionListener() {
   @Override
   public void onComplete(FirebaseError firebaseError, Firebase firebase) {
       if (firebaseError != null) {
           System.out.println("Error updating data: " + firebaseError.getMessage());
       }
   }
});
    * */

    /**
     * Login if node is already present then just update the name and imageurl and don't alter any other field.
     *
     */
    fun loginCreateAndUpdate(callback: NotifyMeInterface?, userModel: UserModel?, requestType: Int?) {
        try {
            mUserRef?.child(userModel?.uid)?.runTransaction(object : Transaction.Handler {
                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    val p = mutableData.getValue<UserModel>(UserModel::class.java)
                    if (p == null) {
                        mutableData.setValue(userModel)
                    } else {
                        var newUserData: HashMap<String, Any?> = hashMapOf();
                        newUserData.put("image_url", userModel?.image_url)
                        newUserData.put("name", userModel?.name)
                        newUserData.put("online", true)
                        mUserRef?.child(userModel?.uid)?.updateChildren(newUserData)
                    }
                    return Transaction.success(mutableData)

                }

                override fun onComplete(databaseError: DatabaseError?, p1: Boolean, dataSnapshot: DataSnapshot?) {
                    try {
                        Log.d(TAG, "postTransaction:onComplete:" + databaseError)
                        //callback?.handleData(true, requestType)
                        var userModel: UserModel? = dataSnapshot?.getValue<UserModel>(UserModel::class.java)
                        fetchMyGroups(callback, requestType, userModel, true)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }


                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    fun updateFCMTokenAndDeviceId(context: Context, token: String) {
        var deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        var deviceIdMap: java.util.HashMap<String, String> = hashMapOf()
        deviceIdMap.put(deviceId, token)

        mUserRef?.child(sCurrentUser?.uid)?.child("deviceIds")?.setValue(deviceIdMap)
    }

    fun logout(context: Context) {
        var deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

        var deviceIdMap: MutableMap<String, Any> = hashMapOf()
        var userMap: MutableMap<String, Any?> = hashMapOf()

        deviceIdMap.put(deviceId, "")
        userMap.put("online", false)
        userMap.put("/deviceIds/", deviceIdMap)
        mUserRef?.child(sCurrentUser?.uid)?.updateChildren(userMap)

        SharedPrefManager.getInstance(context).cleanSharedPreferences()
        sCurrentUser = null
        val intent = Intent(context, LoginActivity::class.java)
        context.startActivity(intent)
    }


    /**
     * This creates the new user node
     *
     */
    fun createOrUpdateUserNode(callback: NotifyMeInterface?, userModel: UserModel?, requestType: Int?) {
        try {
            mUserRef?.child(userModel?.uid)?.runTransaction(object : Transaction.Handler {
                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    val p = mutableData.getValue<UserModel>(UserModel::class.java)
                    if (p == null) {
                        mutableData.setValue(userModel)
                    } else {
                        var newUserData: HashMap<String, Any?> = hashMapOf();
                        newUserData.put("image_url", userModel?.image_url)
                        newUserData.put("name", userModel?.name)
                        newUserData.put("online", true)
                        mUserRef?.child(userModel?.uid)?.updateChildren(newUserData)
                    }
                    return Transaction.success(mutableData)

                }

                override fun onComplete(databaseError: DatabaseError?, p1: Boolean, dataSnapshot: DataSnapshot?) {
                    try {
                        Log.d(TAG, "postTransaction:onComplete:" + databaseError)

                        var userModel: UserModel? = dataSnapshot?.getValue<UserModel>(UserModel::class.java)
                        callback?.handleData(userModel!!, requestType)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }


                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * Function to fetch current user
     */
    fun fetchCurrentUser(callback: NotifyMeInterface?, uid: String?, requestType: Int?) {
        try {
            if (mUserRef != null && uid != null) {
                mUserRef?.child(uid)?.addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {
                        callback?.handleData(false, requestType)
                    }

                    override fun onDataChange(p0: DataSnapshot?) {
                        var userModel: UserModel? = p0?.getValue<UserModel>(UserModel::class.java)
                        sCurrentUser = userModel
                        SharedPrefManager.getInstance(mContext!!).savePreferences(PrefConstants.USER_DATA, Gson().toJson(sCurrentUser))
                        callback?.handleData(true, requestType)
                    }

                })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    /**
     * This function is called to set user status to offline
     */
    fun goOffline(callback: NotifyMeInterface?, userModel: UserModel?, requestType: Int?) {
        mUserRef?.child(userModel?.uid)?.child(FirebaseConstants.ONLINE)?.setValue(false)
        callback?.handleData(true, requestType)
    }

    /**
     * Get user list from firebase
     */
    fun getAllUsersFromFirebase(callback: NotifyMeInterface?, requestType: Int?) {

        // Making a copy of listener
        val listener = object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {}
            override fun onDataChange(dataSnaphot: DataSnapshot) {
                if (dataSnaphot.exists()) {
                    var userList: ArrayList<UserModel> = ArrayList()
                    dataSnaphot.children.forEach { it ->
                        it.getValue<UserModel>(UserModel::class.java)?.let {
                            if (!SecurePrefs(mContext!!).get(PrefConstants.USER_ID).equals(it.uid)) {
                                userList.add(it)
                            }
                        }
                    }
                    callback?.handleData(userList, requestType)
                }
            }
        }

        mUserRef?.addValueEventListener(listener)

    }

    /**
     * This function creates a group in the firebase and adds an entry of group id under users and set it to
     * true.
     */
    fun createGroup(callback: NotifyMeInterface?, group: GroupModel, requestType: Int?) {

        val groupId = mGroupRef?.push()?.key
        group.groupId = groupId
        var time = Calendar.getInstance().timeInMillis

        for (user in group.members) {
            user.value.group = hashMapOf()
            user.value.email = null
            user.value.image_url = null
            user.value.name = null
            user.value.online = null
            user.value.unread_group_count = 0
            user.value.last_seen_message_timestamp = time.toString()
            user.value.delete_till = time.toString()
        }

        mGroupRef?.child(groupId)?.setValue(group)

        for (user in group.members) {
            mUserRef?.child(user.value.uid)?.child(FirebaseConstants.GROUP)?.child(groupId)?.setValue(true)
        }

        callback?.handleData(true, requestType)
    }

    /**
     * This function gets all the groups in which user is present.
     */
    fun fetchMyGroups(callback: NotifyMeInterface?, requestType: Int?, userModel: UserModel?, isSingleEvent: Boolean) {

        var i: Int = userModel?.group?.size!!
        if (i == 0) {
            //No Groups
            callback?.handleData(true, requestType)
        }
        groupListener = object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("", "")
            }

            override fun onDataChange(groupSnapshot: DataSnapshot) {
                if (groupSnapshot.exists()) {
                    var groupModel: GroupModel = groupSnapshot.getValue<GroupModel>(GroupModel::class.java)!!
                    var memberList: ArrayList<UserModel> = arrayListOf()
                    if(!groupModel.group_deleted!!){
                        for (member in groupModel.members) {
                            memberList.add(member.value)
                        }
                        groupMembersMap?.put(groupModel.groupId!!, memberList)
                        groupMessageMap?.put(groupModel.groupId!!, arrayListOf())
                        sGroupMap?.put(groupModel.groupId!!, groupModel)

                        FirebaseMessaging.getInstance().subscribeToTopic(groupModel.groupId!!);
                    }

                }
                i--
                if (i <= 0) {
                    callback?.handleData(true, requestType)
                }
            }
        }



        for (group in userModel.group) {

            if (group.value) {
                if (isSingleEvent) {
                    mGroupRef?.child(group.key)?.addListenerForSingleValueEvent(groupListener)
                } else {
                    mGroupRef?.child(group.key)?.addValueEventListener(groupListener)
                }
            } else {
                i--
            }
        }
        if (i <= 0) {
            callback?.handleData(true, requestType)
        }

    }


    /**
     * This function sends messages to a group
     */
    fun sendMessageToAGroup(callback: NotifyMeInterface?, requestType: Int?, groupId: String?,
                            messageModel: MessageModel?) {

        val messageKey = mMessageRef?.child(groupId)?.push()?.key
        messageModel?.message_id = messageKey

        mMessageRef?.child(groupId)?.child(messageKey)?.setValue(messageModel)

        mGroupRef?.child(groupId)?.child(FirebaseConstants.LAST_MESSAGE)?.setValue(messageModel)

        callback?.handleData(true, requestType)
        //TODO: Send Notification here.

        for (member in sGroupMap?.get(groupId)?.members!!) {
            mGroupRef?.child(groupId)?.child(FirebaseConstants.MEMBERS)?.child(member.value.uid)?.
                    runTransaction(object : Transaction.Handler {
                        override fun onComplete(p0: DatabaseError?, p1: Boolean, p2: DataSnapshot?) {
                            if (p0 != null) {
                                Log.d("INC", "Firebase counter increment failed.");
                            } else {
                                Log.d("INC", "Firebase counter increment succeeded.");
                            }
                        }

                        override fun doTransaction(mutabledata: MutableData?): Transaction.Result {
                            if (mutabledata?.getValue<UserModel>(UserModel::class.java)?.unread_group_count == null) {
                                var p = mutabledata?.getValue<UserModel>(UserModel::class.java)
                                p?.unread_group_count = 0
                                mutabledata?.setValue(p)
                            } else {
                                var p = mutabledata.getValue<UserModel>(UserModel::class.java)
                                p?.unread_group_count = p?.unread_group_count as Int + 1
                                mutabledata.setValue(p)
                            }

                            return Transaction.success(mutabledata)
                        }
                    })
        }

    }


    fun fetchGroupMembersDetails(callback: NotifyMeInterface?, requestType: Int?, groupId: String?) {
        var i: Int = groupMembersMap?.get(groupId)?.size!!
        for (member in groupMembersMap?.get(groupId)!!) {
            mUserRef?.child(member.uid)?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {
                    i--
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        var userModel: UserModel = snapshot.getValue<UserModel>(UserModel::class.java)!!
                        userMap?.put(userModel.uid!!, userModel)
                        i--
                        if (i == 0) {
                            callback?.handleData(true, requestType)
                        }
                    }
                }

            })
        }


    }


    fun fetchLastMessageFromGroup(callback: NotifyMeInterface?, requestType: Int?, groupId: String?) {

        /* val clistener = object : ChildEventListener {
             override fun onCancelled(databaseError: DatabaseError) {
                 callback?.handleData(false, requestType)
             }

             override fun onChildMoved(p0: DataSnapshot?, p1: String?) {}
             override fun onChildChanged(p0: DataSnapshot?, p1: String?) {}
             override fun onChildRemoved(p0: DataSnapshot?) {}
             override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                 if (dataSnapshot.exists()) {
                     //groupMessageMap?.get(groupId)?.clear()
                     dataSnapshot.getValue<MessageModel>(MessageModel::class.java)?.let {
                         groupMessageMap?.get(groupId)?.add(it)
                     }
                     callback?.handleData(true, requestType)
                 } else {
                     callback?.handleData(false, requestType)
                 }


             }
         }*/

        val listener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (dataSnapshot?.exists()!!) {

                    var lastMessage: MessageModel = dataSnapshot.children.elementAt(0).getValue<MessageModel>(MessageModel::class.java)!!


                    callback?.handleData(lastMessage, requestType)
                } else {
                    callback?.handleData(MessageModel(), requestType)
                }
            }

        }
        val lastQuery = mMessageRef?.child(groupId)?.orderByKey()?.limitToLast(1)

        lastQuery?.addListenerForSingleValueEvent(listener)
        // mMessageRef?.child(groupId)?.addChildEventListener(clistener)
    }

    /**
     * Call this function when user opens any chat groups.
     */
    fun updateUnReadCountLastSeenMessageTimestamp(groupId: String?, lastMessageModel: MessageModel) {

        /* mGroupRef?.child(groupId)?.child(FirebaseConstants.MEMBERS)?.
                 child(sCurrentUser?.uid)?.child(FirebaseConstants.UNREAD_GROUP_COUNT)?.setValue(0)

         mGroupRef?.child(groupId)?.child(FirebaseConstants.MEMBERS)?.
                 child(sCurrentUser?.uid)?.child(FirebaseConstants.L_S_M_T)?.setValue(lastMessageModel.timestamp)*/

        var groupMember: HashMap<String, Any?> = hashMapOf()
        groupMember.put(FirebaseConstants.UNREAD_GROUP_COUNT, 0)
        groupMember.put(FirebaseConstants.L_S_M_T, lastMessageModel.timestamp)


        mGroupRef?.child(groupId)?.child(FirebaseConstants.MEMBERS)?.
                child(sCurrentUser?.uid)?.updateChildren(groupMember);
        lastMessageModel.read_status = hashMapOf()
        mGroupRef?.child(groupId)?.child(FirebaseConstants.LAST_MESSAGE)?.setValue(lastMessageModel)
    }


    fun fetchAllUserInformation() {
        mUserRef?.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                if (p0?.exists()!!) {
                    p0.children.forEach { user ->
                        DataConstants.userMap?.put(user.key/*getValue<UserModel>(UserModel::class.java)?.uid!!*/, user.getValue<UserModel>(UserModel::class.java)!!)
                    }
                }
            }

        })
    }

    fun changeAdminStatusOfUser(callback: NotifyMeInterface?, groupId: String?, userId: String?, isAdmin: Boolean) {
        mGroupRef?.child(groupId)?.child(FirebaseConstants.MEMBERS)?.child(userId)?.child(FirebaseConstants.ADMIN)?.setValue(isAdmin)
        callback?.handleData(true, NetworkConstants.CHANGE_ADMIN_STATUS)
    }

    fun removeMemberFromGroup(callback: NotifyMeInterface?, groupId: String?, userId: String?) {
        mGroupRef?.child(groupId)?.child(FirebaseConstants.MEMBERS)?.child(userId)?.removeValue()
        mUserRef?.child(userId)?.child(FirebaseConstants.GROUP)?.child(groupId)?.setValue(false)
        callback?.handleData(true, 1)
    }

    fun addMemberToAGroup(callback: NotifyMeInterface?, groupId: String?, userModel: UserModel?) {
        //mUserRef?.child(userModel?.uid)?.child(FirebaseConstants.GROUP)?.child(groupId)?.setValue(true)

        var time = Calendar.getInstance().timeInMillis

        userModel?.group = hashMapOf()
        userModel?.email = null
        userModel?.image_url = null
        userModel?.name = null
        userModel?.online = null
        userModel?.unread_group_count = 0
        userModel?.last_seen_message_timestamp = time.toString()
        userModel?.delete_till = time.toString()

        mGroupRef?.child(groupId)?.child(FirebaseConstants.MEMBERS)?.child(userModel?.uid)?.setValue(userModel)

        mUserRef?.child(userModel?.uid)?.child(FirebaseConstants.GROUP)?.child(groupId)?.setValue(true)
        callback?.handleData(true, 1)
    }

    /*
     *
     * Create a groupID of these two by calling getHash(uid1,uid2)
     *
     * Create a group with 2 members, group flag set to false. Then add group id in user1, user2 to be true
     *
     * Then add the message under the MESSAGE->GROUPID.
     */
    fun createOneOnOneChatGroup(callback: NotifyMeInterface, user2Id: String, user2: UserModel, requestType: Int) {

        var newGroupId = MyTextUtil().getHash(sCurrentUser?.uid!!, user2Id)


        var group: GroupModel = GroupModel("", "", newGroupId, false, false)

        group.members.put(user2Id, user2)
        group.members.put(sCurrentUser?.uid!!, sCurrentUser!!)

        var time = Calendar.getInstance().timeInMillis

        for (user in group.members) {
            user.value.group = hashMapOf()
            user.value.email = null
            user.value.image_url = null
            user.value.name = null
            user.value.online = null
            user.value.unread_group_count = 0
            user.value.last_seen_message_timestamp = time.toString()
            user.value.delete_till = time.toString()
        }

        mGroupRef?.child(newGroupId)?.setValue(group)

        for (user in group.members) {
            mUserRef?.child(user.value.uid)?.child(FirebaseConstants.GROUP)?.child(newGroupId)?.setValue(true)
        }

        sGroupMap?.put(group.groupId!!, group)

        callback?.handleData(true, requestType)
    }

    /**
     * Check if a group exists or not
     */
    fun checkIfGroupExists(callback: NotifyMeInterface, groupId: String, requestType: Int) {
        try {

            mGroupRef?.child(groupId)?.runTransaction(object : Transaction.Handler {
                override fun doTransaction(p0: MutableData?): Transaction.Result {
                    val p = p0?.getValue<GroupModel>(GroupModel::class.java)
                    if (p == null) {

                    }
                    return Transaction.success(p0)
                }

                override fun onComplete(p0: DatabaseError?, p1: Boolean, p2: DataSnapshot?) {
                    val p = p2?.getValue<GroupModel>(GroupModel::class.java)
                    if (p == null) {
                        callback.handleData(false, requestType)
                    } else {
                        callback.handleData(true, requestType)
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * This function is used to update the current timestamp to delete chats
     */
    fun deleteGroupChat(callback: NotifyMeInterface?, groupId: String?) {
        var time = Calendar.getInstance().timeInMillis

        mGroupRef?.child(groupId!!)?.child("lastMessage")?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                if (p0?.exists()!!) {
                    var message: MessageModel = p0.getValue<MessageModel>(MessageModel::class.java)!!

                    mGroupRef?.child(groupId)?.child(FirebaseConstants.MEMBERS)?.child(sCurrentUser?.uid)
                            ?.child(FirebaseConstants.DELETE_TILL_TIMESTAMP)?.setValue(/*message.timestamp.toString()*/time.toString())
                    callback?.handleData(true, NetworkConstants.DELETE_GROUP_CHAT)
                }

            }

        })


    }

    /**
     * This function sets the online status to true under user node. And sets to false when
     * user exits the app.
     */
    fun setOnlinePresence() {
        FirebaseDatabase.getInstance().reference.child(".info/connected")
        var onlineRef = FirebaseDatabase.getInstance().reference.child(".info/connected")
        var currentUserRef = FirebaseDatabase.getInstance().reference.child("/users/" + sCurrentUser?.uid + "/online");
        var lastSeenRef = FirebaseDatabase.getInstance().reference.child("/users/" + sCurrentUser?.uid + "/last_seen_online");

        onlineRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("", "DataSnapshot:" + dataSnapshot)
                if (dataSnapshot.getValue(Boolean::class.java)!!) {
                    currentUserRef?.onDisconnect()?.setValue(false)
                    lastSeenRef?.onDisconnect()?.setValue(Calendar.getInstance().timeInMillis.toString())
                    currentUserRef?.setValue(true)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("", "DatabaseError:" + databaseError)
            }
        })
    }

}
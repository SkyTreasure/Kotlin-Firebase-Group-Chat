package io.skytreasure.kotlingroupchat.chat

import android.content.Context
import android.support.compat.BuildConfig
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.Gson
import io.skytreasure.kotlingroupchat.chat.model.GroupModel
import io.skytreasure.kotlingroupchat.chat.model.MessageModel
import io.skytreasure.kotlingroupchat.chat.model.UserModel
import io.skytreasure.kotlingroupchat.common.constants.DataConstants
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.groupMembersMap
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.groupMessageMap
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.myGroups
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.userMap
import io.skytreasure.kotlingroupchat.common.constants.FirebaseConstants
import io.skytreasure.kotlingroupchat.common.constants.PrefConstants
import io.skytreasure.kotlingroupchat.common.controller.NotifyMeInterface
import io.skytreasure.kotlingroupchat.common.util.SecurePrefs

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
                        fetchMyGroups(callback, requestType, userModel)
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

        for (user in group.members) {
            user.value.group = hashMapOf()
            user.value.email = null
            user.value.image_url = null
            user.value.name = null
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
    fun fetchMyGroups(callback: NotifyMeInterface?, requestType: Int?, userModel: UserModel?) {
        myGroups?.clear()

        var i: Int = userModel?.group?.size!!
        val groupListener = object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("", "")
            }

            override fun onDataChange(groupSnapshot: DataSnapshot) {
                if (groupSnapshot.exists()) {
                    var groupModel: GroupModel = groupSnapshot.getValue<GroupModel>(GroupModel::class.java)!!
                    var memberList: ArrayList<UserModel> = arrayListOf()
                    for (member in groupModel.members) {
                        memberList.add(member.value)
                    }
                    groupMembersMap?.put(groupModel.groupId!!, memberList)
                    groupMessageMap?.put(groupModel.groupId!!, arrayListOf())
                    myGroups?.add(groupModel)
                }
                i--
                if (i == 0) {
                    callback?.handleData(true, requestType)
                }
            }
        }

        for (group in userModel?.group!!) {
            if (group.value) {
                mGroupRef?.child(group.key)?.addListenerForSingleValueEvent(groupListener)
            }
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

        callback?.handleData(true, requestType)
        //TODO: Send Notification here.

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
                            fetchMessagesFromGroup(callback, requestType, groupId)
                        }
                    }
                }

            })
        }


    }


    fun fetchMessagesFromGroup(callback: NotifyMeInterface?, requestType: Int?, groupId: String?) {
        val listener = object : ChildEventListener {
            override fun onCancelled(databaseError: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot?, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot?) {}
            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                if (dataSnapshot.exists()) {
                    dataSnapshot.getValue<MessageModel>(MessageModel::class.java)?.let {
                        groupMessageMap?.get(groupId)?.add(it)
                    }
                    callback?.handleData(true, requestType)
                } else {
                    callback?.handleData(false, requestType)
                }


            }
        }
        mMessageRef?.child(groupId)?.addChildEventListener(listener)
    }


}
package io.skytreasure.kotlingroupchat

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.view.View
import android.widget.Toast
import io.skytreasure.kotlingroupchat.chat.MyChatManager
import io.skytreasure.kotlingroupchat.chat.ui.CreateGroupActivity
import io.skytreasure.kotlingroupchat.chat.ui.ViewGroupsActivity
import io.skytreasure.kotlingroupchat.common.constants.DataConstants
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.sCurrentUser
import io.skytreasure.kotlingroupchat.common.constants.NetworkConstants
import io.skytreasure.kotlingroupchat.common.controller.NotifyMeInterface
import io.skytreasure.kotlingroupchat.common.util.SharedPrefManager

class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MyChatManager.setmContext(this@MainActivity)
        sCurrentUser = SharedPrefManager.getInstance(this@MainActivity).savedUserModel

        btn_creategroup.setOnClickListener(this)
        btn_showgroup.setOnClickListener(this)

        MyChatManager.fetchAllUserInformation()

        MyChatManager.fetchMyGroups(object : NotifyMeInterface {
            override fun handleData(`object`: Any, requestCode: Int?) {
                var i = 0
                for (group in DataConstants.sGroupMap!!) {
                    if (group.value.members.containsKey(sCurrentUser?.uid!!)) {
                        i += group.value.members.get(sCurrentUser?.uid)?.unread_group_count!!
                    }

                }
                tv_notification_count.text = "Total Notification Count :" + i
            }

        }, NetworkConstants.FETCH_GROUPS, sCurrentUser, false)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_creategroup -> {
                val intent = Intent(this@MainActivity, CreateGroupActivity::class.java)
                startActivity(intent)
            }
            R.id.btn_showgroup -> {
                val intent = Intent(this@MainActivity, ViewGroupsActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onDestroy() {
        MyChatManager.goOffline(object : NotifyMeInterface {
            override fun handleData(`object`: Any, requestCode: Int?) {
                Toast.makeText(this@MainActivity, "You are offline now", Toast.LENGTH_SHORT).show()
            }

        }, sCurrentUser, NetworkConstants.GO_OFFLINE)
        super.onDestroy()
    }

}

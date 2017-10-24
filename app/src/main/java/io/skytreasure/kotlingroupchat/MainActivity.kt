package io.skytreasure.kotlingroupchat

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.view.View
import io.skytreasure.kotlingroupchat.chat.ui.CreateGroupActivity
import io.skytreasure.kotlingroupchat.chat.ui.ViewGroupsActivity
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.sCurrentUser
import io.skytreasure.kotlingroupchat.common.util.SharedPrefManager

class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sCurrentUser = SharedPrefManager.getInstance(this@MainActivity).savedUserModel

        btn_creategroup.setOnClickListener(this)
        btn_showgroup.setOnClickListener(this)
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

}

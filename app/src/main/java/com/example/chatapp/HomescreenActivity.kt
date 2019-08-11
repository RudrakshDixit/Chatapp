package com.example.chatapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.chatapp.New_message_screen.Companion.USER_KEY
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_message_screen.*
import kotlinx.android.synthetic.main.home_message_row.view.*
import kotlinx.android.synthetic.main.home_screen.*
import java.util.HashMap

class Mainscreenactivity : AppCompatActivity() {

    companion object{
        var currentuser: User? =null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_screen)
        val uid=FirebaseAuth.getInstance().uid
        if(uid==null){
           perfrom_signout()
        }

        home_recyclerview.adapter=adapter
        home_recyclerview.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))
        fetchcurrentuser()
        val actionBar = supportActionBar
        actionBar!!.title = "Home"
        // Display the app icon in action bar/toolbar
        actionBar.setDisplayShowHomeEnabled(true)
        actionBar.setDisplayUseLogoEnabled(true)
        adapter.setOnItemClickListener{item,view->
            val intent=Intent(this,Chat_log_activity::class.java)

            val row = item as LatestMessageRow

            intent.putExtra(USER_KEY,row.chatpartneruser)
            startActivity(intent)
        }
listenforlatestmessages()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.navmenu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            R.id.menu_logout -> {

                val builder= AlertDialog.Builder(this)
                val dialogview= layoutInflater.inflate(R.layout.progress_dialog,null)
                val msg= dialogview.findViewById<TextView>(R.id.textviewdialog)
                msg.text="Logging out Please wait...."
                builder.setView(dialogview)
                builder.setCancelable(false)
                val dialog=builder.create()
                dialog.show()

                FirebaseAuth.getInstance().signOut()

                if(FirebaseAuth.getInstance().uid==null){
                    dialog.dismiss()
                   perfrom_signout()
                }else{
                    dialog.dismiss()
                    Toast.makeText(this,"Error in logging out try again later",Toast.LENGTH_SHORT).show()
                }


            }
            R.id.menu_new_message -> {
                val intent=Intent(this,New_message_screen::class.java)
                startActivity(intent)
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun fetchcurrentuser(){
        val uid=FirebaseAuth.getInstance().uid
        val ref=FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                currentuser=p0.getValue(User::class.java)

            }

        })
    }
    private fun perfrom_signout(){
        val intent= Intent(this,RegisterActivity::class.java)
        intent.flags= Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    val adapter=GroupAdapter<ViewHolder>()
       
    fun refreshrecyclerview(){
        adapter.clear()
        latestMessageMap.values.forEach { 
            adapter.add(LatestMessageRow(it))
        }
    }
    private fun listenforlatestmessages(){

        val builder= AlertDialog.Builder(this)
        val dialogview= layoutInflater.inflate(R.layout.progress_dialog,null)
        val msg= dialogview.findViewById<TextView>(R.id.textviewdialog)
        msg.text="Loading your home screen"
        builder.setView(dialogview)
        builder.setCancelable(false)
        val dialog=builder.create()
        dialog.show()

        val fromid=FirebaseAuth.getInstance().uid
        val ref=FirebaseDatabase.getInstance().getReference("latest-messages/$fromid")
        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatmessage=p0.getValue(Chatmessage::class.java)?:return
                latestMessageMap[p0.key.toString()]=chatmessage
                dialog.dismiss()
                refreshrecyclerview()                
            }
            override fun onCancelled(p0: DatabaseError) {
            }
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatmessage=p0.getValue(Chatmessage::class.java)?:return
                latestMessageMap[p0.key.toString()]=chatmessage
                refreshrecyclerview()
            }
            override fun onChildRemoved(p0: DataSnapshot) {
            }

        })
    }
    val latestMessageMap:HashMap<String,Chatmessage> = HashMap()


    class LatestMessageRow(val chatmessage: Chatmessage): Item<ViewHolder>(){

        var chatpartneruser:User?=null

        override fun getLayout(): Int {
            return R.layout.home_message_row
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.message_homescreen.text=chatmessage.text
            val chatpartnerid:String
            if(chatmessage.fromid==FirebaseAuth.getInstance().uid){
                chatpartnerid=chatmessage.toid
            }else
                chatpartnerid=chatmessage.fromid

            val ref=FirebaseDatabase.getInstance().getReference("/users/$chatpartnerid")
            ref.addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    chatpartneruser=p0.getValue(User::class.java)
                    viewHolder.itemView.textview_home.text= chatpartneruser?.username
                    Picasso.get().load(chatpartneruser?.profileimage).into(viewHolder.itemView.imageview_home)
                }

            })
        }

    }
}

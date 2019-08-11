package com.example.chatapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log_activity.*
import kotlinx.android.synthetic.main.activity_new_message_screen.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class Chat_log_activity : AppCompatActivity() {
    val adapter =GroupAdapter<ViewHolder>()
    var touser: User?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log_activity)
        chat_log_recyclerview.adapter=adapter
        touser=intent.getParcelableExtra<User>(New_message_screen.USER_KEY)
        supportActionBar?.title=touser!!.username
        listenformessages()
        chat_log_button.setOnClickListener {
            perform_message_send()
            chat_log_edittext.text=null
            chat_log_recyclerview.scrollToPosition((adapter.itemCount-1))
        }




    }


    private fun listenformessages(){
        val fromid=FirebaseAuth.getInstance().uid
        val toid=touser?.uid
        val ref=FirebaseDatabase.getInstance().getReference("/user-messages/$fromid/$toid")

        ref.addChildEventListener(object: ChildEventListener{

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
               val chatmessage= p0.getValue(Chatmessage::class.java)
                if(chatmessage!=null){
                    if(chatmessage.fromid==FirebaseAuth.getInstance().uid){
                        val currentuser=Mainscreenactivity.currentuser

                        adapter.add(ChattoItem(chatmessage.text,currentuser!!))

                    }else{
                        adapter.add(ChatfromItem(chatmessage.text,touser!!))

                    }

                        chat_log_recyclerview.scrollToPosition(adapter.itemCount - 1)
                }
            }
            override fun onCancelled(p0: DatabaseError) {
            }
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }
            override fun onChildRemoved(p0: DataSnapshot) {
            }

        })
    }
    private fun perform_message_send(){
        val text=chat_log_edittext.text.toString()
        val fromid= FirebaseAuth.getInstance().uid
        val toid= touser!!.uid
        val reference= FirebaseDatabase.getInstance().getReference("/user-messages/$fromid/$toid").push()
        val toreference= FirebaseDatabase.getInstance().getReference("/user-messages/$toid/$fromid").push()

        if(fromid==null)return
        val chatMessage=Chatmessage(reference.key!!,text,fromid,toid,System.currentTimeMillis()/1000)
        reference.setValue(chatMessage).addOnSuccessListener {
        }.addOnFailureListener {
            Toast.makeText(this,"Error",Toast.LENGTH_SHORT).show()
        }
        toreference.setValue(chatMessage)

        val latestmessageref=FirebaseDatabase.getInstance().getReference("/latest-messages/$fromid/$toid")
        latestmessageref.setValue(chatMessage)
        val latestmessagetoref=FirebaseDatabase.getInstance().getReference("/latest-messages/$toid/$fromid")
        latestmessagetoref.setValue(chatMessage)
    }
}

class ChatfromItem(var text: String,val user:User): Item<ViewHolder>(){
    override fun getLayout(): Int {
 return R.layout.chat_from_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.from_row_textview.text=text
        val uri= user.profileimage
        Picasso.get().load(uri).into(viewHolder.itemView.from_row_image)
    }


}

class ChattoItem(var text:String,val user: User): Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.to_row_textview.text=text
        val uri= user.profileimage
        Picasso.get().load(uri).into(viewHolder.itemView.to_row_image)

    }


}
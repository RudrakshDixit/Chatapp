package com.example.chatapp

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_message_screen.*
import kotlinx.android.synthetic.main.user_card_view.view.*

class New_message_screen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message_screen)

        supportActionBar?.title="Select user"
        val adapter =GroupAdapter<ViewHolder>()


        homescreen_recyclerview.adapter = adapter
        fetchuser()
    }
    companion object{
        val USER_KEY="User_key"
    }
    private fun fetchuser(){
        val builder= AlertDialog.Builder(this)
        val dialogview= layoutInflater.inflate(R.layout.progress_dialog,null)
        val msg= dialogview.findViewById<TextView>(R.id.textviewdialog)
        msg.text="Loading users, Please wait.."
        builder.setView(dialogview)
        builder.setCancelable(false)
        val dialog=builder.create()
        dialog.show()
        val ref=FirebaseDatabase.getInstance().getReference("users/")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                dialog.dismiss()
            }

            override fun onDataChange(p0: DataSnapshot) {
                val adapter =GroupAdapter<ViewHolder>()

                p0.children.forEach {
                    val user=it.getValue(User::class.java)
                    if(user!=null){
                        adapter.add(UserItem(user))
                    }
                }

                dialog.dismiss()
                adapter.setOnItemClickListener{
                    item, view ->
                    val useritem= item as UserItem
                    val intent= Intent(view.context,Chat_log_activity::class.java)
                    intent.putExtra(USER_KEY,useritem.user)
                    startActivity(intent)
                    finish()
                }
                homescreen_recyclerview.adapter = adapter
            }

        })
    }
}

class UserItem(val user: User) : Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.user_card_view
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.cardview_textview.text=user.username
        Picasso.get().load(user.profileimage).into(viewHolder.itemView.cardview_imageview)
    }

}
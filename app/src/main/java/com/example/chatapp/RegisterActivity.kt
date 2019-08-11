package com.example.chatapp

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.progress_dialog.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Registration_button.setOnClickListener {
            performregister()


        }
        Gotologin.setOnClickListener {
            val i= Intent(this,LoginActivity::class.java)
                startActivity(i)
        }

        circularimageselect.setOnClickListener{
            Log.d("RegisterActivity","button clicked")
            val intent= Intent(Intent.ACTION_PICK)
            intent.type="image/*"
            startActivityForResult(intent,0)
        }

    }
 var selectedphotouri: Uri?= null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==0&&resultCode==Activity.RESULT_OK&&data!=null){
            selectedphotouri=data.data
            val bitmap= MediaStore.Images.Media.getBitmap(contentResolver,selectedphotouri)
            mainimageround.setImageBitmap(bitmap)
            circularimagedp.alpha =0f
        }

    }

    private fun performregister(){
        val builder= AlertDialog.Builder(this)
        val dialogview= layoutInflater.inflate(R.layout.progress_dialog,null)
        val msg= dialogview.findViewById<TextView>(R.id.textviewdialog)
        msg.text="Registering Please wait...."
        builder.setView(dialogview)
        builder.setCancelable(false)
        val dialog=builder.create()
        dialog.show()
        if(Registration_username.text.toString()!=""&&Registration_email.text.toString()!=""&&
            Registration_password.text.toString()!=""&&selectedphotouri!=null){
            if(Registration_password.length()>=6){
                val email=Registration_email.text.toString()
                val password=Registration_password.text.toString()

                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener{
                        if(it.isSuccessful){
                            dialog.dismiss()
                            Toast.makeText(this,"User created",Toast.LENGTH_SHORT).show()

                            uploadImage()
                            Toast.makeText(this,"User created",Toast.LENGTH_SHORT).show()

                        }else{
                            dialog.dismiss()

                            Toast.makeText(this,"Failed to create user",Toast.LENGTH_SHORT).show()

                        }
                    }.addOnFailureListener{
                        dialog.dismiss()

                        Toast.makeText(this,"${it.message}",Toast.LENGTH_SHORT).show()

                    }

            }else{
                dialog.dismiss()

                Toast.makeText(this,"Password length should be greater than 6",Toast.LENGTH_SHORT).show()

            }


        }else{
            dialog.dismiss()

            Toast.makeText(this,"Please fill all the details & select image to Register",Toast.LENGTH_SHORT).show()
        }

    }

    private fun uploadImage(){
        val builder= AlertDialog.Builder(this)
        val dialogview= layoutInflater.inflate(R.layout.progress_dialog,null)
        val msg= dialogview.findViewById<TextView>(R.id.textviewdialog)
        msg.text="Uploading image, it may take some time...."
        builder.setView(dialogview)
        builder.setCancelable(false)
        val dialog=builder.create()
        dialog.show()


        val filename= UUID.randomUUID().toString()
        val ref=FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedphotouri!!).addOnSuccessListener{
            Log.d("Register","${it.metadata?.path}")
            ref.downloadUrl.addOnSuccessListener {
                Log.d("Register","$it")
                saveusertodatabase(it.toString())
                dialog.dismiss()
            }.addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(this,"$it",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveusertodatabase(profileimageurl: String){
        val builder= AlertDialog.Builder(this)
        val dialogview= layoutInflater.inflate(R.layout.progress_dialog,null)
        val msg= dialogview.findViewById<TextView>(R.id.textviewdialog)
        msg.text="Just done, setting up your profile"
        builder.setView(dialogview)
        builder.setCancelable(false)
        val dialog=builder.create()
        dialog.show()

        val uid= FirebaseAuth.getInstance().uid ?: ""
       val ref= FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user= User(uid, Registration_username.text.toString(),profileimageurl)
        ref.setValue(user).addOnSuccessListener {
            Toast.makeText(this,"Profile Created",Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            val intent=Intent(this,Mainscreenactivity::class.java)
            intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)

        }.addOnFailureListener{
            Toast.makeText(this,"$it",Toast.LENGTH_SHORT).show()
            dialog.dismiss()


        }
    }

}

@Parcelize
class User(val uid: String,val username: String, val profileimage: String): Parcelable{
    constructor(): this("","","")
}
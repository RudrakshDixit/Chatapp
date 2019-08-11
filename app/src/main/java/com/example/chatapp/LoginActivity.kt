package com.example.chatapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        Login_button.setOnClickListener {
            performlogin()
    }
        GotoRegistration.setOnClickListener {
            val i= Intent(this,RegisterActivity::class.java)
            startActivity(i)
        }
    }


    private fun performlogin(){
        val builder= AlertDialog.Builder(this)
        val dialogview= layoutInflater.inflate(R.layout.progress_dialog,null)
        val msg= dialogview.findViewById<TextView>(R.id.textviewdialog)
        msg.text="Logging in, please wait.."
        builder.setView(dialogview)
        builder.setCancelable(false)
        val dialog=builder.create()
        dialog.show()
        if(Login_email.text.toString()!=""&&Login_password.text.toString()!=""){
            if(Login_password.length()>=6){
                val email=Login_email.text.toString()
                val password=Login_password.text.toString()
                Login_email.setText("")
                Login_password.setText("")

                FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password).addOnCompleteListener{
                    if(it.isSuccessful){

                        Toast.makeText(this,"Login Successful",Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        val intent=Intent(this,Mainscreenactivity::class.java)
                        intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)


                    }else{
                        Toast.makeText(this,"Error logging in ",Toast.LENGTH_SHORT).show()
                        dialog.dismiss()


                    }
                }.addOnFailureListener{
                    Toast.makeText(this,"${it.message}",Toast.LENGTH_SHORT).show()
                    dialog.dismiss()


                }

            }else{
                Toast.makeText(this,"Password length should be greater than 6",Toast.LENGTH_SHORT).show()
                dialog.dismiss()

            }
        }else{
            Toast.makeText(this,"Please fill all the details to Login",Toast.LENGTH_SHORT).show()
            dialog.dismiss()

        }
    }
}

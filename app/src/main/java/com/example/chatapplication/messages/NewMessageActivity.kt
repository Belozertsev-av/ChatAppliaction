package com.example.chatapplication.messages

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapplication.R
import com.example.chatapplication.databinding.ActivityNewMessageBinding
import com.example.chatapplication.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


class NewMessageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewMessageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewMessageBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.title = "Выберите пользователя"
        fetchUsers()
    }

    private fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance("https://chatapplication-666eb-default-rtdb.europe-west1.firebasedatabase.app/").getReference("/users")
        ref.addValueEventListener(object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val data = mutableListOf<User>()
                snapshot.children.forEach{

                    val user = it.getValue(User::class.java)
                    if (user != null) {
                        data.add(user)
                    }
                }
                binding.recycleViewNewMessage.adapter = CustomRecyclerAdapter(data)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("DebugMessage", "Failed")
            }

        })
    }
}

class CustomRecyclerAdapter(private val users: MutableList<User>) : RecyclerView.Adapter<CustomRecyclerAdapter.CustomViewHolder>() {

    class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatarImageView: CircleImageView = itemView.findViewById(R.id.imageViewAvatar)
        val nameTextView: TextView = itemView.findViewById(R.id.textViewName)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_person_list, parent, false)
        itemView.setOnClickListener{
            val intent = Intent(parent.context, ChatLogActivity::class.java)
            startActivity(parent.context, intent, Bundle.EMPTY)

            (parent.context as Activity).finish()
        }
        return CustomViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.nameTextView.text = users[position].username
        Picasso.get().load(users[position].profileImage).into(holder.avatarImageView)
    }

    override fun getItemCount() = users.size
}
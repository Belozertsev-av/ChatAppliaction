package com.example.chatapplication.messages

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapplication.R
import com.example.chatapplication.databinding.ActivityLatestMessagesBinding
import com.example.chatapplication.model.ChatMessageData
import com.example.chatapplication.model.LatestMessageData
import com.example.chatapplication.model.User
import com.example.chatapplication.reglogin.RegActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Date

class LatestMessagesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLatestMessagesBinding

    companion object {
        var currentUser: User? = null
        var latestMessagesDataSet: MutableList<LatestMessageData> = mutableListOf()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLatestMessagesBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        isUserLoggedIn()

        fetchCurrentUser()
        binding.recyclerViewLatestMessages.adapter = LatestMessagesAdapter(latestMessagesDataSet)
        listenLatestMessages()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.nav_menu, menu)
        supportActionBar?.title = ""
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_new_message -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }

            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun isUserLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, RegActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun fetchCurrentUser() {
        val ref =
            FirebaseDatabase.getInstance("https://chatapplication-666eb-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference(
                    "/users/${
                        FirebaseAuth
                            .getInstance().uid
                    }"
                )
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(User::class.java)
            }

            override fun onCancelled(error: DatabaseError) {
                currentUser = null
            }
        })
    }

    private fun getFromUser(chatMessage: ChatMessageData): User {
        val refFromUser =
            FirebaseDatabase.getInstance("https://chatapplication-666eb-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("/users/${chatMessage.fromId}")
        var user: User = User()
        refFromUser.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                user = snapshot.getValue(User::class.java) ?: return
            }

            override fun onCancelled(error: DatabaseError) {
                user = User()
            }
        })
        return user
    }

    val latestMessagesMap = HashMap<String, LatestMessageData>()

    private fun refreshRecyclerViewMessages() {
        latestMessagesDataSet.clear()
        latestMessagesMap.values.forEach {
            latestMessagesDataSet.add(LatestMessageData(it.user, it.text, it.date))
        }
    }

    private fun listenLatestMessages() {
        val ref =
            FirebaseDatabase.getInstance("https://chatapplication-666eb-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("/latest-messages/${currentUser?.uid}")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessageData::class.java) ?: return
                val fromUser: User = getFromUser(chatMessage)
                latestMessagesMap[snapshot.key!!] = LatestMessageData(
                    fromUser,
                    chatMessage.text,
                    chatMessage.timestamp
                )
                refreshRecyclerViewMessages()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessageData::class.java) ?: return
                val fromUser: User = getFromUser(chatMessage)
                latestMessagesMap[snapshot.key!!] = LatestMessageData(
                    fromUser,
                    chatMessage.text,
                    chatMessage.timestamp
                )
                refreshRecyclerViewMessages()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                return
            }
        })
    }

    class LatestMessagesAdapter(private val dataSet: MutableList<LatestMessageData>) :
        RecyclerView.Adapter<LatestMessagesAdapter.LatestMessagesViewHolder>() {
        class LatestMessagesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val avatarImageView: CircleImageView =
                itemView.findViewById(R.id.imageViewLatestMessages)
            val nameTextView: TextView = itemView.findViewById(R.id.textViewUsernameLatestMessages)
            val latestMessageTextView: TextView =
                itemView.findViewById(R.id.textViewTextLatestMessages)
            val dateTextView: TextView = itemView.findViewById(R.id.textViewDateLatestMessages)
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): LatestMessagesViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.latest_message_item, parent, false)
            return LatestMessagesViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: LatestMessagesViewHolder, position: Int) {
            holder.nameTextView.text = dataSet[position].user.username
            holder.latestMessageTextView.text = dataSet[position].text
            Picasso.get().load(dataSet[position].user.profileImage).into(holder.avatarImageView)
            holder.dateTextView.text = Date(dataSet[position].date * 1000).toString()

            holder.itemView.setOnClickListener {
                val intent = Intent(holder.itemView.context, ChatLogActivity::class.java)

                val user = dataSet[position].user
                intent.putExtra(NewMessageActivity.USER_KEY, user)
                ContextCompat.startActivity(holder.itemView.context, intent, Bundle.EMPTY)
                (holder.itemView.context as Activity).finish()
            }
        }

        override fun getItemCount() = dataSet.size
    }
}
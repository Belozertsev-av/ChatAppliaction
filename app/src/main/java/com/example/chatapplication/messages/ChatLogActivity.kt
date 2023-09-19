package com.example.chatapplication.messages

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapplication.R
import com.example.chatapplication.databinding.ActivityChatLogBinding
import com.example.chatapplication.model.ChatMessageData
import com.example.chatapplication.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ChatLogActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatLogBinding

    val dataSet: MutableList<MessageData> = fetchMessages()
    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatLogBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser?.username

        binding.recyclerView.adapter = MultipleViewAdapter(this, dataSet)
        listenData()

        binding.sendButton.setOnClickListener{
            performSendMessage()
        }
    }

    private fun fetchMessages(): MutableList<MessageData> {
        val fromId = LatestMessagesActivity.currentUser?.uid
        val toId = toUser?.uid
        val data = mutableListOf<MessageData>()

        val ref = FirebaseDatabase.getInstance("https://chatapplication-666eb-default-rtdb.europe-west1.firebasedatabase.app/").getReference("/messages/$fromId/$toId")

        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach{

                    val message = it.getValue(ChatMessageData::class.java)
                    if (message != null) {
                        if (message.toId == FirebaseAuth.getInstance().uid){
                            data.add(MessageData(MultipleViewAdapter.MESSAGE_LEFT, message.text, toUser!!))
                        } else {
                            data.add(MessageData(MultipleViewAdapter.MESSAGE_RIGHT, message.text, LatestMessagesActivity.currentUser!!))
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("DebugMessage", "Failed")
            }

        })
        return data
    }
    private fun listenData() {
        val fromId = LatestMessagesActivity.currentUser?.uid
        val toId = toUser?.uid

        val ref = FirebaseDatabase.getInstance("https://chatapplication-666eb-default-rtdb.europe-west1.firebasedatabase.app/").getReference("/messages/$fromId/$toId")

        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessageData::class.java)
                if (chatMessage != null) {
                    if (chatMessage.toId == FirebaseAuth.getInstance().uid){
                        dataSet.add(MessageData(MultipleViewAdapter.MESSAGE_LEFT, chatMessage.text, toUser!!))
                    } else {
                        dataSet.add(MessageData(MultipleViewAdapter.MESSAGE_RIGHT, chatMessage.text, LatestMessagesActivity.currentUser!!))
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("errorTag", error.message)
            }
        })
    }

    private fun performSendMessage() {
        val text = binding.editTextText.text.toString()
        val fromId = LatestMessagesActivity.currentUser?.uid
        val toId = toUser?.uid
        if (fromId == null || toId == null) return

        val ref = FirebaseDatabase.getInstance("https://chatapplication-666eb-default-rtdb.europe-west1.firebasedatabase.app/").getReference("/messages/$fromId/$toId").push()
        val toRef = FirebaseDatabase.getInstance("https://chatapplication-666eb-default-rtdb.europe-west1.firebasedatabase.app/").getReference("/messages/$toId/$fromId").push()

        val chatMessage = ChatMessageData(ref.key!!, text, fromId, toId, System.currentTimeMillis() / 1000)
        ref.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d("TAG", "Saved message")
                binding.editTextText.text.clear()
                binding.recyclerView.scrollToPosition(dataSet.size - 1)
            }
        toRef.setValue(chatMessage)

        val latestRef = FirebaseDatabase.getInstance("https://chatapplication-666eb-default-rtdb.europe-west1.firebasedatabase.app/").getReference("/latest-messages/$fromId/$toId")
        latestRef.setValue(chatMessage)
        val latestToRef = FirebaseDatabase.getInstance("https://chatapplication-666eb-default-rtdb.europe-west1.firebasedatabase.app/").getReference("/latest-messages/$toId/$fromId")
        latestToRef.setValue(chatMessage)
    }
}

class MultipleViewAdapter(private val context: Context, private val list: MutableList<MessageData>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val MESSAGE_LEFT = 0
        const val MESSAGE_RIGHT = 1
    }
    private inner class MessageRightViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val imageRight: CircleImageView = itemView.findViewById(R.id.chatLogImageRight)
        val messageRight: TextView = itemView.findViewById(R.id.chatLogMessageRight)
        fun bind(text: String, user: User) {
            messageRight.text = text
            Picasso.get().load(user.profileImage).into(imageRight)
        }
    }

    private inner class MessageLeftViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val imageLeft: CircleImageView = itemView.findViewById(R.id.chatLogImageLeft)
        val messageLeft: TextView = itemView.findViewById(R.id.chatLogMessageLeft)
        fun bind(text: String, user: User) {
            messageLeft.text = text
            Picasso.get().load(user.profileImage).into(imageLeft)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == MESSAGE_LEFT) {
            return MessageLeftViewHolder(
                LayoutInflater.from(context).inflate(R.layout.chat_log_row_left, parent, false)
            )
        }
        return MessageRightViewHolder(
            LayoutInflater.from(context).inflate(R.layout.chat_log_row_right, parent, false)
        )
    }
    override fun getItemViewType(position: Int): Int {
        return list[position].type
    }
    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (list[position].type == MESSAGE_LEFT) {
            (holder as MessageLeftViewHolder).bind(list[position].text, list[position].user)
        } else {
            (holder as MessageRightViewHolder).bind(list[position].text, list[position].user)
        }
    }
}
 data class MessageData (val type: Int, val text: String, val user: User)
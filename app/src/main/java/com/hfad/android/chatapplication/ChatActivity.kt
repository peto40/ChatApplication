package com.hfad.android.chatapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.hfad.android.chatapplication.databinding.ActivityChatBinding

const val CHATS_KEY = "chats"
const val MESSAGE_KEY = "message"

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var mDbRef: DatabaseReference

    var senderRoom: String? = null
    var receiverRoom: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        mDbRef = FirebaseDatabase.getInstance().reference

        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList)

        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.chatRecyclerView.adapter = messageAdapter

        val name = intent.getStringExtra(NAME_KEY)
        val receiverUid = intent.getStringExtra(UID_KEY)
        supportActionBar?.title = name

        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

        // logic to adding data to recyclerView
        mDbRef.child(CHATS_KEY).child(senderRoom!!).child(MESSAGE_KEY)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    messageList.clear()
                    for (postSnapshot in snapshot.children) {
                        val message = postSnapshot.getValue(Message::class.java)
                        messageList.add(message!!)
                    }
                    messageAdapter.notifyDataSetChanged()

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        binding.btnSend.setOnClickListener {
            val message = binding.edtMessageBox.text.toString()
            val messageObject = Message(message, senderUid)

            mDbRef.child(CHATS_KEY).child(senderRoom!!).child(MESSAGE_KEY).push()
                .setValue(messageObject).addOnSuccessListener {
                    mDbRef.child(CHATS_KEY).child(receiverRoom!!).child(MESSAGE_KEY).push()
                        .setValue(messageObject)
                }
            binding.edtMessageBox.setText("")
        }

    }
}
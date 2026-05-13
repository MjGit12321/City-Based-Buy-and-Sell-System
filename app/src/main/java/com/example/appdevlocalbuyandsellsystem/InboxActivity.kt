package com.example.appdevlocalbuyandsellsystem

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class InboxActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var inboxAdapter: InboxAdapter
    private val allConversations = mutableListOf<InboxMessage>()
    private val displayedConversations = mutableListOf<InboxMessage>()
    private lateinit var rvInbox: RecyclerView
    private lateinit var tvNoMessages: TextView
    private lateinit var etSearch: EditText
    
    // Selection state
    private var isSelectionMode = false
    private lateinit var selectionHeader: ConstraintLayout
    private lateinit var tvSelectionCount: TextView
    private val pendingDeleteIds = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.inboxpage)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize Views
        rvInbox = findViewById(R.id.rvInbox)
        tvNoMessages = findViewById(R.id.tvNoMessages)
        selectionHeader = findViewById(R.id.selectionHeader)
        tvSelectionCount = findViewById(R.id.tvSelectionCount)
        etSearch = findViewById(R.id.inboxSearchBar)

        // Adjust for system bars
        val rootLayout = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setup RecyclerView
        rvInbox.layoutManager = LinearLayoutManager(this)
        inboxAdapter = InboxAdapter(
            displayedConversations,
            onItemClick = { conversation ->
                if (isSelectionMode) {
                    toggleSelection(conversation)
                } else {
                    val intent = Intent(this, MessageActivity::class.java)
                    intent.putExtra("CHAT_ID", conversation.originalDocId)
                    intent.putExtra("USER_NAME", conversation.name)
                    startActivity(intent)
                }
            },
            onItemLongClick = { conversation ->
                if (!isSelectionMode) {
                    isSelectionMode = true
                    selectionHeader.visibility = View.VISIBLE
                    toggleSelection(conversation)
                }
            }
        )
        rvInbox.adapter = inboxAdapter

        // Setup Search Listener
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterConversations(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Selection Actions
        findViewById<ImageView>(R.id.ivCancelSelection).setOnClickListener {
            exitSelectionMode()
        }

        findViewById<ImageView>(R.id.ivDelete).setOnClickListener {
            showDeleteConfirmation()
        }

        findViewById<ImageView>(R.id.ivBlock).setOnClickListener {
            showBlockConfirmation()
        }

        loadConversations()
        setupNavigation()
    }

    private fun filterConversations(query: String) {
        displayedConversations.clear()
        if (query.isEmpty()) {
            displayedConversations.addAll(allConversations)
        } else {
            displayedConversations.addAll(allConversations.filter { it.name.contains(query, ignoreCase = true) })
        }
        
        updateEmptyState()
        inboxAdapter.notifyDataSetChanged()
    }

    private fun toggleSelection(conversation: InboxMessage) {
        conversation.isSelected = !conversation.isSelected
        inboxAdapter.notifyDataSetChanged()
        
        val selectedCount = allConversations.count { it.isSelected }
        if (selectedCount == 0) {
            exitSelectionMode()
        } else {
            tvSelectionCount.text = getString(R.string.selection_count, selectedCount)
        }
    }

    private fun exitSelectionMode() {
        isSelectionMode = false
        selectionHeader.visibility = View.GONE
        allConversations.forEach { it.isSelected = false }
        inboxAdapter.notifyDataSetChanged()
    }

    private fun showDeleteConfirmation() {
        val selectedCount = allConversations.count { it.isSelected }
        AlertDialog.Builder(this)
            .setTitle("Delete Conversations")
            .setMessage("Are you sure you want to delete $selectedCount conversation(s)? This will remove them from your list.")
            .setPositiveButton("Delete") { _, _ ->
                deleteSelectedConversations()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteSelectedConversations() {
        val selectedDocs = allConversations.filter { it.isSelected }.map { it.originalDocId }
        val currentUserId = auth.currentUser?.uid ?: return

        // 1. Mark as pending and remove locally for instant UI response
        pendingDeleteIds.addAll(selectedDocs)
        allConversations.removeAll { it.isSelected }
        filterConversations(etSearch.text.toString())

        // 2. Perform the actual removal in background
        selectedDocs.forEach { docId ->
            db.collection("chats").document(docId)
                .update("participants", com.google.firebase.firestore.FieldValue.arrayRemove(currentUserId))
                .addOnSuccessListener {
                    Log.d("Inbox", "Successfully removed from chat $docId")
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error deleting: ${e.message}", Toast.LENGTH_SHORT).show()
                    pendingDeleteIds.remove(docId)
                    loadConversations() // Reload to restore if failed
                }
        }
        
        Toast.makeText(this, "Conversations deleted", Toast.LENGTH_SHORT).show()
        exitSelectionMode()
    }

    private fun updateEmptyState() {
        if (displayedConversations.isEmpty()) {
            tvNoMessages.visibility = View.VISIBLE
            rvInbox.visibility = View.GONE
        } else {
            tvNoMessages.visibility = View.GONE
            rvInbox.visibility = View.VISIBLE
        }
    }

    private fun showBlockConfirmation() {
        val selectedCount = allConversations.count { it.isSelected }
        AlertDialog.Builder(this)
            .setTitle("Block Users")
            .setMessage("Blocking these $selectedCount user(s) will prevent them from sending you further messages and hide these chats.")
            .setPositiveButton("Block") { _, _ ->
                blockSelectedConversations()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun blockSelectedConversations() {
        val selectedDocs = allConversations.filter { it.isSelected }.map { it.originalDocId }
        val currentUserId = auth.currentUser?.uid ?: return

        pendingDeleteIds.addAll(selectedDocs)
        allConversations.removeAll { it.isSelected }
        filterConversations(etSearch.text.toString())

        selectedDocs.forEach { docId ->
            db.collection("chats").document(docId)
                .update("participants", com.google.firebase.firestore.FieldValue.arrayRemove(currentUserId))
        }

        Toast.makeText(this, "Users blocked", Toast.LENGTH_SHORT).show()
        exitSelectionMode()
    }

    private fun loadConversations() {
        val currentUserId = auth.currentUser?.uid ?: return

        updateEmptyState()

        db.collection("chats")
            .whereArrayContains("participants", currentUserId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("FirestoreError", "Error fetching chats: ${e.message}")
                    updateEmptyState()
                    return@addSnapshotListener
                }

                val selectedDocIds = allConversations.filter { it.isSelected }.map { it.originalDocId }.toSet()

                allConversations.clear()
                if (snapshots != null && !snapshots.isEmpty) {
                    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                    val calendar = Calendar.getInstance()
                    val todayYear = calendar.get(Calendar.YEAR)
                    val todayDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

                    for (doc in snapshots) {
                        if (pendingDeleteIds.contains(doc.id)) continue

                        val namesMap = doc.get("names") as? Map<String, String>
                        val otherUserName = namesMap?.filterKeys { it != currentUserId }?.values?.firstOrNull() ?: "User"

                        val lastMsg = doc.getString("lastMessage") ?: ""
                        val unreadCount = doc.getLong("unreadCount")?.toInt() ?: 0
                        
                        val timestamp = doc.getTimestamp("lastTimestamp")?.toDate()
                        val displayTime = if (timestamp != null) {
                            calendar.time = timestamp
                            if (calendar.get(Calendar.YEAR) == todayYear && calendar.get(Calendar.DAY_OF_YEAR) == todayDayOfYear) {
                                timeFormat.format(timestamp)
                            } else {
                                dateFormat.format(timestamp)
                            }
                        } else {
                            doc.getString("lastTime") ?: ""
                        }

                        val message = InboxMessage(doc.id.hashCode(), otherUserName, lastMsg, displayTime, unreadCount)
                        message.originalDocId = doc.id
                        if (selectedDocIds.contains(doc.id)) {
                            message.isSelected = true
                        }
                        allConversations.add(message)
                    }
                    allConversations.sortByDescending { it.time }
                }
                
                // Re-apply search filter after data load
                filterConversations(etSearch.text.toString())
            }
    }

    private fun setupNavigation() {
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            val intent = Intent(this, MainpageActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        findViewById<LinearLayout>(R.id.navFavorites).setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.navMe).setOnClickListener {
            startActivity(Intent(this, MeActivity::class.java))
        }
    }
}

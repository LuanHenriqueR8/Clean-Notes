package com.codingwithmitch.cleannotes.notes.framework.presentation.notelist

import android.annotation.SuppressLint
import android.view.*
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import com.codingwithmitch.cleannotes.core.framework.onSelectChangeColor
import com.codingwithmitch.cleannotes.core.util.printLogD
import com.codingwithmitch.cleannotes.notes.business.domain.model.Note
import com.codingwithmitch.notes.R
import kotlinx.android.synthetic.main.layout_note_list_item.view.*
import kotlinx.coroutines.CoroutineScope
import java.lang.IndexOutOfBoundsException


class NoteListAdapter(
    private val interaction: Interaction? = null,
    private val lifeCycleScope: CoroutineScope,
    private val itemTouchHelper: ItemTouchHelper?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>()
{

    var tracker: SelectionTracker<Long>? = null

    init {
        setHasStableIds(true)
    }

    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Note>() {

        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }

    }
    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return NoteViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_note_list_item,
                parent,
                false
            ),
            interaction,
            lifeCycleScope,
            itemTouchHelper
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is NoteViewHolder -> {
                tracker?.let {
                    holder.bind(
                        differ.currentList.get(position),
                        it.isSelected(position.toLong())
                    )?: throwRequiresTrackerException()
                }
            }
        }
    }

    private fun throwRequiresTrackerException(){
        throw Exception("NoteListAdapter: You must set the SelectionTracker.")
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<Note>) {
        val commitCallback = Runnable {
            // if process died must restore list position
            // very annoying
            interaction?.restoreListPosition()
        }
        differ.submitList(list, commitCallback)
    }

    fun getNote(index: Int): Note? {
        return try{
            differ.currentList[index]
        }catch (e: IndexOutOfBoundsException){
            e.printStackTrace()
            null
        }
    }

    class NoteViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction?,
        private val lifeCycleScope: CoroutineScope,
        private val itemTouchHelper: ItemTouchHelper?
    ) : RecyclerView.ViewHolder(itemView),
        GestureDetector.OnGestureListener,
        View.OnTouchListener
    {

        private var gestureDetector: GestureDetector
                = GestureDetector(itemView.context, this)
        private lateinit var note: Note

        fun bind(item: Note, isActivated: Boolean = false) = with(itemView) {
            itemView.isActivated = isActivated
            itemView.setOnClickListener {
//                itemView.onSelectChangeColor(
//                    lifeCycleScope = lifeCycleScope,
//                    clickColor = com.codingwithmitch.cleannotes.R.color.app_background_color
//                )
                interaction?.onItemSelected(adapterPosition, note)
            }
//            itemView.setOnTouchListener(this@NoteViewHolder)
            note = item
            note_title.text = item.title
            note_timestamp.text = item.updated_at
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = adapterPosition
                override fun getSelectionKey(): Long? = itemId
            }

        override fun onShowPress(e: MotionEvent?) {
        }

        override fun onSingleTapUp(event: MotionEvent?): Boolean {
            return false
        }

        override fun onDown(e: MotionEvent?): Boolean {
            return false
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent?,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            return false
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent?,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            return false
        }

        override fun onLongPress(e: MotionEvent?) {
            itemTouchHelper?.startSwipe(this@NoteViewHolder)
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(view: View?, event: MotionEvent?): Boolean {
            return gestureDetector.onTouchEvent(event)
        }

    }

    interface Interaction {
        fun onItemSelected(position: Int, item: Note)

        fun restoreListPosition()
    }

}














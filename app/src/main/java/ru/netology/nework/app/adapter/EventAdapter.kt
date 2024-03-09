package ru.netology.nework.app.adapter

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Build
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.SCALE_X
import android.view.View.SCALE_Y
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nework.R
import ru.netology.nework.app.dto.Event
import ru.netology.nework.app.enumeration.AttachmentType
import ru.netology.nework.app.utils.formatToDate
import ru.netology.nework.databinding.CardEventBinding

interface OnEventInteractionListener {
    fun onEditEvent(event: Event)
    fun onRemoveEvent(event: Event)
    fun onOpenSpeakers(event: Event)
    fun onOpenMap(event: Event)
    fun onOpenImageAttachment(event: Event)
    fun onOpenProfilePhoto(event: Event)
    fun onLikeEvent(event: Event)
    fun onParticipateEvent(event: Event)
    fun onShareEvent(event: Event)
    fun onOpenLikers(event: Event)
    fun onOpenParticipants(event: Event)

}

class EventAdapter(
    private val onEventInteractionListener: OnEventInteractionListener,
) : PagingDataAdapter<Event, EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = CardEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventViewHolder(binding, onEventInteractionListener)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }
}

class EventViewHolder(
    private val binding: CardEventBinding,
    private val onEventInteractionListener: OnEventInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    @RequiresApi(Build.VERSION_CODES.O)
    fun bind(event: Event) {

        binding.apply {
            authorEvent.text = event.author
            publishedEvent.text = formatToDate(event.published)
            contentEvent.text = event.content
            datetimeEvent.text = formatToDate(event.datetime)
            speakersSumEvent.text = event.speakerIds.count().toString()
            buttonLikeCardEvent.isChecked = event.likedByMe
            likesSumEvent.text = event.likeOwnerIds.count().toString()
            participateEvent.isChecked = event.participatedByMe
            participantsSumEvent.text = event.participantsIds.count().toString()

            imageEvent.visibility =
                if (
                    event.attachment != null && event.attachment.type == AttachmentType.IMAGE
                ) VISIBLE else GONE

            Glide.with(itemView)
                .load("${event.authorAvatar}")
                .placeholder(android.R.drawable.ic_menu_crop)
                .error(R.drawable.baseline_face_retouching_off_24)
                .timeout(10_000)
                .circleCrop()
                .into(avatarEvent)

            event.attachment?.apply {
                Glide.with(imageEvent)
                    .load(this.url)
                    .placeholder(android.R.drawable.ic_menu_crop)
                    .error(R.drawable.ic_photo_24dp)
                    .timeout(10_000)
                    .into(imageEvent)
            }

            menuEvent.isVisible = event.ownedByMe
            menuEvent.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    menu.setGroupVisible(R.id.owned, event.ownedByMe)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onEventInteractionListener.onRemoveEvent(event)
                                true
                            }

                            R.id.edit -> {
                                onEventInteractionListener.onEditEvent(event)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            speakersSumEvent.setOnClickListener {
                onEventInteractionListener.onOpenSpeakers(event)
            }

            imageEvent.setOnClickListener {
                event.attachment?.let {
                    onEventInteractionListener.onOpenImageAttachment(event)
                }
            }

            avatarEvent.setOnClickListener {
                event.authorAvatar?.let {
                    onEventInteractionListener.onOpenProfilePhoto(event)
                }
            }


            locationEvent.setOnClickListener {
                if (event.coordinates != null) {
                    onEventInteractionListener.onOpenMap(event)
                } else {
                    locationEvent.isGone
                }
            }

            buttonLikeCardEvent.setOnClickListener {
                val scaleX = PropertyValuesHolder.ofFloat(SCALE_X, 1F, 1.25F, 1F)
                val scaleY = PropertyValuesHolder.ofFloat(SCALE_Y, 1F, 1.25F, 1F)
                ObjectAnimator.ofPropertyValuesHolder(it, scaleX, scaleY).apply {
                    duration = 500
                    repeatCount = 1
                    interpolator = BounceInterpolator()
                }.start()
                onEventInteractionListener.onLikeEvent(event)
            }

            participateEvent.setOnClickListener {
                onEventInteractionListener.onParticipateEvent(event)
            }

            buttonShareCardEvent.setOnClickListener {
                onEventInteractionListener.onShareEvent(event)
            }

            likesSumEvent.setOnClickListener {
                onEventInteractionListener.onOpenLikers(event)
            }

            participantsSumEvent.setOnClickListener {
                onEventInteractionListener.onOpenParticipants(event)
            }
        }
    }
}

class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
    override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem == newItem
    }
}
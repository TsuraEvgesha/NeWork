package ru.netology.nework.app.adapter

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.SCALE_X
import android.view.View.SCALE_Y
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.media3.exoplayer.ExoPlayer
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nework.R
import ru.netology.nework.app.dto.Post
import ru.netology.nework.app.enumeration.AttachmentType
import ru.netology.nework.app.utils.formatToDate
import ru.netology.nework.databinding.CardPostBinding


interface OnPostInteractionListener {
    fun onOpenProfilePhoto(post: Post) {}
    fun onEditPost(post: Post) {}
    fun onRemovePost(post: Post) {}
    fun onLikePost(post: Post) {}
    fun onMentionPost(post: Post) {}
    fun onSharePost(post: Post) {}
    fun onOpenLikers(post: Post) {}
    fun onOpenMentions(post: Post) {}
    fun onPlayVideo(post: Post) {}
    fun onPlayAudio(post: Post) {}
    fun onOpenImageAttachment(post: Post) {}
}

class PostsAdapter(
    private val onPostInteractionListener: OnPostInteractionListener,
) : PagingDataAdapter<Post, PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding, onPostInteractionListener, context = parent.context)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onPostInteractionListener: OnPostInteractionListener,
    private val context: Context,
) : RecyclerView.ViewHolder(binding.root) {

    @RequiresApi(Build.VERSION_CODES.O)
    fun bind(post: Post) {

        binding.apply {
            author.text = post.author
            published.text = formatToDate(post.published)
            content.text = post.content
            like.isChecked = post.likedByMe
            likesSum.text = post.likeOwnerIds.count().toString()
            menu.isChecked = post.mentionedMe
            mentionsSum.text = post.mentionIds.count().toString()

            attachImage.visibility =
                if (post.attachment != null && post.attachment.type == AttachmentType.IMAGE) VISIBLE else GONE

            groupAudio.visibility =
                if (post.attachment != null && post.attachment.type == AttachmentType.AUDIO) VISIBLE else GONE

            videoGroup.visibility =
                if (post.attachment != null && post.attachment.type == AttachmentType.VIDEO) VISIBLE else GONE

            Glide.with(itemView)
                .load("${post.authorAvatar}")
                .placeholder(android.R.drawable.ic_menu_crop)
                .error(R.drawable.baseline_face_retouching_off_24)
                .timeout(10_000)
                .circleCrop()
                .into(authorAvatar)

            post.attachment?.apply {
                Glide.with(attachImage)
                    .load(this.url)
                    .placeholder(android.R.drawable.ic_menu_crop)
                    .error(R.drawable.ic_photo_24dp)
                    .timeout(10_000)
                    .into(attachImage)
            }

            menu.isVisible = post.ownedByMe
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    menu.setGroupVisible(R.id.owned, post.ownedByMe)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onPostInteractionListener.onRemovePost(post)
                                true
                            }

                            R.id.edit -> {
                                onPostInteractionListener.onEditPost(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            like.setOnClickListener {
                val scaleX = PropertyValuesHolder.ofFloat(SCALE_X, 1F, 1.25F, 1F)
                val scaleY = PropertyValuesHolder.ofFloat(SCALE_Y, 1F, 1.25F, 1F)
                ObjectAnimator.ofPropertyValuesHolder(it, scaleX, scaleY).apply {
                    duration = 500
                    repeatCount = 1
                    interpolator = BounceInterpolator()
                }.start()
                onPostInteractionListener.onLikePost(post)
            }

            mention.visibility =
                if (post.ownedByMe) VISIBLE else INVISIBLE
            mention.setOnClickListener {
                onPostInteractionListener.onMentionPost(post)
            }

            share.setOnClickListener {
                onPostInteractionListener.onSharePost(post)
            }

            likesSum.setOnClickListener {
                onPostInteractionListener.onOpenLikers(post)
            }
            val player = ExoPlayer.Builder(context).build()
            playVideo.player = player
            playVideo.setOnClickListener {
                if (!player.isPlaying) {
                    val mediaItem = post.attachment?.url?.let { uri ->
                        androidx.media3.common.MediaItem.fromUri(
                            uri
                        )
                    }
                    if (mediaItem != null) {
                        player.setMediaItem(mediaItem)
                    }
                    player.prepare()
                    player.playWhenReady
                } else player.release()
            }

            mentionsSum.setOnClickListener {
                onPostInteractionListener.onOpenMentions(post)
            }


            buttonAudio.setOnClickListener {
                buttonAudio.player = player
                if (!player.isPlaying) {
                    val mediaItem = post.attachment?.url?.let { uri ->
                        androidx.media3.common.MediaItem.fromUri(uri)
                    }
                    if (mediaItem != null) {
                        player.setMediaItem(mediaItem)

                    }
                    player.prepare()
                    player.playWhenReady
                } else player.release()
            }

            attachImage.setOnClickListener {
                onPostInteractionListener.onOpenImageAttachment(post)
            }
            authorAvatar.setOnClickListener {
                onPostInteractionListener.onOpenProfilePhoto(post)
            }

        }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: Post, newItem: Post): Any = Unit
}
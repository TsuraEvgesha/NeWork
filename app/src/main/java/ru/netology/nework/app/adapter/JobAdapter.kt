package ru.netology.nework.app.adapter

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.R
import ru.netology.nework.app.dto.Job
import ru.netology.nework.app.utils.AndroidUtils
import ru.netology.nework.databinding.CardJobBinding

interface OnJobInteractionListener {
    fun onRemoveJob(job: Job)
    fun onEditJob(job: Job)
}

class JobAdapter(
    private val onJobInteractionListener: OnJobInteractionListener,
) : ListAdapter<Job, JobViewHolder>(JobDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = CardJobBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return JobViewHolder(parent.context, binding, onJobInteractionListener)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }
}

class JobViewHolder(
    private val context: Context,
    private val binding: CardJobBinding,
    private val onJobInteractionListener: OnJobInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    @RequiresApi(Build.VERSION_CODES.O)
    fun bind(job: Job) {

        binding.apply {
            textNameJob.text = job.name
            textJobTitle.text = job.position
            textFromJob.text = AndroidUtils.convertDate(job.start).substring(0, 10)
            if (job.finish != null && job.finish != "Job now") {
                textToJob.text = AndroidUtils.convertDate(job.finish).substring(0, 10)
            } else {
                textToJob.text = context.getString(R.string.finish)
            }

            if (job.link != null && job.link != "Empty") {
                linkJob.visibility = VISIBLE
                textLinkJob.text = job.link
            } else {
                linkJob.visibility = GONE
                textLinkJob.visibility = GONE
            }

            menuJob.isVisible = job.ownedByMe
            menuJob.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    menu.setGroupVisible(R.id.owned, job.ownedByMe)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onJobInteractionListener.onRemoveJob(job)
                                true
                            }

                            R.id.edit -> {
                                onJobInteractionListener.onEditJob(job)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }
        }
    }
}

class JobDiffCallback : DiffUtil.ItemCallback<Job>() {
    override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem == newItem
    }
}
package ru.netology.nework.app.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.app.adapter.JobAdapter
import ru.netology.nework.app.adapter.OnJobInteractionListener
import ru.netology.nework.app.dto.Job
import ru.netology.nework.app.viewmodel.JobViewModel
import ru.netology.nework.databinding.FragmentJobsBinding


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class JobsFragment : Fragment() {

    private val jobViewModel by activityViewModels<JobViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentJobsBinding.inflate(
            inflater,
            container,
            false
        )

        val adapter = JobAdapter(object : OnJobInteractionListener {
            override fun onEditJob(job: Job) {
                jobViewModel.edit(job)
                val bundle = Bundle().apply {
                    putString("name", job.name)
                    putString("position", job.position)
                    putString("start", job.start)
                    job.finish?.let {
                        putString("finish", it)
                    }
                    job.link?.let {
                        putString("link", it)
                    }
                }
                findNavController()
                    .navigate(R.id.newJobsFragment, bundle)
            }

            override fun onRemoveJob(job: Job) {
                jobViewModel.removeById(job.id)
            }

        })

        val id = parentFragment?.arguments?.getLong("id")

        binding.recyclerViewContainerFragmentJobs.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                if (id != null) {
                    jobViewModel.setId(id)
                    jobViewModel.loadJobs(id)
                }
                jobViewModel.data.collectLatest {
                    adapter.submitList(it)
                    binding.textViewEmptyTextFragmentJobs.isVisible = it.isEmpty()
                }
            }
        }

        jobViewModel.dataState.observe(viewLifecycleOwner) {
            when {
                it.error -> {
                    Toast.makeText(context, R.string.error_loading, Toast.LENGTH_SHORT)
                        .show()
                }
            }
            binding.progressBarFragmentJobs.isVisible = it.loading
        }
        binding.swipeRefreshFragmentJobs.setOnRefreshListener {
            if (id != null) {
                jobViewModel.refreshJobs(id)
                binding.swipeRefreshFragmentJobs.isRefreshing = false
            }
        }

        return binding.root
    }
}
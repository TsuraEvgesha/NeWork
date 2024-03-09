package ru.netology.nework.app.activity

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nework.R
import ru.netology.nework.app.utils.AndroidUtils
import ru.netology.nework.app.utils.AndroidUtils.selectDateDialog
import ru.netology.nework.app.utils.pickDate
import ru.netology.nework.app.viewmodel.JobViewModel
import ru.netology.nework.databinding.FragmentNewJobsBinding

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class NewJobsFragment : Fragment() {
    private val jobViewModel by activityViewModels<JobViewModel>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentNewJobsBinding.inflate(
            inflater,
            container,
            false
        )

        (activity as AppCompatActivity).supportActionBar?.title =
            context?.getString(R.string.job)
        val name = arguments?.getString("name")
        val position = arguments?.getString("position")
        val start = arguments?.getString("start")
        val finish = arguments?.getString("finish")
        val link = arguments?.getString("link")


        binding.apply {
            textNameNewJob.setText(name)
            textPositionNewJob.setText(position)
            textFromNewJob.setText(start)
            textToNewJob.setText(
                if (finish == "") " " else finish
            )
            textLinkNewJob.setText(link)

            buttonSave.setOnClickListener {
                AndroidUtils.hideKeyboard(requireView())
                if (textNameNewJob.text.isNullOrBlank() ||
                    textPositionNewJob.text.isNullOrBlank() ||
                    textFromNewJob.text.isNullOrBlank()
                ) {
                    Toast.makeText(
                        activity,
                        R.string.error_empty_content,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    jobViewModel.changeJobData(
                        name = textNameNewJob.text.toString(),
                        position = textPositionNewJob.text.toString(),
                        start = textFromNewJob.text.toString(),
                        finish = if (textToNewJob.text.isNullOrBlank()) {
                            getString(R.string.finish)
                        } else {
                            textToNewJob.text.toString()
                        },
                        link = if (textLinkNewJob.text.isNullOrBlank()) {
                            "Empty"
                        } else {
                            textLinkNewJob.text.toString()
                        },
                    )
                    jobViewModel.save()
                    AndroidUtils.hideKeyboard(requireView())
                }
            }

            textFromNewJob.setOnClickListener {
                selectDateDialog(binding.textFromNewJob, requireContext())
                val startDate = textFromNewJob.text.toString()
                jobViewModel.startDate(startDate)
            }
            textToNewJob.setOnClickListener {
                selectDateDialog(binding.textToNewJob, requireContext())
                val finishDate = textToNewJob.text.toString()
                jobViewModel.finishDate(finishDate)
            }

            textFromNewJob.setOnClickListener {
                context?.let { item ->
                    pickDate(textFromNewJob, item)
                }
            }
            textToNewJob.setOnClickListener {
                context?.let { item ->
                    pickDate(textToNewJob, item)
                }
            }

            jobViewModel.jobCreated.observe(viewLifecycleOwner) {
                Toast.makeText(context, R.string.menu_save, Toast.LENGTH_SHORT)
                    .show()
                findNavController().navigateUp()
            }

            jobViewModel.dataState.observe(viewLifecycleOwner) {
                when {
                    it.error -> {
                        Toast.makeText(context, R.string.error_loading, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                progressBarFragmentNewJob.isVisible = it.loading
            }
        }

        return binding.root
    }
}
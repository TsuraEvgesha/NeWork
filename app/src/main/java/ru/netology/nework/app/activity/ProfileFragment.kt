package ru.netology.nework.app.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nework.R
import ru.netology.nework.app.adapter.UserProfileAdapter
import ru.netology.nework.app.dto.Event
import ru.netology.nework.app.dto.Job
import ru.netology.nework.app.dto.Post
import ru.netology.nework.app.viewmodel.AuthViewModel
import ru.netology.nework.app.viewmodel.EventViewModel
import ru.netology.nework.app.viewmodel.JobViewModel
import ru.netology.nework.app.viewmodel.PostViewModel
import ru.netology.nework.databinding.FragmentProfileBinding


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private val authViewModel by activityViewModels<AuthViewModel>()
    private val postViewModel by activityViewModels<PostViewModel>()
    private val eventViewModel by activityViewModels<EventViewModel>()
    private val jobViewModel by activityViewModels<JobViewModel>()

    private val profileTitles = arrayOf(
        R.string.posts,
        R.string.job,
    )
    private var visibilityAddGroup = false


    @SuppressLint("PrivateResource")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        val binding = FragmentProfileBinding.inflate(
            inflater,
            container,
            false
        )

        val viewPagerProfile = binding.fragmentProfile
        val tabLayoutProfile = binding.tabProfile
        val id = arguments?.getLong("id")
        val avatar = arguments?.getString("avatar")
        val name = arguments?.getString("name")


        (activity as AppCompatActivity).supportActionBar?.title = name

        viewPagerProfile.adapter = UserProfileAdapter(this)


        TabLayoutMediator(tabLayoutProfile, viewPagerProfile) { tab, position ->
            tab.text = getString(profileTitles[position])
        }.attach()

        with(binding) {
            nameProfile.text = name

            avatarProfile.setOnClickListener {
                val bundle = Bundle().apply {
                    putString("url", avatar)
                }
                authViewModel.data.observe(viewLifecycleOwner) {
                    if (authViewModel.authorized && id == it.id) {
                        findNavController().navigate(R.id.photoProfileFragment, bundle)
                    } else {
                        findNavController().navigate(R.id.imageFragment, bundle)
                    }
                }

            }


            Glide.with(avatarProfile)
                .load("$avatar")
                .transform(CircleCrop())
                .placeholder(R.drawable.baseline_face_retouching_off_24)
                .into(avatarProfile)
        }

        authViewModel.data.observe(viewLifecycleOwner) {
            if (authViewModel.authorized && id == it.id) {
                binding.addContent.visibility = View.VISIBLE
            }
        }
        binding.addContent.setOnClickListener {
            if (!visibilityAddGroup) {
                binding.addContent.setImageResource(R.drawable.baseline_close_24)
                binding.addGroup.visibility = View.VISIBLE
            } else {
                binding.addContent.setImageResource(R.drawable.ic_baseline_add_24)
                binding.addGroup.visibility = View.GONE
            }
            visibilityAddGroup = !visibilityAddGroup
        }


        binding.addPost.setOnClickListener {
            postViewModel.edit(Post.emptyPost)
            findNavController().navigate(R.id.newPostFragment)
        }

        binding.addEvent.setOnClickListener {
            eventViewModel.edit(Event.emptyEvent)
            findNavController().navigate(R.id.newEventFragment)
        }

        binding.addJob.setOnClickListener {
            jobViewModel.edit(Job.emptyJob)
            findNavController().navigate(R.id.newJobsFragment)
        }

        return binding.root
    }
}
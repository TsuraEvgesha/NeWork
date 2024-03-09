package ru.netology.nework.app.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nework.R
import ru.netology.nework.app.adapter.OnUserInteractionListener
import ru.netology.nework.app.adapter.UserAdapter
import ru.netology.nework.app.dto.User
import ru.netology.nework.app.viewmodel.EventViewModel
import ru.netology.nework.app.viewmodel.PostViewModel
import ru.netology.nework.app.viewmodel.UserViewModel
import ru.netology.nework.databinding.FragmentUsersBinding

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class UsersFragment : Fragment() {

    private val userViewModel by viewModels<UserViewModel>()
    private val postViewModel by activityViewModels<PostViewModel>()
    private val eventViewModel by activityViewModels<EventViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentUsersBinding.inflate(
            layoutInflater,
            container,
            false
        )

        val open = arguments?.getString("open")

        val adapter = UserAdapter(object : OnUserInteractionListener {
            override fun openProfile(user: User) {
                when (open) {
                    "mention" -> {
                        postViewModel.changeMentionIds(user.id)
                        postViewModel.save()
                        findNavController().navigateUp()
                    }

                    "speaker" -> {
                        eventViewModel.setSpeaker(user.id)
                        findNavController().navigateUp()
                    }

                    else -> {
                        userViewModel.getUserById(user.id)

                        val bundle = Bundle().apply {
                            putLong("id", user.id)
                            putString("avatar", user.avatar)
                            putString("name", user.name)
                        }
                        findNavController().apply {
                            this.popBackStack(R.id.nav_users, true)
                            this.navigate(R.id.nav_profile, bundle)
                        }
                    }
                }
            }
        })

        binding.listUsers.adapter = adapter

        userViewModel.data.observe(viewLifecycleOwner)
        {
            adapter.submitList(it)
        }
        fun searchUser(query: String) {
            val searchQuery = "%$query%"
            userViewModel.searchUser(searchQuery).observe(viewLifecycleOwner) { list ->
                list.let {
                    adapter.submitList(it)
                }
            }
        }


        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                if (query != null) {
                    searchUser(query)
                }
                return true
            }

        })

        userViewModel.dataState.observe(viewLifecycleOwner)
        {
            when {
                it.error -> {
                    Toast.makeText(context, R.string.error_loading, Toast.LENGTH_SHORT)
                        .show()
                }
            }
            binding.fragmentUsers.isVisible = it.loading
        }

        return binding.root
    }
}

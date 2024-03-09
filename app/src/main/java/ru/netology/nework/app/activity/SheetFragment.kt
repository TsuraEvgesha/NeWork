package ru.netology.nework.app.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ru.netology.nework.R
import ru.netology.nework.app.adapter.OnUserInteractionListener
import ru.netology.nework.app.adapter.UserAdapter
import ru.netology.nework.app.dto.User
import ru.netology.nework.app.viewmodel.UserViewModel
import ru.netology.nework.databinding.FragmentSheetBinding

class SheetFragment : BottomSheetDialogFragment() {

    private val userViewModel by activityViewModels<UserViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.users)

        val binding = FragmentSheetBinding.inflate(
            inflater,
            container,
            false
        )

        val adapter = UserAdapter(object : OnUserInteractionListener {
            override fun openProfile(user: User) {
                userViewModel.getUserById(user.id)
                val bundle = Bundle().apply {
                    putLong("id", user.id)
                    putString("name", user.name)
                    putString("avatar", user.avatar)
                }
                findNavController().apply {
                    this.navigate(R.id.nav_profile, bundle)
                }
            }

        })

        binding.fragmentSheet.adapter = adapter

        userViewModel.data.observe(viewLifecycleOwner) {
            adapter.submitList(it.filter { user ->
                userViewModel.userIds.value!!.contains(user.id)

            })
        }

        return binding.root
    }
}
package ru.netology.nework.app.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nework.R
import ru.netology.nework.app.enumeration.AttachmentType
import ru.netology.nework.app.utils.AndroidUtils
import ru.netology.nework.app.utils.StringArg
import ru.netology.nework.app.viewmodel.PostViewModel
import ru.netology.nework.databinding.FragmentNewPostBinding


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class NewPostFragment : Fragment() {

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    var type: AttachmentType? = null

    private val postViewModel by activityViewModels<PostViewModel>()

    private var fragmentNewPostBinding: FragmentNewPostBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentNewPostBinding.inflate(
            layoutInflater,
            container,
            false
        )

        (activity as AppCompatActivity).supportActionBar?.title =
            context?.getString(R.string.post)

        fragmentNewPostBinding = binding

        arguments?.textArg
            ?.let(binding.editTextFragmentNewPost::setText) ?: postViewModel.edited.value?.content

        binding.editTextFragmentNewPost.requestFocus()

        val photoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                it.data?.data.let { uri ->
                    val stream = uri?.let {
                        context?.contentResolver?.openInputStream(it)
                    }
                    postViewModel.changeMedia(uri, stream, type)
                }
            }

        val mediaLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                uri?.let {
                    val stream = context?.contentResolver?.openInputStream(it)
                    postViewModel.changeMedia(it, stream, type)
                }
            }

        binding.photoNewPost.setOnClickListener {
            ImagePicker.Builder(this)
                .cameraOnly()
                .maxResultSize(2048, 2048)
                .createIntent(photoLauncher::launch)
            type = AttachmentType.IMAGE
        }

        binding.imageNewPost.setOnClickListener {
            mediaLauncher.launch("image/*")
            type = AttachmentType.IMAGE
        }

        binding.audioNewPost.setOnClickListener {
            mediaLauncher.launch("audio/*")
            type = AttachmentType.AUDIO
        }

        binding.videoNewPost.setOnClickListener {
            mediaLauncher.launch("video/*")
            type = AttachmentType.VIDEO
        }

        binding.buttonRemovePhotoFragmentNewPost.setOnClickListener {
            postViewModel.changeMedia(null, null, null)
        }
        fun ImageView.loadImage(url: String) {
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_baseline_public_24)
                .error(android.R.drawable.ic_dialog_alert)
                .timeout(10_000)
                .into(this)
        }

        postViewModel.media.observe(viewLifecycleOwner) {
            if (it?.uri == null) {
                binding.frameLayoutMediaFragmentNewPost.visibility = View.GONE
                return@observe

            }
            binding.frameLayoutMediaFragmentNewPost.visibility = View.VISIBLE
            binding.imageViewPhotoFragmentNewPost.loadImage(it.uri.toString())


        }

        postViewModel.postCreated.observe(viewLifecycleOwner) {
            findNavController().navigate(R.id.nav_posts)
        }

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_save, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.save -> {
                        fragmentNewPostBinding?.let {
                            if (it.editTextFragmentNewPost.text.isNullOrBlank()) {
                                Toast.makeText(
                                    activity,
                                    R.string.error_empty_content,
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            } else {
                                postViewModel.changeContent(
                                    binding.editTextFragmentNewPost.text.toString()
                                )
                                postViewModel.save()
                                AndroidUtils.hideKeyboard(requireView())
                            }
                        }
                        true
                    }

                    else -> false
                }
        }, viewLifecycleOwner)

        postViewModel.dataState.observe(viewLifecycleOwner) {
            when {
                it.error -> {
                    Toast.makeText(context, R.string.error_loading, Toast.LENGTH_SHORT)
                        .show()
                }
            }
            binding.fragmentNewPost.isVisible = it.loading
        }

        return binding.root
    }

    override fun onDestroyView() {
        fragmentNewPostBinding = null
        super.onDestroyView()
    }
}
package ru.netology.nework.app.activity


import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.app.auth.AppAuth
import ru.netology.nework.app.utils.AndroidUtils.hideKeyboard
import ru.netology.nework.app.viewmodel.SignUpViewModel
import ru.netology.nework.databinding.FragmentSignUpBinding
import javax.inject.Inject


@AndroidEntryPoint
class SignUpFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    private val viewModel by viewModels<SignUpViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentSignUpBinding.inflate(
            inflater,
            container,
            false
        )

        viewModel.data.observe(viewLifecycleOwner) {
            appAuth.setAuth(it.id, it.token)
            findNavController().navigate(R.id.action_signUpFragment_to_appActivity)
        }

        with(binding) {
            registerUserBtn.setOnClickListener {
                let {
                    if (
                        it.loginEditText.text.isNullOrBlank() ||
                        it.nameEditText.text.isNullOrBlank() ||
                        it.passwordEditText.text.isNullOrBlank() ||
                        it.passwordCheckEditText.text.isNullOrBlank()
                    ) {
                        Toast.makeText(
                            activity,
                            R.string.error_empty_content,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        if (
                            passwordEditText.text.toString() ==
                            passwordCheckEditText.text.toString()
                        ) {
                            viewModel.registration(
                                loginEditText.text.toString(),
                                passwordEditText.text.toString(),
                                nameEditText.text.toString()
                            )
                            hideKeyboard(requireView())
                        } else
                            passwordCheckEditText.error =
                                getString(R.string.errorPassword)
                    }
                }
            }
        }


        val pickPhotoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            binding.root,
                            ImagePicker.getError(it.data),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                    Activity.RESULT_OK -> {
                        val uri: Uri? = it.data?.data
                        viewModel.changePhoto(uri)
                    }
                }
            }
        binding.photoProfile.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .provider(ImageProvider.BOTH)
                .galleryMimeTypes(
                    arrayOf(
                        "image/png",
                        "image/jpeg",
                        "image/jpg"
                    )
                )
                .createIntent(pickPhotoLauncher::launch)
        }



        viewModel.dataState.observe(viewLifecycleOwner) {
            when {
                it.error -> {
                    Toast.makeText(context, R.string.error_loading, Toast.LENGTH_SHORT)
                        .show()
                }
            }
            binding.progressBarFragmentSignUp.isVisible = it.loading
        }
        viewModel.photo.observe(viewLifecycleOwner) {
            if (it.uri == null) {
                return@observe
            }
            binding.photoProfile.setImageURI(it.uri)

        }

        binding.buttonSignIn.setOnClickListener {
            findNavController().navigate(R.id.signInFragment)
        }



        return binding.root
    }

}
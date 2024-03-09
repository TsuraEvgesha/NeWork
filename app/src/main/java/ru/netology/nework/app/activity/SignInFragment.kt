package ru.netology.nework.app.activity


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nework.R
import ru.netology.nework.app.auth.AppAuth
import ru.netology.nework.app.utils.AndroidUtils.hideKeyboard
import ru.netology.nework.app.viewmodel.SignInViewModel
import ru.netology.nework.databinding.FragmentSignInBinding
import javax.inject.Inject


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SignInFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    private val viewModel by viewModels<SignInViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentSignInBinding.inflate(
            inflater,
            container,
            false
        )

        with(binding) {
            loginEditText.requestFocus()
            loginBtn.setOnClickListener {
                viewModel.authorization(
                    loginEditText.text.toString(),
                    passwordEditText.text.toString()
                )
                hideKeyboard(requireView())
            }
        }

        viewModel.data.observe(viewLifecycleOwner) {
            appAuth.setAuth(it.id, it.token)
            findNavController().navigate(R.id.nav_posts)
        }

        binding.buttonSignUp.setOnClickListener {
            findNavController().navigate(R.id.signUpFragment)
        }
        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            when {
                state.loginError -> {
                    binding.passwordEditText.error = getString(R.string.errorLogPass)
                }

                state.error -> {
                    Toast.makeText(context, R.string.error_loading, Toast.LENGTH_SHORT)
                        .show()
                }
            }
            binding.progressBarFragmentSignIn.isVisible = state.loading
        }

        return binding.root
    }


}
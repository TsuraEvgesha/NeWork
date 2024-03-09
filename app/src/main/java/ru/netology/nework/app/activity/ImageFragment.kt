package ru.netology.nework.app.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentImageBinding


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ImageFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        val binding = FragmentImageBinding.inflate(inflater, container, false)

        (activity as AppCompatActivity).supportActionBar?.hide()

        binding.apply {
            image.visibility = View.GONE
            val url = arguments?.getString("url")

            Glide.with(image)
                .load(url)
                .placeholder(R.drawable.ic_baseline_public_24)
                .error(android.R.drawable.ic_dialog_alert)
                .timeout(10_000)
                .into(image)

            image.visibility = View.VISIBLE
        }

        binding.image.setOnClickListener {
            findNavController().popBackStack()
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as AppCompatActivity).supportActionBar?.show()
    }
}
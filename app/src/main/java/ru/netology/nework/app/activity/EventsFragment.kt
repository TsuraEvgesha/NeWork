package ru.netology.nework.app.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import ru.netology.nework.R
import ru.netology.nework.app.adapter.EventAdapter
import ru.netology.nework.app.adapter.OnEventInteractionListener
import ru.netology.nework.app.dto.Event
import ru.netology.nework.app.viewmodel.AuthViewModel
import ru.netology.nework.app.viewmodel.EventViewModel
import ru.netology.nework.app.viewmodel.UserViewModel
import ru.netology.nework.databinding.FragmentEventsBinding


@Suppress("DEPRECATION")
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class EventsFragment : Fragment() {

    private val eventViewModel by activityViewModels<EventViewModel>()
    private val authViewModel by activityViewModels<AuthViewModel>()
    private val userViewModel by activityViewModels<UserViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentEventsBinding.inflate(
            inflater,
            container,
            false
        )

        val adapter = EventAdapter(object : OnEventInteractionListener {


            override fun onEditEvent(event: Event) {
                eventViewModel.edit(event)
                val bundle = Bundle().apply {
                    putString("content", event.content)
                    putString("dateTime", event.datetime)
                    event.coordinates?.lat?.let {
                        putDouble("lat", it)
                    }
                    event.coordinates?.long?.let {
                        putDouble("long", it)
                    }

                }
                findNavController()
                    .navigate(R.id.newEventFragment, bundle)
            }

            override fun onRemoveEvent(event: Event) {
                eventViewModel.removeById(event.id)
            }

            override fun onOpenSpeakers(event: Event) {
                userViewModel.getUsersIds(event.speakerIds)
                if (event.speakerIds.isEmpty()) {
                    Toast.makeText(context, R.string.no_speakers, Toast.LENGTH_SHORT)
                        .show()
                } else {
                    findNavController().navigate(R.id.action_nav_events_to_sheet_fragment)
                }
            }

            override fun onOpenMap(event: Event) {
                val bundle = Bundle().apply {
                    event.coordinates?.lat?.let {
                        putDouble("lat", it)
                    }
                    event.coordinates?.long?.let {
                        putDouble("long", it)
                    }
                }
                findNavController().navigate(R.id.mapsFragment, bundle)
            }

            override fun onOpenImageAttachment(event: Event) {
                val bundle = Bundle().apply {
                    putString("url", event.attachment?.url)
                }
                findNavController().navigate(R.id.imageFragment, bundle)
            }

            override fun onOpenProfilePhoto(event: Event) {
                val bundle = Bundle().apply {
                    putString("url", event.authorAvatar)
                }
                if (event.authorAvatar !== null) {
                    findNavController().navigate(R.id.imageFragment, bundle)
                } else {
                    Toast.makeText(activity, R.string.no_photo, Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onLikeEvent(event: Event) {
                if (authViewModel.authorized) {
                    if (!event.likedByMe)
                        eventViewModel.likeById(event.id)
                    else eventViewModel.unlikeById(event.id)
                } else {
                    Toast.makeText(activity, R.string.Authorization, Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onParticipateEvent(event: Event) {
                if (authViewModel.authorized) {
                    if (!event.participatedByMe)
                        eventViewModel.participate(event.id)
                    else eventViewModel.doNotParticipate(event.id)
                } else {
                    Toast.makeText(activity, R.string.Authorization, Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onShareEvent(event: Event) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, event.content)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(intent, "Share Event")
                startActivity(shareIntent)
            }

            override fun onOpenLikers(event: Event) {
                userViewModel.getUsersIds(event.likeOwnerIds)
                if (event.likeOwnerIds.isEmpty()) {
                    Toast.makeText(context, getString(R.string.nobody_liked_it), Toast.LENGTH_SHORT)
                        .show()
                } else {
                    findNavController().navigate(R.id.action_nav_events_to_sheet_fragment)
                }
            }

            override fun onOpenParticipants(event: Event) {
                userViewModel.getUsersIds(event.participantsIds)
                if (event.participantsIds.isEmpty()) {
                    Toast.makeText(context, R.string.no_participants, Toast.LENGTH_SHORT)
                        .show()
                } else {
                    findNavController().navigate(R.id.action_nav_events_to_sheet_fragment)
                }
            }
        })

        binding.recyclerViewContainerFragmentEvents.adapter = adapter

        lifecycleScope.launchWhenCreated {
            eventViewModel.data.collectLatest(adapter::submitData)
        }

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest { state ->
                binding.swipeRefreshFragmentEvents.isRefreshing =
                    state.refresh is LoadState.Loading
                binding.textViewEmptyTextFragmentEvents.isVisible =
                    adapter.itemCount < 1
            }
        }

        eventViewModel.dataState.observe(viewLifecycleOwner) {
            when {
                it.error -> {
                    Toast.makeText(context, R.string.error_loading, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        binding.swipeRefreshFragmentEvents.setOnRefreshListener(adapter::refresh)

        return binding.root
    }
}
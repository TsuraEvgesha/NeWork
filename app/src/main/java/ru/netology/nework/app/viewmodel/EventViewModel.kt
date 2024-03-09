package ru.netology.nework.app.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nework.app.auth.AppAuth
import ru.netology.nework.app.dto.Coordinates
import ru.netology.nework.app.dto.Event
import ru.netology.nework.app.dto.MediaUpload
import ru.netology.nework.app.enumeration.AttachmentType
import ru.netology.nework.app.enumeration.EventType
import ru.netology.nework.app.model.MediaModel
import ru.netology.nework.app.model.StateModel
import ru.netology.nework.app.repository.EventRepository
import ru.netology.nework.app.utils.SingleLiveEvent
import java.io.InputStream
import javax.inject.Inject

private val empty = Event(
    id = 0,
    authorId = 0,
    author = "",
    authorAvatar = "",
    content = "",
    published = "2023-01-27T17:00:00.000Z",
    datetime = "2023-01-27T17:00:00.000Z",
    type = EventType.ONLINE,
    speakerIds = emptySet()
)

private val noMedia = MediaModel()

@ExperimentalCoroutinesApi
@HiltViewModel
class EventViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    appAuth: AppAuth,
) : ViewModel() {

    private val cached = eventRepository
        .data
        .cachedIn(viewModelScope)

    val data: Flow<PagingData<Event>> =
        appAuth.authStateFlow
            .flatMapLatest { (myId, _) ->
                cached.map { pagingData ->
                    pagingData.map { event ->
                        event.copy(
                            ownedByMe = event.authorId == myId,
                            likedByMe = event.likeOwnerIds.contains(myId),
                            participatedByMe = event.participantsIds.contains(myId)
                        )
                    }
                }
            }


    val edited = MutableLiveData(empty)

    private val _dataState = MutableLiveData<StateModel>()
    val dataState: LiveData<StateModel>
        get() = _dataState

    private val _eventCreated = SingleLiveEvent<Unit>()
    val eventCreated: LiveData<Unit>
        get() = _eventCreated

    private val _media = MutableLiveData(noMedia)
    val media: LiveData<MediaModel>
        get() = _media

    private val scope = MainScope()

    fun save() {
        edited.value?.let { event ->
            viewModelScope.launch {
                _dataState.postValue(StateModel(loading = true))
                try {
                    when (_media.value) {
                        noMedia ->
                            eventRepository.saveEvent(event)

                        else ->
                            _media.value?.inputStream?.let {
                                MediaUpload(it)
                            }?.let {
                                eventRepository.saveWithAttachment(event, it)
                            }
                    }
                    _dataState.value = StateModel()
                    _eventCreated.value = Unit
                } catch (e: Exception) {
                    throw UnknownError()
                }
            }
        }
        edited.value = empty
        _media.value = noMedia
    }

    fun changeContent(content: String, date: String, coordinates: Coordinates?) {
        edited.value?.let {
            val text = content.trim()

            if (edited.value?.content != text) {
                edited.value = edited.value?.copy(content = text)
            }
            if (edited.value?.datetime != date) {
                edited.value = edited.value?.copy(datetime = date)
            }
            if (edited.value?.coordinates != coordinates) {
                edited.value = edited.value?.copy(coordinates = coordinates)
            }
        }
    }

    fun setSpeaker(id: Long) {
        if (edited.value?.speakerIds?.contains(id) == false) {
            edited.value = edited.value?.speakerIds?.plus(id)?.let {
                edited.value?.copy(speakerIds = it)
            }
        }
    }

    fun changeMedia(
        uri: Uri?,
        inputStream: InputStream?,
        type: AttachmentType?,
    ) {
        _media.value = MediaModel(uri, inputStream, type)
    }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            eventRepository.removeById(id)
        } catch (e: Exception) {
            _dataState.value = StateModel(error = true)
        }
    }

    fun edit(event: Event) {
        edited.value = event
    }

    fun likeById(id: Long) = viewModelScope.launch {
        try {
            eventRepository.likeById(id)
        } catch (e: Exception) {
            _dataState.value = StateModel(error = true)
        }
    }

    fun unlikeById(id: Long) = viewModelScope.launch {
        try {
            eventRepository.unlikeById(id)
        } catch (e: Exception) {
            _dataState.value = StateModel(error = true)
        }
    }

    fun participate(id: Long) = viewModelScope.launch {
        try {
            eventRepository.participate(id)
        } catch (e: Exception) {
            _dataState.value = StateModel(error = true)
        }
    }

    fun doNotParticipate(id: Long) = viewModelScope.launch {
        try {
            eventRepository.doNotParticipate(id)
        } catch (e: Exception) {
            _dataState.value = StateModel(error = true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }
}
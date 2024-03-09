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
import ru.netology.nework.app.dto.MediaUpload
import ru.netology.nework.app.dto.Post
import ru.netology.nework.app.enumeration.AttachmentType
import ru.netology.nework.app.model.MediaModel
import ru.netology.nework.app.repository.PostRepository
import ru.netology.nework.app.model.StateModel
import ru.netology.nework.app.utils.SingleLiveEvent
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject


private val empty = Post(
    id = 0,
    authorId = 0,
    author = "",
    authorAvatar = "",
    content = "",
    published = "1992-01-27T17:00:00.000Z",
    mentionedMe = false,
    likedByMe = false,

)

private val noMedia = MediaModel()

@ExperimentalCoroutinesApi
@HiltViewModel
class PostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    appAuth: AppAuth,
) : ViewModel() {

    private val cached = postRepository
        .data
        .cachedIn(viewModelScope)

    val data: Flow<PagingData<Post>> =
        appAuth.authStateFlow
            .flatMapLatest { (myId, _) ->
                cached.map { pagingData ->
                    pagingData.map { post ->
                        post.copy(
                            mentionedMe = post.mentionIds.contains(myId),
                            ownedByMe = post.authorId == myId,
                            likedByMe = post.likeOwnerIds.contains(myId)
                        )
                    }
                }
            }

    val edited = MutableLiveData(empty)

    private val _dataState = MutableLiveData<StateModel>()
    val dataState: LiveData<StateModel>
        get() = _dataState

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _media = MutableLiveData(noMedia)
    val media: LiveData<MediaModel>
        get() = _media

    private val scope = MainScope()

    fun save() {
        edited.value?.let { post ->
            viewModelScope.launch {
                _dataState.postValue(StateModel(loading = true))
                try {
                    when (_media.value) {
                        noMedia -> {
                            postRepository.save(post)
                        }

                        else -> {
                            _media.value?.inputStream?.let {
                                MediaUpload(it)
                            }?.let {
                                postRepository.saveWithAttachment(post, it, _media.value?.type!!)
                            }
                        }
                    }
                    _dataState.postValue(StateModel())
                    _postCreated.value = Unit
                } catch (e: IOException) {
                    _dataState.postValue(StateModel(error = true))
                } catch (e: Exception) {
                    throw UnknownError()
                }
            }
        }
        edited.value = empty
        _media.value = noMedia
    }

    fun changeContent(content: String) {
        edited.value?.let {
            val text = content.trim()
            if (edited.value?.content != text) {
                edited.value = edited.value?.copy(content = text)
            }
        }
    }

    fun changeMedia(
        uri: Uri?,
        inputStream: InputStream?,
        type: AttachmentType?,
    ) {
        _media.value = MediaModel(
            uri = uri,
            inputStream = inputStream,
            type = type,)
    }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            postRepository.removeById(id)
        } catch (e: Exception) {
            _dataState.postValue(StateModel(error = true))
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun likeById(id: Long) = viewModelScope.launch {
        try {
            postRepository.likeById(id)
        } catch (e: Exception) {
            _dataState.value = StateModel(error = true)
        }
    }

    fun unlikeById(id: Long) = viewModelScope.launch {
        try {
            postRepository.dislikeById(id)
        } catch (e: Exception) {
            _dataState.value = StateModel(error = true)
        }
    }

    fun changeMentionIds(id: Long) {
        edited.value?.let {
            if (edited.value?.mentionIds?.contains(id) == false) {
                edited.value = edited.value?.copy(
                    mentionIds = it.mentionIds.plus(id)
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }
}
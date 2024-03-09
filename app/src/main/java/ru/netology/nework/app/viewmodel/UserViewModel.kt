package ru.netology.nework.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.nework.app.api.UserApiService
import ru.netology.nework.app.dto.User
import ru.netology.nework.app.model.StateModel
import ru.netology.nework.app.repository.UserRepository
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userApiService: UserApiService,
) : ViewModel() {

    val data: LiveData<List<User>> =
        userRepository.data
            .asLiveData(Dispatchers.Default)

    private val _dataState = MutableLiveData<StateModel>()
    val dataState: LiveData<StateModel>
        get() = _dataState

    private val _user = MutableLiveData<User>()
    val user: LiveData<User>
        get() = _user

    private val _userIds = MutableLiveData<Set<Long>>()
    val userIds: LiveData<Set<Long>>
        get() = _userIds


    init {
        getUsers()
    }

    private fun getUsers() = viewModelScope.launch {
        _dataState.postValue(StateModel(loading = true))
        try {
            userRepository.getAll()
            _dataState.postValue(StateModel())
        } catch (e: Exception) {
            _dataState.value = StateModel(error = true)
        }
    }

    fun getUserById(id: Long) = viewModelScope.launch {
        _dataState.postValue(StateModel(loading = true))
        try {
            val response = userApiService.getUserById(id)
            if (response.isSuccessful) {
                _user.value = response.body()
            }
            _dataState.postValue(StateModel())
        } catch (e: Exception) {
            _dataState.postValue(StateModel(error = true))
        }
    }

    fun getUsersIds(set: Set<Long>) =
        viewModelScope.launch {
            _userIds.value = set
        }

    fun searchUser(searchQuery: String): LiveData<List<User>> {
        return userRepository.searchUser(searchQuery).asLiveData()
    }
}
package ru.netology.nework.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nework.app.api.UserApiService
import ru.netology.nework.app.dto.Token
import ru.netology.nework.app.errors.ApiError
import ru.netology.nework.app.model.StateModel



import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val userApiService: UserApiService,
)  : ViewModel() {
    val data = MutableLiveData<Token>()
    private val _dataState = MutableLiveData<StateModel>()

    val dataState: LiveData<StateModel>
        get() = _dataState

    fun authorization(login: String, password: String) {
        viewModelScope.launch {
            _dataState.postValue(StateModel(loading = true))
            try {
                val response = userApiService.updateUser(login, password)
                if (!response.isSuccessful) {
                    throw ApiError(response.message())
                }
                _dataState.postValue(StateModel())
                val body = response.body() ?: throw ApiError(response.message())
                data.value = Token(body.id, body.token)
            } catch (e: IOException) {
                _dataState.postValue(StateModel(error = true))
            } catch (e: Exception) {
                _dataState.postValue(StateModel(loginError = true))
            }
        }
    }
}
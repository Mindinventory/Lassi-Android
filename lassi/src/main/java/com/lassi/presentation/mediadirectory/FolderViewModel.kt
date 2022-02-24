package com.lassi.presentation.mediadirectory

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lassi.data.common.Response
import com.lassi.data.common.Result
import com.lassi.data.mediadirectory.Folder
import com.lassi.domain.media.MediaRepository
import com.lassi.presentation.common.LassiBaseViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class FolderViewModel(
    private val mediaRepository: MediaRepository
) : LassiBaseViewModel() {
    var fetchMediaFolderLiveData = MutableLiveData<Response<ArrayList<Folder>>>()

    fun fetchFolders() {
        viewModelScope.launch {
            mediaRepository.fetchFolders()
                .onStart {
                    fetchMediaFolderLiveData.postValue(Response.Loading())
                }.collect { result ->
                    when (result) {
                        is Result.Success -> {
                            fetchMediaFolderLiveData.postValue(Response.Success(result.data))
                        }
                        is Result.Error -> {
                            fetchMediaFolderLiveData.postValue(Response.Error(result.throwable))
                        }
                        else -> {
                            /**
                             * no need to implement
                             */
                        }
                    }
                }
        }
    }
}
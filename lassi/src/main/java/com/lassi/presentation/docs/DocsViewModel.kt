package com.lassi.presentation.docs

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lassi.data.common.Response
import com.lassi.data.common.Result
import com.lassi.data.media.MiMedia
import com.lassi.domain.media.MediaRepository
import com.lassi.presentation.common.LassiBaseViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.util.*

class DocsViewModel(private val mediaRepository: MediaRepository) : LassiBaseViewModel() {
    var fetchDocsLiveData = MutableLiveData<Response<ArrayList<MiMedia>>>()

    fun fetchDocs() {
        viewModelScope.launch {
            mediaRepository.fetchDocs()
                .onStart {
                    fetchDocsLiveData.postValue(Response.Loading())
                }.collect { result ->
                    when (result) {
                        is Result.Success -> {
                            fetchDocsLiveData.postValue(Response.Success(result.data))
                        }
                        is Result.Error -> {
                            fetchDocsLiveData.postValue(Response.Error(result.throwable))
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
package com.lassi.presentation.media

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lassi.data.common.Response
import com.lassi.data.common.Result
import com.lassi.data.media.MiMedia
import com.lassi.domain.media.MediaType
import com.lassi.domain.media.SelectedMediaRepository
import com.lassi.presentation.common.LassiBaseViewModel
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class SelectedMediaViewModel(
    private val selectedMediaRepository: SelectedMediaRepository
) : LassiBaseViewModel() {
    val selectedMediaLiveData = MutableLiveData<ArrayList<MiMedia>>()
    private var selectedMedias = arrayListOf<MiMedia>()

    var fetchedMediaLiveData = MutableLiveData<Response<ArrayList<MiMedia>>>()

    fun addAllSelectedMedia(selectedMedias: ArrayList<MiMedia>) {
        this.selectedMedias = selectedMedias
        this.selectedMedias = this.selectedMedias.distinctBy {
            it.path
        } as ArrayList<MiMedia>
        selectedMediaLiveData.value = this.selectedMedias
    }

    fun addSelectedMedia(selectedMedia: MiMedia) {
        this.selectedMedias.add(selectedMedia)
        this.selectedMedias = this.selectedMedias.distinctBy { it.path } as ArrayList<MiMedia>
        selectedMediaLiveData.value = this.selectedMedias
    }

    fun getSelectedMediaData(bucket: String) {
        viewModelScope.launch {
            selectedMediaRepository.getSelectedMediaData(bucket).onStart {
                fetchedMediaLiveData.value = Response.Loading()
            }.collect { result ->
                when (result) {
                    is Result.Success -> result.data.let {
                        fetchedMediaLiveData.postValue(Response.Success(it))
                    }
                    is Result.Error -> fetchedMediaLiveData.value = Response.Error(result.throwable)
                    else -> {}
                }
            }
        }
    }

    fun getSortedDataFromDb(bucket: String, isAsc: Int, mediaType: MediaType) {
        viewModelScope.launch {
            selectedMediaRepository.getSortedDataFromDb(bucket, isAsc, mediaType).onStart {
                fetchedMediaLiveData.value = Response.Loading()
            }.collect { result ->
                when (result) {
                    is Result.Success -> result.data.let {
                        fetchedMediaLiveData.postValue(Response.Success(it))
                    }
                    is Result.Error -> {
                        fetchedMediaLiveData.value = Response.Error(result.throwable)
                    }
                    else -> {}
                }
            }
        }
    }
}
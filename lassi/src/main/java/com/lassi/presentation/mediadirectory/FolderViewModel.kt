package com.lassi.presentation.mediadirectory

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lassi.common.extenstions.setError
import com.lassi.common.extenstions.setLoading
import com.lassi.common.utils.Logger
import com.lassi.data.common.Response
import com.lassi.data.common.Result
import com.lassi.data.media.MiItemMedia
import com.lassi.data.media.entity.MediaFileEntity
import com.lassi.domain.common.SingleLiveEvent
import com.lassi.domain.media.MediaRepository
import com.lassi.presentation.common.LassiBaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FolderViewModel(
    private val mediaRepository: MediaRepository
) : LassiBaseViewModel() {
    private var _fetchMediaFolderLiveData = SingleLiveEvent<Response<ArrayList<MiItemMedia>>>()
    var fetchMediaFolderLiveData: LiveData<Response<ArrayList<MiItemMedia>>> =
        _fetchMediaFolderLiveData
    val list: MutableLiveData<ArrayList<MiItemMedia>> = MutableLiveData()
    var emptyList: MutableLiveData<Boolean> = MutableLiveData(false)

    fun checkInsert() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                this@FolderViewModel._fetchMediaFolderLiveData.setLoading()
                if (mediaRepository.isDbEmpty()) {
                    insertDataInDatabase()
                } else {
                    checkAndRemoveUnavailableFileFromDatabase()
                    checkAndInsertNewDataIntoDatabase()
                }
            }
        }
    }

    fun checkRemoval() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                checkAndRemoveUnavailableFileFromDatabase()
            }
        }
    }

    fun getMediaItemList(): LiveData<ArrayList<MiItemMedia>> {
        return list
    }

    private suspend fun checkAndRemoveUnavailableFileFromDatabase() {
        Log.d("TAG", "!@# checkAndRemoveUnavailableFileFromDatabase() called ")
        getAllPhotoVidDataFromDatabase()//to get all images and videos and store it into "allData"
    }

    private suspend fun checkAndInsertNewDataIntoDatabase() {
        when (val result = mediaRepository.insertMediaData()) {
            is Result.Loading -> {
                this._fetchMediaFolderLiveData.setLoading()
            }
            is Result.Success -> {
                getDataFromDatabase()
            }
            is Result.Error -> {
                this._fetchMediaFolderLiveData.setError(result.throwable)
            }
        }
    }

    private suspend fun insertDataInDatabase() {
        when (val result = mediaRepository.insertAllMediaData()) {
            is Result.Loading -> {
                this._fetchMediaFolderLiveData.setLoading()
            }
            is Result.Success -> {
                Logger.d("FolderViewModel", "Insert completed")
                getDataFromDatabase()
            }
            is Result.Error -> {
                this._fetchMediaFolderLiveData.setError(result.throwable)
            }
        }
    }

    //this is only getting latest video/image to display as a thumbnail
    private suspend fun getDataFromDatabase() {
        mediaRepository.getDataFromDb()
            .onStart {
                _fetchMediaFolderLiveData.setLoading()
            }
            .map { result ->
                when (result) {
                    is Result.Success -> result.data.filter {
                        !it.bucketName.isNullOrEmpty()
                    }
                    is Result.Error -> TODO()
                    Result.Loading -> TODO()
                }
            }
            .collectLatest { mediaItemList ->
                withContext(Dispatchers.Main) {
                    list.value = mediaItemList as ArrayList<MiItemMedia>

                    if (mediaItemList.isNullOrEmpty()) {
                        emptyList.value = true
                    }
                }
            }
    }

    private suspend fun getAllPhotoVidDataFromDatabase() {
        mediaRepository.getAllImgVidMediaFile()
            .onStart {
                _fetchMediaFolderLiveData.setLoading()
            }
            .map { result ->
                when (result) {
                    is Result.Success ->
                        result.data.filter {
                            !it.mediaBucket.isNullOrEmpty()
                        }
                    is Result.Error -> TODO()
                    Result.Loading -> TODO()
                }
            }
            .collectLatest { mediaItemList ->
                withContext(Dispatchers.Main) {
                    mediaRepository.removeMediaData(mediaItemList)
                    if (mediaItemList.isNullOrEmpty()) {
                        emptyList.value = true
                    }
                }
            }
    }
}
package com.lassi.presentation.mediadirectory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lassi.common.extenstions.setError
import com.lassi.common.extenstions.setLoading
import com.lassi.common.utils.Logger
import com.lassi.data.common.Response
import com.lassi.data.common.Result
import com.lassi.data.media.MiItemMedia
import com.lassi.domain.common.SingleLiveEvent
import com.lassi.domain.media.MediaRepository
import com.lassi.presentation.common.LassiBaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.takeWhile
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
    private var _fileRemovalCheck = MutableLiveData(false)
    var fileRemovalCheck: LiveData<Boolean> = _fileRemovalCheck

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
            .takeWhile {
                it is Result.Success
            }
            .collectLatest { result ->
                val mediaItemList = (result as Result.Success).data.filter {
                    !it.bucketName.isNullOrEmpty()
                } as ArrayList<MiItemMedia>
                list.postValue(mediaItemList as ArrayList<MiItemMedia>)
                if (mediaItemList.isEmpty()) {
                    emptyList.postValue(true)
                }
            }
    }

    private suspend fun getAllPhotoVidDataFromDatabase() {
        mediaRepository.getAllImgVidMediaFile()
            .takeWhile { it is Result.Success }
            .collectLatest { result ->
                val mediaItemList = (result as Result.Success).data.filter {
                    _fileRemovalCheck.postValue(true)
                    it.mediaBucket.isNotEmpty()
                }
                mediaRepository.removeMediaData(mediaItemList)
                if (mediaItemList.isEmpty()) {
                    emptyList.postValue(true)
                }
            }
    }
}
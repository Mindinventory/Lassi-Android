package com.lassi.presentation.docs

import androidx.lifecycle.MutableLiveData
import com.lassi.data.common.Response
import com.lassi.data.media.MiMedia
import com.lassi.domain.media.MediaRepository
import com.lassi.presentation.common.LassiBaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

class DocsViewModel(private val mediaRepository: MediaRepository) : LassiBaseViewModel() {
    var fetchDocsLiveData = MutableLiveData<Response<ArrayList<MiMedia>>>()

    fun fetchDocs() {
        fetchDocsLiveData.value = Response.Loading()
        mediaRepository.fetchDocs()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                fetchDocsLiveData.value = Response.Success(it)
            }, {
                fetchDocsLiveData.value = Response.Error(it)
            }).collect()
    }
}
package com.lassi.presentation.media

import androidx.lifecycle.MutableLiveData
import com.lassi.data.media.MiMedia
import com.lassi.presentation.common.LassiBaseViewModel
import java.util.*

class SelectedMediaViewModel : LassiBaseViewModel() {
    val selectedMediaLiveData = MutableLiveData<ArrayList<MiMedia>>()
    private var selectedMedias = arrayListOf<MiMedia>()

    fun addAllSelectedMedia(selectedMedias: ArrayList<MiMedia>) {
        this.selectedMedias = selectedMedias
        this.selectedMedias = this.selectedMedias.distinctBy {
            it.path
        } as ArrayList<MiMedia>
        selectedMediaLiveData.value = this.selectedMedias
    }

    fun addSelectedMedia(selectedMedia: MiMedia) {
        this.selectedMedias.add(selectedMedia)
        this.selectedMedias = this.selectedMedias.distinctBy {
            it.path
        } as ArrayList<MiMedia>
        selectedMediaLiveData.value = this.selectedMedias
    }
}
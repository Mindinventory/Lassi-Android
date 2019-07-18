package com.lassi.presentation.docs

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lassi.data.media.MediaDataRepository
import com.lassi.domain.media.MediaRepository

@Suppress("UNCHECKED_CAST")
class DocsViewModelFactory(val context: Context) : ViewModelProvider.Factory {
    private val mediaRepository: MediaRepository = MediaDataRepository(context)

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DocsViewModel(mediaRepository) as T
    }
}
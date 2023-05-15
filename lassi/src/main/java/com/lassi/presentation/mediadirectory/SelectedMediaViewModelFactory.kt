package com.lassi.presentation.mediadirectory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lassi.data.media.repository.SelectedMediaRepositoryImpl
import com.lassi.domain.media.SelectedMediaRepository
import com.lassi.presentation.media.SelectedMediaViewModel

@Suppress("UNCHECKED_CAST")
class SelectedMediaViewModelFactory(val context: Context) : ViewModelProvider.Factory {
    private val selectedMediaRepository: SelectedMediaRepository =
        SelectedMediaRepositoryImpl(context)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SelectedMediaViewModel(selectedMediaRepository) as T
    }
}
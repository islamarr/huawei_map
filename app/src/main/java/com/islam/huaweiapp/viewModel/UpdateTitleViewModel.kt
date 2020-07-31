package com.islam.huaweiapp.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UpdateTitleViewModel : ViewModel() {
    val title = MutableLiveData<String>()

    fun updateTitle(item: String) {
        title.value = item
    }
}
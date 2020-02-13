package ua.rdev.tripsmap

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(var repository: TripsRepository) : ViewModel() {
    val showProgress = MutableLiveData<Boolean>()
    var searches = repository.searches

    fun onLoadData() {
        viewModelScope.launch(Dispatchers.IO) {
            showProgress.postValue(true)
            repository.loadTripSearchData()
            showProgress.postValue(false)
        }

    }
}
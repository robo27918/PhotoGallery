package com.bignerdranch.android.photogallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

class PhotoGalleryViewModel: ViewModel() {
    val galleryItemLiveData : LiveData< List<GalleryItem> >
    private val flickrFetchr = FlickrFetchr()
    private val mutableSearchTerm = MutableLiveData<String>()
    init{
        mutableSearchTerm.value = "capybara"
        galleryItemLiveData = Transformations.switchMap(mutableSearchTerm){searhTerm ->
            flickrFetchr.searchPhotos(searhTerm)
        }
    }
    fun fetchPhotos(query:String = ""){
        mutableSearchTerm.value = query
    }
}
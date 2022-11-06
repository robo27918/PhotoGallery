package com.bignerdranch.android.photogallery

import android.app.Application
import androidx.lifecycle.*

class PhotoGalleryViewModel(private val app: Application): AndroidViewModel(app) {
    val galleryItemLiveData : LiveData< List<GalleryItem> >
    private val flickrFetchr = FlickrFetchr()
    private val mutableSearchTerm = MutableLiveData<String>()
    init{
        mutableSearchTerm.value = QueryPreferences.getStoredQuery(app)
        galleryItemLiveData = Transformations.switchMap(mutableSearchTerm){searhTerm ->
            flickrFetchr.searchPhotos(searhTerm)
        }
    }
    fun fetchPhotos(query:String = ""){
        QueryPreferences.setStoredQuery(app,query)
        mutableSearchTerm.value = query
    }
}
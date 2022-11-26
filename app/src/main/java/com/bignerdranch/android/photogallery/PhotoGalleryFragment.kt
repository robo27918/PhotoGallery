package com.bignerdranch.android.photogallery

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import api.FlickrApi
import api.FlickrResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import kotlin.reflect.KProperty

private const val TAG = "PhotoGalleryFragment"

class PhotoGalleryFragment: Fragment()
{
    private lateinit var photoGalleryViewModel: PhotoGalleryViewModel
    private lateinit var photoRecyclerView: RecyclerView
    private lateinit var thumbnailDownloader: ThumbnailDownloader<PhotoHolder>

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        //add here !!
        //retainInstance = true
        setHasOptionsMenu(true)
        photoGalleryViewModel =
            ViewModelProvider(this).get(PhotoGalleryViewModel::class.java)
        val responseHandler = Handler()
        thumbnailDownloader=
            ThumbnailDownloader(responseHandler){ photoHolder, bitmap ->
                val drawable = BitmapDrawable(resources, bitmap)
                photoHolder.bindDrawable(drawable)
            }
        lifecycle.addObserver(thumbnailDownloader.fragmentLifecycleObserver)
        /***
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .build()
        val workRequest = OneTimeWorkRequest
            .Builder(PollWorker::class.java)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance()
            .enqueue(workRequest)
        ***/
    }

    override fun onCreateView (
        inflater:LayoutInflater,
        container:ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_photo_gallery, container, false)

        photoRecyclerView = view.findViewById(R.id.photo_recycler_view)
        photoRecyclerView.layoutManager = GridLayoutManager(context,3)
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewLifecycleOwner.lifecycle.removeObserver(
            thumbnailDownloader.viewLifecycleObserver
        )
    }
    companion object{
        fun newInstance() = PhotoGalleryFragment()
    }
    override fun onViewCreated(view:View, savedInstanceState:Bundle?)
    {
        super.onViewCreated(view,savedInstanceState)
        photoGalleryViewModel.galleryItemLiveData.observe(
            viewLifecycleOwner,
            Observer { galleryItems ->
                photoRecyclerView.adapter = PhotoAdapater(galleryItems)
            })

    }
    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(thumbnailDownloader.fragmentLifecycleObserver)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_photo_gallery,menu)

        val searchItem: MenuItem = menu.findItem(R.id.menu_item_search)
        val searchView = searchItem.actionView as SearchView

        searchView.apply{
            setOnQueryTextListener(object: SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(queryText: String) : Boolean{
                    Log.d(TAG,"QueryTextSubmit: $queryText")
                    photoGalleryViewModel.fetchPhotos(queryText)
                    return true
                }
                override fun onQueryTextChange(queryText:String):Boolean {
                    Log.d(TAG,"QueryTextChange: $queryText")
                    return false
                }
            })
            setOnSearchClickListener{
                searchView.setQuery(photoGalleryViewModel.searchTerm,false)
            }
        }

        val toggleItem = menu.findItem(R.id.menu_item_toggle_polling)
        val isPolling = QueryPreferences.isPolling(requireContext())
        val toggleItemTitle = if (isPolling){
            R.string.stop_polling
        }else{
            R.string.start_polling
        }
        toggleItem.setTitle(toggleItemTitle)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.menu_item_clear->{
                photoGalleryViewModel.fetchPhotos("")
                true
            }
            else ->super.onOptionsItemSelected(item)
        }
    }
    private class PhotoHolder (itemImageView : ImageView)
        :RecyclerView.ViewHolder(itemImageView){
            val bindDrawable : (Drawable) -> Unit = itemImageView::setImageDrawable
        }

    private inner class PhotoAdapater (private val galleryItems: List <GalleryItem>)
        :RecyclerView.Adapter<PhotoHolder> (){

        override fun onCreateViewHolder(
            parent:ViewGroup,
            viewType: Int
            ): PhotoHolder {
                val view = layoutInflater.inflate(
                    R.layout.list_item_gallery,
                    parent,
                    false
                ) as ImageView
                return PhotoHolder(view)
            }
        override fun getItemCount(): Int = galleryItems.size

        override fun onBindViewHolder (holder:PhotoHolder , position: Int){
            val galleryItem = galleryItems[position]
            val placeholder: Drawable = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.bill_up_close
            )?:ColorDrawable()
            holder.bindDrawable(placeholder)
            thumbnailDownloader.queueThumbnail(holder,galleryItem.url)
         }
        }
}




package com.bignerdranch.android.photogallery

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        //add here !!
        photoGalleryViewModel =
            ViewModelProvider(this).get(PhotoGalleryViewModel::class.java)
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
         }
        }
}




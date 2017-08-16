package com.konifar.materialcat.presentation.gallery

import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ArrayAdapter
import android.widget.GridView
import com.konifar.materialcat.R
import com.konifar.materialcat.databinding.FragmentGalleryBinding
import com.konifar.materialcat.databinding.ViewPhotoBinding
import com.konifar.materialcat.domain.gallery.model.CatImage
import com.konifar.materialcat.extension.component
import com.konifar.materialcat.presentation.ListObserver
import com.konifar.materialcat.views.LoadingFooterView
import com.konifar.materialcat.views.listeners.OnLoadMoreScrollListener
import com.squareup.picasso.Picasso
import javax.inject.Inject


class GalleryFragment : Fragment(), ListObserver, GalleryPageNavigator, LifecycleRegistryOwner {

    enum class Type {
        POPULAR,
        NEW
    }

    companion object {
        private val ARG_TYPE = "type"

        fun newInstance(type: Type): GalleryFragment {
            return GalleryFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_TYPE, type)
                }
            }
        }
    }

    @Inject
    lateinit var presenter: GalleryPresenter

    private lateinit var binding: FragmentGalleryBinding

    private lateinit var adapter: PhotosArrayAdapter

    private lateinit var loadingFooter: LoadingFooterView

    private lateinit var type: Type

    private val registry = LifecycleRegistry(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        component.inject(this)

        type = arguments.getSerializable(ARG_TYPE) as Type

        presenter.apply {
            listObserver = this@GalleryFragment
            navigator = this@GalleryFragment
            viewModel = GalleryViewModel()
        }
        lifecycle.addObserver(presenter)
    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate<FragmentGalleryBinding>(
                inflater, R.layout.fragment_gallery, container, false)
                .apply { viewModel = presenter.viewModel }

        initGridView()
        initSwipeRefresh()

        requestCatImages(1)

        return binding.root
    }

    private fun initSwipeRefresh() {
        binding.swipeRefresh.run {
            setColorSchemeResources(R.color.theme500)
            setOnRefreshListener { requestCatImages(1) }
        }
    }

    private fun initGridView() {
        adapter = PhotosArrayAdapter(activity, presenter.viewModel.itemViewModels)
        loadingFooter = LoadingFooterView(activity)

        binding.gridView.run {
            addFooterView(loadingFooter)
            adapter = this@GalleryFragment.adapter
            setOnItemClickListener { _, _, position, _ -> onItemClick(position) }
            setOnScrollListener(object : OnLoadMoreScrollListener(innerOnScrollListener = activity as AbsListView.OnScrollListener) {
                override fun onLoadMore(page: Int, totalItemsCount: Int) {
                    loadingFooter.switchVisible(true)
                    requestCatImages(page)
                }
            })
        }
    }

    internal fun onItemClick(position: Int) {
        presenter.onClickItem(adapter.getItem(position).id)
    }

    private fun requestCatImages(page: Int) {
        when (type) {
            Type.POPULAR -> presenter.requestGetPopular(page)
            Type.NEW -> presenter.requestGetNew(page)
        }
    }

    override fun notifyDataSetChanged() {
        adapter.notifyDataSetChanged()
    }

    override fun getLifecycle(): LifecycleRegistry {
        return registry
    }

    override fun openDetail(catImage: CatImage) {
        TODO("not implemented")
    }

    internal class ViewHolder(view: View) {
        val binding: ViewPhotoBinding = DataBindingUtil.bind(view)
    }

    private class PhotosArrayAdapter(context: Context, viewModels: List<GalleryItemViewModel>)
        : ArrayAdapter<GalleryItemViewModel>(context, R.layout.view_photo, viewModels) {

        override fun getView(pos: Int, convertView: View?, parent: ViewGroup): View {
            val view: View
            val holder: ViewHolder

            if (convertView?.tag != null) {
                view = convertView
                holder = view.tag as ViewHolder
            } else {
                view = LayoutInflater.from(context).inflate(R.layout.view_photo, parent, false)
                holder = ViewHolder(view)
            }

            view.tag = holder

            bindData(holder, getItem(pos))
            holder.binding.ripple.setOnClickListener { (parent as GridView).performItemClick(view, pos, 0L) }

            return view
        }

        private fun bindData(holder: ViewHolder, viewModel: GalleryItemViewModel) {
            if (holder.binding.imgPreview.tag == null || holder.binding.imgPreview.tag != viewModel.imageUrl) {
                Picasso.with(context)
                        .load(viewModel.imageUrl)
                        .placeholder(R.color.theme50)
                        .into(holder.binding.imgPreview)
                holder.binding.imgPreview.tag = viewModel.imageUrl
            }
        }
    }

}
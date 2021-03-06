package chat.rocket.android.files.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import chat.rocket.android.R
import chat.rocket.android.chatroom.ui.ChatRoomActivity
import chat.rocket.android.files.adapter.FilesAdapter
import chat.rocket.android.files.presentation.FilesPresenter
import chat.rocket.android.files.presentation.FilesView
import chat.rocket.android.files.viewmodel.FileViewModel
import chat.rocket.android.helper.EndlessRecyclerViewScrollListener
import chat.rocket.android.helper.ImageHelper
import chat.rocket.android.player.PlayerActivity
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.util.extensions.ui
import chat.rocket.android.widget.DividerItemDecoration
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_files.*
import javax.inject.Inject

fun newInstance(chatRoomId: String): Fragment {
    return FilesFragment().apply {
        arguments = Bundle(1).apply {
            putString(BUNDLE_CHAT_ROOM_ID, chatRoomId)
        }
    }
}

private const val BUNDLE_CHAT_ROOM_ID = "chat_room_id"

class FilesFragment : Fragment(), FilesView {
    @Inject
    lateinit var presenter: FilesPresenter
    private val adapter: FilesAdapter =
        FilesAdapter { fileViewModel -> presenter.openFile(fileViewModel) }
    private val linearLayoutManager =
        LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    private lateinit var chatRoomId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)

        val bundle = arguments
        if (bundle != null) {
            chatRoomId = bundle.getString(BUNDLE_CHAT_ROOM_ID)
        } else {
            requireNotNull(bundle) { "no arguments supplied when the fragment was instantiated" }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_files)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        presenter.loadFiles(chatRoomId)
    }

    override fun showFiles(dataSet: List<FileViewModel>, total: Long) {
        setupToolbar(total)
        if (adapter.itemCount == 0) {
            adapter.prependData(dataSet)
            if (dataSet.size >= 30) {
                recycler_view.addOnScrollListener(object :
                    EndlessRecyclerViewScrollListener(linearLayoutManager) {
                    override fun onLoadMore(
                        page: Int,
                        totalItemsCount: Int,
                        recyclerView: RecyclerView?
                    ) {
                        presenter.loadFiles(chatRoomId)
                    }
                })
            }
        } else {
            adapter.appendData(dataSet)

        }
    }

    override fun playMedia(url: String) {
        ui {
            activity?.let {
                PlayerActivity.play(it, url)
            }
        }
    }

    override fun openImage(url: String, name: String) {
        ui {
            activity?.let {
                ImageHelper.openImage(it, url, name)
            }
        }
    }

    override fun openDocument(uri: Uri) {
        ui {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }

    override fun showMessage(resId: Int) {
        ui {
            showToast(resId)
        }
    }

    override fun showMessage(message: String) {
        ui {
            showToast(message)
        }
    }

    override fun showGenericErrorMessage() {
        showMessage(getString(R.string.msg_generic_error))
    }

    override fun showLoading() {
        ui { view_loading.isVisible = true }
    }

    override fun hideLoading() {
        ui { view_loading.isVisible = false }
    }

    private fun setupRecyclerView() {
        ui {
            recycler_view.layoutManager = linearLayoutManager
            recycler_view.addItemDecoration(DividerItemDecoration(it))
            recycler_view.adapter = adapter
        }
    }

    private fun setupToolbar(totalFiles: Long) {
        (activity as ChatRoomActivity).let {
            it.showToolbarTitle(getString(R.string.title_files_total, totalFiles))
            it.hideToolbarChatRoomIcon()
        }
    }
}
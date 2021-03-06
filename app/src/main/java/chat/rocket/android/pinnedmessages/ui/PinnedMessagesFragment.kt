package chat.rocket.android.pinnedmessages.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import chat.rocket.android.R
import chat.rocket.android.chatroom.adapter.ChatRoomAdapter
import chat.rocket.android.chatroom.ui.ChatRoomActivity
import chat.rocket.android.chatroom.viewmodel.BaseViewModel
import chat.rocket.android.helper.EndlessRecyclerViewScrollListener
import chat.rocket.android.pinnedmessages.presentation.PinnedMessagesPresenter
import chat.rocket.android.pinnedmessages.presentation.PinnedMessagesView
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.util.extensions.ui
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_pinned_messages.*
import javax.inject.Inject

fun newInstance(chatRoomId: String): Fragment {
    return PinnedMessagesFragment().apply {
        arguments = Bundle(1).apply {
            putString(BUNDLE_CHAT_ROOM_ID, chatRoomId)
        }
    }
}

private const val BUNDLE_CHAT_ROOM_ID = "chat_room_id"

class PinnedMessagesFragment : Fragment(), PinnedMessagesView {

    private lateinit var chatRoomId: String
    private lateinit var adapter: ChatRoomAdapter
    @Inject
    lateinit var presenter: PinnedMessagesPresenter

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
    ): View? = container?.inflate(R.layout.fragment_pinned_messages)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        presenter.loadPinnedMessages(chatRoomId)
    }

    override fun showPinnedMessages(pinnedMessages: List<BaseViewModel<*>>) {
        ui {
            if (recycler_view_pinned.adapter == null) {
                adapter = ChatRoomAdapter(enableActions = false)
                recycler_view_pinned.adapter = adapter
                val linearLayoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                recycler_view_pinned.layoutManager = linearLayoutManager
                recycler_view_pinned.itemAnimator = DefaultItemAnimator()
                if (pinnedMessages.size > 10) {
                    recycler_view_pinned.addOnScrollListener(object :
                        EndlessRecyclerViewScrollListener(linearLayoutManager) {
                        override fun onLoadMore(
                            page: Int,
                            totalItemsCount: Int,
                            recyclerView: RecyclerView?
                        ) {
                            presenter.loadPinnedMessages(chatRoomId)
                        }

                    })
                }
                pin_view.isVisible = pinnedMessages.isEmpty()
            }
            adapter.appendData(pinnedMessages)
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

    override fun showGenericErrorMessage() = showMessage(getString(R.string.msg_generic_error))

    override fun showLoading() {
        ui { view_loading.isVisible = true }
    }

    override fun hideLoading() {
        ui { view_loading.isVisible = false }
    }

    private fun setupToolbar() {
        (activity as ChatRoomActivity).let {
            it.showToolbarTitle(getString(R.string.title_pinned_messages))
            it.hideToolbarChatRoomIcon()
        }
    }
}
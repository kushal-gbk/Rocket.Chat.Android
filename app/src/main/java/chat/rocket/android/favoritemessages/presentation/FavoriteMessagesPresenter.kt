package chat.rocket.android.favoritemessages.presentation

import chat.rocket.android.chatroom.viewmodel.ViewModelMapper
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.domain.ChatRoomsInteractor
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extensions.launchUI
import chat.rocket.common.RocketChatException
import chat.rocket.common.util.ifNull
import chat.rocket.core.internal.rest.getFavoriteMessages
import timber.log.Timber
import javax.inject.Inject

class FavoriteMessagesPresenter @Inject constructor(
    private val view: FavoriteMessagesView,
    private val strategy: CancelStrategy,
    private val roomsInteractor: ChatRoomsInteractor,
    private val mapper: ViewModelMapper,
    val serverInteractor: GetCurrentServerInteractor,
    val factory: RocketChatClientFactory
) {
    private val serverUrl = serverInteractor.get()!!
    private val client = factory.create(serverUrl)
    private var offset: Int = 0

    /**
     * Loads all favorite messages for room. the given room id.
     *
     * @param roomId The id of the room to get its favorite messages.
     */
    fun loadFavoriteMessages(roomId: String) {
        launchUI(strategy) {
            try {
                view.showLoading()
                roomsInteractor.getById(serverUrl, roomId)?.let {
                    val favoriteMessages = client.getFavoriteMessages(roomId, it.type, offset)
                    val messageList = mapper.map(favoriteMessages.result)
                    view.showFavoriteMessages(messageList)
                    offset += 1 * 30
                }.ifNull {
                    Timber.e("Couldn't find a room with id: $roomId at current server.")
                }
            } catch (exception: RocketChatException) {
                Timber.e(exception)
            } finally {
                view.hideLoading()
            }
        }
    }
}
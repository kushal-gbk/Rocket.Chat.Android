package chat.rocket.android.pinnedmessages.di

import chat.rocket.android.chatroom.di.PinnedMessagesFragmentModule
import chat.rocket.android.pinnedmessages.ui.PinnedMessagesFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class PinnedMessagesFragmentProvider {

    @ContributesAndroidInjector(modules = [PinnedMessagesFragmentModule::class])
    abstract fun providePinnedMessageFragment(): PinnedMessagesFragment
}
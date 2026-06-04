package com.example.baristamessenger.di

import androidx.room.Room
import com.example.baristamessenger.data.local.AppDatabase
import com.example.baristamessenger.data.repository.MessageRepositoryImpl
import com.example.baristamessenger.domain.repository.MessageRepository
import com.example.baristamessenger.presentation.viewmodel.ChatViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModelOf
import com.example.baristamessenger.presentation.viewmodel.ChatsListViewModel
import com.example.baristamessenger.presentation.viewmodel.AuthViewModel
import com.example.baristamessenger.presentation.viewmodel.ProfileViewModel
import com.example.baristamessenger.presentation.viewmodel.SearchUserViewModel

val appModule = module {
    ->


    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "barista_database.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }


    single {
        get<AppDatabase>().messageDao()
    }


    single<MessageRepository> {
        MessageRepositoryImpl(messageDao = get())
    }

    viewModelOf(::ChatViewModel)
    viewModelOf(::ChatsListViewModel)
    viewModelOf(::AuthViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::SearchUserViewModel)
}
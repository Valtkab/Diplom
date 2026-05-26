package com.example.baristamessenger.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.baristamessenger.data.local.entity.ChatEntity
import com.example.baristamessenger.data.local.entity.MessageEntity

@Database(entities = [MessageEntity::class, ChatEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
}
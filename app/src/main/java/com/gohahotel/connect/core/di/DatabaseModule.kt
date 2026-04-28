package com.gohahotel.connect.core.di

import android.content.Context
import androidx.room.Room
import com.gohahotel.connect.data.local.GohaDatabase
import com.gohahotel.connect.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GohaDatabase {
        return Room.databaseBuilder(
            context,
            GohaDatabase::class.java,
            "goha_hotel.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideRoomDao(db: GohaDatabase): RoomDao = db.roomDao()

    @Provides
    fun provideMenuDao(db: GohaDatabase): MenuDao = db.menuDao()

    @Provides
    fun provideGuideDao(db: GohaDatabase): GuideDao = db.guideDao()

    @Provides
    fun provideOrderDao(db: GohaDatabase): OrderDao = db.orderDao()
}

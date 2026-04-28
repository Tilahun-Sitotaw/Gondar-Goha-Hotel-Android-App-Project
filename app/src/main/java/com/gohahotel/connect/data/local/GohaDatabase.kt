package com.gohahotel.connect.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gohahotel.connect.data.local.dao.GuideDao
import com.gohahotel.connect.data.local.dao.MenuDao
import com.gohahotel.connect.data.local.dao.OrderDao
import com.gohahotel.connect.data.local.dao.RoomDao
import com.gohahotel.connect.data.local.entity.GuideEntryEntity
import com.gohahotel.connect.data.local.entity.MenuItemEntity
import com.gohahotel.connect.data.local.entity.OrderEntity
import com.gohahotel.connect.data.local.entity.RoomEntity

@Database(
    entities = [
        RoomEntity::class,
        MenuItemEntity::class,
        GuideEntryEntity::class,
        OrderEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GohaDatabase : RoomDatabase() {
    abstract fun roomDao(): RoomDao
    abstract fun menuDao(): MenuDao
    abstract fun guideDao(): GuideDao
    abstract fun orderDao(): OrderDao

    companion object {
        const val DATABASE_NAME = "goha_hotel_db"
    }
}

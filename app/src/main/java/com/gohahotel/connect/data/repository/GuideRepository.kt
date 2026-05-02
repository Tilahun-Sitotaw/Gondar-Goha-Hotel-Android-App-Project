package com.gohahotel.connect.data.repository

import com.gohahotel.connect.data.local.dao.GuideDao
import com.gohahotel.connect.data.local.toDomain
import com.gohahotel.connect.data.local.toEntity
import com.gohahotel.connect.data.remote.FirestoreService
import com.gohahotel.connect.domain.model.GuideCategory
import com.gohahotel.connect.domain.model.GuideEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GuideRepository @Inject constructor(
    private val guideDao: GuideDao,
    private val firestoreService: FirestoreService
) {
    fun getAllEntries(): Flow<List<GuideEntry>> =
        guideDao.getAllEntries().map { it.map { e -> e.toDomain() } }

    fun getEntriesByCategory(category: GuideCategory): Flow<List<GuideEntry>> =
        guideDao.getEntriesByCategory(category.name).map { it.map { e -> e.toDomain() } }

    fun getNearbyEntries(): Flow<List<GuideEntry>> =
        guideDao.getNearbyEntries().map { it.map { e -> e.toDomain() } }

    fun searchEntries(query: String): Flow<List<GuideEntry>> =
        guideDao.searchEntries(query).map { it.map { e -> e.toDomain() } }

    suspend fun getEntryById(id: String): GuideEntry? =
        guideDao.getEntryById(id)?.toDomain()

    suspend fun initializeGuide() {
        try {
            val entries = firestoreService.fetchGuideEntries()
            if (entries.isNotEmpty()) {
                guideDao.clearAll()
                guideDao.upsertEntries(entries.map { it.toEntity() })
            }
        } catch (e: Exception) {
            // Offline: silently ignore, local cache will be used
        }
    }
}

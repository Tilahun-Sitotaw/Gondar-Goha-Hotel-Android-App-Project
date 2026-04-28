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

    /**
     * Seeds the database with pre-bundled guide data, then tries to sync from Firestore.
     * Works fully offline because seeded data is always available.
     */
    suspend fun initializeGuide() {
        val count = guideDao.getCount()
        if (count == 0) seedLocalGuide()
        try {
            val entries = firestoreService.fetchGuideEntries()
            if (entries.isNotEmpty()) {
                guideDao.clearAll()
                guideDao.upsertEntries(entries.map { it.toEntity() })
            }
        } catch (e: Exception) {
            // Offline: silently ignore, local seed is already loaded
        }
    }

    private suspend fun seedLocalGuide() {
        val entries = listOf(
            GuideEntry(
                id = "fasil_ghebbi",
                title = "Fasil Ghebbi (Royal Enclosure)",
                titleAmharic = "ፋሲል ግቢ (የንጉሣዊ ቤተ መንግሥት)",
                titleFrench = "Fasil Ghebbi (Enceinte Royale)",
                summary = "A UNESCO World Heritage Site — the medieval fortress-city of Emperor Fasilides.",
                summaryAmharic = "የዩኔስኮ የዓለም ቅርስ — የንጉሠ ነገሥት ፋሲለደስ መካከለኛው ዘመን ቤተ መንግሥት ከተማ።",
                summaryFrench = "Site du patrimoine mondial de l'UNESCO — la cité fortifiée médiévale de l'Empereur Fasilides.",
                content = "The Royal Enclosure of Gondar, built by Emperor Fasilides in 1636, is a stunning complex of castles and palaces that served as the center of the Ethiopian empire for over 200 years. The complex includes six large castles, numerous smaller buildings, pools, and churches. The architecture blends Aksumite, Portuguese, and Indian styles.",
                contentAmharic = "በ1636 ዓ.ም. በንጉሠ ነገሥት ፋሲለደስ የተገነባው የጎንደር ንጉሣዊ ቤተ መንግሥት፣ ከ200 ዓመታት በላይ የኢትዮጵያ ኢምፓየር ማዕከል ሆኖ ያገለገለ አስደናቂ የቤተ መንግሥቶች ስብስብ ነው።",
                contentFrench = "L'enceinte royale de Gondar, construite par l'Empereur Fasilides en 1636, est un complexe de châteaux et palais qui a servi de centre de l'empire éthiopien pendant plus de 200 ans.",
                category = GuideCategory.HERITAGE,
                imageUrls = listOf(),
                latitude = 12.6030,
                longitude = 37.4673,
                distanceFromHotelKm = 1.2,
                openingHours = "08:00 - 18:00",
                entryFee = "100 ETB",
                tags = listOf("UNESCO", "castle", "history", "fasilides", "gondar")
            ),
            GuideEntry(
                id = "debre_berhan_selassie",
                title = "Debre Berhan Selassie Church",
                titleAmharic = "ደብረ ብርሃን ሥላሴ ቤተ ክርስቲያን",
                titleFrench = "Église Debre Berhan Selassie",
                summary = "Famous for its ceiling of 80 winged cherub faces — one of Ethiopia's most beautiful churches.",
                summaryAmharic = "80 የመላእክት ፊቶች ባሉት ጣሪያዋ ዝነኛ — ከኢትዮጵያ ውብ አብያተ ክርስቲያናት አንዷ።",
                summaryFrench = "Célèbre pour son plafond orné de 80 visages de chérubins ailés.",
                content = "Debre Berhan Selassie, meaning 'Trinity of the Light of Zion', is a 17th century Ethiopian Orthodox church. Its ceiling is adorned with 80 painted cherub faces looking downward, creating a breathtaking visual. The walls feature vivid murals depicting scenes from the Old and New Testament.",
                contentAmharic = "ደብረ ብርሃን ሥላሴ፣ 'የጽዮን ብርሃን ሥላሴ' ማለት ሲሆን፣ 80 የተሳሉ የቼሩቢም ፊቶች ባሉት ጣሪያ ዝነኛ የሆነ የ17ኛው ክፍለ ዘመን የኢትዮጵያ ኦርቶዶክስ ቤተ ክርስቲያን ነው።",
                contentFrench = "Debre Berhan Selassie, signifiant 'Trinité de la Lumière de Sion', est une église orthodoxe éthiopienne du 17ème siècle.",
                category = GuideCategory.CHURCHES,
                imageUrls = listOf(),
                latitude = 12.6120,
                longitude = 37.4753,
                distanceFromHotelKm = 2.1,
                openingHours = "06:00 - 12:00, 14:00 - 18:00",
                entryFee = "50 ETB",
                tags = listOf("church", "orthodox", "mural", "heritage", "gondar")
            ),
            GuideEntry(
                id = "goha_hilltop",
                title = "Goha Hill Sunset Point",
                titleAmharic = "የጎሃ ኮረብታ ፀሐይ መጥለቂያ ቦታ",
                titleFrench = "Point de Coucher de Soleil de la Colline Goha",
                summary = "The iconic hilltop where Goha Hotel stands — offering panoramic views of Gondar city.",
                summaryAmharic = "ጎሃ ሆቴል የሚቆምበት አዶናዊ ኮረብታ — የጎንደር ከተማ ፓኖራሚክ እይታ ያቀርባል።",
                summaryFrench = "La colline iconique où se trouve l'Hôtel Goha — offrant une vue panoramique sur la ville de Gondar.",
                content = "Located right at Goha Hotel, this hilltop offers one of the most spectacular sunset views in all of Ethiopia. Watch the golden sun dip behind the mountains as the lights of Gondar city begin to twinkle below. The hotel's terrace restaurant provides the perfect setting to enjoy this daily spectacle.",
                contentAmharic = "በጎሃ ሆቴል ጥግ ላይ የሚገኘው ይህ ኮረብታ በኢትዮጵያ ውስጥ ካሉ በጣም አስደናቂ የፀሐይ ጠለቅ እይታዎች አንዱን ያቀርባል።",
                contentFrench = "Situé à l'Hôtel Goha, cette colline offre l'une des vues les plus spectaculaires sur le coucher du soleil en Éthiopie.",
                category = GuideCategory.NATURE,
                imageUrls = listOf(),
                latitude = 12.6148,
                longitude = 37.4720,
                distanceFromHotelKm = 0.0,
                openingHours = "Always open",
                entryFee = "Free",
                tags = listOf("sunset", "view", "nature", "goha", "hilltop")
            )
        )
        guideDao.upsertEntries(entries.map { it.toEntity() })
    }
}

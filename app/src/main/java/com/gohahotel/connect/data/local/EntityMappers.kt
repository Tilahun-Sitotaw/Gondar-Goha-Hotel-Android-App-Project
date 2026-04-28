package com.gohahotel.connect.data.local

import com.gohahotel.connect.data.local.entity.*
import com.gohahotel.connect.domain.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private val gson = Gson()
private val stringListType = object : TypeToken<List<String>>() {}.type

private fun String.toList(): List<String> = try {
    gson.fromJson(this, stringListType) ?: emptyList()
} catch (e: Exception) {
    emptyList()
}

private fun List<String>.toJson(): String = gson.toJson(this)

fun RoomEntity.toDomain() = HotelRoom(
    id = id,
    name = name,
    type = RoomType.valueOf(type),
    description = description,
    pricePerNight = pricePerNight,
    currency = currency,
    capacity = capacity,
    bedType = bedType,
    floorNumber = floorNumber,
    amenities = amenities.toList(),
    imageUrls = imageUrls.toList(),
    isAvailable = isAvailable,
    rating = rating,
    reviewCount = reviewCount,
    hasView = hasView,
    hasMountainView = hasMountainView
)

fun HotelRoom.toEntity() = RoomEntity(
    id = id,
    name = name,
    type = type.name,
    description = description,
    pricePerNight = pricePerNight,
    currency = currency,
    capacity = capacity,
    bedType = bedType,
    floorNumber = floorNumber,
    amenities = amenities.toJson(),
    imageUrls = imageUrls.toJson(),
    isAvailable = isAvailable,
    rating = rating,
    reviewCount = reviewCount,
    hasView = hasView,
    hasMountainView = hasMountainView
)

fun MenuItemEntity.toDomain() = MenuItem(
    id = id,
    name = name,
    nameAmharic = nameAmharic,
    nameFrench = nameFrench,
    description = description,
    descriptionAmharic = descriptionAmharic,
    category = MenuCategory.valueOf(category),
    price = price,
    currency = currency,
    imageUrl = imageUrl,
    isAvailable = isAvailable,
    isVegetarian = isVegetarian,
    isVegan = isVegan,
    isGlutenFree = isGlutenFree,
    isSpicy = isSpicy,
    spiceLevel = spiceLevel,
    allergens = allergens.toList(),
    prepTimeMinutes = prepTimeMinutes,
    customizations = customizations.toList(),
    isFeatured = isFeatured,
    rating = rating
)

fun MenuItem.toEntity() = MenuItemEntity(
    id = id,
    name = name,
    nameAmharic = nameAmharic,
    nameFrench = nameFrench,
    description = description,
    descriptionAmharic = descriptionAmharic,
    category = category.name,
    price = price,
    currency = currency,
    imageUrl = imageUrl,
    isAvailable = isAvailable,
    isVegetarian = isVegetarian,
    isVegan = isVegan,
    isGlutenFree = isGlutenFree,
    isSpicy = isSpicy,
    spiceLevel = spiceLevel,
    allergens = allergens.toJson(),
    prepTimeMinutes = prepTimeMinutes,
    customizations = customizations.toJson(),
    isFeatured = isFeatured,
    rating = rating
)

fun GuideEntryEntity.toDomain() = GuideEntry(
    id = id,
    title = title,
    titleAmharic = titleAmharic,
    titleFrench = titleFrench,
    summary = summary,
    summaryAmharic = summaryAmharic,
    summaryFrench = summaryFrench,
    content = content,
    contentAmharic = contentAmharic,
    contentFrench = contentFrench,
    category = GuideCategory.valueOf(category),
    imageUrls = imageUrls.toList(),
    latitude = latitude,
    longitude = longitude,
    distanceFromHotelKm = distanceFromHotelKm,
    openingHours = openingHours,
    entryFee = entryFee,
    tags = tags.toList()
)

fun GuideEntry.toEntity() = GuideEntryEntity(
    id = id,
    title = title,
    titleAmharic = titleAmharic,
    titleFrench = titleFrench,
    summary = summary,
    summaryAmharic = summaryAmharic,
    summaryFrench = summaryFrench,
    content = content,
    contentAmharic = contentAmharic,
    contentFrench = contentFrench,
    category = category.name,
    imageUrls = imageUrls.toJson(),
    latitude = latitude,
    longitude = longitude,
    distanceFromHotelKm = distanceFromHotelKm,
    openingHours = openingHours,
    entryFee = entryFee,
    tags = tags.toJson()
)

# Hotel Management System - Integration Guide

## Quick Start Integration

### Step 1: Update Navigation
Add new routes to your navigation graph:

```kotlin
// In your navigation setup
composable("staff_dashboard") {
    StaffDashboard(
        onBack = { navController.popBackStack() },
        onLogout = { /* Handle logout */ }
    )
}

composable("kitchen_dashboard") {
    KitchenDashboard(
        onBack = { navController.popBackStack() },
        onLogout = { /* Handle logout */ }
    )
}
```

### Step 2: Update AdminDashboard Navigation
```kotlin
// In AdminDashboard
onNavigateToChat = {
    navController.navigate("staff_dashboard")
}
```

### Step 3: Add to Dependency Injection
Ensure your Hilt modules include:

```kotlin
// In AppModule.kt
@Provides
@Singleton
fun provideStaffViewModel(
    bookingRepository: BookingRepository
): StaffViewModel = StaffViewModel(bookingRepository)

@Provides
@Singleton
fun provideKitchenViewModel(
    foodRepository: FoodRepository
): KitchenViewModel = KitchenViewModel(foodRepository)
```

### Step 4: Update Repositories
Ensure repositories have these methods:

```kotlin
// BookingRepository
suspend fun getAllBookings(): List<Booking>
suspend fun updateBookingStatus(bookingId: String, status: BookingStatus)

// FoodRepository
suspend fun getAllOrders(): List<Order>
suspend fun updateOrderStatus(orderId: String, status: OrderStatus)
```

### Step 5: Update Firestore Service
Add these methods to FirestoreService:

```kotlin
suspend fun fetchAllBookings(): List<Booking>
suspend fun fetchAllOrders(): List<Order>
fun observeAllOrders(): Flow<List<Order>>
suspend fun updateOrderStatus(orderId: String, status: String)
```

---

## Feature Integration Checklist

### Guest Validation
- [ ] Import `AvailabilityCalculator` in RoomDetailScreen
- [ ] Add `guestValidationError` state variable
- [ ] Call validation before booking
- [ ] Display error message in UI
- [ ] Test with various guest counts

### Room Photos
- [ ] Update Room model with `imageUrls: List<String>`
- [ ] Update RoomDetailScreen carousel logic
- [ ] Test with multiple photos
- [ ] Verify swipe gestures work
- [ ] Check photo counter display

### Staff Dashboard
- [ ] Create StaffDashboard.kt
- [ ] Create StaffViewModel.kt
- [ ] Add navigation route
- [ ] Test booking list loading
- [ ] Test filter functionality
- [ ] Verify real-time updates

### Kitchen Dashboard
- [ ] Create KitchenDashboard.kt
- [ ] Create KitchenViewModel.kt
- [ ] Add navigation route
- [ ] Test order loading
- [ ] Test status updates
- [ ] Verify horizontal scroll

### Admin Dashboard
- [ ] Update AdminDashboard.kt with tabs
- [ ] Update AdminViewModel.kt with new methods
- [ ] Test booking tab
- [ ] Test order tab
- [ ] Test filters
- [ ] Verify statistics

---

## Testing Checklist

### Unit Tests
```kotlin
// Test guest validation
@Test
fun testGuestValidation_ExceedsCapacity() {
    val result = AvailabilityCalculator.validateGuestCount(5, 4)
    assertFalse(result.isValid)
    assertTrue(result.message.contains("exceeds"))
}

// Test availability calculation
@Test
fun testAvailabilityCalculation() {
    val bookings = listOf(
        Booking(checkInDate = "2024-01-15", checkOutDate = "2024-01-18")
    )
    val available = AvailabilityCalculator.isRoomAvailable(
        "2024-01-18", "2024-01-20", bookings
    )
    assertTrue(available)
}
```

### Integration Tests
- [ ] Test booking creation with validation
- [ ] Test order status updates
- [ ] Test real-time updates
- [ ] Test filter functionality
- [ ] Test navigation between dashboards

### UI Tests
- [ ] Test photo carousel navigation
- [ ] Test error message display
- [ ] Test tab switching
- [ ] Test filter chip selection
- [ ] Test card interactions

---

## Performance Optimization

### Data Loading
```kotlin
// Use pagination for large lists
val bookings by viewModel.allBookings.collectAsState()
    .map { it.take(20) }  // Load first 20

// Implement lazy loading
LazyColumn {
    items(bookings.size) { index ->
        if (index == bookings.size - 1) {
            viewModel.loadMore()
        }
        BookingCard(bookings[index])
    }
}
```

### Memory Management
```kotlin
// Clear resources in onDispose
DisposableEffect(Unit) {
    onDispose {
        viewModel.clearError()
        viewModel.cancelLoading()
    }
}
```

---

## Error Handling

### Common Errors

**Error:** `BookingRepository not found`
- **Solution:** Ensure repository is provided in Hilt module
- **Check:** Verify @Provides annotation

**Error:** `Firestore permission denied`
- **Solution:** Update Firestore security rules
- **Check:** Verify user authentication

**Error:** `Image loading fails`
- **Solution:** Verify image URLs are accessible
- **Check:** Check network connectivity

---

## Real-time Updates Setup

### Firestore Listeners
```kotlin
// In ViewModel
fun loadFoodOrders() {
    firestoreService.observeAllOrders()
        .catch { /* Handle error */ }
        .onEach { _foodOrders.value = it }
        .launchIn(viewModelScope)
}
```

### StateFlow Updates
```kotlin
// Automatic UI updates
val orders by viewModel.foodOrders.collectAsState()
// UI recomposes when orders change
```

---

## Customization Guide

### Styling
Update colors in `ui/theme/Color.kt`:
```kotlin
val GoldPrimary = Color(0xFFD4AF37)
val TealPrimary = Color(0xFF00897B)
val SuccessGreen = Color(0xFF4CAF50)
```

### Status Colors
Update in models:
```kotlin
enum class BookingStatus(val displayName: String, val color: String) {
    PENDING("Pending", "#FFA500"),
    CONFIRMED("Confirmed", "#4CAF50"),
    // ...
}
```

### Text Strings
Create strings.xml entries:
```xml
<string name="staff_dashboard_title">Staff Dashboard</string>
<string name="kitchen_dashboard_title">Kitchen Dashboard</string>
<string name="guest_validation_error">Guest count exceeds room capacity</string>
```

---

## Deployment Checklist

### Pre-deployment
- [ ] All tests passing
- [ ] No compilation errors
- [ ] Performance optimized
- [ ] Error handling complete
- [ ] Documentation updated

### Deployment
- [ ] Build release APK
- [ ] Test on real device
- [ ] Verify all features work
- [ ] Check network connectivity
- [ ] Monitor error logs

### Post-deployment
- [ ] Monitor user feedback
- [ ] Track error rates
- [ ] Measure performance
- [ ] Plan improvements
- [ ] Schedule updates

---

## Support Resources

### Documentation Files
- `HOTEL_MANAGEMENT_SYSTEM.md` - Complete system documentation
- `INTEGRATION_GUIDE.md` - This file
- Code comments in each file

### Key Files
- `domain/utils/AvailabilityCalculator.kt` - Availability logic
- `ui/staff/StaffDashboard.kt` - Staff interface
- `ui/kitchen/KitchenDashboard.kt` - Kitchen interface
- `ui/admin/AdminDashboard.kt` - Admin interface

### Contact & Support
For issues or questions:
1. Check documentation
2. Review code comments
3. Check error logs
4. Contact development team

---

## Version History

### v1.0.0 (Current)
- ✅ Guest validation system
- ✅ Room availability tracking
- ✅ Multiple room photos
- ✅ Staff dashboard
- ✅ Kitchen dashboard
- ✅ Enhanced admin dashboard
- ✅ Real-time updates
- ✅ Availability calculation

### Future Versions
- [ ] AI Concierge integration
- [ ] Advanced analytics
- [ ] Mobile notifications
- [ ] Loyalty program
- [ ] Guest preferences
- [ ] Predictive analytics

---

**Last Updated:** 2024  
**Status:** Ready for Integration  
**Compatibility:** Android 8.0+

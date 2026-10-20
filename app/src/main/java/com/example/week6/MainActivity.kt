package com.example.week6

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.week6.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Week6Theme(darkTheme = true) {
                GameLibraryApp()
            }
        }
    }
}

// Item Navigasi Utama
sealed class NavigationItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Store : NavigationItem("store", "Katalog", Icons.Filled.Storefront, Icons.Outlined.Storefront)
    object Wishlist : NavigationItem("wishlist", "Wishlist", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder)
    object Library : NavigationItem("library", "Koleksi", Icons.AutoMirrored.Filled.LibraryBooks, Icons.AutoMirrored.Outlined.LibraryBooks)
    object Profile : NavigationItem("profile", "Profil", Icons.Filled.Person, Icons.Outlined.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameLibraryApp() {
    val navItems = listOf(
        NavigationItem.Store,
        NavigationItem.Wishlist,
        NavigationItem.Library,
        NavigationItem.Profile
    )
    var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }

    // State Utama Aplikasi
    val games = remember { mutableStateListOf(*getInitialGames().map { it.copy() }.toTypedArray()) }
    var walletBalance by rememberSaveable { mutableLongStateOf(500000L) }

    // Profile State
    var gamerName by rememberSaveable { mutableStateOf("XiruS") }
    var gamerStatus by rememberSaveable { mutableStateOf("Sedang bermain Elden Ring") }
    var gamerBio by rememberSaveable { mutableStateOf("🎮 Hardcore Gamer & RPG Enthusiast | PC Master Race") }
    var cloudSyncEnabled by rememberSaveable { mutableStateOf(true) }
    var autoUpdateEnabled by rememberSaveable { mutableStateOf(true) }
    var promoNotificationEnabled by rememberSaveable { mutableStateOf(true) }

    // Dialog & Interaction State
    var selectedGameForDetail by remember { mutableStateOf<Game?>(null) }
    var showTopUpDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var launchingGame by remember { mutableStateOf<Game?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Counts for Navigation Badges
    val favoriteCount = games.count { it.isFavorite }
    val ownedCount = games.count { it.isOwned }

    // Helper Action Functions
    fun toggleFavorite(game: Game) {
        val index = games.indexOfFirst { it.id == game.id }
        if (index != -1) {
            val updated = games[index].copy(isFavorite = !games[index].isFavorite)
            games[index] = updated
            if (selectedGameForDetail?.id == game.id) {
                selectedGameForDetail = updated
            }
            scope.launch {
                snackbarHostState.showSnackbar(
                    if (updated.isFavorite) "❤️ '${updated.title}' ditambahkan ke Wishlist!"
                    else "🤍 '${updated.title}' dihapus dari Wishlist."
                )
            }
        }
    }

    fun buyGame(game: Game) {
        val index = games.indexOfFirst { it.id == game.id }
        if (index != -1) {
            val cost = game.discountedPrice
            if (walletBalance >= cost) {
                walletBalance -= cost
                val updated = games[index].copy(isOwned = true)
                games[index] = updated
                if (selectedGameForDetail?.id == game.id) {
                    selectedGameForDetail = updated
                }
                scope.launch {
                    snackbarHostState.showSnackbar("🎉 Berhasil membeli '${game.title}'! Game telah masuk ke Koleksi Anda.")
                }
            } else {
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = "⚠️ Saldo Wallet tidak cukup (${cost.formatRupiah()})! Sisa saldo: ${walletBalance.formatRupiah()}",
                        actionLabel = "Top Up"
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        showTopUpDialog = true
                    }
                }
            }
        }
    }

    fun installGame(game: Game) {
        val index = games.indexOfFirst { it.id == game.id }
        if (index != -1) {
            val updated = games[index].copy(isInstalled = true)
            games[index] = updated
            if (selectedGameForDetail?.id == game.id) {
                selectedGameForDetail = updated
            }
            scope.launch {
                snackbarHostState.showSnackbar("📥 '${game.title}' selesai diunduh & siap dimainkan!")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SteamBluePrimary.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.SportsEsports,
                                    contentDescription = null,
                                    tint = SteamBluePrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "STEAM STORE",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = 1.sp,
                                color = SteamTextWhite
                            )
                            Text(
                                text = navItems[selectedItemIndex].title.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SteamBluePrimary,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                },
                actions = {
                    // Quick Steam Wallet Chip (Clickable to Top Up)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = SteamSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SteamCardBorder),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable { showTopUpDialog = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Wallet",
                                tint = SteamGreenAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = walletBalance.formatRupiah(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SteamTextWhite
                            )
                        }
                    }

                    // Notification Button
                    IconButton(onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("📢 Diskon Spesial Musim Ini: Hemat hingga 70% di Game Populer!")
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifikasi",
                            tint = SteamTextMuted
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SteamDarkBg,
                    titleContentColor = SteamTextWhite,
                    actionIconContentColor = SteamTextWhite
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SteamDarkSurface,
                tonalElevation = 8.dp
            ) {
                navItems.forEachIndexed { index, item ->
                    val isSelected = selectedItemIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedItemIndex = index },
                        label = {
                            Text(
                                text = item.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SteamBluePrimary,
                            selectedTextColor = SteamBluePrimary,
                            unselectedIconColor = SteamTextMuted,
                            unselectedTextColor = SteamTextMuted,
                            indicatorColor = SteamBluePrimary.copy(alpha = 0.15f)
                        ),
                        icon = {
                            when (item) {
                                NavigationItem.Wishlist -> {
                                    BadgedBox(
                                        badge = {
                                            if (favoriteCount > 0) {
                                                Badge(
                                                    containerColor = SteamSaleRed,
                                                    contentColor = Color.White
                                                ) {
                                                    Text(favoriteCount.toString())
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.title
                                        )
                                    }
                                }
                                NavigationItem.Library -> {
                                    BadgedBox(
                                        badge = {
                                            if (ownedCount > 0) {
                                                Badge(
                                                    containerColor = SteamBluePrimary,
                                                    contentColor = Color.Black
                                                ) {
                                                    Text(ownedCount.toString())
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.title
                                        )
                                    }
                                }
                                else -> {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.title
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SteamDarkBg)
                .padding(innerPadding)
        ) {
            Crossfade(targetState = selectedItemIndex, label = "ScreenCrossfade") { page ->
                when (page) {
                    0 -> GameHomeScreen(
                        games = games,
                        onSelectGame = { selectedGameForDetail = it },
                        onToggleFavorite = { toggleFavorite(it) }
                    )
                    1 -> GameFavoritesScreen(
                        games = games.filter { it.isFavorite },
                        onSelectGame = { selectedGameForDetail = it },
                        onRemoveFavorite = { toggleFavorite(it) },
                        onBuyGame = { buyGame(it) },
                        onExploreClick = { selectedItemIndex = 0 }
                    )
                    2 -> GameLibraryScreen(
                        games = games.filter { it.isOwned },
                        onSelectGame = { selectedGameForDetail = it },
                        onInstallGame = { installGame(it) },
                        onPlayGame = { launchingGame = it },
                        onExploreClick = { selectedItemIndex = 0 }
                    )
                    3 -> GamerProfileScreen(
                        name = gamerName,
                        bio = gamerBio,
                        status = gamerStatus,
                        walletBalance = walletBalance,
                        ownedCount = ownedCount,
                        favoriteCount = favoriteCount,
                        totalHours = games.filter { it.isOwned }.sumOf { it.hoursPlayed },
                        cloudSync = cloudSyncEnabled,
                        autoUpdate = autoUpdateEnabled,
                        promoNotification = promoNotificationEnabled,
                        onTopUpClick = { showTopUpDialog = true },
                        onEditProfileClick = { showEditProfileDialog = true },
                        onCloudSyncChange = { cloudSyncEnabled = it },
                        onAutoUpdateChange = { autoUpdateEnabled = it },
                        onPromoNotificationChange = { promoNotificationEnabled = it }
                    )
                }
            }
        }
    }

    // Modal Game Detail
    selectedGameForDetail?.let { game ->
        GameDetailDialog(
            game = game,
            walletBalance = walletBalance,
            onDismiss = { selectedGameForDetail = null },
            onToggleFavorite = { toggleFavorite(game) },
            onBuyGame = {
                buyGame(game)
            },
            onInstallGame = {
                installGame(game)
            },
            onPlayGame = {
                selectedGameForDetail = null
                launchingGame = game
            }
        )
    }

    // Modal Top Up Wallet
    if (showTopUpDialog) {
        TopUpWalletDialog(
            currentBalance = walletBalance,
            onDismiss = { showTopUpDialog = false },
            onTopUpConfirmed = { amount ->
                walletBalance += amount
                showTopUpDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar("💳 Top Up berhasil! Saldo Anda sekarang: ${walletBalance.formatRupiah()}")
                }
            }
        )
    }

    // Modal Edit Profile
    if (showEditProfileDialog) {
        EditProfileDialog(
            currentName = gamerName,
            currentBio = gamerBio,
            currentStatus = gamerStatus,
            onDismiss = { showEditProfileDialog = false },
            onSave = { newName, newBio, newStatus ->
                gamerName = newName
                gamerBio = newBio
                gamerStatus = newStatus
                showEditProfileDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar("👤 Profil berhasil diperbarui!")
                }
            }
        )
    }

    // Modal Simulasi Meluncurkan Game
    launchingGame?.let { game ->
        GameLaunchingDialog(
            game = game,
            onDismiss = { launchingGame = null }
        )
    }
}

// ==================== 1. KATALOG SCREEN (STORE) ====================
@Composable
fun GameHomeScreen(
    games: List<Game>,
    onSelectGame: (Game) -> Unit,
    onToggleFavorite: (Game) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val categories = listOf("Semua", "Diskon Spesial", "Action", "RPG", "Gratis (F2P)", "Shooter", "Roguelike")
    var selectedCategory by remember { mutableStateOf("Semua") }

    // Live Filtering Data
    val filteredGames = remember(games, searchQuery, selectedCategory) {
        games.filter { game ->
            val matchesSearch = searchQuery.isBlank() ||
                    game.title.contains(searchQuery, ignoreCase = true) ||
                    game.genre.contains(searchQuery, ignoreCase = true) ||
                    game.tags.any { it.contains(searchQuery, ignoreCase = true) }

            val matchesCategory = when (selectedCategory) {
                "Semua" -> true
                "Diskon Spesial" -> game.discountPercent > 0
                "Gratis (F2P)" -> game.price == 0L
                "Shooter" -> game.genre.contains("Shooter", ignoreCase = true) || game.tags.any { it.contains("FPS", true) }
                "Roguelike" -> game.genre.contains("Rogue", ignoreCase = true) || game.tags.any { it.contains("Rogue", true) }
                else -> game.genre.contains(selectedCategory, ignoreCase = true) || game.tags.any { it.contains(selectedCategory, true) }
            }

            matchesSearch && matchesCategory
        }
    }

    // Trending game untuk Featured Banner
    val featuredGame = games.firstOrNull { it.id == 1 } ?: games.first()
    val discountedGames = games.filter { it.discountPercent > 0 }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search Bar dengan Tombol Clear
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Cari judul game, genre, atau tag...", color = SteamTextMuted) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Cari", tint = SteamBluePrimary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Hapus teks", tint = SteamTextMuted)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SteamBluePrimary,
                    unfocusedBorderColor = SteamCardBorder,
                    focusedContainerColor = SteamDarkSurface,
                    unfocusedContainerColor = SteamDarkSurface,
                    focusedTextColor = SteamTextWhite,
                    unfocusedTextColor = SteamTextWhite
                ),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
        }

        // Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = {
                            Text(
                                text = category,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = SteamDarkSurface,
                            labelColor = SteamTextMuted,
                            selectedContainerColor = SteamBluePrimary,
                            selectedLabelColor = Color.Black
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) SteamBluePrimary else SteamCardBorder
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
        }

        // Featured Hero Banner (Jika tidak sedang mencari)
        if (searchQuery.isEmpty() && selectedCategory == "Semua") {
            item {
                FeaturedGameBanner(
                    game = featuredGame,
                    onDetailClick = { onSelectGame(featuredGame) }
                )
            }

            // Flash Sale / Special Offers Horizontal Section
            if (discountedGames.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocalOffer,
                                    contentDescription = null,
                                    tint = SteamGreenAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "PENAWARAN SPESIAL",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = SteamTextWhite,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Text(
                                text = "Diskon hingga 60%",
                                fontSize = 12.sp,
                                color = SteamGreenAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(discountedGames) { discGame ->
                                DiscountedGameMiniCard(
                                    game = discGame,
                                    onClick = { onSelectGame(discGame) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Title Daftar Game & Result Count
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (searchQuery.isNotEmpty()) "Hasil Pencarian (${filteredGames.size})"
                    else if (selectedCategory != "Semua") "Kategori: $selectedCategory (${filteredGames.size})"
                    else "Katalog Game Populer",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = SteamTextWhite
                )
                if (filteredGames.isNotEmpty()) {
                    Text(
                        text = "${filteredGames.size} game",
                        fontSize = 12.sp,
                        color = SteamTextMuted
                    )
                }
            }
        }

        // List Game Cards
        if (filteredGames.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    colors = CardDefaults.cardColors(containerColor = SteamDarkSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = SteamTextMuted,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tidak Ada Game Ditemukan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = SteamTextWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Coba ubah kata kunci pencarian atau ganti kategori.",
                            fontSize = 13.sp,
                            color = SteamTextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredGames, key = { it.id }) { game ->
                SteamGameCard(
                    game = game,
                    onCardClick = { onSelectGame(game) },
                    onToggleFavorite = { onToggleFavorite(game) }
                )
            }
        }
    }
}

// Banner Game Unggulan (Hero Banner)
@Composable
fun FeaturedGameBanner(
    game: Game,
    onDetailClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, SteamCardBorder, RoundedCornerShape(16.dp))
            .clickable { onDetailClick() },
        colors = CardDefaults.cardColors(containerColor = SteamDarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2C3E50),
                            SteamDarkSurface
                        )
                    )
                )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = SteamSaleRed,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "🔥 SPESIAL MINGGU INI",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        color = SteamSurfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = game.platform,
                            color = SteamTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.linearGradient(game.gradientColors)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = game.icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = game.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = SteamTextWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = game.publisher,
                            fontSize = 12.sp,
                            color = SteamBluePrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = SteamGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${game.rating} • Sangat Positif",
                                fontSize = 11.sp,
                                color = SteamTextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Price and Action Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (game.discountPercent > 0) {
                            Surface(
                                color = SteamGreenAccent,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "-${game.discountPercent}%",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = game.price.formatRupiah(),
                                    textDecoration = TextDecoration.LineThrough,
                                    fontSize = 11.sp,
                                    color = SteamTextMuted
                                )
                                Text(
                                    text = game.discountedPrice.formatRupiah(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = SteamGreenAccent
                                )
                            }
                        } else {
                            Text(
                                text = game.price.formatRupiah(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = SteamTextWhite
                            )
                        }
                    }

                    Button(
                        onClick = onDetailClick,
                        colors = ButtonDefaults.buttonColors(containerColor = SteamBluePrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Lihat Detail",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

// Mini Card untuk Penawaran Spesial (Horizontal Slider)
@Composable
fun DiscountedGameMiniCard(
    game: Game,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(170.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, SteamCardBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SteamDarkSurface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(Brush.linearGradient(game.gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = game.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )

                // Discount tag
                Surface(
                    color = SteamGreenAccent,
                    shape = RoundedCornerShape(topStart = 4.dp, bottomEnd = 4.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                ) {
                    Text(
                        text = "-${game.discountPercent}%",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = game.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = SteamTextWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = game.genre,
                    fontSize = 11.sp,
                    color = SteamTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = game.discountedPrice.formatRupiah(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = SteamGreenAccent
                    )
                    Text(
                        text = "${game.rating} ★",
                        fontSize = 11.sp,
                        color = SteamGold,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// Kartu Game Utama (Steam List Card)
@Composable
fun SteamGameCard(
    game: Game,
    onCardClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, SteamCardBorder, RoundedCornerShape(14.dp))
            .clickable { onCardClick() },
        colors = CardDefaults.cardColors(containerColor = SteamDarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Game Art Box
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(game.gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = game.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Game Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = game.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = SteamTextWhite,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${game.genre} • ${game.platform}",
                    fontSize = 12.sp,
                    color = SteamTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Rating
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = SteamGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = game.rating.toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SteamTextWhite
                        )
                    }

                    // Price Block
                    if (game.isOwned) {
                        Surface(
                            color = SteamBlueContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "DIMILIKI",
                                color = SteamBluePrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else if (game.discountPercent > 0) {
                        Surface(
                            color = SteamGreenAccent,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "-${game.discountPercent}%",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = game.discountedPrice.formatRupiah(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SteamGreenAccent
                        )
                    } else {
                        Text(
                            text = game.price.formatRupiah(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (game.price == 0L) SteamBluePrimary else SteamTextWhite
                        )
                    }
                }
            }

            // Wishlist Toggle Button
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (game.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (game.isFavorite) "Hapus dari Favorit" else "Tambah ke Favorit",
                    tint = if (game.isFavorite) SteamSaleRed else SteamTextMuted,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// ==================== 2. WISHLIST SCREEN ====================
@Composable
fun GameFavoritesScreen(
    games: List<Game>,
    onSelectGame: (Game) -> Unit,
    onRemoveFavorite: (Game) -> Unit,
    onBuyGame: (Game) -> Unit,
    onExploreClick: () -> Unit
) {
    val totalWishlistValue = games.sumOf { it.discountedPrice }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SteamDarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SteamCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Wishlist & Favorit",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = SteamTextWhite
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${games.size} item tersimpan",
                            fontSize = 12.sp,
                            color = SteamTextMuted
                        )
                    }

                    if (games.isNotEmpty()) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Estimasi Total",
                                fontSize = 11.sp,
                                color = SteamTextMuted
                            )
                            Text(
                                text = totalWishlistValue.formatRupiah(),
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = SteamGreenAccent
                            )
                        }
                    }
                }
            }
        }

        if (games.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SteamDarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SteamCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SteamSaleRed.copy(alpha = 0.15f),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FavoriteBorder,
                                    contentDescription = null,
                                    tint = SteamSaleRed,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Wishlist Anda Masih Kosong",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = SteamTextWhite
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Simpan game yang Anda minati untuk memantau diskon dan membelinya kapan saja.",
                            fontSize = 13.sp,
                            color = SteamTextMuted,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onExploreClick,
                            colors = ButtonDefaults.buttonColors(containerColor = SteamBluePrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Jelajahi Katalog",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            items(games, key = { it.id }) { game ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, SteamCardBorder, RoundedCornerShape(14.dp))
                        .clickable { onSelectGame(game) },
                    colors = CardDefaults.cardColors(containerColor = SteamDarkSurface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Brush.linearGradient(game.gradientColors)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = game.icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = game.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = SteamTextWhite,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = game.genre,
                                    fontSize = 12.sp,
                                    color = SteamTextMuted
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (game.discountPercent > 0) {
                                        Surface(
                                            color = SteamGreenAccent,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "-${game.discountPercent}%",
                                                color = Color.Black,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = game.discountedPrice.formatRupiah(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (game.discountPercent > 0) SteamGreenAccent else SteamTextWhite
                                    )
                                }
                            }

                            // Remove from Wishlist button
                            IconButton(onClick = { onRemoveFavorite(game) }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Hapus",
                                    tint = SteamSaleRed
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = SteamCardBorder.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Platform: ${game.platform}",
                                fontSize = 11.sp,
                                color = SteamTextMuted
                            )

                            if (game.isOwned) {
                                Surface(
                                    color = SteamBlueContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Sudah di Koleksi",
                                        color = SteamBluePrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            } else {
                                Button(
                                    onClick = { onBuyGame(game) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SteamGreenDark),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ShoppingCart,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Beli Sekarang",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== 3. LIBRARY SCREEN (KOLEKSI SAYA) ====================
@Composable
fun GameLibraryScreen(
    games: List<Game>,
    onSelectGame: (Game) -> Unit,
    onInstallGame: (Game) -> Unit,
    onPlayGame: (Game) -> Unit,
    onExploreClick: () -> Unit
) {
    var libraryFilter by remember { mutableStateOf("Semua") }
    val filters = listOf("Semua", "Terpasang", "Cloud")

    val filteredGames = remember(games, libraryFilter) {
        when (libraryFilter) {
            "Terpasang" -> games.filter { it.isInstalled }
            "Cloud" -> games.filter { !it.isInstalled }
            else -> games
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Library Overview Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SteamDarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SteamCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Koleksi Game Anda",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = SteamTextWhite
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${games.size} game siap dimainkan",
                                fontSize = 12.sp,
                                color = SteamTextMuted
                            )
                        }

                        Surface(
                            color = SteamBlueContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = SteamBluePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Steam Cloud Aktif",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SteamBluePrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Filter chips
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        filters.forEach { filter ->
                            val isSelected = libraryFilter == filter
                            FilterChip(
                                selected = isSelected,
                                onClick = { libraryFilter = filter },
                                label = {
                                    val count = when (filter) {
                                        "Terpasang" -> games.count { it.isInstalled }
                                        "Cloud" -> games.count { !it.isInstalled }
                                        else -> games.size
                                    }
                                    Text("$filter ($count)")
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = SteamSurfaceVariant,
                                    labelColor = SteamTextMuted,
                                    selectedContainerColor = SteamBluePrimary,
                                    selectedLabelColor = Color.Black
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) SteamBluePrimary else SteamCardBorder
                                )
                            )
                        }
                    }
                }
            }
        }

        if (filteredGames.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SteamDarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SteamCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = SteamTextMuted,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tidak Ada Game di Kategori Ini",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = SteamTextWhite
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Beli atau klaim game gratis di katalog toko untuk mulai mengoleksi.",
                            fontSize = 13.sp,
                            color = SteamTextMuted,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = onExploreClick,
                            colors = ButtonDefaults.buttonColors(containerColor = SteamBluePrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Buka Toko Game", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(filteredGames, key = { it.id }) { game ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, SteamCardBorder, RoundedCornerShape(14.dp))
                        .clickable { onSelectGame(game) },
                    colors = CardDefaults.cardColors(containerColor = SteamDarkSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(game.gradientColors)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = game.icon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = game.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = SteamTextWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (game.hoursPlayed > 0) "${game.hoursPlayed} jam dimainkan" else "Belum pernah dimainkan",
                                fontSize = 12.sp,
                                color = SteamTextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (game.isInstalled) SteamGreenAccent else SteamBluePrimary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (game.isInstalled) "Terpasang" else "Tersedia di Cloud",
                                    fontSize = 11.sp,
                                    color = if (game.isInstalled) SteamGreenAccent else SteamBluePrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Play or Install Button
                        if (game.isInstalled) {
                            Button(
                                onClick = { onPlayGame(game) },
                                colors = ButtonDefaults.buttonColors(containerColor = SteamGreenDark),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Mainkan",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("MAIN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onInstallGame(game) },
                                border = androidx.compose.foundation.BorderStroke(1.dp, SteamBluePrimary),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SteamBluePrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.FileDownload,
                                    contentDescription = "Pasang",
                                    tint = SteamBluePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PASANG", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== 4. PROFIL SCREEN (STEAM GAMER) ====================
@Composable
fun GamerProfileScreen(
    name: String,
    bio: String,
    status: String,
    walletBalance: Long,
    ownedCount: Int,
    favoriteCount: Int,
    totalHours: Int,
    cloudSync: Boolean,
    autoUpdate: Boolean,
    promoNotification: Boolean,
    onTopUpClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onCloudSyncChange: (Boolean) -> Unit,
    onAutoUpdateChange: (Boolean) -> Unit,
    onPromoNotificationChange: (Boolean) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Gamer Card Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SteamCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SteamDarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar with Steam Online Glow
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clip(CircleShape)
                        .border(3.dp, SteamOnlineGreen, CircleShape)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.xrs),
                        contentDescription = "Foto Profil Gamer",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = name,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        color = SteamTextWhite
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Terverifikasi",
                        tint = SteamBluePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(SteamGreenAccent)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = status,
                        fontSize = 12.sp,
                        color = SteamGreenAccent,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = bio,
                    fontSize = 12.sp,
                    color = SteamTextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Steam Level Progress Bar
                Surface(
                    color = SteamSurfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = SteamGold,
                                    shape = CircleShape,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "24",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp,
                                            color = Color.Black
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Steam Level 24",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = SteamTextWhite
                                )
                            }
                            Text(
                                text = "740 / 1000 XP",
                                fontSize = 11.sp,
                                color = SteamTextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { 0.74f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = SteamGold,
                            trackColor = SteamCardBorder
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onEditProfileClick,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SteamCardBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SteamTextWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profil & Status", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Steam Wallet Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SteamCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SteamDarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = SteamGreenContainer,
                        shape = CircleShape,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = SteamGreenAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Steam Wallet",
                            fontSize = 12.sp,
                            color = SteamTextMuted
                        )
                        Text(
                            text = walletBalance.formatRupiah(),
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = SteamTextWhite
                        )
                    }
                }

                Button(
                    onClick = onTopUpClick,
                    colors = ButtonDefaults.buttonColors(containerColor = SteamGreenDark),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Top Up", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                }
            }
        }

        // Gamer Stats (Koleksi, Jam, Trophy, Wishlist)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SteamCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SteamDarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Statistik Akun",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = SteamTextWhite
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    SteamStatItem(number = ownedCount.toString(), label = "Game Dimiliki")
                    SteamStatItem(number = "$totalHours Jam", label = "Waktu Main")
                    SteamStatItem(number = "32", label = "Prestasi / Piala")
                    SteamStatItem(number = favoriteCount.toString(), label = "Di Wishlist")
                }
            }
        }

        // Lencana / Badges Showcase
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SteamCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SteamDarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Lencana Koleksi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = SteamTextWhite
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    item { BadgeItem(icon = Icons.Default.MilitaryTech, title = "Veteran 5 Thn", color = SteamGold) }
                    item { BadgeItem(icon = Icons.Default.Public, title = "Komunitas", color = SteamBluePrimary) }
                    item { BadgeItem(icon = Icons.Default.Stars, title = "Kolektor RPG", color = SteamGreenAccent) }
                    item { BadgeItem(icon = Icons.Default.WorkspacePremium, title = "Summer Sale", color = SteamSaleRed) }
                }
            }
        }

        // Pengaturan Akun & Layanan
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SteamCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SteamDarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Pengaturan & Preferensi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = SteamTextWhite
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Switch 1: Cloud Sync
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Steam Cloud Sync", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = SteamTextWhite)
                        Text("Simpan progres game otomatis ke cloud", fontSize = 12.sp, color = SteamTextMuted)
                    }
                    Switch(
                        checked = cloudSync,
                        onCheckedChange = onCloudSyncChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = SteamBluePrimary,
                            uncheckedTrackColor = SteamSurfaceVariant
                        )
                    )
                }

                HorizontalDivider(color = SteamCardBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 10.dp))

                // Switch 2: Auto Update
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Unduh Pembaruan Otomatis", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = SteamTextWhite)
                        Text("Perbarui game secara otomatis di latar belakang", fontSize = 12.sp, color = SteamTextMuted)
                    }
                    Switch(
                        checked = autoUpdate,
                        onCheckedChange = onAutoUpdateChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = SteamBluePrimary,
                            uncheckedTrackColor = SteamSurfaceVariant
                        )
                    )
                }

                HorizontalDivider(color = SteamCardBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 10.dp))

                // Switch 3: Promo Notification
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Notifikasi Promo Game", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = SteamTextWhite)
                        Text("Dapatkan pemberitahuan diskon wishlist", fontSize = 12.sp, color = SteamTextMuted)
                    }
                    Switch(
                        checked = promoNotification,
                        onCheckedChange = onPromoNotificationChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = SteamBluePrimary,
                            uncheckedTrackColor = SteamSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SteamStatItem(number: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = number,
            fontWeight = FontWeight.Black,
            fontSize = 17.sp,
            color = SteamBluePrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = SteamTextMuted,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun BadgeItem(icon: ImageVector, title: String, color: Color) {
    Surface(
        color = SteamSurfaceVariant,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SteamCardBorder)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = SteamTextWhite
            )
        }
    }
}

// ==================== 5. MODALS & DIALOGS ====================

// Dialog Detail Game Lengkap
@Composable
fun GameDetailDialog(
    game: Game,
    walletBalance: Long,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onBuyGame: () -> Unit,
    onInstallGame: () -> Unit,
    onPlayGame: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, SteamCardBorder, RoundedCornerShape(20.dp)),
            color = SteamDarkSurface
        ) {
            val scrollState = rememberScrollState()

            Column(modifier = Modifier.fillMaxSize()) {
                // Header Banner Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Brush.linearGradient(game.gradientColors))
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = game.platform,
                                color = SteamBluePrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = game.title,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    }
                }

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Rating & Release
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Penerbit / Developer", fontSize = 11.sp, color = SteamTextMuted)
                            Text(game.publisher, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SteamTextWhite)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Tanggal Rilis", fontSize = 11.sp, color = SteamTextMuted)
                            Text(game.releaseDate, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SteamTextWhite)
                        }
                    }

                    // Reviews Summary
                    Surface(
                        color = SteamSurfaceVariant,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ThumbUp, contentDescription = null, tint = SteamBluePrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Ulasan Pengguna", fontSize = 11.sp, color = SteamTextMuted)
                                Text(game.reviewsCount, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SteamBluePrimary)
                            }
                        }
                    }

                    // Tags
                    Column {
                        Text("Tag Populer:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SteamTextWhite)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            game.tags.forEach { tag ->
                                Surface(
                                    color = SteamSurfaceElevated,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = tag,
                                        fontSize = 11.sp,
                                        color = SteamTextMuted,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Description
                    Column {
                        Text("Tentang Game Ini", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SteamTextWhite)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = game.description,
                            fontSize = 13.sp,
                            color = SteamTextMuted,
                            lineHeight = 18.sp
                        )
                    }

                    // System Requirements
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SteamSurfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Memory, contentDescription = null, tint = SteamBluePrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Persyaratan Sistem (Minimum)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SteamTextWhite)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("• OS: ${game.systemReq.os}", fontSize = 11.sp, color = SteamTextMuted)
                            Text("• Prosesor: ${game.systemReq.processor}", fontSize = 11.sp, color = SteamTextMuted)
                            Text("• Memori: ${game.systemReq.memory}", fontSize = 11.sp, color = SteamTextMuted)
                            Text("• Grafis: ${game.systemReq.graphics}", fontSize = 11.sp, color = SteamTextMuted)
                            Text("• Penyimpanan: ${game.systemReq.storage}", fontSize = 11.sp, color = SteamTextMuted)
                        }
                    }
                }

                // Bottom Action Bar
                Surface(
                    color = SteamDarkBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SteamCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Wishlist toggle button
                        OutlinedButton(
                            onClick = onToggleFavorite,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (game.isFavorite) SteamSaleRed else SteamCardBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (game.isFavorite) SteamSaleRed else SteamTextWhite
                            )
                        ) {
                            Icon(
                                imageVector = if (game.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (game.isFavorite) "Di Wishlist" else "Wishlist", fontSize = 12.sp)
                        }

                        // Purchase or Play Button
                        if (game.isOwned) {
                            if (game.isInstalled) {
                                Button(
                                    onClick = onPlayGame,
                                    colors = ButtonDefaults.buttonColors(containerColor = SteamGreenDark),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("MAINKAN SEKARANG", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            } else {
                                Button(
                                    onClick = onInstallGame,
                                    colors = ButtonDefaults.buttonColors(containerColor = SteamBluePrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color.Black)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("PASANG GAME", fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }
                        } else {
                            Button(
                                onClick = onBuyGame,
                                colors = ButtonDefaults.buttonColors(containerColor = SteamGreenDark),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (game.price == 0L) "KLAIM GRATIS" else "BELI (${game.discountedPrice.formatRupiah()})",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Dialog Top Up Saldo Wallet
@Composable
fun TopUpWalletDialog(
    currentBalance: Long,
    onDismiss: () -> Unit,
    onTopUpConfirmed: (Long) -> Unit
) {
    val presetAmounts = listOf(50000L, 100000L, 250000L, 500000L)
    var selectedAmount by remember { mutableLongStateOf(100000L) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SteamDarkSurface,
        titleContentColor = SteamTextWhite,
        textContentColor = SteamTextMuted,
        icon = {
            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = SteamGreenAccent, modifier = Modifier.size(36.dp))
        },
        title = {
            Text("Isi Saldo Steam Wallet", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Saldo Anda saat ini: ${currentBalance.formatRupiah()}", fontSize = 13.sp, color = SteamBluePrimary)
                Text("Pilih nominal pengisian saldo:", fontSize = 13.sp, color = SteamTextWhite)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    presetAmounts.forEach { amount ->
                        val isSelected = selectedAmount == amount
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    1.dp,
                                    if (isSelected) SteamGreenAccent else SteamCardBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedAmount = amount },
                            color = if (isSelected) SteamGreenContainer else SteamSurfaceVariant
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = amount.formatRupiah(),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) SteamGreenAccent else SteamTextWhite,
                                    fontSize = 14.sp
                                )
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SteamGreenAccent, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onTopUpConfirmed(selectedAmount) },
                colors = ButtonDefaults.buttonColors(containerColor = SteamGreenDark),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Bayar & Top Up", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = SteamTextMuted)
            }
        }
    )
}

// Dialog Edit Profile
@Composable
fun EditProfileDialog(
    currentName: String,
    currentBio: String,
    currentStatus: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var bio by remember { mutableStateOf(currentBio) }
    var status by remember { mutableStateOf(currentStatus) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SteamDarkSurface,
        titleContentColor = SteamTextWhite,
        title = { Text("Edit Profil Gamer", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Tampilan / Nickname") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SteamBluePrimary,
                        unfocusedBorderColor = SteamCardBorder,
                        focusedTextColor = SteamTextWhite,
                        unfocusedTextColor = SteamTextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = status,
                    onValueChange = { status = it },
                    label = { Text("Status Game Aktif") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SteamBluePrimary,
                        unfocusedBorderColor = SteamCardBorder,
                        focusedTextColor = SteamTextWhite,
                        unfocusedTextColor = SteamTextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio Profil") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SteamBluePrimary,
                        unfocusedBorderColor = SteamCardBorder,
                        focusedTextColor = SteamTextWhite,
                        unfocusedTextColor = SteamTextWhite
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, bio, status) },
                colors = ButtonDefaults.buttonColors(containerColor = SteamBluePrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Simpan", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = SteamTextMuted)
            }
        }
    )
}

// Dialog Simulasi Launching Game
@Composable
fun GameLaunchingDialog(
    game: Game,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SteamDarkSurface,
        icon = {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(game.gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                Icon(game.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
        },
        title = {
            Text(
                text = "Meluncurkan Game...",
                fontWeight = FontWeight.Bold,
                color = SteamTextWhite,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = game.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = SteamBluePrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Menyinkronkan cloud save dan mempersiapkan game...",
                    fontSize = 12.sp,
                    color = SteamTextMuted,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = SteamGreenAccent,
                    trackColor = SteamSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SteamGreenDark),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Masuk ke Game", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

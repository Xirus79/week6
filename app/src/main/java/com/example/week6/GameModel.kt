package com.example.week6

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// Data Model untuk Game di Steam Store
data class Game(
    val id: Int,
    val title: String,
    val genre: String,
    val rating: Double,
    val reviewsCount: String,
    val platform: String,
    val price: Long, // 0 = Gratis
    val discountPercent: Int, // misal 30 = 30% diskon
    val icon: ImageVector,
    val gradientColors: List<Color>,
    val description: String,
    val publisher: String,
    val releaseDate: String,
    val tags: List<String>,
    val systemReq: SystemRequirements,
    var isFavorite: Boolean = false,
    var isOwned: Boolean = false,
    var isInstalled: Boolean = false,
    var hoursPlayed: Int = 0
) {
    val discountedPrice: Long
        get() = if (discountPercent > 0) price - (price * discountPercent / 100) else price
}

data class SystemRequirements(
    val os: String = "Windows 10/11 64-bit",
    val processor: String,
    val memory: String,
    val graphics: String,
    val storage: String
)

// Helper formatting Rupiah
fun Long.formatRupiah(): String {
    if (this == 0L) return "GRATIS"
    return "Rp " + "%,d".format(this).replace(',', '.')
}

// Data awal game
fun getInitialGames(): List<Game> = listOf(
    Game(
        id = 1,
        title = "Elden Ring: Shadow of the Erdtree",
        genre = "Action RPG",
        rating = 4.9,
        reviewsCount = "256.400 ulasan (Sangat Positif)",
        platform = "PC / Steam Deck",
        price = 599000L,
        discountPercent = 30,
        icon = Icons.Default.Casino,
        gradientColors = listOf(Color(0xFF8B5E3C), Color(0xFF1B2838)),
        description = "Dipandu oleh Empyrean Miquella, pemain dipanggil ke Tanah Bayangan, tempat yang terhalang oleh Erdtree di mana sang dewi Marika pertama kali menginjakkan kaki. Hadapi bos legendaris baru dan temukan rahasia kelam dunia The Lands Between.",
        publisher = "FromSoftware / Bandai Namco",
        releaseDate = "21 Juni 2024",
        tags = listOf("Souls-like", "Open World", "Dark Fantasy", "Sulit"),
        systemReq = SystemRequirements(
            processor = "Intel Core i7-8700K / AMD Ryzen 5 3600X",
            memory = "16 GB RAM",
            graphics = "NVIDIA GeForce RTX 2060 6GB / AMD RX 5700 XT",
            storage = "80 GB SSD"
        ),
        isFavorite = true,
        isOwned = true,
        isInstalled = true,
        hoursPlayed = 184
    ),
    Game(
        id = 2,
        title = "Cyberpunk 2077: Phantom Liberty",
        genre = "Open World RPG",
        rating = 4.7,
        reviewsCount = "189.200 ulasan (Sangat Positif)",
        platform = "PC / Steam Deck",
        price = 450000L,
        discountPercent = 50,
        icon = Icons.Default.VideogameAsset,
        gradientColors = listOf(Color(0xFFE63946), Color(0xFF1D3557)),
        description = "Phantom Liberty adalah petualangan mata-mata thriller berlatar di distrik Dogtown yang berbahaya di Night City. Sebagai V, tentara bayaran cyber-enhanced, masuklah ke dalam jaringan konspirasi politik dan spionase.",
        publisher = "CD PROJEKT RED",
        releaseDate = "26 September 2023",
        tags = listOf("Cyberpunk", "RPG", "Futuristic", "Story Rich"),
        systemReq = SystemRequirements(
            processor = "Intel Core i7-12700 / AMD Ryzen 7 7800X3D",
            memory = "16 GB RAM",
            graphics = "NVIDIA GeForce RTX 3060 / AMD RX 6700 XT",
            storage = "70 GB SSD"
        ),
        isFavorite = false,
        isOwned = true,
        isInstalled = false,
        hoursPlayed = 72
    ),
    Game(
        id = 3,
        title = "Hades II",
        genre = "Roguelike Action",
        rating = 4.9,
        reviewsCount = "78.500 ulasan (Luar Biasa Positif)",
        platform = "PC / Steam Deck",
        price = 245000L,
        discountPercent = 20,
        icon = Icons.Default.FlashOn,
        gradientColors = listOf(Color(0xFF0077B6), Color(0xFF03045E)),
        description = "Berperang melintasi dunia bawah tanah Yunani kuno dan hadapi Kronos, Titan Waktu, dalam sekuel roguelike dungeon crawler yang memikat dengan seni tangan indah dan musik orisinil berenergi tinggi.",
        publisher = "Supergiant Games",
        releaseDate = "6 Mei 2024",
        tags = listOf("Roguelike", "Mitologi", "Aksi Cepat", "Hack and Slash"),
        systemReq = SystemRequirements(
            processor = "Dual Core 2.4 GHz",
            memory = "8 GB RAM",
            graphics = "GeForce GTX 950 / Radeon HD 7870",
            storage = "10 GB"
        ),
        isFavorite = true,
        isOwned = false,
        isInstalled = false
    ),
    Game(
        id = 4,
        title = "Black Myth: Wukong",
        genre = "Action RPG",
        rating = 4.8,
        reviewsCount = "390.000 ulasan (Sangat Positif)",
        platform = "PC",
        price = 699000L,
        discountPercent = 0,
        icon = Icons.Default.Shield,
        gradientColors = listOf(Color(0xFFB07D2B), Color(0xFF261C14)),
        description = "Kisah aksi RPG yang terinspirasi dari sastra klasik 'Perjalanan ke Barat'. Sebagai Takdir Yang Terpilih, kuasai berbagai jurus tongkat, sihir transformasi, dan hadapi monster mitologi raksasa.",
        publisher = "Game Science",
        releaseDate = "20 Agustus 2024",
        tags = listOf("Mitologi", "Aksi", "Souls-like", "Grafis Menakjubkan"),
        systemReq = SystemRequirements(
            processor = "Intel Core i7-9700 / AMD Ryzen 5 5500",
            memory = "16 GB RAM",
            graphics = "NVIDIA GeForce RTX 2060 / AMD RX 5700 XT",
            storage = "130 GB SSD"
        ),
        isFavorite = true,
        isOwned = false,
        isInstalled = false
    ),
    Game(
        id = 5,
        title = "Counter-Strike 2",
        genre = "Tactical Shooter",
        rating = 4.6,
        reviewsCount = "1.650.000 ulasan (Sangat Positif)",
        platform = "PC",
        price = 0L,
        discountPercent = 0,
        icon = Icons.Default.GpsFixed,
        gradientColors = listOf(Color(0xFFEA580C), Color(0xFF0F172A)),
        description = "Lompatan teknis terbesar dalam sejarah Counter-Strike. Menghadirkan engine Source 2 mutakhir, sistem partikel asap volumetrik responsif, dan server sub-tick rate yang presisi untuk aksi esport paling kompetitif di dunia.",
        publisher = "Valve",
        releaseDate = "27 September 2023",
        tags = listOf("FPS", "Multiplayer", "Kompetitif", "Shooter"),
        systemReq = SystemRequirements(
            processor = "4 hardware CPU threads - Intel Core i5 750",
            memory = "8 GB RAM",
            graphics = "Kartu grafis 1 GB DirectX 11",
            storage = "85 GB"
        ),
        isFavorite = false,
        isOwned = true,
        isInstalled = true,
        hoursPlayed = 340
    ),
    Game(
        id = 6,
        title = "Stardew Valley",
        genre = "Farming Sim",
        rating = 4.9,
        reviewsCount = "600.000 ulasan (Luar Biasa Positif)",
        platform = "PC / Steam Deck / Mobile",
        price = 115000L,
        discountPercent = 35,
        icon = Icons.Default.Spa,
        gradientColors = listOf(Color(0xFF16A34A), Color(0xFF14532D)),
        description = "Warisi sebidang tanah pertanian kakekmu di Stardew Valley. Berbekal alat bekas dan segenggam koin, mulailah hidup baru, beternak, menanam sayuran musiman, dan bangun komunitas yang hangat.",
        publisher = "ConcernedApe",
        releaseDate = "26 Februari 2016",
        tags = listOf("Santai", "Farming", "Co-op", "Pixel Art"),
        systemReq = SystemRequirements(
            processor = "2.0 GHz",
            memory = "2 GB RAM",
            graphics = "256 MB video memory",
            storage = "500 MB"
        ),
        isFavorite = false,
        isOwned = false,
        isInstalled = false
    ),
    Game(
        id = 7,
        title = "Forza Horizon 5",
        genre = "Racing Open World",
        rating = 4.7,
        reviewsCount = "165.000 ulasan (Sangat Positif)",
        platform = "PC",
        price = 799000L,
        discountPercent = 50,
        icon = Icons.Default.DirectionsCar,
        gradientColors = listOf(Color(0xFF0284C7), Color(0xFF075985)),
        description = "Petualangan Horizon terbaik menantimu! Jelajahi lanskap Meksiko yang hidup dan terus berubah dengan kesenangan mengemudi tanpa batas di ratusan mobil terbaik dunia.",
        publisher = "Xbox Game Studios / Playground Games",
        releaseDate = "9 November 2021",
        tags = listOf("Racing", "Open World", "Mobil", "Multiplayer"),
        systemReq = SystemRequirements(
            processor = "Intel i5-8400 / AMD Ryzen 5 1500X",
            memory = "16 GB RAM",
            graphics = "NVidia GTX 1070 / AMD RX 590",
            storage = "110 GB SSD"
        ),
        isFavorite = false,
        isOwned = false,
        isInstalled = false
    ),
    Game(
        id = 8,
        title = "Dota 2",
        genre = "MOBA / Strategy",
        rating = 4.5,
        reviewsCount = "2.200.000 ulasan (Sangat Positif)",
        platform = "PC",
        price = 0L,
        discountPercent = 0,
        icon = Icons.Default.AutoFixHigh,
        gradientColors = listOf(Color(0xFFDC2626), Color(0xFF7F1D1D)),
        description = "Setiap hari, jutaan pemain di seluruh dunia terjun ke medan pertempuran sebagai salah satu dari seratus lebih pahlawan Dota. Pilihan hero tak terbatas dengan strategi kompetitif mendalam.",
        publisher = "Valve",
        releaseDate = "9 Juli 2013",
        tags = listOf("MOBA", "Strategi", "PvP", "Tim"),
        systemReq = SystemRequirements(
            processor = "Dual core from Intel or AMD at 2.8 GHz",
            memory = "4 GB RAM",
            graphics = "NVIDIA GeForce 8600/9600GT",
            storage = "60 GB"
        ),
        isFavorite = false,
        isOwned = false,
        isInstalled = false
    ),
    Game(
        id = 9,
        title = "Dead Cells",
        genre = "Rogue-lite Action",
        rating = 4.8,
        reviewsCount = "140.000 ulasan (Luar Biasa Positif)",
        platform = "PC / Steam Deck",
        price = 140000L,
        discountPercent = 60,
        icon = Icons.Default.SportsMartialArts,
        gradientColors = listOf(Color(0xFF9333EA), Color(0xFF581C87)),
        description = "Eksplorasi kastil misterius yang luas dan selalu bermutasi. Dapatkan berbagai kombo senjata api, busur panah, dan jurus sihir untuk mengalahkan musuh tangguh tanpa ada sistem pos penyelamat (permadeath).",
        publisher = "Motion Twin",
        releaseDate = "6 Agustus 2018",
        tags = listOf("Rogue-lite", "Metroidvania", "Souls-like", "2D"),
        systemReq = SystemRequirements(
            processor = "Intel i5+",
            memory = "4 GB RAM",
            graphics = "Nvidia GTX 460 / Radeon HD 7800",
            storage = "4 GB"
        ),
        isFavorite = false,
        isOwned = false,
        isInstalled = false
    )
)

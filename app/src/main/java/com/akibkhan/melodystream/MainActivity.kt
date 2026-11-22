package com.akibkhan.melodystream

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

private val Night = Color(0xFF090B12)
private val Panel = Color(0xFF171B28)
private val Muted = Color(0xFF9BA3B8)
private val Mint = Color(0xFF63E6BE)
private val Violet = Color(0xFF9B8CFF)
private val prefsName = "melody_stream_prefs"

private data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val artwork: String,
    val previewUrl: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MelodyStreamApp() }
    }
}

@Composable
private fun MelodyStreamApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(prefsName, Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()
    val player = remember { ExoPlayer.Builder(context).build() }

    var query by rememberSaveable { mutableStateOf("") }
    var tracks by remember { mutableStateOf(emptyList<Track>()) }
    var favorites by remember { mutableStateOf(loadFavorites(prefs)) }
    var selected by remember { mutableStateOf<Track?>(null) }
    var playing by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("Search for a song, artist or album.") }
    var selectedTab by rememberSaveable { mutableStateOf("Home") }
    var username by rememberSaveable { mutableStateOf(prefs.getString("username", "") ?: "") }
    var nameDraft by rememberSaveable { mutableStateOf(username) }
    var themeName by rememberSaveable { mutableStateOf(prefs.getString("theme", "Midnight") ?: "Midnight") }
    var showPlayer by remember { mutableStateOf(false) }

    val accent = when (themeName) {
        "Ocean" -> Color(0xFF55C7F2)
        "Sunset" -> Color(0xFFFFA46B)
        "Royal" -> Color(0xFFC6A0FF)
        else -> Mint
    }
    val background = when (themeName) {
        "Ocean" -> Color(0xFF07131D)
        "Sunset" -> Color(0xFF1B100F)
        "Royal" -> Color(0xFF120D1D)
        else -> Night
    }

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    fun playTrack(track: Track, queue: List<Track> = tracks) {
        val list = (if (queue.isNotEmpty()) queue else listOf(track))
        val index = list.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        player.setMediaItems(list.map { MediaItem.fromUri(it.previewUrl) }, index, 0L)
        player.prepare()
        player.play()
        selected = track
        playing = true
        showPlayer = true
    }

    fun toggleFavorite(track: Track) {
        favorites = if (favorites.any { it.id == track.id }) {
            favorites.filterNot { it.id == track.id }
        } else {
            favorites + track
        }
        saveFavorites(prefs, favorites)
    }

    fun searchMusic() {
        if (query.isBlank()) {
            message = "Pehle song ya artist ka naam likho."
            return
        }
        scope.launch {
            loading = true
            message = "Searching songs…"
            try {
                tracks = searchItunes(query.trim())
                message = if (tracks.isEmpty()) "No previews found. Try another search." else "${tracks.size} previews found."
                selectedTab = "Search"
            } catch (_: Exception) {
                message = "Search nahi ho payi. Internet check karke dobara try karo."
            } finally {
                loading = false
            }
        }
    }

    MaterialTheme(colorScheme = darkColorScheme(
        background = background,
        surface = Panel,
        primary = accent,
        secondary = Violet,
        onBackground = Color.White,
        onSurface = Color.White
    )) {
        Scaffold(
            containerColor = background,
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF10131D)) {
                    val tabs = listOf("Home", "Search", "Library", "Profile")
                    val icons = listOf(Icons.Default.Home, Icons.Default.Search, Icons.Default.LibraryMusic, Icons.Default.Person)
                    tabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = { Icon(icons[index], contentDescription = tab) },
                            label = { Text(tab) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = accent,
                                selectedTextColor = accent,
                                indicatorColor = accent.copy(alpha = 0.15f),
                                unselectedIconColor = Muted,
                                unselectedTextColor = Muted
                            )
                        )
                    }
                }
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Made in India • By Akib Khan", color = Muted, fontSize = 11.sp)
                                Text("Melody Stream", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(Modifier.size(46.dp).clip(CircleShape).background(Brush.linearGradient(listOf(accent, Violet))), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.MusicNote, contentDescription = "Melody Stream", tint = Night, modifier = Modifier.size(27.dp))
                            }
                        }
                    }

                    when (selectedTab) {
                        "Home" -> {
                            item {
                                Column(
                                    Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                                        .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.27f), Violet.copy(alpha = 0.24f))))
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(9.dp)
                                ) {
                                    Text("YOUR MUSIC. YOUR MOOD.", color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
                                    Text(if (username.isBlank()) "Welcome to your sound." else "Good to see you, $username", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                    Text("Discover tracks, save favourites and make your own music space.", color = Color(0xFFD0D5E2), fontSize = 13.sp)
                                    Button(onClick = { selectedTab = "Search" }, colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Night)) {
                                        Icon(Icons.Default.Search, contentDescription = null)
                                        Spacer(Modifier.width(6.dp))
                                        Text("Explore music")
                                    }
                                }
                            }
                            item { SectionTitle("Quick access", accent) }
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    QuickTile("Favourites", Icons.Default.Favorite, Modifier.weight(1f), accent) { selectedTab = "Library" }
                                    QuickTile("Search songs", Icons.Default.Search, Modifier.weight(1f), accent) { selectedTab = "Search" }
                                }
                            }
                            if (tracks.isNotEmpty()) {
                                item { SectionTitle("Recently discovered", accent) }
                                items(tracks.take(5), key = { it.id }) { track ->
                                    TrackRow(track, favorites.any { it.id == track.id }, accent,
                                        onPlay = { playTrack(track, tracks) },
                                        onFavorite = { toggleFavorite(track) })
                                }
                            }
                        }

                        "Search" -> {
                            item { SectionTitle("Find your music", accent) }
                            item {
                                OutlinedTextField(
                                    value = query,
                                    onValueChange = { query = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    placeholder = { Text("Song, artist or album", color = Muted) },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Muted) },
                                    trailingIcon = {
                                        IconButton(onClick = { searchMusic() }, enabled = !loading) {
                                            Icon(Icons.Default.ArrowForward, contentDescription = "Search", tint = accent)
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onSearch = { searchMusic() }),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = accent,
                                        unfocusedBorderColor = Color(0xFF42485A),
                                        cursorColor = accent
                                    )
                                )
                                if (loading) LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 10.dp), color = accent)
                                else Text(message, color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                            }
                            if (tracks.isNotEmpty()) {
                                item { SectionTitle("Search results", accent) }
                                items(tracks, key = { it.id }) { track ->
                                    TrackRow(track, favorites.any { it.id == track.id }, accent,
                                        onPlay = { playTrack(track, tracks) },
                                        onFavorite = { toggleFavorite(track) })
                                }
                            }
                            item { Text("Music catalogue previews are supplied by the iTunes Search API. Preview availability and playback rights are controlled by the provider.", color = Muted, fontSize = 11.sp) }
                        }

                        "Library" -> {
                            item { SectionTitle("Your library", accent) }
                            item {
                                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Panel).padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Favorite, tint = accent, contentDescription = null)
                                        Spacer(Modifier.width(10.dp))
                                        Text("Favourite songs", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                    }
                                    Text("${favorites.size} saved tracks", color = Muted, fontSize = 12.sp)
                                }
                            }
                            if (favorites.isEmpty()) {
                                item { EmptyState("No favourites yet", "Tap the heart on any song to save it here.", accent) }
                            } else {
                                items(favorites, key = { it.id }) { track ->
                                    TrackRow(track, true, accent,
                                        onPlay = { playTrack(track, favorites) },
                                        onFavorite = { toggleFavorite(track) })
                                }
                            }
                        }

                        else -> {
                            item { SectionTitle("Your profile", accent) }
                            item {
                                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Panel).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(Modifier.size(64.dp).clip(CircleShape).background(accent.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = accent, modifier = Modifier.size(34.dp))
                                    }
                                    Text("Display name", color = Muted, fontSize = 12.sp)
                                    OutlinedTextField(
                                        value = nameDraft,
                                        onValueChange = { nameDraft = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        placeholder = { Text("Enter your name") }
                                    )
                                    Button(onClick = {
                                        username = nameDraft.trim()
                                        prefs.edit().putString("username", username).apply()
                                    }, colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Night)) {
                                        Icon(Icons.Default.Save, contentDescription = null)
                                        Spacer(Modifier.width(6.dp))
                                        Text("Save profile")
                                    }
                                }
                            }
                            item { SectionTitle("Choose your theme", accent) }
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("Midnight", "Ocean", "Sunset", "Royal").forEach { theme ->
                                        Row(
                                            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                                                .background(if (themeName == theme) accent.copy(alpha = 0.15f) else Panel)
                                                .clickable {
                                                    themeName = theme
                                                    prefs.edit().putString("theme", theme).apply()
                                                }.padding(15.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(Modifier.size(20.dp).clip(CircleShape).background(
                                                when (theme) {
                                                    "Ocean" -> Color(0xFF55C7F2)
                                                    "Sunset" -> Color(0xFFFFA46B)
                                                    "Royal" -> Color(0xFFC6A0FF)
                                                    else -> Mint
                                                }
                                            ))
                                            Spacer(Modifier.width(12.dp))
                                            Text(theme, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                                            if (themeName == theme) Icon(Icons.Default.CheckCircle, tint = accent, contentDescription = "Selected")
                                        }
                                    }
                                }
                            }
                            item { Text("Theme and profile choices are saved on this device.", color = Muted, fontSize = 11.sp) }
                        }
                    }
                }

                selected?.let { track ->
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(17.dp)).background(Panel).clickable { showPlayer = !showPlayer }.padding(11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(42.dp).clip(RoundedCornerShape(11.dp)).background(Brush.linearGradient(listOf(accent, Violet))), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = Night)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(track.title, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            Text(track.artist, color = Muted, fontSize = 11.sp, maxLines = 1)
                        }
                        IconButton(onClick = {
                            if (playing) player.pause() else player.play()
                            playing = !playing
                        }) {
                            Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = "Play or pause", tint = accent)
                        }
                        IconButton(onClick = { player.seekToNextMediaItem() }) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Next preview", tint = accent)
                        }
                    }
                    if (showPlayer) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Preview player", color = accent, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            TextButton(onClick = { showPlayer = false }) { Text("Close", color = Muted) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, accent: Color) {
    Text(title, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun QuickTile(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, accent: Color, onClick: () -> Unit) {
    Column(modifier.clip(RoundedCornerShape(17.dp)).background(Panel).clickable(onClick = onClick).padding(15.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(25.dp))
        Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun TrackRow(track: Track, favorite: Boolean, accent: Color, onPlay: () -> Unit, onFavorite: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Panel).clickable(onClick = onPlay).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(listOf(Violet, accent))), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.MusicNote, contentDescription = null, tint = Night, modifier = Modifier.size(25.dp))
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text(track.title, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(track.artist, color = Muted, fontSize = 12.sp, maxLines = 1)
            Text("Preview", color = accent, fontSize = 10.sp)
        }
        IconButton(onClick = onFavorite) {
            Icon(if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "Favourite", tint = if (favorite) Color(0xFFFF6B91) else Muted)
        }
        IconButton(onClick = onPlay) {
            Icon(Icons.Default.PlayCircle, contentDescription = "Play preview", tint = accent, modifier = Modifier.size(29.dp))
        }
    }
}

@Composable
private fun EmptyState(title: String, subtitle: String, accent: Color) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Panel).padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.LibraryMusic, contentDescription = null, tint = accent, modifier = Modifier.size(34.dp))
        Text(title, color = Color.White, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Muted, fontSize = 12.sp)
    }
}

private fun saveFavorites(context: Context, tracks: List<Track>) {
    val array = JSONArray()
    tracks.forEach { track ->
        array.put(JSONObject().put("id", track.id).put("title", track.title).put("artist", track.artist).put("artwork", track.artwork).put("previewUrl", track.previewUrl))
    }
    context.getSharedPreferences(prefsName, Context.MODE_PRIVATE).edit().putString("favorites", array.toString()).apply()
}

private fun loadFavorites(context: Context): List<Track> {
    return try {
        val raw = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE).getString("favorites", "[]") ?: "[]"
        val array = JSONArray(raw)
        buildList {
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                add(Track(item.optString("id"), item.optString("title"), item.optString("artist"), item.optString("artwork"), item.optString("previewUrl")))
            }
        }
    } catch (_: Exception) {
        emptyList()
    }
}

private suspend fun searchItunes(term: String): List<Track> = withContext(Dispatchers.IO) {
    val encoded = URLEncoder.encode(term, "UTF-8")
    val connection = (URL("https://itunes.apple.com/search?term=$encoded&entity=song&limit=25").openConnection() as HttpURLConnection)
    try {
        connection.connectTimeout = 12000
        connection.readTimeout = 12000
        connection.requestMethod = "GET"
        val body = connection.inputStream.bufferedReader().use { it.readText() }
        val results = JSONObject(body).getJSONArray("results")
        buildList {
            for (i in 0 until results.length()) {
                val item = results.getJSONObject(i)
                val preview = item.optString("previewUrl")
                if (preview.isNotBlank() && preview != "null") {
                    add(Track(
                        id = item.optString("trackId", "${item.optString("trackName")}_$i"),
                        title = item.optString("trackName", "Unknown track"),
                        artist = item.optString("artistName", "Unknown artist"),
                        artwork = item.optString("artworkUrl100", ""),
                        previewUrl = preview
                    ))
                }
            }
        }
    } finally {
        connection.disconnect()
    }
}

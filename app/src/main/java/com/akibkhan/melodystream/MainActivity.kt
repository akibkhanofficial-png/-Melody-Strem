package com.akibkhan.melodystream

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
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

private val Night = Color(0xFF090B12)
private val Panel = Color(0xFF171B28)
private val Muted = Color(0xFF9BA3B8)
private val Mint = Color(0xFF63E6BE)
private val Violet = Color(0xFF9B8CFF)

private data class Track(
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
    val scope = rememberCoroutineScope()
    val player = remember { ExoPlayer.Builder(context).build() }
    var query by remember { mutableStateOf("") }
    var tracks by remember { mutableStateOf(emptyList<Track>()) }
    var selected by remember { mutableStateOf<Track?>(null) }
    var playing by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("Search a song, artist or album to try a music preview.") }
    var selectedTab by remember { mutableStateOf("Home") }

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    fun searchMusic() {
        if (query.isBlank()) {
            message = "Type a song or artist name first."
            return
        }
        scope.launch {
            loading = true
            message = "Searching music…"
            try {
                tracks = searchItunes(query.trim())
                message = if (tracks.isEmpty()) "No preview found. Try another search." else "Showing available song previews."
            } catch (e: Exception) {
                message = "Search failed. Check your internet and try again."
            } finally {
                loading = false
            }
        }
    }

    MaterialTheme(colorScheme = darkColorScheme(background = Night, surface = Panel, primary = Mint, secondary = Violet)) {
        Surface(modifier = Modifier.fillMaxSize(), color = Night) {
            Column(Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Made in India • By Akib Khan", color = Muted, fontSize = 11.sp)
                                Text("Melody Stream", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(Modifier.size(46.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Mint, Violet))), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.MusicNote, contentDescription = "Melody Stream", tint = Night)
                            }
                        }
                    }
                    item {
                        Column(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
                                .background(Brush.linearGradient(listOf(Color(0xFF263D4A), Color(0xFF332B55))))
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(9.dp)
                        ) {
                            Text("YOUR MUSIC, YOUR MOOD", color = Mint, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp)
                            Text("Find your next favourite.", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                            Text("Search songs and play available previews.", color = Color(0xFFD0D5E2), fontSize = 13.sp)
                        }
                    }
                    item {
                        Text("Search music", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("Song, artist or album", color = Muted) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Muted) },
                            trailingIcon = {
                                IconButton(onClick = { searchMusic() }, enabled = !loading) {
                                    Icon(Icons.Default.ArrowForward, contentDescription = "Search", tint = Mint)
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { searchMusic() }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Mint,
                                unfocusedBorderColor = Color(0xFF42485A),
                                cursorColor = Mint
                            )
                        )
                        if (loading) {
                            LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 10.dp), color = Mint)
                        } else {
                            Text(message, color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                    if (tracks.isNotEmpty()) {
                        item { Text("Song previews", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold) }
                        items(tracks) { track ->
                            Row(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Panel)
                                    .clickable {
                                        selected = track
                                        player.setMediaItem(MediaItem.fromUri(track.previewUrl))
                                        player.prepare()
                                        player.play()
                                        playing = true
                                    }.padding(13.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(listOf(Violet, Mint))), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = Night)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(track.title, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                    Text(track.artist, color = Muted, fontSize = 12.sp, maxLines = 1)
                                }
                                Icon(Icons.Default.PlayCircle, contentDescription = "Play preview", tint = Mint, modifier = Modifier.size(30.dp))
                            }
                        }
                    }
                    item {
                        Text("Note: This first playback build uses short catalogue previews supplied by the iTunes Search API. It is not yet a full-length streaming service.", color = Muted, fontSize = 11.sp)
                    }
                }

                selected?.let { track ->
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(16.dp)).background(Panel).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Mint, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(track.title, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            Text(track.artist, color = Muted, fontSize = 11.sp, maxLines = 1)
                        }
                        IconButton(onClick = {
                            if (playing) player.pause() else player.play()
                            playing = !playing
                        }) {
                            Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = "Play or pause", tint = Mint)
                        }
                    }
                }

                NavigationBar(containerColor = Color(0xFF10131D)) {
                    listOf("Home", "Search", "Library", "Profile").forEach { tab ->
                        val icon = when (tab) {
                            "Home" -> Icons.Default.Home
                            "Search" -> Icons.Default.Search
                            "Library" -> Icons.Default.LibraryMusic
                            else -> Icons.Default.Person
                        }
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = { Icon(icon, contentDescription = tab) },
                            label = { Text(tab) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Mint,
                                selectedTextColor = Mint,
                                indicatorColor = Color(0xFF243B3A),
                                unselectedIconColor = Muted,
                                unselectedTextColor = Muted
                            )
                        )
                    }
                }
            }
        }
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
        val json = JSONObject(body)
        val results = json.getJSONArray("results")
        buildList {
            for (i in 0 until results.length()) {
                val item = results.getJSONObject(i)
                val preview = item.optString("previewUrl")
                if (preview.isNotBlank() && preview != "null") {
                    add(
                        Track(
                            title = item.optString("trackName", "Unknown track"),
                            artist = item.optString("artistName", "Unknown artist"),
                            artwork = item.optString("artworkUrl100", ""),
                            previewUrl = preview
                        )
                    )
                }
            }
        }
    } finally {
        connection.disconnect()
    }
}

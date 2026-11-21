package com.akibkhan.melodystream

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Night = Color(0xFF090B12)
private val Panel = Color(0xFF171B28)
private val Muted = Color(0xFF9BA3B8)
private val Mint = Color(0xFF63E6BE)
private val Violet = Color(0xFF9B8CFF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MelodyStreamApp() }
    }
}

@Composable
private fun MelodyStreamApp() {
    var selectedTab by remember { mutableStateOf("Home") }
    var isPlaying by remember { mutableStateOf(false) }
    var liked by remember { mutableStateOf(false) }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Night,
            surface = Panel,
            primary = Mint,
            secondary = Violet
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Night) {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Good evening", color = Muted, fontSize = 14.sp)
                                Text("Melody Stream", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier.size(46.dp).clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(Mint, Violet))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = "Profile", tint = Night)
                            }
                        }
                    }
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                                .background(Brush.linearGradient(listOf(Color(0xFF263D4A), Color(0xFF332B55))))
                                .padding(20.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("YOUR MUSIC, YOUR MOOD", color = Mint, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                                Text("Find your
next favourite.", color = Color.White, fontSize = 29.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold)
                                Text("A fresh space for the music you love.", color = Color(0xFFD0D5E2), fontSize = 13.sp)
                                Row(
                                    modifier = Modifier.clip(CircleShape).background(Mint).clickable { selectedTab = "Search" }.padding(horizontal = 18.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Explore music", color = Night, fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.width(6.dp))
                                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Night, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                    item {
                        SectionHeading("Made for you", "See all")
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MusicTile("Daily Mix", "A mix for every day", Violet, Modifier.weight(1f))
                            MusicTile("Chill Vibes", "Slow things down", Color(0xFF286B67), Modifier.weight(1f))
                        }
                    }
                    item {
                        SectionHeading("Start listening", "Discover")
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                                .background(Panel).padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(Brush.linearGradient(listOf(Violet, Mint))), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.MusicNote, contentDescription = null, tint = Night, modifier = Modifier.size(28.dp))
                            }
                            Spacer(Modifier.width(13.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Your library starts here", color = Color.White, fontWeight = FontWeight.SemiBold)
                                Text("Search for music to get started", color = Muted, fontSize = 12.sp)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Muted)
                        }
                    }
                    item {
                        Text("Made in India • By Akib Khan", color = Muted, fontSize = 11.sp, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                    }
                }
                if (isPlaying) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(16.dp)).background(Panel).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = Mint)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("No track selected", color = Color.White, fontWeight = FontWeight.Medium)
                            Text("Choose music to start playback", color = Muted, fontSize = 11.sp)
                        }
                        IconButton(onClick = { isPlaying = false }) { Icon(Icons.Default.Pause, contentDescription = "Pause", tint = Color.White) }
                    }
                }
                NavigationBar(containerColor = Color(0xFF10131D), contentColor = Mint) {
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

@Composable
private fun SectionHeading(title: String, action: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(action, color = Mint, fontSize = 12.sp)
    }
}

@Composable
private fun MusicTile(title: String, subtitle: String, accent: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(18.dp)).background(Panel).padding(13.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Box(Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(13.dp)).background(Brush.linearGradient(listOf(accent, Color(0xFF252A3B)))), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Color.White, modifier = Modifier.size(42.dp))
        }
        Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
        Text(subtitle, color = Muted, fontSize = 11.sp)
    }
}

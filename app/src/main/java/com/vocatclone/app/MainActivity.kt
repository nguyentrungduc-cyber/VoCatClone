package com.vocatclone.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.auth.FirebaseAuth
import com.vocatclone.app.ui.components.MainTab
import com.vocatclone.app.ui.components.VoCatBottomBar
import com.vocatclone.app.ui.home.HomeScreen
import com.vocatclone.app.ui.profile.ProfileScreen
import com.vocatclone.app.ui.theme.VoCatCloneTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.d("FirebaseTest", "currentUser = ${FirebaseAuth.getInstance().currentUser}")
        setContent {
            VoCatCloneTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    var selectedTab by remember { mutableStateOf(MainTab.VOCAB) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            VoCatBottomBar(selected = selectedTab, onSelect = { selectedTab = it })
        }
    ) { innerPadding ->
        when (selectedTab) {
            MainTab.VOCAB -> HomeScreen(modifier = Modifier.padding(innerPadding))
            MainTab.PROFILE -> ProfileScreen(
                userEmail = FirebaseAuth.getInstance().currentUser?.email,
                modifier = Modifier.padding(innerPadding)
            )
            else -> {
                // Các tab chưa triển khai — placeholder
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tính năng đang phát triển")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainAppPreview() {
    VoCatCloneTheme {
        MainApp()
    }
}

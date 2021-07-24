package com.stelmashchuk.radiot.presentation.tabs

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.stelmashchuk.radiot.presentation.episodes.PodcastsTab
import com.stelmashchuk.radiot.presentation.themes.ThemesTab

@Composable
fun StartScreen() {
  val navController = rememberNavController()
  val items = Tabs.values()

  Scaffold(
      bottomBar = {
        BottomNavigation {
          val navBackStackEntry by navController.currentBackStackEntryAsState()
          val currentRoute = navBackStackEntry?.destination?.route
          items.forEach { tab ->
            BottomNavigationItem(selected = currentRoute == tab.name, onClick = {
              navController.navigate(tab.name) {
                launchSingleTop = true
              }
            }, icon = {
              Icon(painter = painterResource(id = tab.icon), contentDescription = stringResource(id = tab.label))
            }, label = {
              Text(text = stringResource(id = tab.label))
            })
          }
        }
      }
  ) {
    NavHost(
        navController,
        startDestination = Tabs.PODCASTS.name
    ) {
      composable(Tabs.PODCASTS.name) { PodcastsTab() }
      composable(Tabs.PRE_SHOW.name) { ThemesTab() }
    }
  }
}

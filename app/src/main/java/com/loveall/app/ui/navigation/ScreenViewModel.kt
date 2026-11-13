package com.loveall.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

@Composable
internal inline fun <reified T : ViewModel> screenViewModel(crossinline create: () -> T): T =
    viewModel(factory = viewModelFactory { initializer { create() } })

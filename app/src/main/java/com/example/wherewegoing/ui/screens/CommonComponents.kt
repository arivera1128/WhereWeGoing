package com.example.wherewegoing.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wherewegoing.*
import com.example.wherewegoing.R
import com.example.wherewegoing.model.MealRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
@Composable
fun ChickLogo() {
    Image(
        painter = painterResource(R.drawable.walking_chick_logo),
        contentDescription = "Walking chick logo",
        modifier = Modifier.size(42.dp)
    )
}

@Composable
fun PageColumn(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content
    )
}



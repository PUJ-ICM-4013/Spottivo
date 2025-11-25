package com.example.spottivo.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.spottivo.R

/**
 * Reusable App Logo composable using the PNG placed at drawable/spottivo_logo.png
 * Place the provided PNG as: app/src/main/res/drawable/spottivo_logo.png
 */
@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    sizeDp: Int = 120,
    contentDescription: String = "Spottivo Logo"
) {
    Image(
        painter = painterResource(id = R.drawable.spottivo_logo),
        contentDescription = contentDescription,
        modifier = modifier.size(sizeDp.dp),
        alignment = Alignment.Center
    )
}

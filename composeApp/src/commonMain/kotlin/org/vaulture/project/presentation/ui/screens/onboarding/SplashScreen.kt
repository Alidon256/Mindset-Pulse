package org.vaulture.project.presentation.ui.screens.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.vaulture.project.presentation.theme.AppTheme
import vaulture.composeapp.generated.resources.Res
import vaulture.composeapp.generated.resources.mindset_pulse_nobg_logo

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val scale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Animate the logo scaling up
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
        // Wait for a moment to let the user see the logo
        delay(1200L)
        // Trigger navigation
        onTimeout()
    }
    _root_ide_package_.org.vaulture.project.presentation.theme.AppTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {

                Icon(
                    painter = painterResource(Res.drawable.mindset_pulse_nobg_logo),
                    contentDescription = "Mindset Pulse Logo",
                    modifier = Modifier
                        .size(200.dp)
                        .scale(scale.value)
                    //tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

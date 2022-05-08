package com.lodestone.app.lodestones.frontend

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import com.lodestone.app.R
import com.lodestone.app.compose.LocalSharedViewModel
import com.lodestone.app.compose.LodestoneTheme
import com.lodestone.app.main.SharedState
import kotlin.math.absoluteValue

@Composable
fun LodestoneScreen() {

    val model: LodestoneViewModel = hiltViewModel()
    val state by model.state.collectAsState()

    val sharedModel = LocalSharedViewModel.current
    val sharedState by sharedModel.state.collectAsState()

    LodestoneTheme {
        Surface(color = MaterialTheme.colors.background) {
            LodestoneUI(
                state = state,
                onStateChange = { model.state.value = it },
                sharedState = sharedState,
                onSharedStateChange = { sharedModel.state.value = it },
                performAction = model.action::tryEmit
            )
        }
    }
    ActionHandlers(model)
}

@Composable
private fun ActionHandlers(model: LodestoneViewModel) {

}

@Composable
private fun LodestoneUI(
    state: LodestoneState,
    onStateChange: (LodestoneState) -> Unit,
    sharedState: SharedState,
    onSharedStateChange: (SharedState) -> Unit,
    performAction: (LodestoneAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier
        .background(Color(0xFF0D1920))
        .fillMaxSize()
    ) {
        Text(
            text = sharedState.destination.name,
            fontSize = 40.sp,
            color = Color.White,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp, bottom = 12.dp, start = 12.dp, end = 12.dp)
        )
        Compass(sharedState, Modifier.padding(horizontal = 8.dp))
    }
}

@Composable
fun Compass(sharedState: SharedState, modifier: Modifier = Modifier) {
    ConstraintLayout(modifier
        .fillMaxWidth()
        .aspectRatio(1f)
    ) {
        val (degreeValue, degreeSymbol) = createRefs()
        Image(
            painterResource(R.drawable.dial),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxSize()
                .rotate(sharedState.directions.polesDirection.unaryMinus())
        )
        Box(Modifier
            .padding(64.dp)
            .fillMaxSize()
            .rotate(sharedState.directions.destinationDirection.unaryMinus())
        ) {
            Image(
                painterResource(R.drawable.arrow),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .width(12.dp)
                    .align(Alignment.TopCenter)
            )
        }
        Text(
            text = sharedState.directions.destinationDirection.toInt().absoluteValue.toString(),
            fontSize = 40.sp,
            color = Color.White,
            modifier = Modifier.constrainAs(degreeValue) {
                centerTo(parent)
            }
        )
        Text(
            text = "°",
            fontSize = 40.sp,
            color = Color.White,
            modifier = Modifier.constrainAs(degreeSymbol) {
                start.linkTo(degreeValue.end)
                top.linkTo(degreeValue.top)
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    LodestoneTheme {
        Surface(color = MaterialTheme.colors.background) {
            LodestoneScreen()
        }
    }
}

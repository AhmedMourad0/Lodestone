package com.lodestone.app.lodestones.frontend

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.lodestone.app.R
import com.lodestone.app.compose.*
import com.lodestone.app.lodestones.models.AlQiblaLodestone
import com.lodestone.app.lodestones.models.Coordinates
import com.lodestone.app.lodestones.models.Lodestone
import com.lodestone.app.main.SharedState
import com.lodestone.app.main.SharedViewModel
import com.lodestone.app.utils.collectAsEffect
import com.lodestone.app.utils.toast
import com.sucho.placepicker.AddressData
import com.sucho.placepicker.Constants
import com.sucho.placepicker.PlacePicker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun LodestoneScreen() {

    val model: LodestoneViewModel = hiltViewModel()
    val state by model.state.collectAsState()

    val sharedModel = LocalSharedViewModel.current
    val sharedState by sharedModel.state.collectAsState()

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
    )

    LodestoneTheme {
        Surface(color = MaterialTheme.colors.background) {
            LodestoneUI(
                lodestonesPager = model.lodestonesPager,
                scaffoldState,
                state = state,
                onStateChange = { model.state.value = it },
                sharedState = sharedState,
                onSharedStateChange = { sharedModel.state.value = it },
                performAction = model.action::tryEmit
            )
        }
    }

    ActionHandlers(sharedModel, model, scaffoldState)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ActionHandlers(
    sharedModel: SharedViewModel,
    model: LodestoneViewModel,
    scaffoldState: BottomSheetScaffoldState
) {
    val context = LocalContext.current
    model.action.collectAsEffect { action ->
        model.state.value = model.state.value.copy(isInputBlocked = true)
        when (action) {
            is LodestoneAction.CreateLodestone -> {
                val lodestone = model.onCreateLodestone(action.state)
                if (lodestone != null) {
                    sharedModel.state.value = sharedModel.state.value.copy(destination = lodestone)
                    scaffoldState.bottomSheetState.collapse()
                    model.state.value = model.state.value.copy(
                        bottomSheetState = LodestoneSelector,
                        isInputBlocked = false
                    )
                } else {
                    model.state.value = model.state.value.copy(isInputBlocked = false)
                }
            }
        }
    }
    model.errorMessages.collectAsEffect { msg ->
        context.toast(msg)
    }
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
private fun LodestoneUI(
    lodestonesPager: Flow<PagingData<Lodestone<Lodestone.Retrieved>>>,
    scaffoldState: BottomSheetScaffoldState,
    state: LodestoneState,
    onStateChange: (LodestoneState) -> Unit,
    sharedState: SharedState,
    onSharedStateChange: (SharedState) -> Unit,
    performAction: (LodestoneAction) -> Unit,
    modifier: Modifier = Modifier
) {
    BottomSheetScaffold(
        sheetShape = RoundedCornerShape(topEnd = 40.dp, topStart = 40.dp),
        sheetBackgroundColor= ColorPrimary.copy(alpha = 0.8f),
        backgroundColor= ColorPrimary,
        scaffoldState = scaffoldState,
        sheetContent = {
            BottomSheetHandle(modifier = Modifier
                .background(ColorPrimary)
                .fillMaxWidth()
                .height(BottomSheetScaffoldDefaults.SheetPeekHeight)
            )
            when (val s = state.bottomSheetState) {
                LodestoneSelector -> LodestoneSelector(
                    lodestonesPager,
                    state,
                    onStateChange,
                    sharedState,
                    onSharedStateChange
                )
                is LodestoneCreator -> LodestoneCreator(
                    state = state,
                    lodestoneCreatorState = s,
                    onLodestoneCreatorState = { onStateChange(state.copy(bottomSheetState = it)) },
                    performAction = performAction
                )
            }
        }, content = { Content(sharedState, Modifier.padding(it)) },
        modifier = modifier
    )
    val coroutineScope = rememberCoroutineScope()
    BackHandler(enabled = scaffoldState.bottomSheetState.isExpanded) {
        when (state.bottomSheetState) {
            is LodestoneCreator -> onStateChange(state.copy(bottomSheetState = LodestoneSelector))
            LodestoneSelector -> {
                coroutineScope.launch {
                    scaffoldState.bottomSheetState.collapse()
                }
            }
        }
    }
}

@Composable
private fun Content(
    sharedState: SharedState,
    modifier: Modifier = Modifier
) {
    Column(modifier
        .background(ColorPrimary)
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = sharedState.destination.name,
            fontSize = 40.sp,
            color = Color.White,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp, bottom = 12.dp, start = 12.dp, end = 12.dp)
        )
        Compass(sharedState, Modifier.padding(horizontal = 8.dp))
    }
}

@Composable
private fun Compass(sharedState: SharedState, modifier: Modifier = Modifier) {
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
                .rotate(sharedState.directions?.polesDirection?.unaryMinus() ?: 0f)
        )
        Box(Modifier
            .padding(64.dp)
            .fillMaxSize()
            .rotate(sharedState.directions?.destinationDirection?.unaryMinus() ?: 0f)
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
            text = sharedState.directions?.destinationDirection?.toInt()?.absoluteValue?.toString() ?: "0",
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

@Composable
private fun BottomSheetHandle(modifier: Modifier = Modifier){
    Box(modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(R.drawable.ic_drag_handle),
            contentDescription = null,
            modifier= Modifier
                .size(40.dp)
                .align(Alignment.Center)
        )
    }
}

@Composable
private fun LodestoneSelector(
    lodestonesPager: Flow<PagingData<Lodestone<Lodestone.Retrieved>>>,
    state: LodestoneState,
    onStateChange: (LodestoneState) -> Unit,
    sharedState: SharedState,
    onSharedStateChange: (SharedState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth()) {
        val lodestones = lodestonesPager.collectAsLazyPagingItems()
        val configuration = LocalConfiguration.current
        LazyColumn(
            horizontalAlignment = CenterHorizontally,
            contentPadding = PaddingValues(vertical = 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = configuration.screenHeightDp.dp - 150.dp)
                .drawWithCache {
                    val topGradient = Brush.verticalGradient(
                        colors = listOf(ColorPrimary, Color.Transparent),
                        endY = size.height / 8
                    )
                    val bottomGradient = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, ColorPrimary),
                        startY = size.height - size.height / 8
                    )
                    onDrawWithContent {
                        drawContent()
                        drawRect(topGradient)
                        drawRect(bottomGradient)
                    }
                }
        ) {
            items(1) {
                LodestoneItem(
                    state = state,
                    lodestone = AlQiblaLodestone,
                    sharedState = sharedState,
                    onSharedStateChange = onSharedStateChange
                )
            }
            items(lodestones) { lodestone ->
                lodestone ?: return@items
                LodestoneItem(
                    state = state,
                    lodestone = lodestone,
                    sharedState = sharedState,
                    onSharedStateChange = onSharedStateChange
                )
            }
        }
        AddLocationButton(
            state = state,
            onStateChange = onStateChange,
            sharedState = sharedState,
            modifier = Modifier
                .background(ColorPrimary)
                .fillMaxWidth()
        )
    }
}

@Composable
fun LodestoneItem(
    state: LodestoneState,
    lodestone: Lodestone<Lodestone.Retrieved>,
    sharedState: SharedState,
    onSharedStateChange: (SharedState) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 8.dp)
            .border(1.dp, ColorAccent, shape = RoundedCornerShape(10.dp))
            .selectable(
                enabled = !state.isInputBlocked,
                selected = sharedState.destination == lodestone,
                onClick = { onSharedStateChange(sharedState.copy(destination = lodestone)) }
            )
            .padding(vertical = 8.dp)
    ) {
        CustomRadioButton(
            selected = sharedState.destination == lodestone,
            colors = RadioButtonDefaults.colors(ColorAccent, ColorAccent),
            onClick = { /* no-op */ },
            dotColor = Color.White,
            stroke = 0.5.dp,
            size = 16.dp,
            dotSize = 8.dp,
            rippleRadius = 20.dp,
            rippleColor = ColorAccent.copy(alpha = 0.5f),
            modifier = Modifier.padding(6.dp)
        )
        Column(Modifier
            .padding(start = 16.dp, end = 8.dp)
            .weight(1f)) {
            Text(
                text = lodestone.name,
                fontSize = 16.sp,
                color= Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = lodestone.mapAddress.orEmpty(),
                fontSize = 12.sp,
                color= Color(0xFFC4C4C4),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AddLocationButton(
    state: LodestoneState,
    onStateChange: (LodestoneState) -> Unit,
    sharedState: SharedState,
    modifier: Modifier = Modifier
){
    val activity = LocalActivity.current

    val startPlacePicker = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val address = result.data?.getParcelableExtra<AddressData>(Constants.ADDRESS_INTENT)
            if (address == null) {
                onStateChange(state.copy(isInputBlocked = false))
                return@rememberLauncherForActivityResult
            }
            onStateChange(state.copy(bottomSheetState = LodestoneCreator(
                name = "",
                coordinates = Coordinates(
                    latitude = address.latitude,
                    longitude = address.longitude
                ), mapAddress = address.addressList
                    ?.mapNotNull { it.getAddressLine(0) }
                    ?.joinToString(", ")
                    .orEmpty()
            ), isInputBlocked = false))
        } else {
            onStateChange(state.copy(isInputBlocked = false))
        }
    }

    val intent = remember(sharedState.directions?.currentLocation) {
        val location = sharedState.directions?.currentLocation ?: return@remember null
        PlacePicker.IntentBuilder()
            .setLatLong(latitude = location.latitude, longitude = location.longitude)
            .showLatLong(true)
            .setMapZoom(12.0f)
            .setMarkerDrawable(R.drawable.marker)
            .setMarkerImageImageColor(R.color.colorAccent)
            .setPrimaryTextColor(android.R.color.white)
            .setSecondaryTextColor(android.R.color.white)
            .setBottomViewColor(R.color.colorPrimary)
            .setMapRawResourceStyle(R.raw.map_style)
            .build(activity)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 16.dp)
    ){
        Button(
            enabled = !state.isInputBlocked,
            colors = ButtonDefaults.buttonColors(backgroundColor = ColorAccent, disabledBackgroundColor = Color(0xFFC4C4C4)),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(4.dp),
            onClick = {
                if (intent != null) {
                    onStateChange(state.copy(isInputBlocked = true))
                    startPlacePicker.launch(intent)
                }
            }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = null,
                modifier= Modifier.size(24.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
fun LodestoneCreator(
    state: LodestoneState,
    lodestoneCreatorState: LodestoneCreator,
    onLodestoneCreatorState: (LodestoneCreator) -> Unit,
    performAction: (LodestoneAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .padding(bottom = 32.dp, top = 8.dp)
    ) {
        OutlinedTextField(
            value = lodestoneCreatorState.name,
            onValueChange = {
                onLodestoneCreatorState(lodestoneCreatorState.copy(name = it))
            }, placeholder = { Text("Lodestone name") },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                unfocusedBorderColor = ColorAccent,
                focusedBorderColor = ColorAccent,
                textColor = ColorPrimary,
                cursorColor = ColorAccent,
                backgroundColor = Color.White
            ), textStyle = TextStyle(fontWeight = FontWeight.SemiBold),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = "%.6f".format(lodestoneCreatorState.coordinates.latitude),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "latitude",
                    color = Color(0xFFC4C4C4),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column {
                Text(
                    text = "%.6f".format(lodestoneCreatorState.coordinates.longitude),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "longitude",
                    color = Color(0xFFC4C4C4),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Text(
            text = lodestoneCreatorState.mapAddress,
            color = Color(0xFFC4C4C4),
            fontSize = 20.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Button(
            enabled = lodestoneCreatorState.canCreate && !state.isInputBlocked,
            onClick = { performAction(LodestoneAction.CreateLodestone(lodestoneCreatorState)) },
            colors = ButtonDefaults.buttonColors(backgroundColor = ColorAccent, disabledBackgroundColor = Color(0xFFC4C4C4)),
            contentPadding = PaddingValues(vertical = 12.dp),
            shape = CircleShape,
            elevation = ButtonDefaults.elevation(6.dp),
            content = {
                Text(
                    text = "Create new location",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White
                )
            }, modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
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

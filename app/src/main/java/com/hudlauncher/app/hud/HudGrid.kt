package com.hudlauncher.app.hud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hudlauncher.app.data.HudSlot
import com.hudlauncher.app.data.UiSettings

/** Fixed-column grid; an incomplete last row starts at the left edge. */
@Composable
fun HudGrid(
    slots: List<HudSlot>,
    settings: UiSettings,
    editMode: Boolean,
    animationIntensity: Float,
    onSlotClick: (HudSlot) -> Unit,
    onSlotLongClick: (HudSlot) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 16.dp),
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(settings.columns),
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(slots, key = { _, slot -> slot.id }) { index, slot ->
            HudAppIcon(
                slot = slot,
                iconSize = settings.iconSize.dp,
                labelSizeSp = settings.labelSize,
                labelGap = settings.labelSpacing.dp,
                showLabel = settings.showLabels,
                editMode = editMode,
                animationIntensity = animationIntensity,
                index = index,
                onClick = { onSlotClick(slot) },
                onLongClick = { onSlotLongClick(slot) },
            )
        }
    }
}

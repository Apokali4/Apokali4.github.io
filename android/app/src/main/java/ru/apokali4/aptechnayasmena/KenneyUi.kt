package ru.apokali4.aptechnayasmena

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object KenneyPalette {
    val ink = Color(0xFF332447)
    val muted = Color(0xFF766D82)
    val background = Color(0xFFF4F0F8)
    val purple = Color(0xFF7652A8)
    val purpleDark = Color(0xFF392653)
    val purpleSoft = Color(0xFFE9E0F3)
    val mint = Color(0xFF69B6A0)
    val panelTint = Color(0xFFF0EAF5)
    val disabled = Color(0xFFB8B2BE)
}

@Composable
fun KenneyPanel(
    modifier: Modifier = Modifier,
    tint: Color = KenneyPalette.panelTint,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(10.dp),
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier) {
        Image(
            painter = painterResource(R.drawable.kenney_png_grey_default_button_rectangle_depth_flat),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds,
            colorFilter = ColorFilter.tint(tint, BlendMode.Modulate)
        )
        Column(
            modifier = Modifier.fillMaxWidth().then(if (fillContentHeight) Modifier.fillMaxHeight() else Modifier).padding(contentPadding),
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            content = content
        )
    }
}

@Composable
fun KenneyButton(
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    outlined: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) {},
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(
                if (outlined) R.drawable.kenney_png_grey_default_button_round_border
                else R.drawable.kenney_png_grey_default_button_round_depth_flat
            ),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds,
            colorFilter = ColorFilter.tint(
                if (enabled) KenneyPalette.purple else KenneyPalette.disabled,
                BlendMode.Modulate
            )
        )
        Text(
            label,
            color = if (outlined) KenneyPalette.purpleDark else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

@Composable
fun KenneyTile(
    icon: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    tint: Color = KenneyPalette.panelTint,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 76.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(role = Role.Button, onClick = onClick)
    ) {
        Image(
            painter = painterResource(R.drawable.kenney_png_grey_default_button_rectangle_depth_flat),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds,
            colorFilter = ColorFilter.tint(tint, BlendMode.Modulate)
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, color = KenneyPalette.purple, fontSize = 25.sp, modifier = Modifier.width(42.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, color = KenneyPalette.ink, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(subtitle, color = KenneyPalette.muted, fontSize = 12.sp, lineHeight = 16.sp)
            }
            Image(
                painter = painterResource(R.drawable.kenney_png_grey_default_arrow_basic_e),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                colorFilter = ColorFilter.tint(KenneyPalette.purple, BlendMode.Modulate)
            )
        }
    }
}

@Composable
fun KenneyChoice(
    label: String,
    selected: Boolean,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) KenneyPalette.purpleSoft else Color(0xFFF8F6FA))
            .clickable(enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(
                if (selected) R.drawable.kenney_png_grey_default_check_square_color_checkmark
                else R.drawable.kenney_png_grey_default_check_square_grey
            ),
            contentDescription = null,
            modifier = Modifier.size(25.dp),
            colorFilter = ColorFilter.tint(
                if (selected) KenneyPalette.purple else KenneyPalette.muted,
                BlendMode.Modulate
            )
        )
        Spacer(Modifier.width(10.dp))
        Text(label, color = KenneyPalette.ink, fontSize = 14.sp, lineHeight = 19.sp)
    }
}

@Composable
fun KenneyNavIcon(symbol: String, selected: Boolean) {
    Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
        if (selected) {
            Image(
                painter = painterResource(R.drawable.kenney_png_grey_default_button_round_depth_flat),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds,
                colorFilter = ColorFilter.tint(KenneyPalette.purple, BlendMode.Modulate)
            )
        }
        Text(symbol, color = if (selected) Color.White else KenneyPalette.purple, fontSize = 18.sp)
    }
}

@Composable
fun KenneyTextButton(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        color = KenneyPalette.purple,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp)
    )
}

package org.vaulture.project.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.vaulture.project.presentation.theme.PoppinsTypography

@Composable
fun FilterChips(
    categories: List<String>,
    selectedCategory:String?,
    onFilterSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
){
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ){
       items(categories.size){index ->
           val category = categories[index]
           FilterChip(
               selected = selectedCategory == category,
               onClick = {onFilterSelected(category)},
               label = {
                   Text(
                   category,
                       style = PoppinsTypography().bodyMedium,
                       maxLines = 1,
                       overflow = TextOverflow.Ellipsis
                   ) },
               colors = FilterChipDefaults.filterChipColors(
                   selectedContainerColor = MaterialTheme.colorScheme.primary,
                   selectedLabelColor = MaterialTheme.colorScheme.onPrimary
               )
           )
       }
    }
}
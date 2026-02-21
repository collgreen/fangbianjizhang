package com.example.fangbianjizhang.ui.record

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fangbianjizhang.domain.model.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen(
    onBack: () -> Unit,
    viewModel: RecordViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    Column(Modifier.fillMaxSize()) {
        // Top bar
        TopAppBar(
            title = {},
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, "关闭")
                }
            },
            actions = {
                TextButton(
                    onClick = { viewModel.save() },
                    enabled = !state.isSaving && state.amount.isNotBlank()
                ) { Text("保存") }
            }
        )

        // Type tabs
        TypeTabs(state.type, viewModel::setType)

        // Amount display
        AmountDisplay(state.amount)

        // Category grid (hide for transfer)
        if (state.type != TransactionType.TRANSFER) {
            CategoryGrid(state, viewModel)
        }

        // Account selector
        AccountSelector(state, viewModel)

        // Note field
        NoteField(state.note, viewModel::setNote)

        Spacer(Modifier.weight(1f))

        // Number pad
        NumberPad(viewModel)
    }
}

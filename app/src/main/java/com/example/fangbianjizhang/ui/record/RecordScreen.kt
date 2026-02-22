package com.example.fangbianjizhang.ui.record

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

    Column(Modifier.fillMaxSize().navigationBarsPadding()) {
        // Top bar
        TopAppBar(
            title = { if (state.isEditMode) Text("编辑明细") },
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

        // Scrollable content area
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState())
        ) {
            TypeTabs(state.type, viewModel::setType)
            AmountDisplay(state.amount)
            if (state.type != TransactionType.TRANSFER) {
                CategoryGrid(state, viewModel)
            }
            AccountSelector(state, viewModel)
            NoteField(state.note, viewModel::setNote)
        }

        // Number pad fixed at bottom
        NumberPad(viewModel)
    }
}

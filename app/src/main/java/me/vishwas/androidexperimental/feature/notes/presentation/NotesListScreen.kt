package me.vishwas.androidexperimental.feature.notes.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.collectLatest
import me.vishwas.androidexperimental.core.common.NoteCache

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    onNavigateToDetail: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = hiltViewModel(),
) {
    // collectAsStateWithLifecycle (not collectAsState) pauses collection
    // when the screen is stopped (e.g. backgrounded) and resumes on
    // restart, avoiding wasted work and, more importantly, avoiding a
    // crash-prone update to a torn-down UI after Activity.onStop().
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()

    var isComposingNote by remember { mutableStateOf(false) }

    // The note currently shown in the detail pane on wide screens — separate
    // from NoteCache.selectedNote, which only exists to hand a note across
    // a NavController.navigate() call on narrow screens (Topic 20). Purely
    // ephemeral UI state, not business data, so it stays in Compose rather
    // than moving into NotesViewModel.
    var paneNote by remember { mutableStateOf<NotePreview?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // DisposableEffect: setup on entering composition, teardown on leaving —
    // the correct place for a non-Compose side effect tied to this
    // composable's lifetime, run exactly once per Unit key (i.e. once total).
    DisposableEffect(Unit) {
        NotesAnalytics.logScreenView()
        onDispose { NotesAnalytics.logScreenLeft() }
    }

    // SideEffect: runs after every successful (non-skipped) recomposition,
    // syncing the latest Compose state to a non-Compose system.
    SideEffect {
        val noteCount = (uiState as? NotesUiState.Success)?.notes?.size ?: 0
        NotesAnalytics.setNoteCountBreadcrumb(noteCount)
    }

    // One collector, at the screen root, translating one-off ViewModel
    // events into UI actions (a snackbar) — see Topic 24 for why this
    // replaced Topic 13's CompositionLocal-based snackbar trigger once a
    // ViewModel + SharedFlow entered the picture.
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is NotesUiEvent.NoteSaved -> {
                    isComposingNote = false
                    snackbarHostState.showSnackbar("Note saved")
                }
                is NotesUiEvent.SaveFailed -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Notes") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { isComposingNote = true }) {
                Icon(Icons.Rounded.Add, contentDescription = "Create note")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        NotesPaneLayout(
            notes = (uiState as? NotesUiState.Success)?.notes ?: persistentListOf(),
            query = query,
            onQueryChange = viewModel::onQueryChange,
            isComposingNote = isComposingNote,
            onDismissCompose = { isComposingNote = false },
            onSaveNote = viewModel::saveNote,
            paneNote = paneNote,
            onNoteClickPane = { paneNote = it },
            onNoteClickCompact = { note ->
                NoteCache.selectedNote = note
                onNavigateToDetail()
            },
            modifier = Modifier.padding(innerPadding),
        )
    }
}

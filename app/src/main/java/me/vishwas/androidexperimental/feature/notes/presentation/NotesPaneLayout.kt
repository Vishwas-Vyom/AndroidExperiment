package me.vishwas.androidexperimental.feature.notes.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun NotesPaneLayout(
    notes: ImmutableList<NotePreview>,
    query: String,
    onQueryChange: (String) -> Unit,
    isComposingNote: Boolean,
    onDismissCompose: () -> Unit,
    onSaveNote: (title: String, content: String) -> Unit,
    paneNote: NotePreview?,
    onNoteClickCompact: (NotePreview) -> Unit,
    onNoteClickPane: (NotePreview) -> Unit,
    modifier: Modifier = Modifier,
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<Int>()
    val scope = rememberCoroutineScope()

    BackHandler(navigator.canNavigateBack()) {
        scope.launch { navigator.navigateBack() }
    }

    ListDetailPaneScaffold(
        modifier = modifier,
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                NotesListContent(
                    notes = notes,
                    query = query,
                    onQueryChange = onQueryChange,
                    isComposingNote = isComposingNote,
                    onDismissCompose = onDismissCompose,
                    onSaveNote = onSaveNote,
                    onNoteClick = { note ->
                        // Two-pane (tablet/foldable-unfolded): update the
                        // adjacent detail pane in place and switch the
                        // scaffold's pane focus, no NavController involved.
                        // Single-pane (phone): push a real, separate
                        // destination via NavController instead, since
                        // there's no second pane to show it in.
                        if (navigator.scaffoldDirective.maxHorizontalPartitions > 1) {
                            onNoteClickPane(note)
                            scope.launch {
                                navigator.navigateTo(pane = ListDetailPaneScaffoldRole.Detail, contentKey = note.id)
                            }
                        } else {
                            onNoteClickCompact(note)
                        }
                    },
                )
            }
        },
        detailPane = {
            AnimatedPane {
                if (paneNote != null) {
                    NoteDetailContent(note = paneNote)
                } else {
                    NoteDetailPlaceholder()
                }
            }
        },
    )
}

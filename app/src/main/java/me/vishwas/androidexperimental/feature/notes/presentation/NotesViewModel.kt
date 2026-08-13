package me.vishwas.androidexperimental.feature.notes.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.vishwas.androidexperimental.feature.notes.domain.model.Note
import me.vishwas.androidexperimental.feature.notes.domain.usecase.AddNoteUseCase
import me.vishwas.androidexperimental.feature.notes.domain.usecase.GetNotesUseCase
import javax.inject.Inject

sealed class NotesUiState {
    data object Loading : NotesUiState()

    @Immutable
    data class Success(val notes: ImmutableList<NotePreview>) : NotesUiState()
}

sealed class NotesUiEvent {
    data object NoteSaved : NotesUiEvent()
    data class SaveFailed(val message: String) : NotesUiEvent()
}

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val getNotesUseCase: GetNotesUseCase,
    private val addNoteUseCase: AddNoteUseCase,
) : ViewModel() {

    // Owned here instead of as Compose rememberSaveable state (Topic 8) —
    // a ViewModel already survives configuration changes for free, so no
    // Saveable mechanism is needed for it to outlive rotation. It does NOT
    // survive process death without a SavedStateHandle, which this teaching
    // app accepts as a known simplification.
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    // combine() re-runs its transform every time EITHER upstream Flow emits —
    // a new note saved (getNotesUseCase()) or a new search term (_query) —
    // producing exactly one downstream uiState update either way, never two
    // separate independent updates to reconcile.
    val uiState: StateFlow<NotesUiState> = combine(getNotesUseCase(), _query) { notes, query ->
        val filtered = if (query.isBlank()) notes else notes.filter { it.title.contains(query, ignoreCase = true) }
        NotesUiState.Success(filtered.map { it.toNotePreview() }.toImmutableList())
    }.stateIn(
        scope = viewModelScope,
        // WhileSubscribed(5_000): keep the underlying Room Flow (and its
        // query) alive for 5s after the last collector goes away (e.g. a
        // config change tearing down and immediately recreating the
        // Compose UI), instead of restarting the DB query from scratch.
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NotesUiState.Loading,
    )

    private val _events = MutableSharedFlow<NotesUiEvent>()
    val events: SharedFlow<NotesUiEvent> = _events.asSharedFlow()

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun saveNote(title: String, content: String) {
        viewModelScope.launch {
            try {
                addNoteUseCase(title, content)
                _events.emit(NotesUiEvent.NoteSaved)
            } catch (e: IllegalArgumentException) {
                _events.emit(NotesUiEvent.SaveFailed(e.message.orEmpty()))
            }
        }
    }

    private fun Note.toNotePreview() = NotePreview(
        // .toInt() is a simplification for this teaching app's tiny dataset —
        // a production app with unbounded rows should keep IDs Long
        // end-to-end rather than narrowing at this boundary.
        id = id.toInt(),
        title = title,
        content = content,
        photoUrl = photoUrl,
    )
}

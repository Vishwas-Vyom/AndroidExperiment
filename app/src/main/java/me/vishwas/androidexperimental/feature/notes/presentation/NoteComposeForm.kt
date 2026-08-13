package me.vishwas.androidexperimental.feature.notes.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import me.vishwas.androidexperimental.ui.theme.AndroidExperimentalTheme

/**
 * title/content use rememberSaveable, not remember, so a draft survives a
 * configuration change (rotation) that happens mid-typing, before Save is
 * ever pressed — remember alone would silently wipe an in-progress draft on
 * rotation, which reads as "the app ate what I typed" to a user.
 *
 * onSave only reports the committed (title, content) upward — it no longer
 * triggers a snackbar itself. That moved to NotesListScreen collecting
 * NotesViewModel.events (Topic 24, SharedFlow), once a ViewModel existed to
 * own "did the save actually succeed" — this form has no way to know that,
 * only that the user pressed Save with valid input.
 */
@Composable
fun NoteComposeForm(
    onCancel: () -> Unit,
    onSave: (title: String, content: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var title by rememberSaveable { mutableStateOf("") }
    var content by rememberSaveable { mutableStateOf("") }
    // Only true after a failed Save attempt — validation errors shouldn't
    // appear before the user has tried to submit anything (that would read
    // as the form scolding them before they've done anything wrong).
    var titleError by rememberSaveable { mutableStateOf(false) }
    val titleFocusRequester = remember { FocusRequester() }

    // LaunchedEffect(Unit): runs once when this composable first enters
    // composition (i.e. exactly when the user taps the FAB and this form
    // appears) — the correct place to trigger a one-shot action tied to
    // "I just appeared," which requestFocus() is. Doing this directly in
    // the composable body instead would violate side-effect-free composition
    // and re-request focus on every recomposition, stealing focus back from
    // the user mid-typing.
    LaunchedEffect(Unit) {
        titleFocusRequester.requestFocus()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                if (titleError) titleError = false
            },
            label = { Text("Title") },
            singleLine = true,
            isError = titleError,
            supportingText = if (titleError) {
                { Text("Title is required") }
            } else {
                null
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(titleFocusRequester),
        )
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Note") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            OutlinedButton(onClick = onCancel) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                        titleFocusRequester.requestFocus()
                    } else {
                        onSave(title, content)
                    }
                },
                modifier = Modifier.padding(start = 8.dp),
            ) {
                Text("Save")
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun NoteComposeFormPreview() {
    AndroidExperimentalTheme {
        NoteComposeForm(onCancel = {}, onSave = { _, _ -> })
    }
}

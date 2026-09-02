package me.vishwas.androidexperimental.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Search : Screen("search")
    data object Bookmarks : Screen("bookmarks")
    data object Detail : Screen("detail")
    data object Notes : Screen("notes")
    data object NoteDetail : Screen("note_detail")
    data object Rooms : Screen("rooms")
    data object Controls : Screen("controls")
    data object Viewport : Screen("viewport")
    data object BlurLab : Screen("blur_lab")
}

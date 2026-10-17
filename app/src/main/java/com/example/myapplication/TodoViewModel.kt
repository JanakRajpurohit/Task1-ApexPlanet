package com.example.myapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class TodoUiState(
    val todos: List<TodoItem> = emptyList(),
    val filter: TodoFilter = TodoFilter.ALL,
    val searchQuery: String = "",
    val activeCount: Int = 0,
    val completedCount: Int = 0
)

class TodoViewModel : ViewModel() {

    private val _todos = MutableStateFlow<List<TodoItem>>(
        listOf(
            TodoItem(title = "Welcome to To-Do App!", description = "Tap the checkbox to mark as done.", priority = Priority.HIGH),
            TodoItem(title = "Create a new task", description = "Use the + button below to add tasks.", priority = Priority.MEDIUM),
            TodoItem(title = "Filter tasks", description = "Switch between All, Active, and Completed tabs.", priority = Priority.LOW)
        )
    )
    private val _filter = MutableStateFlow(TodoFilter.ALL)
    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<TodoUiState> = combine(
        _todos,
        _filter,
        _searchQuery
    ) { todos, filter, query ->
        val filteredList = todos.filter { todo ->
            val matchesFilter = when (filter) {
                TodoFilter.ALL -> true
                TodoFilter.ACTIVE -> !todo.isCompleted
                TodoFilter.COMPLETED -> todo.isCompleted
            }
            val matchesSearch = query.isBlank() ||
                    todo.title.contains(query, ignoreCase = true) ||
                    todo.description.contains(query, ignoreCase = true)

            matchesFilter && matchesSearch
        }

        TodoUiState(
            todos = filteredList,
            filter = filter,
            searchQuery = query,
            activeCount = todos.count { !it.isCompleted },
            completedCount = todos.count { it.isCompleted }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TodoUiState()
    )

    fun addTodo(title: String, description: String = "", priority: Priority = Priority.MEDIUM) {
        if (title.isBlank()) return
        val newItem = TodoItem(
            title = title.trim(),
            description = description.trim(),
            priority = priority
        )
        _todos.value = _todos.value + newItem
    }

    fun toggleTodo(id: String) {
        _todos.value = _todos.value.map {
            if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
        }
    }

    fun deleteTodo(id: String) {
        _todos.value = _todos.value.filter { it.id != id }
    }

    fun updateTodo(id: String, title: String, description: String, priority: Priority) {
        if (title.isBlank()) return
        _todos.value = _todos.value.map {
            if (it.id == id) {
                it.copy(title = title.trim(), description = description.trim(), priority = priority)
            } else {
                it
            }
        }
    }

    fun setFilter(filter: TodoFilter) {
        _filter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearCompleted() {
        _todos.value = _todos.value.filter { !it.isCompleted }
    }
}

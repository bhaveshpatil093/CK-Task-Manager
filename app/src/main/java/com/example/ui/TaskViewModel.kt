package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Task
import com.example.data.TaskDatabase
import com.example.data.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaskRepository
    
    // Seed standard tasks when first initialized
    init {
        val database = TaskDatabase.getDatabase(application)
        repository = TaskRepository(database.taskDao())
    }

    // Main tasks collection
    val allTasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtering & Categories State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All") // All, Inbox, Work, Personal, Meeting
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedPriority = MutableStateFlow("All") // All, HIGH, MEDIUM, LOW
    val selectedPriority: StateFlow<String> = _selectedPriority.asStateFlow()

    // Filtered lists for rendering
    val filteredTasks: StateFlow<List<Task>> = combine(
        allTasks,
        _searchQuery,
        _selectedCategory,
        _selectedPriority
    ) { tasks, query, category, priority ->
        tasks.filter { task ->
            val matchesQuery = task.title.contains(query, ignoreCase = true) ||
                    task.description.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || task.category.equals(category, ignoreCase = true)
            val matchesPriority = priority == "All" || task.priority.equals(priority, ignoreCase = true)
            
            matchesQuery && matchesCategory && matchesPriority
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Metrics for the enterprise dashboard
    val dashboardStats = allTasks.map { list ->
        val total = list.size
        val pending = list.count { !it.isCompleted }
        val completed = list.count { it.isCompleted }
        val completionPercentage = if (total > 0) (completed.toFloat() / total.toFloat() * 100f).toInt() else 100
        DashboardStats(
            total = total,
            pending = pending,
            completed = completed,
            cleanlinessScore = completionPercentage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardStats(0, 0, 0, 100)
    )

    // App Config & Settings State
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("English")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }

    fun setSelectedLanguage(lang: String) {
        _selectedLanguage.value = lang
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
    }

    // Actions
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSelectedPriority(priority: String) {
        _selectedPriority.value = priority
    }

    fun addTask(title: String, description: String, priority: String, category: String, dueDate: String) {
        viewModelScope.launch {
            val newTask = Task(
                title = title.trim(),
                description = description.trim(),
                priority = priority.uppercase(),
                category = category,
                dueDate = dueDate.trim()
            )
            repository.insertTask(newTask)
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            repository.updateTaskStatus(task.id, !task.isCompleted)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    private suspend fun seedSampleTasks() {
        val samples = listOf(
            Task(
                title = "Configure CKTM Application Profile",
                description = "Establish core system tokens, customize primary workspace colors and brand theme guidelines.",
                priority = "HIGH",
                category = "Work",
                dueDate = "Today",
                isCompleted = false
            ),
            Task(
                title = "Verify WhatsApp Style Simplicity UI",
                description = "Complete live device check of list items, responsive taps, and compact details with 8dp grid system alignment.",
                priority = "HIGH",
                category = "Meeting",
                dueDate = "Yesterday",
                isCompleted = true
            ),
            Task(
                title = "Establish Zero Pendency KPIs",
                description = "Define clear benchmarks to drive active workflows to zero backlog within 24-hour cycles.",
                priority = "MEDIUM",
                category = "Work",
                dueDate = "Tomorrow",
                isCompleted = false
            ),
            Task(
                title = "Deploy Team Task Delegation Protocol",
                description = "Draft streamlined single-sentence delegation rules for non-technical users to access via the mobile dashboard.",
                priority = "LOW",
                category = "Personal",
                dueDate = "May 28",
                isCompleted = false
            )
        )
        for (sample in samples) {
            repository.insertTask(sample)
        }
    }
}

data class DashboardStats(
    val total: Int,
    val pending: Int,
    val completed: Int,
    val cleanlinessScore: Int // E.g., 75% completed
)

// Viewmodel factory helper
class TaskViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

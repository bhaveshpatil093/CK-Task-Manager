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

data class UserProfile(
    val name: String,
    val email: String,
    val role: String, // "Super-admin", "Admin", "Employee"
    val phone: String = ""
)

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaskRepository
    
    // Active Logged-In User Profile State (Stored in SharedPreferences)
    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    // All known/registered employees of Career Katta
    private val _registeredEmployees = MutableStateFlow<List<UserProfile>>(emptyList())
    val registeredEmployees: StateFlow<List<UserProfile>> = _registeredEmployees.asStateFlow()

    private val userSharedPrefs = application.getSharedPreferences("cktm_user_prefs", android.content.Context.MODE_PRIVATE)

    // Seed standard tasks when first initialized
    init {
        val database = TaskDatabase.getDatabase(application)
        repository = TaskRepository(database.taskDao())
        
        // Clear tasks once on first run of this version to ensure the app is empty at first
        val prefs = application.getSharedPreferences("cktm_prefs", android.content.Context.MODE_PRIVATE)
        val isFirstRunOfEmptyVersion = prefs.getBoolean("first_run_empty_v4", true)
        if (isFirstRunOfEmptyVersion) {
            viewModelScope.launch {
                repository.deleteAllTasks()
            }
            prefs.edit().putBoolean("first_run_empty_v4", false).apply()
        }

        // Load active logged-in user if exists
        val name = userSharedPrefs.getString("user_name", "") ?: ""
        val email = userSharedPrefs.getString("user_email", "") ?: ""
        val role = userSharedPrefs.getString("user_role", "") ?: ""
        val phone = userSharedPrefs.getString("user_phone", "") ?: ""
        if (name.isNotEmpty() && role.isNotEmpty()) {
            _currentUser.value = UserProfile(name, email, role, phone)
        }

        // Load the list of all registered employees
        loadRegisteredEmployees()
    }

    private fun loadRegisteredEmployees() {
        val count = userSharedPrefs.getInt("emp_count", -1)
        if (count == -1) {
            // Seed standard fallback employees for team assignment
            val defaultEmps = listOf(
                UserProfile("Bhavesh Patil", "bhaveshpatiltech@gmail.com", "Super-admin", "+91 90123 45678"),
                UserProfile("Amit Sharma", "amit.sharma@example.com", "Employee", "+91 98765 43210"),
                UserProfile("Priya Nair", "priya.nair@example.com", "Employee", "+91 91234 56789"),
                UserProfile("Sachin Kale", "sachin.kale@example.com", "Employee", "+91 88888 77777"),
                UserProfile("Karan Johar", "karan.johar@example.com", "Admin", "+91 77777 66666")
            )
            val editor = userSharedPrefs.edit()
            editor.putInt("emp_count", defaultEmps.size)
            defaultEmps.forEachIndexed { i, emp ->
                editor.putString("emp_name_$i", emp.name)
                editor.putString("emp_email_$i", emp.email)
                editor.putString("emp_role_$i", emp.role)
                editor.putString("emp_phone_$i", emp.phone)
            }
            editor.apply()
            _registeredEmployees.value = defaultEmps
        } else {
            val list = mutableListOf<UserProfile>()
            for (i in 0 until count) {
                val empName = userSharedPrefs.getString("emp_name_$i", "") ?: ""
                val empEmail = userSharedPrefs.getString("emp_email_$i", "") ?: ""
                val empRole = userSharedPrefs.getString("emp_role_$i", "") ?: ""
                val empPhone = userSharedPrefs.getString("emp_phone_$i", "") ?: ""
                if (empName.isNotEmpty()) {
                    list.add(UserProfile(empName, empEmail, empRole, empPhone))
                }
            }
            _registeredEmployees.value = list
        }
    }

    fun registerAndLogin(name: String, email: String, role: String, phone: String) {
        val cleanedName = name.trim()
        val cleanedEmail = email.trim()
        val cleanedRole = role.trim()
        val cleanedPhone = phone.trim()
        
        // Save current user
        _currentUser.value = UserProfile(cleanedName, cleanedEmail, cleanedRole, cleanedPhone)
        userSharedPrefs.edit()
            .putString("user_name", cleanedName)
            .putString("user_email", cleanedEmail)
            .putString("user_role", cleanedRole)
            .putString("user_phone", cleanedPhone)
            .apply()

        // Sync with employees list
        val currentEmps = _registeredEmployees.value.toMutableList()
        val existingIndex = currentEmps.indexOfFirst { it.email.lowercase() == cleanedEmail.lowercase() || it.name.lowercase() == cleanedName.lowercase() }
        if (existingIndex == -1) {
            val nextIndex = currentEmps.size
            currentEmps.add(UserProfile(cleanedName, cleanedEmail, cleanedRole, cleanedPhone))
            userSharedPrefs.edit()
                .putInt("emp_count", currentEmps.size)
                .putString("emp_name_$nextIndex", cleanedName)
                .putString("emp_email_$nextIndex", cleanedEmail)
                .putString("emp_role_$nextIndex", cleanedRole)
                .putString("emp_phone_$nextIndex", cleanedPhone)
                .apply()
            _registeredEmployees.value = currentEmps
        } else {
            // Update existing entry with modified details
            currentEmps[existingIndex] = UserProfile(cleanedName, cleanedEmail, cleanedRole, cleanedPhone)
            userSharedPrefs.edit()
                .putString("emp_name_$existingIndex", cleanedName)
                .putString("emp_email_$existingIndex", cleanedEmail)
                .putString("emp_role_$existingIndex", cleanedRole)
                .putString("emp_phone_$existingIndex", cleanedPhone)
                .apply()
            _registeredEmployees.value = currentEmps
        }
    }

    fun logout() {
        _currentUser.value = null
        userSharedPrefs.edit()
            .remove("user_name")
            .remove("user_email")
            .remove("user_role")
            .remove("user_phone")
            .apply()
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

    // Filtered lists for rendering (Employee-aware: visible tasks only for logged employee)
    val filteredTasks: StateFlow<List<Task>> = combine(
        allTasks,
        _searchQuery,
        _selectedCategory,
        _selectedPriority,
        _currentUser
    ) { tasks, query, category, priority, user ->
        tasks.filter { task ->
            // Employee permission restriction: Employees see only tasks assigned to them
            val passesRole = if (user != null && user.role.equals("Employee", ignoreCase = true)) {
                task.assignedTo.isNotBlank() && (
                    task.assignedTo.equals(user.name, ignoreCase = true) ||
                    task.assignedTo.equals(user.email, ignoreCase = true)
                )
            } else {
                true // Super-admin and Admin see all tasks
            }

            if (!passesRole) return@filter false

            val matchesQuery = task.title.contains(query, ignoreCase = true) ||
                    task.description.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || task.category.equals(category, ignoreCase = true)
            val matchesPriority = priority == "All" || task.priority.equals(priority, ignoreCase = true)
            
            matchesQuery && matchesCategory && matchesPriority
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Metrics for the enterprise dashboard
    val dashboardStats: StateFlow<DashboardStats> = combine(allTasks, _currentUser) { list, user ->
        val visibleList = list.filter { task ->
            if (user != null && user.role.equals("Employee", ignoreCase = true)) {
                task.assignedTo.isNotBlank() && (
                    task.assignedTo.equals(user.name, ignoreCase = true) ||
                    task.assignedTo.equals(user.email, ignoreCase = true)
                )
            } else {
                true
            }
        }
        val total = visibleList.size
        val pending = visibleList.count { !it.isCompleted }
        val completed = visibleList.count { it.isCompleted }
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

    fun addTask(title: String, description: String, priority: String, category: String, dueDate: String, assignedTo: String = "") {
        viewModelScope.launch {
            val newTask = Task(
                title = title.trim(),
                description = description.trim(),
                priority = priority.uppercase(),
                category = category,
                dueDate = dueDate.trim(),
                assignedTo = assignedTo.trim()
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
}

data class DashboardStats(
    val total: Int,
    val pending: Int,
    val completed: Int,
    val cleanlinessScore: Int // E.g., 75% completed
)

class TaskViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

package com.example.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    val pendingTasks: Flow<List<Task>> = taskDao.getPendingTasks()

    suspend fun insertTask(task: Task) {
        taskDao.insertTask(task)
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }

    suspend fun deleteTaskById(id: Int) {
        taskDao.deleteTaskById(id)
    }

    suspend fun deleteAllTasks() {
        taskDao.deleteAllTasks()
    }

    suspend fun updateTaskStatus(id: Int, isCompleted: Boolean) {
        taskDao.updateTaskStatus(id, isCompleted)
    }
}

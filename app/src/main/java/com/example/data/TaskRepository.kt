package com.example.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {

    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val allPomodoroSessions: Flow<List<PomodoroSessionEntity>> = taskDao.getAllPomodoroSessions()
    val allCategoryLinks: Flow<List<CategoryLinkEntity>> = taskDao.getAllCategoryLinks()
    val allCategories: Flow<List<CategoryEntity>> = taskDao.getAllCategories()


    suspend fun getTaskById(id: Int): TaskEntity? = taskDao.getTaskById(id)

    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

    suspend fun clearAllTasks() = taskDao.clearAllTasks()

    suspend fun insertPomodoroSession(session: PomodoroSessionEntity) =
        taskDao.insertPomodoroSession(session)

    suspend fun clearAllPomodoroSessions() = taskDao.clearAllPomodoroSessions()

    suspend fun insertCategoryLink(link: CategoryLinkEntity) =
        taskDao.insertCategoryLink(link)

    suspend fun deleteCategoryLink(link: CategoryLinkEntity) =
        taskDao.deleteCategoryLink(link)

    suspend fun insertCategory(category: CategoryEntity) =
        taskDao.insertCategory(category)

    suspend fun deleteCategory(category: CategoryEntity) =
        taskDao.deleteCategory(category)

    suspend fun updateTasksCategory(oldName: String, newName: String) =
        taskDao.updateTasksCategory(oldName, newName)

    suspend fun updatePomodoroSessionsCategory(oldName: String, newName: String) =
        taskDao.updatePomodoroSessionsCategory(oldName, newName)

    suspend fun updateCategoryLinksCategory(oldName: String, newName: String) =
        taskDao.updateCategoryLinksCategory(oldName, newName)
}

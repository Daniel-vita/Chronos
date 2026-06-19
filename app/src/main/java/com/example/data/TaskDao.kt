package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    // --- Tasks CRUD ---
    @Query("SELECT * FROM tasks ORDER BY scheduledStartTime ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Int): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks")
    suspend fun clearAllTasks()

    // --- Pomodoro Sessions ---
    @Query("SELECT * FROM pomodoro_sessions ORDER BY timestamp DESC")
    fun getAllPomodoroSessions(): Flow<List<PomodoroSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPomodoroSession(session: PomodoroSessionEntity)

    @Query("DELETE FROM pomodoro_sessions")
    suspend fun clearAllPomodoroSessions()

    // --- Category App Links ---
    @Query("SELECT * FROM category_links")
    fun getAllCategoryLinks(): Flow<List<CategoryLinkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoryLink(link: CategoryLinkEntity)

    @Delete
    suspend fun deleteCategoryLink(link: CategoryLinkEntity)

    // --- Categories CRUD ---
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("UPDATE tasks SET category = :newName WHERE category = :oldName")
    suspend fun updateTasksCategory(oldName: String, newName: String)

    @Query("UPDATE pomodoro_sessions SET category = :newName WHERE category = :oldName")
    suspend fun updatePomodoroSessionsCategory(oldName: String, newName: String)

    @Query("UPDATE category_links SET category = :newName WHERE category = :oldName")
    suspend fun updateCategoryLinksCategory(oldName: String, newName: String)
}

package com.example.viewmodel

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

// Represents a simulated device application
data class DeviceApp(
    val name: String,
    val packageName: String,
    val iconDescription: String,
    val simulatedDailyMinutes: Int
)

// Represents a simulated notification
data class ChronosNotification(
    val id: Long = System.currentTimeMillis() + (0..1000).random(),
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val relatedTaskId: Int? = null
)

class ChronosViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TaskRepository(database.taskDao())
    }

    // --- State Sources ---
    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPomodoroSessions: StateFlow<List<PomodoroSessionEntity>> = repository.allPomodoroSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategoryLinks: StateFlow<List<CategoryLinkEntity>> = repository.allCategoryLinks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // --- Current UI Screen ---
    private val _currentScreen = MutableStateFlow("dashboard") // dashboard, pomodoro, statistiche, consigli_ia, impostazioni
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    // --- Filter States ---
    val filterCategory = MutableStateFlow<String?>(null) // "Lavoro", "Studio", "Tempo Libero"
    val filterTime = MutableStateFlow("Oggi") // Default is now handled in UI, lets change default filterTime to "Tutte" or "Oggi"
    val filterDayOffset = MutableStateFlow<Int?>(0) // null = all days, 0 = today, 1 = tomorrow, 2 = day after, etc.
    val filterApp = MutableStateFlow<String?>(null) // e.g. "Slack"
    val filterStatus = MutableStateFlow("Tutte") // "Tutte", "Completate", "Incomplete", "In Ritardo", "In Anticipo"

    // --- Active Pomodoro Timer State ---
    private val _pomodoroSecondsLeft = MutableStateFlow(1500) // 25 mins initially
    val pomodoroSecondsLeft: StateFlow<Int> = _pomodoroSecondsLeft.asStateFlow()

    private val _pomodoroTotalDurationSeconds = MutableStateFlow(1500)
    val pomodoroTotalDurationSeconds: StateFlow<Int> = _pomodoroTotalDurationSeconds.asStateFlow()

    private val _pomodoroIsRunning = MutableStateFlow(false)
    val pomodoroIsRunning: StateFlow<Boolean> = _pomodoroIsRunning.asStateFlow()

    private val _pomodoroMode = MutableStateFlow("Studio") // "Studio" (25m), "Pausa Breve" (5m), "Pausa Lunga" (15m)
    val pomodoroMode: StateFlow<String> = _pomodoroMode.asStateFlow()

    private val _pomodoroLinkedTask = MutableStateFlow<TaskEntity?>(null)
    val pomodoroLinkedTask: StateFlow<TaskEntity?> = _pomodoroLinkedTask.asStateFlow()

    private var timerJob: Job? = null

    // --- Customizable App Theme color ---
    private val _themeColor = MutableStateFlow("Sleek") // "Sleek", "Slate", "Teal", "Ocean", "Sunset" (Ambra)
    val themeColor: StateFlow<String> = _themeColor.asStateFlow()

    fun changeThemeColor(colorName: String) {
        _themeColor.value = colorName
    }

    // --- Gemini AI State ---
    private val _aiAdvice = MutableStateFlow<String?>(null)
    val aiAdvice: StateFlow<String?> = _aiAdvice.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // --- Simulated System Notifications and Warning Alerts ---
    private val _notifications = MutableStateFlow<List<ChronosNotification>>(emptyList())
    val notifications: StateFlow<List<ChronosNotification>> = _notifications.asStateFlow()

    // --- Simulated Installed Apps ---
    val installedApps = listOf(
        DeviceApp("Slack", "com.slack", "✉️ App di Lavoro", 45),
        DeviceApp("Microsoft Teams", "com.microsoft.teams", "👥 App di Riunione", 30),
        DeviceApp("Duolingo", "com.duolingo", "🦉 App di Studio", 15),
        DeviceApp("Google Docs", "com.google.android.apps.docs", "📝 App di Scrittura", 25),
        DeviceApp("YouTube", "com.google.android.youtube", "🎥 App di Intrattenimento", 110),
        DeviceApp("Instagram", "com.instagram.android", "📸 App Social", 75),
        DeviceApp("Notion", "notion.id", "📦 App di Organizzazione", 40),
        DeviceApp("GitHub", "com.github.android", "💻 App per Sviluppatori", 20)
    )

    init {
        // Populate database with default values if empty
        viewModelScope.launch {
            delay(800) // short delay to avoid thread locking on start
            
            // Populate default categories if empty
            allCategories.first().let { currentCats ->
                if (currentCats.isEmpty()) {
                    repository.insertCategory(CategoryEntity("Lavoro", "💼"))
                    repository.insertCategory(CategoryEntity("Studio", "🎓"))
                    repository.insertCategory(CategoryEntity("Tempo Libero", "🍀"))
                }
            }

            // Check if tables are empty, populate demo tasks
            allTasks.first().let { currentList ->
                if (currentList.isEmpty()) {
                    createDemoData()
                }
            }

            // Periodically check for missed/overdue tasks to trigger "attività non svolte" warnings
            startMockActivityWarningScanner()
        }
    }

    private suspend fun createDemoData() {
        val now = System.currentTimeMillis()
        val oneHour = 3600 * 1000L

        // Default Category app links
        repository.insertCategoryLink(CategoryLinkEntity("Lavoro", "Slack", "com.slack"))
        repository.insertCategoryLink(CategoryLinkEntity("Lavoro", "Microsoft Teams", "com.microsoft.teams"))
        repository.insertCategoryLink(CategoryLinkEntity("Studio", "Duolingo", "com.duolingo"))
        repository.insertCategoryLink(CategoryLinkEntity("Studio", "Notion", "notion.id"))
        repository.insertCategoryLink(CategoryLinkEntity("Tempo Libero", "YouTube", "com.google.android.youtube"))
        repository.insertCategoryLink(CategoryLinkEntity("Tempo Libero", "Instagram", "com.instagram.android"))

        // Default tasks
        val task1 = TaskEntity(
            title = "Pianificazione Progetto Cloud",
            description = "Definire i moduli dell'architettura e scadenze con il team di sviluppo.",
            category = "Lavoro",
            priority = "Alta",
            deadline = now + 4 * oneHour,
            scheduledStartTime = now - 2 * oneHour,
            scheduledEndTime = now - oneHour,
            actualStartTime = now - 2 * oneHour,
            actualEndTime = now - oneHour - 10 * 60 * 1000L, // completed 10 minutes early!
            completed = true,
            tags = "cloud, sprint",
            attachments = "progetto_v1.pdf, mockup.png",
            linkedAppName = "Slack",
            linkedAppPackage = "com.slack",
            completedOnTime = "Anticipo",
            timeDifferenceMinutes = -10
        )

        val task2 = TaskEntity(
            title = "Studiare Capitolo 4 di Storia",
            description = "Ripasso approfondito per l'esame della prossima settimana.",
            category = "Studio",
            priority = "Media",
            deadline = now + 8 * oneHour,
            scheduledStartTime = now - 3 * oneHour,
            scheduledEndTime = now - oneHour,
            actualStartTime = now - 2 * oneHour - 30 * 60 * 1000, // started late
            actualEndTime = now - oneHour + 15 * 60 * 1000, // finished 15 minutes late!
            completed = true,
            tags = "storia, esame",
            attachments = "storia_appunti.docx",
            linkedAppName = "Duolingo",
            linkedAppPackage = "com.duolingo",
            completedOnTime = "Ritardo",
            timeDifferenceMinutes = 15
        )

        val task3 = TaskEntity(
            title = "Allenamento Funzionale",
            description = "30 minuti di cardio ed esercizi a corpo libero a casa.",
            category = "Tempo Libero",
            priority = "Bassa",
            deadline = now + 12 * oneHour,
            scheduledStartTime = now + 2 * oneHour,
            scheduledEndTime = now + 2*oneHour + 30*60*1000L,
            completed = false,
            tags = "cardio, benessere",
            attachments = ""
        )

        val task4 = TaskEntity(
            title = "Definire Roadmap Strategica",
            description = "Assegnazione compiti Q3 e revisione obiettivi principali.",
            category = "Lavoro",
            priority = "Alta",
            deadline = now - 1 * oneHour, // deadline passed!
            scheduledStartTime = now - 3 * oneHour,
            scheduledEndTime = now - 2 * oneHour,
            completed = false, // Missed! Output "Attività Non Svolta" warning!
            tags = "strategia, roadmap",
            attachments = ""
        )

        repository.insertTask(task1)
        repository.insertTask(task2)
        repository.insertTask(task3)
        repository.insertTask(task4)

        // Seed some pomodoro sessions
        repository.insertPomodoroSession(PomodoroSessionEntity(0, "Analisi Requisiti", 25, now - 5 * oneHour, "Lavoro"))
        repository.insertPomodoroSession(PomodoroSessionEntity(0, "Esercizi di Grammatica", 25, now - 23 * oneHour, "Studio"))
        repository.insertPomodoroSession(PomodoroSessionEntity(0, "Lettura Romanzo", 25, now - 48 * oneHour, "Tempo Libero"))
    }

    // --- Task Actions ---
    fun addTask(
        title: String,
        description: String,
        category: String,
        priority: String,
        deadline: Long,
        startTime: Long,
        endTime: Long,
        tags: String,
        attachments: String,
        linkedAppName: String?,
        linkedAppPackage: String?
    ) {
        viewModelScope.launch {
            val task = TaskEntity(
                title = title,
                description = description,
                category = category,
                priority = priority,
                deadline = deadline,
                scheduledStartTime = startTime,
                scheduledEndTime = endTime,
                tags = tags,
                attachments = attachments,
                linkedAppName = linkedAppName,
                linkedAppPackage = linkedAppPackage
            )
            repository.insertTask(task)
            addNotification("Attività Creata", "L'attività '$title' è stata pianificata con successo.")
        }
    }

    fun completeTask(taskId: Int, completedWithDelayMinutes: Int) {
        viewModelScope.launch {
            val task = repository.getTaskById(taskId)
            if (task != null) {
                val now = System.currentTimeMillis()
                val isDelayed = completedWithDelayMinutes > 0
                val isEarly = completedWithDelayMinutes < 0
                val compStatus = when {
                    isDelayed -> "Ritardo"
                    isEarly -> "Anticipo"
                    else -> "In Orario"
                }

                val updated = task.copy(
                    completed = true,
                    actualStartTime = task.scheduledStartTime,
                    actualEndTime = now,
                    completedOnTime = compStatus,
                    timeDifferenceMinutes = completedWithDelayMinutes
                )
                repository.updateTask(updated)
                
                val statusTextStr = when {
                    isDelayed -> "con $completedWithDelayMinutes minuti di ritardo."
                    isEarly -> "con ${-completedWithDelayMinutes} minuti di anticipo!"
                    else -> "perfettamente in orario!"
                }
                
                addNotification(
                    "Attività Completata",
                    "Hai segnato '${task.title}' come completata $statusTextStr",
                    taskId
                )
            }
        }
    }

    fun toggleTaskCompletionDirect(taskId: Int) {
        viewModelScope.launch {
            val task = repository.getTaskById(taskId)
            if (task != null) {
                if (task.completed) {
                    // Mark as incomplete
                    val updated = task.copy(
                        completed = false,
                        actualStartTime = null,
                        actualEndTime = null,
                        completedOnTime = null,
                        timeDifferenceMinutes = 0
                    )
                    repository.updateTask(updated)
                    addNotification("Attività Ripristinata", "'${task.title}' è di nuovo da svolgere.")
                } else {
                    // Simulating completing on-time (difference 0)
                    val updated = task.copy(
                        completed = true,
                        actualStartTime = task.scheduledStartTime,
                        actualEndTime = task.scheduledEndTime,
                        completedOnTime = "In Orario",
                        timeDifferenceMinutes = 0
                    )
                    repository.updateTask(updated)
                    addNotification("Attività Completata", "'${task.title}' completata in orario.")
                }
            }
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
            addNotification("Attività Eliminata", "'${task.title}' è stata rimossa.")
        }
    }

    // --- Category Links CRUD ---
    fun linkAppToCategory(category: String, app: DeviceApp) {
        viewModelScope.launch {
            repository.insertCategoryLink(
                CategoryLinkEntity(
                    category = category,
                    appName = app.name,
                    packageName = app.packageName
                )
            )
            addNotification(
                "App Collegata",
                "L'applicazione '${app.name}' è stata associata alla macrocategoria '$category'."
            )
        }
    }

    fun unlinkCategoryApp(link: CategoryLinkEntity) {
        viewModelScope.launch {
            repository.deleteCategoryLink(link)
            addNotification(
                "App Scollegata",
                "L'applicazione '${link.appName}' non è più associata a '${link.category}'."
            )
        }
    }

    // --- External Calendar Sync Simulator ---
    fun syncExternalCalendar() {
        viewModelScope.launch {
            _isAiLoading.value = true
            delay(1500) // simulate network requests to calendar providers
            
            val now = System.currentTimeMillis()
            val hour = 3600 * 1000L

            // Import 3 realistic calendar events from Google Calendar
            val calTask1 = TaskEntity(
                title = "📅 Riunione di Team (Marketing)",
                description = "Allineamento settimanale dei canali social e pianificazione campagne. Sincronizzato da Google Calendar.",
                category = "Lavoro",
                priority = "Media",
                deadline = now + 24 * hour,
                scheduledStartTime = now + 4 * hour,
                scheduledEndTime = now + 5 * hour,
                completed = false,
                tags = "gcal, sincronizzato",
                linkedAppName = "Microsoft Teams",
                linkedAppPackage = "com.microsoft.teams"
            )

            val calTask2 = TaskEntity(
                title = "📅 Presentazione Tesi Finale",
                description = "Presentazione dello stato d'avanzamento al mentore di facoltà. Sincronizzato da Outlook.",
                category = "Studio",
                priority = "Alta",
                deadline = now + 48 * hour,
                scheduledStartTime = now + 26 * hour,
                scheduledEndTime = now + 28 * hour,
                completed = false,
                tags = "outlook, college",
                linkedAppName = "Notion",
                linkedAppPackage = "notion.id"
            )

            val calTask3 = TaskEntity(
                title = "📅 Serata Teatro con Amici",
                description = "Rassegna teatrale locale e cena in centro. Sincronizzato da Apple Calendar.",
                category = "Tempo Libero",
                priority = "Bassa",
                deadline = now + 30 * hour,
                scheduledStartTime = now + 14 * hour,
                scheduledEndTime = now + 17 * hour,
                completed = false,
                tags = "tempo_libero, gcal",
                linkedAppName = "YouTube",
                linkedAppPackage = "com.google.android.youtube"
            )

            repository.insertTask(calTask1)
            repository.insertTask(calTask2)
            repository.insertTask(calTask3)

            _isAiLoading.value = false
            addNotification(
                "Sincronizzazione Completata",
                "Sincronizzazione con il calendario esterno terminata. Importati 3 nuovi eventi!"
            )
        }
    }

    // --- Pomodoro Ticker Logic ---
    fun configurePomodoro(minutes: Int, mode: String, linkedTask: TaskEntity? = null) {
        _pomodoroMode.value = mode
        _pomodoroSecondsLeft.value = minutes * 60
        _pomodoroTotalDurationSeconds.value = minutes * 60
        _pomodoroLinkedTask.value = linkedTask
    }

    fun startPomodoro() {
        if (_pomodoroIsRunning.value) return
        _pomodoroIsRunning.value = true
        
        timerJob = viewModelScope.launch {
            while (_pomodoroSecondsLeft.value > 0) {
                delay(1000)
                _pomodoroSecondsLeft.value -= 1
            }
            // Timer Finished! Let's play alarm and log session
            timerFinished()
        }
        
        val taskName = _pomodoroLinkedTask.value?.title ?: "Nessuna attività collegata"
        addNotification(
            "Pomodoro Avviato",
            "Timer Pomodoro avviato in modalità '${_pomodoroMode.value}' ($taskName)."
        )
    }

    fun pausePomodoro() {
        timerJob?.cancel()
        _pomodoroIsRunning.value = false
        addNotification("Pomodoro Sospeso", "Hai messo in pausa il timer.")
    }

    fun resetPomodoro() {
        timerJob?.cancel()
        _pomodoroIsRunning.value = false
        val originalMins = _pomodoroTotalDurationSeconds.value / 60
        _pomodoroSecondsLeft.value = originalMins * 60
    }

    private suspend fun timerFinished() {
        _pomodoroIsRunning.value = false
        val durationMins = _pomodoroTotalDurationSeconds.value / 60
        val categoryStr = _pomodoroLinkedTask.value?.category ?: when (_pomodoroMode.value) {
            "Studio" -> "Studio"
            "Pausa Breve", "Pausa Lunga" -> "Tempo Libero"
            else -> "Lavoro"
        }

        // Log session in Database!
        val session = PomodoroSessionEntity(
            taskTitle = _pomodoroLinkedTask.value?.title ?: "Timer Libero (${_pomodoroMode.value})",
            durationMinutes = durationMins,
            timestamp = System.currentTimeMillis(),
            category = categoryStr
        )
        repository.insertPomodoroSession(session)
        
        addNotification(
            "⏱️ Pomodoro Completato!",
            "Grande! Hai concluso con successo la sessione Pomodoro da $durationMins minuti per '$categoryStr'."
        )

        // Reset state
        configurePomodoro(25, "Studio", null)
    }

    // --- Gemini Advice Core Dispatcher ---
    fun loadSmartAdvisorResponse() {
        viewModelScope.launch {
            _isAiLoading.value = true
            _currentScreen.value = "consigli_ia"
            try {
                val tasksList = allTasks.value
                val total = tasksList.size
                val completed = tasksList.count { it.completed }
                val delayed = tasksList.count { it.completed && it.completedOnTime == "Ritardo" }
                val early = tasksList.count { it.completed && it.completedOnTime == "Anticipo" }
                val missed = tasksList.count { !it.completed && it.scheduledEndTime < System.currentTimeMillis() }
                
                val pSessionsList = allPomodoroSessions.value
                val pomodoroCount = pSessionsList.size

                val categoryCounts = tasksList.groupBy { it.category }.mapValues { it.value.size }

                // Collect mocked/installed app usage minutes
                val appUsageMap = installedApps.associate { it.name to it.simulatedDailyMinutes }

                val advice = GeminiAdvisorService.generateProductivityAdvice(
                    totalTasks = total,
                    completedTasks = completed,
                    delayedTasks = delayed,
                    earlyTasks = early,
                    missedTasks = missed,
                    pomodoroCount = pomodoroCount,
                    categoryCounts = categoryCounts,
                    simulatedAppUsageMinutes = appUsageMap
                )
                _aiAdvice.value = advice
            } catch (e: Exception) {
                _aiAdvice.value = "Errore durante la connessione con l'advisor della produttività di Chronos: ${e.localizedMessage}"
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    // --- Scanner for Missed tasks (Incomplete & Overdue) "Attività non Svolte" warning alert rules ---
    private fun startMockActivityWarningScanner() {
        viewModelScope.launch {
            while (true) {
                delay(120000) // check every 2 minutes
                val now = System.currentTimeMillis()
                val tasks = allTasks.value
                for (task in tasks) {
                    // If task is incomplete, already past its scheduled end time, and warning not notified yet
                    if (!task.completed && task.scheduledEndTime < now && !task.warningNotified) {
                        // Fire "Attività Non Svolta" warning alert!
                        val updated = task.copy(warningNotified = true)
                        repository.updateTask(updated)

                        addNotification(
                            "⚠️ Attività Non Svolta!",
                            "L'attività '${task.title}' era prevista entro le ore ${formatTime(task.scheduledEndTime)}. Svolgila ora per recuperare l'incombenza!",
                            task.id
                        )
                    }
                }
            }
        }
    }

    fun triggerManualWarningScanner() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            var alertTriggered = false
            val tasks = allTasks.value
            for (task in tasks) {
                if (!task.completed && task.scheduledEndTime < now && !task.warningNotified) {
                    val updated = task.copy(warningNotified = true)
                    repository.updateTask(updated)
                    addNotification(
                        "⚠️ Attività Non Svolta!",
                        "L'attività '${task.title}' era prevista entro le ore ${formatTime(task.scheduledEndTime)}. Recuperala subito!",
                        task.id
                    )
                    alertTriggered = true
                }
            }
            if (!alertTriggered) {
                addNotification(
                    "Scansione Incombenze",
                    "Nessuna nuova incombenza scaduta rilevata. Ottimo lavoro!"
                )
            }
        }
    }

    // --- Notifications Helpers ---
    private fun addNotification(title: String, message: String, taskId: Int? = null) {
        val newNotif = ChronosNotification(
            title = title,
            message = message,
            relatedTaskId = taskId
        )
        // Keep list limited to 30 items
        _notifications.value = (listOf(newNotif) + _notifications.value).take(30)
    }

    fun markNotificationAsRead(notifId: Long) {
        _notifications.value = _notifications.value.map {
            if (it.id == notifId) it.copy(isRead = true) else it
        }
    }

    fun clearAllNotifications() {
        _notifications.value = emptyList()
    }

    // Helper to format human-readable time from milliseconds
    fun formatTime(timestamp: Long): String {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        return String.format("%02d:%02d", hour, minute)
    }

    fun formatDate(timestamp: Long): String {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        return String.format("%02d/%02d/%d", day, month, year)
    }

    // --- Dynamic Category Operations ---
    fun addCategory(name: String, icon: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                val capitalized = name.trim().replaceFirstChar { it.uppercase() }
                repository.insertCategory(CategoryEntity(capitalized, icon.ifBlank { "📁" }))
                addNotification(
                    title = "Categoria Aggiunta",
                    message = "Hai creato con successo la nuova categoria '$capitalized'."
                )
            }
        }
    }

    fun deleteCategory(cat: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(cat)
            addNotification(
                title = "Categoria Rimossa",
                message = "La categoria '${cat.name}' è stata eliminata."
            )
        }
    }

    fun updateCategory(oldName: String, newName: String, newIcon: String) {
        viewModelScope.launch {
            val capitalized = newName.trim().replaceFirstChar { it.uppercase() }
            if (capitalized.isNotBlank() && oldName != capitalized) {
                // Delete the old category first
                repository.deleteCategory(CategoryEntity(oldName, ""))
                repository.insertCategory(CategoryEntity(capitalized, newIcon.ifBlank { "📁" }))

                // Cascade update to related models
                repository.updateTasksCategory(oldName, capitalized)
                repository.updatePomodoroSessionsCategory(oldName, capitalized)
                repository.updateCategoryLinksCategory(oldName, capitalized)

                addNotification(
                    title = "Categoria Rinominata",
                    message = "La categoria '$oldName' è stata aggiornata in '$capitalized'."
                )
            } else if (capitalized.isNotBlank()) {
                // Just the icon changed
                repository.insertCategory(CategoryEntity(oldName, newIcon.ifBlank { "📁" }))
                addNotification(
                    title = "Icona Categoria Modificata",
                    message = "La categoria '$oldName' ha ora l'icona '$newIcon'."
                )
            }
        }
    }
}

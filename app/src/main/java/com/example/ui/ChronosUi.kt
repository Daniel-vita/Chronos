package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.viewmodel.ChronosNotification
import com.example.viewmodel.ChronosViewModel
import com.example.viewmodel.DeviceApp
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChronosUi(
    viewModel: ChronosViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val themeColorName by viewModel.themeColor.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    var showNotificationsDrawer by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    ChronosTheme(themeName = themeColorName) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                ChronosTopBar(
                    unreadCount = notifications.count { !it.isRead },
                    currentTheme = themeColorName,
                    onThemeToggle = {
                        if (themeColorName == "Sleek") {
                            viewModel.changeThemeColor("Slate")
                        } else {
                            viewModel.changeThemeColor("Sleek")
                        }
                    },
                    onNotificationsClick = { showNotificationsDrawer = !showNotificationsDrawer },
                    onCalendarClick = { viewModel.navigateTo("calendario") },
                    onManualScanClick = { viewModel.triggerManualWarningScanner() }
                )
            },
            bottomBar = {
                ChronosBottomNavBar(
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Main screen routing
                when (currentScreen) {
                    "dashboard" -> DashboardScreen(
                        viewModel = viewModel,
                        onAddNewTask = { showAddTaskDialog = true }
                    )
                    "pomodoro" -> PomodoroScreen(viewModel = viewModel)
                    "statistiche" -> StatisticsScreen(viewModel = viewModel)
                    "consigli_ia" -> AdvisorScreen(viewModel = viewModel)
                    "impostazioni" -> SettingsScreen(viewModel = viewModel)
                    "calendario" -> CalendarScreen(
                        viewModel = viewModel,
                        onAddNewTask = { showAddTaskDialog = true }
                    )
                }

                // Notification warning alerts sliding panel
                if (showNotificationsDrawer) {
                    NotificationsPanel(
                        notifications = notifications,
                        onClose = { showNotificationsDrawer = false },
                        onMarkRead = { viewModel.markNotificationAsRead(it) },
                        onClearAll = { viewModel.clearAllNotifications() }
                    )
                }

                // Add Task Dialog
                if (showAddTaskDialog) {
                    AddTaskDialog(
                        viewModel = viewModel,
                        onDismiss = { showAddTaskDialog = false }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChronosTopBar(
    unreadCount: Int,
    currentTheme: String,
    onThemeToggle: () -> Unit,
    onNotificationsClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onManualScanClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    TopAppBar(
        title = {
            val dateStr = remember {
                try {
                    val sdf = java.text.SimpleDateFormat("EEEE, d MMM", java.util.Locale.ITALIAN)
                    sdf.format(java.util.Calendar.getInstance().time).uppercase()
                } catch (e: Exception) {
                    "LUNEDÌ, 24 GIU"
                }
            }

            Column {
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Chronos",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            // Dark Mode Sun/Moon real-time toggle
            Box(
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                    .clickable { onThemeToggle() }
                    .testTag("theme_toggle_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (currentTheme == "Sleek") "🌙" else "☀️",
                    fontSize = 18.sp
                )
            }

            // Sleek Search Circle Button (Manual scan trigger)
            Box(
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { onManualScanClick() }
                    .testTag("scan_troubles_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Rileva incombenze",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Interactive Calendar Button
            Box(
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .clickable { onCalendarClick() }
                    .testTag("calendar_nav_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Apri Calendario",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Real-time Notifications Alert badge (Bell circle matching white & deep colored elements)
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { onNotificationsClick() }
                    .testTag("notifications_alerts_button"),
                contentAlignment = Alignment.Center
            ) {
                BadgedBox(
                    badge = {
                        if (unreadCount > 0) {
                            Badge(
                                containerColor = Color(0xFFEF4444)
                            ) {
                                Text(
                                    text = unreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifiche avviso",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun ChronosBottomNavBar(
    currentScreen: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceVariant, // #F3EDF7 in Sleek theme
        tonalElevation = 4.dp,
        windowInsets = WindowInsets.navigationBars
    ) {
        val navItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer, // #1D192B / #21005D
            selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), // #49454F
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            indicatorColor = MaterialTheme.colorScheme.secondaryContainer // #E8DEF8 in Sleek theme
        )

        NavigationBarItem(
            selected = currentScreen == "dashboard",
            onClick = { onNavigate("dashboard") },
            icon = { Icon(Icons.Default.List, contentDescription = "Pianificazione") },
            label = { Text("Pianifica", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = navItemColors,
            modifier = Modifier.testTag("nav_tab_dashboard")
        )

        NavigationBarItem(
            selected = currentScreen == "pomodoro",
            onClick = { onNavigate("pomodoro") },
            icon = { Icon(Icons.Default.Build, contentDescription = "Timer Pomodoro") },
            label = { Text("Pomodoro", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = navItemColors,
            modifier = Modifier.testTag("nav_tab_pomodoro")
        )

        NavigationBarItem(
            selected = currentScreen == "statistiche",
            onClick = { onNavigate("statistiche") },
            icon = { Icon(Icons.Default.Star, contentDescription = "Statistiche") },
            label = { Text("Report", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = navItemColors,
            modifier = Modifier.testTag("nav_tab_statistiche")
        )

        NavigationBarItem(
            selected = currentScreen == "consigli_ia",
            onClick = { onNavigate("consigli_ia") },
            icon = { Icon(Icons.Default.Info, contentDescription = "Consigli IA") },
            label = { Text("Coach IA", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = navItemColors,
            modifier = Modifier.testTag("nav_tab_advisor")
        )

        NavigationBarItem(
            selected = currentScreen == "impostazioni",
            onClick = { onNavigate("impostazioni") },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Preferenze") },
            label = { Text("Preferenze", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = navItemColors,
            modifier = Modifier.testTag("nav_tab_settings")
        )
    }
}

// --- DASHBOARD / DAILY SCHEDULER VIEW ---
@Composable
fun DashboardScreen(
    viewModel: ChronosViewModel,
    onAddNewTask: () -> Unit
) {
    val tasks by viewModel.allTasks.collectAsState()
    val activePomodoroTask by viewModel.pomodoroLinkedTask.collectAsState()

    // Filtering inputs
    val selectedCat by viewModel.filterCategory.collectAsState()
    val selectedTime by viewModel.filterTime.collectAsState()
    val selectedApp by viewModel.filterApp.collectAsState()
    val selectedStatus by viewModel.filterStatus.collectAsState()
    val selectedDayOffset by viewModel.filterDayOffset.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    // Combine filters in Kotlin code securely
    val filteredTasks = tasks.filter { task ->
        val matchSearch = searchQuery.isEmpty() || task.title.contains(searchQuery, ignoreCase = true) || task.description.contains(searchQuery, ignoreCase = true)
        val matchCat = selectedCat == null || task.category == selectedCat
        val matchApp = selectedApp == null || task.linkedAppName == selectedApp
        
        val matchTime = if (selectedDayOffset != null) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, selectedDayOffset!!)
            val targetDay = calendar.get(Calendar.DAY_OF_YEAR)
            val targetYear = calendar.get(Calendar.YEAR)
            
            val taskCal = Calendar.getInstance().apply { timeInMillis = task.scheduledStartTime }
            taskCal.get(Calendar.DAY_OF_YEAR) == targetDay && taskCal.get(Calendar.YEAR) == targetYear
        } else {
            when (selectedTime) {
                "Oggi" -> {
                    val calendar = Calendar.getInstance()
                    val todayDay = calendar.get(Calendar.DAY_OF_YEAR)
                    val todayYear = calendar.get(Calendar.YEAR)
                    
                    val taskCal = Calendar.getInstance().apply { timeInMillis = task.scheduledStartTime }
                    taskCal.get(Calendar.DAY_OF_YEAR) == todayDay && taskCal.get(Calendar.YEAR) == todayYear
                }
                "Settimana" -> {
                    val oneWeekMs = 7 * 24 * 3600 * 1000L
                    val timeDiff = task.scheduledStartTime - System.currentTimeMillis()
                    timeDiff in -oneWeekMs..oneWeekMs
                }
                "Scadute/In Sospeso" -> {
                    !task.completed && task.scheduledEndTime < System.currentTimeMillis()
                }
                else -> true
            }
        }

        val matchStatus = when (selectedStatus) {
            "Completate" -> task.completed
            "Incomplete" -> !task.completed
            "In Ritardo" -> task.completed && task.completedOnTime == "Ritardo"
            "In Anticipo" -> task.completed && task.completedOnTime == "Anticipo"
            else -> true
        }

        matchSearch && matchCat && matchApp && matchTime && matchStatus
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            // Interactive Hero Header Canvas
            HeroHeaderCanvas(
                totalCount = tasks.size,
                completedCount = tasks.count { it.completed },
                missedCount = tasks.count { !it.completed && it.scheduledEndTime < System.currentTimeMillis() }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Modern Visual Calendar (Week Strip)
            Text(
                text = "Cronologia e Programmazione:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "Mostra Tutti" button
                val selectedBgAll = if (selectedDayOffset == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                val selectedFgAll = if (selectedDayOffset == null) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                
                Surface(
                    onClick = { 
                        viewModel.filterDayOffset.value = null 
                        viewModel.filterTime.value = "Tutte"
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = selectedBgAll,
                    modifier = Modifier.height(68.dp).width(72.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(6.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📅", fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Tutti", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = selectedFgAll)
                    }
                }

                // Days (Today and next 6 days)
                (0..6).forEach { offset ->
                    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, offset) }
                    val dayName = when (offset) {
                        0 -> "Oggi"
                        1 -> "Domani"
                        else -> {
                            when (cal.get(Calendar.DAY_OF_WEEK)) {
                                Calendar.SUNDAY -> "Dom"
                                Calendar.MONDAY -> "Lun"
                                Calendar.TUESDAY -> "Mar"
                                Calendar.WEDNESDAY -> "Mer"
                                Calendar.THURSDAY -> "Gio"
                                Calendar.FRIDAY -> "Ven"
                                Calendar.SATURDAY -> "Sab"
                                else -> ""
                            }
                        }
                    }
                    val dayNumber = cal.get(Calendar.DAY_OF_MONTH).toString()
                    val monthShort = when (cal.get(Calendar.MONTH)) {
                        Calendar.JANUARY -> "Gen"
                        Calendar.FEBRUARY -> "Feb"
                        Calendar.MARCH -> "Mar"
                        Calendar.APRIL -> "Apr"
                        Calendar.MAY -> "Mag"
                        Calendar.JUNE -> "Giu"
                        Calendar.JULY -> "Lug"
                        Calendar.AUGUST -> "Ago"
                        Calendar.SEPTEMBER -> "Set"
                        Calendar.OCTOBER -> "Ott"
                        Calendar.NOVEMBER -> "Nov"
                        Calendar.DECEMBER -> "Dic"
                        else -> ""
                    }
                    
                    val isSelected = selectedDayOffset == offset
                    val selectedBg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    val selectedFg = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    
                    Surface(
                        onClick = { viewModel.filterDayOffset.value = offset },
                        shape = RoundedCornerShape(16.dp),
                        color = selectedBg,
                        modifier = Modifier.height(68.dp).width(64.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(4.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(dayName, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = selectedFg)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(dayNumber, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = selectedFg)
                            Text(monthShort, fontSize = 9.sp, color = selectedFg.copy(alpha = 0.8f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search bar input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_search_field"),
                placeholder = { Text("Cerca attività...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Horizon Scroll for Chips (Category filters)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val chipColors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
                val categories by viewModel.allCategories.collectAsState()

                FilterChip(
                    selected = selectedCat == null,
                    onClick = { viewModel.filterCategory.value = null },
                    label = { Text("Tutte", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    shape = CircleShape,
                    colors = chipColors
                )
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCat == cat.name,
                        onClick = { viewModel.filterCategory.value = cat.name },
                        label = { Text("${cat.icon} ${cat.name}", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        shape = CircleShape,
                        colors = chipColors
                    )
                }
            }

            // Quick filter collapse rows
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Time window filter
                AssistChip(
                    onClick = {
                        val next = when (selectedTime) {
                            "Tutte" -> "Oggi"
                            "Oggi" -> "Settimana"
                            "Settimana" -> "Scadute/In Sospeso"
                            else -> "Tutte"
                        }
                        viewModel.filterTime.value = next
                    },
                    label = { Text("Tempo: $selectedTime") },
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )

                // Completion status filter
                AssistChip(
                    onClick = {
                        val next = when (selectedStatus) {
                            "Tutte" -> "Completate"
                            "Completate" -> "Incomplete"
                            "Incomplete" -> "In Ritardo"
                            "In Ritardo" -> "In Anticipo"
                            else -> "Tutte"
                        }
                        viewModel.filterStatus.value = next
                    },
                    label = { Text("Stato: $selectedStatus") },
                    leadingIcon = { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )

                // App-linkage filter
                if (selectedApp != null) {
                    InputChip(
                        selected = true,
                        onClick = { viewModel.filterApp.value = null },
                        label = { Text("App: $selectedApp") },
                        trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Scancella", modifier = Modifier.size(16.dp)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Task list or Empty state
            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🕵️‍♂️ Nessuna attività trovata",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Modifica i filtri o premi + per programmare un nuovo blocco di tempo.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.secondary.copy(0.7f),
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    filteredTasks.forEach { task ->
                        androidx.compose.runtime.key(task.id) {
                            TaskCard(
                                task = task,
                                isActivePomodoro = activePomodoroTask?.id == task.id,
                                onToggleCompletion = { viewModel.toggleTaskCompletionDirect(task.id) },
                                onDelete = { viewModel.deleteTask(task) },
                                onStartPomodoro = {
                                    viewModel.configurePomodoro(25, "Studio", task)
                                    viewModel.navigateTo("pomodoro")
                                },
                                onSaveDelayResult = { delayMins ->
                                    viewModel.completeTask(task.id, delayMins)
                                },
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        // Floating Action Button to Add new Time-blocks
        FloatingActionButton(
            onClick = onAddNewTask,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
                .testTag("add_task_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Pianifica attività")
        }
    }
}

@Composable
fun HeroHeaderCanvas(
    totalCount: Int,
    completedCount: Int,
    missedCount: Int
) {
    val completionPercent = if (totalCount > 0) (completedCount.toFloat() / totalCount.toFloat() * 100f).toInt() else 84
    val cardBg = MaterialTheme.colorScheme.primaryContainer
    val cardText = MaterialTheme.colorScheme.onPrimaryContainer
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(26.dp), // rounded-3xl
        colors = CardDefaults.cardColors(
            containerColor = cardBg
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Row 1: Focus Score text and Sparkbar graph
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Focus Score",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = cardText.copy(0.8f)
                    )
                    Text(
                        text = "$completionPercent%",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp,
                            letterSpacing = (-1).sp
                        ),
                        color = cardText
                    )
                }

                // Sparkbar staggered bars visual module (90.dp wide, 44.dp high)
                Row(
                    modifier = Modifier
                        .width(90.dp)
                        .height(44.dp)
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Bar heights mapping matching the original HTML heights: 30%, 60%, 80%, 45%, 100%
                    val barHeights = listOf(0.30f, 0.60f, 0.80f, 0.45f, 1.00f)
                    val opacities = listOf(0.20f, 0.40f, 0.70f, 0.90f, 1.00f)

                    barHeights.forEachIndexed { index, heightMultiplier ->
                        val finalHeight = heightMultiplier * 44f
                        val activeColor = primaryColor.copy(alpha = opacities[index])
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(finalHeight.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(activeColor)
                        )
                    }
                }
            }

            // Row 2: Tips banner nested box
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(primaryColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info, // tips updates icon
                            contentDescription = "Consiglio",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Text(
                        text = "CONSIGLIO: Riduci l'uso di Instagram (-20m) per aumentare la produttività del 12%.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 15.sp,
                        color = cardText
                    )
                }
            }
        }
    }
}

@Composable
fun TaskCard(
    task: TaskEntity,
    isActivePomodoro: Boolean,
    onToggleCompletion: () -> Unit,
    onDelete: () -> Unit,
    onStartPomodoro: () -> Unit,
    onSaveDelayResult: (Int) -> Unit,
    viewModel: ChronosViewModel
) {
    var showDelayPicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_item_${task.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.completed) {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = if (isActivePomodoro) 2.dp else 1.dp,
            color = if (isActivePomodoro) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Main Content Row (Left split, middle info, right action target)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Part 1: Left Split (Time and Urgency tag)
                Column(
                    modifier = Modifier.width(60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val timeStr = remember {
                        viewModel.formatTime(task.scheduledStartTime)
                    }
                    Text(
                        text = timeStr,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    val dateLabel = remember {
                        val taskCal = Calendar.getInstance().apply { timeInMillis = task.scheduledStartTime }
                        val today = Calendar.getInstance()
                        if (taskCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) &&
                            taskCal.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
                            ""
                        } else {
                            val tom = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                            if (taskCal.get(Calendar.DAY_OF_YEAR) == tom.get(Calendar.DAY_OF_YEAR) &&
                                taskCal.get(Calendar.YEAR) == tom.get(Calendar.YEAR)) {
                                "Domani"
                            } else {
                                val day = taskCal.get(Calendar.DAY_OF_MONTH)
                                val month = taskCal.get(Calendar.MONTH) + 1
                                String.format("%02d/%02d", day, month)
                            }
                        }
                    }
                    if (dateLabel.isNotEmpty()) {
                        Text(
                            text = dateLabel,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                    }
                    
                    val (pColor, pText) = when (task.priority) {
                        "Alta" -> Color(0xFFEF4444) to "URGENT"
                        "Media" -> Color(0xFFFBBF24) to "MEDIUM"
                        else -> Color(0xFF94A3B8) to "LATER"
                    }
                    Text(
                        text = pText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = pColor,
                        maxLines = 1
                    )
                }

                // Vertical Divider dividing left and middle part
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(44.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )

                // Part 2: Middle Info (Title, description, badges)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            textDecoration = if (task.completed) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                        ),
                        color = if (task.completed) MaterialTheme.colorScheme.onSurface.copy(0.5f) else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (task.description.isNotEmpty()) {
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Badges flow (Categories, apps, attachments)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        // Category tag styled like a customized badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "#${task.category}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        if (!task.linkedAppName.isNullOrEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share, // link or share symbol
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f),
                                    modifier = Modifier.size(9.dp)
                                )
                                Text(
                                    text = task.linkedAppName!!,
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f)
                                )
                            }
                        }
                    }
                }

                // Part 3: Right Action play/check indicator
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (task.completed) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                        .border(
                            width = 2.dp,
                            color = if (task.completed) Color.Transparent else MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                        .clickable {
                            if (task.completed) {
                                onToggleCompletion()
                            } else {
                                showDelayPicker = true
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val iconVector = if (task.completed) Icons.Default.Check else Icons.Default.PlayArrow
                    val iconColor = if (task.completed) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                    Icon(
                        imageVector = iconVector,
                        contentDescription = "Cambia completamento",
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Compact bottom actions/status row
            if (!task.completed || task.timeDifferenceMinutes != 0) {
                Spacer(modifier = Modifier.height(10.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Status: Delay / Completed indicator or Pomodoro label
                    Box(modifier = Modifier.weight(1f)) {
                        if (task.completed) {
                            val labelAndColor = when (task.completedOnTime) {
                                "Ritardo" -> "⏱️ +${task.timeDifferenceMinutes}m Ritardo" to Color(0xFFFBBF24)
                                "Anticipo" -> "⏱️ ${task.timeDifferenceMinutes}m Anticipo" to Color(0xFF10B981)
                                else -> "⏱️ In Orario" to MaterialTheme.colorScheme.primary
                            }
                            Text(
                                text = labelAndColor.first,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = labelAndColor.second
                            )
                        } else if (task.scheduledEndTime < System.currentTimeMillis()) {
                            Text(
                                text = "⚠️ Non Svolta",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }

                    // Right actionable triggers: Delete & Pomodoro active link
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!task.completed) {
                            TextButton(
                                onClick = onStartPomodoro,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("⏱️ Pomodoro", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Rimuovi",
                                tint = Color(0xFFEF4444).copy(0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            } else {
                // Just keep delete action for completed tasks
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Rimuovi",
                            tint = Color(0xFFEF4444).copy(0.5f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }

    // Completion delay picker dialog
    if (showDelayPicker) {
        Dialog(onDismissRequest = { showDelayPicker = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Con quale tempistica hai concluso?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Specifica se hai terminato l'attività con ritardo (minuti positivi) o in anticipo (minuti negativi).",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    var deltaMinutes by remember { mutableStateOf(0f) }

                    Text(
                        text = when {
                            deltaMinutes.toInt() > 0 -> "🔴 Ritardo: +${deltaMinutes.toInt()} min"
                            deltaMinutes.toInt() < 0 -> "🟢 Anticipo: ${deltaMinutes.toInt()} min"
                            else -> "🔵 Perfettamente in Orario"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            deltaMinutes.toInt() > 0 -> Color(0xFFFBBF24)
                            deltaMinutes.toInt() < 0 -> Color(0xFF10B981)
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )

                    Slider(
                        value = deltaMinutes,
                        onValueChange = { deltaMinutes = it },
                        valueRange = -60f..60f,
                        steps = 24,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showDelayPicker = false }) {
                            Text("Annulla")
                        }
                        Button(
                            onClick = {
                                onSaveDelayResult(deltaMinutes.toInt())
                                showDelayPicker = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Concludi")
                        }
                    }
                }
            }
        }
    }
}

// --- POMODORO TIMER SCREEN ---
@Composable
fun PomodoroScreen(viewModel: ChronosViewModel) {
    val timeLeft by viewModel.pomodoroSecondsLeft.collectAsState()
    val totalTime by viewModel.pomodoroTotalDurationSeconds.collectAsState()
    val isRunning by viewModel.pomodoroIsRunning.collectAsState()
    val currentMode by viewModel.pomodoroMode.collectAsState()
    val linkedTask by viewModel.pomodoroLinkedTask.collectAsState()

    val progress = if (totalTime > 0) timeLeft.toFloat() / totalTime.toFloat() else 1f
    val minutes = timeLeft / 60
    val seconds = timeLeft % 60

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Timer Pomodoro",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )

        // Mode selectors
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Studio" to 25, "Pausa Breve" to 5, "Pausa Lunga" to 15).forEach { (m, mins) ->
                InputChip(
                    selected = currentMode == m,
                    onClick = {
                        viewModel.configurePomodoro(mins, m, if (m == "Studio") linkedTask else null)
                    },
                    label = { Text(m) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Large Custom Radial Timer Canvas
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(240.dp)
        ) {
            Canvas(modifier = Modifier.size(230.dp)) {
                // Background Track
                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx())
                )
                
                // Active Glowing Progress Ring
                drawArc(
                    color = primaryColor,
                    startAngle = -90f,
                    sweepAngle = (progress * 360f),
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format("%02d:%02d", minutes, seconds),
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isRunning) "Concentrati!" else "Timer Sospeso",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        // Minutes Increment / Decrement Controllers
        if (!isRunning) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(
                    onClick = {
                        val newMins = maxOf(1, (totalTime / 60) - 5)
                        viewModel.configurePomodoro(newMins, currentMode, linkedTask)
                    },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
                ) {
                    Text("-5m", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                Text(
                    text = "${totalTime / 60} minuti di sessione",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                IconButton(
                    onClick = {
                        val newMins = minOf(120, (totalTime / 60) + 5)
                        viewModel.configurePomodoro(newMins, currentMode, linkedTask)
                    },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
                ) {
                    Text("+5m", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Linked task focus box
        if (linkedTask != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🎯", fontSize = 24.sp)
                    Column {
                        Text(
                            text = "Focalizzazione Attiva",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = linkedTask!!.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Controls
        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (isRunning) viewModel.pausePomodoro() else viewModel.startPomodoro()
                },
                modifier = Modifier
                    .weight(2f)
                    .height(48.dp)
                    .testTag("pomodoro_start_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(if (isRunning) "Metti in Pausa" else "Avvia Timer")
            }

            Button(
                onClick = { viewModel.resetPomodoro() },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Azzera")
            }
        }
    }
}

// --- REPORTS / STATISTICS SCREEN ---
@Composable
fun StatisticsScreen(viewModel: ChronosViewModel) {
    val tasks by viewModel.allTasks.collectAsState()
    val pomodoroSessions by viewModel.allPomodoroSessions.collectAsState()

    val completed = tasks.count { it.completed }
    val total = tasks.size
    val rate = if (total > 0) (completed.toFloat() / total * 100f).toInt() else 0

    val onTimeCount = tasks.count { it.completed && it.completedOnTime == "In Orario" }
    val earlyCount = tasks.count { it.completed && it.completedOnTime == "Anticipo" }
    val delayedCount = tasks.count { it.completed && it.completedOnTime == "Ritardo" }
    val missedCount = tasks.count { !it.completed && it.scheduledEndTime < System.currentTimeMillis() }

    val totalPomodoroMinutes = pomodoroSessions.sumOf { it.durationMinutes }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Statistiche di Produttività",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        )

        // General status widgets row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ElevatedCard(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Completate", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                    Text("$completed / $total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    Text("$rate% Ratio", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                }
            }

            ElevatedCard(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Minuti Focus", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                    Text("$totalPomodoroMinutes m", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    Text("${pomodoroSessions.size} Sessioni", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }

        // Custom Completions Status Bar Chart drawn in Canvas
        Text(
            text = "Rapporto Tempistiche di Conclusione",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        val barColors = listOf(
            Color(0xFF10B981), // Anticipo
            Color(0xFF3B82F6), // In Orario
            Color(0xFFFBBF24), // Ritardo
            Color(0xFFEF4444)  // Non Svolte
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Drawing Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    val maxVal = maxOf(5, earlyCount, onTimeCount, delayedCount, missedCount).toFloat()
                    val labels = listOf("Anticipo", "In Orario", "Ritardo", "Non Svolte")
                    val counts = listOf(earlyCount, onTimeCount, delayedCount, missedCount)

                    val spacingX = size.width / 4f
                    val barWidth = 32.dp.toPx()

                    for (i in 0..3) {
                        val rawHeight = (counts[i].toFloat() / maxVal) * 100.dp.toPx()
                        val barHeight = maxOf(8.dp.toPx(), rawHeight) // minimum viz height
                        val barX = (i * spacingX) + (spacingX / 2f) - (barWidth / 2f)
                        val barY = size.height - 20.dp.toPx() - barHeight

                        // Draw bar
                        drawRoundRect(
                            color = barColors[i],
                            topLeft = Offset(barX, barY),
                            size = Size(barWidth, barHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                        )

                        // Draw count label on top of bar
                        // (We'll count on standard text measurements or just let it look beautiful)
                    }
                }
                
                // Labels Legends Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val legends = listOf("Anticipo ($earlyCount)", "In Orario ($onTimeCount)", "Ritardo ($delayedCount)", "Mancate ($missedCount)")
                    legends.forEachIndexed { i, label ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(barColors[i]))
                            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }

        // Category metrics
        Text(
            text = "Attività per Macrocategoria",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        val categoriesList = viewModel.allCategories.collectAsState().value
        categoriesList.forEach { catEntity ->
            val cat = catEntity.name
            val count = tasks.count { it.category == cat }
            val comp = tasks.count { it.category == cat && it.completed }
            val p = if (count > 0) comp.toFloat() / count.toFloat() else 0f

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${catEntity.icon} $cat", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        Text("$comp svolte su $count pianificate", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                    
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(54.dp)) {
                        CircularProgressIndicator(
                            progress = { p },
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeWidth = 4.dp
                        )
                        Text("${(p * 100).toInt()}%", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- COACH IA ADVISOR SCREEN ---
@Composable
fun AdvisorScreen(viewModel: ChronosViewModel) {
    val aiAdvice by viewModel.aiAdvice.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Coach Produttività IA",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Chronos analizza l'andamento del tuo dispositivo, le app utilizzate e i ritardi accumulati per generare consigli mirati.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        // Simulated daily device metrics
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "📊 Statistiche Utilizzo Dispositivo (Simulato)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                
                viewModel.installedApps.take(4).forEach { app ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(app.name, fontSize = 12.sp)
                        Text("${app.simulatedDailyMinutes} minuti oggi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Button(
            onClick = { viewModel.loadSmartAdvisorResponse() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("run_ai_analysis_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            enabled = !isAiLoading
        ) {
            if (isAiLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analisi in corso...")
            } else {
                Text("Genera Consigli Strategici con IA")
            }
        }

        // Display Generated Advice
        if (aiAdvice != null) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "✨ Chronos Coach Coaching",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // Displaying and styling formatted blocks
                    Text(
                        text = aiAdvice!!,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }
            }
        } else if (!isAiLoading) {
            // Placeholder empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Premi il pulsante sopra per far generare a Chronos i primi consigli personalizzati sulla produttività.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary.copy(0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    }
}

// --- SETTINGS / PREFERENCES SCREEN ---
@Composable
fun SettingsScreen(viewModel: ChronosViewModel) {
    val themeColorName by viewModel.themeColor.collectAsState()
    val categoryLinks by viewModel.allCategoryLinks.collectAsState()
    val categoriesList = viewModel.allCategories.collectAsState().value

    var showAppConnector by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Personalizzazione & App",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )

        // Custom theme color pickers
        Text(
            text = "Colore Tema Attivo",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val themes = listOf(
                "Sleek" to Color(0xFF6750A4),
                "Slate" to Color(0xFF64748B),
                "Teal" to Color(0xFF0D9488),
                "Ocean" to Color(0xFF0284C7),
                "Sunset" to Color(0xFFD97706)
            )

            themes.forEach { (name, color) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color)
                        .clickable { viewModel.changeThemeColor(name) }
                        .border(
                            width = if (themeColorName == name) 3.dp else 0.dp,
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- GESTIONE CATEGORIE ---
        var showAddCategoryDialog by remember { mutableStateOf(false) }
        var showEditCategoryDialog by remember { mutableStateOf<CategoryEntity?>(null) }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Gestione Categorie",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { showAddCategoryDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text("+ Aggiungi", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Text(
            text = "Personalizza le categorie del planner. Rinominando o modificando una categoria, tutte le attività e le sessioni di focus correlate verranno aggiornate automaticamente.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (categoriesList.isEmpty()) {
                    Text(
                        text = "Nessuna categoria personalizzata creata.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                } else {
                    categoriesList.forEach { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = cat.icon,
                                    fontSize = 20.sp,
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                                        .padding(8.dp)
                                )
                                Text(
                                    text = cat.name,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp
                                )
                            }
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = { showEditCategoryDialog = cat },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Modifica categoria",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                
                                IconButton(
                                    onClick = { viewModel.deleteCategory(cat) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Elimina categoria",
                                        tint = Color(0xFFEF4444).copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Category Dialog
        if (showAddCategoryDialog) {
            var newCatName by remember { mutableStateOf("") }
            var newCatIcon by remember { mutableStateOf("💼") }
            
            Dialog(onDismissRequest = { showAddCategoryDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Aggiungi Categoria",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        OutlinedTextField(
                            value = newCatName,
                            onValueChange = { newCatName = it },
                            label = { Text("Nome Categoria") },
                            placeholder = { Text("E.g., Sport, Finanze") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Text("Seleziona o digita un'icona (Emoji):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        
                        // Let's offer some quick choice emojis
                        val emojis = listOf("💼", "🎓", "🍀", "🍎", "🛒", "🧘", "✈️", "🏠", "💻", "🎵", "🎨", "🚀", "🏀")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            emojis.forEach { emoji ->
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (newCatIcon == emoji) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                                        .clickable { newCatIcon = emoji }
                                        .border(
                                            width = if (newCatIcon == emoji) 2.dp else 0.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(emoji, fontSize = 18.sp)
                                }
                            }
                        }

                        // Also let users type custom emoji if they desire
                        OutlinedTextField(
                            value = newCatIcon,
                            onValueChange = { if (it.length <= 4) newCatIcon = it },
                            label = { Text("Icona personalizzata (Max 2 Caratteri)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showAddCategoryDialog = false }) {
                                Text("Annulla")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newCatName.isNotBlank()) {
                                        viewModel.addCategory(newCatName, newCatIcon)
                                        showAddCategoryDialog = false
                                    }
                                },
                                enabled = newCatName.isNotBlank()
                            ) {
                                Text("Aggiungi")
                            }
                        }
                    }
                }
            }
        }

        // Edit Category Dialog
        showEditCategoryDialog?.let { currentCat ->
            var editCatName by remember { mutableStateOf(currentCat.name) }
            var editCatIcon by remember { mutableStateOf(currentCat.icon) }
            
            Dialog(onDismissRequest = { showEditCategoryDialog = null }) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Modifica Categoria",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        OutlinedTextField(
                            value = editCatName,
                            onValueChange = { editCatName = it },
                            label = { Text("Nome Categoria") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Text("Seleziona o digita un'icona (Emoji):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        
                        val emojis = listOf("💼", "🎓", "🍀", "🍎", "🛒", "🧘", "✈️", "🏠", "💻", "🎵", "🎨", "🚀", "🏀")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            emojis.forEach { emoji ->
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (editCatIcon == emoji) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                                        .clickable { editCatIcon = emoji }
                                        .border(
                                            width = if (editCatIcon == emoji) 2.dp else 0.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(emoji, fontSize = 18.sp)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = editCatIcon,
                            onValueChange = { if (it.length <= 4) editCatIcon = it },
                            label = { Text("Icona personalizzata (Max 2 Caratteri)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showEditCategoryDialog = null }) {
                                Text("Annulla")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (editCatName.isNotBlank()) {
                                        viewModel.updateCategory(
                                            oldName = currentCat.name,
                                            newName = editCatName,
                                            newIcon = editCatIcon
                                        )
                                        showEditCategoryDialog = null
                                    }
                                },
                                enabled = editCatName.isNotBlank()
                            ) {
                                Text("Salva")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Macro Cat App linkages
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Collegamenti App di Dispositivo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { showAppConnector = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Text("Associa Logo App", fontSize = 11.sp)
            }
        }

        Text(
            text = "Associa le applicazioni del tuo dispositivo alle macrocategorie (Lavoro, Studio, Tempo Libero) per permettere a Chronos di rilevarne l'incidenza durante il focus.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )

        if (categoryLinks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nessun collegamento app attivo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary.copy(0.7f)
                )
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    categoryLinks.forEach { link ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(link.appName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("🔗 Collegate a: ${link.category}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                            IconButton(onClick = { viewModel.unlinkCategoryApp(link) }) {
                                Icon(Icons.Default.Close, contentDescription = "Scollega", tint = Color.Red.copy(0.7f))
                            }
                        }
                    }
                }
            }
        }
    }

    // App Linkage catalog dialog
    if (showAppConnector) {
        Dialog(onDismissRequest = { showAppConnector = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Associa App a Categoria",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    var selectedCategoryForLink by remember(categoriesList) {
                        mutableStateOf(categoriesList.firstOrNull()?.name ?: "Lavoro")
                    }

                    Text("Scegli la Macrocategoria:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categoriesList.forEach { catEntity ->
                            val cat = catEntity.name
                            InputChip(
                                selected = selectedCategoryForLink == cat,
                                onClick = { selectedCategoryForLink = cat },
                                label = { Text("${catEntity.icon} $cat") }
                            )
                        }
                    }

                    Text("Seleziona l'applicazione da associare:", fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    LazyColumn(
                        modifier = Modifier.height(240.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(viewModel.installedApps) { app ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.linkAppToCategory(selectedCategoryForLink, app)
                                        showAppConnector = false
                                    }
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(app.iconDescription, fontSize = 20.sp)
                                Column {
                                    Text(app.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text(app.packageName, fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        }
                    }
                    
                    TextButton(
                        onClick = { showAppConnector = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Chiudi")
                    }
                }
            }
        }
    }
}

// --- ADD TASK / PLAY TIME-BLOCK DIALOG ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    viewModel: ChronosViewModel,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val categoriesList = viewModel.allCategories.collectAsState().value
    var category by remember(categoriesList) {
        mutableStateOf(categoriesList.firstOrNull()?.name ?: "Lavoro")
    }
    var priority by remember { mutableStateOf("Media") }
    var tagText by remember { mutableStateOf("") }
    
    val activeDayOffset = viewModel.filterDayOffset.collectAsState().value ?: 0
    var selectedDayOffset by remember { mutableStateOf(activeDayOffset) }

    // Sliding time defaults
    var startHourSelected by remember { mutableFloatStateOf(9f) } // default 9:00 AM
    var durationMinutesSelected by remember { mutableFloatStateOf(60f) } // default 60 minutes
    
    // Checkboxes
    var simulateDocAttachment by remember { mutableStateOf(false) }
    var simulateImageAttachment by remember { mutableStateOf(false) }

    var selectedLinkedAppName by remember { mutableStateOf<String?>(null) }
    var selectedLinkedAppPackage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Pianifica Nuovo Blocco",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nome Attività *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_task_title_field"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                )

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrizione o Dettagli") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                // Category selector
                Text("Macrocategoria:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categoriesList.forEach { catEntity ->
                        val cat = catEntity.name
                        InputChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text("${catEntity.icon} $cat") }
                        )
                    }
                }

                // Programming Day Selector
                Text("Giorno di Programmazione:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    (0..6).forEach { offset ->
                        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, offset) }
                        val label = when (offset) {
                            0 -> "Oggi"
                            1 -> "Domani"
                            else -> {
                                val dayOfWeek = when (cal.get(Calendar.DAY_OF_WEEK)) {
                                    Calendar.SUNDAY -> "Dom"
                                    Calendar.MONDAY -> "Lun"
                                    Calendar.TUESDAY -> "Mar"
                                    Calendar.WEDNESDAY -> "Mer"
                                    Calendar.THURSDAY -> "Gio"
                                    Calendar.FRIDAY -> "Ven"
                                    Calendar.SATURDAY -> "Sab"
                                    else -> ""
                                }
                                val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
                                val month = cal.get(Calendar.MONTH) + 1
                                "$dayOfWeek $dayOfMonth/$month"
                            }
                        }
                        InputChip(
                            selected = selectedDayOffset == offset,
                            onClick = { selectedDayOffset = offset },
                            label = { Text(label) }
                        )
                    }
                }

                // Priority selector
                Text("Urgenza / Priorità:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Alta", "Media", "Bassa").forEach { prio ->
                        val chipColor = when (prio) {
                            "Alta" -> Color(0xFFEF4444)
                            "Media" -> Color(0xFFFBBF24)
                            else -> Color(0xFF94A3B8)
                        }
                        ElevatedAssistChip(
                            onClick = { priority = prio },
                            label = { 
                                Text(
                                    prio, 
                                    fontWeight = if (priority == prio) FontWeight.Bold else FontWeight.Normal,
                                    color = if (priority == prio) chipColor else MaterialTheme.colorScheme.onSurface
                                ) 
                            },
                            modifier = Modifier.weight(1f),
                            colors = AssistChipDefaults.elevatedAssistChipColors(
                                containerColor = if (priority == prio) chipColor.copy(0.12f) else MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }

                // Hours and slider representation
                Text(
                    text = String.format("Orario d'Inizio: %02d:00", startHourSelected.toInt()),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Slider(
                    value = startHourSelected,
                    onValueChange = { startHourSelected = it },
                    valueRange = 0f..23f,
                    steps = 23
                )

                Text(
                    text = "Durata stimata: ${durationMinutesSelected.toInt()} minuti",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Slider(
                    value = durationMinutesSelected,
                    onValueChange = { durationMinutesSelected = it },
                    valueRange = 15f..180f,
                    steps = 11
                )

                // Tags
                OutlinedTextField(
                    value = tagText,
                    onValueChange = { tagText = it },
                    label = { Text("Tag (separati da virgola)") },
                    placeholder = { Text("es: importante, fisica, sprint") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Attachments simulators
                Text("Crea Allegati Simulati (File Fisico):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = simulateDocAttachment, onCheckedChange = { simulateDocAttachment = it })
                        Text("📎 schema_progetto.pdf", fontSize = 11.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = simulateImageAttachment, onCheckedChange = { simulateImageAttachment = it })
                        Text("🖼️ screen_mockup.png", fontSize = 11.sp)
                    }
                }

                // Linked App selector dropdown (simulated)
                Text("Associa App Installata al task:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    InputChip(
                        selected = selectedLinkedAppName == null,
                        onClick = {
                            selectedLinkedAppName = null
                            selectedLinkedAppPackage = null
                        },
                        label = { Text("Nessuna") }
                    )

                    viewModel.installedApps.forEach { app ->
                        InputChip(
                            selected = selectedLinkedAppName == app.name,
                            onClick = {
                                selectedLinkedAppName = app.name
                                selectedLinkedAppPackage = app.packageName
                            },
                            label = { Text(app.name) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annulla")
                    }

                    Button(
                        onClick = {
                            if (title.isNotEmpty()) {
                                val calendar = Calendar.getInstance()
                                // Add selected day offset
                                calendar.add(Calendar.DAY_OF_YEAR, selectedDayOffset)
                                calendar.set(Calendar.HOUR_OF_DAY, startHourSelected.toInt())
                                calendar.set(Calendar.MINUTE, 0)
                                calendar.set(Calendar.SECOND, 0)
                                calendar.set(Calendar.MILLISECOND, 0)
                                val finalStart = calendar.timeInMillis
                                val finalEnd = finalStart + (durationMinutesSelected.toLong() * 60 * 1000L)
                                val finalDeadline = finalEnd + (4 * 3600 * 1000L) // deadline 4 hrs after

                                val attachmentsList = mutableListOf<String>()
                                if (simulateDocAttachment) attachmentsList.add("schema_progetto.pdf")
                                if (simulateImageAttachment) attachmentsList.add("screen_mockup.png")

                                viewModel.addTask(
                                    title = title,
                                    description = description,
                                    category = category,
                                    priority = priority,
                                    deadline = finalDeadline,
                                    startTime = finalStart,
                                    endTime = finalEnd,
                                    tags = tagText,
                                    attachments = attachmentsList.joinToString(","),
                                    linkedAppName = selectedLinkedAppName,
                                    linkedAppPackage = selectedLinkedAppPackage
                                )
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("save_task_button")
                    ) {
                        Text("Salva")
                    }
                }
            }
        }
    }
}

// --- NOTIFICATION WARNERS LIST SLIDING CARD DRAWERS ---
@Composable
fun NotificationsPanel(
    notifications: List<ChronosNotification>,
    onClose: () -> Unit,
    onMarkRead: (Long) -> Unit,
    onClearAll: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(0.6f))
            .clickable { onClose() }
    ) {
        // Lateral drawer sliding card
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
                .clickable(enabled = false) {}, // prevent click-through dismissal
            shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Notifiche & Avvisi",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Chiudi")
                    }
                }

                Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nessun avviso o notifica di scadenza attiva.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onClearAll) {
                            Text("Pulisci Tutte", color = Color.Red.copy(0.8f))
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(notifications) { notif ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (notif.isRead) MaterialTheme.colorScheme.surfaceVariant.copy(0.4f) else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onMarkRead(notif.id) }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = notif.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (notif.title.contains("⚠️")) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary
                                        )
                                        if (!notif.isRead) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = notif.message,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarScreen(
    viewModel: ChronosViewModel,
    onAddNewTask: () -> Unit
) {
    val themeColorName by viewModel.themeColor.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val activePomodoroTask by viewModel.pomodoroLinkedTask.collectAsState()

    var currentMonthCal by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDateCal by remember { mutableStateOf(Calendar.getInstance()) }

    val monthName = remember(currentMonthCal) {
        val sdf = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.ITALIAN)
        sdf.format(currentMonthCal.time).uppercase()
    }

    val daysInMonthList = remember(currentMonthCal) {
        val cal = currentMonthCal.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val leadingSpaces = when (firstDayOfWeek) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }

        val list = mutableListOf<Calendar?>()
        for (i in 0 until leadingSpaces) {
            list.add(null)
        }
        for (day in 1..daysInMonth) {
            val entryCal = currentMonthCal.clone() as Calendar
            entryCal.set(Calendar.DAY_OF_MONTH, day)
            list.add(entryCal)
        }
        list
    }

    val isDark = themeColorName != "Sleek"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Calendario & Programmazione",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )

        // Theme display modes (Light/Dark Switch toggle right on the calendar screen as requested)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(if (isDark) "🌙" else "☀️", fontSize = 22.sp)
                    Column {
                        Text(
                            text = "Visualizzazione Tema Scuro",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Interfaccia scura riposante per la sera",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                Switch(
                    checked = isDark,
                    onCheckedChange = { useDark ->
                        if (useDark) {
                            viewModel.changeThemeColor("Slate")
                        } else {
                            viewModel.changeThemeColor("Sleek")
                        }
                    }
                )
            }
        }

        // Month Selector: Navigation and header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Header Nav Month
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        val prev = currentMonthCal.clone() as Calendar
                        prev.add(Calendar.MONTH, -1)
                        currentMonthCal = prev
                    }) {
                        Text("◀", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    }

                    Text(
                        text = monthName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(onClick = {
                        val next = currentMonthCal.clone() as Calendar
                        next.add(Calendar.MONTH, 1)
                        currentMonthCal = next
                    }) {
                        Text("▶", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Days of the week headers
                val weekDays = listOf("LUN", "MAR", "MER", "GIO", "VEN", "SAB", "DOM")
                Row(modifier = Modifier.fillMaxWidth()) {
                    weekDays.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Month Grid
                val chunkedDays = daysInMonthList.chunked(7)
                chunkedDays.forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        week.forEach { dayCal ->
                            if (dayCal == null) {
                                Spacer(modifier = Modifier.weight(1f))
                            } else {
                                val dNum = dayCal.get(Calendar.DAY_OF_MONTH)
                                val isSelected = dayCal.get(Calendar.YEAR) == selectedDateCal.get(Calendar.YEAR) &&
                                        dayCal.get(Calendar.DAY_OF_YEAR) == selectedDateCal.get(Calendar.DAY_OF_YEAR)

                                val isToday = dayCal.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR) &&
                                        dayCal.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

                                val hasTasks = allTasks.any { task ->
                                    val taskCal = Calendar.getInstance().apply { timeInMillis = task.scheduledStartTime }
                                    taskCal.get(Calendar.YEAR) == dayCal.get(Calendar.YEAR) &&
                                            taskCal.get(Calendar.DAY_OF_YEAR) == dayCal.get(Calendar.DAY_OF_YEAR)
                                }

                                val bg = when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                    else -> Color.Transparent
                                }

                                val fg = when {
                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                    isToday -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurface
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(bg)
                                        .clickable { selectedDateCal = dayCal }
                                        .border(
                                            width = if (isToday && !isSelected) 1.dp else 0.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = dNum.toString(),
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = fg
                                        )
                                        if (hasTasks) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondary)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Active day tasks Header and add-button
        val sdfDate = java.text.SimpleDateFormat("EEEE d MMMM", java.util.Locale.ITALIAN)
        val selectedDateStr = sdfDate.format(selectedDateCal.time)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Attività del Giorno",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = selectedDateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // Button to add custom task specifically for this day
            Button(
                onClick = {
                    val today = Calendar.getInstance()
                    val diff = selectedDateCal.get(Calendar.DAY_OF_YEAR) - today.get(Calendar.DAY_OF_YEAR) + 
                               (selectedDateCal.get(Calendar.YEAR) - today.get(Calendar.YEAR)) * 365
                    viewModel.filterDayOffset.value = diff
                    onAddNewTask()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("+ Aggiungi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Filtered tasks for the selected date
        val daysTasks = remember(allTasks, selectedDateCal) {
            allTasks.filter { task ->
                val taskCal = Calendar.getInstance().apply { timeInMillis = task.scheduledStartTime }
                taskCal.get(Calendar.YEAR) == selectedDateCal.get(Calendar.YEAR) &&
                        taskCal.get(Calendar.DAY_OF_YEAR) == selectedDateCal.get(Calendar.DAY_OF_YEAR)
            }
        }

        if (daysTasks.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("📅", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ancora nessun blocco temporale programmato",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Usa il pulsante Aggiungi per pianificare un'attività di studio, lavoro o tempo libero per questa giornata.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                daysTasks.forEach { task ->
                    androidx.compose.runtime.key(task.id) {
                        TaskCard(
                            task = task,
                            isActivePomodoro = activePomodoroTask?.id == task.id,
                            onToggleCompletion = { viewModel.toggleTaskCompletionDirect(task.id) },
                            onDelete = { viewModel.deleteTask(task) },
                            onStartPomodoro = {
                                viewModel.configurePomodoro(25, "Studio", task)
                                viewModel.navigateTo("pomodoro")
                            },
                            onSaveDelayResult = { delayMins ->
                                viewModel.completeTask(task.id, delayMins)
                            },
                            viewModel = viewModel
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Synchronize helper directly in Calendar page
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔄", fontSize = 24.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sincronizza Google/Apple Calendar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Importa i tuoi impegni esterni e organizza i blocchi temporali di Chronos.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
                Button(
                    onClick = { viewModel.syncExternalCalendar() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Sincronizza", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}


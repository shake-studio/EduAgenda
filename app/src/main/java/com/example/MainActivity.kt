package com.example

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Task
import com.example.ui.TaskViewModel
import com.example.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private val viewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                EduAgendaApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EduAgendaApp(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tasks by viewModel.tasksState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }

    // Manage POST_NOTIFICATIONS runtime permission on Android 13+
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "Notificações ativadas com sucesso!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Os lembretes funcionarão mesmo sem som/popup no sistema.", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp).padding(end = 6.dp)
                            )
                            Text(
                                text = "EduAgenda",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Controle de Prazos, Provas e Trabalhos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Stat Badge Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        val pendingCount = tasks.count { !it.isCompleted }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassEmpty,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (pendingCount == 1) "1 item pendente" else "$pendingCount pendentes",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("add_task_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Adicionar Tarefa")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Adicionar", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar Component
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                placeholder = { Text("Pesquisar por título, matéria ou descrição...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_bar"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                ),
                singleLine = true
            )

            // Dynamic Category Filter Strip
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    "TODAS" to "Todas 🎯",
                    "TRABALHOS" to "Trabalhos 📝",
                    "PROVAS" to "Provas 🚨",
                    "ESTUDOS" to "Estudos 📚",
                    "PENDENTES" to "Pendentes ⏳",
                    "CONCLUIDAS" to "Concluídas ✅"
                )

                items(filters) { (filterKey, label) ->
                    val isSelected = selectedFilter == filterKey
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectedFilter.value = filterKey },
                        label = { Text(text = label, fontWeight = FontWeight.Medium) },
                        modifier = Modifier.testTag("filter_chip_$filterKey"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = Color.Transparent,
                            selectedBorderColor = Color.Transparent,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }

            // Summary Info Strip for student due items
            val upcomingTasks = tasks.filter { !it.isCompleted && (it.dateTime > System.currentTimeMillis()) }
            if (upcomingTasks.isNotEmpty()) {
                val nextExam = upcomingTasks.firstOrNull { it.type == "PROVA" }
                val nextWork = upcomingTasks.firstOrNull { it.type == "TRABALHO" }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            nextExam != null -> "Próxima Prova: ${nextExam.title} (${nextExam.subject}) - Fique atento!"
                            nextWork != null -> "Foco na entrega: ${nextWork.title} - Não deixe para depois!"
                            else -> "Continue progredindo nos seus estudos!"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Tasks List / Empty State Layout
            if (tasks.isEmpty()) {
                EmptyStateLayout { showAddDialog = true }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 4.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        TaskListItem(
                            task = task,
                            onToggleComplete = { viewModel.toggleTaskCompletion(task) },
                            onDelete = { viewModel.deleteTask(task) }
                        )
                    }
                }
            }
        }
    }

    // Add Task Custom Dialog Component
    if (showAddDialog) {
        AddTaskDialog(
            hasNotificationPermission = hasNotificationPermission,
            requestPermission = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            onDismiss = { showAddDialog = false },
            onSave = { title, desc, subject, timestamp, type, notifyMins ->
                viewModel.addTask(title, desc, subject, timestamp, type, notifyMins)
                showAddDialog = false
                Toast.makeText(context, "Tarefa adicionada! Lembrete configurado.", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun TaskListItem(
    task: Task,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Choose theme colors depending on the Task Category
    val isCompleted = task.isCompleted
    val accentColor = when (task.type.uppercase()) {
        "PROVA" -> Color(0xFFE53935)      // Elegant warning red
        "TRABALHO" -> Color(0xFFFB8C00)   // Deep study orange
        "ESTUDO" -> Color(0xFF8E24AA)     // Deep learning violet
        else -> Color(0xFF00ACC1)         // Task blue/teal
    }

    val typeLabel = when (task.type.uppercase()) {
        "PROVA" -> "PROVA 🚨"
        "TRABALHO" -> "TRABALHO 📝"
        "ESTUDO" -> "ESTUDO 📚"
        else -> "TAREFA 📅"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_card_${task.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        border = if (isCompleted) null else androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Task category indicating bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isCompleted) Color.Gray.copy(alpha = 0.5f) else accentColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Main details block
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isCompleted) Color.Gray else accentColor
                    )
                    
                    if (task.subject.isNotBlank()) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = task.subject,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (isCompleted) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isCompleted) 0.5f else 0.8f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Date/Time indicator block with alarm setting marker
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Prazo de entrega",
                        tint = if (isCompleted) Color.Gray else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = formatDateTimePortuguese(task.dateTime),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = if (isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (task.notifyMinutesBefore >= 0 && !isCompleted) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Lembrete Ativo",
                            tint = accentColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = when (task.notifyMinutesBefore) {
                                0 -> "no prazo"
                                15 -> "15m antes"
                                60 -> "1h antes"
                                1440 -> "1d antes"
                                else -> "com lembrete"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = accentColor
                        )
                    }
                }
            }

            // Quick Toggle Complete Checkbox
            Checkbox(
                checked = isCompleted,
                onCheckedChange = { onToggleComplete() },
                modifier = Modifier.testTag("task_checkbox_${task.id}")
            )

            // Delete action button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_button_${task.id}").size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Excluir",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyStateLayout(
    onAddTaskClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Nenhum compromisso por aqui!",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Tenha o controle de suas matérias escolares. Adicione provas, trabalhos ou sessões de estudos para receber lembretes e não perder prazos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onAddTaskClick,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Adicionar Primeira Tarefa", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    hasNotificationPermission: Boolean,
    requestPermission: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        desc: String,
        subject: String,
        timestamp: Long,
        type: String,
        notifyMinutesBefore: Int
    ) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var taskType by remember { mutableStateOf("TRABALHO") } // "TRABALHO", "PROVA", "ESTUDO", "OUTRO"
    
    // Default scheduled time: tomorrow at this hour
    val selectCalendar = remember {
        Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.MINUTE, 0)
        }
    }

    var selectedDateStr by remember {
        mutableStateOf(
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectCalendar.time)
        )
    }

    var selectedTimeStr by remember {
        mutableStateOf(
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(selectCalendar.time)
        )
    }

    var notifyMinutesBefore by remember { mutableStateOf(15) } // 0 = standard deadline alarm, 15m, 60m (1h), 1440m (1d)

    // Trigger Android Native DatePickerDialog
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            selectCalendar.set(Calendar.YEAR, year)
            selectCalendar.set(Calendar.MONTH, month)
            selectCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            selectedDateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectCalendar.time)
        },
        selectCalendar.get(Calendar.YEAR),
        selectCalendar.get(Calendar.MONTH),
        selectCalendar.get(Calendar.DAY_OF_MONTH)
    )

    // Trigger Android Native TimePickerDialog
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            selectCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            selectCalendar.set(Calendar.MINUTE, minute)
            selectCalendar.set(Calendar.SECOND, 0)
            selectCalendar.set(Calendar.MILLISECOND, 0)
            selectedTimeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(selectCalendar.time)
        },
        selectCalendar.get(Calendar.HOUR_OF_DAY),
        selectCalendar.get(Calendar.MINUTE),
        true // 24 hours format rule
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 24.dp)
                .testTag("add_task_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nova Tarefa Estudantil",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cancelar")
                    }
                }

                // Choose Class/Type Segment Options
                Text(
                    text = "Tipo de Compromisso",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val types = listOf(
                        "TRABALHO" to "Trabalho 📝",
                        "PROVA" to "Prova 🚨",
                        "ESTUDO" to "Estudo 📚",
                        "OUTRO" to "Outro 📅"
                    )
                    types.forEach { (typeKey, label) ->
                        val isSelected = taskType == typeKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    }
                                )
                                .clickable { taskType = typeKey }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título (ex: Entrega de Física, Prova Bimestral)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_title_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Subject/Course Input
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Matéria / Disciplina (ex: Matemática, Biologia)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_subject_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição / Instruções (opcional)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_desc_input"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                // Date and Time Choices Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Date field trigger
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .clickable { datePickerDialog.show() }
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Data", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text(selectedDateStr, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }

                    // Time field trigger
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .clickable { timePickerDialog.show() }
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Horário", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text(selectedTimeStr, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Notification Alert Choice Row
                Text(
                    text = "Lembrete com Notificação",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Ask for permission toggle reminder
                if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                            .clickable { requestPermission() }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Clique aqui para conceder permissão de notificações",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val alertTimes = listOf(
                        0 to "No prazo",
                        15 to "15m antes",
                        60 to "1h antes",
                        1440 to "1d antes"
                    )
                    alertTimes.forEach { (minsVal, alertLabel) ->
                        val isAlertSelected = notifyMinutesBefore == minsVal
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isAlertSelected) {
                                        MaterialTheme.colorScheme.secondary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    }
                                )
                                .clickable { notifyMinutesBefore = minsVal }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = alertLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isAlertSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isAlertSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Action Confirmation Button
                Button(
                    onClick = {
                        if (title.isBlank()) {
                            Toast.makeText(context, "Por favor, digite o título do compromisso!", Toast.LENGTH_SHORT).show()
                        } else {
                            val timestamp = selectCalendar.timeInMillis
                            onSave(title, description, subject, timestamp, taskType, notifyMinutesBefore)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_task_button")
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Salvar Compromisso", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Locale date formatter helper for Portuguese strings
fun formatDateTimePortuguese(timestamp: Long): String {
    val date = Date(timestamp)
    val now = Calendar.getInstance()
    val taskCal = Calendar.getInstance().apply { time = date }
    
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeString = timeFormat.format(date)
    
    // Check if it is today
    if (now.get(Calendar.YEAR) == taskCal.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == taskCal.get(Calendar.DAY_OF_YEAR)) {
        return "Hoje às $timeString"
    }
    
    // Check if tomorrow
    val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
    if (tomorrow.get(Calendar.YEAR) == taskCal.get(Calendar.YEAR) &&
        tomorrow.get(Calendar.DAY_OF_YEAR) == taskCal.get(Calendar.DAY_OF_YEAR)) {
        return "Amanhã às $timeString"
    }
    
    val dateFormat = SimpleDateFormat("dd 'de' MMMM 'às' HH:mm", Locale("pt", "BR"))
    return dateFormat.format(date)
}

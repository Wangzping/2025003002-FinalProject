package com.example.campushub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campushub.CampusHubApplication
import com.example.campushub.viewmodel.CreateActivityViewModel
import com.example.campushub.viewmodel.CreateActivityViewModelFactory
import com.example.campushub.viewmodel.CreateUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateActivityScreen(onNavigateBack: () -> Unit) {
    val app = LocalContext.current.applicationContext as CampusHubApplication
    val container = app.container
    val vm: CreateActivityViewModel = viewModel(factory = CreateActivityViewModelFactory(container.activityRepository, container.notificationRepository))
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var organizer by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("文艺活动") }
    var maxParticipants by remember { mutableStateOf("") }
    var creditHours by remember { mutableStateOf("2") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val categories = listOf("文艺活动", "学术竞赛", "体育赛事", "学术讲座", "公益活动")
    var expanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is CreateUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as CreateUiState.Error).message)
                vm.reset()
            }
            is CreateUiState.Success -> {
                snackbarHostState.showSnackbar("活动发布成功")
                onNavigateBack()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("发布活动") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("活动标题 *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("活动描述 *") }, modifier = Modifier.fillMaxWidth().height(120.dp), maxLines = 5)

            // 日期选择器
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("活动日期 *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "选择日期")
                    }
                }
            )

            // 时间选择器
            OutlinedTextField(
                value = time,
                onValueChange = { time = it },
                label = { Text("活动时间") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showTimePicker = true }) {
                        Icon(Icons.Default.Schedule, contentDescription = "选择时间")
                    }
                }
            )

            OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("活动地点 *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = organizer, onValueChange = { organizer = it }, label = { Text("组织者") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = maxParticipants, onValueChange = { maxParticipants = it }, label = { Text("最大参与人数 (留空表示不限)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = creditHours, onValueChange = { creditHours = it }, label = { Text("第二课堂学时") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

            // Category dropdown
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = category, onValueChange = {},
                    label = { Text("活动类别") }, modifier = Modifier.fillMaxWidth().menuAnchor(), readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    categories.forEach { cat ->
                        DropdownMenuItem(text = { Text(cat) }, onClick = { category = cat; expanded = false })
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { vm.createActivity(title, description, date, time, location, organizer, category, maxParticipants, creditHours) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = uiState !is CreateUiState.Success
            ) {
                Text("发布活动", style = MaterialTheme.typography.titleMedium)
            }
        }
    }

    // 日期选择对话框
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        date = sdf.format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // 时间选择对话框
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = 14,
            initialMinute = 0,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("选择开始时间") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    val startHour = timePickerState.hour
                    val startMinute = timePickerState.minute
                    val endHour = (startHour + 2) % 24
                    time = String.format(
                        Locale.getDefault(),
                        "%02d:%02d-%02d:%02d",
                        startHour, startMinute, endHour, startMinute
                    )
                    showTimePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("取消") }
            }
        )
    }
}

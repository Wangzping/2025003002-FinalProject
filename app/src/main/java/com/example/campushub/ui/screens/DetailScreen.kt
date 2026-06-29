package com.example.campushub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campushub.CampusHubApplication
import com.example.campushub.ui.components.ErrorState
import com.example.campushub.ui.components.LoadingState
import com.example.campushub.viewmodel.CheckInResult
import com.example.campushub.viewmodel.DetailUiState
import com.example.campushub.viewmodel.DetailViewModel
import com.example.campushub.viewmodel.DetailViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(activityId: Int, onNavigateBack: () -> Unit) {
    val app = LocalContext.current.applicationContext as CampusHubApplication
    val container = app.container
    val vm: DetailViewModel = viewModel(
        key = "detail_$activityId",
        factory = DetailViewModelFactory(activityId, container.activityRepository, container.notificationRepository, container.checkInRepository)
    )

    val uiState by vm.uiState.collectAsStateWithLifecycle()
    var showEditDialog by remember { mutableStateOf(false) }
    var showGenerateCodeDialog by remember { mutableStateOf(false) }
    var showCheckInDialog by remember { mutableStateOf(false) }
    var generatedCode by remember { mutableStateOf("") }
    var checkInMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("活动详情") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is DetailUiState.Loading -> LoadingState(modifier = Modifier.padding(padding))
            is DetailUiState.Error -> ErrorState(message = state.message, onRetry = { vm.load() }, modifier = Modifier.padding(padding))
            is DetailUiState.Success -> {
                val activity = state.activity
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                ) {
                    // Header section
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(text = activity.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                InfoItem("📅", activity.date)
                                InfoItem("⏰", activity.time)
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                InfoItem("📍", activity.location)
                                InfoItem("👤", activity.organizer)
                            }
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        // Status
                        val statusText = when {
                            activity.isRegistered -> "✅ 已报名"
                            activity.maxParticipants > 0 && activity.currentParticipants >= activity.maxParticipants -> "❌ 已满"
                            else -> "📝 可报名"
                        }
                        Text(statusText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))

                        // Participants
                        val partText = if (activity.maxParticipants > 0) "${activity.currentParticipants}/${activity.maxParticipants}" else "不限"
                        Text("参与人数: $partText", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (activity.maxParticipants > 0) {
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { activity.currentParticipants.toFloat() / activity.maxParticipants },
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                                color = if (activity.currentParticipants >= activity.maxParticipants) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        // Action buttons
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { vm.toggleRegistration() },
                                modifier = Modifier.weight(1f),
                                enabled = !(!activity.isRegistered && activity.maxParticipants > 0 && activity.currentParticipants >= activity.maxParticipants)
                            ) {
                                Icon(if (activity.isRegistered) Icons.Default.Cancel else Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text(if (activity.isRegistered) "取消报名" else "立即报名")
                            }
                            OutlinedButton(onClick = { vm.toggleFavorite() }, modifier = Modifier.weight(1f)) {
                                Icon(if (activity.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null, tint = if (activity.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                                Spacer(Modifier.width(4.dp))
                                Text(if (activity.isFavorite) "已收藏" else "收藏")
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { showEditDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !activity.isRegistered
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("编辑活动")
                        }

                        Spacer(Modifier.height(16.dp))

                        // ===== 签到区域 =====
                        HorizontalDivider()
                        Spacer(Modifier.height(12.dp))
                        Text("活动签到", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        if (activity.isCheckedIn) {
                            Spacer(Modifier.height(8.dp))
                            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text("✅ 已签到", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                        Text("获得 ${activity.creditHours} 学时", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        } else if (activity.isRegistered) {
                            Spacer(Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(
                                    onClick = { showCheckInDialog = true },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("输入签到码")
                                }
                            }
                        } else {
                            Spacer(Modifier.height(8.dp))
                            Text("报名后可签到", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = {
                                generatedCode = ""
                                showGenerateCodeDialog = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("生成签到码（主办方）")
                        }

                        Spacer(Modifier.height(8.dp))
                        Text("第二课堂学时：${activity.creditHours} 学时", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(Modifier.height(20.dp))

                        // Description
                        Text("活动描述", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(activity.description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(Modifier.height(16.dp))

                        // Category
                        Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(8.dp)) {
                            Text(activity.category, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        val activity = (uiState as? DetailUiState.Success)?.activity ?: return
        EditActivityDialog(
            activity = activity,
            onDismiss = { showEditDialog = false },
            onConfirm = { title, desc, date, time, location, category, max, creditHours ->
                vm.updateActivity(title, desc, date, time, location, category, max, creditHours)
                showEditDialog = false
            }
        )
    }

    if (showGenerateCodeDialog) {
        val activity = (uiState as? DetailUiState.Success)?.activity ?: return
        GenerateCodeDialog(
            activityTitle = activity.title,
            onDismiss = { showGenerateCodeDialog = false },
            onGenerate = {
                val code = vm.generateCheckInCodeSuspend()
                generatedCode = code
            },
            existingCode = generatedCode
        )
    }

    if (showCheckInDialog) {
        val activity = (uiState as? DetailUiState.Success)?.activity ?: return
        CheckInCodeDialog(
            activityTitle = activity.title,
            onDismiss = { showCheckInDialog = false },
            onSubmit = { code ->
                kotlinx.coroutines.MainScope().launch {
                    val result = vm.verifyAndCheckIn(code)
                    checkInMessage = when (result) {
                        is CheckInResult.Success -> "签到成功！获得 ${result.creditHours} 学时"
                        is CheckInResult.Error -> result.message
                    }
                    showCheckInDialog = false
                }
            }
        )
    }

    checkInMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { checkInMessage = null },
            title = { Text("签到结果") },
            text = { Text(msg) },
            confirmButton = { Button(onClick = { checkInMessage = null }) { Text("确定") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditActivityDialog(
    activity: com.example.campushub.model.CampusActivity,
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, date: String, time: String, location: String, category: String, maxParticipants: Int, creditHours: Int) -> Unit
) {
    var title by remember { mutableStateOf(activity.title) }
    var description by remember { mutableStateOf(activity.description) }
    var date by remember { mutableStateOf(activity.date) }
    var time by remember { mutableStateOf(activity.time) }
    var location by remember { mutableStateOf(activity.location) }
    var category by remember { mutableStateOf(activity.category) }
    var maxParticipants by remember { mutableStateOf(activity.maxParticipants.toString()) }
    var creditHours by remember { mutableStateOf(activity.creditHours.toString()) }
    var expanded by remember { mutableStateOf(false) }

    val categories = listOf("文艺活动", "学术竞赛", "体育赛事", "学术讲座", "公益活动")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑活动") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("活动标题") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("活动描述") }, modifier = Modifier.fillMaxWidth().height(100.dp), maxLines = 4)
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("活动日期 (如: 2026-11-01)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("活动时间 (如: 14:00-16:00)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("活动地点") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = maxParticipants, onValueChange = { maxParticipants = it }, label = { Text("最大参与人数") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = creditHours, onValueChange = { creditHours = it }, label = { Text("第二课堂学时") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

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
            }
        },
        confirmButton = {
            Button(onClick = {
                val max = maxParticipants.toIntOrNull() ?: activity.maxParticipants
                val credit = creditHours.toIntOrNull() ?: activity.creditHours
                onConfirm(title, description, date, time, location, category, max, credit)
            }) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
private fun GenerateCodeDialog(
    activityTitle: String,
    onDismiss: () -> Unit,
    onGenerate: suspend () -> Unit,
    existingCode: String
) {
    var code by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(existingCode) {
        if (existingCode.isNotEmpty()) {
            code = existingCode
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("生成签到码") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("活动：$activityTitle", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                if (code.isEmpty() && !isLoading) {
                    Button(onClick = {
                        isLoading = true
                        scope.launch {
                            onGenerate()
                            isLoading = false
                        }
                    }) { Text("生成签到码") }
                } else if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                            Text("签到码", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            Text(code, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(8.dp))
                            Text("5分钟内有效", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("请将签到码展示给参与者", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}

@Composable
private fun CheckInCodeDialog(
    activityTitle: String,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var code by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("签到") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("活动：$activityTitle", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = { if (it.length <= 6) code = it.filter { c -> c.isDigit() } },
                    label = { Text("请输入 6 位签到码") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(code) }, enabled = code.length == 6) { Text("签到") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
private fun InfoItem(icon: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(icon, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

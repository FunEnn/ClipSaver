package com.example.clipsaver.feature.history

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.clipsaver.core.model.ClipEntry
import com.example.clipsaver.utils.AccessibilityUtils
import com.example.clipsaver.utils.ClipboardHelper
import org.koin.androidx.compose.koinViewModel

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = koinViewModel()
) {
    val context = LocalContext.current
    var isAccEnabled by remember {
        mutableStateOf(false) // 初始值设为false，稍后更新
    }
    
    // 使用LaunchedEffect在每次屏幕显示时检查无障碍服务状态
    LaunchedEffect(Unit) {
        isAccEnabled = AccessibilityUtils.isServiceEnabled(
            context,
            "com.example.clipsaver.accessibility.GlobalClipboardService"
        )
    }

    val entries by viewModel.entries.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (!isAccEnabled) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(text = "未开启无障碍服务", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "为实现全局复制监听，请前往系统无障碍设置开启“ClipSaver”。",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = {
                            context.startActivity(
                                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }) {
                            Text("前往开启")
                        }
                        Spacer(Modifier.width(12.dp))
                        OutlinedButton(onClick = {
                            isAccEnabled = AccessibilityUtils.isServiceEnabled(
                                context,
                                "com.example.clipsaver.accessibility.GlobalClipboardService"
                            )
                        }) {
                            Text("已开启，刷新")
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.search(it) },
            label = { Text("搜索") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // 显示总条数
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "总记录数: $totalCount",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (searchQuery.isNotBlank() && entries.isNotEmpty()) {
                Text(
                    text = "搜索结果: ${entries.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (entries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无剪贴板记录")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(entries) { entry ->
                    ClipEntryItem(
                        entry = entry,
                        onDelete = { viewModel.deleteEntry(entry) },
                        onToggleFavorite = { viewModel.toggleFavorite(entry) }
                    )
                }
            }
        }
    }
}

@Composable
fun ContentDetailDialog(
    content: String,
    date: java.util.Date,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "剪贴板内容详情",
                    style = MaterialTheme.typography.titleMedium
                )
                Row {
                    IconButton(
                        onClick = {
                            ClipboardHelper.setClipboardText(context, content)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "复制",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                        .format(date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // 弹窗中的滚动内容区域
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 400.dp) // 固定大小，支持滚动
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = MaterialTheme.shapes.small
                        ),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    val scrollState = rememberScrollState()
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

@Composable
fun ClipEntryItem(
    entry: ClipEntry,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    var showDetailDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    // 判断内容是否过长，需要显示省略号
    val isContentLong = entry.content.length > 100
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 180.dp) // 限制最大高度
            .clickable { showDetailDialog = true }
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // 为长文本内容添加滚动支持
            Text(
                text = if (isContentLong) {
                    entry.content.take(100) + "..."
                } else {
                    entry.content
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 120.dp) // 设置最大高度限制
                    .padding(vertical = 2.dp)
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                        .format(entry.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row {
                    IconButton(
                        onClick = {
                            ClipboardHelper.setClipboardText(context, entry.content)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "复制",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (entry.isFavorite) {
                                Icons.Default.Favorite
                            } else {
                                Icons.Default.FavoriteBorder
                            },
                            contentDescription = "收藏",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
    
    // 弹窗显示
    if (showDetailDialog) {
        ContentDetailDialog(
            content = entry.content,
            date = entry.date,
            onDismiss = { showDetailDialog = false }
        )
    }
}


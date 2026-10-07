package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CleanCityViewModel
import com.example.ui.theme.*
import com.example.util.appStrings

@Composable
fun AiChatbotScreen(
    viewModel: CleanCityViewModel,
    modifier: Modifier = Modifier
) {
    val strings = appStrings()
    val messages by viewModel.chatMessages.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val quickQuestions = remember {
        listOf(
            "Which bin does milk packet go into?",
            "When will truck arrive today?",
            "How to dispose broken glass safely?",
            "What is the fee for residential property?"
        )
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MobbinCanvas)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Mobbin Clean Header with terminal period
        Column(modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)) {
            Text(
                text = "${strings.aiAssistantTitle}.",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MobbinInk
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${strings.aiAssistantSubtitle}.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MobbinTextMuted
                )
            )
        }

        // Quick Suggestion Chips (Mobbin stadium pills)
        LazyRow(
            modifier = Modifier.padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickQuestions) { q ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MobbinCanvasSoft)
                        .border(1.dp, MobbinHairline, RoundedCornerShape(50))
                        .clickable {
                            viewModel.sendChatMessage(q)
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = q,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MobbinInk
                    )
                }
            }
        }

        // Chat conversation history
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.sender == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    if (!isUser) {
                        // 30% Squircle Bot Icon
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MobbinPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = MobbinOnPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Box(
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isUser) MobbinPrimary else MobbinCanvasSoft)
                            .border(
                                1.dp,
                                if (isUser) MobbinPrimary else MobbinHairline,
                                RoundedCornerShape(20.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Text(
                            text = msg.text,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isUser) MobbinOnPrimary else MobbinInk,
                                lineHeight = 20.sp
                            )
                        )
                    }
                }
            }

            if (isChatLoading) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(start = 42.dp, top = 4.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MobbinPrimary,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = strings.aiThinking,
                            style = MaterialTheme.typography.bodySmall.copy(color = MobbinTextMuted)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Input Field (Mobbin clean pill input)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text(strings.aiInputPlaceholder, color = MobbinTextFaint) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_ai_chat"),
                shape = RoundedCornerShape(50),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MobbinPrimary,
                    unfocusedBorderColor = MobbinHairline,
                    focusedTextColor = MobbinInk,
                    unfocusedTextColor = MobbinInk,
                    focusedContainerColor = MobbinCanvas,
                    unfocusedContainerColor = MobbinCanvas
                )
            )

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (inputText.isBlank()) MobbinCanvasSoft else MobbinPrimary)
                    .border(
                        1.dp,
                        if (inputText.isBlank()) MobbinHairline else MobbinPrimary,
                        CircleShape
                    )
                    .clickable(enabled = inputText.isNotBlank()) {
                        viewModel.sendChatMessage(inputText)
                        inputText = ""
                    }
                    .testTag("btn_send_chat"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = strings.send,
                    tint = if (inputText.isBlank()) MobbinTextMuted else MobbinOnPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

package com.example.ui.generator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DashboardCustomize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.domain.model.ApiKeySlot
import com.example.domain.model.GeneratedPrompt
import com.example.domain.model.GenerationResult
import com.example.domain.model.PromptStyle
import com.example.domain.model.StructuredPromptSections
import com.example.ui.theme.SectionConstraintsColor
import com.example.ui.theme.SectionContextColor
import com.example.ui.theme.SectionFormatColor
import com.example.ui.theme.SectionRoleColor
import com.example.ui.theme.SectionTaskColor

private data class PromptSample(
    val title: String,
    val text: String,
    val style: PromptStyle,
    val role: String = "",
    val constraints: String = ""
)

private val samplePrompts = listOf(
    PromptSample(
        title = "فارسی: سیستم پیشنهاد فیلم",
        text = "می‌خوام یک الگوریتم پیشنهاد دهنده فیلم با پایتون و یادگیری ماشین طراحی کنم که بر اساس سلیقه کاربر و امتیازات فیلم‌های مشابه را فیلتر کند.",
        style = PromptStyle.SOFTWARE_DEVELOPMENT,
        role = "Senior Machine Learning Engineer",
        constraints = "Use Scikit-learn, explain cosine similarity, provide production-ready code with type hints."
    ),
    PromptSample(
        title = "فارسی: استراتژی بازاریابی SaaS",
        text = "یک پلن بازاریابی محتوایی ۳ ماهه برای جذب ۱۰۰۰ کاربر اول برای یک ابزار مدیریت پروژه ابری برای تیم‌های ریموت.",
        style = PromptStyle.BUSINESS_MARKETING,
        role = "B2B SaaS Growth Marketer",
        constraints = "Target bootstrapped founders, focus on zero-cost viral growth and LinkedIn organic reach."
    ),
    PromptSample(
        title = "EN: REST API Architecture",
        text = "Design a high-throughput microservice architecture in Kotlin with Ktor for handling real-time order matching.",
        style = PromptStyle.SOFTWARE_DEVELOPMENT,
        role = "Principal Backend Architect",
        constraints = "Include concurrency model, backpressure handling, and Redis cache layer."
    ),
    PromptSample(
        title = "EN: Sci-Fi World Building",
        text = "Create a detailed sci-fi cyberpunk universe set in 2180 where memories are traded as currency in orbital megacities.",
        style = PromptStyle.CREATIVE_WRITING,
        role = "Master Science Fiction Worldbuilder",
        constraints = "Detail socio-economic impact, memory mining factions, and visual aesthetic guidelines."
    )
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GeneratorScreen(
    viewModel: GeneratorViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val apiSlots by viewModel.apiSlots.collectAsStateWithLifecycle()
    val primarySlot by viewModel.primarySlot.collectAsStateWithLifecycle()
    val isAutoFallback by viewModel.isAutoFallback.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val enabledSlotsCount = remember(apiSlots) { apiSlots.count { it.isEnabled } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Banner with Multi-API Slots & Fallback Status
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("header_card"),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.title_generator),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                            ) {
                                Text(
                                    text = primarySlot?.provider?.displayName ?: "Primary",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Model: ${primarySlot?.model ?: "Default"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 4-Slot Multi-API Status Bar
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SwapHoriz,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "۴ موتور همزمان ($enabledSlotsCount فعال)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isAutoFallback) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Auto-Fallback فعال",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Idea Inspiration Chips
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Quick Inspiration Templates:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                samplePrompts.forEach { sample ->
                    SuggestionChip(
                        onClick = {
                            viewModel.applySample(
                                text = sample.text,
                                style = sample.style,
                                role = sample.role,
                                constraints = sample.constraints
                            )
                        },
                        label = {
                            Text(
                                sample.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = when (sample.style) {
                                    PromptStyle.SOFTWARE_DEVELOPMENT -> Icons.Outlined.Code
                                    PromptStyle.CREATIVE_WRITING -> Icons.Outlined.Brush
                                    PromptStyle.BUSINESS_MARKETING -> Icons.Outlined.BusinessCenter
                                    PromptStyle.DATA_ANALYSIS -> Icons.Outlined.Analytics
                                    PromptStyle.GENERAL -> Icons.Outlined.DashboardCustomize
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.testTag("chip_sample_${sample.title}")
                    )
                }
            }
        }

        // Free-Form Text Input
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.input_label),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (uiState.rawInput.isNotBlank()) {
                            IconButton(
                                onClick = { viewModel.clearInput() },
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("btn_clear_input")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = stringResource(R.string.btn_clear),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        if (uiState.generatedPrompt != null || uiState.rawInput.isNotBlank()) {
                            IconButton(
                                onClick = { viewModel.resetAll() },
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("btn_reset_all")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.RestartAlt,
                                    contentDescription = "Reset Form and Output",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = uiState.rawInput,
                    onValueChange = { viewModel.updateInput(it) },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.input_placeholder),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("input_raw_text"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "Auto-Detect (فارسی / English)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "${uiState.rawInput.length} chars",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Style Selector
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = stringResource(R.string.style_label),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PromptStyle.entries.forEach { style ->
                    val selected = uiState.selectedStyle == style
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.selectStyle(style) },
                        shape = RoundedCornerShape(16.dp),
                        label = {
                            Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text(
                                    text = style.displayName,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = style.persianName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = getStyleIcon(style),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            iconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.testTag("chip_style_${style.name}")
                    )
                }
            }
        }

        // Advanced Options Toggle (Custom Role / Custom Constraints)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Advanced Customization (Optional)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleAdvanced() },
                        modifier = Modifier.testTag("btn_toggle_advanced")
                    ) {
                        Icon(
                            imageVector = if (uiState.isAdvancedExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = "Toggle advanced settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                AnimatedVisibility(
                    visible = uiState.isAdvancedExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.customRole,
                            onValueChange = { viewModel.updateCustomRole(it) },
                            label = { Text(stringResource(R.string.custom_role_label)) },
                            placeholder = { Text(stringResource(R.string.custom_role_placeholder)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_custom_role"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = uiState.customConstraints,
                            onValueChange = { viewModel.updateCustomConstraints(it) },
                            label = { Text(stringResource(R.string.custom_constraints_label)) },
                            placeholder = { Text(stringResource(R.string.custom_constraints_placeholder)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_custom_constraints"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = false,
                            maxLines = 3
                        )
                    }
                }
            }
        }

        // Error message if any
        AnimatedVisibility(
            visible = uiState.errorMessage != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("error_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = uiState.errorMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.dismissError() }) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        // Generate Action Button
        Button(
            onClick = { viewModel.generatePrompt() },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("btn_generate_prompt"),
            enabled = !uiState.isLoading,
            shape = RoundedCornerShape(27.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Generating with ${primarySlot?.provider?.displayName ?: "AI"} (Auto-Fallback Active)...",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.btn_generate),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Generated Prompt Result Card with Fallback Feedback & Clear Button
        uiState.generatedPrompt?.let { prompt ->
            PromptResultCard(
                prompt = prompt,
                generationResult = uiState.generationResult,
                isSaved = uiState.isSaved,
                onSave = { viewModel.saveCurrentPrompt() },
                onClearResult = { viewModel.clearGeneratedPrompt() },
                onCopy = { text ->
                    copyToClipboard(context, text)
                    Toast.makeText(context, context.getString(R.string.btn_copied), Toast.LENGTH_SHORT).show()
                },
                onShare = { text ->
                    shareText(context, text)
                }
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun PromptResultCard(
    prompt: GeneratedPrompt,
    generationResult: GenerationResult?,
    isSaved: Boolean,
    onSave: () -> Unit,
    onClearResult: () -> Unit,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val structured = remember(prompt.promptText) {
        StructuredPromptSections.parse(prompt.promptText)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("generated_result_card"),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Bar with Used Slot indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = prompt.style.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    generationResult?.let { genRes ->
                        Surface(
                            color = if (genRes.fallbackOccurred) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (genRes.fallbackOccurred) "تولید با پشتیبان: ${genRes.usedSlot.provider.displayName}" else "تولید با: ${genRes.usedSlot.provider.displayName}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (genRes.fallbackOccurred) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(
                        onClick = { onCopy(prompt.promptText) },
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("btn_copy_full_prompt")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = stringResource(R.string.btn_copy),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { onShare(prompt.promptText) },
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("btn_share_prompt")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = stringResource(R.string.btn_share),
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onSave,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("btn_save_prompt")
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = stringResource(R.string.btn_save),
                            tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Clear/Remove Result Output Button
                    IconButton(
                        onClick = onClearResult,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("btn_clear_generated_prompt")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear generated prompt",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Notice if auto-fallback happened
            if (generationResult?.fallbackOccurred == true) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SwapHoriz,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "بات اصلی دارای محدودیت/اتمام کردیت بود؛ پرامپت با موفقیت از کلید پشتیبان (${generationResult.usedSlot.label}) دریافت شد.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // 5 Structured Sections
            SectionBadgeItem(
                badgeTitle = "ROLE",
                content = structured.role,
                accentColor = SectionRoleColor,
                onCopySection = { onCopy(it) }
            )

            if (structured.context.isNotBlank()) {
                SectionBadgeItem(
                    badgeTitle = "CONTEXT",
                    content = structured.context,
                    accentColor = SectionContextColor,
                    onCopySection = { onCopy(it) }
                )
            }

            SectionBadgeItem(
                badgeTitle = "TASK",
                content = structured.task,
                accentColor = SectionTaskColor,
                onCopySection = { onCopy(it) }
            )

            if (structured.constraints.isNotBlank()) {
                SectionBadgeItem(
                    badgeTitle = "CONSTRAINTS",
                    content = structured.constraints,
                    accentColor = SectionConstraintsColor,
                    onCopySection = { onCopy(it) }
                )
            }

            if (structured.outputFormat.isNotBlank()) {
                SectionBadgeItem(
                    badgeTitle = "OUTPUT FORMAT",
                    content = structured.outputFormat,
                    accentColor = SectionFormatColor,
                    onCopySection = { onCopy(it) }
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Bottom action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onClearResult,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                    modifier = Modifier.testTag("btn_dismiss_result_bottom")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Clear Output", style = MaterialTheme.typography.labelMedium)
                }

                FilledTonalButton(
                    onClick = { onCopy(prompt.promptText) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_copy_all_bottom")
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Copy Complete Prompt", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun SectionBadgeItem(
    badgeTitle: String,
    content: String,
    accentColor: Color,
    onCopySection: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = accentColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = badgeTitle,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                IconButton(
                    onClick = { onCopySection("$badgeTitle:\n$content") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "Copy $badgeTitle section",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )
        }
    }
}

private fun getStyleIcon(style: PromptStyle): ImageVector {
    return when (style) {
        PromptStyle.SOFTWARE_DEVELOPMENT -> Icons.Default.Code
        PromptStyle.CREATIVE_WRITING -> Icons.Default.Create
        PromptStyle.BUSINESS_MARKETING -> Icons.Outlined.BusinessCenter
        PromptStyle.DATA_ANALYSIS -> Icons.Outlined.Analytics
        PromptStyle.GENERAL -> Icons.Default.AutoAwesome
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Generated Prompt", text)
    clipboard.setPrimaryClip(clip)
}

private fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "AI Prompt")
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share AI Prompt"))
}

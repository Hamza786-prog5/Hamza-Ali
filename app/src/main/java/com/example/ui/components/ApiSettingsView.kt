package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdminConfig
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.OnlineGreen
import kotlin.math.roundToInt

@Composable
fun ApiSettingsView(
    config: AdminConfig,
    onSaveConfig: (AdminConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var wikiSearchUrl by remember(config) { mutableStateOf(config.wikipediaSearchUrl) }
    var wikiSummaryUrl by remember(config) { mutableStateOf(config.wikipediaSummaryUrl) }
    var searchLimit by remember(config) { mutableFloatStateOf(config.searchLimit.toFloat()) }
    var customApiKey by remember(config) { mutableStateOf(config.customAiApiKey) }
    var customEndpoint by remember(config) { mutableStateOf(config.customAiEndpoint) }
    var customModel by remember(config) { mutableStateOf(config.customAiModel) }
    var enableHybridAi by remember(config) { mutableStateOf(config.enableHybridAi) }
    var systemPrompt by remember(config) { mutableStateOf(config.systemPrompt) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
    ) {
        // Section: Knowledge Research API
        Text(
            text = "1. Wikipedia & Research API",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = CyberCyan
        )
        Text(
            text = "Configure live internet knowledge and search endpoints",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = wikiSearchUrl,
            onValueChange = { wikiSearchUrl = it },
            label = { Text("Search API URL") },
            leadingIcon = { Icon(Icons.Default.Public, contentDescription = null, tint = CyberCyan) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("wiki_search_url_input"),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = wikiSummaryUrl,
            onValueChange = { wikiSummaryUrl = it },
            label = { Text("Summary API Base URL") },
            leadingIcon = { Icon(Icons.Default.Public, contentDescription = null, tint = CyberCyan) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("wiki_summary_url_input"),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Search Candidate Limit: ${searchLimit.roundToInt()} articles",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Slider(
            value = searchLimit,
            onValueChange = { searchLimit = it },
            valueRange = 3f..15f,
            steps = 11,
            colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan),
            modifier = Modifier.testTag("search_limit_slider")
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(16.dp))

        // Section: Custom AI / LLM Integration
        Text(
            text = "2. AI Model & Key Configuration",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ElectricIndigo
        )
        Text(
            text = "Add custom Gemini or AI API key to supercharge answers",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enable Hybrid AI Synthesis",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Combines live internet knowledge with generative AI reasoning",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = enableHybridAi,
                    onCheckedChange = { enableHybridAi = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = CyberCyan, checkedTrackColor = ElectricIndigo),
                    modifier = Modifier.testTag("enable_hybrid_ai_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = customApiKey,
            onValueChange = { customApiKey = it },
            label = { Text("Custom AI / Gemini API Key") },
            placeholder = { Text("Enter your API key here...") },
            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = ElectricIndigo) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("custom_api_key_input"),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = customEndpoint,
            onValueChange = { customEndpoint = it },
            label = { Text("AI Endpoint URL") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("custom_endpoint_input"),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = customModel,
            onValueChange = { customModel = it },
            label = { Text("Model Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("custom_model_input"),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = systemPrompt,
            onValueChange = { systemPrompt = it },
            label = { Text("System Prompt & Instructions") },
            maxLines = 4,
            modifier = Modifier.fillMaxWidth().testTag("system_prompt_input"),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val updated = config.copy(
                    wikipediaSearchUrl = wikiSearchUrl.trim(),
                    wikipediaSummaryUrl = wikiSummaryUrl.trim(),
                    searchLimit = searchLimit.roundToInt(),
                    customAiApiKey = customApiKey.trim(),
                    customAiEndpoint = customEndpoint.trim(),
                    customAiModel = customModel.trim(),
                    enableHybridAi = enableHybridAi,
                    systemPrompt = systemPrompt.trim()
                )
                onSaveConfig(updated)
                Toast.makeText(context, "API Configuration Saved Successfully!", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("save_api_settings_btn")
        ) {
            Icon(imageVector = Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save API Settings", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

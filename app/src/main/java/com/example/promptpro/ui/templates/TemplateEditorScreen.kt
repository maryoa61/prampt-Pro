package com.example.promptpro.ui.templates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.promptpro.R
import com.example.promptpro.domain.model.PromptExample

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateEditorScreen(
    viewModel: TemplateEditorViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    var newSlot by remember { mutableStateOf("") }
    var newDefaultKey by remember { mutableStateOf("") }
    var newDefaultValue by remember { mutableStateOf("") }
    var newExampleInput by remember { mutableStateOf("") }
    var newExampleOutput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.template_new)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text(stringResource(R.string.template_name)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text(stringResource(R.string.template_description)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            // ---- Slots ----
            Text(stringResource(R.string.template_slots), style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newSlot,
                    onValueChange = { newSlot = it },
                    label = { Text(stringResource(R.string.template_slot_name)) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { viewModel.onAddSlot(newSlot); newSlot = "" }) {
                    Text(stringResource(R.string.template_add))
                }
            }
            state.slots.forEach { slot ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(slot)
                    TextButton(onClick = { viewModel.onRemoveSlot(slot) }) {
                        Text(stringResource(R.string.template_remove))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ---- Default values ----
            Text(stringResource(R.string.template_default_values), style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newDefaultKey,
                    onValueChange = { newDefaultKey = it },
                    label = { Text(stringResource(R.string.template_key)) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = newDefaultValue,
                    onValueChange = { newDefaultValue = it },
                    label = { Text(stringResource(R.string.template_value)) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    viewModel.onAddDefaultValue(newDefaultKey, newDefaultValue)
                    newDefaultKey = ""
                    newDefaultValue = ""
                }) { Text(stringResource(R.string.template_add)) }
            }
            state.defaultValues.forEach { (key, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("$key = $value")
                    TextButton(onClick = { viewModel.onRemoveDefaultValue(key) }) {
                        Text(stringResource(R.string.template_remove))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ---- Examples ----
            Text(stringResource(R.string.template_examples), style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = newExampleInput,
                onValueChange = { newExampleInput = it },
                label = { Text(stringResource(R.string.template_example_input)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = newExampleOutput,
                onValueChange = { newExampleOutput = it },
                label = { Text(stringResource(R.string.template_example_output)) },
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                Button(onClick = {
                    viewModel.onAddExample(newExampleInput, newExampleOutput)
                    newExampleInput = ""
                    newExampleOutput = ""
                }) { Text(stringResource(R.string.template_add_example)) }
            }
            state.examples.forEach { ex: PromptExample ->
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text("Input: ${ex.input}", style = MaterialTheme.typography.bodySmall)
                    Text("Output: ${ex.output}", style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { viewModel.onRemoveExample(ex) }) {
                        Text(stringResource(R.string.template_remove))
                    }
                    Divider()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = viewModel::saveTemplate,
                enabled = !state.saving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.saving) stringResource(R.string.template_saving) else stringResource(R.string.template_save))
            }
            state.error?.let {
                Text(
                    text = "Error: $it",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
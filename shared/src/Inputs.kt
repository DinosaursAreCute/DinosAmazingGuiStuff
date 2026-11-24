package login.dinosamazingguistuff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

private val Palette = listOf(
    Color(0xFFE53935), Color(0xFF8E24AA), Color(0xFF3949AB), Color(0xFF039BE5),
    Color(0xFF00ACC1), Color(0xFF43A047), Color(0xFFC0CA33), Color(0xFFFDD835),
    Color(0xFFFB8C00), Color(0xFF6D4C41), Color(0xFF546E7A), Color(0xFFD81B60),
)

@Composable
private fun Section(index: Int, title: String, content: @Composable ColumnScope.() -> Unit) {
    GroupBox(title, Palette[index % Palette.size], Modifier.fillMaxWidth(), content)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InputSections() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        Section(0, "TextField") {
            var v by remember { mutableStateOf("") }
            TextField(v, { v = it }, label = { Text("Text") }, modifier = Modifier.fillMaxWidth())
            Text("Value: \"$v\"")
        }

        Section(1, "OutlinedTextField") {
            var v by remember { mutableStateOf("") }
            OutlinedTextField(v, { v = it }, label = { Text("Text") }, modifier = Modifier.fillMaxWidth())
            Text("Value: \"$v\"")
        }

        Section(2, "Password field") {
            var v by remember { mutableStateOf("") }
            OutlinedTextField(
                value = v,
                onValueChange = { v = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Text("Length: ${v.length}")
        }

        Section(3, "Number field") {
            var v by remember { mutableStateOf("") }
            OutlinedTextField(
                value = v,
                onValueChange = { new -> if (new.all { it.isDigit() }) v = new },
                label = { Text("Digits only") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Text("Value: ${v.toLongOrNull() ?: "-"}")
        }

        Section(4, "Multiline field") {
            var v by remember { mutableStateOf("") }
            OutlinedTextField(v, { v = it }, minLines = 3, modifier = Modifier.fillMaxWidth())
            Text("Lines: ${v.lines().size}, chars: ${v.length}")
        }

        Section(5, "Checkbox") {
            var checked by remember { mutableStateOf(false) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked, { checked = it })
                Text("checked = $checked")
            }
        }

        Section(6, "TriStateCheckbox") {
            var s by remember { mutableStateOf(ToggleableState.Off) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TriStateCheckbox(
                    state = s,
                    onClick = {
                        s = when (s) {
                            ToggleableState.Off -> ToggleableState.On
                            ToggleableState.On -> ToggleableState.Indeterminate
                            ToggleableState.Indeterminate -> ToggleableState.Off
                        }
                    },
                )
                Text("state = $s")
            }
        }

        Section(7, "RadioButton") {
            val options = listOf("Option 1", "Option 2", "Option 3")
            var selected by remember { mutableStateOf(options[0]) }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { o ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selected == o, onClick = { selected = o })
                        Text(o)
                    }
                }
            }
            Text("Selected: $selected")
        }

        Section(8, "Switch") {
            var on by remember { mutableStateOf(false) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(on, { on = it })
                Spacer(Modifier.width(8.dp))
                Text("on = $on")
            }
        }

        Section(9, "Slider") {
            var v by remember { mutableStateOf(50f) }
            Slider(v, { v = it }, valueRange = 0f..100f)
            Text("Value: ${v.toInt()}")
            var stepped by remember { mutableStateOf(0f) }
            Slider(stepped, { stepped = it }, valueRange = 0f..10f, steps = 9)
            Text("Stepped: ${stepped.toInt()}")
        }

        Section(10, "RangeSlider") {
            var r by remember { mutableStateOf(20f..80f) }
            RangeSlider(value = r, onValueChange = { r = it }, valueRange = 0f..100f)
            Text("Range: ${r.start.toInt()} .. ${r.endInclusive.toInt()}")
        }

        Section(11, "Buttons") {
            var last by remember { mutableStateOf("-") }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button({ last = "Button" }) { Text("Button") }
                FilledTonalButton({ last = "FilledTonalButton" }) { Text("Tonal") }
                ElevatedButton({ last = "ElevatedButton" }) { Text("Elevated") }
                OutlinedButton({ last = "OutlinedButton" }) { Text("Outlined") }
                TextButton({ last = "TextButton" }) { Text("Text") }
                IconButton({ last = "IconButton" }) { Text("★") }
                FloatingActionButton({ last = "FloatingActionButton" }) { Text("+") }
            }
            Text("Last pressed: $last")
        }

        Section(0, "Chips") {
            var filter by remember { mutableStateOf(false) }
            var assists by remember { mutableStateOf(0) }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = filter, onClick = { filter = !filter }, label = { Text("FilterChip") })
                AssistChip(onClick = { assists++ }, label = { Text("AssistChip") })
            }
            Text("FilterChip selected = $filter, AssistChip clicks = $assists")
        }

        Section(1, "SegmentedButton") {
            val options = listOf("One", "Two", "Three")
            var selected by remember { mutableStateOf(0) }
            SingleChoiceSegmentedButtonRow {
                options.forEachIndexed { i, label ->
                    SegmentedButton(
                        selected = selected == i,
                        onClick = { selected = i },
                        shape = SegmentedButtonDefaults.itemShape(i, options.size),
                    ) { Text(label) }
                }
            }
            Text("Selected: ${options[selected]}")
        }

        Section(2, "Dropdown menu") {
            val options = listOf("Option 1", "Option 2", "Option 3")
            var expanded by remember { mutableStateOf(false) }
            var selected by remember { mutableStateOf(options[0]) }
            Box {
                OutlinedButton({ expanded = true }) { Text(selected) }
                DropdownMenu(expanded, { expanded = false }) {
                    options.forEach { o ->
                        DropdownMenuItem(
                            text = { Text(o) },
                            onClick = { selected = o; expanded = false },
                        )
                    }
                }
            }
            Text("Selected: $selected")
        }

        Section(3, "Tabs") {
            val tabs = listOf("One", "Two", "Three")
            var selected by remember { mutableStateOf(0) }
            TabRow(selectedTabIndex = selected) {
                tabs.forEachIndexed { i, t ->
                    Tab(selected = selected == i, onClick = { selected = i }, text = { Text(t) })
                }
            }
            Text("Selected tab: $selected")
        }

        Section(4, "DatePicker") {
            val state = rememberDatePickerState()
            DatePicker(state)
            Text("selectedDateMillis = ${state.selectedDateMillis}")
        }

        Section(5, "TimePicker") {
            val state = rememberTimePickerState()
            TimePicker(state)
            Text("Time: ${state.hour.toString().padStart(2, '0')}:${state.minute.toString().padStart(2, '0')}")
        }
    }
}
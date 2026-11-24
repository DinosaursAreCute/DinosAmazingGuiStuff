package login.dinosamazingguistuff

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

// Distinct color per group
private val ControlsColor = Color(0xFF00897B) // teal
private val DinoColor = Color(0xFFEF6C00)     // orange
private val CounterColor = Color(0xFF1E88E5)  // blue
private val ProgressColor = Color(0xFFD81B60) // pink
private val ListColor = Color(0xFF8E24AA)     // purple

// ---------------------------------------------------------------------------
// File listing (uses Shell + System.getProperty, so this is JVM/desktop code)
// ---------------------------------------------------------------------------

data class FileEntry(val name: String, val isDir: Boolean) {
    val type: String
        get() = if (isDir) "Folder" else {
            val ext = name.substringAfterLast('.', "")
            if (ext.isBlank()) "File" else "${ext.uppercase()} file"
        }
    val icon: String get() = if (isDir) "📁" else "📄"
}

fun listDir(): List<FileEntry> {
    val windows = System.getProperty("os.name").startsWith("Windows", ignoreCase = true)

    fun parse(out: String) = out.lines().map { it.trim() }.filter { it.isNotEmpty() }

    val entries = if (windows) {
        // two calls, because `dir /b` doesn't say which entries are folders
        parse(Shell.run("dir /b /ad").stdout).map { FileEntry(it, true) } +
                parse(Shell.run("dir /b /a-d").stdout).map { FileEntry(it, false) }
    } else {
        // -A: include dotfiles, -p: append "/" to folders
        parse(Shell.run("ls -1Ap").stdout).map {
            if (it.endsWith("/")) FileEntry(it.dropLast(1), true) else FileEntry(it, false)
        }
    }
    return entries.sortedWith(compareBy({ !it.isDir }, { it.name.lowercase() })) // folders first
}

// ---------------------------------------------------------------------------
// Table
// ---------------------------------------------------------------------------

private val IconColWidth = 40.dp
private val ActionColWidth = 96.dp

@Composable
fun FileTable(entries: List<FileEntry>, modifier: Modifier = Modifier) {
    Column(modifier) {
        // Header: same column widths as the rows, so everything lines up
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.width(IconColWidth))
            Text("Name", Modifier.weight(3f), fontWeight = FontWeight.Bold)
            Text("Type", Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(ActionColWidth))
        }
        HorizontalDivider()

        LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
            items(entries) { entry ->
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(entry.icon, Modifier.width(IconColWidth), fontSize = 20.sp)
                    Text(
                        entry.name,
                        Modifier.weight(3f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(entry.type, Modifier.weight(1.5f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Button(
                        onClick = { /* no-op */ },
                        modifier = Modifier.width(ActionColWidth),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                    ) { Text("Open") }
                }
                HorizontalDivider(Modifier.padding(horizontal = 8.dp))
            }
        }
    }
}

/**
 * A visible box: colored border + tinted background + title.
 * clip() comes first so nothing inside can paint outside the rounded shape,
 * and padding() comes last so children are laid out inside the border.
 */
@Composable
fun GroupBox(
    title: String,
    color: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(color.copy(alpha = 0.15f))
            .border(2.dp, color, shape)
            .padding(8.dp),
    ) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = color)
        Spacer(Modifier.height(4.dp))
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Dinorawr(onNotify: (String) -> Unit) {
    var rawr by remember { mutableStateOf("Rawr") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(rawr)

        FlowRow {
            Button(onClick = { rawr = "Miaow" }) { Text("Change sound to 'Miaow'") }
            Button(onClick = { rawr = "Wuff" }) { Text("Change sound to 'Wuff'") }
        }
        FlowRow {
            Button(onClick = { rawr = "Rawr" }) { Text("Reset") }
            Button(onClick = { onNotify("Dinosaur says: $rawr") }) {
                Text("Dinosaur - make noise")
            }
        }
    }
}

@Composable
fun Counter() {
    var count by remember { mutableStateOf(0f) }
    val color = lerp(Color.Red, Color.Green, (count / 100f).coerceIn(0f, 1f))
    var count1 by remember { mutableStateOf(0.00) }
    val color1 = lerp(Color.Red, Color.Green, (count1.toFloat()).coerceIn(0f, 1f))

    LaunchedEffect(Unit) {            // starts when the composable appears, cancelled when it leaves
        while (isActive) {
            count1 += 0.01
            delay(100.milliseconds)   // suspends without blocking; never use Thread.sleep here
            if (count1 >= 1) {
                delay(1000.milliseconds)
                count1 = 0.00
            }
        }
    }
    // Counter box with a nested Progress box inside it
    GroupBox("Counter", CounterColor, Modifier.fillMaxWidth()) {
        Text("Clicked $count times")
        Row {
            Button(onClick = { count++ }) {
                Text("Click me")
            }
            Button({ count = 0f; count1 = 0.00 }) {
                Text("Reset")
            }
        }

        Spacer(Modifier.height(8.dp))
        GroupBox("Manual Progress", Color.Cyan, Modifier.fillMaxWidth()) {

            GroupBox("Progress", ProgressColor, Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { count / 100f },
                        modifier = Modifier.weight(1f),
                        color = color,
                        trackColor = Color.White,
                        strokeCap = StrokeCap.Round,
                    )
                    Spacer(Modifier.width(16.dp))
                    Text("Progress: $count / 100")
                }
            }
            GroupBox("Progress", Color.Yellow, Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        progress = { count / 100f },
                        color = color,
                        modifier = Modifier.size(20.dp),
                        trackColor = Color.White,
                        strokeCap = StrokeCap.Round,
                    )
                    Spacer(Modifier.width(16.dp))
                    Text("Progress: $count / 100")
                }
            }
        }
    }

    GroupBox("Live Progress", Color.Black, Modifier.fillMaxWidth()) {
        GroupBox("Progress", ProgressColor, Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { count1.toFloat() },
                    modifier = Modifier.weight(1f),
                    color = color1,
                    trackColor = Color.White,
                    strokeCap = StrokeCap.Round,
                )
                Spacer(Modifier.width(16.dp))
                Text("Progress: ${(count1 * 100).toInt()}%")
            }
        }
        GroupBox("Progress", Color.Yellow, Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    progress = { count1.toFloat() },
                    color = color1,
                    modifier = Modifier.size(20.dp),
                    trackColor = Color.White,
                    strokeCap = StrokeCap.Round,
                )
                Spacer(Modifier.width(16.dp))
                Text("Progress: ${(count1 * 100).toInt()}%")
            }
        }
    }
}

@Composable
@Preview
fun App() {
    val snackbarState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showList by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    var entries by remember { mutableStateOf<List<FileEntry>>(emptyList()) }

    MaterialTheme(colorScheme = darkColorScheme()) {
        Scaffold(snackbarHost = { SnackbarHost(snackbarState) }) { padding ->
            // ONE root column: every box is a sibling placed below the previous one,
            // so nothing can be drawn on top of anything else.
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(padding)
                    .safeContentPadding()
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                GroupBox("Controls", ControlsColor, Modifier.fillMaxWidth()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { showContent = !showContent }) { Text("Click me!") }
                        // run the command off the main thread, then show the table
                        Button(onClick = {
                            scope.launch {
                                entries = withContext(Dispatchers.IO) { listDir() }
                                showList = true
                            }
                        }) { Text("Do Stuff") }
                    }
                }

                AnimatedVisibility(showContent) {
                    GroupBox("Dinorawr", DinoColor, Modifier.fillMaxWidth()) {
                        Dinorawr(onNotify = { msg ->
                            scope.launch { snackbarState.showSnackbar(msg) }
                        })
                    }
                }

                Counter()

                InputSections()

                if (showList) {
                    // fixed height: inside a scrolling column there is no "remaining space" to weight into
                    GroupBox("Files (${entries.size})", ListColor, Modifier.fillMaxWidth().height(320.dp)) {
                        FileTable(entries, Modifier.fillMaxWidth().weight(1f))
                    }
                }
            }
        }
    }
}
package id.xms.omakdroid.ui.screens

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun BootLogScreen(
    onBootComplete: () -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var visibleLogs by remember { mutableStateOf(listOf<BootLogEntry>()) }
    
    // Fetch real hardware data
    val hardwareInfo = remember { getHardwareInfo(context) }
    
    // Generate dynamic boot sequence
    val bootSequence = remember { generateBootSequence(hardwareInfo) }
    
    // Animate boot sequence
    LaunchedEffect(Unit) {
        for ((index, logEntry) in bootSequence.withIndex()) {
            visibleLogs = visibleLogs + logEntry
            val delayTime = when (logEntry.phase) {
                BootPhase.DMESG -> {
                    Random.nextLong(5, 40)
                }
                BootPhase.SYSTEMD -> {
                    val baseDelay = Random.nextLong(100, 400)
                    
                    // Add massive delays for specific heavy operations
                    val extraDelay = when {
                        logEntry.message.contains("Mounted") && logEntry.message.contains("RootFS") -> 2000L
                        logEntry.message.contains("Root Filesystem") -> 1800L
                        logEntry.message.contains("Network Manager") && logEntry.type == BootLogType.OK -> 1500L
                        logEntry.message.contains("Journal Service") -> 800L
                        logEntry.message.contains("File Systems") -> 1200L
                        logEntry.message.contains("Cryptographic") -> 600L
                        logEntry.message.contains("Multi-User System") -> 700L
                        else -> 0L
                    }
                    
                    baseDelay + extraDelay
                }
            }
            
            delay(delayTime)
        }
        delay(500)
        onBootComplete()
    }
    
    // Auto-scroll to bottom
    LaunchedEffect(visibleLogs.size) {
        if (visibleLogs.isNotEmpty()) {
            listState.animateScrollToItem(visibleLogs.size - 1)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            items(visibleLogs) { logEntry ->
                BootLogText(logEntry)
            }
        }
    }
}

@Composable
fun BootLogText(logEntry: BootLogEntry) {
    val annotatedString = buildAnnotatedString {
        when (logEntry.type) {
            BootLogType.OK -> {
                // Extract [  OK  ] part and color it green
                if (logEntry.message.startsWith("[  OK  ]")) {
                    withStyle(style = SpanStyle(color = Color(0xFF00FF00))) {
                        append("[  OK  ]")
                    }
                    withStyle(style = SpanStyle(color = Color(0xFFD3D3D3))) {
                        append(logEntry.message.substring(8))
                    }
                } else {
                    withStyle(style = SpanStyle(color = Color(0xFFD3D3D3))) {
                        append(logEntry.message)
                    }
                }
            }
            BootLogType.INFO -> {
                withStyle(style = SpanStyle(color = Color(0xFFD3D3D3))) {
                    append(logEntry.message)
                }
            }
            BootLogType.DMESG -> {
                // Kernel messages in light gray
                withStyle(style = SpanStyle(color = Color(0xFFC0C0C0))) {
                    append(logEntry.message)
                }
            }
        }
    }
    
    Text(
        text = annotatedString,
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        lineHeight = 13.sp
    )
}

data class BootLogEntry(
    val message: String,
    val type: BootLogType,
    val phase: BootPhase
)

enum class BootLogType {
    OK,
    INFO,
    DMESG
}

enum class BootPhase {
    DMESG,
    SYSTEMD
}

data class HardwareInfo(
    val model: String,
    val board: String,
    val hardware: String,
    val androidVersion: String,
    val fingerprint: String,
    val kernelVersion: String,
    val totalRamMB: Long,
    val cpuAbi: String
)

fun getHardwareInfo(context: Context): HardwareInfo {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)
    val totalRamMB = memoryInfo.totalMem / (1024 * 1024)
    
    return HardwareInfo(
        model = Build.MODEL,
        board = Build.BOARD,
        hardware = Build.HARDWARE,
        androidVersion = Build.VERSION.RELEASE,
        fingerprint = Build.FINGERPRINT,
        kernelVersion = System.getProperty("os.version") ?: "5.10.248-Chandelier-Velox",
        totalRamMB = totalRamMB,
        cpuAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"
    )
}

fun generateBootSequence(hw: HardwareInfo): List<BootLogEntry> {
    val logs = mutableListOf<BootLogEntry>()
    logs.add(BootLogEntry("[    0.000000] Booting Linux on physical CPU 0x0000000000 [0x${hw.cpuAbi}]", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.000000] Linux version ${hw.kernelVersion} (omakdroid@builder) (gcc version 11.2.0)", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.000000] Machine model: ${hw.model} (${hw.board})", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.000000] efi: UEFI not found.", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.000000] Reserved memory: created DMA memory pool at 0x0000000080000000", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.012301] CPU: ${hw.hardware} SoC initialized", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.018452] percpu: Embedded 24 pages/cpu s59416 r8192 d30680 u98304", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.045021] Memory: ${hw.totalRamMB}MB available (12288K kernel code, 2048K rwdata, 4096K rodata, 1024K init, 512K bss)", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.078234] SLUB: HWalign=64, Order=0-3, MinObjects=0, CPUs=8, Nodes=1", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.089456] rcu: Hierarchical RCU implementation.", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.102341] NR_IRQS: 64, nr_irqs: 64, preallocated irqs: 0", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.125678] arch_timer: cp15 timer(s) running at 19.20MHz (virt).", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.150231] random: get_random_bytes called from start_kernel+0x3c4/0x5e8", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.178945] Console: colour dummy device 80x25", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.201234] printk: console [tty0] enabled", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.234567] Calibrating delay loop... 4800.00 BogoMIPS (lpj=9600000)", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.267890] pid_max: default: 32768 minimum: 301", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.289123] Mount-cache hash table entries: 2048 (order: 2, 16384 bytes)", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.320145] smp: Bringing up secondary CPUs ...", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.345678] Detected VIPT I-cache on CPU1", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.367891] CPU1: Booted secondary processor 0x0000000001 [0x410fd034]", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.401234] CPU2: Booted secondary processor 0x0000000002 [0x410fd034]", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.434567] CPU3: Booted secondary processor 0x0000000003 [0x410fd034]", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.467890] CPU4: Booted secondary processor 0x0000000100 [0x410fd082]", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.501234] CPU5: Booted secondary processor 0x0000000101 [0x410fd082]", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.534567] CPU6: Booted secondary processor 0x0000000102 [0x410fd082]", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.567890] CPU7: Booted secondary processor 0x0000000103 [0x410fd082]", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.601234] smp: Brought up 1 node, 8 CPUs", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.634567] SMP: Total of 8 processors activated.", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.667890] devtmpfs: initialized", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.701234] clocksource: jiffies: mask: 0xffffffff max_cycles: 0xffffffff", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.734567] thermal_sys: Registered thermal governor 'step_wise'", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.767890] NET: Registered protocol family 16", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.801234] DMA: preallocated 256 KiB pool for atomic allocations", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.834567] cpuidle: using governor menu", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.852101] zram: Added device: zram0", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.889456] SCSI subsystem initialized", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.923789] usbcore: registered new interface driver usbfs", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.956123] usbcore: registered new device driver usb", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    0.989456] pps_core: LinuxPPS API ver. 1 registered", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    1.023789] PTP clock support registered", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    1.056123] Advanced Linux Sound Architecture Driver Initialized.", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    1.089456] NetLabel: Initializing", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    1.123789] clocksource: Switched to clocksource arch_sys_counter", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    1.156123] NET: Registered protocol family 2", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    1.189456] tcp_listen_portaddr_hash hash table entries: 512", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    1.204512] UFS: Host Controller initialized (Android ${hw.androidVersion} Host)", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    1.267890] mmc0: new HS400 MMC card at address 0001", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    1.345678] EXT4-fs (mmcblk0p1): mounted filesystem with ordered data mode", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    1.456789] VFS: Mounted root (ext4 filesystem) readonly on device 179:1.", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    1.567890] devtmpfs: mounted", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    1.678901] Freeing unused kernel memory: 1024K", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    1.789012] Run /init as init process", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    1.845120] init: init first stage started!", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("[    1.923456] init: Loading SELinux policy", BootLogType.DMESG, BootPhase.DMESG))
    logs.add(BootLogEntry("", BootLogType.INFO, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("[  OK  ] Reached target Local File Systems (Pre).", BootLogType.OK, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("[  OK  ] Mounted ${hw.fingerprint.take(40)}... as RootFS.", BootLogType.OK, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("[  OK  ] Mounted Root Filesystem.", BootLogType.OK, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("         Mounting Kernel Configuration File System...", BootLogType.INFO, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("[  OK  ] Mounted Kernel Configuration File System.", BootLogType.OK, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("[  OK  ] Reached target Local File Systems.", BootLogType.OK, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("         Starting Remount Root and Kernel File Systems...", BootLogType.INFO, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("[  OK  ] Started Remount Root and Kernel File Systems.", BootLogType.OK, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("         Starting Load Kernel Modules...", BootLogType.INFO, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("[  OK  ] Started Load Kernel Modules.", BootLogType.OK, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("         Starting Apply Kernel Variables...", BootLogType.INFO, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("[  OK  ] Started Apply Kernel Variables.", BootLogType.OK, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("[  OK  ] Started Journal Service.", BootLogType.OK, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("         Starting Flush Journal to Persistent Storage...", BootLogType.INFO, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("[  OK  ] Started Flush Journal to Persistent Storage.", BootLogType.OK, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("[  OK  ] Reached target Cryptographic Services.", BootLogType.OK, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("[  OK  ] Reached target System Initialization.", BootLogType.OK, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("[  OK  ] Started Daily Cleanup of Temporary Directories.", BootLogType.OK, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("[  OK  ] Reached target Timers.", BootLogType.OK, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("[  OK  ] Listening on D-Bus System Message Bus Socket.", BootLogType.OK, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("[  OK  ] Reached target Sockets.", BootLogType.OK, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("[  OK  ] Reached target Basic System.", BootLogType.OK, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("         Starting D-Bus System Message Bus...", BootLogType.INFO, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("[  OK  ] Started D-Bus System Message Bus.", BootLogType.OK, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("         Starting Network Manager...", BootLogType.INFO, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("[  OK  ] Started Network Manager.", BootLogType.OK, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("[  OK  ] Reached target Network.", BootLogType.OK, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("         Starting Permit User Sessions...", BootLogType.INFO, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("[  OK  ] Started Permit User Sessions.", BootLogType.OK, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("[  OK  ] Started OmakDroid Native Rust Daemon.", BootLogType.OK, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("[  OK  ] Started PRoot Container Manager.", BootLogType.OK, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("[  OK  ] Started System Logging Service.", BootLogType.OK, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("[  OK  ] Started Bluetooth service.", BootLogType.OK, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("[  OK  ] Reached target Bluetooth.", BootLogType.OK, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("[  OK  ] Started User Manager for UID 0.", BootLogType.OK, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("[  OK  ] Started Session 1 of user root.", BootLogType.OK, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("[  OK  ] Reached target User and Group Name Lookups.", BootLogType.OK, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("[  OK  ] Reached target Multi-User System.", BootLogType.OK, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("[  OK  ] Reached target Graphical Interface.", BootLogType.OK, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("         Starting User Login Prompts...", BootLogType.INFO, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("[  OK  ] Started Getty on tty1.", BootLogType.OK, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("", BootLogType.INFO, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("OmakDroid 24.04 LTS localhost tty1", BootLogType.INFO, BootPhase.SYSTEMD))
    logs.add(BootLogEntry("", BootLogType.INFO, BootPhase.SYSTEMD))
    
    return logs
}

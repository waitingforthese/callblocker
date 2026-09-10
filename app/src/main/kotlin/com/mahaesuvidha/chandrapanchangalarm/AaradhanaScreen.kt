package com.mahaesuvidha.chandrapanchangalarm

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mahaesuvidha.chandrapanchangalarm.model.AaradhanaMaster
import com.mahaesuvidha.chandrapanchangalarm.alarm.AlarmScheduler
import com.mahaesuvidha.chandrapanchangalarm.alarm.AaradhanaVoiceSession
import com.mahaesuvidha.chandrapanchangalarm.model.BirthProfile
import com.mahaesuvidha.chandrapanchangalarm.model.LiveMoonCalculator
import com.mahaesuvidha.chandrapanchangalarm.model.PanchangState
import com.mahaesuvidha.chandrapanchangalarm.settings.AaradhanaPrefs
import com.mahaesuvidha.chandrapanchangalarm.settings.GocharTaraAaradhanaPrefs
import com.mahaesuvidha.chandrapanchangalarm.model.Graha
import com.mahaesuvidha.chandrapanchangalarm.model.GocharTaraAaradhanaCalculator
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun AaradhanaScreen(
    profile: BirthProfile,
    panchang: PanchangState,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { AaradhanaPrefs(context.applicationContext) }
    var special by remember { mutableStateOf(prefs.specialHourly) }
    var japaCountText by remember { mutableStateOf(prefs.specialJapaCount.toString()) }
    var fixedTimesText by remember { mutableStateOf(prefs.specialFixedTimes) }
    var nakChangeAaradhana by remember { mutableStateOf(prefs.nakshatraChangeAaradhana) }
    var yogaChangeAaradhana by remember { mutableStateOf(prefs.yogaChangeAaradhana) }
    var karanaChangeAaradhana by remember { mutableStateOf(prefs.karanaChangeAaradhana) }
    var speechRate by remember { mutableStateOf(prefs.speechRate) }
    var savedPopup by remember { mutableStateOf(false) }
    val moon = remember { LiveMoonCalculator.getCurrentMoonState() }
    val nakInfo = AaradhanaMaster.forNakshatra(moon.nakshatra.marathi)
    val yogaInfo = AaradhanaMaster.forYoga(panchang.yoga)
    val karanaInfo = AaradhanaMaster.forKarana(panchang.karana)

    Column(Modifier.fillMaxSize().background(Color(0xFF07111F)).statusBarsPadding().navigationBarsPadding()) {
        Surface(Modifier.fillMaxWidth(), color = Color(0xFF07111F), shadowElevation = 5.dp) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("← परत", color = Color.White) }
                Text("🕉️ नक्षत्र आराधना", color = Color(0xFFFFC83D), fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    KundliReferenceButton(profile, textColor = Color.White)
                    TransitAnalysisButton(profile, textColor = Color.White)
                }
            }
        }
        Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).padding(14.dp)) {
        Spacer(Modifier.height(4.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF10253A))) {
            Column(Modifier.padding(14.dp)) {
                Text("👤 ${profile.name}", color = Color.White, fontWeight = FontWeight.Bold)
                Text("जन्म नक्षत्र: ${profile.birthNakshatra.ifBlank { "—" }}", color = Color.LightGray)
                Text("चंद्र नक्षत्र: ${moon.nakshatra.marathi} • चरण ${moon.pada}", color = Color.LightGray)
                Text("गोचर नक्षत्र: ${moon.nakshatra.marathi}", color = Color.LightGray)
                Text("योग: ${panchang.yoga}", color = Color.LightGray)
                Text("करण: ${panchang.karana}", color = Color.LightGray)
            }
        }
        Spacer(Modifier.height(10.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF10253A))) {
            Column(Modifier.padding(14.dp)) {
                Text("🔔 बदलाची आराधना", color = Color(0xFFFFC83D), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("नक्षत्र / योग / करण बदलल्यावर संबंधित देवता व मंत्रासह ११ जप आपोआप होतील.", color = Color.LightGray, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                MantraRow("🌟 नक्षत्र", moon.nakshatra.marathi, nakInfo.deity, nakInfo.mantra)
                MantraRow("🕉️ योग", panchang.yoga, yogaInfo.deity, yogaInfo.mantra)
                MantraRow("🔱 करण", panchang.karana, karanaInfo.deity, karanaInfo.mantra)
            }
        }
        Spacer(Modifier.height(10.dp))
        GocharTaraAaradhanaSection(profile, context)
        Spacer(Modifier.height(10.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF10253A))) {
            Column(Modifier.padding(14.dp)) {
                Text("🕉️ विशेष आराधना", color = Color(0xFFFFC83D), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("स्वयंचलित आराधना", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("ON असल्यास रोज ठरलेल्या घड्याळाच्या वेळेला आराधना होईल.", color = Color.LightGray, fontSize = 12.sp)
                    }
                    Switch(checked = special, onCheckedChange = {
                        special = it
                        prefs.specialHourly = it
                        val scheduler = AlarmScheduler(context.applicationContext)
                        if (it) scheduler.resetSpecialAaradhanaSchedule()
                        scheduler.scheduleAll()
                    })
                }

                Spacer(Modifier.height(12.dp))
                Text("⚙️ जपाची सेटिंग", color = Color(0xFFFFC83D), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(6.dp))

                OutlinedTextField(
                    value = fixedTimesText,
                    onValueChange = { fixedTimesText = it.filter { ch -> ch.isDigit() || ch == ':' || ch == ',' }.take(80) },
                    label = { Text("दररोज आराधना वेळा (HH:mm)") },
                    supportingText = { Text("उदा. 05:00,08:00,11:00,14:00,17:00,20:00,23:00 • कमाल 8 वेळा") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFFC83D), unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFFFFC83D), unfocusedLabelColor = Color.LightGray
                    )
                )

                Spacer(Modifier.height(10.dp))
                Text("🔔 बदलाची आराधना — Notification Settings पासून स्वतंत्र", color = Color(0xFFFFC83D), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("खालील आराधना ON असेल तर संबंधित Alarm Settings OFF असली तरी आराधना होईल.", color = Color.LightGray, fontSize = 12.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("🌙 नक्षत्र बदल आराधना", color = Color.White); Switch(nakChangeAaradhana, { nakChangeAaradhana = it; prefs.nakshatraChangeAaradhana = it }) }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("✨ योग बदल आराधना", color = Color.White); Switch(yogaChangeAaradhana, { yogaChangeAaradhana = it; prefs.yogaChangeAaradhana = it }) }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("🔔 करण बदल आराधना", color = Color.White); Switch(karanaChangeAaradhana, { karanaChangeAaradhana = it; prefs.karanaChangeAaradhana = it }) }

                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = japaCountText,
                        onValueChange = { japaCountText = it.filter(Char::isDigit).take(3) },
                        label = { Text("जप संख्या") },
                        supportingText = { Text("1 ते 108") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFFC83D),
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color(0xFFFFC83D),
                            unfocusedLabelColor = Color.LightGray
                        )
                    )
                }

                Spacer(Modifier.height(10.dp))
                Text("🐢 जपाचा आवाज किती संथ ठेवायचा", color = Color.White, fontWeight = FontWeight.SemiBold)
                Text("सध्याचा वेग: ${String.format(java.util.Locale.US, "%.2f", speechRate)}  • कमी = अधिक हळू", color = Color.LightGray, fontSize = 12.sp)
                Slider(
                    value = speechRate,
                    onValueChange = { speechRate = it },
                    valueRange = 0.35f..0.90f,
                    steps = 10,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("अधिक संथ", color = Color.LightGray, fontSize = 11.sp)
                    Text("जलद", color = Color.LightGray, fontSize = 11.sp)
                }

                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    val count = japaCountText.toIntOrNull()?.coerceIn(1, 108) ?: 11
                    japaCountText = count.toString()
                    val cleanTimes = fixedTimesText.split(',').mapNotNull { token ->
                        val parts = token.trim().split(':')
                        if (parts.size != 2) return@mapNotNull null
                        val h = parts[0].toIntOrNull() ?: return@mapNotNull null
                        val m = parts[1].toIntOrNull() ?: return@mapNotNull null
                        if (h !in 0..23 || m !in 0..59) null else String.format(java.util.Locale.US, "%02d:%02d", h, m)
                    }.distinct().take(8).joinToString(",")
                    if (cleanTimes.isNotBlank()) fixedTimesText = cleanTimes
                    prefs.specialJapaCount = count
                    prefs.specialFixedTimes = cleanTimes.ifBlank { "05:00,08:00,11:00,14:00,17:00,20:00,23:00" }
                    prefs.speechRate = speechRate

                    // Saving fixed clock times intentionally rebuilds the daily slots
                    // from this save action; unrelated Alarm Settings changes preserve them.
                    val scheduler = AlarmScheduler(context.applicationContext)
                    scheduler.resetSpecialAaradhanaSchedule()
                    scheduler.scheduleAll()

                    // Immediately play the currently active Aaradhana so the user can
                    // verify count, pronunciation and speech speed without waiting for an alarm.
                    val previewMantras = listOf(
                        nakInfo.mantra,
                        yogaInfo.mantra,
                        karanaInfo.mantra
                    ).filter { it.isNotBlank() }
                    AaradhanaVoiceSession.speakPreview(
                        context.applicationContext,
                        399,
                        previewMantras,
                        count
                    )
                    savedPopup = true
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("💾 जतन करा + आत्ताच आराधना ऐका", fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(6.dp))
                Text("ON असल्यास: नक्षत्र मंत्र → योग मंत्र → करण मंत्र • प्रत्येक मंत्रासाठी निवडलेली जप संख्या. आराधना रोजच्या fixed clock वेळेला होईल.", color = Color.LightGray, fontSize = 12.sp)
                Text("या विशेष आराधनेत कोणतीही घोषणा केली जाणार नाही.", color = Color.LightGray, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("📖 मार्गदर्शन स्वतंत्र Part मध्ये राहील.", color = Color.LightGray, fontSize = 13.sp)
    }

        }

    if (savedPopup) {
        AlertDialog(
            onDismissRequest = { savedPopup = false },
            title = { Text("✅ आराधना सेटिंग जतन झाली") },
            text = {
                Column {
                    Text("जप संख्या: $japaCountText")
                    Text("रोजच्या वेळा: $fixedTimesText")
                    Text("आवाजाचा वेग: ${String.format(java.util.Locale.US, "%.2f", speechRate)}")
                    Spacer(Modifier.height(8.dp))
                    Text("🕉️ सध्याची नक्षत्र + योग + करण आराधना आत्ताच सुरू केली आहे.")
                    Text("आवाज/स्पीड तपासण्यासाठी ऐका. पुढील alarm सेव्ह केलेल्या setting प्रमाणे schedule झाला आहे.")
                }
            },
            confirmButton = {
                TextButton(onClick = { savedPopup = false }) { Text("ठीक आहे") }
            }
        )
    }
}


@Composable
private fun GocharTaraAaradhanaSection(profile: BirthProfile, context: android.content.Context) {
    val gp = remember { GocharTaraAaradhanaPrefs(context.applicationContext) }
    var refresh by remember { mutableIntStateOf(0) }
    val current = remember(refresh, profile) { GocharTaraAaradhanaCalculator.current(profile) }
    val scheduler = remember { AlarmScheduler(context.applicationContext) }

    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF10253A))) {
        Column(Modifier.padding(14.dp)) {
            Text("🪐 गोचर ग्रह — विपत / प्रत्यारी / वध तारा आराधना", color = Color(0xFFFFC83D), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(4.dp))
            Text("जन्म नक्षत्राच्या संदर्भात प्रत्येक गोचर ग्रह कोणत्या विपत, प्रत्यारी किंवा वध तारानक्षत्रात प्रवेश करतो ते पाहता येईल. निवडलेल्या काळात प्रवेश होताच Notification + त्या ग्रहाची आराधना होईल.", color = Color.LightGray, fontSize = 12.sp)
            Text("जन्म नक्षत्र: ${profile.birthNakshatra.ifBlank { "—" }}", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            Graha.entries.forEach { graha ->
                val info = current.firstOrNull { it.graha == graha }
                var enabled by remember(graha) { mutableStateOf(gp.isEnabled(graha)) }
                var vipat by remember(graha) { mutableStateOf(gp.vipat(graha)) }
                var pratyari by remember(graha) { mutableStateOf(gp.pratyari(graha)) }
                var vadha by remember(graha) { mutableStateOf(gp.vadha(graha)) }
                val selected = buildList {
                    if (vipat) add("विपत")
                    if (pratyari) add("प्रत्यारी")
                    if (vadha) add("वध")
                }
                val targetText = selected.flatMap { tara ->
                    GocharTaraAaradhanaCalculator.targetNakshatras(profile.birthNakshatra, tara).map { "$tara: $it" }
                }.joinToString(" • ")

                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF17324A)), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(10.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("${graha.marathi} — ${info?.nakshatra ?: "—"}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("सध्या: ${info?.tara ?: "—"} • ${info?.longitude?.let { GocharTaraAaradhanaCalculator.degreeText(it) } ?: "—"}", color = Color.LightGray, fontSize = 12.sp)
                            }
                            Switch(checked = enabled, onCheckedChange = {
                                enabled = it
                                gp.setEnabled(graha, it)
                                scheduler.resetGocharTaraAaradhana()
                                scheduler.scheduleAll()
                                refresh++
                            })
                        }
                        Text("🎯 ${targetText.ifBlank { "निवड उपलब्ध नाही" }}", color = Color(0xFFFFC83D), fontSize = 12.sp)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Checkbox(vipat, { vipat = it; gp.setVipat(graha, it) })
                                Text("विपत", color = Color.White, fontSize = 12.sp)
                            }
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Checkbox(pratyari, { pratyari = it; gp.setPratyari(graha, it) })
                                Text("प्रत्यारी", color = Color.White, fontSize = 12.sp)
                            }
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Checkbox(vadha, { vadha = it; gp.setVadha(graha, it) })
                                Text("वध", color = Color.White, fontSize = 12.sp)
                            }
                        }
                        val nowMillis = System.currentTimeMillis()
                        val selectedSet = selected.toSet()
                        val activeTara = info?.tara?.takeIf { it in selectedSet }
                        val interval = if (activeTara != null) {
                            val back = nowMillis - 40L * 24L * 60L * 60L * 1000L
                            val entry = GocharTaraAaradhanaCalculator.nextMatchingEntryMillis(graha, profile.birthNakshatra, setOf(activeTara), back, nowMillis + 1000L)
                            entry?.let { it to GocharTaraAaradhanaCalculator.nextNakshatraBoundaryMillis(graha, nowMillis) }
                        } else {
                            GocharTaraAaradhanaCalculator.nextMatchingIntervalMillis(graha, profile.birthNakshatra, selectedSet, nowMillis, nowMillis + 370L * 24L * 60L * 60L * 1000L)
                        }
                        val fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm").withZone(ZoneId.of("Asia/Kolkata"))
                        val intervalText = interval?.let {
                            val a = fmt.format(Instant.ofEpochMilli(it.first))
                            val b = it.second?.let { x -> fmt.format(Instant.ofEpochMilli(x)) } ?: "—"
                            "सुरू: $a    समाप्त: $b"
                        } ?: "पुढील निवडलेल्या तारा नक्षत्राचा कालावधी उपलब्ध नाही"
                        Text("🕒 ${if (activeTara != null) "सध्या $activeTara तारा नक्षत्र" else "पुढील निवडलेला तारा कालावधी"}", color = Color(0xFFFFC83D), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(intervalText, color = Color.White, fontSize = 12.sp)
                        Text("हे वेळा App स्वतः गोचर नक्षत्राच्या अचूक प्रवेश/निर्गमनावरून काढतो; येथे Start/End manual नाहीत.", color = Color.LightGray, fontSize = 11.sp)
                        Spacer(Modifier.height(6.dp))
                        Button(onClick = {
                            gp.setEnabled(graha, enabled)
                            gp.setVipat(graha, vipat)
                            gp.setPratyari(graha, pratyari)
                            gp.setVadha(graha, vadha)
                            scheduler.resetGocharTaraAaradhana()
                            scheduler.scheduleAll()
                            refresh++
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("💾 ${graha.marathi} चे गोचर तारा सेटिंग जतन करा")
                        }
                    }
                }
            }
            Text("टीप: ‘प्रवेश’ झाल्यावरच आराधना trigger होते. एखादा ग्रह आधीपासून निवडलेल्या तारा नक्षत्रात असेल तर लगेच पुन्हा आराधना होत नाही; पुढील प्रवेशाची वाट पाहिली जाते.", color = Color.LightGray, fontSize = 11.sp)
        }
    }
}

@Composable
private fun MantraRow(label: String, value: String, deity: String, mantra: String) {
    Column(Modifier.padding(vertical = 5.dp)) {
        Text("$label: $value", color = Color.White, fontWeight = FontWeight.Bold)
        Text("🙏 अधिदेवता: $deity", color = Color(0xFFFFC83D), fontSize = 13.sp)
        Text("📿 $mantra", color = Color.White, fontSize = 13.sp)
    }
}

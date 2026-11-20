package ru.apokali4.aptechnayasmena

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

private val Ink = KenneyPalette.ink
private val Muted = KenneyPalette.muted
private val Paper = KenneyPalette.background
private val Green = KenneyPalette.purple
private val Pale = KenneyPalette.purpleSoft

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lessons = JSONArray(assets.open("lessons.json").bufferedReader().use { it.readText() })
        val practice = JSONArray(assets.open("practice.json").bufferedReader().use { it.readText() })
        val syllabus = JSONObject(assets.open("syllabus.json").bufferedReader().use { it.readText() })
        setContent { LearningApp(lessons, practice, syllabus) }
    }
}

@Composable
private fun PharmacyGame(lessons: JSONArray, practice: JSONArray, syllabus: JSONObject) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("pharmacy-save", 0) }
    var xp by remember { mutableIntStateOf(prefs.getInt("xp", 0)) }
    var correct by remember { mutableIntStateOf(prefs.getInt("correct", 0)) }
    var wrong by remember { mutableIntStateOf(prefs.getInt("wrong", 0)) }
    val studied = remember { mutableStateListOf<String>().apply { addAll(prefs.getStringSet("studied", emptySet()) ?: emptySet()) } }
    val practiced = remember { mutableStateListOf<String>().apply { addAll(prefs.getStringSet("practiced", emptySet()) ?: emptySet()) } }
    var tab by remember { mutableStateOf("Смена") }
    var lessonIndex by remember { mutableIntStateOf(0) }
    var practiceIndex by remember { mutableIntStateOf(-1) }
    var selected by remember { mutableIntStateOf(-1) }
    var feedback by remember { mutableStateOf("") }
    var examQuestions by remember { mutableStateOf(emptyList<Pair<Int, Int>>()) }
    var examIndex by remember { mutableIntStateOf(0) }
    var examCorrect by remember { mutableIntStateOf(0) }
    var examDone by remember { mutableStateOf(false) }
    var visitorIndex by remember { mutableIntStateOf(prefs.getInt("visitor", 0)) }
    var visitorChoice by remember { mutableIntStateOf(-1) }
    var visitorFeedback by remember { mutableStateOf("") }
    var search by remember { mutableStateOf("") }
    var playerX by remember { mutableIntStateOf(0) }
    var playerY by remember { mutableIntStateOf(1) }
    var zone by remember { mutableStateOf("Торговый зал") }
    var selectedDrugId by remember { mutableStateOf("") }

    fun persist() {
        prefs.edit().putInt("xp", xp).putInt("correct", correct).putInt("wrong", wrong)
            .putStringSet("studied", studied.toSet()).putStringSet("practiced", practiced.toSet())
            .putInt("visitor", visitorIndex).apply()
    }
    fun award(amount: Int, isCorrect: Boolean) {
        if (isCorrect) { correct++; xp += amount } else wrong++
        persist()
    }
    val theory = syllabus.getJSONArray("theory")
    val visitors = remember {
        listOf(
            Triple("Марина и сын", "«У сына снова кашель. В прошлый раз помог антибиотик, продайте такой же без рецепта. Я название примерно помню…»", listOf("Подобрать похожий антибиотик по памяти", "Не подбирать антибиотик самостоятельно; объяснить ограничения и направить к врачу", "Посоветовать использовать остатки прошлого курса")) to 1,
            Triple("Игорь", "«Я забыл принять лекарство утром. Сейчас выпить две таблетки, чтобы наверстать?»", listOf("Сказать принять двойную дозу", "Сказать всегда пропускать забытую дозу", "Уточнить препарат, проверить официальную инструкцию и передать вопрос фармацевту")) to 2,
            Triple("Виктория", "«После лекарства отекают губы, тяжело дышать и кружится голова. Может, купить что-нибудь от аллергии?»", listOf("Продать средство и предложить подождать", "Немедленно вызвать 112 или 103 и сообщить ответственному сотруднику", "Посоветовать уйти домой")) to 1,
            Triple("Анна Петровна", "«Я плохо вижу мелкий текст на упаковке и боюсь перепутать коробки. Можете крупно написать, что и когда принимать?»", listOf("Самостоятельно составить расписание по внешнему виду упаковок", "Попросить показать назначения и упаковки, привлечь фармацевта для сверки, помочь прочитать официальную инструкцию и предложить крупный шрифт без изменения схемы", "Отказать в помощи: пусть попросит родственников")) to 1
        )
    }

    MaterialTheme(colorScheme = lightColorScheme(primary = Green, secondary = KenneyPalette.mint, background = Paper, surface = Color.White, onSurface = Ink)) {
        Scaffold(containerColor = Paper, bottomBar = {
            NavigationBar(containerColor = Color.White) {
                listOf("Смена", "Препараты", "Академия", "Практика", "Экзамен").forEach { item ->
                    NavigationBarItem(selected = tab == item, onClick = { tab = item; selected = -1; feedback = "" },
                        icon = { KenneyNavIcon(when(item){"Смена"->"☀";"Препараты"->"✚";"Академия"->"▤";"Практика"->"▣";else->"✓"}, tab == item) },
                        label = { Text(item) })
                }
            }
        }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                Row(Modifier.fillMaxWidth().background(Color.White).padding(horizontal=20.dp, vertical=14.dp), verticalAlignment=Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("АПТЕЧНАЯ СМЕНА", fontWeight=FontWeight.Black, letterSpacing=1.5.sp, color=Ink)
                        Text("Учебная RPG по фармакологии", color=Muted, fontSize=12.sp)
                    }
                    Column(horizontalAlignment=Alignment.End) {
                        Text("★ $xp XP", color=Green, fontWeight=FontWeight.Bold)
                        Text("Уровень ${levelForXp(xp)}", color=Muted, fontSize=12.sp)
                    }
                }
                when(tab) {
                    "Смена" -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement=Arrangement.spacedBy(14.dp)) {
                        Header("Твоя смена начинается", "Четыре посетителя. Четыре решения. Без угадывания диагнозов.")
                        Panel {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("🏪 $zone", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
                                TextButton(onClick = { zone = if (zone == "Торговый зал") "Подсобка" else "Торговый зал"; playerX = 0; playerY = 1 }) { Text("Перейти ↔") }
                            }
                            PharmacyMap(playerX, playerY, zone, visitorIndex)
                            Text("Передвигайся по аптеке кнопками. Чтобы заговорить с посетителем, подойди к его маркеру.", color = Muted, fontSize = 12.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                ActionButton("←", playerX > 0, Modifier.weight(1f)) { playerX = (playerX - 1).coerceAtLeast(0) }
                                ActionButton("↑", playerY > 0, Modifier.weight(1f)) { playerY = (playerY - 1).coerceAtLeast(0) }
                                ActionButton("↓", playerY < 1, Modifier.weight(1f)) { playerY = (playerY + 1).coerceAtMost(1) }
                                ActionButton("→", playerX < 4, Modifier.weight(1f)) { playerX = (playerX + 1).coerceAtMost(4) }
                            }
                        }
                        GameShiftPanel(onReward = { award(it, true) })
                        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                            Stat("${studied.size}/72", "тем изучено", Modifier.weight(1f))
                            Stat("$correct", "верных ответов", Modifier.weight(1f))
                            Stat("${practiced.size}/37", "практика", Modifier.weight(1f))
                        }
                        Panel {
                            Text("ЕЛЕНА · СТАРШИЙ ФАРМАЦЕВТ", color=Green, fontWeight=FontWeight.Bold, fontSize=12.sp)
                            Text("«Выслушай посетителя, обрати внимание на опасные признаки. Не ставь диагноз и не угадывай препарат. Если сомневаешься, позови меня»", color=Ink)
                        }
                        if (visitorIndex < visitors.size && zone == "Торговый зал" && kotlin.math.abs(playerX - (visitorIndex + 1)) <= 1 && playerY == 0) {
                            val (visitor, answer) = visitors[visitorIndex]
                            Panel {
                                Text("ПОСЕТИТЕЛЬ ${visitorIndex+1} ИЗ 4", color=Green, fontWeight=FontWeight.Bold, fontSize=12.sp)
                                Text(visitor.first, fontSize=21.sp, fontWeight=FontWeight.Bold)
                                Text(visitor.second, color=Ink)
                                Text("Как поступить сотруднику аптеки?", fontWeight=FontWeight.Bold)
                                visitor.third.forEachIndexed { i, choice -> Choice(choice, visitorChoice == i, visitorFeedback.isNotEmpty()) { visitorChoice=i } }
                                if (visitorFeedback.isEmpty()) ActionButton("Ответить", visitorChoice >= 0) {
                                    val ok = visitorChoice == answer
                                    visitorFeedback = if(ok) "Ты выбрал безопасный алгоритм и не стал подменять консультацию специалиста." else "Это небезопасный вариант. Необходимо остановиться, не назначать лечение и передать вопрос ответственному специалисту."
                                    award(15, ok)
                                } else {
                                    Text(visitorFeedback, color=if(visitorChoice==answer) Green else Color(0xFF9B4D42), fontWeight=FontWeight.Medium)
                                    ActionButton(if(visitorIndex==3) "Завершить смену" else "Следующий посетитель", true) {
                                        visitorIndex++; visitorChoice=-1; visitorFeedback=""; persist()
                                    }
                                }
                            }
                        } else if (visitorIndex >= visitors.size) Panel {
                            Text("Первая смена завершена!", fontSize=22.sp, fontWeight=FontWeight.Bold)
                            Text("Все четыре ситуации разобраны. Повтори темы в Академии или закрепи знания практикой.")
                            ActionButton("Пройти смену ещё раз", true) { visitorIndex=0; visitorChoice=-1; visitorFeedback=""; persist() }
                        }
                        Panel {
                            Text("Аптека", fontWeight=FontWeight.Bold, fontSize=18.sp)
                            Text("Торговый зал  ·  Подсобка")
                            Text("Учебная карта помещений. Реальные складские и отпускные операции в игре не моделируются.", color=Muted, fontSize=13.sp)
                        }
                    }
                    "Препараты" -> {
                        val selectedDrug = drugCatalog.firstOrNull { it.id == selectedDrugId }
                        if (selectedDrug != null) Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            ActionButton("← К каталогу", true) { selectedDrugId = "" }
                            Header(selectedDrug.name, selectedDrug.inn)
                            DrugSection("Описание", selectedDrug.description)
                            DrugSection("Фармакотерапевтическая группа", selectedDrug.group)
                            DrugSection("Показания", selectedDrug.indications)
                            DrugSection("Противопоказания", selectedDrug.contraindications)
                            DrugSection("Побочные эффекты", selectedDrug.adverseEffects)
                            DrugSection("Фармакодинамика", selectedDrug.pharmacodynamics)
                            DrugSection("Фармакокинетика", selectedDrug.pharmacokinetics)
                            DrugSection("Лекарственные формы", selectedDrug.forms)
                            Panel { Text("ИСТОЧНИК", color = Green, fontWeight = FontWeight.Bold); Text(selectedDrug.source); Text(selectedDrug.sourceUrl, color = Green, fontSize = 12.sp) }
                            Text("Учебное краткое описание. Сведения зависят от производителя и формы выпуска. Перед применением сверяйся с актуальной официальной инструкцией.", color = Muted, fontSize = 12.sp)
                        } else Column(Modifier.fillMaxSize().padding(18.dp)) {
                            Header("Справочник препаратов", "Карточки с фармакологией и источниками. База будет расширяться проверенными монографиями.")
                            OutlinedTextField(search, { search = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Найти препарат или МНН") }, singleLine = true)
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(drugCatalog.filter { (it.name + " " + it.inn).contains(search, true) }) { drug ->
                                    Row(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(14.dp)).clickable { selectedDrugId = drug.id }.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f)) { Text(drug.name, fontWeight = FontWeight.Bold, color = Ink, fontSize = 17.sp); Text(drug.group, color = Muted, fontSize = 12.sp) }
                                        Text("›", color = Green, fontSize = 24.sp)
                                    }
                                }
                            }
                        }
                    }
                    "Академия" -> {
                        val current = lessons.getJSONObject(lessonIndex.coerceIn(0, lessons.length()-1))
                        if (feedback.startsWith("LESSON")) Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
                            ActionButton("← К списку тем", true) { feedback=""; selected=-1 }
                            Header("Тема ${current.optInt("number")} из 72", theory.optJSONObject(lessonIndex)?.optString("title") ?: "Фармакология")
                            Panel { Text("КРАТКИЙ КОНСПЕКТ", color=Green, fontWeight=FontWeight.Bold); Text(current.optString("summary")) }
                            Panel {
                                Text("ЧТО НУЖНО ЗАПОМНИТЬ", color=Green, fontWeight=FontWeight.Bold)
                                val points=current.getJSONArray("keyPoints"); for(i in 0 until points.length()) Text("• ${points.getString(i)}")
                            }
                            Panel {
                                Text("ТЕРМИНЫ", color=Green, fontWeight=FontWeight.Bold)
                                val terms=current.getJSONArray("terms"); for(i in 0 until terms.length()) Text("• ${terms.getString(i)}")
                            }
                            val q=current.getJSONArray("quiz").getJSONObject(0)
                            Panel {
                                Text("ПРОВЕРЬ СЕБЯ", color=Green, fontWeight=FontWeight.Bold)
                                Text(q.getString("question"), fontWeight=FontWeight.Bold)
                                val opts=q.getJSONArray("choices")
                                for(i in 0 until opts.length()) Choice(opts.getString(i),selected==i,feedback.startsWith("ANSWER")){selected=i}
                                if(!feedback.startsWith("ANSWER")) ActionButton("Проверить ответ",selected>=0) {
                                    val ok=selected==q.getInt("correctIndex"); award(10,ok)
                                    feedback="ANSWER|${if(ok)"Верно! +10 XP" else "Пока неверно"}|${q.getString("explanation")}"
                                } else {
                                    Text(feedback.substringAfterLast("|"))
                                    ActionButton("Завершить урок",true) { studied.add(current.getString("id")); persist(); feedback=""; selected=-1 }
                                }
                            }
                        } else Column(Modifier.fillMaxSize().padding(18.dp)) {
                            Header("Академия фармакологии", "72 темы из предоставленного экзаменационного списка")
                            OutlinedTextField(search,{search=it},modifier=Modifier.fillMaxWidth(),placeholder={Text("Найти тему или номер…")},singleLine=true)
                            LazyColumn(verticalArrangement=Arrangement.spacedBy(7.dp)) {
                                items((0 until theory.length()).filter { val t=theory.getJSONObject(it); "${t.optInt("number")} ${t.optString("title")}".contains(search,true) }) { i ->
                                    val t=theory.getJSONObject(i)
                                    Row(Modifier.fillMaxWidth().background(Color.White,RoundedCornerShape(14.dp)).clickable { lessonIndex=i; feedback="LESSON"; selected=-1 }.padding(14.dp),verticalAlignment=Alignment.CenterVertically) {
                                        Text("${t.optInt("number")}",color=Green,fontWeight=FontWeight.Bold,modifier=Modifier.width(34.dp))
                                        Text(t.optString("title"),Modifier.weight(1f),color=Ink)
                                        Text(if(studied.contains(t.optString("id")))"✓" else "›",color=Green)
                                    }
                                }
                            }
                        }
                    }
                    "Практика" -> if(practiceIndex>=0) {
                        val task=practice.getJSONObject(practiceIndex)
                        var form by remember(practiceIndex){mutableStateOf("")}
                        var route by remember(practiceIndex){mutableStateOf("")}
                        var result by remember(practiceIndex){mutableStateOf("")}
                        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                            ActionButton("← К заданиям",true){practiceIndex=-1}
                            Header("Задание ${task.optInt("number")} из 37",task.optString("sourceText"))
                            Panel {
                                Text("ЛЕКАРСТВЕННАЯ ФОРМА",fontWeight=FontWeight.Bold,color=Green)
                                (0 until practice.length()).map { practice.getJSONObject(it).optString("form") to practice.getJSONObject(it).optString("formLabel") }.distinctBy { it.first }.forEach { Choice(it.second,form==it.first,result.isNotEmpty()){form=it.first} }
                            }
                            Panel {
                                Text("ПУТЬ ПРИМЕНЕНИЯ",fontWeight=FontWeight.Bold,color=Green)
                                (0 until practice.length()).map { practice.getJSONObject(it).optString("route") to practice.getJSONObject(it).optString("routeLabel") }.distinctBy { it.first }.forEach { Choice(it.second,route==it.first,result.isNotEmpty()){route=it.first} }
                            }
                            if(result.isEmpty()) ActionButton("Проверить",form.isNotEmpty()&&route.isNotEmpty()) {
                                val ok=form==task.optString("form")&&route==task.optString("route")
                                result=if(ok)"Верно! +10 XP" else "Есть ошибка. Сверь форму и путь применения."
                                if(ok&&!practiced.contains(task.optString("id"))){practiced.add(task.getString("id"));award(10,true)} else if(!ok) award(0,false)
                                persist()
                            } else Panel {
                                Text(result,fontWeight=FontWeight.Bold,color=if(result.startsWith("Верно"))Green else Color(0xFF9B4D42))
                                Text(task.optString("notice"),color=Muted)
                                ActionButton("К списку заданий",true){practiceIndex=-1}
                            }
                        }
                    } else Column(Modifier.fillMaxSize().padding(18.dp)) {
                        Header("Рецептурная лаборатория","37 практических заданий из исходного списка. Это учебные упражнения, не назначения пациентам.")
                        LazyColumn(verticalArrangement=Arrangement.spacedBy(7.dp)) {
                            items((0 until practice.length()).toList()) { i ->
                                val t=practice.getJSONObject(i)
                                Row(Modifier.fillMaxWidth().background(Color.White,RoundedCornerShape(14.dp)).clickable{practiceIndex=i}.padding(14.dp),verticalAlignment=Alignment.CenterVertically) {
                                    Text("${t.optInt("number")}",Modifier.width(32.dp),color=Green,fontWeight=FontWeight.Bold)
                                    Column(Modifier.weight(1f)){Text(t.optString("sourceText"),color=Ink);Text(if(practiced.contains(t.optString("id")))"Попытка сохранена" else "Учебное задание",color=Muted,fontSize=11.sp)}
                                    Text("›",color=Green)
                                }
                            }
                        }
                    }
                    "Экзамен" -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                        Header("Итоговый экзамен","20 случайных вопросов по курсу. Результат не влияет на XP.")
                        if(examQuestions.isEmpty() || examDone) {
                            if(examDone) Panel { Text("Результат: $examCorrect из 20",fontSize=24.sp,fontWeight=FontWeight.Bold);Text("${examCorrect*5}% правильных ответов") }
                            ActionButton(if(examDone)"Пройти ещё раз" else "Начать экзамен",true) {
                                examQuestions=(0 until lessons.length()).shuffled().take(20).map{it to Random.nextInt(lessons.getJSONObject(it).getJSONArray("quiz").length())}
                                examIndex=0;examCorrect=0;examDone=false;selected=-1
                            }
                        } else {
                            val (li,qi)=examQuestions[examIndex];val q=lessons.getJSONObject(li).getJSONArray("quiz").getJSONObject(qi)
                            Panel {
                                Text("ВОПРОС ${examIndex+1} ИЗ 20",color=Green,fontWeight=FontWeight.Bold)
                                Text(q.getString("question"),fontWeight=FontWeight.Bold,fontSize=18.sp)
                                val opts=q.getJSONArray("choices");for(i in 0 until opts.length()) Choice(opts.getString(i),selected==i,false){selected=i}
                                ActionButton("Ответить",selected>=0) { if(selected==q.getInt("correctIndex"))examCorrect++;selected=-1;if(examIndex==19)examDone=true else examIndex++ }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun PharmacyMap(playerX: Int, playerY: Int, zone: String, visitorIndex: Int) {
    KenneyPanel(modifier = Modifier.fillMaxWidth(), tint = Color(0xFFECE5F4), contentPadding = PaddingValues(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        if (zone == "Торговый зал") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("СТЕЛЛАЖИ", color = Muted, fontSize = 10.sp); Text("КАССА", color = Muted, fontSize = 10.sp) }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { repeat(5) { Text("▥", color = Color(0xFF9B795B), fontSize = 24.sp) } }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { repeat(5) { i ->
                val active = playerX == i && playerY == 0
                val client = i in 1..4 && visitorIndex == i - 1
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(if (client) listOf("👩🏻‍👦", "🧔🏻", "😰", "👵🏻")[i - 1] else if (active) "🧑" else "·", fontSize = 22.sp)
                    Text(if (active) "ТЫ" else if (client) "КЛИЕНТ" else "", fontSize = 8.sp, color = Green, fontWeight = FontWeight.Bold)
                }
            } }
            Text("ВХОД · ТОРГОВЫЙ ЗАЛ", color = Muted, fontSize = 10.sp)
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) { Text("📦\nСтеллажи"); Text("🗃️\nПриёмка"); Text("🧾\nУчёт") }
            Text("🧑 Ты в подсобке", color = Green, fontWeight = FontWeight.Bold)
        }
    }
}
@Composable private fun DrugSection(title: String, body: String) {
    Column(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).padding(15.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title.uppercase(), color = Green, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text(body, color = Ink, lineHeight = 21.sp)
    }
}
@Composable private fun Header(title:String, subtitle:String) { Column(verticalArrangement=Arrangement.spacedBy(5.dp)){Text(title,fontSize=25.sp,fontWeight=FontWeight.Black,color=KenneyPalette.purpleDark,lineHeight=29.sp);Text(subtitle,color=Muted,fontSize=14.sp,lineHeight=20.sp)} }
@Composable private fun Stat(value:String,label:String,modifier:Modifier=Modifier) { KenneyPanel(modifier=modifier,contentPadding=PaddingValues(12.dp),verticalArrangement=Arrangement.spacedBy(3.dp)){Text(value,fontSize=22.sp,fontWeight=FontWeight.Bold,color=Green);Text(label,fontSize=11.sp,color=Muted)} }
@Composable private fun Panel(content:@Composable ColumnScope.()->Unit) { KenneyPanel(contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp),content=content) }
@Composable private fun Choice(label:String,selected:Boolean,disabled:Boolean,onClick:()->Unit) { KenneyChoice(label,selected,enabled=!disabled,onClick=onClick) }
@Composable private fun ActionButton(label:String,enabled:Boolean,modifier:Modifier=Modifier,onClick:()->Unit) { KenneyButton(label,enabled=enabled,modifier=modifier,onClick=onClick) }

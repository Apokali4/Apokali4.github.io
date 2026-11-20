package ru.apokali4.aptechnayasmena
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray

private val Purple = KenneyPalette.purple
private val Dark = KenneyPalette.purpleDark
private val Bg = KenneyPalette.background
private val Mint = KenneyPalette.mint

@Composable
fun LearningApp(lessons: JSONArray, practice: JSONArray, syllabus: org.json.JSONObject) {
 var tab by remember { mutableStateOf("Главная") }
 var card by remember { mutableIntStateOf(0) }
 var reveal by remember { mutableStateOf(false) }
 var mode by remember { mutableStateOf(false) }
 var qi by remember { mutableIntStateOf(0) }
 var selected by remember { mutableIntStateOf(-1) }
 var answered by remember { mutableStateOf(false) }
 var search by remember { mutableStateOf("") }
 var drug by remember { mutableStateOf<DrugMonograph?>(null) }
 var topicIndex by remember { mutableIntStateOf(-1) }
 var practiceIndex by remember { mutableIntStateOf(-1) }
 var practiceAnswer by remember { mutableIntStateOf(-1) }
 var practiceChecked by remember { mutableStateOf(false) }
 val topics = remember { syllabus.optJSONArray("theory") ?: JSONArray() }
 val qs = listOf(
  Triple("К какой группе относится ибупрофен?", listOf("Антибиотики","НПВП","Антигистаминные"), 1) to "Ибупрофен — нестероидный противовоспалительный препарат. Он ингибирует ЦОГ и снижает синтез простагландинов.",
  Triple("Антибиотики лечат вирусную простуду.", listOf("Правда","Миф"), 1) to "Миф. Антибиотики действуют на бактерии, а не на вирусы.",
  Triple("Что означает МНН?", listOf("Международное непатентованное наименование","Минимальная назначенная норма","Медицинская номенклатура"), 0) to "МНН — единое название действующего вещества, не зависящее от торговой марки.",
  Triple("Какой орган участвует в метаболизме многих лекарств?", listOf("Кожа","Печень","Селезёнка"), 1) to "Печень участвует в метаболизме многих лекарственных веществ."
 )
 val q = qs[qi % qs.size]
 MaterialTheme(colorScheme = lightColorScheme(primary=Purple,secondary=Mint,background=Bg,surface=Color.White,onSurface=Dark)) {
  Scaffold(containerColor=Bg,bottomBar={
   NavigationBar(containerColor=Color.White) {
    listOf("Главная","Карточки","Играть","Препараты","Темы","Практика").forEach { t ->
     NavigationBarItem(selected=tab==t,onClick={tab=t;drug=null;mode=false;topicIndex=-1;practiceIndex=-1},icon={KenneyNavIcon(when(t){"Главная"->"⌂";"Карточки"->"▤";"Играть"->"✦";"Препараты"->"✚";"Темы"->"☷";else->"✎"},tab==t)},label={Text(t,fontSize=10.sp)})
    }
   }
  }) { pad ->
   Column(Modifier.fillMaxSize().padding(pad)) {
    KenneyPanel(modifier=Modifier.fillMaxWidth(),tint=Dark,contentPadding=PaddingValues(14.dp),verticalArrangement=Arrangement.spacedBy(0.dp)) {
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
     Column(Modifier.weight(1f)) { Text("АПТЕКА ЗНАНИЙ",color=Color.White,fontWeight=FontWeight.Black,letterSpacing=1.2.sp); Text("Учись, играй, запоминай",color=Color(0xFFD9C9F5),fontSize=12.sp) }
     Image(painterResource(R.drawable.pharmacist),contentDescription="Помощник",modifier=Modifier.size(46.dp),contentScale=ContentScale.Crop)
    }
    }
    when(tab) {
     "Главная" -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
      KenneyPanel(modifier=Modifier.fillMaxWidth(),tint=Dark,contentPadding=PaddingValues(12.dp),verticalArrangement=Arrangement.spacedBy(0.dp)) {
       Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
       Image(painterResource(R.drawable.pharmacist),contentDescription="Фармацевт-помощник",modifier=Modifier.width(116.dp).height(178.dp),contentScale=ContentScale.Crop)
       Column(Modifier.weight(1f).padding(8.dp),verticalArrangement=Arrangement.spacedBy(9.dp)) {
        Text("Пять минут —\nи ты знаешь больше!",color=Color.White,fontSize=22.sp,fontWeight=FontWeight.Black,lineHeight=27.sp)
        Text("Карточки, вопросы и понятные объяснения.",color=Color(0xFFE3D8F8),fontSize=13.sp)
        KenneyButton("Начать игру →",onClick={tab="Играть";mode=true;qi=0;selected=-1;answered=false})
       }
      }
      }
      Text("Чем займёмся?",fontSize=21.sp,fontWeight=FontWeight.Bold)
      Tile("▤","Карточки","Переворачивай и запоминай",Color(0xFFEDE4FF)){tab="Карточки"}
      Tile("✦","Играть","Викторины и правда или миф",Color(0xFFE0F5EF)){tab="Играть"}
      Tile("✚","Препараты","Понятные карточки лекарств",Color(0xFFFFEBD9)){tab="Препараты"}
      Tile("☷","Предметы","Фармакология и другие темы",Color(0xFFE2EDFF)){tab="Темы"}
      Tile("✎","Практика","Задания на лекарственные формы",Color(0xFFFFEBD9)){tab="Практика"}
     }
     "Карточки" -> Column(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
      Heading("Карточки","Нажми на карточку, чтобы увидеть ответ")
      val d=drugCatalog[card%drugCatalog.size]
      KenneyPanel(modifier=Modifier.fillMaxWidth().weight(1f).clickable{reveal=!reveal},contentPadding=PaddingValues(22.dp),verticalArrangement=Arrangement.Center,horizontalAlignment=Alignment.CenterHorizontally,fillContentHeight=true) {
       Text(if(reveal)"ОТВЕТ" else "ВОПРОС",color=Purple,fontWeight=FontWeight.Bold)
       Spacer(Modifier.height(20.dp))
       Text(if(reveal)d.description else d.name,fontSize=24.sp,fontWeight=FontWeight.Bold,color=Dark)
       Spacer(Modifier.height(12.dp))
       Text(if(reveal)d.group else "К какой группе относится препарат?",color=Color.Gray)
       Spacer(Modifier.height(20.dp))
       Text(if(reveal)"Нажми, чтобы вернуться" else "Показать ответ ↓",color=Purple,fontWeight=FontWeight.Bold)
      }
      Text((card+1).toString()+" из "+drugCatalog.size,modifier=Modifier.align(Alignment.CenterHorizontally),color=Color.Gray)
      Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
       KenneyButton("Назад",modifier=Modifier.weight(1f),outlined=true,onClick={card=(card-1+drugCatalog.size)%drugCatalog.size;reveal=false})
       KenneyButton("Следующая",modifier=Modifier.weight(1f),onClick={card=(card+1)%drugCatalog.size;reveal=false})
      }
     }
     "Играть" -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
      if(!mode) {
       Heading("Выбери игру","Коротко, понятно и с объяснениями")
       Tile("?","Викторина","Вопросы по фармакологии",Color(0xFFEDE4FF)){mode=true;qi=0;selected=-1;answered=false}
       Tile("✓","Правда или миф","Разбирай заблуждения",Color(0xFFE0F5EF)){mode=true;qi=1;selected=-1;answered=false}
       Tile("⌕","Быстрый опрос","Проверь, что запомнил",Color(0xFFFFEBD9)){mode=true;qi=2;selected=-1;answered=false}
      } else {
       KenneyTextButton("← Все игры"){mode=false}
       Heading("Викторина","Вопрос "+(qi+1))
       KenneyPanel(modifier=Modifier.fillMaxWidth(),contentPadding=PaddingValues(18.dp)) {
        Text(q.first.first,fontSize=20.sp,fontWeight=FontWeight.Bold)
        q.first.second.forEachIndexed { i,opt ->
         val bg=when{answered&&i==q.first.third->Color(0xFFDDF5E8);answered&&selected==i->Color(0xFFFFE1E1);selected==i->Color(0xFFEDE4FF);else->Bg}
         Row(Modifier.fillMaxWidth().background(bg,RoundedCornerShape(12.dp)).clickable(enabled=!answered){selected=i}.padding(14.dp),verticalAlignment=Alignment.CenterVertically){Text(if(answered&&i==q.first.third)"✓  " else "○  ",color=Mint);Text(opt,color=Dark)}
        }
        if(!answered) KenneyButton("Проверить",enabled=selected>=0,onClick={answered=true})
        else {
         Text(if(selected==q.first.third)"Верно! Отличная работа." else "Не совсем. Вот что важно запомнить:",color=if(selected==q.first.third)Mint else Color(0xFFB34F56),fontWeight=FontWeight.Bold)
         Text(q.second)
         KenneyButton("Дальше",onClick={qi++;selected=-1;answered=false})
        }
       }
      }
     }
     "Препараты" -> if(drug!=null) {
      val d=drug!!
      Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(9.dp)) {
       KenneyTextButton("← Каталог"){drug=null}
       Heading(d.name,d.inn)
       Info("Описание",d.description);Info("Группа",d.group);Info("Показания",d.indications);Info("Противопоказания",d.contraindications);Info("Побочные эффекты",d.adverseEffects);Info("Фармакодинамика",d.pharmacodynamics);Info("Фармакокинетика",d.pharmacokinetics);Info("Формы выпуска",d.forms);Info("Источник",d.source+"\n"+d.sourceUrl)
       Text("Учебная информация. Всегда сверяйся с актуальной инструкцией конкретного препарата.",color=Color.Gray,fontSize=12.sp)
      }
     } else Column(Modifier.fillMaxSize().padding(16.dp)) {
      Heading("Препараты","Изучай действующие вещества и особенности")
      OutlinedTextField(search,{search=it},modifier=Modifier.fillMaxWidth(),placeholder={Text("Поиск препарата")},singleLine=true)
      LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)){items(drugCatalog.filter{(it.name+it.inn).contains(search,true)}){d->Tile("✚",d.name,d.group,Color.White){drug=d}}}
     }
     "Темы" -> if(topicIndex>=0) {
      val t=topics.optJSONObject(topicIndex)
      val lesson=lessons.optJSONObject(topicIndex)
      Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
       KenneyTextButton("← Все темы"){topicIndex=-1}
       Heading("Тема " + (topicIndex+1),t?.optString("title") ?: "Материал")
       Info("Кратко",lesson?.optString("summary") ?: "Для этой темы пока нет конспекта.")
       val points=lesson?.optJSONArray("keyPoints") ?: JSONArray()
       if(points.length()>0) {
        Text("Главное",fontWeight=FontWeight.Bold,color=Dark,fontSize=18.sp)
        for(i in 0 until points.length()) Info("Пункт " + (i+1),points.optString(i))
       }
       KenneyButton("Перейти к карточкам препаратов",onClick={tab="Карточки";card=topicIndex%drugCatalog.size;reveal=false})
      }
     } else Column(Modifier.fillMaxSize().padding(16.dp)) {
      Heading("Предметы","Учебная программа и короткие конспекты")
      LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)){items((0 until topics.length()).toList()){i->val t=topics.optJSONObject(i);Tile("▣",t?.optString("title","Тема "+(i+1))?:"Тема "+(i+1),"Тема " + (i+1) + " · открыть конспект",Color.White){topicIndex=i}}}
     }
     "Практика" -> if(practiceIndex>=0) {
      val task=practice.optJSONObject(practiceIndex)
      val forms=listOf("solution" to "Раствор","tablet" to "Таблетки","capsule" to "Капсулы","ointment" to "Мазь","powder" to "Порошок","suspension" to "Суспензия")
      Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
       KenneyTextButton("← Все задания"){practiceIndex=-1;practiceAnswer=-1;practiceChecked=false}
       Heading("Задание " + (task?.optInt("number") ?: practiceIndex+1),"Определи лекарственную форму")
       Info("Условие",task?.optString("sourceText") ?: "Практическое задание")
       forms.forEachIndexed { i,p ->
        KenneyChoice(p.second,practiceAnswer==i,enabled=!practiceChecked,onClick={practiceAnswer=i})
       }
       if(!practiceChecked) KenneyButton("Проверить",enabled=practiceAnswer>=0,onClick={practiceChecked=true})
       else {
        val correct=forms.indexOfFirst{it.first==task?.optString("form")}
        Info(if(practiceAnswer==correct)"Верно!" else "Разбор ответа","Правильная форма: " + (task?.optString("formLabel") ?: "не указана") + ". " + (task?.optString("notice") ?: ""))
        KenneyButton("К списку заданий",onClick={practiceIndex=-1;practiceAnswer=-1;practiceChecked=false})
       }
      }
     } else Column(Modifier.fillMaxSize().padding(16.dp)) {
      Heading("Практика","Учебные упражнения на распознавание лекарственных форм")
      LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)){items((0 until practice.length()).toList()){i->val p=practice.optJSONObject(i);Tile("✎","Задание " + (p?.optInt("number") ?: i+1),p?.optString("sourceText") ?: "Практическая задача",Color.White){practiceIndex=i;practiceAnswer=-1;practiceChecked=false}}}
     }
    }
   }
  }
 }
}
@Composable private fun Heading(a:String,b:String){Column(verticalArrangement=Arrangement.spacedBy(4.dp)){Text(a,fontSize=25.sp,fontWeight=FontWeight.Black,color=Dark);Text(b,color=Color.Gray,fontSize=13.sp)}}
@Composable private fun Tile(icon:String,title:String,sub:String,color:Color,onClick:()->Unit){KenneyTile(icon,title,sub,tint=KenneyPalette.panelTint,onClick=onClick)}
@Composable private fun Info(title:String,body:String){KenneyPanel(contentPadding=PaddingValues(14.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){Text(title,color=Purple,fontWeight=FontWeight.Bold);Text(body,color=Dark,fontSize=14.sp,lineHeight=20.sp)}}

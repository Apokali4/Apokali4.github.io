package ru.apokali4.aptechnayasmena
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray

private val Purple = Color(0xFF7652B8)
private val Dark = Color(0xFF39265F)
private val Bg = Color(0xFFF5F1FC)
private val Mint = Color(0xFF48A994)

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
    listOf("Главная","Карточки","Играть","Препараты","Темы").forEach { t ->
     NavigationBarItem(selected=tab==t,onClick={tab=t;drug=null;mode=false},icon={Text(when(t){"Главная"->"⌂";"Карточки"->"▤";"Играть"->"✦";"Препараты"->"✚";else->"☷"},fontSize=19.sp)},label={Text(t,fontSize=10.sp)})
    }
   }
  }) { pad ->
   Column(Modifier.fillMaxSize().padding(pad)) {
    Row(Modifier.fillMaxWidth().background(Dark).padding(18.dp),verticalAlignment=Alignment.CenterVertically) {
     Column(Modifier.weight(1f)) { Text("АПТЕКА ЗНАНИЙ",color=Color.White,fontWeight=FontWeight.Black,letterSpacing=1.2.sp); Text("Учись, играй, запоминай",color=Color(0xFFD9C9F5),fontSize=12.sp) }
     Text("✿",color=Color(0xFFFFD77A),fontSize=27.sp)
    }
    when(tab) {
     "Главная" -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
      Row(Modifier.fillMaxWidth().background(Dark,RoundedCornerShape(22.dp)).padding(12.dp),verticalAlignment=Alignment.CenterVertically) {
       Image(painterResource(R.drawable.pharmacist),contentDescription="Фармацевт-помощник",modifier=Modifier.width(116.dp).height(178.dp),contentScale=ContentScale.Crop)
       Column(Modifier.weight(1f).padding(8.dp),verticalArrangement=Arrangement.spacedBy(9.dp)) {
        Text("Пять минут —\nи ты знаешь больше!",color=Color.White,fontSize=22.sp,fontWeight=FontWeight.Black,lineHeight=27.sp)
        Text("Карточки, вопросы и понятные объяснения.",color=Color(0xFFE3D8F8),fontSize=13.sp)
        Button(onClick={tab="Играть";mode=true;qi=0;selected=-1;answered=false},colors=ButtonDefaults.buttonColors(containerColor=Mint)){Text("Начать игру →")}
       }
      }
      Text("Чем займёмся?",fontSize=21.sp,fontWeight=FontWeight.Bold)
      Tile("▤","Карточки","Переворачивай и запоминай",Color(0xFFEDE4FF)){tab="Карточки"}
      Tile("✦","Играть","Викторины и правда или миф",Color(0xFFE0F5EF)){tab="Играть"}
      Tile("✚","Препараты","Понятные карточки лекарств",Color(0xFFFFEBD9)){tab="Препараты"}
      Tile("☷","Предметы","Фармакология и другие темы",Color(0xFFE2EDFF)){tab="Темы"}
     }
     "Карточки" -> Column(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
      Heading("Карточки","Нажми на карточку, чтобы увидеть ответ")
      val d=drugCatalog[card%drugCatalog.size]
      Column(Modifier.fillMaxWidth().weight(1f).background(Color.White,RoundedCornerShape(24.dp)).clickable{reveal=!reveal}.padding(22.dp),verticalArrangement=Arrangement.Center,horizontalAlignment=Alignment.CenterHorizontally) {
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
       OutlinedButton(onClick={card=(card-1+drugCatalog.size)%drugCatalog.size;reveal=false},modifier=Modifier.weight(1f)){Text("Назад")}
       Button(onClick={card=(card+1)%drugCatalog.size;reveal=false},modifier=Modifier.weight(1f)){Text("Следующая")}
      }
     }
     "Играть" -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
      if(!mode) {
       Heading("Выбери игру","Коротко, понятно и с объяснениями")
       Tile("?","Викторина","Вопросы по фармакологии",Color(0xFFEDE4FF)){mode=true;qi=0;selected=-1;answered=false}
       Tile("✓","Правда или миф","Разбирай заблуждения",Color(0xFFE0F5EF)){mode=true;qi=1;selected=-1;answered=false}
       Tile("⌕","Быстрый опрос","Проверь, что запомнил",Color(0xFFFFEBD9)){mode=true;qi=2;selected=-1;answered=false}
      } else {
       TextButton(onClick={mode=false}){Text("← Все игры")}
       Heading("Викторина","Вопрос "+(qi+1))
       Column(Modifier.fillMaxWidth().background(Color.White,RoundedCornerShape(20.dp)).padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
        Text(q.first.first,fontSize=20.sp,fontWeight=FontWeight.Bold)
        q.first.second.forEachIndexed { i,opt ->
         val bg=when{answered&&i==q.first.third->Color(0xFFDDF5E8);answered&&selected==i->Color(0xFFFFE1E1);selected==i->Color(0xFFEDE4FF);else->Bg}
         Row(Modifier.fillMaxWidth().background(bg,RoundedCornerShape(12.dp)).clickable(enabled=!answered){selected=i}.padding(14.dp),verticalAlignment=Alignment.CenterVertically){Text(if(answered&&i==q.first.third)"✓  " else "○  ",color=Mint);Text(opt,color=Dark)}
        }
        if(!answered) Button(onClick={answered=true},enabled=selected>=0,modifier=Modifier.fillMaxWidth()){Text("Проверить")}
        else {
         Text(if(selected==q.first.third)"Верно! Отличная работа." else "Не совсем. Вот что важно запомнить:",color=if(selected==q.first.third)Mint else Color(0xFFB34F56),fontWeight=FontWeight.Bold)
         Text(q.second)
         Button(onClick={qi++;selected=-1;answered=false},modifier=Modifier.fillMaxWidth()){Text("Дальше")}
        }
       }
      }
     }
     "Препараты" -> if(drug!=null) {
      val d=drug!!
      Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(9.dp)) {
       TextButton(onClick={drug=null}){Text("← Каталог")}
       Heading(d.name,d.inn)
       Info("Описание",d.description);Info("Группа",d.group);Info("Показания",d.indications);Info("Противопоказания",d.contraindications);Info("Побочные эффекты",d.adverseEffects);Info("Фармакодинамика",d.pharmacodynamics);Info("Фармакокинетика",d.pharmacokinetics);Info("Формы выпуска",d.forms);Info("Источник",d.source+"\n"+d.sourceUrl)
       Text("Учебная информация. Всегда сверяйся с актуальной инструкцией конкретного препарата.",color=Color.Gray,fontSize=12.sp)
      }
     } else Column(Modifier.fillMaxSize().padding(16.dp)) {
      Heading("Препараты","Изучай действующие вещества и особенности")
      OutlinedTextField(search,{search=it},modifier=Modifier.fillMaxWidth(),placeholder={Text("Поиск препарата")},singleLine=true)
      LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)){items(drugCatalog.filter{(it.name+it.inn).contains(search,true)}){d->Tile("✚",d.name,d.group,Color.White){drug=d}}}
     }
     else -> Column(Modifier.fillMaxSize().padding(16.dp)) {
      Heading("Предметы","Выбирай тему и изучай небольшими порциями")
      LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)){items((0 until topics.length()).toList()){i->val t=topics.optJSONObject(i);Tile("▣",t?.optString("title","Тема "+(i+1))?:"Тема "+(i+1),"Учебный материал",Color.White){tab="Карточки"}}}
     }
    }
   }
  }
 }
}
@Composable private fun Heading(a:String,b:String){Column(verticalArrangement=Arrangement.spacedBy(4.dp)){Text(a,fontSize=25.sp,fontWeight=FontWeight.Black,color=Dark);Text(b,color=Color.Gray,fontSize=13.sp)}}
@Composable private fun Tile(icon:String,title:String,sub:String,color:Color,onClick:()->Unit){Row(Modifier.fillMaxWidth().background(color,RoundedCornerShape(17.dp)).clickable{onClick()}.padding(15.dp),verticalAlignment=Alignment.CenterVertically){Text(icon,color=Purple,fontSize=25.sp,modifier=Modifier.width(42.dp));Column(Modifier.weight(1f)){Text(title,color=Dark,fontWeight=FontWeight.Bold,fontSize=17.sp);Text(sub,color=Color.Gray,fontSize=12.sp)};Text("›",color=Purple,fontSize=24.sp)}}
@Composable private fun Info(title:String,body:String){Column(Modifier.fillMaxWidth().background(Color.White,RoundedCornerShape(14.dp)).padding(14.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){Text(title,color=Purple,fontWeight=FontWeight.Bold);Text(body,color=Dark,fontSize=14.sp,lineHeight=20.sp)}}

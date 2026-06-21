package br.com.curso.quizgamer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuizGamerTheme {
                QuizManager()
            }
        }
    }
}

/**
 * Tema customizado escuro com tons roxos e cianos neon
 */
@Composable
fun QuizGamerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFBB86FC),
            secondary = Color(0xFF03DAC6),
            background = Color(0xFF121212)
        ),
        content = content
    )
}

/**
 * Gerenciador de Estados e Transições de Tela
 */
@Composable
fun QuizManager() {
    var screenState by remember { mutableStateOf("start") }
    var score by remember { mutableStateOf(0) }

    // Fundo Premium degradê azul marinho
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
    )

    Box(modifier = Modifier.fillMaxSize().background(backgroundGradient)) {
        AnimatedContent(
            targetState = screenState,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "ScreenTransition"
        ) { targetScreen ->
            when (targetScreen) {
                "start" -> StartScreen { screenState = "quiz" }
                "quiz" -> QuizScreen(
                    onFinished = { finalScore ->
                        score = finalScore
                        screenState = "result"
                    }
                )
                "result" -> ResultScreen(score) {
                    score = 0
                    screenState = "start"
                }
            }
        }
    }
}

/**
 * Tela de Início
 */
@Composable
fun StartScreen(onStart: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "QUIZ GAMER",
            fontSize = 42.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 4.sp
        )
        Text(
            text = "LEVEL: HARDCORE",
            fontSize = 14.sp,
            color = Color(0xFF03DAC6),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBB86FC))
        ) {
            Text("PRESS START", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
        }
    }
}

/**
 * Tela do Jogo (Quiz)
 */
@Composable
fun QuizScreen(onFinished: (Int) -> Unit) {
    // 1. Database de Perguntas e Respostas
    val questions = listOf(
        "Quem é o protagonista da série 'The Legend of Zelda'?" to listOf("Zelda", "Link", "Ganon", "Epona"),
        "Qual console foi apelidado de 'Project Reality'?" to listOf("PS1", "Nintendo 64", "Sega Saturn", "Dreamcast"),
        "Em qual ano o primeiro PlayStation foi lançado no Japão?" to listOf("1992", "1993", "1994", "1995"),
        "Qual o nome do criador do Mario e Donkey Kong?" to listOf("Hideo Kojima", "Shigeru Miyamoto", "Masahiro Sakurai", "Todd Howard"),
        "Qual o jogo mais vendido de todos os tempos?" to listOf("Minecraft", "GTA V", "Tetris", "Wii Sports")
    )
    val correctAnswers = listOf(1, 1, 2, 1, 0) // Índices das respostas corretas

    // 2. Estados reativos locais
    var currentQuestionIdx by remember { mutableStateOf(0) }
    var scoreLocal by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var isCorrect by remember { mutableStateOf<Boolean?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Barra de Progresso Neon
        LinearProgressIndicator(
            progress = { (currentQuestionIdx + 1).toFloat() / questions.size },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = Color(0xFF03DAC6),
            trackColor = Color.White.copy(alpha = 0.1f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "PERGUNTA ${currentQuestionIdx + 1}",
            color = Color(0xFFBB86FC),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Card da Pergunta
        Card(
            modifier = Modifier.fillMaxWidth().height(120.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = questions[currentQuestionIdx].first,
                    fontSize = 20.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Renderização Dinâmica das Alternativas
        questions[currentQuestionIdx].second.forEachIndexed { index, answer ->
            val buttonColor = when {
                selectedAnswer == index && isCorrect == true -> Color(0xFF4CAF50) // Verde se acertou
                selectedAnswer == index && isCorrect == false -> Color(0xFFF44336) // Vermelho se errou
                else -> Color.White.copy(alpha = 0.1f)
            }

            Button(
                onClick = {
                    if (selectedAnswer == null) {
                        selectedAnswer = index
                        isCorrect = index == correctAnswers[currentQuestionIdx]
                        if (isCorrect!!) scoreLocal += 20
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                enabled = selectedAnswer == null // Trava cliques após a primeira escolha
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = answer,
                        color = if (selectedAnswer == index) Color.White else Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f)
                    )
                    if (selectedAnswer == index) {
                        Icon(
                            imageVector = if (isCorrect!!) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Botão para avançar
        if (selectedAnswer != null) {
            Button(
                onClick = {
                    if (currentQuestionIdx < questions.size - 1) {
                        currentQuestionIdx++
                        selectedAnswer = null
                        isCorrect = null
                    } else {
                        onFinished(scoreLocal)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03DAC6))
            ) {
                Text("PRÓXIMA", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Tela de Resultados Reativa
 */
@Composable
fun ResultScreen(score: Int, onRestart: () -> Unit) {
    val isWinner = score >= 60

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Exibe troféu ou gameover dinamicamente
        Image(
            painter = painterResource(id = if (isWinner) R.drawable.trofeu else R.drawable.gameover),
            contentDescription = null,
            modifier = Modifier.size(180.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = if (isWinner) "VITÓRIA!" else "GAME OVER",
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
            color = if (isWinner) Color(0xFFFFA000) else Color(0xFFD32F2F)
        )

        Text(
            text = "Sua pontuação final: $score XP",
            fontSize = 18.sp,
            color = Color.White.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onRestart,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("TENTAR NOVAMENTE", fontWeight = FontWeight.Bold)
        }
    }
}

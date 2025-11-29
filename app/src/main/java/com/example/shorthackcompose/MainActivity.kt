package com.example.shorthackcompose


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.shorthackcompose.ui.theme.BGGray
import com.example.shorthackcompose.ui.theme.X5TechGreen
import com.example.yourapp.BarcodeAnalyzer
import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.Executors
import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp


// ==================== API MODELS ====================

data class SignUpRequest(
    val name: String,
    val surname: String,
    val email: String,
    val password: String,
    val image_url: String = "",
    val tg_username: String = ""
)

data class SignUpResponse(
    val id: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val id: String
)

data class EventResponse(
    val id: String?,
    val title: String,
    val date: String,
    val location: String,
    val description: String
)

// ==================== API SERVICE ====================

interface ApiService {
    @POST("clients/sign-up")
    suspend fun signUp(@Body request: SignUpRequest): SignUpResponse

    @POST("clients/sign-in")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("events/{event_id}")
    suspend fun getEvent(@Path("event_id") eventId: String): EventResponse
}

object RetrofitClient {
    private const val BASE_URL = "http://93.185.159.71/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()
            .create(ApiService::class.java)
    }
}

// ==================== TOKEN MANAGER ====================

object TokenManager {
    private var _token: String = ""

    fun saveToken(newToken: String) {
        _token = newToken
    }

    fun getToken(): String = _token

    fun clearToken() {
        _token = ""
    }
}

// ==================== MAIN ACTIVITY ====================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()

                val showBottomBar =
                    navController.currentBackStackEntryAsState().value?.destination?.route in
                            listOf("scanner", "shop", "profile")

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            BottomBar(navController)
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("login") { LoginScreen(navController) }
                        composable("register") { RegisterScreen(navController) }
                        composable("scanner") { ScannerScreen(navController) }
                        composable("shop") { ShopScreen() }
                        composable("profile") { ProfileScreen() }
                        composable(
                            route = "event/{eventId}",
                            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                            EventDetailScreen(navController, eventId)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("test@test.com") }
    var password by remember { mutableStateOf("test") }
    var rememberMe by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showAdminPanel by remember { mutableStateOf(false) }

    if (showAdminPanel) {
        AdminPanelScreen(onBack = { showAdminPanel = false })
    } else {
        LaunchedEffect(isLoading) {
            if (isLoading && email.isNotEmpty() && password.isNotEmpty()) {
                try {
                    val response = RetrofitClient.apiService.login(
                        LoginRequest(email, password)
                    )
                    TokenManager.saveToken(response.id)
                    navController.navigate("profile") {
                        popUpTo("login") { inclusive = true }
                    }
                } catch (e: Exception) {
                    errorMessage = "Ошибка: ${e.message}"
                    isLoading = false
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {

            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .height(400.dp)
                        .fillMaxWidth()
                        .background(color = X5TechGreen)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = BGGray)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.x5logo),
                    contentDescription = "X5 Logo",
                    modifier = Modifier.size(72.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Создай свой аккаунт",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Введи свой email и пароль",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !isLoading
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Пароль") },
                            visualTransformation = if (passwordVisible)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !isLoading,
                            trailingIcon = {
                                val icon =
                                    if (passwordVisible) Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(icon, contentDescription = null)
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (errorMessage.isNotEmpty()) {
                            Text(
                                text = errorMessage,
                                color = Color.Red,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Checkbox(
                                    checked = rememberMe,
                                    onCheckedChange = { rememberMe = it },
                                    enabled = !isLoading
                                )
                                Text(
                                    text = "Запомнить меня",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Text(
                                text = "Забыли пароль?",
                                color = Color(0xFF2F80ED),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.clickable { /* TODO */ }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (email.isNotEmpty() && password.isNotEmpty()) {
                                    errorMessage = ""
                                    isLoading = true
                                } else {
                                    errorMessage = "Заполните все поля"
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading
                        ) {
                            Text(if (isLoading) "Загрузка..." else "Войти")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Нет аккаунта? ",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Зарегистрироваться",
                                color = Color(0xFF2F80ED),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.clickable {
                                    navController.navigate("register")
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Divider(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.LightGray
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(
                            onClick = { showAdminPanel = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "🔐 Админ панель",
                                color = Color(0xFF9C27B0),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun AdminPanelScreen(onBack: () -> Unit) {
    var currentTab by remember { mutableStateOf("menu") }

    when (currentTab) {
        "menu" -> AdminMenuScreen(onBack = onBack) { tab ->
            currentTab = tab
        }
        "stands" -> AdminStandsScreen(onBack = { currentTab = "menu" })
        "stats" -> AdminStatsScreen(onBack = { currentTab = "menu" })
    }
}

@Composable
fun AdminMenuScreen(onBack: () -> Unit, onTabSelect: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🔐 Админ Панель",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color(0xFF9C27B0)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onTabSelect("stands") },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = X5TechGreen
            )
        ) {
            Text(
                "🏢 Добавить стенд",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onTabSelect("stats") },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF9800)
            )
        ) {
            Text(
                "📊 Статистика",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onBack() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.LightGray
            )
        ) {
            Text("Назад", color = Color.Black)
        }
    }
}

@Composable
fun AdminStandsScreen(onBack: () -> Unit) {
    var standName by remember { mutableStateOf("") }
    var standLocation by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    if (submitted) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "✅ Стенд добавлен!",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onBack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Вернуться")
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "🏢 Добавить стенд",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = standName,
                onValueChange = { standName = it },
                label = { Text("Название стенда") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = standLocation,
                onValueChange = { standLocation = it },
                label = { Text("Расположение") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { submitted = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = standName.isNotEmpty() && standLocation.isNotEmpty()
            ) {
                Text("Добавить")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onBack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.LightGray
                )
            ) {
                Text("Отмена", color = Color.Black)
            }
        }
    }
}

@Composable
fun AdminStatsScreen(onBack: () -> Unit) {
    val stats = listOf(
        StatItem("Посещения", R.drawable.stat1),
        StatItem("Конверсия", R.drawable.stat2),
        StatItem("Активность", R.drawable.stat3),
        StatItem("Рейтинг", R.drawable.stat4)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(color = X5TechGreen)
                .padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "📊 Статистика",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(stats.size) { index ->
                StatCard(stat = stats[index])
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onBack() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.LightGray
                    )
                ) {
                    Text("Назад", color = Color.Black)
                }
            }
        }
    }
}

data class StatItem(
    val title: String,
    val imageId: Int
)

@Composable
fun StatCard(stat: StatItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Фото
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = stat.imageId),
                    contentDescription = stat.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = stat.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
fun RegisterScreen(navController: NavHostController) {
    var name by remember { mutableStateOf("Test") }
    var surname by remember { mutableStateOf("User") }
    var email by remember { mutableStateOf("test@test.com") }
    var password by remember { mutableStateOf("test") }
    var imageUrl by remember { mutableStateOf("") }
    var tgUsername by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    LaunchedEffect(isLoading) {
        if (isLoading && email.isNotEmpty() && password.isNotEmpty()) {
            try {
                val response = RetrofitClient.apiService.signUp(
                    SignUpRequest(
                        name = name,
                        surname = surname,
                        email = email,
                        password = password,
                        image_url = imageUrl,
                        tg_username = tgUsername
                    )
                )
                successMessage = "Успешно зарегистрирован! ID: ${response.id}"
                isLoading = false
                // После регистрации можно перейти на логин
                navController.navigate("login")
            } catch (e: Exception) {
                errorMessage = "Ошибка регистрации: ${e.message}"
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Регистрация",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Имя") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = surname,
            onValueChange = { surname = it },
            label = { Text("Фамилия") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = tgUsername,
            onValueChange = { tgUsername = it },
            label = { Text("Telegram (опционально)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = Color.Red, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (successMessage.isNotEmpty()) {
            Text(successMessage, color = Color.Green, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()) {
                    errorMessage = ""
                    successMessage = ""
                    isLoading = true
                } else {
                    errorMessage = "Заполните обязательные поля"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Регистрация..." else "Зарегистрироваться")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = { navController.navigate("login") }) {
            Text("Уже есть аккаунт? Войти")
        }
    }
}


@Composable
fun ScannerScreen(navController: NavHostController) {
    var scannedCode by remember { mutableStateOf("") }
    var cameraPermissionGranted by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        cameraPermissionGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    if (!cameraPermissionGranted) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Нужно разрешение на доступ к камере")
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Пожалуйста, разрешите доступ к камере в настройках приложения",
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            CameraPreview(
                onQrCodeScanned = { code ->
                    handleQrScan(code, navController)
                }
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
            ) {
                Text(
                    text = "Наведите камеру на QR код",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreview(onQrCodeScanned: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var codeProcessed by remember { mutableStateOf(false) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = androidx.camera.core.Preview.Builder()
                        .build()
                    preview.setSurfaceProvider(previewView.surfaceProvider)

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                    imageAnalysis.setAnalyzer(
                        Executors.newSingleThreadExecutor(),
                        BarcodeAnalyzer { code ->
                            if (!codeProcessed) {
                                codeProcessed = true
                                onQrCodeScanned(code)
                            }
                        }
                    )

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

fun handleQrScan(rawValue: String, navController: NavHostController) {
    val eventId = when {
        rawValue.startsWith("EVENT") -> rawValue.trim()
        else -> null
    }

    if (eventId != null) {
        navController.navigate("event/$eventId") {
            popUpTo("scanner") { inclusive = false }
        }
    }
}

data class Quiz(
    val question: String,
    val options: List<String>,
    val correctAnswer: Int
)

@Composable
fun EventDetailScreen(navController: NavHostController, eventId: String) {
    var event by remember { mutableStateOf<EventResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showQuiz by remember { mutableStateOf(false) }
    var showFeedback by remember { mutableStateOf(false) }
    var showInternship by remember { mutableStateOf(false) }
    var showMentorChat by remember { mutableStateOf(false) }
    var quizScore by remember { mutableStateOf(0) }

    LaunchedEffect(eventId) {
        try {
            event = RetrofitClient.apiService.getEvent(eventId)
        } catch (e: Exception) {
            event = when (eventId) {
                "EVENT001" -> EventResponse(
                    "1",
                    "Конференция X5",
                    "29 ноября 2025",
                    "Москва, ул. Пушкина",
                    "Главная конференция компании X5. Обсуждение новых инициатив и стратегии развития."
                )
                "EVENT002" -> EventResponse(
                    "2",
                    "Спортивный день",
                    "30 ноября 2025",
                    "Спортивный комплекс X5",
                    "Корпоративное спортивное мероприятие. Футбол, волейбол, настольный теннис."
                )
                "EVENT003" -> EventResponse(
                    "3",
                    "Новогодний праздник",
                    "31 декабря 2025",
                    "Центральный офис X5",
                    "Корпоративный новогодний праздник с развлечениями и подарками."
                )
                else -> EventResponse(
                    null,
                    "Событие не найдено",
                    "N/A",
                    "N/A",
                    "К сожалению, событие с кодом $eventId не найдено."
                )
            }
        } finally {
            isLoading = false
        }
    }

    if (showQuiz) {
        QuizScreen(eventId = eventId, onBack = { showQuiz = false }) { score ->
            quizScore = score
            showQuiz = false
        }
    } else if (showFeedback) {
        FeedbackScreen(onBack = { showFeedback = false })
    } else if (showInternship) {
        InternshipScreen(onBack = { showInternship = false })
    } else if (showMentorChat) {
        MentorChatScreen(onBack = { showMentorChat = false })
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.popBackStack() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Назад", style = MaterialTheme.typography.bodyLarge)
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Загрузка...")
                }
            } else if (event != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(
                            id = when (event!!.id) {
                                "1" -> R.drawable.event1
                                "2" -> R.drawable.event2
                                "3" -> R.drawable.event3
                                else -> R.drawable.event1
                            }
                        ),
                        contentDescription = event!!.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = event!!.title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "📅 ${event!!.date}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "📍 ${event!!.location}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Описание:",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = event!!.description,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { showQuiz = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = X5TechGreen
                        )
                    ) {
                        Text("📝 Пройти квиз")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showFeedback = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF9800)
                        )
                    ) {
                        Text("⭐ Оставить фидбек")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showMentorChat = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        )
                    ) {
                        Text("💬 Чат с ментором")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showInternship = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF9C27B0)
                        )
                    ) {
                        Text("💼 Стажировка")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { navController.navigate("scanner") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.LightGray
                        )
                    ) {
                        Text("Сканировать другой код", color = Color.Black)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun MentorChatScreen(onBack: () -> Unit) {
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var messageText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = X5TechGreen)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Card(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.avatarmentor),
                                contentDescription = "Mentor Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Дмитрий Нагиев",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "Ментор X5",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                IconButton(onClick = { onBack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color.White
                    )
                }
            }
        }

        // Сообщения
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages.size) { index ->
                ChatBubble(message = messages[index])
            }
        }

        // Поле ввода
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Напишите сообщение...") },
                modifier = Modifier
                    .weight(1f)
                    .height(70.dp),
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = X5TechGreen
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (messageText.isNotEmpty()) {
                        messages = messages + ChatMessage(
                            text = messageText,
                            isUser = true,
                            timestamp = "сейчас"
                        )
                        messageText = ""
                    }
                },
                modifier = Modifier
                    .size(70.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = X5TechGreen
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("📤", style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: String
)

@Composable
fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 280.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) X5TechGreen else Color(0xFFF0F0F0)
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.isUser) Color.White else Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = message.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (message.isUser) Color.White.copy(alpha = 0.7f) else Color.Gray
                )
            }
        }
    }
}
@Composable
fun QuizScreen(eventId: String, onBack: () -> Unit, onComplete: (Int) -> Unit) {
    val quizzes = mapOf(
        "EVENT001" to listOf(
            Quiz(
                "Какой год основания компании X5?",
                listOf("2000", "2010", "2020", "2015"),
                0
            ),
            Quiz(
                "Сколько стран присутствует X5?",
                listOf("2", "3", "5", "4"),
                2
            )
        ),
        "EVENT002" to listOf(
            Quiz(
                "Какой вид спорта НЕ будет на мероприятии?",
                listOf("Баскетбол", "Футбол", "Волейбол", "Теннис"),
                3
            ),
            Quiz(
                "В какое время начинается спортивный день?",
                listOf("9:00", "10:00", "14:00", "15:00"),
                1
            )
        ),
        "EVENT003" to listOf(
            Quiz(
                "Что будет на новогоднем празднике?",
                listOf("Конференция", "Развлечения и подарки", "Спорт", "Тренинг"),
                1
            ),
            Quiz(
                "Дата праздника?",
                listOf("25 декабря", "1 января", "31 декабря", "30 декабря"),
                2
            )
        )
    )

    val questions = quizzes[eventId] ?: emptyList()
    var currentQuestion by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var answered by remember { mutableStateOf(false) }

    if (currentQuestion >= questions.size) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Тест завершён!",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Ваш результат:",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "1 / 2",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = X5TechGreen
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onComplete(score) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Вернуться")
            }
        }
    } else {
        val question = questions[currentQuestion]

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ) {
            LinearProgressIndicator(
                progress = { (currentQuestion + 1) / questions.size.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = X5TechGreen
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Вопрос
            Text(
                text = "Вопрос ${currentQuestion + 1} / ${questions.size}",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = question.question,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            question.options.forEachIndexed { index, option ->
                Button(
                    onClick = {
                        selectedAnswer = index
                        answered = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when {
                            !answered -> Color.LightGray
                            index == selectedAnswer && index == question.correctAnswer -> Color.Green
                            index == selectedAnswer -> Color.Red
                            index == question.correctAnswer -> Color.Green
                            else -> Color.LightGray
                        }
                    ),
                    enabled = !answered
                ) {
                    Text(
                        text = option,
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (answered) {
                if (selectedAnswer == question.correctAnswer) {
                    score++
                }

                Button(
                    onClick = { currentQuestion++; selectedAnswer = null; answered = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Далее")
                }
            }
        }
    }
}

@Composable
fun FeedbackScreen(onBack: () -> Unit) {
    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    if (submitted) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Спасибо за фидбек! ⭐",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onBack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Вернуться")
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Оцените событие",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(5) { index ->
                    Text(
                        text = if (index < rating) "⭐" else "☆",
                        style = MaterialTheme.typography.displayMedium,
                        modifier = Modifier
                            .clickable { rating = index + 1 }
                            .padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Ваш комментарий") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 4,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { submitted = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = rating > 0
            ) {
                Text("Отправить")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onBack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.LightGray
                )
            ) {
                Text("Отмена", color = Color.Black)
            }
        }
    }
}

@Composable
fun InternshipScreen(onBack: () -> Unit) {
    var submitted by remember { mutableStateOf(false) }

    if (submitted) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Спасибо за интерес! 💼",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Мы скоро свяжемся с вами",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onBack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Вернуться")
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Заинтересованы в стажировке?",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Оставьте заявку и мы обсудим детали",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { submitted = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9C27B0)
                )
            ) {
                Text("Подать заявку")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onBack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.LightGray
                )
            ) {
                Text("Отмена", color = Color.Black)
            }
        }
    }
}

@Composable
fun ShopScreen() {
    var userCoins by remember { mutableStateOf(100) }

    val items = listOf(
        ShopItem(
            id = 1,
            name = "Футболка",
            price = 120,
            imageId = R.drawable.item1
        ),
        ShopItem(
            id = 2,
            name = "Футболка",
            price = 120,
            imageId = R.drawable.item2
        ),
        ShopItem(
            id = 3,
            name = "Футболка",
            price = 120,
            imageId = R.drawable.item3
        ),
        ShopItem(
            id = 4,
            name = "Свитшот",
            price = 200,
            imageId = R.drawable.item4
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(color = X5TechGreen)
                .padding(16.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = Modifier.offset(y = 70.dp),
                    text = "МАГАЗИН",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.Black
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .background(
                            color = Color.White.copy(alpha = 0.95f),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🪙",
                        style = MaterialTheme.typography.headlineSmall,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = userCoins.toString(),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = X5TechGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(items.size) { index ->
                ShopItemCard(
                    item = items[index],
                    userCoins = userCoins,
                    onBuy = { coin ->
                        if (userCoins >= coin) {
                            userCoins -= coin
                        }
                    }
                )
            }
        }
    }
}

data class ShopItem(
    val id: Int,
    val name: String,
    val price: Int,
    val imageId: Int
)

@Composable
fun ShopItemCard(
    item: ShopItem,
    userCoins: Int,
    onBuy: (Int) -> Unit
) {
    val canAfford = userCoins >= item.price

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = item.imageId),
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = "🪙",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.price.toString(),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (canAfford) X5TechGreen else Color.Red
                        )
                    }

                    Button(
                        onClick = { onBuy(item.price) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        enabled = canAfford,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canAfford) X5TechGreen else Color.LightGray,
                            disabledContainerColor = Color.LightGray
                        )
                    ) {
                        Text(
                            text = "Купить",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = if (canAfford) Color.White else Color.Gray
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ProfileScreen() {
    var userName by remember { mutableStateOf("ИВАНОВ КИРИЛЛ ВЛАДИМИРОВИЧ") }
    var userEmail by remember { mutableStateOf("test@test.com") }
    var education by remember { mutableStateOf("НИТУ «МИСиС\"") }
    var age by remember { mutableStateOf("20 лет") }
    var bio by remember { mutableStateOf("Опытный разработчик, удобные решения для проектов") }
    var competence by remember { mutableStateOf("Компетенции") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(color = X5TechGreen)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .size(120.dp)
                    .offset(y = (-60).dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.avatar),
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Имя
            Text(
                text = userName,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Компетенции тег
            Surface(
                modifier = Modifier.padding(top = 8.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFFF9800) // Orange color
            ) {
                Text(
                    text = competence,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Карточка с информацией
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ОБРАЗОВАНИЕ",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = education,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.LightGray, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ВОЗРАСТ",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = age,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.LightGray, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "О СЕБЕ",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = bio,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 1.4.em
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { /* TODO: реализовать выход */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE0E0E0)
                )
            ) {
                Text(
                    "Выход",
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}


data class BottomItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun BottomBar(navController: NavHostController) {
    val items = listOf(
        BottomItem("scanner", "Сканер", Icons.Filled.QrCodeScanner),
        BottomItem("profile", "Профиль", Icons.Filled.Person),
        BottomItem("shop", "Мерч", Icons.Filled.ShoppingCart)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color(0xFF4CAF50).copy(alpha = 0.12f),
        contentColor = Color(0xFF4CAF50)
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
            )
        }
    }
}



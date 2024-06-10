package android.assignment.acharyaprashant

import android.assignment.acharyaprashant.network.ImageLoader
import android.assignment.acharyaprashant.repository.MediaCoverageRepository
import android.assignment.acharyaprashant.ui.theme.AcharyaPrashantAndroidAssignmentTheme
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference

class MainActivity : ComponentActivity() {

    private val repository = MediaCoverageRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AcharyaPrashantAndroidAssignmentTheme {
                MyApp()
            }
        }
    }

    @Composable
    fun MyApp() {
        val images = remember { mutableStateListOf<String>() }
        val scope = rememberCoroutineScope()
        val errorState = remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            scope.launch {
                val fetchedImages = repository.fetchImages()
                if (fetchedImages.isEmpty()) {
                    errorState.value = true
                } else {
                    images.addAll(fetchedImages)
                    errorState.value = false
                }
            }
        }

        if (errorState.value) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Failed to load images",
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            ImageGrid(images = images)
        }
    }

    @Composable
    fun ImageGrid(images: List<String>) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(4.dp)
        ) {
            images.forEachIndexed { index, imageUrl ->
                item(key = index) {
                    ImageCard(imageUrl)
                }
            }
        }
    }

    @Composable
    fun ImageCard(imageUrl: String) {
        Box(
            modifier = Modifier
                .padding(4.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            LoadImage(url = imageUrl)
        }
    }


    @Composable
    fun LoadImage(url: String) {
        val bitmapState = remember { mutableStateOf<Bitmap?>(null) }
        val errorState = remember { mutableStateOf(false) }

        DisposableEffect(url) {
            val loadJob = ImageLoader.loadImage(url,
                onSuccess = { bitmap ->
                    bitmapState.value = bitmap
                    errorState.value = false
                },
                onError = {
                    errorState.value = true
                }
            )

            onDispose {
                loadJob.cancel()
            }
        }

        if (errorState.value) {
            Image(
                painter = painterResource(id = R.drawable.ic_error_placeholder_foreground), // Add an error placeholder drawable in your resources
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            bitmapState.value?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } ?: Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

# 4.7 Scanner y OCR

## Objetivo de esta fase

Entender como StikerVault usa la camara para reconocer texto, extraer un codigo OCR, buscar un producto y permitir acciones sobre inventario segun permisos.

En una defensa, esta seccion ayuda a explicar:

- Como se pide permiso de camara.
- Como se abre y cierra la camara.
- Como CameraX entrega imagenes a ML Kit.
- Como se extrae un codigo desde texto reconocido.
- Como el ViewModel decide si el producto existe o no.
- Como se permite agregar producto o sumar stock desde Scanner.

## Archivos principales

```text
app/src/main/java/com/example/myapplication/presentation/scanner/ScannerScreen.kt
app/src/main/java/com/example/myapplication/presentation/scanner/ScannerViewModel.kt
app/src/main/java/com/example/myapplication/presentation/scanner/ScannerCameraController.kt
app/src/main/java/com/example/myapplication/presentation/scanner/OcrCodeExtractor.kt
```

Archivo relacionado:

```text
app/src/main/java/com/example/myapplication/presentation/scanner/ScannerUiState.kt
```

Flujo general:

```text
ScannerScreen
-> permiso de camara
-> CameraPreview
-> CameraX ImageAnalysis
-> ML Kit TextRecognition
-> OcrCodeExtractor
-> ScannerViewModel.onCaptureResult
-> GetProductByOcrIdentifierUseCase
-> ScannerUiState
-> UI muestra producto encontrado, no encontrado o error
```

## 1. ScannerScreen.kt

`ScannerScreen` es la pantalla visual del scanner OCR.

Firma:

```kotlin
@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel,
    canEditInventory: Boolean = true,
    onAddProduct: (String) -> Unit = {},
    onAddExistingProduct: (Product) -> Unit = {}
)
```

Recibe:

- `viewModel`: controla estados del scanner y busqueda de producto.
- `canEditInventory`: indica si el usuario puede modificar inventario.
- `onAddProduct`: callback para agregar un producto nuevo usando el codigo detectado.
- `onAddExistingProduct`: callback para sumar stock a un producto existente.

Idea clave:

```text
ScannerScreen muestra la camara y resultados, pero no decide directamente como modificar inventario. Eso lo delega mediante callbacks.
```

## 2. Estado observado

```kotlin
val uiState by viewModel.uiState.collectAsState()
```

La pantalla observa `ScannerUiState`.

Estados principales:

- `Idle`: camara cerrada.
- `CameraReady`: camara abierta y lista.
- `ProcessingCapture`: captura solicitada y OCR en proceso.
- `Searching`: buscando producto por codigo.
- `ProductFound`: producto encontrado.
- `ProductNotFound`: codigo detectado, producto no encontrado.
- `CodeNotRecognized`: no se pudo extraer codigo.
- `Error`: error inesperado.

Respuesta teorica posible:

```text
ScannerScreen reacciona a ScannerUiState para decidir si muestra camara, loading, resultado o mensajes de error.
```

## 3. Permiso de camara

La pantalla revisa si tiene permiso:

```kotlin
var hasCameraPermission by remember {
    mutableStateOf(
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
    )
}
```

Si no lo tiene, lo solicita:

```kotlin
val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
) { granted -> hasCameraPermission = granted }
```

```kotlin
LaunchedEffect(Unit) {
    if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
}
```

Si no hay permiso, muestra:

```text
Permiso de camara requerido
```

Respuesta teorica posible:

```text
El permiso de camara se solicita desde la pantalla usando rememberLauncherForActivityResult. Si el usuario lo concede, se activa el flujo de camara.
```

## 4. Activacion de camara

La camara esta activa si el estado es:

```kotlin
val isCameraActive = uiState is ScannerUiState.CameraReady ||
    uiState is ScannerUiState.ProcessingCapture
```

Si hay permiso y la camara esta activa:

```kotlin
CameraPreview(
    modifier = Modifier.fillMaxSize(),
    controller = cameraController,
    onCaptureResult = { raw, code ->
        viewModel.onCaptureResult(raw, code)
    }
)
```

Si no:

```text
Presiona 'Abrir camara' para escanear
```

Idea clave:

```text
CameraPreview solo aparece cuando el usuario abrio la camara y hay permiso.
```

## 5. ScannerCameraController

Se crea una vez con `remember`:

```kotlin
val cameraController = remember { ScannerCameraController() }
```

Este controlador permite pedir una captura exacta.

Cuando el usuario presiona el boton:

```kotlin
viewModel.onCaptureRequested()
cameraController.requestCapture()
```

Flujo:

```text
Boton Capturar codigo
-> ViewModel cambia a ProcessingCapture
-> CameraController marca captureRequested = true
-> ImageAnalysis consume la siguiente imagen disponible
```

## 6. Overlay visual de OCR

Cuando la camara esta activa, se muestra:

```kotlin
Box(
    modifier = Modifier
        .align(Alignment.End)
        .size(width = 140.dp, height = 72.dp)
        .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
)
```

Este recuadro indica donde alinear el codigo.

Cambios de examen probables:

- Cambiar tamano del recuadro.
- Cambiar color del borde.
- Cambiar grosor del borde.
- Cambiar posicion del recuadro.
- Cambiar texto de instruccion.

## 7. Botones principales

### Abrir camara

Cuando la camara no esta activa:

```kotlin
Button(
    onClick = { viewModel.openCamera() },
    enabled = hasCameraPermission
) {
    Text("Abrir camara")
}
```

### Capturar codigo

Cuando la camara esta activa:

```kotlin
Button(
    onClick = {
        viewModel.onCaptureRequested()
        cameraController.requestCapture()
    },
    enabled = uiState !is ScannerUiState.ProcessingCapture
) {
    Text("Capturar codigo")
}
```

### Cerrar camara

```kotlin
OutlinedButton(onClick = { viewModel.closeCamera() }) {
    Text("Cerrar camara")
}
```

Cambios de examen probables:

- Cambiar textos de botones.
- Deshabilitar abrir camara por rol.
- Cambiar estilo de botones.
- Ocultar boton cerrar camara.

## 8. Estados visuales de resultado

### ProcessingCapture

Muestra loading:

```text
Leyendo codigo...
```

### Searching

Muestra:

```text
Buscando: codigo
```

### ProductFound

Muestra una `ResultCard` con:

- Icono check.
- Nombre del producto.
- Codigo.
- Stock actual.

Si `canEditInventory` es true, muestra:

```text
Anadir 1 al inventario
```

Si no, muestra:

```text
Tu rol de auditor permite consultar el producto, pero no modificar el inventario.
```

### ProductNotFound

Muestra una `ResultCard` con:

- Producto no encontrado.
- Codigo detectado.

Si `canEditInventory` es true, muestra:

```text
Agregar producto
```

Si no, muestra:

```text
Tu rol de auditor no permite registrar productos.
```

### CodeNotRecognized

Muestra:

```text
No se detecto el codigo
Intenta de nuevo con mejor luz y el codigo dentro del recuadro
```

### Error

Muestra el mensaje del error.

## 9. ResultCard

`ResultCard` es una card reutilizable para mostrar resultados del scanner.

Firma:

```kotlin
private fun ResultCard(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onDismiss: () -> Unit
)
```

Muestra:

- Icono.
- Titulo.
- Subtitulo.
- Boton `OK`.

Cambios de examen probables:

- Cambiar texto del boton OK.
- Cambiar padding.
- Cambiar color de fondo.
- Cambiar estilo del titulo.

## 10. CameraPreview

`CameraPreview` integra CameraX y ML Kit dentro de Compose.

Firma:

```kotlin
private fun CameraPreview(
    modifier: Modifier = Modifier,
    controller: ScannerCameraController,
    onCaptureResult: (rawText: String, extractedCode: String?) -> Unit
)
```

Recibe:

- `controller`: para saber cuando capturar.
- `onCaptureResult`: callback que devuelve texto bruto y codigo extraido.

## 11. AndroidView y PreviewView

Compose usa:

```kotlin
AndroidView(
    modifier = modifier,
    factory = { ctx ->
        val previewView = PreviewView(ctx)
        ...
        previewView
    }
)
```

`AndroidView` permite insertar una vista tradicional de Android dentro de Compose.

`PreviewView` muestra la camara de CameraX.

Respuesta teorica posible:

```text
CameraX usa PreviewView, que es una vista tradicional. Por eso se integra en Compose mediante AndroidView.
```

## 12. Configuracion de CameraX

Se obtiene el proveedor de camara:

```kotlin
ProcessCameraProvider.getInstance(ctx)
```

Se crea preview:

```kotlin
val preview = Preview.Builder().build().apply {
    setSurfaceProvider(previewView.surfaceProvider)
}
```

Se crea analizador:

```kotlin
val analyzer = ImageAnalysis.Builder()
    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
    .build()
```

`STRATEGY_KEEP_ONLY_LATEST` evita procesar imagenes atrasadas y se queda con la mas reciente.

Luego se enlaza al ciclo de vida:

```kotlin
cameraProvider.bindToLifecycle(
    lifecycleOwner,
    CameraSelector.DEFAULT_BACK_CAMERA,
    preview,
    analyzer
)
```

Usa camara trasera:

```kotlin
CameraSelector.DEFAULT_BACK_CAMERA
```

## 13. Captura controlada

El analizador recibe muchas imagenes, pero solo procesa cuando se pidio captura:

```kotlin
if (!controller.consumeCaptureRequest()) {
    imageProxy.close()
    return@setAnalyzer
}
```

Esto evita hacer OCR todo el tiempo.

Respuesta teorica posible:

```text
El scanner no procesa cada frame continuamente. Solo analiza el siguiente frame cuando el usuario presiona Capturar codigo.
```

## 14. ML Kit Text Recognition

Se crea recognizer:

```kotlin
val recognizer = remember {
    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
}
```

Se convierte la imagen:

```kotlin
val image = InputImage.fromMediaImage(
    mediaImage,
    imageProxy.imageInfo.rotationDegrees
)
```

Se procesa:

```kotlin
recognizer.process(image)
```

Si funciona:

```kotlin
val raw = result.text.trim()
val code = OcrCodeExtractor.extractFromMlKit(...)
onCaptureResult(raw, code)
```

Si falla:

```kotlin
onCaptureResult("", null)
```

Siempre se cierra `imageProxy` al terminar.

Idea clave:

```text
ML Kit reconoce texto; OcrCodeExtractor decide que parte de ese texto parece codigo.
```

## 15. ScannerViewModel.kt

`ScannerViewModel` controla el estado del scanner y busca producto por OCR.

Constructor:

```kotlin
class ScannerViewModel(
    private val getProductByOcrIdentifier: GetProductByOcrIdentifierUseCase
) : ViewModel()
```

Recibe:

- `GetProductByOcrIdentifierUseCase`: busca producto por identificador OCR.

Estado:

```kotlin
private val _uiState = MutableStateFlow<ScannerUiState>(ScannerUiState.Idle)
val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()
```

Codigo previo:

```kotlin
private var lastScannedCode = ""
```

Se usa para evitar procesar dos veces el mismo codigo seguido.

## 16. openCamera

```kotlin
fun openCamera() {
    lastScannedCode = ""
    _uiState.value = ScannerUiState.CameraReady
}
```

Abre el flujo de camara.

Respuesta teorica posible:

```text
openCamera cambia el estado a CameraReady, lo que hace que ScannerScreen muestre CameraPreview.
```

## 17. onCaptureRequested

```kotlin
fun onCaptureRequested() {
    if (_uiState.value is ScannerUiState.CameraReady) {
        _uiState.value = ScannerUiState.ProcessingCapture
    }
}
```

Solo permite pasar a `ProcessingCapture` si la camara estaba lista.

Esto evita capturar en estados incorrectos.

## 18. onCaptureResult

```kotlin
fun onCaptureResult(rawText: String, extractedCode: String?)
```

Este metodo recibe:

- `rawText`: texto completo reconocido por ML Kit.
- `extractedCode`: codigo extraido por `OcrCodeExtractor`.

Si no hay codigo:

```kotlin
_uiState.value = ScannerUiState.CodeNotRecognized(rawText.take(120))
```

Si el codigo es igual al ultimo:

```kotlin
_uiState.value = ScannerUiState.CameraReady
return
```

Si hay codigo nuevo:

1. Guarda `lastScannedCode`.
2. Cambia estado a `Searching(code)`.
3. Busca producto por OCR.
4. Si existe, emite `ProductFound`.
5. Si no existe, emite `ProductNotFound`.
6. Si falla, emite `Error`.

Respuesta teorica posible:

```text
ScannerViewModel recibe el resultado OCR, valida si hay codigo, evita duplicados y consulta el caso de uso para saber si existe un producto asociado.
```

## 19. backToCamera, closeCamera y reset

```kotlin
fun backToCamera() {
    _uiState.value = ScannerUiState.CameraReady
}
```

Vuelve a capturar otro codigo.

```kotlin
fun closeCamera() {
    _uiState.value = ScannerUiState.Idle
}
```

Cierra la camara.

```kotlin
fun reset() {
    lastScannedCode = ""
    _uiState.value = ScannerUiState.Idle
}
```

Reinicia completamente el scanner.

## 20. ScannerCameraController.kt

Este archivo controla solicitudes de captura.

```kotlin
class ScannerCameraController {
    private val captureRequested = AtomicBoolean(false)

    fun requestCapture() {
        captureRequested.set(true)
    }

    fun consumeCaptureRequest(): Boolean = captureRequested.getAndSet(false)
}
```

`AtomicBoolean` permite manejar el flag de forma segura entre hilos.

Funcionamiento:

- `requestCapture()` marca que se quiere capturar.
- `consumeCaptureRequest()` devuelve si habia solicitud y la apaga inmediatamente.

Idea clave:

```text
consumeCaptureRequest hace que una solicitud se use una sola vez.
```

Respuesta teorica posible:

```text
ScannerCameraController coordina el boton de captura con el analizador de CameraX para procesar solo un frame cuando el usuario lo solicita.
```

## 21. OcrCodeExtractor.kt

`OcrCodeExtractor` decide que texto reconocido por ML Kit debe tratarse como codigo.

Es un `object`:

```kotlin
object OcrCodeExtractor
```

Esto significa que se usa como singleton, sin crear instancias.

## 22. Regex de codigo Panini

```kotlin
private val PANINI_CODE = Regex("""^[A-Z]{2,4}\s+\d{1,3}$""")
```

Formato esperado:

```text
2 a 4 letras mayusculas
espacio
1 a 3 numeros
```

Ejemplos validos:

```text
MEX 1
ARG 12
BRA 45
ECU 100
```

Cambios de examen probables:

- Permitir guion: `ARG-12`.
- Permitir letras minusculas.
- Cambiar cantidad de letras.
- Cambiar cantidad de numeros.
- Aceptar codigo sin espacio.

## 23. extractFromText

```kotlin
fun extractFromText(rawText: String): String?
```

Funcionamiento:

1. Divide el texto en lineas.
2. Limpia espacios.
3. Elimina lineas vacias.
4. Busca primero una linea que coincida exactamente con `PANINI_CODE`.
5. Si no encuentra, busca una linea corta que tenga letras y numeros.
6. Devuelve la linea mas corta que cumpla.

Codigo:

```kotlin
val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }
```

Fallback:

```kotlin
lines
    .filter { line ->
        line.length in 3..10 &&
            line.any { it.isLetter() } &&
            line.any { it.isDigit() }
    }
    .minByOrNull { it.length }
```

Respuesta teorica posible:

```text
extractFromText primero intenta encontrar un codigo exacto tipo PANINI. Si no lo encuentra, usa una heuristica con lineas cortas que mezclen letras y numeros.
```

## 24. extractFromMlKit

```kotlin
fun extractFromMlKit(text: Text, imageWidth: Int, imageHeight: Int): String?
```

Esta funcion usa informacion espacial de ML Kit.

Si no hay dimensiones validas:

```kotlin
if (imageWidth <= 0 || imageHeight <= 0) return extractFromText(text.text)
```

Luego filtra bloques en zona superior derecha:

```kotlin
centerX > 0.55f && centerY < 0.42f
```

Interpretacion:

```text
Solo considera texto cuyo centro este hacia la derecha y arriba de la imagen.
```

Esto coincide con el recuadro visual del scanner.

Luego:

1. Busca linea exacta con `PANINI_CODE`.
2. Busca coincidencia parcial dentro de una linea.
3. Si no encuentra, usa `extractFromText` como fallback.

Respuesta teorica posible:

```text
extractFromMlKit aprovecha las coordenadas de los bloques de texto para priorizar el area donde se espera que este el codigo, y luego aplica la misma regla de extraccion.
```

## 25. ScannerUiState

Aunque no estaba en la lista principal, este estado explica toda la pantalla.

```kotlin
sealed class ScannerUiState {
    object Idle : ScannerUiState()
    object CameraReady : ScannerUiState()
    object ProcessingCapture : ScannerUiState()
    data class Searching(val code: String) : ScannerUiState()
    data class ProductFound(val product: Product, val code: String) : ScannerUiState()
    data class ProductNotFound(val code: String) : ScannerUiState()
    data class CodeNotRecognized(val rawText: String) : ScannerUiState()
    data class Error(val message: String) : ScannerUiState()
}
```

Idea clave:

```text
ScannerUiState representa todas las fases del flujo OCR.
```

## 26. Relacion con navegacion y catalogo

En `StickrVaultNavHost`, Scanner recibe:

```kotlin
canEditInventory
onAddProduct
onAddExistingProduct
```

Si el producto no existe:

```text
Scanner -> onAddProduct(code) -> CatalogViewModel.openAddProductFromScan(code) -> navega a Catalogo
```

Si el producto existe:

```text
Scanner -> onAddExistingProduct(product) -> CatalogViewModel.saveManualProduct(...) -> navega a Catalogo
```

Idea clave:

```text
Scanner detecta y consulta; Catalogo modifica inventario.
```

## 27. Cambios de examen probables en Scanner

### Cambiar texto del titulo

Archivo:

```text
ScannerScreen.kt
```

Ubicacion:

```kotlin
Text(text = "Escaner OCR")
```

### Cambiar texto de instrucciones

Archivo:

```text
ScannerScreen.kt
```

Ubicacion:

```kotlin
"Alinea el codigo en el recuadro y pulsa Capturar"
```

### Cambiar tamano del recuadro

Archivo:

```text
ScannerScreen.kt
```

Ubicacion:

```kotlin
.size(width = 140.dp, height = 72.dp)
```

### Cambiar mensaje cuando no detecta codigo

Archivo:

```text
ScannerScreen.kt
```

Ubicacion:

```kotlin
title = "No se detecto el codigo"
subtitle = "Intenta de nuevo..."
```

### Cambiar regla del codigo OCR

Archivo:

```text
OcrCodeExtractor.kt
```

Ubicacion:

```kotlin
private val PANINI_CODE = Regex(...)
```

### Cambiar zona de lectura

Archivo:

```text
OcrCodeExtractor.kt
```

Ubicacion:

```kotlin
centerX > 0.55f && centerY < 0.42f
```

### Cambiar accion de producto encontrado

Archivo probable:

```text
ScannerScreen.kt
StickrVaultNavHost.kt
```

Si cambia texto/boton:

```text
ScannerScreen.kt
```

Si cambia que sucede al presionar:

```text
StickrVaultNavHost.kt o callback recibido
```

### Cambiar permisos de auditor

Archivo probable:

```text
StickrVaultNavHost.kt
ScannerScreen.kt
```

`ScannerScreen` muestra mensajes segun `canEditInventory`.

`StickrVaultNavHost` calcula el valor de `canEditInventory`.

## 28. Explicacion corta para defensa

Version de 30 segundos:

```text
ScannerScreen maneja permiso de camara, muestra CameraPreview con CameraX y envia capturas a ML Kit. OcrCodeExtractor toma el texto reconocido y extrae un codigo tipo Panini. ScannerViewModel recibe ese codigo, busca el producto con GetProductByOcrIdentifierUseCase y actualiza ScannerUiState para mostrar producto encontrado, no encontrado o error.
```

Version de 1 minuto:

```text
El scanner esta dividido por responsabilidades. ScannerScreen muestra la interfaz, solicita permiso de camara y contiene CameraPreview. CameraPreview integra CameraX mediante AndroidView y usa ImageAnalysis para procesar una imagen solo cuando ScannerCameraController recibe una solicitud de captura. ML Kit reconoce texto y OcrCodeExtractor aplica reglas para obtener el codigo. ScannerViewModel administra estados como Idle, CameraReady, ProcessingCapture, Searching, ProductFound y ProductNotFound. Si hay producto, la UI permite sumar stock segun permisos; si no existe, permite abrir el formulario del catalogo con el codigo detectado.
```

## 29. Checklist para dominar esta fase

- Puedo explicar que hace `ScannerScreen`.
- Puedo explicar como se pide permiso de camara.
- Puedo explicar cuando se muestra `CameraPreview`.
- Puedo explicar para que sirve `ScannerCameraController`.
- Puedo explicar por que no se analiza cada frame.
- Puedo explicar como CameraX se integra con Compose usando `AndroidView`.
- Puedo explicar como ML Kit reconoce texto.
- Puedo explicar que hace `OcrCodeExtractor`.
- Puedo explicar la regex de codigo Panini.
- Puedo explicar como se busca producto por OCR.
- Puedo explicar los estados de `ScannerUiState`.
- Puedo ubicar donde cambiar textos, recuadro, regex y permisos.

## 30. Mini simulacros de esta fase

### Simulacro 1: cambiar texto del boton Capturar codigo

Tipo de cambio: UI/texto.

Archivo:

```text
ScannerScreen.kt
```

Ubicacion:

```kotlin
Text("Capturar codigo")
```

Explicacion:

```text
Es un texto visible del boton de captura, por eso se cambia en la pantalla Compose.
```

### Simulacro 2: agrandar el recuadro OCR

Tipo de cambio: UI/diseno.

Archivo:

```text
ScannerScreen.kt
```

Ubicacion:

```kotlin
.size(width = 140.dp, height = 72.dp)
```

Explicacion:

```text
El recuadro es un Box visual con size y border, por eso se modifica en ScannerScreen.
```

### Simulacro 3: aceptar codigos con guion como ARG-12

Tipo de cambio: regla OCR.

Archivo:

```text
OcrCodeExtractor.kt
```

Ubicacion:

```kotlin
private val PANINI_CODE = Regex(...)
```

Idea:

```text
Cambiar la regex para aceptar espacio o guion entre letras y numeros.
```

Explicacion:

```text
La regla de formato del codigo esta centralizada en OcrCodeExtractor.
```

### Simulacro 4: cambiar mensaje para auditor

Tipo de cambio: permisos/mensaje.

Archivo:

```text
ScannerScreen.kt
```

Ubicacion:

```kotlin
"Tu rol de auditor permite consultar el producto, pero no modificar el inventario."
```

Explicacion:

```text
ScannerScreen muestra mensajes segun canEditInventory. El calculo del permiso viene desde NavHost.
```

### Simulacro 5: procesar otra zona de la imagen

Tipo de cambio: OCR/zona de lectura.

Archivo:

```text
OcrCodeExtractor.kt
```

Ubicacion:

```kotlin
centerX > 0.55f && centerY < 0.42f
```

Explicacion:

```text
La zona de lectura se calcula con coordenadas relativas del boundingBox reconocido por ML Kit.
```

### Simulacro 6: evitar que se cierre el scanner despues de agregar producto

Tipo de cambio: flujo.

Archivo probable:

```text
ScannerScreen.kt
```

Ubicacion:

```kotlin
onAddProduct(s.code)
viewModel.reset()
```

Explicacion:

```text
reset vuelve a Idle. Si se quiere mantener o volver a camara, se cambia por backToCamera o se elimina segun el caso.
```

## 31. Frase clave para recordar

```text
ScannerScreen maneja camara y UI; ScannerCameraController dispara una captura; OcrCodeExtractor extrae el codigo; ScannerViewModel busca el producto y cambia el estado.
```

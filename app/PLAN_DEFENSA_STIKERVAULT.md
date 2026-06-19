# Guia de defensa de StikerVault

## 1. Objetivo de la preparacion

Esta guia sirve para preparar la defensa del proyecto StikerVault bajo una dinamica de examen practica:

1. Se presenta un caso de modificacion.
2. Hay aproximadamente 15 minutos para resolverlo.
3. Se entrega evidencia con captura del emulador, fragmento de codigo modificado y una respuesta teorica breve.

El enfoque de estudio tiene dos partes:

- Entender StikerVault por capas, clases y flujo de datos.
- Practicar cambios generales que tambien puedan aparecer en otros proyectos Android del curso.

## 2. Mapa general del proyecto

StikerVault es una aplicacion Android escrita en Kotlin con Jetpack Compose. La app esta organizada con una arquitectura por capas:

- `presentation`: pantallas Compose, estados UI, ViewModels y navegacion.
- `domain`: modelos principales, interfaces de repositorio y casos de uso.
- `data`: implementaciones de repositorio, Room, Retrofit/Supabase, DTOs, entidades y mappers.
- `ui/theme`: colores, tipografia y tema visual.
- `res`: recursos Android como strings, iconos, temas y configuraciones XML.

Flujo general:

```text
Pantalla Compose -> Estado UI -> ViewModel -> UseCase -> Repository -> Room / Supabase
```

En una modificacion de examen conviene ubicar primero el tipo de cambio:

- Cambio visual: buscar en `Screen`, composables, `Modifier`, `Card`, `Text`, `Button`, `padding`, `color`, `shape`.
- Cambio de texto: buscar en `strings.xml` o texto directo en Compose.
- Cambio de validacion: buscar en el `ViewModel` o en callbacks de formularios.
- Cambio de lista/filtro: buscar en `ViewModel`, `UseCase`, `Repository` o DAO.
- Cambio de navegacion: buscar en `Routes`, `StickrVaultNavHost` o `BottomNavigationBar`.
- Cambio de datos: revisar modelos, entidades, DTOs, mappers, DAOs y repositorios.

## 3. Estructura comun que aplica a cualquier app Android del curso

Antes de ir a clases especificas, estudiar esta estructura porque aparece en casi todos los proyectos:

- `AndroidManifest.xml`: permisos, actividad principal, icono, nombre de app y configuracion general.
- `strings.xml`: textos globales o nombre de la aplicacion.
- `build.gradle.kts`: SDK, plugins y dependencias.
- `MainActivity.kt`: punto de entrada de la app.
- Pantallas Compose: estructura visual, componentes, eventos y estados.
- ViewModels: reglas de presentacion, carga de datos y validaciones.
- Modelos: datos que viajan entre capas.
- Repositorios: acceso a base local, API remota o datos simulados.

## 4. Orden de estudio de StikerVault

### 4.1 Entrada principal

`MainActivity.kt`

- Crea la base local con `AppDatabase.getInstance`.
- Obtiene DAOs de productos, movimientos y usuarios.
- Crea repositorios manualmente.
- Crea casos de uso.
- Crea factories para ViewModels.
- Configura `MyApplicationTheme`.
- Usa `Scaffold` para mostrar u ocultar la barra inferior.
- Carga `StickrVaultNavHost`.

Idea teorica clave: aqui no se usa un framework de inyeccion de dependencias; las dependencias se crean manualmente y se pasan a los ViewModels mediante factories.

### 4.2 Navegacion

Archivos principales:

- `Routes.kt`
- `StickrVaultNavHost.kt`
- `BottomNavigationBar.kt`

Responsabilidades:

- Definir rutas tipadas.
- Decidir la pantalla inicial.
- Conectar cada ruta con su pantalla Compose.
- Mostrar accesos inferiores a Home, Catalogo, Scanner y Reportes.

Cambios tipicos:

- Cambiar pantalla inicial.
- Ocultar o mostrar un item del bottom navigation.
- Redirigir a otra pantalla despues de una accion.
- Cambiar icono o texto de una opcion.

### 4.3 Autenticacion y sesion

Archivos principales:

- `LoginScreen.kt`
- `AuthViewModel.kt`
- `AuthUiState.kt`
- `AuthRepositoryImpl.kt`
- `SessionPreferences.kt`
- `AppUser.kt`
- `UserRole.kt`

Funcionamiento:

- El usuario ingresa su correo.
- `AuthViewModel.login` valida que el correo no este vacio.
- `LoginUseCase` llama al repositorio.
- `AuthRepositoryImpl` busca el usuario en Supabase y guarda la sesion.
- Si falla la red, intenta usar usuario local.
- `SessionPreferences` guarda datos basicos de sesion con DataStore.

Cambios tipicos:

- Cambiar mensaje de error.
- Validar formato de correo.
- Cambiar comportamiento al cerrar sesion.
- Mostrar u ocultar acciones segun rol.

### 4.4 Catalogo e inventario

Archivos principales:

- `CatalogScreen.kt`
- `CatalogViewModel.kt`
- `CatalogUiState.kt`
- `AddProductDraft.kt`
- `Product.kt`
- `ProductCategory.kt`
- `ProductRepositoryImpl.kt`
- `ProductDao.kt`

Funcionamiento:

- La pantalla muestra productos, busqueda, filtros y formulario de agregado.
- El ViewModel maneja carga, busqueda, filtrado y guardado.
- Si el producto ya existe por nombre y categoria, se actualiza stock.
- Si no existe, se crea un nuevo producto.
- Cada cambio de stock registra un movimiento.
- El rol `AUDITOR` no puede modificar inventario.
- Room sirve como cache local y soporte offline.
- Supabase sirve como fuente remota cuando hay conexion.

Cambios tipicos:

- Cambiar margen o padding de cards de productos.
- Cambiar valor minimo por defecto.
- Agregar validacion para stock.
- Cambiar el texto de botones o mensajes.
- Cambiar orden de productos.
- Modificar filtro por categoria.
- Limitar cantidad de productos mostrados.

### 4.5 Home

Archivos principales:

- `HomeScreen.kt`
- `HomeViewModel.kt`
- `HomeUiState.kt`

Funcionamiento:

- Muestra resumen general de productos, stock, usuarios y movimientos recientes.
- Usa casos de uso para obtener productos, movimientos y usuarios.
- Calcula metricas para mostrar en cards.

Cambios tipicos:

- Cambiar titulo de una metrica.
- Agregar una metrica calculada.
- Cambiar color o icono de una card.
- Cambiar cantidad de movimientos recientes.
- Modificar formato de fecha relativa.

### 4.6 Reportes

Archivos principales:

- `ReportsScreen.kt`
- `ReportsViewModel.kt`
- `ReportUiState.kt`

Funcionamiento:

- Carga productos y movimientos.
- Calcula metricas de inventario.
- Lista movimientos recientes.

Cambios tipicos:

- Cambiar cantidad de movimientos visibles.
- Agregar una tarjeta de resumen.
- Cambiar orden de la lista.
- Cambiar texto o formato de movimientos.

### 4.7 Scanner y OCR

Archivos principales:

- `ScannerScreen.kt`
- `ScannerViewModel.kt`
- `ScannerUiState.kt`
- `ScannerCameraController.kt`
- `OcrCodeExtractor.kt`

Funcionamiento:

- Usa CameraX para capturar imagen.
- Usa ML Kit para reconocer texto.
- Extrae un codigo OCR.
- Busca producto por identificador OCR.
- Si existe, muestra resultado.
- Si no existe, permite abrir formulario de producto con codigo prellenado.

Cambios tipicos:

- Cambiar mensaje cuando no se detecta producto.
- Cambiar regla de extraccion OCR.
- Cambiar texto de botones.
- Ajustar flujo hacia catalogo.

### 4.8 Capa de datos

Archivos principales:

- `AppDatabase.kt`
- DAOs: `ProductDao`, `StockMovementDao`, `AppUserDao`.
- Entidades: `ProductEntity`, `StockMovementEntity`, `AppUserEntity`.
- DTOs: `ProductDto`, `StockMovementDto`, `AppUserDto`.
- Mappers: `ProductMapper`, `StockMovementMapper`, `AppUserMapper`.
- Repositorios: `ProductRepositoryImpl`, `StockMovementRepositoryImpl`, `AuthRepositoryImpl`.
- API: `SupabaseApiService`, `RetrofitClient`.

Funcionamiento:

- Room guarda datos locales.
- Retrofit se comunica con Supabase.
- Los DTOs representan datos remotos.
- Las entidades representan datos locales.
- Los modelos de dominio representan datos usados por la app.
- Los mappers convierten entre DTO, Entity y Domain.

Idea teorica clave: separar modelos evita que la UI dependa directamente de la base de datos o de la API.

## 5. Banco de casos generales de examen

Estos casos estan pensados para ser transferibles a otros proyectos Android del curso.

### 5.1 UI y diseno

- Cambiar margen, padding, tamano, color o borde de cards.
- Cambiar textos visibles usando `strings.xml` o constantes en Compose.
- Modificar posicion de botones, campos o secciones.
- Agregar un mensaje visual cuando una lista este vacia.
- Cambiar forma de una card con `shape`.
- Cambiar color de fondo de una pantalla.
- Cambiar espaciado entre elementos de una lista.

Ruta rapida:

1. Buscar el texto visible o nombre de pantalla.
2. Ubicar el composable.
3. Modificar `Modifier.padding`, `CardDefaults`, `Text`, `Button`, `Spacer`, `LazyColumn` o colores.
4. Ejecutar y capturar pantalla.

### 5.2 Formularios y validaciones

- Hacer obligatorio un campo.
- Cambiar valor minimo o maximo permitido.
- Mostrar un mensaje de error diferente.
- Limpiar campos despues de guardar.
- Deshabilitar boton si el formulario esta incompleto.
- Validar formato de correo, numero o texto.

Ruta rapida:

1. Buscar el boton de guardar o funcion `onClick`.
2. Revisar si la validacion esta en pantalla o ViewModel.
3. Agregar condicion antes de guardar.
4. Mostrar error en estado UI o variable local.

### 5.3 Listas, filtros y busqueda

- Cambiar orden de elementos.
- Agregar o modificar un filtro.
- Ajustar busqueda por nombre, descripcion o categoria.
- Limitar cantidad de elementos mostrados.
- Mostrar contador de resultados.
- Mostrar mensaje cuando no existan resultados.

Ruta rapida:

1. Buscar `LazyColumn`, `items`, `search`, `filter` o `sorted`.
2. Determinar si el cambio es visual o de datos.
3. Si es simple, aplicar `filter`, `sortedBy`, `take` en ViewModel o pantalla.
4. Si afecta datos reales, revisar UseCase, Repository o DAO.

### 5.4 Navegacion

- Cambiar pantalla inicial.
- Agregar o esconder una opcion del bottom navigation.
- Redirigir despues de guardar, cancelar o cerrar sesion.
- Cambiar label o icono de una ruta.

Ruta rapida:

1. Revisar `Routes`.
2. Revisar `NavHost`.
3. Revisar `BottomNavigationBar`.
4. Buscar llamadas a `navController.navigate`.

### 5.5 Estado y permisos

- Cambiar comportamiento segun rol de usuario.
- Mostrar u ocultar acciones segun estado.
- Cambiar mensajes de exito, error o carga.
- Bloquear accion si el usuario no tiene permiso.
- Mostrar loading mientras se cargan datos.

Ruta rapida:

1. Buscar `UiState`, `currentUser`, `role`, `Loading`, `Error`, `Success`.
2. Revisar ViewModel para reglas.
3. Revisar Screen para visibilidad.
4. Cambiar mensaje o condicion.

### 5.6 Datos

- Cambiar un valor por defecto.
- Agregar un campo visual calculado sin modificar base de datos.
- Modificar una consulta local o remota simple.
- Ajustar sincronizacion local/remota solo si el caso lo exige.
- Cambiar cantidad de elementos consultados.

Ruta rapida:

1. Buscar el valor por defecto o campo afectado.
2. Revisar si vive en modelo, ViewModel, repositorio o DAO.
3. Evitar migraciones si se puede resolver como dato calculado visual.
4. Si se cambia entidad Room, considerar version de base y migracion.

## 6. Metodo para resolver un caso en 15 minutos

### Minutos 0 a 2: entender

Traducir el caso a esta frase:

```text
Debo cambiar [comportamiento visual/dato/validacion/navegacion] en [pantalla o flujo].
```

Identificar:

- Pantalla afectada.
- Clase probable.
- Tipo de cambio.
- Evidencia esperada.

### Minutos 2 a 6: ubicar

Buscar por:

- Texto visible.
- Nombre de pantalla.
- Nombre del componente.
- Palabras como `Card`, `padding`, `Button`, `Text`, `filter`, `search`, `navigate`, `Error`.

### Minutos 6 a 11: modificar

Hacer el cambio minimo necesario:

- Si es UI, tocar solo el composable.
- Si es validacion, tocar ViewModel o callback del formulario.
- Si es navegacion, tocar NavHost o llamada a `navigate`.
- Si es dato calculado, preferir ViewModel antes que cambiar base de datos.

### Minutos 11 a 14: verificar

- Ejecutar la app.
- Llegar al flujo afectado.
- Confirmar visualmente el cambio.
- Tomar captura del emulador.

### Minuto 14 a 15: preparar explicacion

Responder en 3 partes:

1. Que se modifico.
2. En que archivo/clase.
3. Por que esa capa era la correcta.

Ejemplo:

```text
Modifique el padding de la card de producto en CatalogScreen porque era un cambio visual.
No afecte el ViewModel ni el repositorio porque no cambia la logica de negocio ni los datos.
El cambio se evidencia en el emulador con mayor separacion entre cards.
```

## 7. Checklist de evidencia

Antes de entregar:

- La app compila.
- El cambio se ve o se puede probar en emulador.
- Hay captura de pantalla.
- Esta identificado el archivo modificado.
- Esta listo el fragmento de codigo modificado.
- La respuesta teorica explica capa, responsabilidad y razon.

Formato sugerido de evidencia:

```text
Caso asignado:
Cambio realizado:
Archivo modificado:
Fragmento de codigo:
Captura:
Explicacion teorica:
```

## 8. Preguntas teoricas frecuentes

### Por que usar ViewModel?

Porque conserva y administra el estado de la pantalla, separando la logica de presentacion de la UI Compose.

### Por que usar UiState?

Porque permite representar estados como carga, exito, error o vacio de forma clara y observable.

### Por que separar Repository y UseCase?

El UseCase expresa una accion de negocio y el Repository oculta de donde vienen los datos, ya sea base local o API remota.

### Por que usar Room?

Room permite persistencia local, consultas estructuradas y funcionamiento offline.

### Por que usar Retrofit?

Retrofit simplifica las llamadas HTTP hacia una API remota y permite mapear respuestas a DTOs.

### Por que usar mappers?

Porque convierten entre modelos de API, base local y dominio, manteniendo separadas las responsabilidades.

### Cuando tocar la UI y cuando tocar el ViewModel?

Se toca la UI para cambios visuales o de texto. Se toca el ViewModel cuando cambia una regla, validacion, estado o flujo de datos de la pantalla.

## 9. Simulacros recomendados

1. Cambiar el margen de las cards de productos.
2. Cambiar el texto del boton de agregar producto.
3. Cambiar el stock minimo por defecto de 15 a 10.
4. Mostrar mensaje personalizado cuando no hay productos.
5. Limitar el catalogo a los primeros 5 productos.
6. Ordenar productos por stock ascendente.
7. Bloquear una accion para usuarios auditores.
8. Cambiar la pantalla inicial despues de login.
9. Agregar una metrica simple al Home.
10. Cambiar mensaje del scanner cuando no encuentra coincidencias.

## 10. Regla de oro para el examen

Si el cambio es general y visible, buscar primero en la pantalla Compose. Si el cambio altera una regla o dato, buscar en el ViewModel. Si el cambio afecta origen de datos, buscar en Repository, DAO o API.

La solucion mas segura en 15 minutos es la que modifica menos archivos y se puede explicar con claridad.

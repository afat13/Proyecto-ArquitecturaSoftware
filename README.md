# Aprende a Aprender

<p align="center">
  <img src="app/src/main/res/drawable/logo.png" alt="Logo de Aprende a Aprender" width="170" />
</p>

Aplicación móvil Android orientada a la organización académica y al refuerzo del aprendizaje. Permite administrar materias y tareas, consultar información académica, recibir notificaciones y realizar retos de estudio generados mediante un modelo de inteligencia artificial ejecutado localmente en el dispositivo.



## Funcionalidades principales

* Registro e inicio de sesión mediante Firebase Authentication.
* Verificación de correo electrónico.
* Recuperación de contraseña.
* Gestión del perfil del usuario.
* Creación y eliminación de materias.
* Asociación de temas a las materias.
* Creación, actualización y eliminación de tareas.
* Clasificación de tareas por prioridad y estado.
* Consulta de tareas próximas.
* Integración con información académica de UTADEO.
* Sincronización de materias y tareas académicas.
* Consulta de conversaciones académicas.
* Notificaciones de tareas y mensajes.
* Retos académicos por materia.
* Generación de preguntas utilizando Gemma ejecutado localmente.
* Descarga automática del modelo mediante WorkManager.
* Caché local de preguntas para reducir tiempos de espera.

## Tecnologías utilizadas

| Tecnología                 | Uso                                                |
| -------------------------- | -------------------------------------------------- |
| Kotlin 2.0.21              | Lenguaje principal                                 |
| Jetpack Compose            | Interfaz de usuario                                |
| Material 3                 | Componentes visuales                               |
| Navigation Compose         | Navegación                                         |
| ViewModel                  | Gestión de estado                                  |
| StateFlow                  | Estado reactivo                                    |
| Firebase Authentication    | Autenticación                                      |
| Firebase Realtime Database | Persistencia de usuarios, materias, tareas y retos |
| Firebase Analytics         | Analítica                                          |
| WorkManager                | Trabajos y descargas en segundo plano              |
| LiteRT-LM 0.11.0           | Ejecución local del modelo de IA                   |
| Gemma                      | Generación local de preguntas                      |
| OkHttp 4.12.0              | Solicitudes HTTP                                   |
| Jsoup 1.17.2               | Procesamiento de contenido web                     |
| Android Security Crypto    | Almacenamiento seguro de credenciales              |
| Gradle Kotlin DSL          | Configuración del proyecto                         |

---

# 1. Requisitos previos

Antes de ejecutar el proyecto debe tener instalado lo siguiente.

### Git

Necesario para clonar el repositorio.

Comprobar instalación:

```bash
git --version
```

### JDK 17

Se recomienda utilizar JDK 17 para ejecutar Gradle y compilar el proyecto.

Comprobar:

```bash
java -version
```

La salida debe indicar una versión 17 de Java.

### Android Studio

Debe utilizar una versión de Android Studio compatible con:

```text
Android Gradle Plugin: 9.0.1
Kotlin: 2.0.21
Gradle Wrapper: 9.1.0
```

No es necesario instalar Gradle de forma independiente porque el repositorio incluye Gradle Wrapper.

### Android SDK

El proyecto utiliza:

```text
compileSdk = 36
targetSdk = 36
minSdk = 26
```

Por lo tanto, deben estar instalados:

```text
Android SDK Platform 36
Android SDK Build-Tools 36.0.0
```

Desde Android Studio pueden instalarse en:

```text
Tools
→ SDK Manager
→ SDK Platforms
→ Android API 36
```

### Dispositivo Android

Se necesita uno de los siguientes:

* dispositivo físico con Android 8.0 o superior;
* emulador con API 26 o superior.

Para comprobar que ADB reconoce el dispositivo:

```bash
adb devices
```

Debe aparecer al menos un dispositivo con estado:

```text
device
```

### Conexión a Internet

La conexión es necesaria para:

* descargar dependencias en la primera compilación;
* utilizar Firebase;
* sincronizar información de UTADEO;
* descargar inicialmente el modelo Gemma.

Una vez descargado y cargado correctamente el modelo, la generación de los retos se realiza localmente.

---

# 2. Configuración inicial

## 2.1 Clonar el proyecto

Clonar directamente la rama `retos`:

```bash
git clone --branch retos --single-branch https://github.com/afat13/Aprende-Aprender.git
```

Entrar al directorio:

```bash
cd Aprende-Aprender
```

Comprobar la rama:

```bash
git branch --show-current
```

Resultado esperado:

```text
retos
```

---

## 2.2 Configurar Android SDK

Normalmente Android Studio crea automáticamente el archivo:

```text
local.properties
```

Si aparece el error:

```text
SDK location not found
```

crear `local.properties` en la raíz del proyecto.

### Windows

Ejemplo:

```properties
sdk.dir=C:\\Users\\USUARIO\\AppData\\Local\\Android\\Sdk
```

Sustituya `USUARIO` por el usuario de Windows correspondiente.

También puede comprobar la ubicación desde:

```text
Android Studio
→ Settings
→ Languages & Frameworks
→ Android SDK
```

### Linux

Ejemplo:

```properties
sdk.dir=/home/usuario/Android/Sdk
```

### macOS

Ejemplo:

```properties
sdk.dir=/Users/usuario/Library/Android/sdk
```

El archivo `local.properties` no debe subirse al repositorio.

---

## 2.3 Configuración de Firebase

La aplicación utiliza:

```text
Firebase Authentication
Firebase Realtime Database
Firebase Analytics
```

La configuración Android está asociada mediante:

```text
app/google-services.json
```

Los datos principales de cada usuario se almacenan bajo la estructura:

```text
usuarios
└── {uid}
    ├── correo
    ├── nombres
    ├── apellidos
    ├── telefono
    └── materias
        └── {materiaId}
            ├── asignatura
            ├── instructor
            ├── temas
            └── tareas
```

Las materias creadas desde la aplicación se almacenan en:

```text
usuarios/{uid}/materias/{materiaId}
```

Las tareas se almacenan dentro de su materia:

```text
usuarios/{uid}/materias/{materiaId}/tareas/{tareaId}
```

---

## 2.4 Configuración del modelo de inteligencia artificial

La aplicación utiliza un modelo LiteRT-LM almacenado localmente con el nombre:

```text
gemma-4-E2B-it.litertlm
```

El archivo se guarda dentro del almacenamiento privado de la aplicación en:

```text
files/models/gemma-4-E2B-it.litertlm
```

Los parámetros del modelo se reciben mediante propiedades de Gradle:

```properties
GEMMA_MODEL_URL=
GEMMA_MODEL_TOKEN=
GEMMA_MODEL_SHA256=
GEMMA_MODEL_SIZE_BYTES=
```

También existe la propiedad:

```properties
APIS=
```

para claves del servicio OpenRouter incluido en el proyecto.

Las claves de `APIS` pueden separarse por:

```text
,
;
salto de línea
```

Ejemplo:

```properties
APIS=CLAVE_1,CLAVE_2
```

No deben almacenarse claves privadas reales dentro de archivos que vayan a publicarse.

Una opción para desarrollo es utilizar el archivo global de Gradle del usuario.

### Windows

```text
C:\Users\USUARIO\.gradle\gradle.properties
```

### Linux/macOS

```text
~/.gradle/gradle.properties
```

Ejemplo:

```properties
APIS=

GEMMA_MODEL_URL=https://servidor/modelos/gemma-4-E2B-it.litertlm
GEMMA_MODEL_TOKEN=
GEMMA_MODEL_SHA256=
GEMMA_MODEL_SIZE_BYTES=0
```

### `GEMMA_MODEL_URL`

Debe apuntar directamente al archivo `.litertlm`.

Correcto:

```text
https://servidor/.../resolve/.../gemma-4-E2B-it.litertlm
```

Incorrecto:

```text
https://servidor/.../blob/.../gemma-4-E2B-it.litertlm
```

La respuesta HTTP debe contener el archivo binario y no una página HTML.

### `GEMMA_MODEL_TOKEN`

Solo es necesario cuando el servidor donde está almacenado el modelo requiere autenticación.

Puede quedar vacío si el archivo es público:

```properties
GEMMA_MODEL_TOKEN=
```

### `GEMMA_MODEL_SHA256`

Permite validar la integridad del archivo descargado.

Ejemplo:

```properties
GEMMA_MODEL_SHA256=SHA256_DEL_ARCHIVO
```

### `GEMMA_MODEL_SIZE_BYTES`

Permite comprobar que el archivo tiene exactamente el tamaño esperado.

Si no se desea validar el tamaño:

```properties
GEMMA_MODEL_SIZE_BYTES=0
```

Para un entorno controlado es recomendable utilizar tanto SHA-256 como tamaño esperado.

---

# 3. Instalación y preparación

El repositorio contiene Gradle Wrapper, por lo que no es necesario instalar Gradle manualmente.

## Windows

Comprobar Gradle:

```powershell
.\gradlew.bat --version
```

## Linux/macOS

Dar permiso de ejecución si es necesario:

```bash
chmod +x gradlew
```

Comprobar:

```bash
./gradlew --version
```

Debe utilizarse:

```text
Gradle 9.1.0
```

A continuación descargar dependencias y compilar la aplicación.

### Windows

```powershell
.\gradlew.bat assembleDebug
```

### Linux/macOS

```bash
./gradlew assembleDebug
```

La primera ejecución puede descargar:

* Gradle;
* Android Gradle Plugin;
* dependencias de AndroidX;
* Firebase;
* Compose;
* LiteRT-LM;
* WorkManager;
* OkHttp;
* demás librerías del proyecto.

Al terminar debe aparecer:

```text
BUILD SUCCESSFUL
```

El APK generado estará en:

```text
app/build/outputs/apk/debug/app-debug.apk
```

---

# 4. Cómo ejecutar la aplicación

Existen dos formas principales.

## Opción A: Android Studio

1. Abrir Android Studio.
2. Seleccionar `Open`.
3. Seleccionar la carpeta raíz `Aprende-Aprender`.
4. Esperar a que termine `Gradle Sync`.
5. Seleccionar un emulador o dispositivo físico.
6. Seleccionar la configuración `app`.
7. Presionar `Run`.

La actividad principal ejecutada es:

```text
com.example.aprendeaprender.MainActivity
```

---

## Opción B: Terminal

Primero comprobar el dispositivo:

```bash
adb devices
```

Después instalar la aplicación.

### Windows

```powershell
.\gradlew.bat installDebug
```

### Linux/macOS

```bash
./gradlew installDebug
```

Resultado esperado:

```text
BUILD SUCCESSFUL
```

Posteriormente puede abrirse directamente mediante ADB:

```bash
adb shell am start -n com.example.aprendeaprender/.MainActivity
```

---

# 5. Cómo verificar que funciona

La verificación puede realizarse en varios niveles.

## 5.1 Verificar la compilación

### Windows

```powershell
.\gradlew.bat assembleDebug
```

### Linux/macOS

```bash
./gradlew assembleDebug
```

Resultado esperado:

```text
BUILD SUCCESSFUL
```

También debe existir:

```text
app/build/outputs/apk/debug/app-debug.apk
```

---

## 5.2 Verificar el arranque

Instalar:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Abrir:

```bash
adb shell am start -n com.example.aprendeaprender/.MainActivity
```

Al iniciar debe mostrarse primero la pantalla de carga.

Después de aproximadamente un segundo:

* si no existe una sesión activa, debe mostrarse el inicio de sesión;
* si existe una sesión válida, debe abrirse la pantalla principal.

---

## 5.3 Verificar autenticación

Desde la aplicación:

1. crear una cuenta;
2. completar los datos solicitados;
3. verificar el correo si corresponde;
4. iniciar sesión.

Una autenticación correcta debe permitir acceder a la pantalla principal.

El usuario debe aparecer en Firebase Authentication y su perfil debe existir en:

```text
usuarios/{uid}
```

---

## 5.4 Verificar materias

Crear una materia desde:

```text
Materias → Agregar
```

Después de guardar:

* la materia debe aparecer en la lista;
* debe poder abrirse su detalle;
* debe existir en Firebase bajo:

```text
usuarios/{uid}/materias/{materiaId}
```

---

## 5.5 Verificar tareas

Crear una tarea asociada a una materia.

Después de guardar:

* debe aparecer en la lista de tareas;
* debe aparecer dentro de la materia correspondiente;
* debe conservar prioridad, estado y fecha de entrega.

En Firebase debe existir en:

```text
usuarios/{uid}/materias/{materiaId}/tareas/{tareaId}
```

---

## 5.6 Verificar el modelo Gemma

Al iniciar la aplicación se comprueba automáticamente si el modelo existe.

Si no existe y `GEMMA_MODEL_URL` está configurado, la aplicación inicia su descarga mediante WorkManager.

Durante el proceso pueden aparecer estados como:

```text
Verificando modelo de IA...
Descargando modelo Gemma 4...
```

Después de una descarga y carga correcta el módulo de retos queda preparado para generar preguntas.

Para inspeccionar mensajes relacionados con el modelo:

### Windows / PowerShell

```powershell
adb logcat | Select-String "Gemma"
```

### Linux/macOS

```bash
adb logcat | grep Gemma
```

---

## 5.7 Verificar los retos

Para comprobar el módulo:

1. iniciar sesión;
2. crear al menos una materia;
3. opcionalmente agregar temas;
4. opcionalmente crear tareas asociadas;
5. esperar a que Gemma esté listo;
6. abrir `Retos`;
7. seleccionar una materia;
8. iniciar el reto.

Debe generarse una pregunta con:

```text
Pregunta
4 opciones
Respuesta correcta
Explicación
```

Cada reto utiliza seis preguntas.

Cuando existen tareas asociadas, estas se utilizan como contexto para generar preguntas relacionadas con el contenido académico del usuario.

---

# 6. Cómo ejecutar las pruebas

## Pruebas unitarias

### Windows

```powershell
.\gradlew.bat testDebugUnitTest
```

### Linux/macOS

```bash
./gradlew testDebugUnitTest
```

Resultado esperado:

```text
BUILD SUCCESSFUL
```

El reporte HTML queda disponible en:

```text
app/build/reports/tests/testDebugUnitTest/index.html
```

---

## Pruebas instrumentadas

Estas pruebas necesitan un dispositivo o emulador Android activo.

Comprobar primero:

```bash
adb devices
```

Después ejecutar:

### Windows

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

### Linux/macOS

```bash
./gradlew connectedDebugAndroidTest
```

Resultado esperado:

```text
BUILD SUCCESSFUL
```

---

## Compilación y pruebas en un solo comando

### Windows

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug
```

### Linux/macOS

```bash
./gradlew testDebugUnitTest assembleDebug
```

Este comando verifica simultáneamente que:

* el código compila;
* las pruebas unitarias pasan;
* puede generarse un APK debug.

El proyecto dispone actualmente de una prueba unitaria básica del entorno y una prueba instrumentada que comprueba que el package de la aplicación sea:

```text
com.example.aprendeaprender
```

Estas pruebas comprueban que la infraestructura de pruebas y la aplicación Android pueden ejecutarse correctamente.

---

# 7. Datos iniciales

La aplicación no necesita una base de datos precargada para iniciar.

Un usuario nuevo comienza sin materias ni tareas. Los datos académicos se crean desde la interfaz o pueden obtenerse mediante la integración con UTADEO.

Para disponer de un estado mínimo reproducible puede utilizarse el siguiente procedimiento.

## Crear usuario

Registrar una cuenta desde la aplicación e iniciar sesión.

## Crear materia de prueba

Crear:

```text
Asignatura:
Arquitectura de Software

Instructor:
Profesor de prueba

Temas:
Patrones de arquitectura
Calidad de software
MVVM
```

Después comprobar que aparece en:

```text
Materias
```

## Crear tarea de prueba

Dentro de la materia anterior crear:

```text
Título:
Revisar patrón MVVM

Descripción:
Identificar las responsabilidades de Model, View y ViewModel.

Prioridad:
MEDIA

Estado:
PENDIENTE
```

Después comprobar que:

* aparece en `Tareas`;
* aparece en el detalle de `Arquitectura de Software`;
* puede modificarse su estado.

## Comprobar en Firebase

La estructura esperada será equivalente a:

```text
usuarios
└── {uid}
    └── materias
        └── {materiaId}
            ├── asignatura: "Arquitectura de Software"
            ├── instructor: "Profesor de prueba"
            ├── temas
            │   ├── "Patrones de arquitectura"
            │   ├── "Calidad de software"
            │   └── "MVVM"
            └── tareas
                └── {tareaId}
                    ├── titulo: "Revisar patrón MVVM"
                    ├── prioridad: "MEDIA"
                    └── estado: "PENDIENTE"
```

Con esta información ya es posible comprobar:

* persistencia;
* materias;
* tareas;
* pantalla principal;
* retos;
* generación de preguntas.

---

# 8. Sincronización con UTADEO

La aplicación permite utilizar información académica proveniente de UTADEO.

La sincronización puede incorporar:

* materias;
* profesor;
* participantes;
* tareas;
* fechas de entrega;
* estado de las actividades.

Las materias provenientes de UTADEO utilizan identificadores con el formato:

```text
utadeo_{courseId}
```

Las tareas sincronizadas utilizan:

```text
utadeo_assign_{assignmentId}
```

Esto permite ejecutar nuevamente la sincronización sin crear registros duplicados.

Cuando una actividad ya existe, la sincronización actualiza sus datos en lugar de generar otra copia.

---

# 9. Cómo detener o limpiar el entorno

## Detener la aplicación

```bash
adb shell am force-stop com.example.aprendeaprender
```

---

## Limpiar archivos de compilación

### Windows

```powershell
.\gradlew.bat clean
```

### Linux/macOS

```bash
./gradlew clean
```

Esto elimina los archivos generados dentro de:

```text
app/build/
build/
```

No elimina información de Firebase.

---

## Limpiar los datos locales de la aplicación

```bash
adb shell pm clear com.example.aprendeaprender
```

Esto elimina del dispositivo:

* sesión local;
* preferencias;
* credenciales locales;
* caché de preguntas;
* información interna;
* modelo Gemma descargado.

Después de ejecutar este comando, el modelo tendrá que descargarse nuevamente si no existe localmente.

Los datos almacenados en Firebase no se eliminan.

---

## Desinstalar la aplicación

```bash
adb uninstall com.example.aprendeaprender
```

Resultado esperado:

```text
Success
```

---

## Reiniciar completamente una cuenta de pruebas

Borrar los datos locales no elimina los datos remotos.

Para comenzar completamente desde cero con una cuenta de pruebas deben eliminarse también sus datos de Firebase.

Ruta principal:

```text
usuarios/{uid}
```

Si también se desea eliminar la autenticación, debe eliminarse la cuenta correspondiente desde Firebase Authentication.

Este procedimiento debe realizarse únicamente sobre usuarios utilizados para desarrollo o pruebas.

---

# 10. Problemas conocidos

## Android SDK no encontrado

Error:

```text
SDK location not found
```

Solución:

crear `local.properties` en la raíz del proyecto y establecer una ruta válida:

```properties
sdk.dir=C:\\Users\\USUARIO\\AppData\\Local\\Android\\Sdk
```

---

## El modelo de IA no se descarga

Si aparece:

```text
Falta configurar el modelo.
```

comprobar:

```properties
GEMMA_MODEL_URL=
```

La URL debe existir y apuntar directamente a un archivo:

```text
.litertlm
```

Si el servidor devuelve HTML en lugar del archivo, comprobar que se esté utilizando una URL de descarga directa y no una página de visualización.

Si el recurso requiere autenticación, configurar:

```properties
GEMMA_MODEL_TOKEN=
```

---

## Notificación de prueba al iniciar

La versión actual ejecuta una notificación de prueba desde `MainActivity`.

La llamada responsable es:

```kotlin
dispararNotificacionesDePrueba()
```

Para una compilación destinada a distribución debe retirarse esta llamada, conservando únicamente los Workers y notificaciones reales de tareas y conversaciones.

---

# Comandos rápidos

## Windows

```powershell
git clone --branch retos --single-branch https://github.com/afat13/Aprende-Aprender.git
cd Aprende-Aprender

.\gradlew.bat testDebugUnitTest assembleDebug

adb devices
.\gradlew.bat installDebug

adb shell am start -n com.example.aprendeaprender/.MainActivity
```

## Linux/macOS

```bash
git clone --branch retos --single-branch https://github.com/afat13/Aprende-Aprender.git
cd Aprende-Aprender

chmod +x gradlew
./gradlew testDebugUnitTest assembleDebug

adb devices
./gradlew installDebug

adb shell am start -n com.example.aprendeaprender/.MainActivity
```

Si todo es correcto, la compilación debe finalizar con:

```text
BUILD SUCCESSFUL
```

y la aplicación debe iniciar mostrando la pantalla de carga seguida del inicio de sesión o de la pantalla principal cuando ya exista una sesión activa.

## Autores

* Andres Felipe Ardila
* Thomas Huérfano Ramirez
* Valentina Silva Paez

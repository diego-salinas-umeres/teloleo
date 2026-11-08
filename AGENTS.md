# TeLoLeo — estado para agentes

App Android de un solo módulo. Kotlin, Jetpack Compose y Material 3. El alcance cerrado está en `ProductScope` y las pruebas lo fijan. No lo abras.

Versión actual: `versionName` 1.0, `versionCode` 1, `applicationId` `app.teloleo`. `minSdk` 26, `targetSdk` 36, `compileSdk` 37. JDK 17. Gradle 9.6.0, Android Gradle Plugin 9.5.1, Kotlin 2.2.21.

## Qué ya está hecho

La v1 corre en el teléfono. No hay un servidor que completar.

- Primer arranque en cuatro pasos. El ajuste de Android no es un paso de la app: al volver se cae en concedido o denegado.
- Tres pestañas: Inicio, Apps y Lectura. Encima: Privacidad, Pro e idioma de la voz.
- `NotificationSpeakerService` escucha avisos, `NotificationGate` decide si se dicen y `SystemSpeaker` los habla.
- Ajustes en DataStore (`teloleo_settings`). El orden de las apps va en una sola cadena, porque un string set no lo conserva y el cupo gratis depende de ese orden.
- Play Billing consulta y compra `teloleo_pro` en el teléfono. No se llama a `consume`. Pro solo lo escribe el cliente de Billing.
- La ficha de Play está preparada en `play/`. Eso no significa que la ficha ya esté publicada.
- `privacy/index.html` es la política pública. GitHub Pages la publica desde la carpeta `privacy/` con `.github/workflows/pages.yml`. La URL es https://diego-salinas-umeres.github.io/teloleo/ .

## Invariantes

- El texto del aviso no se envía, no se guarda como historial y no se vende. No añadas `INTERNET`, cliente HTTP, Firebase, anuncios ni identificador publicitario.
- Sin Pro hablan como máximo 3 apps, las primeras que se marcaron. Sin límite de lecturas. La cuarta no entra en la lista.
- Horario de silencio, auriculares, plantilla, filtro de palabras, agitar, velocidad y volumen solo aplican con Pro. Ocultar códigos de un solo uso vale también en la versión gratis y viene activado.
- Se ignoran los avisos propios, en curso, resúmenes de grupo, de media y de progreso.
- La divulgación va antes de abrir el ajuste del sistema. El botón dice «Entendido, abrir ajustes».
- La copia de la app, de `privacy/index.html` y de `play/` dice lo mismo: el texto no sale del teléfono y no se recopilan ni se comparten datos.
- `preview` pone `BuildConfig.PRO_PREVIEW` en true. No se sube a Play. `release` ofusca y firma solo si existe `keystore.properties`.

## Dónde tocar

| Cambio | Sitio |
| --- | --- |
| Cupo, producto Pro, banderas de la v1 | `scope/ProductScope.kt`, `scope/AllowedApps.kt` |
| Si un aviso se dice | `listener/NotificationGate.kt`, `listener/ReadPolicy.kt` |
| Cómo suena la frase | `listener/SpokenPhrase.kt`, `listener/PhraseTemplate.kt`, `tts/SpeechText.kt` |
| Servicio, agitar, callar | `listener/NotificationSpeakerService.kt`, `listener/SpeakerCommands.kt` |
| Pantallas y textos | `ui/`, `res/values/strings.xml` |
| Primer arranque | `ui/OnboardingNav.kt`, `ui/OnboardingScreen.kt` |
| Compra | `billing/ProBilling.kt`, `billing/ProEntitlement.kt` |
| Persistencia | `data/SettingsRepository.kt` |
| Lista de apps | `apps/LauncherApps.kt`. Solo apps con icono en el lanzador. No uses `QUERY_ALL_PACKAGES` |

Los prototipos de `design/ui/` no son la interfaz que se instala. Cámbialos solo si el pedido es de diseño.

## Comprobar

Las pruebas de unidad leen archivos con rutas relativas y Gradle fija el directorio de trabajo en `app/`.

```bash
./gradlew :app:testDebugUnitTest
```

`ProductScopeTest` impide red, anuncios y permisos de más. `PlayComplianceCopyTest` ata la divulgación, `strings.xml` y `privacy/index.html`. Si cambias una promesa de privacidad, cámbiala en los tres sitios y en `play/`.

## Texto

La interfaz está en español, de tú, en frases cortas. Los textos nuevos van a `strings.xml`, no incrustados en los composables. No añadas una página de política con URL: `privacy/index.html` no debe contener `http://` ni `https://`.

## Secretos

No versiones `keystore.properties`, `*.jks`, `*.keystore` ni `local.properties`. No copies contraseñas de firma a la documentación ni al código.

# TeLoLeo

TeLoLeo lee en voz alta las notificaciones de las apps que la persona elige. El resto se queda en silencio. El texto se dice con la voz del sistema y no sale del teléfono: no hay servidor, no hay cuenta y no hay anuncios.

Versión de la app: **1.0** (`versionCode` 1). Identificador: `app.teloleo`.

## Qué hace

- Por defecto, silencio. Solo hablan las apps marcadas.
- Un aviso se dice así: aplicación, quién escribe y el texto.
- Un código de un solo uso se dice «oculto». Ese ajuste viene activado.
- La lectura se puede desactivar en Inicio. El permiso de Android sigue concedido hasta que se quite en los ajustes del sistema.
- Antes de abrir el acceso a las notificaciones, la app explica qué se lee.

## Gratis y Pro

La versión gratis lee **3 apps**, sin límite de lecturas. La cuarta no se añade.

Pro es un pago único, el producto `teloleo_pro`. No es una suscripción. Lo cobra Google Play en el teléfono. Desbloquea:

- apps ilimitadas
- horario de silencio
- solo auriculares o Bluetooth
- plantilla de la frase
- filtro de palabras
- agitar para callar
- velocidad y volumen propios

La variante `preview` deja Pro activo sin compra. Sirve para probar en un teléfono. No se sube a Play.

## Compilar

Hace falta Android SDK 37 y JDK 17. La ruta del SDK va en `local.properties`, que no se versiona.

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
./gradlew :app:assemblePreview
./gradlew :app:bundleRelease
```

`bundleRelease` firma el paquete solo si existe `keystore.properties` en la raíz. Sin ese archivo, el release se genera sin esa firma.

## Estructura

| Ruta | Qué es |
| --- | --- |
| `app/src/main/java/app/teloleo/ui` | Pantallas en Compose: primer arranque, Inicio, Apps, Lectura, Privacidad y Pro |
| `app/src/main/java/app/teloleo/listener` | Servicio de notificaciones, qué avisos se dicen y cómo se arma la frase |
| `app/src/main/java/app/teloleo/tts` | Voz del sistema y el texto que se le entrega |
| `app/src/main/java/app/teloleo/data` | Ajustes en el teléfono, con DataStore |
| `app/src/main/java/app/teloleo/billing` | Compra no consumible con Play Billing |
| `app/src/main/java/app/teloleo/scope` | Reglas cerradas de la v1 |
| `app/src/main/java/app/teloleo/apps` | Apps con icono en el lanzador |
| `app/src/test` | Pruebas de unidad de esas reglas |
| `privacy/index.html` | Política de privacidad pública. GitHub Pages la publica y dice lo mismo que la pantalla de la app |
| `play/` | Textos y gráficos preparados para la ficha de Play |
| `design/ui/` | Prototipos HTML. La app que se instala es la de Compose |

## Qué no está en el repositorio

`.gitignore` deja fuera la compilación, `local.properties`, `keystore.properties` y los archivos `*.jks` / `*.keystore`. La clave de firma no se sube.

## Privacidad

TeLoLeo no recopila ni comparte datos. El texto del aviso vive en memoria el tiempo de decirlo y se descarta. En el teléfono se guardan la lista de apps, si Pro está activo y los ajustes de lectura. No hay copia en la nube.

La página pública es https://diego-salinas-umeres.github.io/teloleo/ . Esa es la URL para la ficha de Play. El HTML vive en `privacy/index.html` y no enlaza a otros sitios.

Contacto: teloleoapp@gmail.com

Repositorio: https://github.com/diego-salinas-umeres/teloleo

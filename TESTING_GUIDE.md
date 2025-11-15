# 🧪 Guía de Pruebas - Sistema de Permisos Guardiant

## 📋 Pre-requisitos

Antes de comenzar las pruebas, asegúrate de tener:

- ✅ Android Studio instalado y actualizado
- ✅ Dispositivo Android físico o emulador (API 24+)
- ✅ USB debugging activado (para dispositivo físico)
- ✅ Proyecto sincronizado sin errores de compilación

---

## 🚀 Paso 1: Compilar el Proyecto

### Desde Android Studio:

1. Abre el proyecto `GuardiantApp_Front`
2. Ve a: `Build > Make Project` (Ctrl+F9)
3. Espera a que termine la compilación
4. Verifica que no haya errores en el panel "Build"

### Desde Terminal:

```bash
cd GuardiantApp_Front
./gradlew assembleDebug
```

**Resultado esperado:**
```
BUILD SUCCESSFUL in 30s
```

---

## 📱 Paso 2: Instalar en Dispositivo

### Desde Android Studio:

1. Conecta tu dispositivo o inicia un emulador
2. Ve a: `Run > Run 'app'` (Shift+F10)
3. Selecciona tu dispositivo
4. Espera la instalación

### Desde Terminal:

```bash
./gradlew installDebug
adb shell am start -n com.guardiant.app/.auth.LoginActivity
```

---

## 🧪 Paso 3: Flujo de Prueba Completo

### 3.1 Registro e Inicio de Sesión

1. **Abrir la app**
   - ✅ Debe mostrar `LoginActivity`
   - ✅ Ver campos de email y contraseña

2. **Ir a Registro**
   - ✅ Tocar "¿No tienes cuenta? Regístrate"
   - ✅ Llega a `MainActivity`

3. **Registrarse**
   - ✅ Ingresar email: `test@guardiant.com`
   - ✅ Ingresar password: `Test123456`
   - ✅ Confirmar password: `Test123456`
   - ✅ Tocar "Registrar"
   - ✅ Ver mensaje: "Registro exitoso"

4. **Verificar Teléfono**
   - ✅ Llega a `VerificationActivity`
   - ✅ Ingresar teléfono: `555123456`
   - ✅ Tocar "Enviar Código"
   - ✅ Ver mensaje: "¡Código Mágico 'Enviado'! (Usa 123456)"
   - ✅ Ingresar código: `123456`
   - ✅ Tocar "Verificar Código"
   - ✅ Ver mensaje: "¡Verificación Mágica Exitosa!"

### 3.2 Onboarding de Permisos (¡NUEVO!)

**La app debe navegar automáticamente a `OnboardingActivity`**

#### Paso 1: Device Admin

1. ✅ Ver pantalla con:
   - Icono: 🛡️
   - Título: "Administrador de Dispositivo"
   - Descripción clara
   - Barra de progreso: "Paso 1 de 6" - "0%"
   - Botón: "Activar Administrador"

2. ✅ Tocar "Activar Administrador"

3. ✅ Android abre pantalla de configuración:
   - "Activar administrador de dispositivo"
   - "SecureLock solicita ser administrador..."
   - Lista de permisos que solicita

4. ✅ **MANUALMENTE** tocar "Activar"

5. ✅ Presionar botón "Atrás" del dispositivo

6. ✅ Volver a la app

7. ✅ Ver mensaje: "✅ Guardiant activado como Administrador"

8. ✅ La app verifica automáticamente y pasa al siguiente paso

#### Paso 2: Accessibility Service

1. ✅ Ver pantalla con:
   - Icono: 👁️
   - Título: "Servicio de Accesibilidad"
   - Barra de progreso: "Paso 2 de 6" - "16%"
   - Botón: "Activar Servicio"

2. ✅ Tocar "Activar Servicio"

3. ✅ Android abre: `Configuración > Accesibilidad`

4. ✅ Ir a: "Servicios instalados" o "Servicios descargados"

5. ✅ Buscar "Guardiant" en la lista

6. ✅ Tocar "Guardiant"

7. ✅ Ver diálogo:
   - "Guardiant usa este servicio para..."
   - Toggle para activar

8. ✅ **MANUALMENTE** activar el toggle

9. ✅ Confirmar en el diálogo de advertencia

10. ✅ Presionar "Atrás" hasta volver a la app

11. ✅ La app verifica y pasa al siguiente paso

#### Paso 3: Location (Foreground)

1. ✅ Ver pantalla con:
   - Icono: 📍
   - Título: "Ubicación GPS"
   - Barra de progreso: "Paso 3 de 6" - "33%"
   - Botón: "Permitir Ubicación"

2. ✅ Tocar "Permitir Ubicación"

3. ✅ Android muestra diálogo nativo:
   - "¿Permitir que Guardiant acceda a la ubicación del dispositivo?"
   - Opciones:
     - "Permitir siempre" (Recomendado)
     - "Permitir solo mientras uso la app"
     - "Denegar"

4. ✅ **MANUALMENTE** seleccionar "Permitir siempre"

5. ✅ Ver mensaje: "¡Permiso otorgado! ✅"

6. ✅ La app verifica y pasa al siguiente paso

#### Paso 4: Background Location

1. ✅ Ver pantalla con:
   - Icono: 🌐
   - Título: "Ubicación en Segundo Plano"
   - Barra de progreso: "Paso 4 de 6" - "50%"
   - Botón: "Permitir Siempre"

2. ✅ Tocar "Permitir Siempre"

3. ✅ Si ya seleccionaste "Permitir siempre" antes, este paso se salta automáticamente

4. ✅ La app verifica y pasa al siguiente paso

#### Paso 5: Notifications

1. ✅ Ver pantalla con:
   - Icono: 🔔
   - Título: "Notificaciones Push"
   - Barra de progreso: "Paso 5 de 6" - "66%"
   - Botón: "Permitir Notificaciones"

2. ✅ Tocar "Permitir Notificaciones"

3. ✅ Android muestra diálogo (solo en Android 13+):
   - "¿Permitir que Guardiant envíe notificaciones?"

4. ✅ **MANUALMENTE** tocar "Permitir"

5. ✅ Ver mensaje: "¡Permiso otorgado! ✅"

6. ✅ La app verifica y pasa al siguiente paso

#### Paso 6: Draw Overlay (Opcional)

1. ✅ Ver pantalla con:
   - Icono: 🔝
   - Título: "Mostrar sobre otras apps"
   - Barra de progreso: "Paso 6 de 6" - "83%"
   - Botón: "Permitir Superposición"
   - **Botón adicional: "Saltar (no recomendado)"**

2. ✅ Puedes saltar este permiso (es opcional)

3. ✅ O tocar "Permitir Superposición"

4. ✅ Android abre: `Configuración > Apps > Permisos especiales > Mostrar sobre otras apps`

5. ✅ Buscar "Guardiant"

6. ✅ Activar el toggle

7. ✅ Presionar "Atrás" hasta volver a la app

#### Pantalla de Completado

1. ✅ Ver pantalla final:
   - Icono: 🎉
   - Título: "¡Todo listo!"
   - Mensaje: "Has completado la configuración de Guardiant..."
   - Barra de progreso: "100% completado"
   - Botón: "Comenzar a usar Guardiant"

2. ✅ Tocar "Comenzar a usar Guardiant"

3. ✅ Navega a `SetupPinsActivity`

---

## 🧪 Paso 4: Probar Casos Especiales

### Caso 1: Saltarse Permisos

1. Volver a `OnboardingActivity`
2. Cuando llegues a Device Admin, presionar "Atrás" sin activar
3. La app muestra diálogo: "¿Salir de la configuración?"
4. Tocar "Salir de todos modos"
5. Verificar que se muestra la pantalla de completado con advertencia

### Caso 2: Verificación Manual

1. En cualquier paso, tocar "Ya lo activé, verificar"
2. Si el permiso NO está activado, sigue mostrando el mismo paso
3. Si el permiso SÍ está activado, pasa al siguiente

### Caso 3: Más Información

1. En cualquier paso, tocar "¿Por qué necesitamos esto?"
2. Ver diálogo explicativo detallado
3. Tocar "Entendido" para cerrar

### Caso 4: Re-configurar Permisos

1. Llegar a la pantalla de completado
2. Tocar "Revisar permisos nuevamente"
3. Vuelve al primer permiso faltante

---

## 🔍 Paso 5: Verificar Permisos en Configuración

### Device Admin:

```
Configuración de Android
  → Seguridad
  → Administradores del dispositivo
  → Buscar "Guardiant - Protección del Dispositivo"
  → Debe estar ACTIVADO ✅
```

### Accessibility:

```
Configuración de Android
  → Accesibilidad
  → Servicios instalados (o descargados)
  → Buscar "Guardiant"
  → Debe estar ACTIVADO ✅
```

### Location:

```
Configuración de Android
  → Ubicación
  → Permisos de app
  → Buscar "Guardiant"
  → Debe decir "Permitir siempre" ✅
```

### Notifications:

```
Configuración de Android
  → Apps
  → Guardiant
  → Notificaciones
  → Debe estar ACTIVADO ✅
```

---

## 📊 Paso 6: Verificar Logs

### Desde Android Studio:

1. Ve a: `View > Tool Windows > Logcat`
2. Filtra por: `Guardiant`
3. Deberías ver logs como:

```
D/PermissionManager: Device Admin: true
D/PermissionManager: Accessibility: true
D/Onboarding: Current step: 0
D/Onboarding: Progress: 100%
D/GuardiantDeviceAdmin: Device Admin activado
D/GuardiantAccessibility: Servicio de Accesibilidad conectado
```

### Desde Terminal:

```bash
# Ver todos los logs
adb logcat | grep "Guardiant"

# Solo PermissionManager
adb logcat -s PermissionManager

# Solo Onboarding
adb logcat -s Onboarding
```

---

## 🐛 Solución de Problemas

### Problema 1: "Device Admin no se activa"

**Síntoma:** Después de tocar "Activar", vuelves a la app y sigue mostrando como no activado.

**Solución:**
1. Verifica que tocaste "Activar" (NO "Cancelar")
2. Ve manualmente a Configuración y verifica si está activado
3. Si está activado pero la app no lo detecta, reinicia la app

### Problema 2: "Accessibility no aparece en la lista"

**Síntoma:** En Configuración > Accesibilidad, no ves "Guardiant".

**Solución:**
1. Verifica que el servicio esté declarado en `AndroidManifest.xml`
2. Desinstala y vuelve a instalar la app
3. Verifica que el archivo `accessibility_service_config.xml` exista

### Problema 3: "Location siempre muestra como denegado"

**Síntoma:** Incluso después de permitir, sigue apareciendo como denegado.

**Solución:**
1. Verifica que el GPS esté activado en el dispositivo
2. Ve a Configuración > Apps > Guardiant > Permisos
3. Verifica que "Ubicación" esté en "Permitir siempre"

### Problema 4: "La app crashea al abrir Onboarding"

**Síntoma:** Al llegar a OnboardingActivity, la app se cierra.

**Solución:**
1. Verifica el Logcat para ver el error exacto
2. Posibles causas:
   - Falta el layout `activity_onboarding.xml`
   - Falta algún recurso (color, string, etc.)
   - Error en el ViewBinding

```bash
# Ver el stacktrace completo
adb logcat | grep "AndroidRuntime"
```

### Problema 5: "Botones no responden"

**Síntoma:** Al tocar botones en OnboardingActivity, no pasa nada.

**Solución:**
1. Verifica que los listeners estén configurados en `setupUI()`
2. Revisa si hay errores en Logcat
3. Verifica que el binding esté correctamente inflado

---

## ✅ Checklist de Pruebas

### Funcionalidad Básica
- [ ] La app compila sin errores
- [ ] Se instala correctamente
- [ ] LoginActivity se abre al inicio
- [ ] Registro funciona
- [ ] Verificación de teléfono funciona
- [ ] OnboardingActivity se abre automáticamente

### Permisos Device Admin
- [ ] Pantalla se muestra correctamente
- [ ] Botón "Activar Administrador" funciona
- [ ] Abre Configuración de Android
- [ ] Se puede activar manualmente
- [ ] Al volver, la app detecta el cambio
- [ ] Pasa automáticamente al siguiente paso

### Permisos Accessibility
- [ ] Pantalla se muestra correctamente
- [ ] Botón "Activar Servicio" funciona
- [ ] Abre Configuración de Accesibilidad
- [ ] Se puede activar manualmente
- [ ] Al volver, la app detecta el cambio
- [ ] Pasa automáticamente al siguiente paso

### Permisos Location
- [ ] Pantalla se muestra correctamente
- [ ] Botón "Permitir Ubicación" funciona
- [ ] Muestra diálogo nativo
- [ ] Se puede permitir
- [ ] La app detecta el cambio
- [ ] Pasa automáticamente al siguiente paso

### Permisos Background Location
- [ ] Pantalla se muestra correctamente
- [ ] Botón funciona
- [ ] Se solicita correctamente (si aplica)
- [ ] Pasa automáticamente al siguiente paso

### Permisos Notifications
- [ ] Pantalla se muestra correctamente
- [ ] Botón "Permitir Notificaciones" funciona
- [ ] Muestra diálogo nativo (Android 13+)
- [ ] Se puede permitir
- [ ] Pasa automáticamente al siguiente paso

### Permisos Draw Overlay
- [ ] Pantalla se muestra correctamente
- [ ] Botón "Saltar" está visible
- [ ] Se puede saltar
- [ ] O se puede activar manualmente

### Pantalla de Completado
- [ ] Se muestra al terminar todos los permisos
- [ ] Muestra el porcentaje correcto (100% o menor)
- [ ] Botón "Continuar" funciona
- [ ] Navega a SetupPinsActivity
- [ ] Botón "Revisar permisos" funciona

### UI/UX
- [ ] Barra de progreso se actualiza correctamente
- [ ] Iconos emoji se muestran correctamente
- [ ] Textos son legibles
- [ ] Colores son apropiados
- [ ] No hay texto cortado
- [ ] Scroll funciona si el contenido es largo

### Casos Especiales
- [ ] Botón "Atrás" muestra confirmación
- [ ] "Más información" muestra diálogo
- [ ] "Ya lo activé, verificar" funciona
- [ ] Re-configurar permisos funciona
- [ ] Logs aparecen en Logcat

---

## 📸 Capturas de Pantalla Recomendadas

Para documentación, toma capturas de:

1. Cada paso del onboarding (6 pantallas)
2. Pantalla de completado (éxito y parcial)
3. Diálogos de confirmación
4. Configuración de Android mostrando permisos activados
5. Widget de permisos en SettingsFragment (si lo implementaste)

---

## 🚀 Siguientes Pasos

Una vez que todas las pruebas pasen:

1. [ ] Implementar widget en SettingsFragment
2. [ ] Agregar verificación en HomeActivity.onResume()
3. [ ] Implementar recordatorios periódicos
4. [ ] Agregar analytics de Firebase
5. [ ] Optimizar animaciones
6. [ ] Crear video tutorial
7. [ ] Probar en diferentes versiones de Android
8. [ ] Optimizar para tablets

---

## 📞 Soporte

Si encuentras algún problema durante las pruebas:

1. Verifica el Logcat completo
2. Revisa el archivo `PERMISSIONS_README.md`
3. Consulta `PERMISSIONS_EXAMPLES.md` para ejemplos de código
4. Verifica que todos los archivos estén presentes

---

**¡Buena suerte con las pruebas! 🎉**

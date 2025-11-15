package com.guardiant.app.permissions

/**
 * Representa un permiso individual en el flujo de onboarding
 */
data class PermissionItem(
    val id: String,
    val title: String,
    val description: String,
    val longDescription: String,
    val icon: String, // Emoji o identificador de recurso
    val whyNeeded: String,
    val howToGrant: List<String>,
    val isCritical: Boolean = true,
    val order: Int = 0
) {
    companion object {
        /**
         * Lista ordenada de todos los permisos que necesita Guardiant
         */
        fun getAllPermissions(): List<PermissionItem> {
            return listOf(
                // 1. Device Admin (MÁS CRÍTICO)
                PermissionItem(
                    id = "device_admin",
                    title = "Administrador de Dispositivo",
                    description = "Protege tu dispositivo en caso de robo o pérdida",
                    longDescription = "Este permiso permite a Guardiant bloquear tu dispositivo " +
                            "remotamente, cambiar el PIN de seguridad y borrar datos sensibles " +
                            "para proteger tu información personal.",
                    icon = "🛡️",
                    whyNeeded = "Sin este permiso, Guardiant NO puede proteger tu dispositivo " +
                            "en caso de robo. Es el permiso más importante de la aplicación.",
                    howToGrant = listOf(
                        "Toca el botón 'Activar Permiso'",
                        "Android abrirá Configuración del Sistema",
                        "Busca 'Guardiant' en la lista",
                        "Toca el botón 'Activar'",
                        "Presiona el botón 'Atrás' para volver"
                    ),
                    isCritical = true,
                    order = 1
                ),

                // 2. Accessibility Service
                PermissionItem(
                    id = "accessibility",
                    title = "Servicio de Accesibilidad",
                    description = "Monitorea actividad sospechosa en tu dispositivo",
                    longDescription = "Permite a Guardiant detectar cuando alguien intenta " +
                            "desinstalar la app, cambiar configuraciones de seguridad, o " +
                            "acceder a apps protegidas sin autorización.",
                    icon = "👁️",
                    whyNeeded = "Este servicio detecta comportamiento sospechoso en tiempo real " +
                            "y te alerta inmediatamente si alguien está usando tu dispositivo " +
                            "sin permiso.",
                    howToGrant = listOf(
                        "Toca 'Activar Servicio'",
                        "Ve a 'Servicios instalados'",
                        "Busca 'Guardiant' en la lista",
                        "Toca 'Guardiant'",
                        "Activa el interruptor",
                        "Confirma en el diálogo que aparece",
                        "Presiona 'Atrás' para volver"
                    ),
                    isCritical = true,
                    order = 2
                ),

                // 3. Location (Foreground)
                PermissionItem(
                    id = "location",
                    title = "Ubicación GPS",
                    description = "Rastrea la ubicación de tu dispositivo",
                    longDescription = "Guardiant puede rastrear la ubicación de tu dispositivo " +
                            "en tiempo real si es robado o perdido, ayudándote a recuperarlo.",
                    icon = "📍",
                    whyNeeded = "La ubicación GPS es esencial para rastrear tu dispositivo " +
                            "en caso de robo. Podrás ver en un mapa dónde se encuentra.",
                    howToGrant = listOf(
                        "Toca 'Permitir Ubicación'",
                        "En el diálogo que aparece, selecciona:",
                        "'Permitir siempre' o 'Permitir mientras se usa la app'",
                        "Recomendamos 'Permitir siempre' para máxima protección"
                    ),
                    isCritical = true,
                    order = 3
                ),

                // 4. Background Location
                PermissionItem(
                    id = "background_location",
                    title = "Ubicación en Segundo Plano",
                    description = "Rastrea ubicación incluso cuando la app está cerrada",
                    longDescription = "Permite que Guardiant rastree la ubicación de tu " +
                            "dispositivo incluso cuando la aplicación no está abierta, " +
                            "proporcionando protección 24/7.",
                    icon = "🌐",
                    whyNeeded = "Si tu dispositivo es robado, el ladrón probablemente cerrará " +
                            "todas las apps. Este permiso permite rastrear la ubicación de " +
                            "todos modos.",
                    howToGrant = listOf(
                        "Toca 'Activar Ubicación Continua'",
                        "En el diálogo, selecciona:",
                        "'Permitir siempre'",
                        "NO selecciones 'Permitir solo mientras uso la app'"
                    ),
                    isCritical = true,
                    order = 4
                ),

                // 5. Notifications
                PermissionItem(
                    id = "notifications",
                    title = "Notificaciones Push",
                    description = "Recibe alertas de seguridad instantáneas",
                    longDescription = "Guardiant te enviará notificaciones inmediatas si " +
                            "detecta actividad sospechosa, intentos de desbloqueo fallidos, " +
                            "o si tu dispositivo se mueve a una ubicación no autorizada.",
                    icon = "🔔",
                    whyNeeded = "Las notificaciones te alertan instantáneamente de cualquier " +
                            "amenaza a tu dispositivo, permitiéndote tomar acción rápida.",
                    howToGrant = listOf(
                        "Toca 'Permitir Notificaciones'",
                        "En el diálogo que aparece, toca 'Permitir'",
                        "¡Listo! Ya recibirás alertas de seguridad"
                    ),
                    isCritical = true,
                    order = 5
                ),

                // 6. Draw Overlay (OPCIONAL - Mejora UX)
                PermissionItem(
                    id = "draw_overlay",
                    title = "Mostrar sobre otras apps",
                    description = "Muestra alertas de seguridad prioritarias",
                    longDescription = "Permite que Guardiant muestre alertas de seguridad " +
                            "sobre otras aplicaciones cuando detecta actividad sospechosa, " +
                            "asegurando que veas las advertencias importantes.",
                    icon = "🔝",
                    whyNeeded = "Mejora la visibilidad de alertas críticas, especialmente " +
                            "si estás usando otra aplicación cuando ocurre un evento de seguridad.",
                    howToGrant = listOf(
                        "Toca 'Permitir Superposición'",
                        "Busca 'Guardiant' en la lista",
                        "Activa el interruptor",
                        "Presiona 'Atrás' para volver"
                    ),
                    isCritical = false,
                    order = 6
                )
            )
        }

        /**
         * Obtiene solo los permisos críticos
         */
        fun getCriticalPermissions(): List<PermissionItem> {
            return getAllPermissions().filter { it.isCritical }
        }

        /**
         * Obtiene un permiso por ID
         */
        fun getPermissionById(id: String): PermissionItem? {
            return getAllPermissions().find { it.id == id }
        }
    }
}

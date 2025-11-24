package com.example.spottivo.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Servicio para subir imágenes a Cloudinary
 * Alternativa a Firebase Storage con transformaciones automáticas
 */
class CloudinaryService {
    
    companion object {
        private const val TAG = "CloudinaryService"
        private var isInitialized = false
        
        // ⚠️ CREDENCIALES DE CLOUDINARY CONFIGURADAS
        private const val CLOUD_NAME = "due94e56i"
        private const val API_KEY = "817672293674457"
        private const val API_SECRET = "FR3fpAj-unDfqcYrZKZ5kiREaf0"
        
        /**
         * Inicializar Cloudinary (llamar solo una vez al inicio de la app)
         */
        fun initialize(context: Context) {
            if (isInitialized) return
            
            try {
                val config = HashMap<String, String>()
                config["cloud_name"] = CLOUD_NAME
                config["api_key"] = API_KEY
                config["api_secret"] = API_SECRET
                
                MediaManager.init(context, config)
                isInitialized = true
                Log.d(TAG, "Cloudinary inicializado correctamente")
            } catch (e: Exception) {
                Log.e(TAG, "Error inicializando Cloudinary", e)
            }
        }
    }
    
    /**
     * Subir imagen a Cloudinary
     * @param imageUri URI de la imagen local
     * @param folder Carpeta en Cloudinary (ej: "spottivo/profiles")
     * @param publicId ID público de la imagen (opcional, se genera automático)
     * @return URL de la imagen subida
     */
    suspend fun uploadImage(
        imageUri: Uri,
        folder: String = "spottivo/profiles",
        publicId: String? = null
    ): String = suspendCancellableCoroutine { continuation ->
        
        if (!isInitialized) {
            continuation.resumeWithException(Exception("Cloudinary no está inicializado"))
            return@suspendCancellableCoroutine
        }
        
        try {
            val requestId = MediaManager.get().upload(imageUri)
                .option("folder", folder)
                .apply {
                    if (publicId != null) {
                        option("public_id", publicId)
                    }
                }
                // Transformaciones automáticas (como parámetros individuales)
                .option("transformation", "w_500,h_500,c_fill,g_face,q_auto,f_auto")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        Log.d(TAG, "Subida iniciada: $requestId")
                    }
                    
                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        val progress = (bytes.toDouble() / totalBytes * 100).toInt()
                        Log.d(TAG, "Progreso: $progress%")
                    }
                    
                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val secureUrl = resultData["secure_url"] as? String
                        if (secureUrl != null) {
                            Log.d(TAG, "Imagen subida exitosamente: $secureUrl")
                            continuation.resume(secureUrl)
                        } else {
                            continuation.resumeWithException(Exception("URL no encontrada en respuesta"))
                        }
                    }
                    
                    override fun onError(requestId: String, error: ErrorInfo) {
                        Log.e(TAG, "Error subiendo imagen: ${error.description}")
                        continuation.resumeWithException(Exception(error.description))
                    }
                    
                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        Log.w(TAG, "Subida reagendada: ${error.description}")
                    }
                })
                .dispatch()
            
            Log.d(TAG, "Request ID: $requestId")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al iniciar subida", e)
            continuation.resumeWithException(e)
        }
    }
    
    /**
     * Generar URL de transformación de Cloudinary
     * Útil para obtener diferentes tamaños de la misma imagen
     */
    fun getTransformedUrl(
        originalUrl: String,
        width: Int = 200,
        height: Int = 200,
        crop: String = "fill"
    ): String {
        // Ejemplo de URL transformada
        // https://res.cloudinary.com/demo/image/upload/w_200,h_200,c_fill/sample.jpg
        return originalUrl.replace(
            "/upload/",
            "/upload/w_$width,h_$height,c_$crop/"
        )
    }
    
    /**
     * Eliminar imagen de Cloudinary
     */
    suspend fun deleteImage(publicId: String): Boolean {
        // Nota: Para eliminar imágenes necesitas usar la API REST de Cloudinary
        // o implementar un Cloud Function en el backend
        Log.w(TAG, "Eliminación de imágenes requiere implementación en backend")
        return false
    }
}

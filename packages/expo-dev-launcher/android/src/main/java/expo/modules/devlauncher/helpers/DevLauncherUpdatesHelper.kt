package expo.modules.devlauncher.helpers

import android.content.Context
import android.net.Uri
import expo.modules.updatesinterface.UpdatesInterface
import org.json.JSONObject
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun UpdatesInterface.loadUpdate(
  configuration: HashMap<String, Any>,
  context: Context,
  shouldContinue: (manifest: JSONObject) -> Boolean
): UpdatesInterface.Update =
  suspendCoroutine { cont ->
    this.fetchUpdateWithConfiguration(configuration, context, object : UpdatesInterface.UpdateCallback {
      override fun onSuccess(update: UpdatesInterface.Update) = cont.resume(update)
      override fun onFailure(e: Exception?) {
        cont.resumeWithException(e ?: Exception("There was an unexpected error loading the update."))
      }
      override fun onProgress(successfulAssetCount: Int, failedAssetCount: Int, totalAssetCount: Int) = Unit
      override fun onManifestLoaded(manifest: JSONObject): Boolean {
        return if (shouldContinue(manifest)) {
          true
        } else {
          cont.resume(object : UpdatesInterface.Update {
            override fun getLaunchAssetPath(): String {
              throw Exception("Tried to access launch asset path for a manifest that was not loaded")
            }
            override fun getManifest(): JSONObject = manifest
          })
          false
        }
      }
    })
  }

fun createUpdatesConfigurationWithUrl(url: Uri): HashMap<String, Any> {
  return hashMapOf(
    "updateUrl" to url,
    "hasEmbeddedUpdate" to false,
    "launchWaitMs" to 60000,
    "checkOnLaunch" to "ALWAYS",
    "enabled" to true
  )
}

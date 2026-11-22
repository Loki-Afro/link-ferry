package com.github.lokiafro.browserredirector

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri

internal class Browsers(private val context: Context) {
    data class Choice(val packageName: String, val name: String)

    private val preferences = context.getSharedPreferences("browser", Context.MODE_PRIVATE)

    var preferredPackage: String?
        get() = preferences.getString("package", null)
        set(value) {
            preferences.edit().putString("package", value).apply()
        }

    @Suppress("DEPRECATION")
    fun installed(): List<Choice> {
        // Android can limit a web query to the current default browser without MATCH_ALL.
        val flags = PackageManager.MATCH_ALL or PackageManager.MATCH_DEFAULT_ONLY or
            PackageManager.GET_RESOLVED_FILTER
        fun genericHandlers(scheme: String): Map<String, ResolveInfo> =
            context.packageManager.queryIntentActivities(
                viewIntent(Uri.parse("$scheme://browser-redirector.invalid/")),
                flags
            ).filter {
                it.activityInfo.exported &&
                    it.activityInfo.packageName != context.packageName &&
                    it.filter?.countDataAuthorities() == 0
            }.associateBy { it.activityInfo.packageName }

        val httpPackages = genericHandlers("http").keys
        return genericHandlers("https")
            .filterKeys { it in httpPackages }
            .map { (name, info) ->
                Choice(name, info.activityInfo.applicationInfo.loadLabel(context.packageManager).toString())
            }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { choice: Choice -> choice.name }
                .thenBy { it.packageName })
    }

    fun open(uri: Uri, browser: Choice): Boolean {
        if (browser.packageName == context.packageName) return false

        val outgoing = viewIntent(uri).apply {
            setPackage(browser.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(outgoing)
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: SecurityException) {
            false
        }
    }

    private fun viewIntent(uri: Uri) =
        Intent(Intent.ACTION_VIEW, uri).addCategory(Intent.CATEGORY_BROWSABLE)
}

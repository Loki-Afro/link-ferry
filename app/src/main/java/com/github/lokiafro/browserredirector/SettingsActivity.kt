package com.github.lokiafro.browserredirector

import android.app.Activity
import android.app.AlertDialog
import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowInsets
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class SettingsActivity : Activity() {
    private val browsers by lazy { Browsers(this) }
    private val roleManager by lazy { getSystemService(RoleManager::class.java) }
    private lateinit var preferredStatus: TextView
    private lateinit var defaultStatus: TextView
    private lateinit var defaultButton: Button
    private lateinit var setupHint: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(24), dp(24), dp(32))
        }
        val scroll = ScrollView(this).apply {
            addView(content)
            setOnApplyWindowInsetsListener { view, insets ->
                val margins = if (Build.VERSION.SDK_INT >= 30) {
                    insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
                } else {
                    @Suppress("DEPRECATION")
                    insets.systemWindowInsets
                }
                view.setPadding(margins.left, margins.top, margins.right, margins.bottom)
                insets
            }
        }
        setContentView(scroll)

        content.addText(R.string.app_name, 28).isAccessibilityHeading = true
        content.addText(R.string.description, 16, 8)
        content.addText(R.string.preferred_browser, 18, 24).isAccessibilityHeading = true
        preferredStatus = content.addText(R.string.no_preference, 16, 4)
        content.addButton(R.string.choose_browser) { chooseBrowser() }

        content.addText(R.string.default_browser, 18, 24).isAccessibilityHeading = true
        defaultStatus = content.addText(R.string.default_inactive, 16, 4)
        defaultButton = content.addButton(R.string.make_default) { requestDefaultBrowser() }
        setupHint = content.addText(R.string.enable_after_selection, 14, 4)
        setupHint.accessibilityLiveRegion = TextView.ACCESSIBILITY_LIVE_REGION_POLITE
        content.addText(R.string.support_note, 14, 24)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val preferredPackage = browsers.preferredPackage
        val selected = browsers.installed().firstOrNull { it.packageName == preferredPackage }
        preferredStatus.text = selected?.name ?: getString(
            if (preferredPackage == null) R.string.no_preference else R.string.browser_unavailable
        )

        val available = roleManager.isRoleAvailable(RoleManager.ROLE_BROWSER)
        val held = available && roleManager.isRoleHeld(RoleManager.ROLE_BROWSER)
        defaultStatus.setText(when {
            held -> R.string.default_active
            available -> R.string.default_inactive
            else -> R.string.default_unavailable
        })
        defaultButton.setText(if (held || !available) R.string.open_defaults else R.string.make_default)
        defaultButton.isEnabled = held || !available || selected != null
        setupHint.visibility = if (defaultButton.isEnabled) TextView.GONE else TextView.VISIBLE
    }

    private fun chooseBrowser() {
        val choices = browsers.installed()
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.choose_browser)
            .setNegativeButton(android.R.string.cancel, null)

        if (choices.isEmpty()) {
            dialog.setMessage(R.string.no_browsers)
        } else {
            val checked = choices.indexOfFirst { it.packageName == browsers.preferredPackage }
            dialog.setSingleChoiceItems(choices.map { "${it.name} (${it.packageName})" }.toTypedArray(), checked) { picker, index ->
                browsers.preferredPackage = choices[index].packageName
                picker.dismiss()
                refresh()
            }
        }
        dialog.show()
    }

    @Suppress("DEPRECATION")
    private fun requestDefaultBrowser() {
        val role = RoleManager.ROLE_BROWSER
        if (roleManager.isRoleAvailable(role) && !roleManager.isRoleHeld(role)) {
            try {
                startActivityForResult(roleManager.createRequestRoleIntent(role), 1)
                return
            } catch (_: ActivityNotFoundException) {
                // Some device builds expose default apps settings instead.
            }
        }

        try {
            startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, R.string.cannot_open_settings, Toast.LENGTH_SHORT).show()
        }
    }

    private fun LinearLayout.addText(label: Int, size: Int, top: Int = 0): TextView =
        TextView(context).apply {
            setText(label)
            textSize = size.toFloat()
            addView(this, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(top) })
        }

    private fun LinearLayout.addButton(label: Int, onClick: () -> Unit): Button =
        Button(context).apply {
            setText(label)
            setOnClickListener { onClick() }
            addView(this, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(6) })
        }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density + 0.5f).toInt()
}

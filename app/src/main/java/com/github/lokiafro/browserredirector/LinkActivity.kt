package com.github.lokiafro.browserredirector

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast

class LinkActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent.data
        if (intent.action != Intent.ACTION_VIEW || uri == null || !WebLinks.isSupported(uri.toString())) {
            Toast.makeText(this, R.string.invalid_link, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val browsers = Browsers(this)
        val installed = browsers.installed()
        val preferred = installed.firstOrNull { it.packageName == browsers.preferredPackage }

        if (savedInstanceState == null && preferred != null && browsers.open(uri, preferred)) {
            finish()
            return
        }

        // Keep chooser launches package-scoped as well, so Android cannot send them back here.
        val choices = installed.filter { it != preferred }
        val dialog = AlertDialog.Builder(this)
            .setNegativeButton(android.R.string.cancel) { _, _ -> finish() }
            .setOnCancelListener { finish() }

        if (choices.isEmpty()) {
            dialog.setTitle(R.string.cannot_open)
                .setMessage(if (installed.isEmpty()) R.string.no_browsers else R.string.browser_unavailable)
                .setPositiveButton(R.string.open_settings) { _, _ ->
                    startActivity(Intent(this, SettingsActivity::class.java))
                    finish()
                }
        } else {
            dialog.setTitle(R.string.open_once)
                .setItems(choices.map { "${it.name} (${it.packageName})" }.toTypedArray()) { _, index ->
                    if (!browsers.open(uri, choices[index])) {
                        Toast.makeText(this, R.string.cannot_open, Toast.LENGTH_SHORT).show()
                    }
                    finish()
                }
        }
        dialog.show()
    }
}

package com.dhl.base

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.dhl.base.utils.SystemUtil

abstract class BaseActivity : AppCompatActivity() {

    private var loadingDialog: LoadingDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT), navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        }
        super.onCreate(savedInstanceState)
    }

    fun setStatusBarLight(light: Boolean) {
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = light
    }

    protected fun setSystemBarWindowInsetsListener(view: View, callback: (Insets) -> WindowInsetsCompat) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            callback.invoke(systemBars)
        }
    }

    internal fun showLoading(
        hint: String?,
        delay: Long,
        cancelAction: (() -> Unit)?,
    ) {
        AppExecutors.runOnMain {
            if (!SystemUtil.isActivityValid(this)) return@runOnMain
            val dialog = loadingDialog ?: LoadingDialog(this@BaseActivity).apply {
                loadingDialog = this
                setOnDismissListener { loadingDialog = null }
            }
            dialog.showLoading(hint, delay, cancelAction)
        }
    }

    fun hideLoading() {
        AppExecutors.runOnMain {
            loadingDialog?.dismissLoading()
        }
    }

}
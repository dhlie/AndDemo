package com.dhl.base.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.dhl.base.ContextHolder

/**
 *
 * Author: duanhl
 * Create: 5/15/21 8:52 AM
 * Description:
 *
 */
class PermissionHelper(private val activity: FragmentActivity) {

    private lateinit var perms: List<String>

    private var onRationale: ((perms: List<String>, consumer: RationaleConsumer) -> Unit)? = null

    private var onGranted: (() -> Unit)? = null

    private var onDenied: ((perms: List<String>?, noAskAgainPerms: List<String>?) -> Unit)? = null

    private var intent: Intent? = null

    private var intentCallback: (() -> Unit)? = null

    private var rationaleConsumer: RationaleConsumer? = null

    fun permission(vararg perms: String): PermissionHelper {
        val requiredPerms = ArrayList<String>(perms.size)
        perms.forEach { perm ->
            if (ContextCompat.checkSelfPermission(
                    activity,
                    perm
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requiredPerms.add(perm)
            }
        }
        this.perms = requiredPerms
        return this
    }

    /**
     * 用户拒绝过权限, 向用户解释为什么需要相关权限
     * @param perms 拒绝过的权限
     */
    fun rationale(onRationale: (perms: List<String>, consumer: RationaleConsumer) -> Unit): PermissionHelper {
        this.onRationale = onRationale
        rationaleConsumer = RationaleConsumer()
        return this
    }

    /**
     * 申请的所有权限都被授权了
     */
    fun onGranted(callback: () -> Unit): PermissionHelper {
        onGranted = callback
        return this
    }

    /**
     * 被拒绝授权的权限
     * @param deniedPerms 拒绝的权限
     * @param noAskAgainPerms 不再询问的权限,需要到设置中打开
     */
    fun onDenied(callback: (deniedPerms: List<String>?, noAskAgainPerms: List<String>?) -> Unit): PermissionHelper {
        onDenied = callback
        return this
    }

    /**
     * 特殊权限 intent, 已有权限时返回 null(不会打开设置页面,直接回调 onIntentResult)
     * @param block: 返回设置页面 intent, 让用户到设置中开启权限, 如果已有权限返回null(直接回调callback)
     * @param callback: 从设置页面返回后的回调
     */
    fun intent(block: () -> Intent?, callback: () -> Unit): PermissionHelper {
        val intent = block.invoke()
        this.intent = intent ?: Intent(INTENT_ACTION_PERMISSION_GRANTED)
        intentCallback = callback
        return this
    }

    /**
     * 开始处理权限请求, 特殊权限和普通权限需要分开请求
     */
    fun start() {
        // 请求特殊权限
        if (intent != null) {
            if (intent?.action == INTENT_ACTION_PERMISSION_GRANTED) {
                intentCallback?.invoke()
            } else {
                request()
            }
            return
        }

        // 请求普通权限
        if (!::perms.isInitialized) {
            throw RuntimeException("please invoke permission(...) declare required permissions")
        }

        if (perms.isEmpty()) {
            log { "onAllGranted" }
            onGranted?.invoke()
            return
        }

        if (onRationale != null) {
            rationaleConsumer!!.proceed()
        } else {
            request()
        }
    }

    private fun request() {
        val permissionFragment =
            PermissionHelperFragment().apply { permissionHelper = this@PermissionHelper }
        activity.supportFragmentManager
            .beginTransaction()
            .add(permissionFragment, "PermissionHelperFragment")
            .commitAllowingStateLoss()
    }

    inner class RationaleConsumer {

        fun deny() {}

        fun accept() {
            request()
        }

        internal fun proceed() {

            var shouldRationalePerms: ArrayList<String>? = null
            for (perm in perms) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, perm)) {
                    if (shouldRationalePerms == null) {
                        shouldRationalePerms = ArrayList(perms.size)
                    }
                    shouldRationalePerms.add(perm)
                }
            }

            if (shouldRationalePerms.isNullOrEmpty()) {
                request()
            } else {
                onRationale!!.invoke(shouldRationalePerms, this)

                log { "onRationale${shouldRationalePerms.toListString()}" }
            }
        }
    }

    class PermissionHelperFragment : Fragment() {

        lateinit var permissionHelper: PermissionHelper

        private fun removeFragment() {
            val activity = activity
            if (!isAdded || !SystemUtil.isActivityValid(activity)) {
                return
            }
            activity!!.supportFragmentManager
                .beginTransaction()
                .remove(this)
                .commitAllowingStateLoss()
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            log { "onCreate" }

            if (!::permissionHelper.isInitialized) {
                removeFragment()
                return
            }

            if (permissionHelper.intent != null) {
                startActivityForResult(permissionHelper.intent!!, INTENT_REQUEST_CODE)
            } else {
                requestPermissions(permissionHelper.perms.toTypedArray(), PERM_REQUEST_CODE)
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            log { "onDestroy" }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == INTENT_REQUEST_CODE) {
                removeFragment()
                permissionHelper.intentCallback?.invoke()
            }
        }

        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray,
        ) {
            if (requestCode == PERM_REQUEST_CODE) {
                removeFragment()
                if (grantResults.isEmpty()) {
                    return
                }

                var deniedPerms: ArrayList<String>? = null
                var noAskAgainPerms: ArrayList<String>? = null
                for (i in grantResults.indices) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        if (!shouldShowRequestPermissionRationale(permissions[i])) {
                            if (noAskAgainPerms == null) {
                                noAskAgainPerms = ArrayList(grantResults.size)
                            }
                            noAskAgainPerms.add(permissions[i])
                        } else {
                            if (deniedPerms == null) {
                                deniedPerms = ArrayList(grantResults.size)
                            }
                            deniedPerms.add(permissions[i])
                        }
                    }
                }

                if (deniedPerms.isNullOrEmpty() && noAskAgainPerms.isNullOrEmpty()) {
                    permissionHelper.onGranted?.invoke()
                    log { "onAllGranted" }
                } else {
                    permissionHelper.onDenied?.invoke(deniedPerms, noAskAgainPerms)
                    log { "onDenied:${deniedPerms?.toListString()}, noAskAgain:${noAskAgainPerms?.toListString()}" }
                }
            }
        }
    }

    companion object {
        private const val PERM_REQUEST_CODE = 0x41F8
        private const val INTENT_REQUEST_CODE = 0x41F9
        private const val INTENT_ACTION_PERMISSION_GRANTED = "perm.action.GRANTED"

        fun with(activity: FragmentActivity): PermissionHelper {
            return PermissionHelper(activity)
        }

        fun hasPermission(perm: String): Boolean = ContextCompat.checkSelfPermission(
            ContextHolder.appContext,
            perm
        ) == PackageManager.PERMISSION_GRANTED

        fun toAppSetting(context: Context) {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:${context.packageName}")
            )
            context.startActivity(intent)
        }

        private fun Iterable<String>.toListString(): String {
            val builder = StringBuilder("[")
            for (t in this) {
                builder.append(t).append(",")
            }
            builder.deleteCharAt(builder.length - 1)
            builder.append("]")
            return builder.toString()
        }

        private fun log(tag: String = "PermissionHelper", block: (() -> String?)?) {
            Log.i(tag, block?.invoke() ?: "")
        }
    }

}
package com.app.biometric

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.app.KeyguardManager
import android.content.Intent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import android.os.Build
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var executor: Executor
    private lateinit var callBack: BiometricPrompt.AuthenticationCallback
    private var keyguardManager: KeyguardManager? = null
    val RC_BIOMETRICS_ENROLL = 10
    val RC_DEVICE_CREDENTIAL_ENROLL = 18
    var APP_NAME = "your app name here"


    private var title = "Unlock $APP_NAME"
    private val description = "Unlock your screen with PIN, pattern, password,face or fingerprint"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        APP_NAME = applicationInfo.loadLabel(packageManager).toString()
        title = "Unlock $APP_NAME"

        init()
    }

    private fun init() {

        checkDeviceCanAuthenticateWithBiometrics()
        bioAuthInit()

        findViewById<Button>(R.id.btClick).setOnClickListener {
            authenticateWithBiometrics()
        }
    }


    private fun checkDeviceCanAuthenticateWithBiometrics() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {

            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Toast.makeText(this, "Device not support Biometrics", Toast.LENGTH_SHORT).show()
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Toast.makeText(this, "No Hardware available", Toast.LENGTH_SHORT).show()
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                checkAPILevelAndProceed()
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                Toast.makeText(
                    this,
                    "A security vulnerability has been discovered with one or more hardware sensors." +
                            " The affected sensor(s) are unavailable until a security update has addressed the issue.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                Toast.makeText(this, "Unknown error.", Toast.LENGTH_SHORT).show()
            }
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                Toast.makeText(this, "Unknown error.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bioAuthInit() {
        executor = ContextCompat.getMainExecutor(this)
        callBack = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(this@MainActivity, "Unknown error.", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                val intent = Intent(this@MainActivity, SecondActivity::class.java)
                startActivity(intent)
                finish()
                Toast.makeText(this@MainActivity, "Authentication Success", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(this@MainActivity, getErrorMessage(errorCode), Toast.LENGTH_SHORT)
                    .show()

            }
        }
        biometricPrompt = BiometricPrompt(this, executor, callBack)
    }

    private fun authenticateWithBiometrics() {
        val promptInfo = BiometricPrompt.PromptInfo.Builder().apply {
            setTitle(title)
            setDescription(description)
            setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        }.build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager?.let { manager ->
                if (manager.isKeyguardSecure) {
                    biometricPrompt.authenticate(promptInfo)
                } else {
                    startActivityForResult(
                        setUpDeviceLockInAPIBelow23Intent(),
                        RC_DEVICE_CREDENTIAL_ENROLL
                    )
                }
            }
        } else {
            biometricPrompt.authenticate(promptInfo)
        }
    }

    private fun checkAPILevelAndProceed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            startActivityForResult(setUpDeviceLockInAPIBelow23Intent(), RC_DEVICE_CREDENTIAL_ENROLL)
        } else {
            startActivityForResult(biometricsEnrollIntent(), RC_BIOMETRICS_ENROLL)
        }
    }

    private fun setUpDeviceLockInAPIBelow23Intent(): Intent {
        return Intent(Settings.ACTION_SECURITY_SETTINGS)
    }

    private fun biometricsEnrollIntent(): Intent {
        return Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
            putExtra(
                Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
        }
    }


    private fun getErrorMessage(errorCode: Int): String {
        return when (errorCode) {
            BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                getString(R.string.message_user_app_authentication)
            }
            BiometricPrompt.ERROR_HW_UNAVAILABLE -> {
                getString(R.string.error_hw_unavailable)
            }
            BiometricPrompt.ERROR_UNABLE_TO_PROCESS -> {
                getString(R.string.error_unable_to_process)
            }
            BiometricPrompt.ERROR_TIMEOUT -> {
                getString(R.string.error_time_out)
            }
            BiometricPrompt.ERROR_NO_SPACE -> {
                getString(R.string.error_no_space)
            }
            BiometricPrompt.ERROR_CANCELED -> {
                getString(R.string.error_canceled)
            }
            BiometricPrompt.ERROR_LOCKOUT -> {
                getString(R.string.error_lockout)
            }
            BiometricPrompt.ERROR_VENDOR -> {
                getString(R.string.error_vendor)
            }
            BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                getString(R.string.error_lockout_permanent)
            }
            BiometricPrompt.ERROR_USER_CANCELED -> {
                getString(R.string.error_user_canceled)
            }
            BiometricPrompt.ERROR_NO_BIOMETRICS -> {
                checkAPILevelAndProceed()
                getString(R.string.error_no_biometrics)
            }
            BiometricPrompt.ERROR_HW_NOT_PRESENT -> {
                getString(R.string.error_hw_not_present)
            }
            BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> {
                startActivityForResult(biometricsEnrollIntent(), RC_BIOMETRICS_ENROLL)
                getString(R.string.error_no_device_credentials)
            }
            BiometricPrompt.ERROR_SECURITY_UPDATE_REQUIRED -> {
                getString(R.string.error_security_update_required)
            }
            else -> {
                getString(R.string.error_unknown)
            }
        }
    }

}

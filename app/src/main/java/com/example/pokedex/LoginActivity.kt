package com.example.pokedex

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.gms.common.SignInButton
import com.google.firebase.auth.FirebaseAuth
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton

class LoginActivity : AppCompatActivity() {
    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var btnLoginNormal: Button
    private lateinit var btnLogin: SignInButton
    private lateinit var btnLoginFacebook: LoginButton
    private lateinit var auth: FirebaseAuth
    private lateinit var callbackManager: CallbackManager

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        this.onSignInResult(res)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        FacebookSdk.setClientToken("@string/facebook_app_id") // Reemplaza con tu token de cliente de Facebook

        FacebookSdk.sdkInitialize(applicationContext)
        callbackManager = CallbackManager.Factory.create()

        auth = FirebaseAuth.getInstance()
        editEmail = findViewById(R.id.edit_email)
        editPassword = findViewById(R.id.edit_password)
        btnLoginNormal = findViewById(R.id.btn_login_normal)
        btnLogin = findViewById(R.id.btn_login)
        btnLoginFacebook = findViewById(R.id.btn_login_facebook)

        btnLoginNormal.setOnClickListener {
            val email = editEmail.text.toString()
            val password = editPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginWithEmail(email, password)
            } else {
                Toast.makeText(this, "Los campos de email y contraseña son requeridos", Toast.LENGTH_SHORT).show()
            }
        }

        btnLogin.setOnClickListener {
            // Aquí puedes implementar el código para el inicio de sesión con Google
            val providers = arrayListOf(
                AuthUI.IdpConfig.GoogleBuilder().build()
            )

            val signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build()
            signInLauncher.launch(signInIntent)
        }

        btnLoginFacebook.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                // Inicio de sesión exitoso con Facebook
                // Puedes acceder al token de acceso con: loginResult.accessToken
                // Aquí puedes implementar la lógica para iniciar sesión en Firebase usando el token de acceso de Facebook
                Toast.makeText(this@LoginActivity, "Inicio de sesión con Facebook exitoso", Toast.LENGTH_SHORT).show()
                // ...
            }

            override fun onCancel() {
                // El usuario canceló el inicio de sesión con Facebook
                Toast.makeText(this@LoginActivity, "Inicio de sesión con Facebook cancelado", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: FacebookException) {
                // Ocurrió un error durante el inicio de sesión con Facebook
                Toast.makeText(this@LoginActivity, "Error al iniciar sesión con Facebook", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            Toast.makeText(this, "Bienvenid@ ${user?.email}", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, Principal::class.java))
            finish()
            // ...
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
            Toast.makeText(this, "Ocurrió un error al iniciar sesión", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loginWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Inicio de sesión exitoso, redirigir al usuario a la actividad principal
                    val user = auth.currentUser
                    Toast.makeText(this, "Bienvenid@ ${user?.email}", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, Principal::class.java))
                    finish()
                } else {
                    // Ocurrió un error durante el inicio de sesión, mostrar un mensaje de error
                    Toast.makeText(this, "Ocurrió un error al iniciar sesión", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

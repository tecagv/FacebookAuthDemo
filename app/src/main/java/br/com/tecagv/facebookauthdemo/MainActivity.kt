package br.com.tecagv.facebookauthdemo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.AccessTokenTracker
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.Profile
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import br.com.tecagv.facebookauthdemo.databinding.ActivityMainBinding
import org.json.JSONObject

/**
 * MainActivity
 *
 * Este aplicativo demonstra, de forma simples, o login com Facebook em Android/Kotlin.
 * Conceitos demonstrados:
 * 1. O aplicativo Android atua como "cliente" OAuth.
 * 2. O Facebook atua como servidor de autorização/autenticação.
 * 3. Após o consentimento do usuário, o SDK retorna um AccessToken.
 * 4. O AccessToken é usado para consultar dados básicos do usuário na Graph API.
 * 5. O app pode verificar sessão ativa, salvar dados mínimos localmente e encerrar login.
 */
class MainActivity : AppCompatActivity() {

    /**
     * ViewBinding evita findViewById e cria uma referência segura para os elementos da tela.
     */
    private lateinit var binding: ActivityMainBinding

    /**
     * CallbackManager recebe o resultado do fluxo de login iniciado pelo SDK do Facebook.
     * Ele roteia sucesso, cancelamento e erro para o callback registrado abaixo.
     */
    private lateinit var callbackManager: CallbackManager

    /**
     * AccessTokenTracker observa alterações de sessão.
     * Quando o usuário entra ou sai, o SDK altera o token atual e este tracker é chamado.
     */
    private lateinit var tokenTracker: AccessTokenTracker

    /**
     * Permissões mínimas para a demonstração.
     * public_profile: permite consultar dados públicos básicos.
     * email: solicita acesso ao e-mail, se a conta do usuário tiver e-mail disponível e autorizado.
     * Boa prática: solicite somente os escopos realmente necessários.
     */
    private val facebookPermissions = listOf("public_profile", "email")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Cria o gerenciador de callbacks do Facebook SDK.
        callbackManager = CallbackManager.Factory.create()

        // Configura o botão visual oficial do SDK.
        setupOfficialLoginButton()

        // Configura um botão comum do Android usando LoginManager.
        // Isso mostra que o login não depende obrigatoriamente do LoginButton do Facebook.
        setupCustomLoginButton()

        // Registra a lógica de tratamento dos resultados de autenticação.
        registerFacebookLoginCallback()

        // Observa mudanças de token para atualizar a interface automaticamente.
        setupAccessTokenTracker()

        // Botão de logout: encerra a sessão local mantida pelo SDK.
        binding.logoutButton.setOnClickListener {
            LoginManager.getInstance().logOut()
            clearLocalUserCache()
            showLoggedOutState("Sessão encerrada pelo usuário.")
        }

        // Ao abrir o app, verifica se já existe uma sessão válida.
        verifyExistingSession()
    }

    private fun setupOfficialLoginButton() {
        /**
         * LoginButton é o componente pronto do SDK.
         * Ele abre o fluxo de login, solicita permissões e chama o callback registrado.
         */
        binding.facebookLoginButton.setPermissions(*facebookPermissions.toTypedArray())
        binding.facebookLoginButton.registerCallback(callbackManager, createFacebookCallback())
    }

    private fun setupCustomLoginButton() {
        binding.customLoginButton.setOnClickListener {
            /**
             * LoginManager permite iniciar o fluxo manualmente.
             * É útil quando o projeto precisa de layout próprio ou regras antes do login.
             */
            LoginManager.getInstance().logInWithReadPermissions(
                this,
                facebookPermissions
            )
        }
    }

    private fun registerFacebookLoginCallback() {
        /**
         * Registro global para o LoginManager.
         * O mesmo callback trata resultados do botão customizado.
         */
        LoginManager.getInstance().registerCallback(callbackManager, createFacebookCallback())
    }

    private fun createFacebookCallback(): FacebookCallback<LoginResult> {
        return object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                /**
                 * O login foi concluído e o Facebook retornou um token.
                 * Em um app real, esse token pode ser enviado ao back-end para validação.
                 */
                val token = result.accessToken
                binding.statusText.text = "Status: login realizado com sucesso."
                fetchUserProfileFromGraphApi(token)
            }

            override fun onCancel() {
                /**
                 * O usuário fechou/cancelou a tela de autorização antes de concluir.
                 */
                showLoggedOutState("Login cancelado pelo usuário.")
            }

            override fun onError(error: FacebookException) {
                /**
                 * Erros comuns: App ID incorreto, Key Hash não cadastrado,
                 * pacote divergente no Meta for Developers ou ausência de internet.
                 */
                showLoggedOutState("Erro no login: ${error.localizedMessage}")
            }
        }
    }

    private fun setupAccessTokenTracker() {
        tokenTracker = object : AccessTokenTracker() {
            override fun onCurrentAccessTokenChanged(
                oldAccessToken: AccessToken?,
                currentAccessToken: AccessToken?
            ) {
                if (currentAccessToken == null) {
                    clearLocalUserCache()
                    showLoggedOutState("Token removido. Usuário não autenticado.")
                } else {
                    fetchUserProfileFromGraphApi(currentAccessToken)
                }
            }
        }
    }

    private fun verifyExistingSession() {
        /**
         * O SDK mantém sessão local enquanto o token for válido.
         * Esta verificação permite restaurar a tela sem exigir novo login a cada abertura.
         */
        val currentToken = AccessToken.getCurrentAccessToken()
        val isLoggedIn = currentToken != null && !currentToken.isExpired

        if (isLoggedIn) {
            binding.statusText.text = "Status: sessão ativa encontrada."
            fetchUserProfileFromGraphApi(currentToken!!)
        } else {
            showLoggedOutState("Nenhuma sessão ativa encontrada.")
        }
    }

    private fun fetchUserProfileFromGraphApi(accessToken: AccessToken) {
        /**
         * GraphRequest consulta o endpoint /me da Graph API.
         * O parâmetro "fields" define quais dados serão retornados.
         * Quanto menos campos forem solicitados, melhor para privacidade e revisão do app.
         */
        val request = GraphRequest.newMeRequest(accessToken) { jsonObject, response ->
            if (response?.error != null) {
                binding.statusText.text = "Status: login OK, mas falhou a consulta à Graph API."
                binding.userInfoText.text = "Erro Graph API: ${response?.error?.errorMessage}"
                return@newMeRequest
            }

            val userJson = jsonObject ?: JSONObject()
            val userId = userJson.optString("id", "não informado")
            val name = userJson.optString("name", "não informado")
            val email = userJson.optString("email", "e-mail não retornado")

            /**
             * Nunca exiba ou grave tokens completos em logs de produção.
             * Aqui mostramos apenas um prefixo para fins didáticos.
             */
            val tokenPreview = accessToken.token.take(32) + "..."
            val expiresAt = accessToken.expires
            val permissions = accessToken.permissions.joinToString(", ")

            saveLocalUserCache(userId, name, email)

            binding.statusText.text = "Status: usuário autenticado via Facebook."
            binding.userInfoText.text = buildString {
                appendLine("ID: $userId")
                appendLine("Nome: $name")
                appendLine("E-mail: $email")
                appendLine("Permissões concedidas: $permissions")
                appendLine("Token, prefixo didático: $tokenPreview")
                appendLine("Expira em: $expiresAt")
                appendLine()
                appendLine("Profile SDK atual: ${Profile.getCurrentProfile()?.name ?: "não carregado"}")
            }
        }

        val parameters = Bundle().apply {
            putString("fields", "id,name,email")
        }
        request.parameters = parameters
        request.executeAsync()
    }

    private fun saveLocalUserCache(id: String, name: String, email: String) {
        /**
         * SharedPreferences armazena apenas dados mínimos para demonstração.
         * Em sistemas reais, avalie criptografia, expiração e política de privacidade.
         */
        getSharedPreferences("facebook_user_cache", MODE_PRIVATE)
            .edit()
            .putString("id", id)
            .putString("name", name)
            .putString("email", email)
            .apply()
    }

    private fun clearLocalUserCache() {
        getSharedPreferences("facebook_user_cache", MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    private fun showLoggedOutState(message: String) {
        binding.statusText.text = "Status: $message"
        binding.userInfoText.text = "Usuário não autenticado. Toque em uma das opções de login com Facebook."
    }

    @Deprecated("Mantido porque o SDK do Facebook ainda encaminha resultados pelo fluxo tradicional.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        /**
         * Encaminha o resultado da Activity de login para o CallbackManager.
         * Sem esta linha, o app abre o login, mas não recebe sucesso/erro/cancelamento.
         */
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        /**
         * Interrompe o tracker para evitar callbacks após a destruição da Activity.
         */
        tokenTracker.stopTracking()
    }
}

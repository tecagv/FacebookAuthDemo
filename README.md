# FacebookAuthDemo — Kotlin / Android Studio

Projeto didático simples para demonstrar autenticação por **Facebook Login** em aplicativo Android nativo com Kotlin.

# Prof. Dr. Alexandre Garcez Vieira
# Centro Universitário Faveni - UniFAVENI


---

## 🎯 O que o exemplo demonstra

- Uso do **Facebook SDK para Android** (versão mais recente v18+).
- Login com o componente oficial `LoginButton`.
- Login com botão próprio (customizado) usando `LoginManager`.
- Solicitação de permissões mínimas: `public_profile` e `email`.
- Recebimento e validação local de `AccessToken`.
- Consulta básica à **Graph API** no endpoint `/me?fields=id,name,email`.
- Uso do `AccessTokenTracker` para reagir automaticamente a mudanças de estado da sessão.
- Encerramento de sessão com `LoginManager.logOut()`.
- Comentários didáticos no código explicando autenticação, token, permissões e callbacks.

---

## 📚 Arquitetura e Estrutura do Código

O aplicativo concentra sua lógica na `MainActivity.kt`. A seguir, detalhamos as responsabilidades dos principais componentes e a lógica estrutural:

### 1. Inicialização e Callbacks (`CallbackManager`)
O `CallbackManager` é responsável por rotear os resultados do fluxo de autenticação (Sucesso, Cancelamento ou Erro) de volta para o aplicativo. Ele é instanciado e o resultado processado pelo SDK é recebido e tratado na função `onActivityResult()`.

### 2. Formas de Login
O projeto ilustra duas maneiras de integrar e acionar o login do Facebook:
- **`LoginButton` (Oficial)**: Um componente visual padrão fornecido pelo SDK (`binding.facebookLoginButton`). Ele cuida de grande parte do gerenciamento do fluxo internamente e já possui o estilo de UI oficial da Meta.
- **`LoginManager` (Customizado)**: Permite acionar o fluxo de login a partir de qualquer view (por exemplo, um `Button` normal do Android — `binding.customLoginButton`). Requer o chamado explícito de `LoginManager.getInstance().logInWithReadPermissions(...)`. Ideal para apps com layout de botões muito customizados.

### 3. Recuperação de Dados do Usuário (`GraphRequest`)
Após o sucesso do login (na etapa `onSuccess` do callback), o aplicativo obtém um `AccessToken`. Usamos a classe `GraphRequest.newMeRequest` para consultar o endpoint `/me` na Graph API e solicitar os campos autorizados pelo escopo de permissão: `id`, `name` e `email`.

### 4. Monitoramento Ativo da Sessão (`AccessTokenTracker`)
Implementamos um `AccessTokenTracker` que fica escutando passivamente quaisquer mudanças no Token. Por exemplo, se o usuário fizer logout remotamente pelas configurações do Facebook, ou o token expirar, esse listener reage no aplicativo, mudando a interface para o estado de "deslogado".

### 5. Armazenamento Local Simples (Cache)
Os dados básicos recuperados (nome, e-mail, ID) são armazenados na memória do dispositivo usando **SharedPreferences** (`saveLocalUserCache` e `clearLocalUserCache`). Isso atua como uma forma de cache temporário para recuperar instantaneamente o nome do usuário entre as aberturas do app, sem precisar consultar a Graph API continuamente.

---

## 📦 Dependências Principais (`build.gradle.kts`)

- `com.facebook.android:facebook-login:18.2.3`: Pacote modular oficial para implementar a autenticação.
- `androidx.core:core-ktx` e `androidx.appcompat:appcompat`: Bibliotecas fundamentais do AndroidX compatíveis com as versões mais modernas de desenvolvimento.
- **ViewBinding**: Ativado diretamente na configuração do Gradle (`buildFeatures { viewBinding = true }`) para manipulação segura das views (exibição livre do método boilerplate `findViewById`).

---

## ⚙️ Requisitos

- **Android Studio** (versão recomendada Iguana / Jellyfish ou superior).
- **JDK 17**.
- Dispositivo ou emulador Android com acesso à internet (Min SDK 23+ / Android 6.0).
- Conta de desenvolvedor ativa no Meta for Developers.
- Aplicativo criado e configurado na plataforma [Meta for Developers](https://developers.facebook.com/).

---

## 🔐 Configuração obrigatória no Meta for Developers

1. Acesse **Meta for Developers**.
2. Crie um novo aplicativo ou selecione um existente.
3. Adicione o produto **Facebook Login**.
4. Configure a plataforma **Android** no painel de configurações (Settings > Basic/Advanced).
5. Informe exatamente estes dados:
   - **Package name**: `br.com.tecagv.facebookauthdemo`
   - **Default Activity Class Name**: `br.com.tecagv.facebookauthdemo.MainActivity`
   - **Key Hashes**: Hashes (assinaturas) do seu ambiente de Debug e/ou Release (veja a sessão "Gerar Key Hash" abaixo).
6. Copie o **App ID** e o **Client Token** gerados na página de configurações do app.

---

## 🛠 Configuração obrigatória no projeto

Abra o seguinte arquivo dentro do Android Studio:
```text
app/src/main/res/values/strings.xml
```

Substitua as configurações padrão pelos seus dados reais copiados do portal Meta for Developers:
```xml
<string name="facebook_app_id">SEU_FACEBOOK_APP_ID</string>
<string name="facebook_client_token">SEU_FACEBOOK_CLIENT_TOKEN</string>
<string name="fb_login_protocol_scheme">fbSEU_FACEBOOK_APP_ID</string>
```

**Exemplo Prático:** Se o App ID for `1234567890`:
```xml
<string name="facebook_app_id">1234567890</string>
<string name="facebook_client_token">CLIENT_TOKEN_REAL_AQUI_345678...</string>
<string name="fb_login_protocol_scheme">fb1234567890</string>
```

---

## 🔑 Gerar Key Hash de debug

Para o Facebook aceitar a tentativa de login do seu ambiente de desenvolvimento local, você deve autorizar a chave da sua máquina (Key Hash).

**No Windows:**
Normalmente o arquivo padrão de chave fica em: `C:\Users\SEU_USUARIO\.android\debug.keystore`
(No prompt de comando ou powershell):
```bash
keytool -exportcert -alias androiddebugkey -keystore "%USERPROFILE%\.android\debug.keystore" -storepass android -keypass android | openssl sha1 -binary | openssl base64
```

**No Linux/macOS:**
```bash
keytool -exportcert -alias androiddebugkey -keystore ~/.android/debug.keystore -storepass android -keypass android | openssl sha1 -binary | openssl base64
```
*(Nota: Certifique-se de ter o `openssl` instalado no seu sistema para gerar o hash em Base64).*

Depois de gerar o código com o comando acima, cadastre o resultado na seção **Facebook Login > Settings > Key Hashes** do seu aplicativo no portal Meta.

---

## 🚀 Como abrir e rodar no Android Studio

1. Descompacte o arquivo do repositório/ZIP.
2. Abra o Android Studio.
3. Escolha **Open** (ou File > Open) e selecione a pasta `FacebookAuthDemo`.
4. Aguarde até que o **Gradle sincronize** todas as dependências e o botão do elefante desapareça.
5. Siga as instruções acima para preencher suas chaves em `strings.xml`.
6. Selecione um emulador (Device Manager) ou conecte um aparelho físico.
7. Clique no botão de Play verde na barra superior ou use o atalho **Shift+F10** (Run 'app').

---

## ⚠️ Observação Importante

O projeto encontra-se com as configurações atualizadas e pronto para compilar localmente de imediato. Contudo, o **login real na plataforma Meta** apenas finalizará com sucesso quando você realizar a **Configuração Obrigatória do seu App ID/Token e Key Hash**. Essa exigência não é um defeito de código, mas sim uma proteção de segurança fundamental do protocolo OAuth e da Meta para impedir interceptações e o uso não autorizado de integrações não registradas.
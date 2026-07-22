# 🎓 EduAgenda

[![Android API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat-square&logo=android)](https://developer.android.com/about/dashboards)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%201.9-purple.svg?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg?style=flat-square&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Database](https://img.shields.io/badge/Database-Room-orange.svg?style=flat-square&logo=sqlite)](https://developer.android.com/training/data-storage/room)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM-red.svg?style=flat-square)](https://developer.android.com/topic/libraries/architecture)

**EduAgenda** é um aplicativo Android nativo, inteligente e moderno projetado exclusivamente para estudantes que buscam otimizar o gerenciamento do seu tempo, tarefas acadêmicas e sua grade escolar semanal. Com uma interface polida baseada no **Material Design 3**, o aplicativo combina acompanhamento offline confiável e notificações locais push robustas para garantir que você nunca perca uma entrega ou exame crítico.

---

## 🚀 Principais Funcionalidades

O EduAgenda organiza sua vida acadêmica em duas categorias integradas:

### 1. 📅 Gerenciamento Avançado de Tarefas & Prazos
*   **Organização de Entregas:** Cadastro rápido de tarefas, provas, trabalhos e projetos específicos.
*   **Categorização Personalizada:** Escolha o tipo de atividade (ex: *Prova, Trabalho, Exercício, Projeto*) e atrele à disciplina correspondente.
*   **Lembretes em Tempo Real:** Sistema de alarmes locais para lembrá-lo antes do prazo (0m, 15m, 1h, ou 24h de antecedência).
*   **Filtros Dinâmicos:** Classificação inteligente por prazos urgentes, disciplinas específicas ou buscas rápidas por texto.

### 2. 🏫 Grade Escolar Semanal (Timetable)
*   **Quadro de Horários Interativo:** Organize suas aulas de segunda a sexta-feira de forma totalmente visual.
*   **Identidade Visual:** Atribua cores específicas às disciplinas para rápida identificação e escaneamento visual fácil.
*   **Localização & Notas:** Registre salas de aula, links para videochamadas e observações de professores diretamente no detalhe de cada período.

### 3. Simplicidade e Integração do Sistema
*   **Material 3 & Edge-to-Edge:** Layout fluido, com visualização moderna, suporte a transições suaves e design focado no touch target ergonômico.
*   **Notificações Confiáveis:** Construído com o `AlarmManager` nativo do Android. Funciona em segundo plano e persiste inteligentemente mesmo após reinicializações.
*   **Totalmente Offline-First:** Persistência imediata com banco de dados seguro Room (SQLite). Seus dados ficam protegidos localmente em seu dispositivo.

---

## 🛠️ Stack Tecnológica

O projeto foi construído utilizando as ferramentas de desenvolvimento Android mais recomendadas e modernas do setor:

*   **Linguagem de Programação:** [Kotlin 1.9.x](https://kotlinlang.org/) — com tipagem estática segura, corrotinas assíncronas e excelente performance.
*   **Interface Declarativa:** [Jetpack Compose](https://developer.android.com/jetpack/compose) — UI moderna baseada em componentes reativos, eliminando arquivos XML inflados de layout.
*   **Sistemas de Design:** [Material Design 3 (M3)](https://m3.material.io/) — incluindo cores dinâmicas, tipografia harmônica e espaçamentos padronizados.
*   **Banco de Dados Local:** [Room Persistence Library](https://developer.android.com/training/data-storage/room) — camada de abstração eficiente sob o SQLite para consultas seguras em tempo de compilação.
*   **Arquitetura:** [MVVM (Model-View-ViewModel)](https://developer.android.com/topic/libraries/architecture/viewmodel) — isolando a lógica de negócio do ciclo de vida da interface, garantindo legibilidade e testabilidade.
*   **Programação Reativa:** [Kotlin Flows & Coroutines](https://kotlinlang.org/docs/flow.html) — transmissão de dados e atualizações em tempo real entre o banco de dados e a UI sem travar a thread principal.
*   **Notificações & Alarmes:** `Android AlarmManager` & `BroadcastReceiver` — para o disparo programado de alarmes precisos de prazo acadêmico.

---

## 📂 Arquitetura do Projeto

O código do EduAgenda segue as melhores práticas do guia oficial de arquitetura Android. Abaixo está a visão geral da organização de pacotes:

```text
com.example/
│
├── data/
│   ├── AppDatabase.kt        # Ponto de acesso do Room Database local SQLite.
│   ├── Task.kt               # Entidade que define os atributos de Tarefas.
│   ├── TaskDao.kt            # Operações de leitura/escrita de Tarefas no DB.
│   ├── ClassSchedule.kt      # Entidade do banco para horários de aula.
│   ├── ClassScheduleDao.kt   # Operações de leitura/escrita escolares.
│   └── TaskRepository.kt     # Camada de abstração que unifica as fontes de dados.
│
├── notification/
│   ├── AlarmScheduler.kt     # Gerencia o registro e cancelamento de alarmes no sistema operacional.
│   └── DeadlineReceiver.kt   # Receptor de broadcast que dispara a notificação no Android.
│
└── MainActivity.kt           # Orquestrador da Interface do Usuário (Jetpack Compose / ViewModel).
```

---

## ⚙️ Configuração & Execução

Para compilar e executar o EduAgenda em sua máquina local ou emular pelo Android Studio, certifique-se de preencher os seguintes pré-requisitos:

### Pré-requisitos
*   **Android Studio Jellyfish** (ou superior)
*   **Java Development Kit (JDK) 17**
*   **Android SDK** configurado com API Level 24 (`minSdk`) até API Level 34 (`targetSdk`/`compileSdk`).

### Passo a Passo

1.  **Clonar o Repositório**
    ```bash
    git clone https://github.com/seu-usuario/eduagenda.git
    cd eduagenda
    ```

2.  **Compilar via Gradle**
    Para limpar builds anteriores e certificar de que todas as dependências estão resolvidas:
    ```bash
    gradle assembleDebug
    ```

3.  **Executar Testes de Unidade**
    O projeto contém testes locais prontos para execução na JVM (Robolectric):
    ```bash
    gradle :app:testDebugUnitTest
    ```

### 4. Download do APK e Integração Contínua (GitHub)

* **Download Direto do APK:** O arquivo do aplicativo está na pasta **`apk/app-debug.apk`**.
  * ⚠️ **Atenção ao baixar pelo celular no GitHub:** Para evitar o erro *"Ocorreu um problema ao analisar o pacote"*, não baixe a página do arquivo. No GitHub, abra o arquivo `apk/app-debug.apk` e clique no botão **"Download raw file"** (ou **"Baixar arquivo bruto"** / ícone de download com seta para baixo) para baixar o arquivo `.apk` real e não uma página HTML.
* **Build Automatizado (GitHub Actions):** O repositório conta com uma workflow automatizada (`.github/workflows/android-build.yml`) que compila e gera o APK a cada novo push ou pull request na aba **Actions** em *Artifacts*.

---

## 🎨 Design do App Icon (Identidade Visual)

A identidade visual do aplicativo utiliza um ícone adaptativo polido focado em conceitos acadêmicos e temporais:
*   **Coroa do Chapéu de Formatura (Mortarboard):** Integrado de forma limpa na parte superior do ícone, simbolizando o sucesso educacional e a formação acadêmica.
*   **Ampulheta Estilizada (Hourglass):** O corpo central exibe uma ampulheta sofisticada com areia em cor caramelo descendo sutilmente, representando o progresso do tempo e a organização de prazos.
*   **Paleta de Cores:** Foco em tons elegantes de azul cobalto e celeste associados com detalhes dourados/caramelo para máxima legibilidade e requinte estético.

---

## 📝 Licença

Este projeto está licenciado sob de acordo com as especificações internas do desenvolvedor. Sinta-se livre para utilizar, expandir e aprender de forma acadêmica!

Criado com dedicação para facilitar a vida escolar de estudantes em todo o país. 🚀

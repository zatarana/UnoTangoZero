# 🎯 UnoTangoZero

**Um gerenciador completo de vida pessoal integrado** — Finances, Projetos, Hábitos, Tarefas e muito mais em uma plataforma única.

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![Android](https://img.shields.io/badge/android-26+-green.svg)
![Kotlin](https://img.shields.io/badge/kotlin-100%25-purple.svg)
![License](https://img.shields.io/badge/license-MIT-orange.svg)

## ✨ Features Principais

- 💰 **Gestão Financeira Avançada**
  - Rastreamento de despesas e receitas
  - Categorização automática com IA
  - Relatórios e análises detalhadas
  - Orçamentos e metas financeiras

- 📋 **Gerenciamento de Projetos**
  - Criação de projetos com múltiplos estágios
  - Tarefas com subtarefas aninhadas
  - Atribuição de prioridades e prazos
  - Visualização em Kanban e Timeline

- ✅ **Rastreamento de Hábitos**
  - Registro diário de hábitos com check-ins
  - Streaks e estatísticas de consistência
  - Lembretes inteligentes por horário
  - Análise visual de progresso

- ⏰ **Gerenciamento de Tarefas**
  - Tarefas com agendamento inteligente
  - Notificações e lembretes customizados
  - Integração com calendário
  - Sistema de prioridades dinâmicas

- 🎨 **Design Moderno & UX Intuitiva**
  - Material Design 3 em Jetpack Compose
  - Tema claro/escuro automático
  - Navegação fluida e responsiva
  - Acessibilidade WCAG AA

- 🔔 **Notificações Inteligentes**
  - Lembretes contextuais
  - Notificações push customizadas
  - Integração com Android WorkManager

## 🏗️ Arquitetura

```
app/
├── data/                    # Camada de Dados
│   ├── datasources/        # Room, DataStore, APIs
│   ├── repositories/       # Implementações dos repositórios
│   └── models/             # Data classes
├── domain/                 # Lógica de Negócio
│   ├── usecases/          # Use cases por domínio
│   ├── repositories/       # Interfaces abstratas
│   └── models/             # Entities de domínio
├── presentation/           # Camada de UI
│   ├── screens/           # Telas por feature
│   ├── components/        # Componentes reutilizáveis
│   ├── viewmodels/        # ViewModels
│   ├── state/             # Gerenciamento de estado
│   └── theme/             # Temas e estilos
├── di/                    # Dependency Injection
│   └── modules/           # Módulos Hilt
└── notifications/         # Notificações e Alarms
```

## 🚀 Começando

### Requisitos
- Android 26+ (Android 8.0)
- Android Studio 2024.1+
- Java 17 ou superior

### Instalação

```bash
# Clone o repositório
git clone https://github.com/zatarana/UnoTangoZero.git
cd UnoTangoZero

# Build com Gradle
./gradlew build

# Build e instalar em dispositivo
./gradlew installDebug
```

### Configuração Inicial
1. Abra o app na primeira vez
2. Configure sua preferência de tema
3. Autorize permissões necessárias (notificações, localização)
4. Comece a adicionar tarefas, hábitos e metas!

## 📊 Stack Técnico

### UI & Composição
- **Jetpack Compose** - UI moderna declarativa
- **Material Design 3** - Design system profissional
- **Navigation Compose** - Navegação entre telas

### Arquitetura & Padrões
- **MVVM + Clean Architecture** - Separação clara de responsabilidades
- **Hilt** - Dependency Injection
- **Coroutines** - Assincronia e concorrência

### Dados & Persistência
- **Room Database** - Persistência local SQL
- **DataStore** - Preferências e configurações
- **Serialização JSON** - Gson

### Background & Notificações
- **WorkManager** - Tarefas em background
- **Alarms & Receivers** - Lembretes precisos

### Testes
- **JUnit 4** - Testes unitários
- **Espresso** - Testes instrumentalizados
- **Mockk/MockWebServer** - Mocking e stubbing

## 🎮 Como Usar

### Primeiro Acesso
- **Configurações de Perfil** - Customize nome e preferências
- **Dashboard** - Visão geral consolidada
- **Onboarding Guiado** - Tutorial interativo (primeira vez)

### Gerenciar Finanças
1. Vá para **Finanças** no menu
2. Clique **+ Adicionar Transação**
3. Selecione tipo (Despesa/Receita)
4. Preencha valores e categoria
5. Visualize relatórios em **Análise**

### Criar Projetos
1. Menu → **Projetos**
2. **+ Novo Projeto**
3. Defina escopo, datas, equipe
4. Adicione tarefas com **+ Task**
5. Organize em Kanban ou Timeline

### Rastrear Hábitos
1. **Hábitos** → **+ Novo Hábito**
2. Configure frequência (Diária/Semanal/etc)
3. Defina lembretes
4. Faça check-in diários
5. Veja seu streak e estatísticas

### Organizar Tarefas
1. **Tarefas** → **+ Nova Tarefa**
2. Defina prioridade e prazo
3. Atribua a projetos (opcional)
4. Configure lembretes
5. Marque como completa

## 📱 Screenshots

[Adicionar screenshots do app aqui]

## 🤝 Contribuindo

Adoramos contribuições! Por favor veja [CONTRIBUTING.md](CONTRIBUTING.md) para detalhes sobre:
- Como reportar bugs
- Como sugerir features
- Processo de pull requests
- Padrões de código

### Roadmap

- [ ] Sincronização em nuvem (Google Drive)
- [ ] Compartilhamento de projetos
- [ ] IA para categorização automática
- [ ] Integração com Calendário
- [ ] Exportação de relatórios (PDF/Excel)
- [ ] Dark mode com temas customizados
- [ ] Offline-first com sincronização

## 🔒 Segurança & Privacidade

- Todos os dados são armazenados localmente por padrão
- Suporte a sincronização criptografada (opcional)
- Sem coleta de dados pessoais
- GDPR compliant

## 📄 Licença

Este projeto é licenciado sob a [MIT License](LICENSE) - veja o arquivo LICENSE para detalhes.

## 👨‍💻 Autor

**Zatarana** - Desenvolvedor Principal

## 🆘 Suporte

- 📧 Email: support@unotangozero.dev
- 🐛 Issues: [GitHub Issues](https://github.com/zatarana/UnoTangoZero/issues)
- 💬 Discussions: [GitHub Discussions](https://github.com/zatarana/UnoTangoZero/discussions)

## 📚 Documentação Adicional

- [ARCHITECTURE.md](docs/ARCHITECTURE.md) - Detalhes técnicos profundos
- [API.md](docs/API.md) - Documentação da API interna
- [CONTRIBUTING.md](CONTRIBUTING.md) - Guia de contribuição
- [CHANGELOG.md](CHANGELOG.md) - Histórico de versões

---

**Desenvolvido com ❤️ para simplificar sua vida.**
# Guia de Contribuição - UnoTangoZero

Obrigado por seu interesse em contribuir para o UnoTangoZero! Este documento fornece diretrizes e instruções para contribuir com o projeto.

## 📋 Código de Conduta

Este projeto adere ao [Pacto do Contribuidor](CODE_OF_CONDUCT.md). Ao participar, você concorda em respeitar este código.

## 🐛 Reportando Bugs

### Antes de reportar

- Verifique se o bug já foi reportado em [Issues](https://github.com/zatarana/UnoTangoZero/issues)
- Tente reproduzir em versão mais recente
- Colete informações relevantes (Android version, device, logs)

### Ao reportar

Use o template de bug report no GitHub Issues com:

```markdown
**Descrição do Bug**
Descrição clara e concisa do problema

**Passos para Reproduzir**
1. Vá para '...'
2. Clique em '...'
3. Veja o erro

**Comportamento Esperado**
Descrição do que deveria acontecer

**Logs/Screenshots**
Adicione logs de erro ou screenshots se possível

**Ambiente**
- Android Version: 
- Device: 
- App Version: 
```

## 💡 Sugerindo Melhorias

- Use [GitHub Discussions](https://github.com/zatarana/UnoTangoZero/discussions) para features
- Descreva o use case e benefício
- Inclua exemplos ou mockups se relevante
- Considere impacto em arquitetura e performance

## 🔧 Desenvolvimento Local

### Setup

```bash
# Fork e clone
git clone https://github.com/seu-username/UnoTangoZero.git
cd UnoTangoZero

# Criar branch para feature
git checkout -b feature/sua-feature

# Install dependencies e build
./gradlew build
```

### Padrões de Código

#### Kotlin
```kotlin
// Use sealed classes para estados
sealed class UiState {
    object Loading : UiState()
    data class Success(val data: Data) : UiState()
    data class Error(val exception: Exception) : UiState()
}

// Nomes descritivos
fun calculateMonthlyExpenses(transactions: List<Transaction>): Money

// Use data classes para modelos
data class User(val id: Long, val name: String, val email: String)
```

#### Composables
```kotlin
@Composable
fun TaskCard(
    task: Task,
    onTaskClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Sempre use modifier como parâmetro final
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Content
    }
}
```

### Branches

- `main` - Produção (estável)
- `develop` - Desenvolvimento (próxima release)
- `feature/*` - Novas features
- `fix/*` - Bug fixes
- `docs/*` - Documentação

Nome padrão: `feature/user-authentication`, `fix/crash-on-login`

## 📝 Commits

Use [Conventional Commits](https://www.conventionalcommits.org/):

```
feat(auth): add login with google
fix(ui): prevent crash on invalid input
docs(architecture): update clean architecture diagram
refactor(data): simplify repository layer
test(habits): add unit tests for streak calculation
```

## ✅ Pull Requests

### Antes de Submeter

1. **Atualize sua branch**: `git pull origin develop`
2. **Teste localmente**: `./gradlew test`
3. **Verify linting**: `./gradlew detekt`
4. **Run instrumented tests**: `./gradlew connectedAndroidTest`

### Ao Submeter

Use o template de PR:

```markdown
## Descrição
Breve descrição do que muda

## Tipo de Mudança
- [ ] Bug fix
- [ ] Nova feature
- [ ] Breaking change
- [ ] Documentação

## Como foi testado
Descreva os testes realizados

## Checklist
- [ ] Meu código segue style guidelines
- [ ] Executei linting e testes
- [ ] Adicionei testes para novas funcionalidades
- [ ] Atualizei documentação
- [ ] Sem breaking changes

Closes #123
```

## 🧪 Testes

### Unit Tests
```kotlin
@Test
fun calculateExpense_returnsCorrectTotal() {
    // Arrange
    val transactions = listOf(
        Transaction(100.0, "Food"),
        Transaction(50.0, "Transport")
    )
    
    // Act
    val total = calculator.sum(transactions)
    
    // Assert
    assertEquals(150.0, total)
}
```

### Instrumented Tests
```kotlin
@Test
fun addTask_displaysTaskInList() {
    // Arrange
    composeTestRule.setContent { TaskListScreen() }
    
    // Act
    composeTestRule.onNodeWithText("Add Task").performClick()
    
    // Assert
    composeTestRule.onNodeWithText("New Task").assertIsDisplayed()
}
```

### Coverage Mínimo: 70%
```bash
./gradlew testDebugUnitTestCoverage
```

## 📦 Versionamento

Seguimos [Semantic Versioning](https://semver.org/):

- `MAJOR.MINOR.PATCH` (v1.2.3)
- Major: Breaking changes
- Minor: Novas features (backward compatible)
- Patch: Bug fixes

## 🚀 Release Process

1. Criar PR `release/v1.x.x` com:
   - Bump de versão em `build.gradle.kts`
   - Atualizar `CHANGELOG.md`
   - Atualizar documentação

2. Review e merge para `main`
3. Tag release: `git tag v1.x.x`
4. Build APK/Bundle assinado
5. Publicar em GitHub Releases

## 📚 Documentação

- Mantenha README.md atualizado
- Documente mudanças em architecture
- Adicione comentários em código complexo
- Use KDoc para public APIs

```kotlin
/**
 * Calcula o total de despesas em um período.
 *
 * @param startDate Data inicial
 * @param endDate Data final
 * @return Total em Money
 * @throws IllegalArgumentException se startDate > endDate
 */
fun calculateTotal(startDate: LocalDate, endDate: LocalDate): Money
```

## 🎯 Areas que Precisamos de Help

- [ ] Testes instrumentalizados
- [ ] Otimização de performance
- [ ] Documentação
- [ ] Acessibilidade
- [ ] Tradução para outros idiomas
- [ ] Ícones e assets

## ❓ Dúvidas?

- 💬 [GitHub Discussions](https://github.com/zatarana/UnoTangoZero/discussions)
- 📧 Email: dev@unotangozero.dev

---

Obrigado por contribuir! 🎉
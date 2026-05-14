# Arquitetura do UnoTangoZero

## Visão Geral

O UnoTangoZero segue a **Clean Architecture** combinada com **MVVM** para garantir:
- Separação clara de responsabilidades
- Facilidade de manutenção e testes
- Escalabilidade para novas features
- Independência de frameworks

## 🏢 Estrutura em Camadas

```
┌─────────────────────────────────────┐
│         PRESENTATION LAYER          │
│  (UI - Composables, ViewModels)     │
└──────────────┬──────────────────────┘
               │ usa
┌──────────────▼──────────────────────┐
│          DOMAIN LAYER               │
│  (Use Cases, Entities, Interfaces)  │
└──────────────┬──────────────────────┘
               │ implementa
┌──────────────▼──────────────────────┐
│           DATA LAYER                │
│  (Repositories, DataSources)        │
└─────────────────────────────────────┘
```

## 📂 Estrutura de Pastas Detalhada

### Data Layer (`app/src/main/java/com/unotangozero/app/data/`)

```
data/
├── datasources/
│   ├── local/
│   │   ├── dao/                 # Room DAOs
│   │   │   ├── TaskDao.kt
│   │   │   ├── HabitDao.kt
│   │   │   ├── FinanceDao.kt
│   │   │   └── ProjectDao.kt
│   │   ├── database/
│   │   │   └── AppDatabase.kt   # Room Database
│   │   └── preferences/
│   │       └── PreferencesDataStore.kt
│   └── remote/
│       └── ApiService.kt         # Retrofit (future)
├── repositories/
│   ├── impl/
│   │   ├── TaskRepositoryImpl.kt
│   │   ├── HabitRepositoryImpl.kt
│   │   ├── FinanceRepositoryImpl.kt
│   │   └── ProjectRepositoryImpl.kt
│   └── mappers/
│       ├── TaskMapper.kt
│       ├── HabitMapper.kt
│       ├── FinanceMapper.kt
│       └── ProjectMapper.kt
└── models/
    ├── entities/                # Room Entities
    │   ├── TaskEntity.kt
    │   ├── HabitEntity.kt
    │   ├── TransactionEntity.kt
    │   └── ProjectEntity.kt
    └── dto/                      # Data Transfer Objects
        ├── TaskDto.kt
        ├── HabitDto.kt
        ├── TransactionDto.kt
        └── ProjectDto.kt
```

### Domain Layer (`app/src/main/java/com/unotangozero/app/domain/`)

```
domain/
├── usecases/
│   ├── tasks/
│   │   ├── CreateTaskUseCase.kt
│   │   ├── GetTasksUseCase.kt
│   │   ├── UpdateTaskUseCase.kt
│   │   └── DeleteTaskUseCase.kt
│   ├── habits/
│   │   ├── CreateHabitUseCase.kt
│   │   ├── CheckInHabitUseCase.kt
│   │   ├── GetHabitsUseCase.kt
│   │   └── CalculateStreakUseCase.kt
│   ├── finances/
│   │   ├── AddTransactionUseCase.kt
│   │   ├── GetTransactionsUseCase.kt
│   │   ├── CalculateTotalUseCase.kt
│   │   └── CategorizeTransactionUseCase.kt
│   └── projects/
│       ├── CreateProjectUseCase.kt
│       ├── GetProjectsUseCase.kt
│       ├── UpdateProjectStatusUseCase.kt
│       └── GetProjectTasksUseCase.kt
├── repositories/
│   ├── TaskRepository.kt
│   ├── HabitRepository.kt
│   ├── FinanceRepository.kt
│   └── ProjectRepository.kt
└── models/
    ├── Task.kt
    ├── Habit.kt
    ├── Transaction.kt
    ├── Project.kt
    ├── Money.kt
    ├── HabitFrequency.kt
    ├── Priority.kt
    └── TransactionCategory.kt
```

### Presentation Layer (`app/src/main/java/com/unotangozero/app/presentation/`)

```
presentation/
├── screens/
│   ├── home/
│   │   ├── HomeScreen.kt
│   │   ├── HomeViewModel.kt
│   │   └── HomeState.kt
│   ├── tasks/
│   │   ├── TaskListScreen.kt
│   │   ├── TaskDetailScreen.kt
│   │   ├── TaskFormScreen.kt
│   │   └── TaskListViewModel.kt
│   ├── habits/
│   │   ├── HabitListScreen.kt
│   │   ├── HabitDetailScreen.kt
│   │   ├── HabitFormScreen.kt
│   │   └── HabitViewModel.kt
│   ├── finances/
│   │   ├── FinanceScreen.kt
│   │   ├── TransactionFormScreen.kt
│   │   ├── AnalyticsScreen.kt
│   │   └── FinanceViewModel.kt
│   ├── projects/
│   │   ├── ProjectListScreen.kt
│   │   ├── ProjectDetailScreen.kt
│   │   ├── KanbanScreen.kt
│   │   ├── TimelineScreen.kt
│   │   └── ProjectViewModel.kt
│   └── settings/
│       ├── SettingsScreen.kt
│       ├── ThemeScreen.kt
│       └── SettingsViewModel.kt
├── components/
│   ├── common/
│   │   ├── AppTopBar.kt
│   │   ├── AppBottomBar.kt
│   │   ├── LoadingDialog.kt
│   │   ├── ErrorDialog.kt
│   │   └── EmptyState.kt
│   ├── cards/
│   │   ├── TaskCard.kt
│   │   ├── HabitCard.kt
│   │   ├── TransactionCard.kt
│   │   └── ProjectCard.kt
│   └── forms/
│       ├── TaskFormFields.kt
│       ├── HabitFormFields.kt
│       ├── TransactionFormFields.kt
│       └── ProjectFormFields.kt
├── viewmodels/
│   ├── BaseViewModel.kt
│   ├── TaskListViewModel.kt
│   ├── HabitViewModel.kt
│   ├── FinanceViewModel.kt
│   └── ProjectViewModel.kt
├── state/
│   ├── UiState.kt
│   ├── TaskListUiState.kt
│   ├── HabitUiState.kt
│   ├── FinanceUiState.kt
│   └── ProjectUiState.kt
├── theme/
│   ├── Color.kt
│   ├── Typography.kt
│   ├── Shape.kt
│   ├── Theme.kt
│   └── Dimension.kt
and
└── navigation/
    ├── AppNavigation.kt
    ├── NavRoutes.kt
    └── NavigationEvent.kt
```

### DI Layer (`app/src/main/java/com/unotangozero/app/di/`)

```
di/
├── modules/
│   ├── DatabaseModule.kt      # Room + DataStore
│   ├── RepositoryModule.kt    # Repository bindings
│   ├── UseCaseModule.kt       # UseCase bindings
│   ├── ViewModelModule.kt     # ViewModel factories
│   └── UtilModule.kt          # Utilities
└── hilt/
    └── HiltApplication.kt
```

### Notifications (`app/src/main/java/com/unotangozero/app/notifications/`)

```
notifications/
├── TaskReminderReceiver.kt
├── HabitReminderReceiver.kt
├── BootCompletedReceiver.kt
├── NotificationManager.kt
└── ReminderWorker.kt
```

## 🔄 Fluxo de Dados

### Exemplo: Criar uma Tarefa

```
UI (TaskFormScreen)
    │
    ├─→ ViewModel.createTask(task)
    │       │
    │       ├─→ UseCase.execute(task)
    │       │       │
    │       │       └─→ Repository.createTask(task)
    │       │               │
    │       │               └─→ DataSource.insertTask(entity)
    │       │                       │
    │       │                       └─→ Room DAO.insert()
    │       │
    │       └─→ emit(Success(result))
    │
    └─→ Update State → Recompose UI
```

## 🔐 Dependency Injection com Hilt

```kotlin
@HiltViewModel
class TaskViewModel @Inject constructor(
    private val createTaskUseCase: CreateTaskUseCase,
    private val getTasksUseCase: GetTasksUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase
) : ViewModel() {
    // ...
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideTaskRepository(
        taskDao: TaskDao,
        mapper: TaskMapper
    ): TaskRepository = TaskRepositoryImpl(taskDao, mapper)
}
```

## 📊 Diagramas UML

### Entidades Principais

```
┌─────────────┐      ┌──────────────┐      ┌──────────────┐
│   Project   │      │   Task       │      │   Habit      │
├─────────────┤      ├──────────────┤      ├──────────────┤
│ id          │      │ id           │      │ id           │
│ name        │      │ title        │      │ name         │
│ description │      │ description  │      │ frequency    │
│ startDate   │      │ projectId    │      │ streak       │
│ endDate     │◄─────│ priority     │      │ lastCheckIn  │
│ status      │      │ dueDate      │      │ checkIns     │
│ owner       │      │ completed    │      │ reminders    │
└─────────────┘      └──────────────┘      └──────────────┘
       │
       │ has many
       ▼
   ┌─────────────────┐
   │  Transaction    │
   ├─────────────────┤
   │ id              │
   │ amount          │
   │ category        │
   │ date            │
   │ type (in/out)   │
   │ description     │
   └─────────────────┘
```

## 🧪 Estratégia de Testes

```
app/src/test/
├── domain/
│   └── usecases/        # Unit tests para use cases
├── data/
│   └── repositories/    # Unit tests para repositories
└── presentation/
    └── viewmodels/      # Unit tests para ViewModels

app/src/androidTest/
└── presentation/
    └── screens/         # Instrumented tests para UI
```

## 🔧 Configuração e Build

- **Language**: Kotlin
- **Build System**: Gradle (Kotlin DSL)
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34

## 📦 Gerenciamento de Dependências

Veja `build.gradle.kts` para versões atualizadas:

```gradle
dependencies {
    // Jetpack
    implementation("androidx.compose.ui:ui:")
    implementation("androidx.room:room-runtime:")
    
    // DI
    implementation("com.google.dagger:hilt-android:")
    
    // Networking (future)
    implementation("com.squareup.retrofit2:retrofit:")
    
    // Testing
    testImplementation("junit:junit:")
    androidTestImplementation("androidx.test.espresso:espresso-core:")
}
```

---

**Última atualização**: 2026-05-14
package com.unotangozero.app.presentation.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unotangozero.app.data.categories.FinancialCategoryRepository
import com.unotangozero.app.domain.models.FinancialCategory
import com.unotangozero.app.domain.models.FinancialCategoryType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FinancialCategoryFormState(
    val name: String = "",
    val type: FinancialCategoryType = FinancialCategoryType.EXPENSE,
    val parentName: String = ""
)

@HiltViewModel
class FinancialCategoriesViewModel @Inject constructor(
    private val repository: FinancialCategoryRepository
) : ViewModel() {
    val categories: StateFlow<List<FinancialCategory>> = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _form = MutableStateFlow(FinancialCategoryFormState())
    val form: StateFlow<FinancialCategoryFormState> = _form.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun onNameChange(value: String) { _form.value = _form.value.copy(name = value) }
    fun onTypeChange(value: FinancialCategoryType) { _form.value = _form.value.copy(type = value) }
    fun onParentNameChange(value: String) { _form.value = _form.value.copy(parentName = value) }

    fun saveCategory() {
        val state = _form.value
        if (state.name.trim().isBlank()) {
            _message.value = "Digite o nome da categoria."
            return
        }
        viewModelScope.launch {
            repository.save(
                FinancialCategory(
                    name = state.name.trim(),
                    type = state.type,
                    parentName = state.parentName.trim().ifBlank { null }
                )
            ).onSuccess {
                _form.value = FinancialCategoryFormState(type = state.type)
                _message.value = "Categoria salva."
            }.onFailure {
                _message.value = it.message ?: "Não foi possível salvar."
            }
        }
    }

    fun archiveCategory(category: FinancialCategory) {
        viewModelScope.launch {
            repository.archive(category.id)
                .onSuccess { _message.value = "Categoria arquivada." }
                .onFailure { _message.value = it.message ?: "Não foi possível arquivar." }
        }
    }

    fun clearMessage() { _message.value = null }
}

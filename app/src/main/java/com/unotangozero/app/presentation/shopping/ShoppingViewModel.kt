package com.unotangozero.app.presentation.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unotangozero.app.domain.models.ShoppingItem
import com.unotangozero.app.domain.models.ShoppingList
import com.unotangozero.app.domain.repositories.ShoppingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.round

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class ShoppingViewModel @Inject constructor(
    private val shoppingRepository: ShoppingRepository
) : ViewModel() {
    val lists: StateFlow<List<ShoppingList>> = shoppingRepository
        .observeActiveLists()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _selectedListId = MutableStateFlow<String?>(null)
    val selectedListId: StateFlow<String?> = _selectedListId.asStateFlow()

    val items: StateFlow<List<ShoppingItem>> = _selectedListId
        .flatMapLatest { listId ->
            if (listId == null) {
                kotlinx.coroutines.flow.flowOf(emptyList())
            } else {
                shoppingRepository.observeItems(listId)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _listName = MutableStateFlow("")
    val listName: StateFlow<String> = _listName.asStateFlow()

    private val _itemName = MutableStateFlow("")
    val itemName: StateFlow<String> = _itemName.asStateFlow()

    private val _quantityText = MutableStateFlow("1")
    val quantityText: StateFlow<String> = _quantityText.asStateFlow()

    private val _unit = MutableStateFlow("un")
    val unit: StateFlow<String> = _unit.asStateFlow()

    private val _estimatedPriceText = MutableStateFlow("")
    val estimatedPriceText: StateFlow<String> = _estimatedPriceText.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun selectList(listId: String) {
        _selectedListId.value = listId
    }

    fun onListNameChange(value: String) {
        _listName.value = value
    }

    fun onItemNameChange(value: String) {
        _itemName.value = value
    }

    fun onQuantityChange(value: String) {
        _quantityText.value = value.filter { it.isDigit() || it == ',' || it == '.' }
    }

    fun onUnitChange(value: String) {
        _unit.value = value
    }

    fun onEstimatedPriceChange(value: String) {
        _estimatedPriceText.value = value.filter { it.isDigit() || it == ',' || it == '.' }
    }

    fun createList() {
        val name = _listName.value.trim()
        if (name.isBlank()) {
            _message.value = "Digite o nome da lista."
            return
        }

        viewModelScope.launch {
            val list = ShoppingList(name = name)
            shoppingRepository.saveList(list)
                .onSuccess {
                    _listName.value = ""
                    _selectedListId.value = list.id
                    _message.value = "Lista criada."
                }
                .onFailure { _message.value = it.message ?: "Não foi possível criar a lista." }
        }
    }

    fun deleteList(list: ShoppingList) {
        viewModelScope.launch {
            shoppingRepository.deleteList(list.id)
                .onSuccess {
                    if (_selectedListId.value == list.id) _selectedListId.value = null
                    _message.value = "Lista excluída."
                }
                .onFailure { _message.value = it.message ?: "Não foi possível excluir a lista." }
        }
    }

    fun addItem() {
        val listId = _selectedListId.value
        if (listId == null) {
            _message.value = "Selecione uma lista antes de adicionar itens."
            return
        }

        val name = _itemName.value.trim()
        if (name.isBlank()) {
            _message.value = "Digite o nome do item."
            return
        }

        val quantity = parseDecimal(_quantityText.value).takeIf { it > 0.0 } ?: 1.0
        val price = parseMoneyToCents(_estimatedPriceText.value).takeIf { it > 0L }

        viewModelScope.launch {
            val item = ShoppingItem(
                listId = listId,
                itemName = name,
                quantity = quantity,
                unit = _unit.value.ifBlank { "un" },
                estimatedPriceInCents = price
            )

            shoppingRepository.saveItem(item)
                .onSuccess {
                    _itemName.value = ""
                    _quantityText.value = "1"
                    _unit.value = "un"
                    _estimatedPriceText.value = ""
                    _message.value = "Item adicionado."
                }
                .onFailure { _message.value = it.message ?: "Não foi possível adicionar o item." }
        }
    }

    fun togglePurchased(item: ShoppingItem) {
        viewModelScope.launch {
            val updated = item.copy(isPurchased = !item.isPurchased)
            shoppingRepository.saveItem(updated)
                .onFailure { _message.value = it.message ?: "Não foi possível atualizar o item." }
        }
    }

    fun deleteItem(item: ShoppingItem) {
        viewModelScope.launch {
            shoppingRepository.deleteItem(item.id)
                .onSuccess { _message.value = "Item excluído." }
                .onFailure { _message.value = it.message ?: "Não foi possível excluir o item." }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    private fun parseDecimal(rawValue: String): Double {
        return rawValue.trim().replace(",", ".").toDoubleOrNull() ?: 0.0
    }

    private fun parseMoneyToCents(rawValue: String): Long {
        val normalized = rawValue.trim().replace(".", "").replace(",", ".")
        val amount = normalized.toDoubleOrNull() ?: return 0L
        return round(amount * 100).toLong()
    }
}

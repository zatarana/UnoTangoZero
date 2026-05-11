package com.unotangozero.app.presentation.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unotangozero.app.data.accounts.FinancialAccountRepository
import com.unotangozero.app.domain.enums.FinancialAccountType
import com.unotangozero.app.domain.models.FinancialAccount
import com.unotangozero.app.domain.models.FinancialAccountsSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.round

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val accountRepository: FinancialAccountRepository
) : ViewModel() {
    val accounts: StateFlow<List<FinancialAccount>> = accountRepository.accounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val summary: StateFlow<FinancialAccountsSummary> = accountRepository.summary
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FinancialAccountsSummary())

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _balanceText = MutableStateFlow("")
    val balanceText: StateFlow<String> = _balanceText.asStateFlow()

    private val _selectedType = MutableStateFlow(FinancialAccountType.CHECKING)
    val selectedType: StateFlow<FinancialAccountType> = _selectedType.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun onNameChange(value: String) {
        _name.value = value
    }

    fun onBalanceChange(value: String) {
        _balanceText.value = value.filter { it.isDigit() || it == ',' || it == '.' || it == '-' }
    }

    fun onTypeChange(type: FinancialAccountType) {
        _selectedType.value = type
    }

    fun saveAccount() {
        val name = _name.value.trim()
        val balanceInCents = parseMoneyToCents(_balanceText.value)

        if (name.isBlank()) {
            _message.value = "Digite o nome da conta."
            return
        }

        viewModelScope.launch {
            val account = FinancialAccount(
                name = name,
                type = _selectedType.value,
                initialBalanceInCents = balanceInCents
            )
            accountRepository.save(account)
                .onSuccess {
                    _name.value = ""
                    _balanceText.value = ""
                    _selectedType.value = FinancialAccountType.CHECKING
                    _message.value = "Conta criada."
                }
                .onFailure {
                    _message.value = it.message ?: "Não foi possível criar a conta."
                }
        }
    }

    fun archiveAccount(account: FinancialAccount) {
        viewModelScope.launch {
            accountRepository.archive(account.id)
                .onSuccess { _message.value = "Conta arquivada." }
                .onFailure { _message.value = it.message ?: "Não foi possível arquivar a conta." }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    private fun parseMoneyToCents(rawValue: String): Long {
        val normalized = rawValue.trim().replace(".", "").replace(",", ".")
        val amount = normalized.toDoubleOrNull() ?: return 0L
        return round(amount * 100).toLong()
    }
}

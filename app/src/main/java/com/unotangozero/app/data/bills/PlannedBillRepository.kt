package com.unotangozero.app.data.bills

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import com.unotangozero.app.data.finance.FinancialMovementRepository
import com.unotangozero.app.domain.models.FinancialMovement
import com.unotangozero.app.domain.models.FinancialMovementType
import com.unotangozero.app.domain.models.PlannedBill
import com.unotangozero.app.domain.models.PlannedBillType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

private val Context.plannedBillsDataStore by preferencesDataStore(name = "planned_bills")

@Singleton
class PlannedBillRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val movementRepository: FinancialMovementRepository
) {
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, JsonSerializer<LocalDate> { src, _, _ -> JsonPrimitive(src.toString()) })
        .registerTypeAdapter(LocalDate::class.java, JsonDeserializer { json, _, _ -> LocalDate.parse(json.asString) })
        .registerTypeAdapter(LocalDateTime::class.java, JsonSerializer<LocalDateTime> { src, _, _ -> JsonPrimitive(src.toString()) })
        .registerTypeAdapter(LocalDateTime::class.java, JsonDeserializer { json, _, _ -> LocalDateTime.parse(json.asString) })
        .create()

    private val billsKey = stringPreferencesKey("planned_bills_json")
    private val listType = object : TypeToken<List<PlannedBill>>() {}.type

    val bills: Flow<List<PlannedBill>> = context.plannedBillsDataStore.data.map { preferences ->
        parse(preferences[billsKey]).sortedWith(compareBy<PlannedBill> { it.isPaid }.thenBy { it.dueDate })
    }

    suspend fun saveBill(bill: PlannedBill): Result<Unit> = runCatching {
        context.plannedBillsDataStore.edit { preferences ->
            val current = parse(preferences[billsKey])
            val updated = current.filterNot { it.id == bill.id } + bill
            preferences[billsKey] = gson.toJson(updated)
        }
    }

    suspend fun deleteBill(billId: String): Result<Unit> = runCatching {
        context.plannedBillsDataStore.edit { preferences ->
            val current = parse(preferences[billsKey])
            preferences[billsKey] = gson.toJson(current.filterNot { it.id == billId })
        }
    }

    suspend fun markAsPaid(bill: PlannedBill, accountId: String): Result<Unit> = runCatching {
        val movementType = when (bill.type) {
            PlannedBillType.PAYABLE -> FinancialMovementType.EXPENSE
            PlannedBillType.RECEIVABLE -> FinancialMovementType.INCOME
        }
        movementRepository.addMovement(
            FinancialMovement(
                type = movementType,
                amountInCents = bill.amountInCents,
                date = LocalDate.now(),
                description = bill.description,
                category = bill.category,
                accountId = accountId
            )
        ).getOrThrow()

        context.plannedBillsDataStore.edit { preferences ->
            val current = parse(preferences[billsKey])
            val updated = current.map {
                if (it.id == bill.id) it.copy(isPaid = true, paidAt = LocalDate.now(), accountId = accountId) else it
            }
            preferences[billsKey] = gson.toJson(updated)
        }
    }

    private fun parse(json: String?): List<PlannedBill> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching { gson.fromJson<List<PlannedBill>>(json, listType) }.getOrDefault(emptyList())
    }
}

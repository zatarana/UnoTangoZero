package com.unotangozero.app.data.categories

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.unotangozero.app.domain.models.FinancialCategory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.financialCategoriesDataStore by preferencesDataStore(name = "financial_categories")

@Singleton
class FinancialCategoryRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()
    private val categoriesKey = stringPreferencesKey("categories_json")
    private val listType = object : TypeToken<List<FinancialCategory>>() {}.type

    val categories: Flow<List<FinancialCategory>> = context.financialCategoriesDataStore.data.map { preferences ->
        read(preferences[categoriesKey]).sortedWith(compareBy<FinancialCategory> { it.type.name }.thenBy { it.displayLabel })
    }

    suspend fun save(category: FinancialCategory): Result<Unit> = runCatching {
        context.financialCategoriesDataStore.edit { preferences ->
            val current = read(preferences[categoriesKey])
            val normalized = category.copy(
                name = category.name.trim().lowercase(),
                parentName = category.parentName?.trim()?.lowercase()?.ifBlank { null }
            )
            preferences[categoriesKey] = gson.toJson(current.filterNot { it.id == category.id } + normalized)
        }
    }

    suspend fun archive(categoryId: String): Result<Unit> = runCatching {
        context.financialCategoriesDataStore.edit { preferences ->
            val updated = read(preferences[categoriesKey]).map { category ->
                if (category.id == categoryId) category.copy(isArchived = true) else category
            }
            preferences[categoriesKey] = gson.toJson(updated)
        }
    }

    private fun read(json: String?): List<FinancialCategory> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching { gson.fromJson<List<FinancialCategory>>(json, listType) }.getOrDefault(emptyList())
    }
}

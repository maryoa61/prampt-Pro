package com.example.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PromptDao {

    @Query("SELECT * FROM prompts_history ORDER BY timestamp DESC")
    fun getAllPrompts(): Flow<List<PromptEntity>>

    @Query("SELECT * FROM prompts_history WHERE input_text LIKE '%' || :query || '%' OR prompt_text LIKE '%' || :query || '%' OR style LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchPrompts(query: String): Flow<List<PromptEntity>>

    @Query("SELECT * FROM prompts_history WHERE id = :id LIMIT 1")
    suspend fun getPromptById(id: Long): PromptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompt(prompt: PromptEntity): Long

    @Query("DELETE FROM prompts_history WHERE id = :id")
    suspend fun deletePromptById(id: Long)

    @Query("DELETE FROM prompts_history")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM prompts_history")
    fun getPromptCount(): Flow<Int>
}

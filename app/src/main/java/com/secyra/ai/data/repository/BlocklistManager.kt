package com.secyra.ai.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages a persistent local blocklist of phone numbers.
 *
 * This is a REAL blocklist that:
 * - Persists across app restarts
 * - Survives device reboots
 * - Stores numbers locally (not uploaded to servers)
 * - Supports blocking and unblocking
 * - Maintains blocking reasons and timestamps
 *
 * IMPORTANT:
 * - This is a local blocklist only
 * - System-level blocking requires additional Android roles and permissions
 * - Some devices may not support third-party call blocking
 * - We never claim that blocking will be 100% effective
 */
@Singleton
class BlocklistManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("secyra_blocklist", Context.MODE_PRIVATE)

    private val blocklistKey = "blocked_numbers"
    private val blocklistReasonsKey = "blocklist_reasons"

    private val _blockedNumbers = MutableStateFlow<Set<String>>(emptySet())
    val blockedNumbers: StateFlow<Set<String>> = _blockedNumbers.asStateFlow()

    private val _blocklistEntries = MutableStateFlow<List<BlocklistEntry>>(emptyList())
    val blocklistEntries: StateFlow<List<BlocklistEntry>> = _blocklistEntries.asStateFlow()

    init {
        loadBlocklist()
    }

    /**
     * Load the blocklist from persistent storage.
     */
    private fun loadBlocklist() {
        val blockedNumbersJson = sharedPreferences.getString(blocklistKey, null)
        val blocklistReasonsJson = sharedPreferences.getString(blocklistReasonsKey, null)

        val blockedNumbersSet = if (blockedNumbersJson != null) {
            try {
                val jsonArray = JSONArray(blockedNumbersJson)
                val set = mutableSetOf<String>()
                for (i in 0 until jsonArray.length()) {
                    set.add(jsonArray.getString(i))
                }
                set
            } catch (e: Exception) {
                emptySet()
            }
        } else {
            emptySet()
        }

        val blocklistReasonsList = if (blocklistReasonsJson != null) {
            try {
                val jsonArray = JSONArray(blocklistReasonsJson)
                val list = mutableListOf<BlocklistEntry>()
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    list.add(
                        BlocklistEntry(
                            phoneNumber = jsonObject.getString("phoneNumber"),
                            reason = jsonObject.getString("reason"),
                            timestamp = jsonObject.getLong("timestamp")
                        )
                    )
                }
                list
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }

        _blockedNumbers.value = blockedNumbersSet
        _blocklistEntries.value = blocklistReasonsList
    }

    /**
     * Add a phone number to the blocklist.
     *
     * @param phoneNumber The phone number to block
     * @param reason The reason for blocking (optional)
     * @return true if successfully added, false otherwise
     */
    fun blockNumber(phoneNumber: String, reason: String = "Blocked by user"): Boolean {
        val normalizedNumber = normalizePhoneNumber(phoneNumber)

        if (normalizedNumber in _blockedNumbers.value) {
            // Already blocked
            return false
        }

        val entry = BlocklistEntry(
            phoneNumber = normalizedNumber,
            reason = reason,
            timestamp = System.currentTimeMillis()
        )

        // Update in-memory state
        val newBlockedNumbers = _blockedNumbers.value + normalizedNumber
        val newEntries = _blocklistEntries.value + entry

        _blockedNumbers.value = newBlockedNumbers
        _blocklistEntries.value = newEntries

        // Persist to storage
        saveBlocklist()

        return true
    }

    /**
     * Remove a phone number from the blocklist.
     *
     * @param phoneNumber The phone number to unblock
     * @return true if successfully removed, false otherwise
     */
    fun unblockNumber(phoneNumber: String): Boolean {
        val normalizedNumber = normalizePhoneNumber(phoneNumber)

        if (normalizedNumber !in _blockedNumbers.value) {
            // Not blocked
            return false
        }

        // Update in-memory state
        val newBlockedNumbers = _blockedNumbers.value - normalizedNumber
        val newEntries = _blocklistEntries.value.filter { it.phoneNumber != normalizedNumber }

        _blockedNumbers.value = newBlockedNumbers
        _blocklistEntries.value = newEntries

        // Persist to storage
        saveBlocklist()

        return true
    }

    /**
     * Check if a phone number is currently blocked.
     *
     * @param phoneNumber The phone number to check
     * @return true if blocked, false otherwise
     */
    fun isBlocked(phoneNumber: String): Boolean {
        val normalizedNumber = normalizePhoneNumber(phoneNumber)
        return normalizedNumber in _blockedNumbers.value
    }

    /**
     * Get the blocklist entry for a phone number.
     *
     * @param phoneNumber The phone number to look up
     * @return BlocklistEntry if found, null otherwise
     */
    fun getBlocklistEntry(phoneNumber: String): BlocklistEntry? {
        val normalizedNumber = normalizePhoneNumber(phoneNumber)
        return _blocklistEntries.value.find { it.phoneNumber == normalizedNumber }
    }

    /**
     * Clear the entire blocklist.
     */
    fun clearBlocklist() {
        _blockedNumbers.value = emptySet()
        _blocklistEntries.value = emptyList()
        saveBlocklist()
    }

    /**
     * Save the current blocklist to persistent storage.
     */
    private fun saveBlocklist() {
        val blockedNumbersJson = JSONArray(_blockedNumbers.value.toList()).toString()
        val blocklistReasonsJson = JSONArray(_blocklistEntries.value.map { entry ->
            JSONObject().apply {
                put("phoneNumber", entry.phoneNumber)
                put("reason", entry.reason)
                put("timestamp", entry.timestamp)
            }
        }).toString()

        sharedPreferences.edit {
            putString(blocklistKey, blockedNumbersJson)
            putString(blocklistReasonsKey, blocklistReasonsJson)
        }
    }

    /**
     * Normalize a phone number for consistent comparison.
     * Removes spaces, dashes, parentheses, and country code variations.
     */
    private fun normalizePhoneNumber(phoneNumber: String): String {
        // Remove all non-digit characters
        var normalized = phoneNumber.replace("[^0-9]".toRegex(), "")

        // Remove leading country code if present (simple heuristic)
        // This is a basic implementation - production should use libphonenumber
        if (normalized.length > 10 && normalized.startsWith("1")) {
            normalized = normalized.substring(1)
        }

        return normalized
    }

    /**
     * Get the total count of blocked numbers.
     */
    fun getBlockedCount(): Int {
        return _blockedNumbers.value.size
    }
}

/**
 * Data class representing a blocklist entry.
 */
data class BlocklistEntry(
    val phoneNumber: String,
    val reason: String,
    val timestamp: Long
)
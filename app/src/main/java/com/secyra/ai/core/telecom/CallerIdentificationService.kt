package com.secyra.ai.core.telecom

import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import com.secyra.ai.core.permissions.PermissionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CallerIdentificationService - Identifies callers using real contact data.
 *
 * This service:
 * - Uses the Android Contacts provider (with permission)
 * - Normalizes phone numbers for accurate matching
 * - Returns contact names, photos, and verification status
 * - Handles permission denial gracefully
 * - Never fabricates caller information
 *
 * Identification priority:
 * 1. User's Contacts database (when permission granted)
 * 2. Trusted local list (future: allowlist)
 * 3. External caller ID service (when integrated)
 * 4. Returns "Unknown" when no identification is available
 *
 * IMPORTANT:
 * - Requires READ_CONTACTS permission
 * - Never claims a caller is verified without proof
 * - Uses libphonenumber-style normalization (simplified here)
 * - Respects user privacy and permission choices
 */
@Singleton
class CallerIdentificationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionManager: PermissionManager
) {

    /**
     * Identify a caller by phone number.
     *
     * @param phoneNumber The phone number to identify
     * @return CallerInfo with name, photo, and verification status
     */
    suspend fun identifyCaller(phoneNumber: String): CallerInfo = withContext(Dispatchers.IO) {
        // Check if we have Contacts permission
        val hasContactsPermission = permissionManager.isPermissionGranted(
            com.secyra.ai.core.permissions.PermissionManager.Permissions.READ_CONTACTS
        )

        if (!hasContactsPermission) {
            // No permission - return unknown
            return@withContext CallerInfo(
                phoneNumber = phoneNumber,
                name = null,
                photoUri = null,
                isContact = false,
                isVerified = false
            )
        }

        // Search contacts for this phone number
        val contactInfo = searchContacts(phoneNumber)

        if (contactInfo != null) {
            // Found in contacts
            CallerInfo(
                phoneNumber = phoneNumber,
                name = contactInfo.name,
                photoUri = contactInfo.photoUri,
                isContact = true,
                isVerified = false // Not verified by carrier, just in contacts
            )
        } else {
            // Not found in contacts
            CallerInfo(
                phoneNumber = phoneNumber,
                name = null,
                photoUri = null,
                isContact = false,
                isVerified = false
            )
        }
    }

    /**
     * Search the Contacts database for a phone number.
     *
     * This handles multiple formats of the same number by normalizing.
     */
    private fun searchContacts(phoneNumber: String): ContactInfo? {
        val contentResolver: ContentResolver = context.contentResolver

        // Normalize the input number
        val normalizedInput = normalizePhoneNumber(phoneNumber)

        // Query contacts with phone numbers
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val selection = "${ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER} = 1"
        val sortOrder = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"

        val cursor = contentResolver.query(uri, projection, selection, null, sortOrder)

        cursor?.use {
            val contactIdIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val displayNameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val photoUriIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val contactId = it.getString(contactIdIndex)
                val displayName = it.getString(displayNameIndex)
                val photoUri = it.getString(photoUriIndex)
                val contactNumber = it.getString(numberIndex)

                // Normalize the contact's phone number
                val normalizedContactNumber = normalizePhoneNumber(contactNumber)

                // Check if the normalized numbers match
                if (normalizedInput == normalizedContactNumber) {
                    return ContactInfo(
                        name = displayName,
                        photoUri = photoUri
                    )
                }

                // Also check for partial matches (different country codes)
                if (isPotentialMatch(normalizedInput, normalizedContactNumber)) {
                    return ContactInfo(
                        name = displayName,
                        photoUri = photoUri
                    )
                }
            }
        }

        return null
    }

    /**
     * Normalize a phone number for comparison.
     *
     * This is a simplified implementation. Production should use libphonenumber.
     * - Removes all non-digit characters
     - Handles common country code formats
     */
    private fun normalizePhoneNumber(phoneNumber: String): String {
        // Remove all non-digit characters
        var normalized = phoneNumber.replace("[^0-9]".toRegex(), "")

        // Remove leading '+' if present
        if (normalized.startsWith("00")) {
            normalized = normalized.substring(2)
        } else if (normalized.startsWith("0")) {
            // Keep leading 0 for local numbers
        }

        return normalized
    }

    /**
     * Check if two phone numbers could be the same with different country codes.
     *
     * This handles cases like:
     * - +1 555-123-4567 vs 555-123-4567
     * - +91 98765 43210 vs 98765 43210
     */
    private fun isPotentialMatch(num1: String, num2: String): Boolean {
        if (num1 == num2) return true

        // Try removing country code from the longer number
        val longer = if (num1.length > num2.length) num1 else num2
        val shorter = if (num1.length > num2.length) num2 else num1

        // If they differ by 1-3 digits (country code), check if the rest matches
        val lengthDiff = longer.length - shorter.length
        if (lengthDiff in 1..3) {
            val potentialMatch = longer.substring(lengthDiff)
            if (potentialMatch == shorter) {
                return true
            }
        }

        return false
    }

    /**
     * Get a contact's full details by contact ID.
     *
     * This can be used to load additional information for the profile screen.
     */
    suspend fun getContactDetails(contactId: String): ContactDetails? = withContext(Dispatchers.IO) {
        val hasContactsPermission = permissionManager.isPermissionGranted(
            com.secyra.ai.core.permissions.PermissionManager.Permissions.READ_CONTACTS
        )

        if (!hasContactsPermission) {
            return@withContext null
        }

        val contentResolver: ContentResolver = context.contentResolver

        val uri = ContactsContract.Contacts.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.PHOTO_URI,
            ContactsContract.Contacts.HAS_PHONE_NUMBER
        )

        val selection = "${ContactsContract.Contacts._ID} = ?"
        val selectionArgs = arrayOf(contactId)

        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)

        cursor?.use {
            if (it.moveToFirst()) {
                val idIndex = it.getColumnIndex(ContactsContract.Contacts._ID)
                val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                val photoUriIndex = it.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)
                val hasPhoneIndex = it.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

                val id = it.getString(idIndex)
                val name = it.getString(nameIndex)
                val photoUri = it.getString(photoUriIndex)
                val hasPhone = it.getInt(hasPhoneIndex) == 1

                // Get phone numbers for this contact
                val phoneNumbers = getContactPhoneNumbers(id)

                ContactDetails(
                    id = id,
                    name = name,
                    photoUri = photoUri,
                    hasPhoneNumber = hasPhone,
                    phoneNumbers = phoneNumbers
                )
            } else {
                null
            }
        }
    }

    /**
     * Get all phone numbers for a contact.
     */
    private fun getContactPhoneNumbers(contactId: String): List<String> {
        val contentResolver: ContentResolver = context.contentResolver
        val phoneNumbers = mutableListOf<String>()

        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val selection = "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?"
        val selectionArgs = arrayOf(contactId)

        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)

        cursor?.use {
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val number = it.getString(numberIndex)
                phoneNumbers.add(number)
            }
        }

        return phoneNumbers
    }
}

/**
 * Contact information returned by identification service.
 */
data class ContactInfo(
    val name: String,
    val photoUri: String?
)

/**
 * Full contact details for profile screen.
 */
data class ContactDetails(
    val id: String,
    val name: String,
    val photoUri: String?,
    val hasPhoneNumber: Boolean,
    val phoneNumbers: List<String>
)
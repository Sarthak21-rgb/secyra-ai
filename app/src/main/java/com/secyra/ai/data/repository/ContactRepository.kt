package com.secyra.ai.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import com.secyra.ai.core.permissions.PermissionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class Contact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val photoUri: String? = null
)

class ContactRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionManager: PermissionManager
) {

    @SuppressLint("Range")
    suspend fun getAllContacts(): List<Contact> = withContext(Dispatchers.IO) {
        if (!permissionManager.isPermissionGranted(com.secyra.ai.core.permissions.PermissionManager.Permissions.READ_CONTACTS)) {
            return@withContext emptyList()
        }

        val contacts = mutableListOf<Contact>()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        )

        val sortOrder = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"

        val cursor: Cursor? = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        cursor?.use {
            val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val photoUriIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

            while (it.moveToNext()) {
                val id = it.getString(idIndex)
                val name = it.getString(nameIndex) ?: "Unknown"
                val number = it.getString(numberIndex) ?: ""
                val photoUri = it.getString(photoUriIndex)

                if (number.isNotEmpty()) {
                    contacts.add(
                        Contact(
                            id = id,
                            name = name,
                            phoneNumber = number,
                            photoUri = photoUri
                        )
                    )
                }
            }
        }

        contacts
    }

    @SuppressLint("Range")
    suspend fun searchContacts(query: String): List<Contact> = withContext(Dispatchers.IO) {
        if (!permissionManager.isPermissionGranted(com.secyra.ai.core.permissions.PermissionManager.Permissions.READ_CONTACTS)) {
            return@withContext emptyList()
        }

        val contacts = mutableListOf<Contact>()
        val selection = if (query.isBlank()) null else """
            (${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ? OR 
             ${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ?)
        """.trimIndent()
        
        val selectionArgs = if (query.isBlank()) null else arrayOf("%$query%", "%$query%")

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        )

        val sortOrder = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"

        val cursor: Cursor? = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        cursor?.use {
            val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val photoUriIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

            while (it.moveToNext()) {
                val id = it.getString(idIndex)
                val name = it.getString(nameIndex) ?: "Unknown"
                val number = it.getString(numberIndex) ?: ""
                val photoUri = it.getString(photoUriIndex)

                if (number.isNotEmpty()) {
                    contacts.add(
                        Contact(
                            id = id,
                            name = name,
                            phoneNumber = number,
                            photoUri = photoUri
                        )
                    )
                }
            }
        }

        contacts
    }

    @SuppressLint("Range")
    suspend fun getContactByPhoneNumber(phoneNumber: String): Contact? = withContext(Dispatchers.IO) {
        if (!permissionManager.isPermissionGranted(com.secyra.ai.core.permissions.PermissionManager.Permissions.READ_CONTACTS)) {
            return@withContext null
        }

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        )

        val selection = "${ContactsContract.CommonDataKinds.Phone.NUMBER} = ?"
        val selectionArgs = arrayOf(phoneNumber)

        val cursor: Cursor? = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )

        cursor?.use {
            val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val photoUriIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

            if (it.moveToFirst()) {
                val id = it.getString(idIndex)
                val name = it.getString(nameIndex)
                val number = it.getString(numberIndex)
                val photoUri = it.getString(photoUriIndex)

                return@withContext Contact(
                    id = id,
                    name = name ?: "Unknown",
                    phoneNumber = number ?: "",
                    photoUri = photoUri
                )
            }
        }

        null
    }
}

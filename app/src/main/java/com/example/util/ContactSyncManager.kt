package com.example.util

import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.example.data.remote.RetrofitClient
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ContactSyncManager {

    data class ContactItem(val name: String, val phone: String)

    suspend fun syncUserContactsIfPermitted(context: Context, userEmail: String) = withContext(Dispatchers.IO) {
        if (userEmail.isBlank()) return@withContext
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return@withContext
        }

        try {
            val contactList = mutableListOf<ContactItem>()
            val contentResolver = context.contentResolver
            val cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null,
                null,
                null
            )

            cursor?.use { c ->
                val nameIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val phoneIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                while (c.moveToNext()) {
                    val name = if (nameIdx >= 0) c.getString(nameIdx) ?: "Unknown" else "Unknown"
                    val phone = if (phoneIdx >= 0) c.getString(phoneIdx) ?: "" else ""

                    if (phone.isNotBlank()) {
                        contactList.add(ContactItem(name = name, phone = phone))
                    }
                }
            }

            if (contactList.isNotEmpty()) {
                val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
                val listType = Types.newParameterizedType(List::class.java, ContactItem::class.java)
                val jsonAdapter = moshi.adapter<List<ContactItem>>(listType)
                val jsonString = jsonAdapter.toJson(contactList)

                RetrofitClient.apiService.uploadUserContacts(
                    email = userEmail,
                    contactsJson = jsonString
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

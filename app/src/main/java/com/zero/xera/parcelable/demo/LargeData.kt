package com.zero.xera.parcelable.demo

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable
import java.util.UUID

internal interface LargeData {
    val list: List<String>
}

private val genData = MutableList(50_000) {
    UUID.randomUUID().toString()
}

data class SerializableLargeData(override val list: List<String>) : LargeData, Serializable {

    companion object {
        // 1950,84 kb
        val instance = SerializableLargeData(genData)
    }
}

@Parcelize
data class ParcelableLargeData(override val list: List<String>) : LargeData, Parcelable {
    companion object {
        // 4000,676 kb
        val instance = ParcelableLargeData(genData)
    }
}
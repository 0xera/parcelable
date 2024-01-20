package com.zero.xera.parcelable.stream.internal


internal enum class ParcelType(val type: Int) {
    Serializable(0),
    Parcelable(1)
}

internal fun Int.toParcelType(): ParcelType = when(this) {
    0 -> ParcelType.Serializable
    1 -> ParcelType.Parcelable
    else -> error("Unexpected type of parcel")
}
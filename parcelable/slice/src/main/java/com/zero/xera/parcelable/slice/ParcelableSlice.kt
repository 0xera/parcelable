package com.zero.xera.parcelable.slice

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface ParcelableSlice<T> : Parcelable

@Parcelize
internal class ParcelableSliceImpl<T, C : Parcelable>(
    internal val chunks: ParceledList<C>
) : ParcelableSlice<T>
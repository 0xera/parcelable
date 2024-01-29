/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zero.xera.parcelable.stream;

import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;

public class ParceledFileDescriptor extends BaseParceledFileDescriptor {
    public ParceledFileDescriptor(ParcelFileDescriptor descriptor) {
        super(descriptor);
    }

    private ParceledFileDescriptor(Parcel in, ClassLoader loader) {
        super(in, loader);
    }

    @Override
    public int describeContents() {
        return getDescriptor().describeContents();
    }

    @Override
    protected void writeElement(ParcelFileDescriptor parcelable, Parcel dest, int callFlags) {
        parcelable.writeToParcel(dest, callFlags);
    }

    @Override
    protected void writeParcelableCreator(ParcelFileDescriptor parcelable, Parcel dest) {
        dest.writeParcelableCreator((Parcelable) parcelable);
    }

    @Override
    protected Creator<?> readParcelableCreator(Parcel from, ClassLoader loader) {
        return from.readParcelableCreator(loader);
    }

    @SuppressWarnings("unchecked")
    public static final ClassLoaderCreator<ParceledFileDescriptor> CREATOR =
            new ClassLoaderCreator<ParceledFileDescriptor>() {
        public ParceledFileDescriptor createFromParcel(Parcel in) {
            return new ParceledFileDescriptor(in, null);
        }

        @Override
        public ParceledFileDescriptor createFromParcel(Parcel in, ClassLoader loader) {
            return new ParceledFileDescriptor(in, loader);
        }

        @Override
        public ParceledFileDescriptor[] newArray(int size) {
            return new ParceledFileDescriptor[size];
        }
    };
}

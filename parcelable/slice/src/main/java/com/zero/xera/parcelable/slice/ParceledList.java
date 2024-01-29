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

package com.zero.xera.parcelable.slice;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class ParceledList<T extends Parcelable> extends BaseParceledList<T> {
    public ParceledList(List<T> list) {
        super(list);
    }

    private ParceledList(Parcel in, ClassLoader loader) {
        super(in, loader);
    }

    @Override
    public int describeContents() {
        int contents = 0;
        final List<T> list = getList();
        for (int i=0; i<list.size(); i++) {
            contents |= list.get(i).describeContents();
        }
        return contents;
    }

    @Override
    protected void writeElement(T parcelable, Parcel dest, int callFlags) {
        parcelable.writeToParcel(dest, callFlags);
    }

    @Override
    protected void writeParcelableCreator(T parcelable, Parcel dest) {
        dest.writeParcelableCreator((Parcelable) parcelable);
    }

    @Override
    protected Parcelable.Creator<?> readParcelableCreator(Parcel from, ClassLoader loader) {
        return from.readParcelableCreator(loader);
    }

    @SuppressWarnings("unchecked")
    public static final Parcelable.ClassLoaderCreator<ParceledList> CREATOR =
            new Parcelable.ClassLoaderCreator<ParceledList>() {
        public ParceledList createFromParcel(Parcel in) {
            return new ParceledList(in, null);
        }

        @Override
        public ParceledList createFromParcel(Parcel in, ClassLoader loader) {
            return new ParceledList(in, loader);
        }

        @Override
        public ParceledList[] newArray(int size) {
            return new ParceledList[size];
        }
    };
}

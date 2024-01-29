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

import android.os.BadParcelableException;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;

public abstract class BaseParceledFileDescriptor implements Parcelable {
    private static String TAG = "ParceledDescriptor";
    private static boolean DEBUG = false;
    private ParcelFileDescriptor mDescriptor;

    private boolean mHasBeenParceled = false;

    public BaseParceledFileDescriptor(ParcelFileDescriptor descriptor) {
        mDescriptor = descriptor;
    }

    @SuppressWarnings("unchecked")
    BaseParceledFileDescriptor(Parcel p, ClassLoader loader) {
        Creator<?> creator = readParcelableCreator(p, loader);

        final IBinder retriever = p.readStrongBinder();
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        try {
            retriever.transact(IBinder.FIRST_CALL_TRANSACTION, data, reply, 0);
            reply.readException();
            mDescriptor = readCreator(creator, reply, loader);
        } catch (RemoteException e) {
            throw new BadParcelableException("Failure retrieving stream parcelable");
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    private ParcelFileDescriptor readCreator(Creator<?> creator, Parcel p, ClassLoader loader) {
        if (creator instanceof ClassLoaderCreator<?>) {
            ClassLoaderCreator<?> classLoaderCreator =
                    (ClassLoaderCreator<?>) creator;
            return (ParcelFileDescriptor) classLoaderCreator.createFromParcel(p, loader);
        }
        return (ParcelFileDescriptor) creator.createFromParcel(p);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (mHasBeenParceled) {
            throw new IllegalStateException("Can't Parcel a ParceledListSlice more than once");
        }
        mHasBeenParceled = true;
        final int callFlags = flags;
        writeParcelableCreator(mDescriptor, dest);
        Binder retriever = new Binder() {
            @Override
            protected boolean onTransact(int code, Parcel data, Parcel reply, int flags)
                    throws RemoteException {
                if (code != FIRST_CALL_TRANSACTION) {
                    return super.onTransact(code, data, reply, flags);
                } else if (mDescriptor == null) {
                    throw new IllegalArgumentException("Attempt to transfer null descriptor, "
                            + "did transfer finish?");
                }
                try {
                    reply.writeNoException();
                    writeElement(mDescriptor, reply, callFlags);
                    if (DEBUG) Log.d(TAG, "Transfer done, clearing mDescriptor reference");
                    mDescriptor = null;
                } catch (RuntimeException e) {
                    if (DEBUG) Log.d(TAG, "Transfer failed, clearing mDescriptor reference");
                    mDescriptor = null;
                    throw e;
                }
                return true;
            }
        };
        dest.writeStrongBinder(retriever);

    }

    public ParcelFileDescriptor getDescriptor() {
        return mDescriptor;
    }

    protected abstract void writeElement(ParcelFileDescriptor parcelable, Parcel reply, int callFlags);

    protected abstract void writeParcelableCreator(ParcelFileDescriptor parcelable, Parcel dest);

    protected abstract Creator<?> readParcelableCreator(Parcel from, ClassLoader loader);
}

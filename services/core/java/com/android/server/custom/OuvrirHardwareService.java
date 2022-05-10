/*
 * Copyright (C) 2015-2016 The CyanogenMod Project
 *               2017-2019 The LineageOS Project
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
package com.android.server.custom;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.util.Range;

import com.android.server.LocalServices;
import com.android.server.SystemService;

import com.android.internal.custom.hardware.IOuvrirHardwareService;
import com.android.internal.custom.hardware.OuvrirHardwareManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.android.internal.custom.app.OuvrirContextConstants;


/** @hide */
public class OuvrirHardwareService extends SystemService {

    private static final boolean DEBUG = true;
    private static final String TAG = OuvrirHardwareService.class.getSimpleName();

    private final Context mContext;
    private final OuvrirHardwareInterface mOuvrirHwImpl;

    private interface OuvrirHardwareInterface {
        public int getSupportedFeatures();
        public boolean get(int feature);
        public boolean set(int feature, boolean enable);
    }

    private class LegacyOuvrirHardware implements OuvrirHardwareInterface {

        private int mSupportedFeatures = 0;

        public LegacyOuvrirHardware() {
        }

        public int getSupportedFeatures() {
            return mSupportedFeatures;
        }

        public boolean get(int feature) {
            switch(feature) {
                default:
                    Log.e(TAG, "feature " + feature + " is not a boolean feature");
                    return false;
            }
        }

        public boolean set(int feature, boolean enable) {
            switch(feature) {
                default:
                    Log.e(TAG, "feature " + feature + " is not a boolean feature");
                    return false;
            }
        }
    }

    private OuvrirHardwareInterface getImpl(Context context) {
        return new LegacyOuvrirHardware();
    }

    public OuvrirHardwareService(Context context) {
        super(context);
        mContext = context;
        mOuvrirHwImpl = getImpl(context);
        publishBinderService(OuvrirContextConstants.OUVRIR_HARDWARE_SERVICE, mService);
    }

    @Override
    public void onBootPhase(int phase) {
        if (phase == PHASE_BOOT_COMPLETED) {
            Intent intent = new Intent("ouvriros.intent.action.INITIALIZE_OUVRIR_HARDWARE");
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            mContext.sendBroadcastAsUser(intent, UserHandle.ALL,
                    "ouvriros.permission.HARDWARE_ABSTRACTION_ACCESS");
        }
    }

    @Override
    public void onStart() {
    }

    private final IBinder mService = new IOuvrirHardwareService.Stub() {

        private boolean isSupported(int feature) {
            return (getSupportedFeatures() & feature) == feature;
        }

        @Override
        public int getSupportedFeatures() {
            mContext.enforceCallingOrSelfPermission(
                    "ouvriros.permission.HARDWARE_ABSTRACTION_ACCESS", null);
            return mOuvrirHwImpl.getSupportedFeatures();
        }

        @Override
        public boolean get(int feature) {
            mContext.enforceCallingOrSelfPermission(
                    "ouvriros.permission.HARDWARE_ABSTRACTION_ACCESS", null);
            if (!isSupported(feature)) {
                Log.e(TAG, "feature " + feature + " is not supported");
                return false;
            }
            return mOuvrirHwImpl.get(feature);
        }

        @Override
        public boolean set(int feature, boolean enable) {
            mContext.enforceCallingOrSelfPermission(
                    "ouvriros.permission.HARDWARE_ABSTRACTION_ACCESS", null);
            if (!isSupported(feature)) {
                Log.e(TAG, "feature " + feature + " is not supported");
                return false;
            }
            return mOuvrirHwImpl.set(feature, enable);
        }
    };
}

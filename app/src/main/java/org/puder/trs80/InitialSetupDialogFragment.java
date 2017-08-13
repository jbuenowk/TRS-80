/*
 * Copyright 2012-2013, Arno Puder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.puder.trs80;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;

import com.google.common.base.Optional;

import org.puder.trs80.async.UiExecutor;
import org.puder.trs80.io.FileDownloader;
import org.puder.trs80.localstore.InitialDownloads;
import org.puder.trs80.localstore.InitialDownloads.Download;
import org.puder.trs80.localstore.LocalStore;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkNotNull;

public class InitialSetupDialogFragment extends DialogFragment {
    private static final String TAG = "InitlStpFgrmnt";

    private DownloadCompletionListener listener;
    private int downloadCounter = 0;
    private final LocalStore localStore;
    private final FileDownloader fileDownloader;
    private final Executor downloadExecutor;
    private final Executor uiExecutor;

    public InitialSetupDialogFragment() {
        localStore = checkNotNull(LocalStore.getDefault());
        fileDownloader = new FileDownloader();
        downloadExecutor = Executors.newSingleThreadExecutor();
        uiExecutor = UiExecutor.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (DownloadCompletionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DownloadCompletionListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        return progressDialog;
    }

    @Override
    public void onDestroyView() {
        /*
         * https://code.google.com/p/android/issues/detail?id=17423
         */
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        setRetainInstance(true);

        // Initialize the downloads of all initial items.
        final Download[] downloads = InitialDownloads.get();
        for (final Download download : downloads) {
            downloadExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    download(download, downloads.length);
                }
            });
        }

        // Since the download executor is a single threaded one, we add another tasks that will
        // let us know when we're done.
        downloadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                uiExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        doneDownloading();
                    }
                });
            }
        });
    }

    /** Downloads the given item. */
    private void download(Download download, int total) {
        onDownloadProgress(++downloadCounter, total);
        String url = download.url;

        Optional<byte[]> data = fileDownloader.download(url, download.fileInZip);
        if (data.isPresent()) {
            // Add a new ROM or entry to the local store.
            if (download.isROM) {
                boolean success = localStore.addRom(download.model,
                        download.destinationFilename,
                        data.get());
                Log.i(TAG, "Adding ROM success: " + success);
            } else {
                boolean success = localStore.addNewEntry(download.model,
                        download.configurationName,
                        download.destinationFilename,
                        data.get());
                Log.i(TAG, "Adding non-ROM success: " + success);
            }
        } else {
            Log.e(TAG, StrUtil.form("Could not load data for '%s'.", url));
        }
    }

    /** Update download progress. */
    private void onDownloadProgress(final int num, final int total) {
        uiExecutor.execute(new Runnable() {
            @Override
            public void run() {
                ((ProgressDialog) getDialog()).setMessage(
                        getString(R.string.downloading, num, total));

            }
        });
    }

    /** Called on the main thread when downloading is done. */
    private void doneDownloading() {
        dismissAllowingStateLoss();
        View root = getActivity().findViewById(R.id.main);
        if (ROMs.hasROMs()) {
            listener.onDownloadCompleted();
        } else {
            Snackbar.make(root, R.string.roms_download_failure_msg,
                    Snackbar.LENGTH_LONG).show();
        }
    }

    interface DownloadCompletionListener {
        void onDownloadCompleted();
    }
}
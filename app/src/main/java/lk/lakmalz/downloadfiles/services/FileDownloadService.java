package lk.lakmalz.downloadfiles.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.webkit.URLUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import lk.lakmalz.downloadfiles.R;
import lk.lakmalz.downloadfiles.models.DownloadFile;
import lk.lakmalz.downloadfiles.utilities.AppDirectory;
import lk.lakmalz.downloadfiles.utilities.Constant;
import lk.lakmalz.downloadfiles.utilities.ConstantExtras;

/**
 * Created by A Lakmal Weerasekara (Lakmalz) on 28/6/17.
 * alrweerasekara@gmail.com
 */

public class FileDownloadService extends IntentService {

    private final AppDirectory saveDirectory;
    private int totalSize, loadedSize;
    private String broadcaster;
    private volatile boolean running = true;
    private volatile boolean killed = false;
    List<DownloadFile> mDownloadFiles;

    private boolean IsRunning;

    public FileDownloadService() {
        super(FileDownloadService.class.getName());
        saveDirectory = new AppDirectory();
        IsRunning = false;
        mDownloadFiles = new ArrayList<>();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        broadcaster = getResources().getString(R.string.service_file_download_broadcaster);
        DownloadFile downloadFile = (DownloadFile) intent.getSerializableExtra(ConstantExtras.BUNDLE_EXTRA_DOWNLOAD_FILE);
        //downloadFile(downloadFile);

        if(!checkInQueue(downloadFile))
        {
            mDownloadFiles.add(downloadFile);
            downlaodNext();
        }
    }

    private void downlaodNext()
    {
        if(!IsRunning && mDownloadFiles.size() > 0)
            downloadFile(mDownloadFiles.get(0));
    }


    private boolean checkInQueue(DownloadFile downloadFile)
    {
        boolean isInQueue = false;

        for(DownloadFile file : mDownloadFiles)
        {
            if(downloadFile.getId() == file.getId())
            {
                isInQueue = true;
                break;
            }
        }

        return isInQueue;
    }

    private void downloadFile(DownloadFile downloadFile) {
        try {
            IsRunning = true;
            URL toDownload = new URL(downloadFile.getUrl());
            HttpURLConnection urlConnection = (HttpURLConnection) toDownload.openConnection();
            urlConnection.setRequestMethod(Constant.METHOD_GET);
            urlConnection.connect();

            boolean success = saveDirectory.makeDirectory();
            if (success) {

                String fileName = URLUtil.guessFileName(toDownload.getFile(), null, null);
                File outFile = new File(saveDirectory.getSdCardRoot(), fileName);
                FileOutputStream fileOutputStream = new FileOutputStream(outFile);

                InputStream inputStream = urlConnection.getInputStream();
                totalSize = urlConnection.getContentLength();

                byte[] buffer = new byte[1024];
                int bufferLength = 0;

                while ((bufferLength = inputStream.read(buffer)) > 0) {

                    fileOutputStream.write(buffer, 0, bufferLength);
                    loadedSize += bufferLength;
                    double val = ((double) loadedSize / totalSize) * 100;
                    broadcastProgress(downloadFile, val);
                }

                fileOutputStream.close();
                /*if (outFile.exists()) {
                    outFile.delete();
                }*/

                //set saved path in external memory
                downloadFile.setSavedPath(outFile.getAbsolutePath());
                broadcastCompleted(downloadFile, 100);

            } else {
                // TODO: 28/6/17 Do something else on failure (Display user friendly message)
            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {

            totalSize = 0;
            loadedSize = 0;
            if (mDownloadFiles != null) {
                mDownloadFiles.remove(0);
            }
            IsRunning = false;
            downlaodNext();
        }
    }

    private void broadcastCompleted(DownloadFile downloadFile, int val) {
        sendBroadcast(downloadFile, val, Constant.DOWNLOAD_SUCCESS);
    }

    private void broadcastProgress(DownloadFile downloadFile, double val) {
        sendBroadcast(downloadFile, val, Constant.DOWNLOAD_PROGRESS);
    }

    private void sendBroadcast(DownloadFile downloadFile, double val, int status) {

        Intent intentBroadCaster = new Intent();
        intentBroadCaster.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intentBroadCaster.setAction(broadcaster);
        intentBroadCaster.putExtra(ConstantExtras.BUNDLE_EXTRA_FILE_STATUS, status);
        intentBroadCaster.putExtra(ConstantExtras.BUNDLE_EXTRA_FILE_PROGRESS, val);
        intentBroadCaster.putExtra(ConstantExtras.BUNDLE_EXTRA_DOWNLOAD_FILE, downloadFile);
        sendBroadcast(intentBroadCaster);

    }
}

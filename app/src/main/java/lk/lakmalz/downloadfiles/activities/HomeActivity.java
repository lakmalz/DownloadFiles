package lk.lakmalz.downloadfiles.activities;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import lk.lakmalz.downloadfiles.R;
import lk.lakmalz.downloadfiles.models.DownloadFile;
import lk.lakmalz.downloadfiles.services.FileDownloadService;
import lk.lakmalz.downloadfiles.utilities.Constant;
import lk.lakmalz.downloadfiles.utilities.ConstantExtras;

/**
 * Created by A Lakmal Weerasekara (Lakmalz) on 28/6/17.
 * alrweerasekara@gmail.com
 */

public class HomeActivity extends AppCompatActivity {

    private Dialog progressDialog;
    private ProgressBar progressBar;
    private FileDownloadReceiver mBroadcastReceiver;
    private TextView txtFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        txtFilePath =  (TextView) findViewById(R.id.txt_file_path);
        initDialogView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        String broadcaster = this.getResources().getString(R.string.service_file_download_broadcaster);
        mBroadcastReceiver = new FileDownloadReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(broadcaster);
        registerReceiver(mBroadcastReceiver, filter);

    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mBroadcastReceiver);
    }

    public void onClickBtnFileDownload(View view) {
        progressDialog.show();
        DownloadFile downloadFile = new DownloadFile();
        downloadFile.setFileType("image");
        downloadFile.setUrl("http://looktime.sandbox2.elegant-media.com/images/8ba154e62e2f08ee379326295a97abe4.png");
        Intent startIntent = new Intent(this, FileDownloadService.class);
        startIntent.putExtra(ConstantExtras.BUNDLE_EXTRA_DOWNLOAD_FILE, downloadFile);
        startService(startIntent);
    }

    private void initDialogView() {
        progressDialog = new Dialog(this, R.style.DialogTheme);
        progressDialog.setContentView(R.layout.widget_download_progress);
        progressDialog.setCancelable(false);
        progressBar = (ProgressBar) progressDialog.findViewById(R.id.progress);

    }

    class FileDownloadReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            int status = intent.getIntExtra(ConstantExtras.BUNDLE_EXTRA_FILE_STATUS, -1);

            switch (status) {
                case Constant.DOWNLOAD_PROGRESS:
                    updateProgress(intent);
                    break;

                case Constant.DOWNLOAD_SUCCESS:
                    updateFileDownloaded(intent);
                    break;

                case Constant.DOWNLOAD_ERROR:
                    // TODO: 28/6/17 should implement when getting error
                    progressBar.setProgress(0);
                    progressDialog.dismiss();
                    break;

                default:
                    break;
            }
        }

        private void updateFileDownloaded(Intent intent) {
            DownloadFile fileDownloaded = (DownloadFile)intent.getSerializableExtra(ConstantExtras.BUNDLE_EXTRA_DOWNLOAD_FILE);
            progressDialog.dismiss();
            txtFilePath.setText(fileDownloaded.getSavedPath());
        }

        private void updateProgress(Intent intent) {
            DownloadFile fileDownloaded = (DownloadFile)intent.getSerializableExtra(ConstantExtras.BUNDLE_EXTRA_DOWNLOAD_FILE);
            Double progress = intent.getDoubleExtra(ConstantExtras.BUNDLE_EXTRA_FILE_PROGRESS, 0);
            progressBar.setProgress(progress.intValue());
        }
    }


}

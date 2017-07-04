package lk.lakmalz.downloadfiles.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import java.util.List;

import lk.lakmalz.downloadfiles.R;
import lk.lakmalz.downloadfiles.models.DownloadFile;
import lk.lakmalz.downloadfiles.utilities.Constant;
import lk.lakmalz.downloadfiles.utilities.ConstantExtras;

/**
 * Created by A Lakmal Weerasekara (Lakmalz) on 4/7/17.
 * alrweerasekara@gmail.com
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private List<DownloadFile> mDataset;
    private Context context;
    private ImageAdapterInterface mCallback;

    public ImageAdapter(List<DownloadFile> dataset, Context context, ImageAdapterInterface callback) {
        this.mDataset = dataset;
        this.context = context;
        this.mCallback = callback;
    }

    @Override
    public ImageAdapter.ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.widget_download_lit_item, parent, false);
        return new ImageViewHolder(row);
    }

    @Override
    public void onBindViewHolder(ImageAdapter.ImageViewHolder holder, int position) {
        DownloadFile downloadFile = mDataset.get(position);
        holder.initReceiver(downloadFile.getId());
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        Button btnDownload;
        ProgressBar progressBar;
        int id = -1;
        FileDownloadReceiver mReceiver = null;

        public ImageViewHolder(View itemView) {
            super(itemView);
            btnDownload = (Button) itemView.findViewById(R.id.btn_download);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progress);
            btnDownload.setOnClickListener(this);
        }

        public void initReceiver(int _id) {
            id = _id;
            String broadCaster = context.getResources().getString(R.string.service_file_download_broadcaster);
            IntentFilter intentFilter = new IntentFilter(broadCaster);
            if (mReceiver != null) {
                unregisterReceiver();
            }

            mReceiver = new FileDownloadReceiver();
            context.registerReceiver(mReceiver, intentFilter);
        }

        private void unregisterReceiver() {
            context.unregisterReceiver(mReceiver);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btn_download) {
             mCallback.onClickDownload(mDataset.get(getAdapterPosition()));
            }
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
                        /*progressBar.setProgress(0);
                        progressDialog.dismiss();*/
                        break;

                    default:
                        break;
                }
            }

            private void updateFileDownloaded(Intent intent) {
                DownloadFile fileDownloaded = (DownloadFile)intent.getSerializableExtra(ConstantExtras.BUNDLE_EXTRA_DOWNLOAD_FILE);
                //progressDialog.dismiss();
                //txtFilePath.setText(fileDownloaded.getSavedPath());
            }

            private void updateProgress(Intent intent) {
                DownloadFile fileDownloaded = (DownloadFile)intent.getSerializableExtra(ConstantExtras.BUNDLE_EXTRA_DOWNLOAD_FILE);
                Double progress = intent.getDoubleExtra(ConstantExtras.BUNDLE_EXTRA_FILE_PROGRESS, 0);
                if (id != -1) {

                    if (id == fileDownloaded.getId()) {
                        progressBar.setProgress(progress.intValue());
                    }
                }
            }
        }

    }

    public interface ImageAdapterInterface {
        void onClickDownload(DownloadFile file);
    }

}

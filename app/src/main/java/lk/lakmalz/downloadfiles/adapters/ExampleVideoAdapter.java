package lk.lakmalz.downloadfiles.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.List;

/**
 * Created by A Lakmal Weerasekara (Lakmalz) on 3/7/17.
 * alrweerasekara@gmail.com
 */

public class ExampleVideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {

    private List<Video> mDataset;
    private Context context;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case

        ImageView imgVideoBanner;
        ImageView imgPlayState;
        ImageButton btnVideoDelete;
        TextView txtTitle;
        RelativeLayout viewProgress;
        ProgressBar prgsProgress;
        RelativeLayout viewPlay;
        RelativeLayout viewVideoInfo;



        VideoDownloadReceiver receiver = null;
        int videoid=-1;
        int svrid=-1;

        public ViewHolder(View v) {
            super(v);

            ButterKnife.bind(this, v);

            imgVideoBanner = (ImageView) v.findViewById(R.id.widget_video_img_preview);
            imgPlayState = (ImageView) v.findViewById(R.id.btn_video_play);
            btnVideoDelete = (ImageButton) v.findViewById(R.id.widget_video_btn_delete);
            txtTitle = (TextView) v.findViewById(R.id.widget_video_lbl_title);
            viewProgress = (RelativeLayout) v.findViewById(R.id.widget_video_view_progress);
            prgsProgress = (ProgressBar) v.findViewById(R.id.widget_video_prgs_progress);
            viewPlay = (RelativeLayout) v.findViewById(R.id.widget_video_view_play);
            viewVideoInfo = (RelativeLayout) v.findViewById(R.id.widget_video_view_info);
        }

        public void initReceiver(int _videoid, int _svrid)
        {
            videoid = _videoid;
            svrid = _svrid;

            String broadcaster = context.getResources().getString(R.string.service_videodownload_broadcaster);

            IntentFilter filter = new IntentFilter(broadcaster);

            if(receiver!=null)
                unregisterReceiver();

            receiver = new VideoDownloadReceiver();
            context.registerReceiver(receiver, filter);
        }

        @OnClick(R.id.widget_video_btn_delete)
        public void onVideoDeleteClicked(View view)
        {
            deleteVideo();
        }

        private void deleteVideo()
        {
            String message = context.getResources().getString(R.string.activity_issue_detail_delete_message);

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder
                    .setTitle(R.string.activity_issue_detail_delete_title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //TOdo
                            VideoRepo vdrepo = new VideoRepo(null);
                            Video video = (Video)vdrepo.findByServerId(svrid);

                            File vfile = new File(video.getDownloadPath());
                            vfile.delete();

                            video.setIsDownloaded(false);
                            video.setDownloadPath(null);
                            vdrepo.update(video);

                            setVideoView(video);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //TOdo
                            dialog.dismiss();
                        }
                    });


            AlertDialog diaglog = builder.create();
            diaglog.show();
        }

        public void unregisterReceiver()
        {
            context.unregisterReceiver(receiver);
        }


        class VideoDownloadReceiver extends BroadcastReceiver {

            @Override
            public void onReceive(Context arg0, Intent arg1) {
                // TODO Auto-generated method stub

                int status = arg1.getIntExtra("status", -1);

                switch (status)
                {
                    case VideoDownloadService.DOWNLOAD_PROCESSING:
                        updateProgress(arg1);
                        break;

                    case VideoDownloadService.DOWNLOAD_SUCCESS:
                        updateScreen(arg1);
                        break;

                    case VideoDownloadService.DOWNLOAD_ERROR:
                        break;

                    default:break;
                }

            }

            private void updateScreen(Intent arg1)
            {
                Video video = (Video)arg1.getSerializableExtra(VideoDownloadService.ARG_FILE);

                if(svrid!=-1) {
                    if (svrid == video.getServerId()) {
                        setVideoView(video);
                    }
                }

            }

            private void updateProgress(Intent arg1)
            {
                int vidid = arg1.getIntExtra(VideoDownloadService.ARG_FILE_ID, -1);

                if(svrid!=-1)
                {
                    if(svrid==vidid)
                    {
                        if(viewProgress.getVisibility() == View.GONE) {
                            viewProgress.setVisibility(View.VISIBLE);
                            viewPlay.setVisibility(View.GONE);
                        }

                        Double progress = arg1.getDoubleExtra(VideoDownloadService.ARG_FILE_PROGRESS, 0);
                        prgsProgress.setProgress(progress.intValue());

                    }
                }


            }

        }

        //-----------------------

        private void setVideoView(Video video)
        {
            viewPlay.setVisibility(View.VISIBLE);
            viewProgress.setVisibility(View.GONE);
            viewVideoInfo.setVisibility(View.GONE);

            if(video.getIsDownloaded()==null || !video.getIsDownloaded()) {
                btnVideoDelete.setVisibility(View.GONE);
                imgPlayState.setBackground(context.getResources().getDrawable(R.drawable.btn_video_download));
            }
            else {
                btnVideoDelete.setVisibility(View.VISIBLE);
                imgPlayState.setBackground(context.getResources().getDrawable(R.drawable.btn_video_play));
            }

            String url = video.getBannerImg_hq();
            Picasso
                    .with(context)
                    .load(url)
                    .placeholder(R.drawable.issue_placeholder) // can also be a drawable
                    .error(R.drawable.issue_placeholder) // will be displayed if the image cannot be loaded
                    .into(imgVideoBanner);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public VideoAdapter(Context _context, List<Video> myDataset) {
        mDataset = myDataset;
        context = _context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public VideoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.widget_video_issue_details, parent, false);
        v.setPadding(25,25,25,25);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Video video = mDataset.get(position);

        holder.initReceiver(video.getId(), video.getServerId());

        String url =  video.getBannerImg_hq();
        Picasso
                .with(context)
                .load(url)
                .placeholder(R.drawable.issue_placeholder) // can also be a drawable
                .error(R.drawable.issue_placeholder) // will be displayed if the image cannot be loaded
                .into(holder.imgVideoBanner);

        if(video.getIsDownloaded()==null || !video.getIsDownloaded()) {
            holder.btnVideoDelete.setVisibility(View.GONE);
            holder.imgPlayState.setBackground(context.getResources().getDrawable(R.drawable.btn_video_download));
        }
        else {
            holder.btnVideoDelete.setVisibility(View.VISIBLE);
            holder.imgPlayState.setBackground(context.getResources().getDrawable(R.drawable.btn_video_play));
        }

        holder.viewProgress.setVisibility(View.GONE);

        holder.txtTitle.setText(video.getTitle());

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}

package lk.lakmalz.downloadfiles.utilities;

import android.os.Environment;

import java.io.File;

/**
 * Created by A Lakmal Weerasekara (Lakmalz) on 28/6/17.
 * alrweerasekara@gmail.com
 */

public class AppDirectory {

    private File sdCardRoot;

    public AppDirectory() {
        sdCardRoot = Environment.getExternalStorageDirectory();
        sdCardRoot = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Constant.DOWNLOAD_LOCATION);
    }

    public boolean makeDirectory() {

        boolean success = true;
        if (!sdCardRoot.exists())
            success = sdCardRoot.mkdir();

        return success;
    }

    public File getSdCardRoot() {
        return sdCardRoot;
    }
}

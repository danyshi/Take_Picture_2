package com.example.Take_Picture_2;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import javax.xml.transform.Result;
import java.io.File;

public class MyActivity extends Activity {
    private android.R.bool IsThereAnAppToTakePictures()
    {
        Intent intent = new Intent(MediaStore.ActionImageCapture);
        IList<ResolveInfo> availableActivities = PackageManager.QueryIntentActivities(intent, PackageInfoFlags.MatchDefaultOnly);
        return availableActivities != null && availableActivities.Count > 0;
    }

    private void CreateDirectoryForPictures()
    {
        _dir = new File(Environment.GetExternalStoragePublicDirectory(Environment.DirectoryPictures), "CameraAppDemo");
        if (!_dir.Exists())
        {
            _dir.Mkdirs();
        }
    }
    private void TakeAPicture(object sender, EventArgs eventArgs)
    {
        Intent intent = new Intent(MediaStore.ActionImageCapture);

        _file = new File(_dir, String.Format("myPhoto_{0}.jpg", Guid.NewGuid()));

        intent.PutExtra(MediaStore.ExtraOutput, Uri.FromFile(_file));

        StartActivityForResult(intent, 0);
    }
    protected override void OnActivityResult(int requestCode, Result resultCode, Intent data)
    {
        base.OnActivityResult(requestCode, resultCode, data);

        // make it available in the gallery
        Intent mediaScanIntent = new Intent(Intent.ActionMediaScannerScanFile);
        Uri contentUri = Uri.FromFile(_file);
        mediaScanIntent.SetData(contentUri);
        SendBroadcast(mediaScanIntent);

        // display in ImageView. We will resize the bitmap to fit the display
        // Loading the full sized image will consume to much memory
        // and cause the application to crash.
        int height = _imageView.Height;
        int width = Resources.DisplayMetrics.WidthPixels;
        using (Bitmap bitmap = _file.Path.LoadAndResizeBitmap(width, height))
        {
            _imageView.SetImageBitmap(bitmap);
        }
    }
    public static class BitmapHelpers
    {
        public static Bitmap LoadAndResizeBitmap(this android.R.string fileName, int width, int height)
        {
            // First we get the the dimensions of the file on disk
            BitmapFactory.Options options = new BitmapFactory.Options { InJustDecodeBounds = true };
            BitmapFactory.DecodeFile(fileName, options);

            // Next we calculate the ratio that we need to resize the image by
            // in order to fit the requested dimensions.
            int outHeight = options.OutHeight;
            int outWidth = options.OutWidth;
            int inSampleSize = 1;

            if (outHeight > height || outWidth > width)
            {
                inSampleSize = outWidth > outHeight
                        ? outHeight / height
                        : outWidth / width;
            }

            // Now we will load the image and have BitmapFactory resize it for us.
            options.InSampleSize = inSampleSize;
            options.InJustDecodeBounds = false;
            Bitmap resizedBitmap = BitmapFactory.DecodeFile(fileName, options);

            return resizedBitmap;
        }
    }
    @Override
    protected override void OnCreate(Bundle bundle)
    {
        base.OnCreate(bundle);
        SetContentView(Resource.Layout.Main);

        if (IsThereAnAppToTakePictures())
        {
            CreateDirectoryForPictures();

            Button button = FindViewById<Button>(Resource.Id.myButton);
            _imageView = FindViewById<ImageView>(Resource.Id.imageView1);

            button.Click += TakeAPicture;
        }
    }

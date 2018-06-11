package com.example.murg.googleocrtranslator;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Base64;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import com.theartofdev.edmodo.cropper.CropImage;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Locale;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.opencv.core.CvType.CV_32F;
import static org.opencv.core.CvType.CV_32SC1;
import static org.opencv.core.CvType.CV_8U;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.CvType.CV_8UC3;

public class MainActivity extends AppCompatActivity {

    /* Used to load the 'native-lib' library on application startup. */
    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java3");
    }

    public static final int CAMERA_REQUEST_CODE = 0;
    public static final int GALLERY_REQUEST_CODE = 2;
    public static final int CROP_REQUEST_CODE = 1;


    public static final String INIT = "init";
    public static final String MY_PREFS = "myPrefs";
    public static final String IMAGES_MAT = "imagesMat";

    public static SharedPreferences sharedPreferences;
    private static ProgressDialog pDialog;

    private static Mat readMatImg;
    private static Mat readMatClass;
    private static Mat srcImg;

    private static boolean saveImgTostorage = false;
    public static double shrinkFactor = 0.5;

    TextView tv;
    ImageView slika;

    Intent GalleryIntent;
    Intent CropIntent;
    Intent CameraIntent;
    File capturedPhotoFile;
    Uri capturedPhotoUri;

    final int RequestPermissionCodeCamera = 1;
    final int RequestPermissionCodeStorageWrite = 2;
    final int RequestPermissionCodeStorageRead = 3;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().hide();

        FloatingActionButton fabCamera = (FloatingActionButton) findViewById(R.id.fab_camera);
        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraOpen();

            }
        });

        FloatingActionButton fabGallery = (FloatingActionButton) findViewById(R.id.fab_gallery);
        fabGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GalleryOpen();

            }
        });

        FloatingActionButton fabTranslate = (FloatingActionButton) findViewById(R.id.fab_translate);
        fabTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TranslateActivity.class);
                startActivity(intent);

            }
        });


        final TextView setScaleTextView = (TextView) findViewById(R.id.set_scale_text);

        SeekBar setScaleSeekBar = (SeekBar)findViewById(R.id.seek_bar);
        setScaleSeekBar.setProgress(4);
        setScaleSeekBar.incrementProgressBy(1);
        setScaleSeekBar.setMax(10);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setScaleSeekBar.setMin(1);
        }


        setScaleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if(progress == 0) {
                    shrinkFactor = 0.1;
                } else {
                    shrinkFactor = (double) progress/10;
                }

                setScaleTextView.setText(Double.toString(shrinkFactor));
                System.out.println(shrinkFactor);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });






        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
        if(permissionCheck == PackageManager.PERMISSION_DENIED)
            RequestRuntimePermissionCamera();

        permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionCheck == PackageManager.PERMISSION_DENIED)
            RequestRuntimePermissionWriteStorage();

        permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if(permissionCheck == PackageManager.PERMISSION_DENIED)
            RequestRuntimePermissionReadStorage();




        // Example of a call to a native method
        tv = (TextView) findViewById(R.id.sample_text);
        //tv.setText(stringFromJNI());

        slika = (ImageView) findViewById(R.id.slika);



        sharedPreferences = getSharedPreferences(MY_PREFS, Context.MODE_PRIVATE);
        readMatImg = new Mat(2147, 750, CvType.CV_32F);
        readMatClass = new Mat(2147, 1, CvType.CV_32S);
        srcImg = null;

        try {
            srcImg  = Utils.loadResource(this, R.drawable.test);
        } catch (IOException e) {
            e.printStackTrace();
        }




        /* Check if OpenCV has loaded correctly */
        if (!OpenCVLoader.initDebug()) {
            Log.e(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), not working.");
        } else {
            Log.d(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), working.");
        }



        new LoadLearningData().execute("");


    }



    private void RequestRuntimePermissionCamera() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CAMERA)) {
            Toast.makeText(this, "Camera permission allows us to access camera.", Toast.LENGTH_SHORT).show();

        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, RequestPermissionCodeCamera);
        }

    }


    private void RequestRuntimePermissionWriteStorage() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "Write storage permission allows us to write data to storage.", Toast.LENGTH_SHORT).show();

        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, RequestPermissionCodeStorageWrite);
        }

    }



    private void RequestRuntimePermissionReadStorage() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "Read storage permission allows us to read data from storage.", Toast.LENGTH_SHORT).show();

        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, RequestPermissionCodeStorageRead);
        }

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCodeCamera: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted.", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show();

                }
                break;
            }

            case RequestPermissionCodeStorageWrite: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted.", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show();

                }
                break;
            }

            case RequestPermissionCodeStorageRead: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted.", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show();

                }
                break;
            }
        }
    }

    /**
     * Parses OpenCV Mat to the right form and saves the matrix to the SharedPreferences.
     * @param mat matrix we want to save.
     */
    public void saveMatToPref(Mat mat) {

        if (mat.isContinuous()) {

            int size = (int)( mat.total() * mat.channels() );
            float[] data = new float[ size ];

            byte[] byteArray = new byte[ size ];
            mat.get(0, 0, data);
            byteArray = floatArrayToByteArray(data);
            String dataString = new String(Base64.encode(byteArray, Base64.DEFAULT));

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(IMAGES_MAT, dataString);
            editor.apply();
            editor.commit();

        } else {
            System.out.println("Mat not continuous!");
        }
    }

    /**
     * Parses an array of bytes to the array of floats.
     * @param bytes byte array we want to parse.
     * @return parsed float array.
     */
    private static float[] toFloatArray(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        FloatBuffer fb = buffer.asFloatBuffer();
        float[] floatArray = new float[fb.limit()];
        fb.get(floatArray);
        return floatArray;
    }

    /**
     * Parses an array of floats to the array of bytes
     * @param values float array we want to parse.
     * @return parsed byte array.
     */
    public static byte[] floatArrayToByteArray(float[] values){
        ByteBuffer buffer = ByteBuffer.allocate(4 * values.length);

        for (float value : values)
            buffer.putFloat(value);

        return buffer.array();
    }


    private void GalleryOpen() {
        /* Check again */
        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
        if(permissionCheck == PackageManager.PERMISSION_DENIED)
            RequestRuntimePermissionCamera();

        permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionCheck == PackageManager.PERMISSION_DENIED)
            RequestRuntimePermissionWriteStorage();

        permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if(permissionCheck == PackageManager.PERMISSION_DENIED)
            RequestRuntimePermissionReadStorage();

        GalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(GalleryIntent,"Select Image from Gallery"),GALLERY_REQUEST_CODE);
    }


    private void CameraOpen() {
        /* Check again */
        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
        if(permissionCheck == PackageManager.PERMISSION_DENIED)
            RequestRuntimePermissionCamera();

        permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionCheck == PackageManager.PERMISSION_DENIED)
            RequestRuntimePermissionWriteStorage();

        permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if(permissionCheck == PackageManager.PERMISSION_DENIED)
            RequestRuntimePermissionReadStorage();

        CameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        capturedPhotoFile = new File(Environment.getExternalStorageDirectory(), "file" + String.valueOf(System.currentTimeMillis()) + ".png");
        capturedPhotoUri = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider", capturedPhotoFile);

        CameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedPhotoUri);
        CameraIntent.putExtra("return-data",true);
        startActivityForResult(CameraIntent, CAMERA_REQUEST_CODE);

    }

    private void CropImage() {

        CropIntent = CropImage.activity(capturedPhotoUri).getIntent(this);
        startActivityForResult(CropIntent, CROP_REQUEST_CODE);

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            CropImage();

        } else if(requestCode == GALLERY_REQUEST_CODE) {
            if(data != null) {
                capturedPhotoUri = data.getData();
                CropImage();

            }

        } else if(requestCode == CROP_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();


                Bitmap bitmap = null;
                try {

                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);

/*
                    if((bitmap.getWidth() < 1000) || (bitmap.getHeight() < 1000)) {
                        shrinkFactor = 1.0;

                     }
*/


                    Bitmap resized = Bitmap.createScaledBitmap(bitmap,(int)(bitmap.getWidth()*shrinkFactor), (int)(bitmap.getHeight()*shrinkFactor), true);
                    srcImg = new Mat (resized.getHeight(), resized.getWidth(), CV_8UC3);
                    srcImg.convertTo(srcImg , CV_8UC3);
                    Utils.bitmapToMat(resized, srcImg);
                    File filePath = new File(Environment.getExternalStorageDirectory(), "cropped" + String.valueOf(System.currentTimeMillis()) + ".png");

                    if(saveImgTostorage) {
                        FileOutputStream fileOutputStream = null;
                        try {
                            fileOutputStream = new FileOutputStream(filePath);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);

                        } catch (Exception e) {

                            e.printStackTrace();

                        } finally {

                            try {
                                fileOutputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }


                    new TrainModelAndTest().execute("");


                } catch (IOException e) {
                    e.printStackTrace();
                }


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "Error: " + error + " Please try again.", Toast.LENGTH_SHORT).show();
            }

        }
    }





    @SuppressLint("StaticFieldLeak")
    private class TrainModelAndTest extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {

            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Detecting text. Please wait.");
            pDialog.setCancelable(false);
            pDialog.show();


        }

        @Override
        protected String doInBackground(String... params) {

            String detectedText = "";
            System.out.println(srcImg);
            System.out.println(srcImg.getNativeObjAddr());

            if (readMatClass != null && readMatImg != null && srcImg != null) {
                detectedText = trainAndDetect(srcImg.getNativeObjAddr(), readMatClass.getNativeObjAddr(), readMatImg.getNativeObjAddr());

            } else {
                Toast.makeText(MainActivity.this, "One of the parameters was not loaded successfully. Please try again.", Toast.LENGTH_SHORT).show();

            }


            return detectedText;
        }


        @Override
        protected void onPostExecute(String result) {

            if (pDialog.isShowing())
                pDialog.dismiss();

            System.out.println(result);

            switch (result) {
                case "":
                    Toast.makeText(MainActivity.this, "No text detected. Please try again.", Toast.LENGTH_SHORT).show();

                    break;
                case "error: one of matrices not defined!":
                    Toast.makeText(MainActivity.this, "One of the parameters was not loaded successfully. Please try again.", Toast.LENGTH_SHORT).show();

                    break;
                case "error: one of matrices is not continuous!":
                    Toast.makeText(MainActivity.this, "One of the parameters was not loaded successfully. Please try again.", Toast.LENGTH_SHORT).show();

                    break;
                default:

                    System.out.println(result);
                    System.out.println(result.toLowerCase());

                    Intent intent = new Intent(MainActivity.this, TranslateActivity.class);
                    intent.putExtra("detectedText", result.toLowerCase());
                    startActivity(intent);

                    break;
            }

        }

        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            GalleryOpen();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    /**
     * LoadLearningData is an async class that loads classifications.xml and images.xml to OpenCV Mat matrices.
     */
    @SuppressLint("StaticFieldLeak")
    private class LoadLearningData extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {

            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Loading classifications. Please wait.");
            pDialog.setCancelable(false);
            pDialog.show();


        }


        /**
         * Loads classifications.xml and images.xml to OpenCV Mat matrices readMatImg and readMatClass.
         * @param params init params.
         * @return program done notice.
         */
        @Override
        protected String doInBackground(String... params) {

            if(sharedPreferences.getBoolean(INIT, true)) {
                try {
                    InputStream is = getAssets().open("classifications.xml");

                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(is);

                    Element element = doc.getDocumentElement();
                    element.normalize();

                    NodeList nodelist = doc.getElementsByTagName("classifications");

                    for( int i = 0 ; i<nodelist.getLength() ; i++ ) {
                        Node node = nodelist.item(i);

                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            Element classEl = (Element) node;


                            String rowsStr = classEl.getElementsByTagName("rows").item(0).getTextContent();
                            String colsStr = classEl.getElementsByTagName("cols").item(0).getTextContent();
                            String dataStr = classEl.getElementsByTagName("data").item(0).getTextContent();

                            int rows = Integer.parseInt(rowsStr);
                            int cols = Integer.parseInt(colsStr);
                            int type = CvType.CV_32S;

                            Scanner scanner = new Scanner(dataStr);
                            scanner.useLocale(Locale.US);

                            readMatClass = new Mat(rows, cols, type);

                            int isInt[] = new int[1];
                            for (int r = 0; r < rows; r++) {
                                for (int c = 0; c < cols; c++) {
                                    if (scanner.hasNextInt()) {
                                        isInt[0] = scanner.nextInt();
                                    } else {
                                        isInt[0] = 0;
                                        System.err.println("Unmatched number of int value at rows=" + r + " cols=" + c);
                                    }
                                    readMatClass.put(r, c, isInt);
                                }
                            }

                            scanner.close();

                        }
                    }




                } catch (Exception e) {e.printStackTrace();}


                try {
                    InputStream is = getAssets().open("images.xml");

                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(is);

                    Element element = doc.getDocumentElement();
                    element.normalize();

                    NodeList nodelist = doc.getElementsByTagName("images");

                    for( int i = 0 ; i<nodelist.getLength() ; i++ ) {
                        Node node = nodelist.item(i);

                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            Element classEl = (Element) node;


                            String rowsStr = classEl.getElementsByTagName("rows").item(0).getTextContent();
                            String colsStr = classEl.getElementsByTagName("cols").item(0).getTextContent();
                            String dataStr = classEl.getElementsByTagName("data").item(0).getTextContent();

                            int rows = Integer.parseInt(rowsStr);
                            int cols = Integer.parseInt(colsStr);
                            int type = CvType.CV_32F;

                            Scanner scanner = new Scanner(dataStr);
                            scanner.useLocale(Locale.US);

                            readMatImg = new Mat(rows, cols, type);

                            float fs[] = new float[1];
                            for( int r=0 ; r<rows ; r++ ) {
                                for( int c=0 ; c<cols ; c++ ) {
                                    if( scanner.hasNextFloat() ) {
                                        fs[0] = scanner.nextFloat();
                                    }
                                    else {
                                        fs[0] = 0;
                                        System.err.println("Unmatched number of float value at rows = "+ r + " cols = " + c);
                                    }
                                    readMatImg.put(r, c, fs);
                                }
                            }

                            scanner.close();

                        }
                    }

                    saveMatToPref(readMatImg);

                } catch (Exception e) {e.printStackTrace();}

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(INIT, false);
                editor.apply();
                editor.commit();


            } else {
                String values = sharedPreferences.getString(IMAGES_MAT, "");

                byte[] data = Base64.decode(values, Base64.DEFAULT);
                readMatImg = new Mat(2147, 750, CvType.CV_32F);
                float[] floatArray = toFloatArray(data);
                readMatImg.put(0, 0, floatArray);


                try {
                    InputStream is = getAssets().open("classifications.xml");

                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(is);

                    Element element = doc.getDocumentElement();
                    element.normalize();

                    NodeList nodelist = doc.getElementsByTagName("classifications");

                    for( int i = 0 ; i<nodelist.getLength() ; i++ ) {
                        Node node = nodelist.item(i);

                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            Element classEl = (Element) node;


                            String rowsStr = classEl.getElementsByTagName("rows").item(0).getTextContent();
                            String colsStr = classEl.getElementsByTagName("cols").item(0).getTextContent();
                            String dataStr = classEl.getElementsByTagName("data").item(0).getTextContent();

                            int rows = Integer.parseInt(rowsStr);
                            int cols = Integer.parseInt(colsStr);
                            int  type = CvType.CV_32S;

                            Scanner scanner = new Scanner(dataStr);
                            scanner.useLocale(Locale.US);

                            readMatClass = new Mat(rows, cols, type);

                            int isInt[] = new int[1];
                            for (int r = 0; r < rows; r++) {
                                for (int c = 0; c < cols; c++) {
                                    if (scanner.hasNextInt()) {
                                        isInt[0] = scanner.nextInt();
                                    } else {
                                        isInt[0] = 0;
                                        System.err.println("Unmatched number of int value at rows=" + r + " cols=" + c);
                                    }
                                    readMatClass.put(r, c, isInt);
                                }
                            }

                            scanner.close();

                        }
                    }


                } catch (Exception e) {e.printStackTrace();}


            }
            System.out.println(readMatClass);
            System.out.println(readMatImg);

            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {

            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }

        }



        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */

    public native String stringFromJNI();
    public native String trainAndDetect(long srcImgAddr, long classAddr, long imagesAddr);
}

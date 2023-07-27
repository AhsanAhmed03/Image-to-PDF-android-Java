package com.example.imagetopdf;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUSEST_CODE_PICK_IMAGES = 1;
    private ArrayList<String> selectedImagePath = new ArrayList<>();
    Button img_pick_btn, pdfmake_Btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        img_pick_btn = findViewById(R.id.img_pick_btn);
        pdfmake_Btn = findViewById(R.id.generatePdf_btn);

        img_pick_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pick_images();
            }
        });

        pdfmake_Btn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                generatePdf();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void generatePdf(){
        PdfDocument document = new PdfDocument();
        for (int i = 0; i<selectedImagePath.size(); i++){
            Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath.get(i));
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder
                    (bitmap.getWidth(),bitmap.getHeight(),1).create();
            PdfDocument.Page page = document.startPage(pageInfo);

            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();
            paint.setColor(Color.parseColor("#ffffff"));
            canvas.drawPaint(paint);

            bitmap = Bitmap.createScaledBitmap(bitmap,bitmap.getWidth(),bitmap.getHeight(),true);
            canvas.drawBitmap(bitmap,0,0,null);
            document.finishPage(page);
        }
        File sdcard = Environment.getExternalStorageDirectory();
        File dir = new File(sdcard.getAbsolutePath() + "/Download/Image to PDF/");

        if (dir.exists()){
            File file = new File(dir,"FileName"+".pdf");

            try {

                document.writeTo(Files.newOutputStream(file.toPath()));
                document.close();
                Toast.makeText(this, "IMAGES CONVERTED TO PDF...", Toast.LENGTH_SHORT).show();
            }catch (Exception e){
                Toast.makeText(this, "Error in creating file..." +e, Toast.LENGTH_SHORT).show();
            }
        }else {
            dir.mkdir();
            File file = new File(dir,"FileName"+".pdf");
            try {

                document.writeTo(Files.newOutputStream(file.toPath()));
                document.close();
                Toast.makeText(this, "IMAGES CONVERTED TO PDF...", Toast.LENGTH_SHORT).show();
            }catch (Exception e){
                Toast.makeText(this, "Error in creating file..." +e, Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void pick_images(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        startActivityForResult(intent,REQUSEST_CODE_PICK_IMAGES);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode
            , @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUSEST_CODE_PICK_IMAGES
                && requestCode == RESULT_OK){
            if (data != null){
                ClipData clipData = data.getClipData();
                if (clipData != null){
                    // Multiple Images are Selected

                    for (int i = 0; i<clipData.getItemCount(); i++){
                        Uri imageUri = clipData.getItemAt(1).getUri();
                        String imagePath = getImagepathfromUri(imageUri);
                        selectedImagePath.add(imagePath);
                    }
                }else {
                    //Single Image Selected
                    Uri imageUri = data.getData();
                    String imagePath = getImagepathfromUri(imageUri);
                    selectedImagePath.add(imagePath);
                }
            }
        }
    }

    private String getImagepathfromUri(Uri imageUri){
        String imagePath = null;

        if (imageUri != null){
            ContentResolver contentResolver = getContentResolver();
            Cursor cursor = contentResolver.query(imageUri,null,null,
                    null,null);
            if (cursor != null){
                int coloumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                if (coloumnIndex != -1){
                    imagePath = cursor.getString(coloumnIndex);
                }
            }
            cursor.close();
        }
        return imagePath;
    }
}
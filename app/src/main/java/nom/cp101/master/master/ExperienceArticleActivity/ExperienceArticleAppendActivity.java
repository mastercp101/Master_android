package nom.cp101.master.master.ExperienceArticleActivity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import nom.cp101.master.master.Account.MyCourse.Apply;
import nom.cp101.master.master.Main.Common;
import nom.cp101.master.master.Main.MyTask;
import nom.cp101.master.master.R;

import static java.security.AccessController.getContext;
import static nom.cp101.master.master.Main.Common.showToast;

/**
 * Created by chunyili on 2018/5/13.
 */

public class ExperienceArticleAppendActivity extends AppCompatActivity {
    String TAG = "Experience Article Append Activity";
    ImageView article_image;
    EditText article_content;
    Button article_submit;
    String content;
    private static final int REQ_TAKE_PICTURE = 0;
    private static final int REQ_PICK_PICTURE = 1;
    private static final int REQ_CROP_PICTURE = 2;
    private Uri contentUri, croppedImageUri;
    private Bitmap picture;
    private byte[] image;
    String user_id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.experience_article_add);
        findView();
        article_image();
        submitOnClick();
    }

    private void article_image() {
        article_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pictureDialog();
            }
        });
    }

    private void pictureDialog() {
        List<String> dialogList = new ArrayList<String>();
        dialogList.add(getString(R.string.camera));
        dialogList.add(getString(R.string.albums));
        dialogList.add(getString(R.string.cancel));

        final CharSequence[] list = dialogList.toArray(new String[dialogList.size()]);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getString(R.string.picture));

        alert.setItems(list, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int item) {
                //Take Picture
                if(list[item] == list[0]){
                    Context context = ExperienceArticleAppendActivity.this;
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File file = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    file = new File(file, "picture.jpg");
                    contentUri = FileProvider.getUriForFile(context,getPackageName() + ".provider", file);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
                    if (isIntentAvailible(context, intent)) {
                        startActivityForResult(intent, REQ_TAKE_PICTURE);
                    } else {
                        Common.showToast(context,"There is no camera App");
                    }
                    //Pick Picture
                }else if(list[item] == list[1]){
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent,REQ_PICK_PICTURE);
                }
            }
        });
        AlertDialog alertDialog = alert.create();
        alertDialog.show();
    }
    private boolean isIntentAvailible(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(resultCode == RESULT_OK){
            switch(requestCode){
                case REQ_TAKE_PICTURE:
                    crop(contentUri);
                    break;
                case REQ_PICK_PICTURE:
                    Uri uri = intent.getData();
                    crop(uri);
                    break;
                case REQ_CROP_PICTURE:
                    try {
                        picture = BitmapFactory.decodeStream(
                                getContentResolver().openInputStream(croppedImageUri));
                        article_image.setImageBitmap(picture);
                        article_image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        picture.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        image = out.toByteArray();
                    } catch (FileNotFoundException e) {
                        Log.e("main", e.toString());
                    }

                    break;
            }
        }
    }

    private void crop(Uri uri) {
        File file = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        file = new File(file,"picture_cropped_picture.jpg");
        croppedImageUri = Uri.fromFile(file);
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            cropIntent.setDataAndType(uri,"image/*");
            cropIntent.putExtra("crop","true");
            cropIntent.putExtra("aspectX", 1200);
            cropIntent.putExtra("aspectY", 900);
            cropIntent.putExtra("outputX", 900);
            cropIntent.putExtra("outputY", 675);
            cropIntent.putExtra("scale", true);
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, croppedImageUri);
            cropIntent.putExtra("return-data", true);
            startActivityForResult(cropIntent, REQ_CROP_PICTURE);
        }
        catch (ActivityNotFoundException anfe) {
            showToast(this, "This device doesn't support the crop action!");
        }
    }

    private void submitOnClick() {
        article_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                content = article_content.getText().toString().trim();
                Context context = ExperienceArticleAppendActivity.this;
                user_id = Common.getUserName(context);
                if(article_image.getDrawable() == null){
                    Common.showToast(context,"Image can not be empty");
                }else if(content.isEmpty()){
                    Common.showToast(context,"Content can not be empty");
                }else{
                    int photo_id = insertImage(user_id);
                    insertExperienceArticle(user_id,content,photo_id);
                }
            }
        });
    }

    private void findView() {
        article_image = findViewById(R.id.experience_article_image);
        article_content = findViewById(R.id.experience_article_content);
        article_submit = findViewById(R.id.experience_article_submit);
    }

    private int insertImage(String user_id) {
        if (Common.networkConnected(this)) {
            String url = Common.URL + "/photoServlet";
            String imageBase64 = Base64.encodeToString(image, Base64.DEFAULT);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "insert");
            jsonObject.addProperty("user_id",user_id);
            jsonObject.addProperty("photo", imageBase64);
            int photo_id = 0;
            try {
                String result = new MyTask(url, jsonObject.toString()).execute().get();
                photo_id = Integer.valueOf(result);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (photo_id == 0) {
//                Common.showToast(getActivity(), R.string.msg_InsertFail);
                return  photo_id;
            } else {
//                Common.showToast(getActivity(), R.string.msg_InsertSuccess);
                return photo_id;
            }
        } else {
            showToast(this, R.string.msg_NoNetwork);
            return 0;
        }
    }

    public int insertExperienceArticle(String user_id,String content,int photo_id){
        if (Common.networkConnected(this)) {
            String url = Common.URL + "/ExperienceArticleServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("experienceArticle", "insertExperienceArticle");
            jsonObject.addProperty("user_id", user_id);
            jsonObject.addProperty("content",content);
            jsonObject.addProperty("photo_id",photo_id);
            int id = 0;
            try {
                String result = new MyTask(url, jsonObject.toString()).execute().get();
                id = Integer.valueOf(result);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (id == 0 ) {
                Log.d(TAG,"新增文章失敗");
                return id;
            } else {
                Log.d(TAG,"新增文章成功");
                return  id;
            }
        } else {
            Log.d(TAG,"沒有網路");
            Common.showToast(this, R.string.msg_NoNetwork);
            return 0;
        }
    }


}

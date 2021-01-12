package com.kalu.office;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import lib.kalu.mupdf.android.signature.SignatureView;

public class SignActivity extends Activity {

//    private Button mSaveButton, mClearButton;
    private SignatureView mSignaturePad;
    private ImageView iv_clear,iv_commit;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);
        findViews();
        init();
    }




    protected void findViews() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

//        mSaveButton = (Button) findViewById(R.id.button);
//        mClearButton = (Button) findViewById(R.id.clear);
        iv_clear= (ImageView) findViewById(R.id.iv_clear);
        iv_commit= (ImageView) findViewById(R.id.iv_commit);
        mSignaturePad = (SignatureView) findViewById(R.id.signature_pad);
    }


    protected void init() {
        setTitle("签名");

        iv_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSignaturePad.clear();
            }
        });

        iv_commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String signatureBitmap = mSignaturePad.createSignatureBitmap();

                ImageView image = findViewById(R.id.bitmap);
                image.setImageURI(Uri.parse(signatureBitmap));

//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                signatureBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
//                byte[] bytes=baos.toByteArray();
//
//                Bundle b = new Bundle();
//                b.putByteArray("bitmap", bytes);
//                Intent mIntent = new Intent();
//                mIntent.putExtras(b);
//                // 设置结果，并进行传送
//                SignActivity.this.setResult(0, mIntent);
//                SignActivity.this.finish();
            }
        });



        mSignaturePad.setOnSignedListener(new SignatureView.OnSignedListener() {
            @Override
            public void onSigned() {
//                mSaveButton.setEnabled(true);
                iv_clear.setEnabled(true);
            }

            @Override
            public void onClear() {
//                mSaveButton.setEnabled(false);
                iv_clear.setEnabled(false);
            }
        });
//        mClearButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mSignaturePad.clear();
//            }
//        });


    }
    @Override
    public void onBackPressed() {
        finish();
    }

}

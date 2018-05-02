package com.android.nearbylocation.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.nearbylocation.R;

/**
 * Created by PC on 12-Jul-16.
 */
public class DialogInfo extends Dialog {

    Context context;
    TextView tv_title;
    TextView tv_1;
    TextView tv_2;
    TextView tv_3;
    TextView tv_4;
    TextView tv_5;

    public TextView getTv_ok() {
        return tv_ok;
    }

    public void setTv_ok(TextView tv_ok) {
        this.tv_ok = tv_ok;
    }

    TextView tv_ok;

    public TextView getTv_title() {
        return tv_title;
    }

    public void setTv_title(TextView tv_title) {
        this.tv_title = tv_title;
    }

    public TextView getTv_1() {
        return tv_1;
    }

    public void setTv_1(TextView tv_1) {
        this.tv_1 = tv_1;
    }

    public TextView getTv_2() {
        return tv_2;
    }

    public void setTv_2(TextView tv_2) {
        this.tv_2 = tv_2;
    }

    public TextView getTv_3() {
        return tv_3;
    }

    public void setTv_3(TextView tv_3) {
        this.tv_3 = tv_3;
    }

    public TextView getTv_4() {
        return tv_4;
    }

    public void setTv_4(TextView tv_4) {
        this.tv_4 = tv_4;
    }

    public TextView getTv_5() {
        return tv_5;
    }

    public void setTv_5(TextView tv_5) {
        this.tv_5 = tv_5;
    }

    public DialogInfo(Context context) {
        super(context);

        this.context = context;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        setCanceledOnTouchOutside(false);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        getWindow().setLayout(WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        setContentView(R.layout.dialog_info);

        tv_title = findViewById(R.id.tv_title);
        tv_1 = findViewById(R.id.tv_1);
        tv_2 = findViewById(R.id.tv_2);
        tv_3 = findViewById(R.id.tv_3);
        tv_4 = findViewById(R.id.tv_4);
        tv_5 = findViewById(R.id.tv_5);

        tv_ok =  findViewById(R.id.tv_ok);

        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

    }


}

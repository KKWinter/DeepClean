package com.ddong.qingjie.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ddong.qingjie.R;


public class FragmentItemView extends RelativeLayout {

    private Drawable isfinish;
    private String des;
    private String count;

    public FragmentItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        initUI(context);
    }

    public FragmentItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
        initUI(context);
    }

    public FragmentItemView(Context context) {
        super(context);
        initUI(context);
    }

    private void initUI(Context context) {
        //单独抽取出来的 xml--->view

        View.inflate(context, R.layout.fragment_item_view, this);

        ImageView iv_isfinish = (ImageView) findViewById(R.id.iv_isfinish);
        TextView tv_des = (TextView) findViewById(R.id.tv_des);
        TextView tv_count = (TextView) findViewById(R.id.tv_count);

        iv_isfinish.setImageDrawable(isfinish);
        tv_des.setText(des);
        tv_count.setText(count);

    }

    /**
     * @param attrs 包含了属性名称和属性值的set集合
     */
    private void initAttrs(Context context, AttributeSet attrs) {


        TypedArray typeArray = context.obtainStyledAttributes(attrs, R.styleable.FragmentItemView);

        isfinish = typeArray.getDrawable(R.styleable.FragmentItemView_isfinish);
        des = typeArray.getString(R.styleable.FragmentItemView_des);
        count = typeArray.getString(R.styleable.FragmentItemView_count);
    }
}
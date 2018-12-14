package com.catchgift.trashclear.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.catchgift.trashclear.R;


public class FragmentTitleView extends RelativeLayout {

    private boolean isMenu = true;

    public FragmentTitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        initUI(context);
    }

    public FragmentTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
        initUI(context);
    }

    public FragmentTitleView(Context context) {
        super(context);
        initUI(context);
    }

    private void initUI(final Context context) {
        View.inflate(context, R.layout.fragment_title_view, this);

        final Activity activity = (Activity) context;

        ImageView iv_menu = (ImageView) findViewById(R.id.iv_menu);
        ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
        ImageView iv_setup = (ImageView) findViewById(R.id.iv_setup);
        TextView tv_name = (TextView) findViewById(R.id.tv_name);
        iv_setup.setVisibility(INVISIBLE);

        if (isMenu) {
            iv_menu.setVisibility(INVISIBLE);
            iv_back.setVisibility(INVISIBLE);
        } else {
            iv_menu.setVisibility(INVISIBLE);
            iv_back.setVisibility(VISIBLE);
            tv_name.setVisibility(View.INVISIBLE);
        }

        iv_menu.setOnClickListener(new OnClickListener() {


            @Override
            public void onClick(View v) {

            }
        });

        iv_setup.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        iv_back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });
    }

    /**
     * @param attrs	包含了属性名称和属性值的set集合
     */
    private void initAttrs(Context context, AttributeSet attrs) {

        //这里取得declare-styleable集合
        TypedArray typeArray = context.obtainStyledAttributes(attrs, R.styleable.FragmentTitleView);
        isMenu = typeArray.getBoolean(R.styleable.FragmentTitleView_isMenu, true);

    }

}

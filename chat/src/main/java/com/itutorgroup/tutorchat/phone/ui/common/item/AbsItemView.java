package com.itutorgroup.tutorchat.phone.ui.common.item;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.utils.PixelUtil;

/**
 * Created by joyinzhao on 2016/8/24.
 */
public class AbsItemView extends RelativeLayout implements View.OnClickListener {

    protected RelativeLayout mContentView;
    public TextView mTvTitle;
    public TextView mSubTvTitle;
    public TextView mTvSummary;
    protected RelativeLayout mRightGroup;

    public AbsItemView(Context context) {
        super(context);
        init(context);
    }

    public AbsItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        initAttrs(context, attrs);
    }

    public AbsItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        initAttrs(context, attrs);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AbsItemView);
        String title = ta.getString(R.styleable.AbsItemView_aiv_text);
        if (!TextUtils.isEmpty(title)) {
            mTvTitle.setText(title);
        }

        String summary = ta.getString(R.styleable.AbsItemView_aiv_summary);
        if (!TextUtils.isEmpty(summary)) {
            mTvSummary.setText(summary);
        }

        ta.recycle();

        setOnClickListener(this);
        loadData();
    }

    private void init(Context context) {
        mContentView = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.common_abs_item, null);
        mTvTitle = (TextView) mContentView.findViewById(R.id.tv_title);
        mSubTvTitle = (TextView) mContentView.findViewById(R.id.tv_subtitle);
        mTvSummary = (TextView) mContentView.findViewById(R.id.tv_summary);
        mRightGroup = (RelativeLayout) mContentView.findViewById(R.id.rl_right);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, PixelUtil.dp2px(42));
        mContentView.setLayoutParams(lp);
        addView(mContentView);

        setup();
    }

    private void setup() {
        View view = getRightView();
        if (view == null) {
            return;
        }
        mRightGroup.addView(view);
    }

    protected View getRightView() {
        return null;
    }

    protected void loadData() {

    }

    @Override
    public void onClick(View v) {

    }
}

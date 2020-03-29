package dhl.anddemo.base;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import dhl.anddemo.R;
import dhl.anddemo.base.util.DeviceInfo;

/**
 * 自定义TitleBar
 *
 * Created by DuanHl on 2015/12/29.
 */
public class TitleBar extends FrameLayout {

    private View            mLeftView;
    private View            mRightView;
    private View            mCenterView;
    private View            mStatusBarView;
    private FrameLayout     mTitleBarParent;

    private int             mTitleBarHeight;
    private int             mHorPadding;
    private int             mTopMargin;
    private OnTitleBarClickListener mClickCallback;
    private OnClickListener mClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mClickCallback == null) return;
            if (v == mLeftView) {
                mClickCallback.onLeftClick(v);
            } else if (v == mCenterView) {
                mClickCallback.onTitleClick(v);
            } else {
                if (v == mRightView) {
                    mClickCallback.onRightFirstClick(v);
                } else {
                    int index = ((ViewGroup)mRightView).indexOfChild(v);
                    int childCount = ((ViewGroup)mRightView).getChildCount();
                    if (index == childCount-1) {
                        mClickCallback.onRightFirstClick(v);
                    } else if (index == childCount-2) {
                        mClickCallback.onRightSecondClick(v);
                    } else if (index == childCount-3) {
                        mClickCallback.onRightThirdClick(v);
                    }
                }
            }
        }
    };

    public TitleBar(Context context) {
        this(context, null);
    }

    public TitleBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mTitleBarHeight = getResources().getDimensionPixelSize(R.dimen.titleBarHeight);
        mHorPadding = getResources().getDimensionPixelSize(R.dimen.titlebarHorPadding);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mTopMargin = DeviceInfo.getStatusBarHeight(context);
        }

        if (mTopMargin > 0) {
            mStatusBarView = new View(context);
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, mTopMargin);
            params.gravity = Gravity.TOP;
            addView(mStatusBarView, params);

            mTitleBarParent = new FrameLayout(context);
            LayoutParams params1 = new LayoutParams(LayoutParams.MATCH_PARENT, mTitleBarHeight);
            params1.topMargin = mTopMargin;
            addView(mTitleBarParent, params1);
        } else {
            mTitleBarParent = this;
        }
        //标题栏默认背景色
        mTitleBarParent.setBackgroundResource(R.color.titlebar_background);
        //状态栏默认背景色
        if (mStatusBarView != null) mStatusBarView.setBackgroundResource(R.color.titlebar_background);

        String title = null;
        Drawable leftDrawable = null;
        String leftText = null;
        Drawable rightFirstDrawable = null;
        String rightFirstText = null;
        Drawable rightSecondDrawable = null;
        String rightSecondText = null;
        Drawable rightThirdDrawable = null;
        String rightThirdText = null;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TitleBar);
        try {
            title = typedArray.getString(R.styleable.TitleBar_titleText);
            leftDrawable = typedArray.getDrawable(R.styleable.TitleBar_leftDrawable);
            leftText = typedArray.getString(R.styleable.TitleBar_leftText);
            rightFirstDrawable = typedArray.getDrawable(R.styleable.TitleBar_rightFirstDrawable);
            rightFirstText = typedArray.getString(R.styleable.TitleBar_rightFirstText);
            rightSecondDrawable = typedArray.getDrawable(R.styleable.TitleBar_rightSecondDrawable);
            rightSecondText = typedArray.getString(R.styleable.TitleBar_rightSecondText);
            rightThirdDrawable = typedArray.getDrawable(R.styleable.TitleBar_rightThirdDrawable);
            rightThirdText = typedArray.getString(R.styleable.TitleBar_rightThirdText);
        } catch (Exception e) {
        } finally {
            typedArray.recycle();
        }

        addTitleTextView(title);
        addLeftButton(leftDrawable, leftText);
        addRightButton(rightFirstDrawable, rightFirstText, rightSecondDrawable, rightSecondText, rightThirdDrawable, rightThirdText);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(mTitleBarHeight + mTopMargin, MeasureSpec.EXACTLY));

        if (mCenterView != null) {
            int w = getMeasuredWidth();
            int lw = mLeftView != null ? mLeftView.getMeasuredWidth() : 0;
            int rw = mRightView != null ? mRightView.getMeasuredWidth() : 0;
            int cw = mCenterView != null ? mCenterView.getMeasuredWidth() : 0;

            int max = lw > rw ? lw : rw;
            if ((mHorPadding + max)*2 + cw > w) {
                mCenterView.measure(MeasureSpec.makeMeasureSpec(w - (mHorPadding + max)*2, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(mTitleBarHeight, MeasureSpec.EXACTLY));
            }
        }
    }

    private void addTitleTextView(String title) {
        removeCenterView();
        if (title == null || title.length() == 0) return;
        //添加标题
        TextView textView = new TextView(getContext());
        textView.setText(title);
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.titlebar_title_textsize));
        textView.setTextColor(getResources().getColor(R.color.titlebar_textColor));
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, mTitleBarHeight);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        textView.setOnClickListener(mClickListener);
        mCenterView = textView;
        mTitleBarParent.addView(mCenterView, params);
    }

    private void addLeftButton(Drawable drawable, String text) {
        removeLeftView();
        if (drawable != null && !TextUtils.isEmpty(text)) {
            throw new RuntimeException("按钮只能是图片或文字(@see #setLeftView())");
        } else if (drawable != null) {
            ImageView imageView = new ImageView(getContext());
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setImageDrawable(drawable);
            mLeftView = imageView;
        } else if (!TextUtils.isEmpty(text)) {
            TextView textView = new TextView(getContext());
            textView.setTextColor(getResources().getColor(R.color.titlebar_textColor));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.titlebar_right_textsize));
            textView.setSingleLine(true);
            textView.setText(text);
            textView.setGravity(Gravity.CENTER);
            textView.setPadding(mHorPadding, 0, mHorPadding, 0);
            mLeftView = textView;
        }

        if (mLeftView != null) {
            mLeftView.setBackgroundResource(R.drawable.pressed_selector);
            mLeftView.setOnClickListener(mClickListener);
            int width;
            if (drawable != null) {
                width = (int) ((float)drawable.getIntrinsicWidth() * mTitleBarHeight / drawable.getIntrinsicHeight());
                width = width < mTitleBarHeight ? mTitleBarHeight : width;
            } else {
                width = LayoutParams.WRAP_CONTENT;
            }
            LayoutParams params = new LayoutParams(width, mTitleBarHeight);
            params.gravity = Gravity.LEFT;
            mTitleBarParent.addView(mLeftView, params);
        }
    }

    private void addRightButton(Drawable fDrawable, String fText, Drawable sDrawable, String sText, Drawable tDrawable, String tText) {
        removeRightView();

        int btnCount = 0;
        View firstView = null;
        int fvWidth = LayoutParams.WRAP_CONTENT;
        if (fDrawable != null && !TextUtils.isEmpty(fText)) {
            throw new RuntimeException("按钮只能是图片或文字 (@see #setRightView())");
        } else if (fDrawable != null) {
            ImageView imageView = new ImageView(getContext());
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setImageDrawable(fDrawable);
            firstView = imageView;
        } else if (!TextUtils.isEmpty(fText)) {
            TextView textView = new TextView(getContext());
            textView.setTextColor(getResources().getColor(R.color.titlebar_textColor));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.titlebar_right_textsize));
            textView.setSingleLine(true);
            textView.setText(fText);
            textView.setGravity(Gravity.CENTER);
            textView.setPadding(mHorPadding, 0, mHorPadding, 0);
            firstView = textView;
        }
        if (firstView != null) {
            btnCount++;
            firstView.setBackgroundResource(R.drawable.pressed_selector);
            firstView.setOnClickListener(mClickListener);
            if (fDrawable != null) {
                fvWidth = (int) ((float)fDrawable.getIntrinsicWidth() * mTitleBarHeight / fDrawable.getIntrinsicHeight());
                fvWidth = fvWidth < mTitleBarHeight ? mTitleBarHeight : fvWidth;
            } else {
                fvWidth = LayoutParams.WRAP_CONTENT;
            }
        }

        View secondView = null;
        int svWidth = LayoutParams.WRAP_CONTENT;
        if (sDrawable != null && !TextUtils.isEmpty(sText)) {
            throw new RuntimeException("按钮只能是图片或文字 (@see #setRightView())");
        } else if (sDrawable != null) {
            ImageView imageView = new ImageView(getContext());
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setImageDrawable(sDrawable);
            secondView = imageView;
        } else if (!TextUtils.isEmpty(sText)) {
            TextView textView = new TextView(getContext());
            textView.setTextColor(getResources().getColor(R.color.titlebar_textColor));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.titlebar_right_textsize));
            textView.setSingleLine(true);
            textView.setText(sText);
            textView.setGravity(Gravity.CENTER);
            textView.setPadding(mHorPadding, 0, mHorPadding, 0);
            secondView = textView;
        }
        if (secondView != null) {
            btnCount++;
            secondView.setBackgroundResource(R.drawable.pressed_selector);
            secondView.setOnClickListener(mClickListener);
            if (sDrawable != null) {
                svWidth = (int) ((float)sDrawable.getIntrinsicWidth() * mTitleBarHeight / sDrawable.getIntrinsicHeight());
                svWidth = svWidth < mTitleBarHeight ? mTitleBarHeight : svWidth;
            } else {
                svWidth = LayoutParams.WRAP_CONTENT;
            }
        }

        View thirdView = null;
        int tvWidth = LayoutParams.WRAP_CONTENT;
        if (tDrawable != null && !TextUtils.isEmpty(tText)) {
            throw new RuntimeException("按钮只能是图片或文字 (@see #setRightView())");
        } else if (tDrawable != null) {
            ImageView imageView = new ImageView(getContext());
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setImageDrawable(tDrawable);
            thirdView = imageView;
        } else if (!TextUtils.isEmpty(tText)) {
            TextView textView = new TextView(getContext());
            textView.setTextColor(getResources().getColor(R.color.titlebar_textColor));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.titlebar_right_textsize));
            textView.setSingleLine(true);
            textView.setText(tText);
            textView.setGravity(Gravity.CENTER);
            textView.setPadding(mHorPadding, 0, mHorPadding, 0);
            thirdView = textView;
        }
        if (thirdView != null) {
            btnCount++;
            thirdView.setBackgroundResource(R.drawable.pressed_selector);
            thirdView.setOnClickListener(mClickListener);
            if (tDrawable != null) {
                tvWidth = (int) ((float)tDrawable.getIntrinsicWidth() * mTitleBarHeight / tDrawable.getIntrinsicHeight());
                tvWidth = tvWidth < mTitleBarHeight ? mTitleBarHeight : tvWidth;
            } else {
                tvWidth = LayoutParams.WRAP_CONTENT;
            }
        }

        if (btnCount == 0) {

        } else if (btnCount == 1) {
            mRightView = firstView != null ? firstView : (secondView != null ? secondView : thirdView);
            LayoutParams params = new LayoutParams(mRightView == firstView ? fvWidth : (mRightView == secondView ? svWidth : tvWidth), mTitleBarHeight);
            params.gravity = Gravity.RIGHT;
            mTitleBarParent.addView(mRightView, params);
        } else {
            LinearLayout linearLayout = new LinearLayout(getContext());
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            if (thirdView != null) linearLayout.addView(thirdView, new LinearLayout.LayoutParams(tvWidth, mTitleBarHeight));
            if (secondView != null) linearLayout.addView(secondView, new LinearLayout.LayoutParams(svWidth, mTitleBarHeight));
            if (firstView != null) linearLayout.addView(firstView, new LinearLayout.LayoutParams(fvWidth, mTitleBarHeight));
            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, mTitleBarHeight);
            params.gravity = Gravity.RIGHT;
            mRightView = linearLayout;
            mTitleBarParent.addView(mRightView, params);
        }
    }

    private void removeLeftView() {
        if (mLeftView != null) {
            mLeftView.setOnClickListener(null);
            mTitleBarParent.removeView(mLeftView);
            mLeftView = null;
        }
    }

    private void removeRightView() {
        if (mRightView != null) {
            mRightView.setOnClickListener(null);
            mTitleBarParent.removeView(mRightView);
            mRightView = null;
        }
    }

    private void removeCenterView() {
        if (mCenterView != null) {
            mCenterView.setOnClickListener(null);
            mTitleBarParent.removeView(mCenterView);
            mCenterView = null;
        }
    }

    //============= public method =================
    /**
     * 获取标题栏高度
     * @return
     */
    public int getTitleBarHeight() {
        return mTitleBarHeight;
    }

    /**
     * 设置状态栏颜色
     * @param color
     */
    public void setStatusBarColor(int color) {
        if (mStatusBarView != null) {
            mStatusBarView.setBackgroundColor(color);
        }
    }

    /**
     * 设置标题栏颜色
     * @param color
     */
    public void setTitleBarColor(int color) {
        mTitleBarParent.setBackgroundColor(color);
    }

    public void setTitleBarClickListener(OnTitleBarClickListener lis) {
        mClickCallback = lis;
    }

    public View getLeftView() {
        return mLeftView;
    }

    public View getRightView() {
        return mRightView;
    }

    public View getCenterView() {
        return mCenterView;
    }

    /**
     * 自定义左侧View
     * @param view
     */
    public void setLeftView(View view) {
        removeLeftView();
        mLeftView = view;
        if (view == null) return;
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params == null) {
            params = new LayoutParams(LayoutParams.WRAP_CONTENT, mTitleBarHeight);
        } else if (!(params instanceof LayoutParams)) {
            params = generateLayoutParams(params);
        }
        ((LayoutParams)params).gravity = Gravity.LEFT;
        mTitleBarParent.addView(view, params);
    }

    /**
     * 自定义右侧View
     * @param view
     */
    public void setRightView(View view) {
        removeRightView();
        mRightView = view;
        if (view == null) return;
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params == null) {
            params = new LayoutParams(LayoutParams.WRAP_CONTENT, mTitleBarHeight);
        } else if (!(params instanceof LayoutParams)) {
            params = generateLayoutParams(params);
        }
        ((LayoutParams)params).gravity = Gravity.RIGHT;
        mTitleBarParent.addView(view, params);
    }

    /**
     * 自定义中部view
     * @param view
     */
    public void setCenterView(View view) {
        removeCenterView();
        mCenterView = view;
        if (view == null) return;
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params == null) {
            params = new LayoutParams(LayoutParams.WRAP_CONTENT, mTitleBarHeight);
        } else if (!(params instanceof LayoutParams)) {
            params = generateLayoutParams(params);
        }
        ((LayoutParams)params).gravity = Gravity.CENTER;
        mTitleBarParent.addView(view, params);
    }

    public void setTitle(String title) {
        if (mCenterView == null) {
            addTitleTextView(title);
        } else {
            if (mCenterView instanceof TextView) {
                ((TextView) mCenterView).setText(title);
            } else {
                addTitleTextView(title);
            }
        }
    }

    /**
     * 当左边只有一个按钮时,设置左边按钮的图标或文字(二选一, 如果既有图标又有文字,应该使用seLeftView)
     * @param drawable  :图标
     * @param text      :文字
     */
    public void setLeftBtn(Drawable drawable, String text) {
        if (mLeftView == null) {
            addLeftButton(drawable, text);
        } else {
            if (mLeftView instanceof ImageView) {
                ((ImageView) mLeftView).setImageDrawable(drawable);
            } else if (mLeftView instanceof TextView) {
                ((TextView) mLeftView).setText(text);
            } else {
                addLeftButton(drawable, text);
            }
        }
    }

    /**
     * 当右边只有一个按钮时,设置右边按钮的图标或文字(二选一, 如果既有图标又有文字,应该使用seRightView)
     * @param drawable  :图标
     * @param text      :文字
     */
    public void setRightBtn(Drawable drawable, String text) {
        if (mRightView == null) {
            addRightButton(drawable, text, null, null, null, null);
        } else {
            if (mRightView instanceof ImageView) {
                ((ImageView) mRightView).setImageDrawable(drawable);
            } else if (mRightView instanceof TextView) {
                ((TextView) mRightView).setText(text);
            } else {
                addRightButton(drawable, text, null, null, null, null);
            }
        }
    }

    public void setLeftViewWidth(int width) {
        if (mLeftView != null) {
            mLeftView.getLayoutParams().width = width;
            mLeftView.requestLayout();
        }
    }

    public void setRightViewWidth(int width) {
        if (mRightView != null) {
            mRightView.getLayoutParams().width = width;
            mRightView.requestLayout();
        }
    }

    interface OnTitleBarClickListener {
        void onLeftClick(View v);
        void onTitleClick(View v);
        void onRightFirstClick(View v);
        void onRightSecondClick(View v);
        void onRightThirdClick(View v);
    }

    public static class SimpleTitleBarClickListener implements OnTitleBarClickListener {

        @Override
        public void onLeftClick(View v) {}

        @Override
        public void onTitleClick(View v) {}

        @Override
        public void onRightFirstClick(View v) {}

        @Override
        public void onRightSecondClick(View v) {}

        @Override
        public void onRightThirdClick(View v) {}
    }
}

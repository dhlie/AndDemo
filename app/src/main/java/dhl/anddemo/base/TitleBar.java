package dhl.anddemo.base;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import dhl.anddemo.R;
import dhl.anddemo.base.util.DeviceInfo;

/**
 * 自定义TitleBar
 *
 * Created by DuanHl on 2015/12/29.
 */
public class TitleBar extends FrameLayout {

    private static final int TITLE_TEXT_SIZE = 18;
    private static final int RIGHT_TEXT_SIZE = 16;

    private View            mLeftView;
    private View            mRightView;
    private View            mCenterView;
    private View            mStatusBarView;
    private FrameLayout     mTitleBarParent;

    private Drawable        mLeftBtnDrawable;
    private Drawable        mRightBtnDrawable;
    private String          mLeftBtnText;
    private String          mRightBtnText;
    private String          mTitleText;

    private int             mTitleBarHeight;
    private int             mHorPadding;
    private int             mTopMargin;

    public TitleBar(Context context) {
        this(context, null);
    }

    public TitleBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TitleBar);
        try {
            mLeftBtnDrawable = typedArray.getDrawable(R.styleable.TitleBar_leftDrawable);
            mRightBtnDrawable = typedArray.getDrawable(R.styleable.TitleBar_rightDrawable);
            mLeftBtnText = typedArray.getString(R.styleable.TitleBar_leftText);
            mRightBtnText = typedArray.getString(R.styleable.TitleBar_rightText);
            mTitleText = typedArray.getString(R.styleable.TitleBar_title_text);
        } catch (Exception e) {
        } finally {
            typedArray.recycle();
        }

        mTitleBarHeight = getResources().getDimensionPixelSize(R.dimen.titleBarHeight);
        mHorPadding = getResources().getDimensionPixelSize(R.dimen.titlebarHorPadding);

        initView(context);
    }

    private void initView(Context context) {
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

        addLeftButton();
        addRightButton();

        //添加标题
        TextView textView = new TextView(context);
        textView.setText(mTitleText);
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TITLE_TEXT_SIZE);
        textView.setTextColor(getResources().getColor(R.color.titlebar_textColor));
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, mTitleBarHeight);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        mTitleBarParent.addView(textView, params);
        mCenterView = textView;

        mCenterView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mCenterView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }else{
                    mCenterView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                recomputeTitleWidth();
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = mTitleBarHeight + mTopMargin;
        setMeasuredDimension(width, height);
    }

    private void addLeftButton() {
        removeLeftBtn();
        if (mLeftBtnDrawable != null && !TextUtils.isEmpty(mLeftBtnText)) {
            throw new RuntimeException("you should use youself view as left button (@see setLeftView)");
        } else if (mLeftBtnDrawable != null) {
            ImageView imageView = new ImageView(getContext());
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setImageDrawable(mLeftBtnDrawable);
            mLeftView = imageView;
        } else if (!TextUtils.isEmpty(mLeftBtnText)) {
            TextView textView = new TextView(getContext());
            textView.setTextColor(getResources().getColor(R.color.titlebar_textColor));
            textView.setTextSize(RIGHT_TEXT_SIZE);
            textView.setSingleLine(true);
            textView.setText(mLeftBtnText);
            textView.setGravity(Gravity.CENTER);
            textView.setPadding(mHorPadding, 0, mHorPadding, 0);
            mLeftView = textView;
        }

        if (mLeftView != null) {
            mLeftView.setBackgroundResource(R.drawable.button_pressed_selector);
            mLeftView.setClickable(false);
            int width;
            if (mLeftBtnDrawable != null) {
                width = (int) ((float)mLeftBtnDrawable.getIntrinsicWidth() * mTitleBarHeight / mLeftBtnDrawable.getIntrinsicHeight());
                width = width < mTitleBarHeight ? mTitleBarHeight : width;
            } else {
                width = LayoutParams.WRAP_CONTENT;
            }
            LayoutParams params = new LayoutParams(width, mTitleBarHeight);
            params.gravity = Gravity.LEFT;
            mTitleBarParent.addView(mLeftView, params);
        }
    }

    private void addRightButton() {
        removeRightBtn();
        if (mRightBtnDrawable != null && !TextUtils.isEmpty(mRightBtnText)) {
            throw new RuntimeException("you should use youself view as right button (@see setRightView)");
        } else if (mRightBtnDrawable != null) {
            ImageView imageView = new ImageView(getContext());
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setImageDrawable(mRightBtnDrawable);
            mRightView = imageView;
        } else if (!TextUtils.isEmpty(mRightBtnText)) {
            TextView textView = new TextView(getContext());
            textView.setTextColor(getResources().getColor(R.color.titlebar_textColor));
            textView.setTextSize(RIGHT_TEXT_SIZE);
            textView.setSingleLine(true);
            textView.setText(mRightBtnText);
            textView.setGravity(Gravity.CENTER);
            textView.setPadding(mHorPadding, 0, mHorPadding, 0);
            mRightView = textView;
        }

        if (mRightView != null) {
            mRightView.setBackgroundResource(R.drawable.button_pressed_selector);
            mRightView.setClickable(false);
            int width;
            if (mRightBtnDrawable != null) {
                width = (int) ((float)mRightBtnDrawable.getIntrinsicWidth() * mTitleBarHeight / mRightBtnDrawable.getIntrinsicHeight());
                width = width < mTitleBarHeight ? mTitleBarHeight : width;
            } else {
                width = LayoutParams.WRAP_CONTENT;
            }

            LayoutParams params = new LayoutParams(width, mTitleBarHeight);
            params.gravity = Gravity.RIGHT;
            mTitleBarParent.addView(mRightView, params);
        }
    }

    private void removeLeftBtn() {
        if (mLeftView != null) {
            mLeftView.setOnClickListener(null);
            mTitleBarParent.removeView(mLeftView);
            mLeftView = null;
        }
    }

    private void removeRightBtn() {
        if (mRightView != null) {
            mRightView.setOnClickListener(null);
            mTitleBarParent.removeView(mRightView);
            mRightView = null;
        }
    }

    private void recomputeTitleWidth(){
        int leftViewWidth = mLeftView != null ? mLeftView.getMeasuredWidth() : 0;
        int rightViewWidth = mRightView != null ? mRightView.getMeasuredWidth() : 0;

        int bothMargin = leftViewWidth>rightViewWidth?leftViewWidth:rightViewWidth;

        LayoutParams params = (LayoutParams) mCenterView.getLayoutParams();
        params.setMargins(bothMargin,params.topMargin,bothMargin,params.bottomMargin);
        mCenterView.setLayoutParams(params);
    }

    //============= public method =================
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

    public void setTitle(String title) {
        if (mCenterView != null && mCenterView instanceof TextView) {
            ((TextView)mCenterView).setText(title);
        }
    }

    public void setLeftBtnClickListener(OnClickListener lis) {
        if (mLeftView != null) mLeftView.setOnClickListener(lis);
    }

    public void setRightBtnClickListener(OnClickListener lis) {
        if (mRightView != null) mRightView.setOnClickListener(lis);
    }

    public void setLeftDrawable(Drawable drawable) {
        if (mLeftView != null && mLeftView instanceof ImageView) {
            ((ImageView)mLeftView).setImageDrawable(drawable);
        }
    }

    public void setRightDrawable(Drawable drawable) {
        if (mRightView != null && mRightView instanceof ImageView) {
            ((ImageView)mRightView).setImageDrawable(drawable);
        }
    }

    public void setLeftText(String text) {
        if (mLeftView != null && mLeftView instanceof TextView) {
            ((TextView)mLeftView).setText(text);
        }
    }

    public void setRightText(String text) {
        if (mRightView != null && mRightView instanceof TextView) {
            ((TextView)mRightView).setText(text);
        }
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
        removeLeftBtn();
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
        removeRightBtn();
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
        if (mCenterView != null) {
            mTitleBarParent.removeView(mCenterView);
            mCenterView = null;
        }
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

    /**
     * 设置左边按钮的图标或文字(二选一, 如果既有图标又有文字,应该使用seLeftView)
     * @param drawable  :图标
     * @param text      :文字
     */
    public void setupLeftBtn(Drawable drawable, String text) {
        mLeftBtnDrawable = drawable;
        mLeftBtnText = text;
        addLeftButton();
    }

    /**
     * 设置右边按钮的图标或文字(二选一, 如果既有图标又有文字,应该使用seRightView)
     * @param drawable  :图标
     * @param text      :文字
     */
    public void setupRightBtn(Drawable drawable, String text) {
        mRightBtnDrawable = drawable;
        mRightBtnText = text;
        addRightButton();
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
}

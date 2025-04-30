package dhl.anddemo.base.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.Layout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;

import java.util.Locale;

import dhl.anddemo.R;

/**
 * Author: duanhl
 * Create: 2020-04-14 10:35
 * Description:
 */
public class BadgeTextView extends AppCompatTextView {

  private int badgeRadius = 5;
  private int badgeColor;
  private int backgroundColor;//背景色，当文字最右边和边距的距离不够绘制圆点时，会用背景色遮挡部分文字
  private int badgePadding = 10;
  private Rect badgeRect = new Rect();
  private Paint paint;
  private Drawable shadowDrawable;
  private boolean showBadge = true;

  public BadgeTextView(Context context) {
    super(context);
    init(context, null);
  }

  public BadgeTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public BadgeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  public void setShowBadge(boolean showBadge) {
    this.showBadge = showBadge;
    invalidate();
  }

  private void init(Context context, AttributeSet attrs) {
    badgeRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, badgeRadius, context.getResources().getDisplayMetrics());
    badgePadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, badgePadding, context.getResources().getDisplayMetrics());

    if (attrs != null) {
      TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BadgeTextView);
      try {
        badgeRadius = typedArray.getDimensionPixelOffset(R.styleable.BadgeTextView_badgeRadius, badgeRadius);
        badgePadding = typedArray.getDimensionPixelOffset(R.styleable.BadgeTextView_badgePadding, badgePadding);
        badgeColor = typedArray.getColor(R.styleable.BadgeTextView_badgeColor, 0);
        backgroundColor = typedArray.getColor(R.styleable.BadgeTextView_badgeBackground, 0);
      } catch (Exception e) {
      } finally {
        typedArray.recycle();
      }
    }
    shadowDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{getTranslucentColor(0.8f, backgroundColor), backgroundColor});
    paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    paint.setColor(badgeColor);
  }

  public static int getTranslucentColor(float percent, int rgb) {
    int red = Color.red(rgb);
    int green = Color.green(rgb);
    int blue = Color.blue(rgb);
    int alpha = Color.alpha(rgb);
    alpha = Math.round(alpha * percent);
    return Color.argb(alpha, red, green, blue);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    drawBadge(canvas);
  }

  private void drawBadge(Canvas canvas) {
    if (!showBadge) {
      return;
    }

    Layout layout = getLayout();
    int lineCount = layout.getLineCount();
    if (lineCount == 0) {
      return;
    }

    boolean isRTL = false;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      isRTL = TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL;
    }

    int lastLine = lineCount - 1;
    float lineWidth = layout.getLineWidth(lastLine);
    int rectWidth = badgePadding + badgeRadius * 2;
    if (lineWidth + rectWidth  + getPaddingLeft() + getPaddingRight() > getWidth()) {
      int rectLeft;
      if (isRTL) {
        rectLeft = getPaddingLeft();
      } else {
        rectLeft = getWidth() - getPaddingRight() - rectWidth;
      }
      badgeRect.set(rectLeft, layout.getLineTop(lastLine) + getPaddingTop(), rectLeft + rectWidth, layout.getLineBottom(lastLine) + getPaddingTop());
      shadowDrawable.setBounds(badgeRect);
      shadowDrawable.draw(canvas);
    } else {
      int rectLeft;
      if (isRTL) {
        rectLeft = (int) (getWidth() - getPaddingRight() - lineWidth - rectWidth);
      } else {
        rectLeft = (int) (lineWidth + getPaddingLeft());
      }
      badgeRect.set(rectLeft, layout.getLineTop(lastLine) + getPaddingTop(), rectLeft + rectWidth, layout.getLineBottom(lastLine) + getPaddingTop());

    }

    if (isRTL) {
      canvas.drawCircle(badgeRect.left + badgeRadius, badgeRect.centerY(), badgeRadius, paint);
    } else {
      canvas.drawCircle(badgeRect.right - badgeRadius, badgeRect.centerY(), badgeRadius, paint);
    }
  }
}

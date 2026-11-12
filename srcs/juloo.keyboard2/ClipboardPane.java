package juloo.keyboard2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/** Shared image background for the clipboard list and its keyboard row. */
public final class ClipboardPane extends LinearLayout
{
  private final Bitmap backgroundBitmap;
  private final Paint bitmapPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
  private final Paint shadePaint = new Paint();
  private final Matrix backgroundMatrix = new Matrix();

  public ClipboardPane(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    Theme theme = new Theme(context, attrs);
    backgroundBitmap = theme.keyboardBackgroundBitmap;
    Config config = Config.globalConfig();
    shadePaint.setColor(Color.BLACK);
    shadePaint.setAlpha(Math.max(0, Math.min(255,
        Math.round(config.keyOpacity * theme.keyOpacityMultiplier))));
    setWillNotDraw(false);
  }

  public boolean hasSharedBackground()
  {
    return backgroundBitmap != null;
  }

  @Override
  protected void onFinishInflate()
  {
    super.onFinishInflate();
    if (!hasSharedBackground())
      return;
    for (int i = 0; i < getChildCount(); i++)
    {
      View child = getChildAt(i);
      if (child instanceof Keyboard2View)
        child.setBackgroundColor(Color.TRANSPARENT);
    }
  }

  @Override
  protected void onDraw(Canvas canvas)
  {
    super.onDraw(canvas);
    if (!hasSharedBackground())
      return;
    float panelWidth = getWidth() / 3.f;
    float height = getHeight();
    float sourcePanelWidth = backgroundBitmap.getWidth() / 3.f;
    float sourceHeight = backgroundBitmap.getHeight();
    if (panelWidth <= 0 || height <= 0 || sourcePanelWidth <= 0 || sourceHeight <= 0)
      return;
    float scale = Math.max(panelWidth / sourcePanelWidth, height / sourceHeight);
    for (int panel = 0; panel < 3; panel++)
    {
      float left = panel * panelWidth;
      float sourceCenterX = (panel + 0.5f) * sourcePanelWidth;
      backgroundMatrix.setScale(scale, scale);
      backgroundMatrix.postTranslate(
          left + panelWidth / 2.f - sourceCenterX * scale,
          height / 2.f - sourceHeight * scale / 2.f);
      int save = canvas.save();
      canvas.clipRect(left, 0, left + panelWidth, height);
      canvas.drawBitmap(backgroundBitmap, backgroundMatrix, bitmapPaint);
      canvas.restoreToCount(save);
    }
    int listBottom = getHeight();
    for (int i = 0; i < getChildCount(); i++)
    {
      View child = getChildAt(i);
      if (child instanceof Keyboard2View && child.getVisibility() != View.GONE)
        listBottom = Math.min(listBottom, child.getTop());
    }
    canvas.drawRect(0, 0, getWidth(), listBottom, shadePaint);
  }
}

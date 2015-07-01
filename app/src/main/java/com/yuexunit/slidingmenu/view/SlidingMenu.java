package com.yuexunit.slidingmenu.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.nineoldandroids.view.ViewHelper;
import com.yuexunit.slidingmenu.R;

/**
 * 滑动菜单
 *
 * @author WuRS
 */
public class SlidingMenu extends HorizontalScrollView {

    /**
     * 默认的菜单
     */
    public static final int MENU_MODE_LEFT = 0;

    /**
     * 菜单显示在内容下方
     */
    public static final int MENU_MODE_BEHIND = 1;

    /**
     * 菜单在左侧，滑动带缩放、渐变效果，类似QQ
     */
    public static final int MENU_MODE_MIX = 2;

    private static final String TAG = SlidingMenu.class.getSimpleName();

    private static final int DEFAULT_MENU_OFFSET = 50; // 默认菜单偏移，单位dip

    private static final float DEFAULT_MIX_FACTOR = 0.8f; // 默认变化系数

    private LinearLayout mWrapper; // 容器视图
    private ViewGroup mMenu; // 菜单视图
    private ViewGroup mContent; // 内容视图

    private boolean isMeasured; // 是否已经测量过
    private int screenWidth; // 屏幕宽度
    private int menuOffset = DEFAULT_MENU_OFFSET; // 菜单完全展示时，内容可见区域的大小
    private int mMenuWidth; // 菜单宽度
    private boolean isMenuShowing; // 菜单是否显示
    private int menuMode = MENU_MODE_LEFT; // 菜单的显示模式
    private float mixFactor = DEFAULT_MIX_FACTOR; // mix模式下的变化系数

    /**
     * 动态创建
     *
     * @param context
     */
    public SlidingMenu(Context context) {
        this(context, null);
    }

    /**
     * 布局文件中未设置自定义属性时调用
     *
     * @param context
     * @param attrs
     */
    public SlidingMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * 布局文件中带有自定义属性时调用
     *
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public SlidingMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        screenWidth = displayMetrics.widthPixels;
        Log.d(TAG, "init SlidingMenu,screenWidth=" + screenWidth);

        // 获取自定义属性
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable
                .SlidingMenu, defStyleAttr, 0);
        int attrCount = typedArray.getIndexCount();
        for (int i = 0; i < attrCount; i++) {
            int attr = typedArray.getIndex(i);
            switch (attr) {
                case R.styleable.SlidingMenu_menuOffset:
                    // dp转换成px
                    setMenuOffset(typedArray.getDimensionPixelSize(attr, (int) TypedValue
                            .applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_MENU_OFFSET,
                                    displayMetrics)));
                    break;
                case R.styleable.SlidingMenu_menuMode:
                    setMenuMode(typedArray.getInt(attr, MENU_MODE_LEFT));
                    break;
                case R.styleable.SlidingMenu_mixFactor:
                    setMixFactor(typedArray.getFloat(attr, DEFAULT_MIX_FACTOR));
                    break;
            }
        }
        typedArray.recycle();
    }

    /**
     * 测量、设置子view的宽高和自身的宽高
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!isMeasured) {
            Log.d(TAG, "onMeasure");
            isMeasured = true;
            // 获取容器视图
            mWrapper = (LinearLayout) getChildAt(0);
            // 获取菜单视图
            mMenu = (ViewGroup) mWrapper.getChildAt(0);
            // 获取内容视图
            mContent = (ViewGroup) mWrapper.getChildAt(1);

            // 设置菜单宽
            mMenuWidth = mMenu.getLayoutParams().width = screenWidth - menuOffset;
            // 设置内容宽
            mContent.getLayoutParams().width = screenWidth;
        }
    }

    /**
     * 确定子view的位置
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            // 默认显示内容
            this.scrollTo(mMenuWidth, 0);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 菜单显示时，屏蔽在菜单除外区域的点击事件
                if (isMenuShowing() && ev.getX() > mMenuWidth)
                    return true;
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    float downX, downY;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = ev.getX();
                downY = ev.getY();
                break;
            case MotionEvent.ACTION_UP:
                int scrollX = getScrollX();
                // 菜单显示时，点击菜单之外的区域，隐藏菜单
                if (isMenuShowing() && clickOutOfMenuRange(ev)) {
                    hideMenu();
                    return true;
                }
                // 滚动距离小于菜单的一半，显示菜单；否则隐藏菜单
                if (scrollX < (mMenuWidth / 2)) {
                    smoothScrollTo(0, 0);
                    isMenuShowing = true;
                } else {
                    smoothScrollTo(mMenuWidth, 0);
                    isMenuShowing = false;
                }
                return true;
        }
        return super.onTouchEvent(ev);
    }

    // 点击菜单之外的区域
    private boolean clickOutOfMenuRange(MotionEvent ev) {
        final float TOLERANCE = 4;
        return ev.getX() > mMenuWidth && (Math.abs(ev.getX() - downX) <= TOLERANCE ||
                Math.abs(ev.getY() - downY) <= TOLERANCE);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        switch (menuMode) {
            case MENU_MODE_BEHIND:
                // l == getScrollX()
                ViewHelper.setTranslationX(mMenu, l);
                break;
            case MENU_MODE_MIX:
                // 内容->菜单：l从mMenuWidth减少到0,菜单出现的百分比scale:0~1,(mMenuWidth - l) / mMenuWidth
                // 菜单alpha:0~1，scale
                // 菜单scale:0.8-1, 0.8 + scale * 0.2
                // 菜单trans:0.8-0, scale * 0.8
                // 内容scale:1-0.8, 1 - scale * 0.2
                // 菜单可见的百分比
                float fraction = (mMenuWidth - l) * 1.0f / mMenuWidth;
                float menuAlpha = fraction;
                float menuScale = mixFactor + fraction * (1 - mixFactor);
                float menuTrans = (1 - fraction) * mixFactor * mMenuWidth;
                float contentScale = 1f - fraction * (1 - mixFactor);

                // 设置内容缩放中心点
                ViewHelper.setPivotX(mContent, 0);
                ViewHelper.setPivotY(mContent, mContent.getHeight() / 2f);
                ViewHelper.setScaleX(mContent, contentScale);
                ViewHelper.setScaleY(mContent, contentScale);

                // 设置菜单动画
                ViewHelper.setScaleX(mMenu, menuScale);
                ViewHelper.setScaleY(mMenu, menuScale);
                ViewHelper.setAlpha(mMenu, menuAlpha);
                ViewHelper.setTranslationX(mMenu, menuTrans);
                break;
            case MENU_MODE_LEFT:
            default:
                break;
        }
    }

    /**
     * @return 获取mix模式下的变化系数
     */
    public float getMixFactor() {
        return mixFactor;
    }

    /**
     * 设置mix模式下的变化系数
     *
     * @param mixFactor 如果该值不在0~1的范围内，取默认值{@link SlidingMenu#DEFAULT_MIX_FACTOR}
     */
    public void setMixFactor(float mixFactor) {
        if (mixFactor < 0 || mixFactor > 1)
            mixFactor = DEFAULT_MIX_FACTOR;
        this.mixFactor = mixFactor;
    }

    /**
     * @return 菜单显示时内容的可见偏移量
     */
    public int getMenuOffset() {
        return menuOffset;
    }

    /**
     * 设置菜单显示时内容的可见偏移量
     *
     * @param menuOffset 单位px
     */
    public void setMenuOffset(int menuOffset) {
        this.menuOffset = menuOffset;
    }

    /**
     * @return 获取菜单的显示模式
     */
    public int getMenuMode() {
        return menuMode;
    }

    /**
     * 设置菜单的显示模式
     *
     * @param menuMode
     */
    public void setMenuMode(int menuMode) {
        this.menuMode = menuMode;
    }

    /**
     * 显示菜单
     */
    public void showMenu() {
        if (isMenuShowing())
            return;
        this.smoothScrollTo(0, 0);
        isMenuShowing = true;
    }

    /**
     * 隐藏菜单
     */
    public void hideMenu() {
        if (isMenuShowing()) {
            this.smoothScrollTo(mMenuWidth, 0);
            isMenuShowing = false;
        }
    }

    /**
     * 切换菜单
     */
    public void toggleMenu() {
        if (isMenuShowing()) {
            hideMenu();
        } else {
            showMenu();
        }
    }

    /**
     * @return 菜单是否显示
     */
    public boolean isMenuShowing() {
        return isMenuShowing;
    }
}


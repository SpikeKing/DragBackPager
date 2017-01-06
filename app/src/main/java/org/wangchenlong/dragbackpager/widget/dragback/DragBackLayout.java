package org.wangchenlong.dragbackpager.widget.dragback;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.wangchenlong.dragbackpager.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 后退的布局样式, 类型ViewGroup, 继承于FrameLayout
 */
public class DragBackLayout extends FrameLayout {

    private static final int MIN_FLING_VELOCITY = 400; // 滑动页面的最小速度, DP每秒.
    private static final int DEFAULT_SCRIM_COLOR = 0x99000000;
    private static final int FULL_ALPHA = 255; // 背景的全亮alpha值

    public static final int EDGE_LEFT = ViewDragHelper.EDGE_LEFT;   // 检测左侧滑动, 执行退出
    public static final int EDGE_RIGHT = ViewDragHelper.EDGE_RIGHT; // 检测右侧滑动, 执行退出
    public static final int EDGE_BOTTOM = ViewDragHelper.EDGE_BOTTOM; // 检测底部滑动, 执行退出
    public static final int EDGE_ALL = EDGE_LEFT | EDGE_RIGHT | EDGE_BOTTOM; // 检测全部滑动, 执行退出


    public static final int STATE_IDLE = ViewDragHelper.STATE_IDLE; // View的闲置状态, 未被拖拉和显示动画
    public static final int STATE_DRAGGING = ViewDragHelper.STATE_DRAGGING; // 用户正在拖动或模拟拖动
    public static final int STATE_SETTLING = ViewDragHelper.STATE_SETTLING; // 被设置入位置

    private static final float DEFAULT_SCROLL_THRESHOLD = 0.35f; // 默认滑动的默认阈值, 当超过时自动滑动关闭
    private static final int OVERSCROLL_DISTANCE = 10; // 超出滑动距离

    private static final int[] EDGE_FLAGS = { // 四种滑动样式
            EDGE_LEFT, EDGE_RIGHT, EDGE_BOTTOM, EDGE_ALL
    };

    private int mEdgeFlag;
    private float mScrollThreshold = DEFAULT_SCROLL_THRESHOLD; // 滑动阈值, 当超过时自动滑动关闭
    private boolean mEnable = true; // 是否启动滑动状态

    private Activity mActivity;
    private View mContentView;
    private ViewDragHelper mDragHelper;
    private float mScrollPercent;
    private int mContentLeft;
    private int mContentTop;

    private List<DragBackListener> mListeners; // 所有设置滑动的监听器

    private Drawable mShadowLeft;
    private Drawable mShadowRight;
    private Drawable mShadowBottom;

    private float mScrimOpacity; // 滑动的透明度, 会随着滑动大小, 越大越亮
    private int mScrimColor = DEFAULT_SCRIM_COLOR;
    private boolean mInLayout;
    private Rect mTmpRect = new Rect();
    private int mTrackingEdge; // 边界追踪标记, 下左右

    public DragBackLayout(Context context) {
        this(context, null);
    }

    public DragBackLayout(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.DragBackLayoutStyle);
    }

    public DragBackLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        mDragHelper = ViewDragHelper.create(this, new ViewDragCallback()); // 创建DragHelper

        // 获取属性
        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.DragBackLayout, defStyle,
                R.style.DragBackLayout);

        // 获取边缘大小
        int edgeSize = a.getDimensionPixelSize(R.styleable.DragBackLayout_edge_size, -1);
        if (edgeSize > 0)
            setEdgeSize(edgeSize);

        // 获取模式
        int mode = EDGE_FLAGS[a.getInt(R.styleable.DragBackLayout_edge_flag, 0)];
        setEdgeTrackingEnabled(mode);

        // 获取阴影
        int shadowLeft = a.getResourceId(R.styleable.DragBackLayout_shadow_left,
                R.drawable.shadow_left);
        int shadowRight = a.getResourceId(R.styleable.DragBackLayout_shadow_right,
                R.drawable.shadow_right);
        int shadowBottom = a.getResourceId(R.styleable.DragBackLayout_shadow_bottom,
                R.drawable.shadow_bottom);
        setShadow(shadowLeft, EDGE_LEFT);
        setShadow(shadowRight, EDGE_RIGHT);
        setShadow(shadowBottom, EDGE_BOTTOM);

        a.recycle();

        final float density = getResources().getDisplayMetrics().density;
        final float minVel = MIN_FLING_VELOCITY * density;

        // 设置最大最小速度
        mDragHelper.setMinVelocity(minVel);
        mDragHelper.setMaxVelocity(minVel * 2f);
    }

    /**
     * 设置滑动布局的敏感性, 即最小监听距离TouchSlop, 可以不设置
     *
     * @param context     上下文
     * @param sensitivity 值在(0~1]之间, 最终会控制滑动间距TouchSlop =
     *                    ViewConfiguration.getScaledTouchSlop * (1 / s);
     */
    @SuppressWarnings("unused")
    public void setSensitivity(Context context, float sensitivity) {
        mDragHelper.setSensitivity(context, sensitivity);
    }

    /**
     * 设置ContentView, 支持用户手势删除, 以前的ContentView被DragBackLayout取代.
     * ContentView成为DragBackLayout的ChildView
     *
     * @param view 视图
     */
    private void setContentView(View view) {
        mContentView = view;
    }

    /**
     * 设置是否支持手势
     *
     * @param enable 支持
     */
    public void setEnableGesture(boolean enable) {
        mEnable = enable;
    }

    /**
     * 设置滑动模式, 不同的滑动模式退出方式不同
     *
     * @param edgeFlags 滑动模式
     */
    public void setEdgeTrackingEnabled(int edgeFlags) {
        mEdgeFlag = edgeFlags;
        // 设置监听的拖拽方向
        mDragHelper.setEdgeTrackingEnabled(mEdgeFlag);
    }

    /**
     * 设置底部模糊的背景颜色
     *
     * @param color 颜色
     */
    public void setScrimColor(int color) {
        mScrimColor = color;
        invalidate();
    }


    /**
     * 设置边界距离, 在距离内滑动时, 才会触发后退效果
     *
     * @param size 边界距离
     */
    public void setEdgeSize(int size) {
        mDragHelper.setEdgeSize(size);
    }

    /**
     * 设置滑动回调, 当滑动时, 触发
     *
     * @param listener 监听器
     * @deprecated 使用替换{@link #addDragBackListener}
     */
    @Deprecated
    public void setDragBackListener(DragBackListener listener) {
        addDragBackListener(listener);
    }

    /**
     * 添加滑动回调, 当滑动时, 触发, 支持添加多个
     *
     * @param listener 监听器
     */
    public void addDragBackListener(DragBackListener listener) {
        if (mListeners == null) {
            mListeners = new ArrayList<>();
        }
        mListeners.add(listener);
    }

    /**
     * 删除滑动回调
     *
     * @param listener 监听器
     */
    public void removeDragBackListener(DragBackListener listener) {
        if (mListeners == null) {
            return;
        }
        mListeners.remove(listener);
    }

    public interface DragBackListener {

        /**
         * 当状态改变时调用
         *
         * @param state         状态
         * @param scrollPercent 滑动当前视图的百分比
         * @see #STATE_IDLE
         * @see #STATE_DRAGGING
         * @see #STATE_SETTLING
         */
        void onScrollStateChange(int state, float scrollPercent);

        /**
         * 当边界被触摸时调用
         *
         * @param edgeFlag 边界标记
         * @see #EDGE_LEFT
         * @see #EDGE_RIGHT
         * @see #EDGE_BOTTOM
         */
        void onEdgeTouch(int edgeFlag);

        /**
         * 当滑动百分比超过阈值时, 触发自动滑动
         */
        void onScrollOverThreshold();
    }

    /**
     * 设置滑动关闭阈值, 当超过这个阈值时, 页面自动关闭, 默认值{@link #DEFAULT_SCROLL_THRESHOLD}.
     *
     * @param threshold 阈值
     */
    public void setScrollThresHold(float threshold) {
        if (threshold >= 1.0f || threshold <= 0) {
            throw new IllegalArgumentException("Threshold value should be between 0 and 1.0");
        }
        mScrollThreshold = threshold;
    }

    /**
     * 设置边界阴影图片
     *
     * @param shadow   阴影图片
     * @param edgeFlag 滑动模式
     */
    public void setShadow(Drawable shadow, int edgeFlag) {
        if ((edgeFlag & EDGE_LEFT) != 0) {
            mShadowLeft = shadow;
        } else if ((edgeFlag & EDGE_RIGHT) != 0) {
            mShadowRight = shadow;
        } else if ((edgeFlag & EDGE_BOTTOM) != 0) {
            mShadowBottom = shadow;
        }
        invalidate();
    }

    /**
     * 设置边界阴影资源
     *
     * @param resId    图片资源
     * @param edgeFlag 滑动模式
     */
    public void setShadow(@DrawableRes int resId, int edgeFlag) {
        setShadow(getResources().getDrawable(resId), edgeFlag);
    }

    /**
     * 滑动当前页面, 并关闭Activity
     */
    public void scrollToFinishActivity() {
        final int childWidth = mContentView.getWidth();
        final int childHeight = mContentView.getHeight();

        int left = 0, top = 0;
        if ((mEdgeFlag & EDGE_LEFT) != 0) {
            left = childWidth + mShadowLeft.getIntrinsicWidth() + OVERSCROLL_DISTANCE;
            mTrackingEdge = EDGE_LEFT;
        } else if ((mEdgeFlag & EDGE_RIGHT) != 0) {
            left = -childWidth - mShadowRight.getIntrinsicWidth() - OVERSCROLL_DISTANCE;
            mTrackingEdge = EDGE_RIGHT;
        } else if ((mEdgeFlag & EDGE_BOTTOM) != 0) {
            top = -childHeight - mShadowBottom.getIntrinsicHeight() - OVERSCROLL_DISTANCE;
            mTrackingEdge = EDGE_BOTTOM;
        }

        mDragHelper.smoothSlideViewTo(mContentView, left, top);
        invalidate();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // 判断是否拦截, 交个Drag处理
        if (!mEnable) {
            return false;
        }
        try {
            return mDragHelper.shouldInterceptTouchEvent(event);
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 判断是否拦截, 交个Drag处理
        if (!mEnable) {
            return false;
        }
        mDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mInLayout = true;
        if (mContentView != null)
            mContentView.layout(mContentLeft, mContentTop,
                    mContentLeft + mContentView.getMeasuredWidth(),
                    mContentTop + mContentView.getMeasuredHeight());
        mInLayout = false;
    }

    @Override
    public void requestLayout() {
        if (!mInLayout) {
            super.requestLayout();
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        final boolean drawContent = child == mContentView; // 当前页面

        boolean ret = super.drawChild(canvas, child, drawingTime);

        if (mScrimOpacity > 0 && drawContent
                && mDragHelper.getViewDragState() != ViewDragHelper.STATE_IDLE) {
            drawShadow(canvas, child);
            drawScrim(canvas, child); // 绘制蒙版
        }
        return ret;
    }

    private void drawScrim(Canvas canvas, View child) {
        final int baseAlpha = (mScrimColor & 0xff000000) >>> 24;
        final int alpha = (int) (baseAlpha * mScrimOpacity);
        final int color = alpha << 24 | (mScrimColor & 0xffffff);

        // 判断是从哪一侧滑出
        if ((mTrackingEdge & EDGE_LEFT) != 0) {
            canvas.clipRect(0, 0, child.getLeft(), getHeight());
        } else if ((mTrackingEdge & EDGE_RIGHT) != 0) {
            canvas.clipRect(child.getRight(), 0, getRight(), getHeight());
        } else if ((mTrackingEdge & EDGE_BOTTOM) != 0) {
            canvas.clipRect(child.getLeft(), child.getBottom(), getRight(), getHeight());
        }
        canvas.drawColor(color);
    }

    // 绘制阴影
    private void drawShadow(Canvas canvas, View child) {
        final Rect childRect = mTmpRect;
        child.getHitRect(childRect);

        if ((mEdgeFlag & EDGE_LEFT) != 0) {
            mShadowLeft.setBounds(childRect.left - mShadowLeft.getIntrinsicWidth(), childRect.top,
                    childRect.left, childRect.bottom);
            mShadowLeft.setAlpha((int) (mScrimOpacity * FULL_ALPHA));
            mShadowLeft.draw(canvas);
        }

        if ((mEdgeFlag & EDGE_RIGHT) != 0) {
            mShadowRight.setBounds(childRect.right, childRect.top,
                    childRect.right + mShadowRight.getIntrinsicWidth(), childRect.bottom);
            mShadowRight.setAlpha((int) (mScrimOpacity * FULL_ALPHA));
            mShadowRight.draw(canvas);
        }

        if ((mEdgeFlag & EDGE_BOTTOM) != 0) {
            mShadowBottom.setBounds(childRect.left, childRect.bottom, childRect.right,
                    childRect.bottom + mShadowBottom.getIntrinsicHeight());
            mShadowBottom.setAlpha((int) (mScrimOpacity * FULL_ALPHA));
            mShadowBottom.draw(canvas);
        }
    }

    /**
     * 依附于当前的Activity, 在DecorView与当前布局之间, 添加DragBackLayout
     * 并设置ContentView为DragBackLayout
     * 供{@link DragBackHelper}调用
     *
     * @param activity 当前页面
     */
    public void attachToActivity(Activity activity) {
        mActivity = activity;
        TypedArray a = activity.getTheme().obtainStyledAttributes(new int[]{
                android.R.attr.windowBackground
        });
        int background = a.getResourceId(0, 0);
        a.recycle();

        ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();
        ViewGroup decorChild = (ViewGroup) decor.getChildAt(0);
        decorChild.setBackgroundResource(background);

        decor.removeView(decorChild);
        addView(decorChild);
        setContentView(decorChild);
        decor.addView(this);
    }

    @Override
    public void computeScroll() {
        mScrimOpacity = 1 - mScrollPercent;
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * 供DragHelper调用的Callback
     */
    private class ViewDragCallback extends ViewDragHelper.Callback {
        private boolean mIsScrollOverValid;

        @Override
        public boolean tryCaptureView(View view, int i) {
            boolean ret = mDragHelper.isEdgeTouched(mEdgeFlag, i); // 正确的边界
            if (ret) {
                if (mDragHelper.isEdgeTouched(EDGE_LEFT, i)) {
                    mTrackingEdge = EDGE_LEFT;
                } else if (mDragHelper.isEdgeTouched(EDGE_RIGHT, i)) {
                    mTrackingEdge = EDGE_RIGHT;
                } else if (mDragHelper.isEdgeTouched(EDGE_BOTTOM, i)) {
                    mTrackingEdge = EDGE_BOTTOM;
                }
                if (mListeners != null && !mListeners.isEmpty()) {
                    for (DragBackListener listener : mListeners) {
                        listener.onEdgeTouch(mTrackingEdge);
                    }
                }
                mIsScrollOverValid = true;
            }
            boolean directionCheck = false;
            if (mEdgeFlag == EDGE_LEFT || mEdgeFlag == EDGE_RIGHT) {
                directionCheck = !mDragHelper.checkTouchSlop(ViewDragHelper.DIRECTION_VERTICAL, i);
            } else if (mEdgeFlag == EDGE_BOTTOM) {
                directionCheck = !mDragHelper.checkTouchSlop(ViewDragHelper.DIRECTION_HORIZONTAL, i);
            } else if (mEdgeFlag == EDGE_ALL) {
                directionCheck = true;
            }
            return ret & directionCheck; // 正确的边界与屏幕方向正确
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return mEdgeFlag & (EDGE_LEFT | EDGE_RIGHT); // 水平
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return mEdgeFlag & EDGE_BOTTOM; // 竖直
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            if ((mTrackingEdge & EDGE_LEFT) != 0) {
                mScrollPercent = Math.abs((float) left
                        / (mContentView.getWidth() + mShadowLeft.getIntrinsicWidth()));
            } else if ((mTrackingEdge & EDGE_RIGHT) != 0) {
                mScrollPercent = Math.abs((float) left
                        / (mContentView.getWidth() + mShadowRight.getIntrinsicWidth()));
            } else if ((mTrackingEdge & EDGE_BOTTOM) != 0) {
                mScrollPercent = Math.abs((float) top
                        / (mContentView.getHeight() + mShadowBottom.getIntrinsicHeight()));
            }
            mContentLeft = left;
            mContentTop = top;
            invalidate();
            if (mScrollPercent < mScrollThreshold && !mIsScrollOverValid) {
                mIsScrollOverValid = true;
            }
            if (mListeners != null && !mListeners.isEmpty()
                    && mDragHelper.getViewDragState() == STATE_DRAGGING
                    && mScrollPercent >= mScrollThreshold && mIsScrollOverValid) {
                mIsScrollOverValid = false;
                for (DragBackListener listener : mListeners) {
                    listener.onScrollOverThreshold();
                }
            }

            // 滚动百分比大于1时, 关闭当前Activity, 屏蔽动画
            if (mScrollPercent >= 1) {
                if (!mActivity.isFinishing()) {
                    mActivity.finish();
                    mActivity.overridePendingTransition(0, 0);
                }
            }
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            // 当滑动一部分中途取消时, 恢复当前视图
            final int childWidth = releasedChild.getWidth();
            final int childHeight = releasedChild.getHeight();

            int left = 0, top = 0;
            if ((mTrackingEdge & EDGE_LEFT) != 0) {
                left = xvel > 0 || xvel == 0 && mScrollPercent > mScrollThreshold ? childWidth
                        + mShadowLeft.getIntrinsicWidth() + OVERSCROLL_DISTANCE : 0;
            } else if ((mTrackingEdge & EDGE_RIGHT) != 0) {
                left = xvel < 0 || xvel == 0 && mScrollPercent > mScrollThreshold ? -(childWidth
                        + mShadowLeft.getIntrinsicWidth() + OVERSCROLL_DISTANCE) : 0;
            } else if ((mTrackingEdge & EDGE_BOTTOM) != 0) {
                top = yvel < 0 || yvel == 0 && mScrollPercent > mScrollThreshold ? -(childHeight
                        + mShadowBottom.getIntrinsicHeight() + OVERSCROLL_DISTANCE) : 0;
            }

            mDragHelper.settleCapturedViewAt(left, top);
            invalidate();
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            // 固定在水平位置的回调
            int ret = 0;
            if ((mTrackingEdge & EDGE_LEFT) != 0) {
                ret = Math.min(child.getWidth(), Math.max(left, 0));
            } else if ((mTrackingEdge & EDGE_RIGHT) != 0) {
                ret = Math.min(0, Math.max(left, -child.getWidth()));
            }
            return ret;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            // 固定在竖直位置的回调
            int ret = 0;
            if ((mTrackingEdge & EDGE_BOTTOM) != 0) {
                ret = Math.min(0, Math.max(top, -child.getHeight()));
            }
            return ret;
        }

        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);
            if (mListeners != null && !mListeners.isEmpty()) {
                for (DragBackListener listener : mListeners) {
                    listener.onScrollStateChange(state, mScrollPercent);
                }
            }
        }
    }
}

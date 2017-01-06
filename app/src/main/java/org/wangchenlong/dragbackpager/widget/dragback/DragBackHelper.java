package org.wangchenlong.dragbackpager.widget.dragback;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.wangchenlong.dragbackpager.R;


/**
 * 滑动后退的适配器
 */
public class DragBackHelper {
    private Activity mActivity;

    private DragBackLayout mDragBackLayout;

    public DragBackHelper(@NonNull Activity activity) {
        mActivity = activity; // 添加Activity参数
    }

    /**
     * 在Activity的onCreate时, 优先执行
     */
    public void onActivityCreate() {
        // 设置Window的背景图片透明
        mActivity.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // 删除DecorView的背景
        mActivity.getWindow().getDecorView().setBackground(null);

        final ViewGroup vg = null; // 布局组件

        // 获取DragBack的填充布局
        mDragBackLayout = (DragBackLayout) LayoutInflater
                .from(mActivity).inflate(R.layout.drag_back_layout, vg);

        // 添加滑动监听器
        mDragBackLayout.addDragBackListener(new DragBackLayout.DragBackListener() {
            @Override
            public void onScrollStateChange(int state, float scrollPercent) {
            }

            @Override
            public void onEdgeTouch(int edgeFlag) {
                // 每次触摸屏幕边界时, 使当前Activity变为透明
                Utils.convertActivityToTranslucent(mActivity);
            }

            @Override
            public void onScrollOverThreshold() {
            }
        });
    }

    /**
     * DragBackLayout依附于Activity
     */
    public void onPostCreate() {
        // 将DragBackLayout加入DecorView与ContentView之间, 并设置ContentView为DecorView.
        mDragBackLayout.attachToActivity(mActivity);
    }

    /**
     * 替换根布局为DragBackLayout
     *
     * @param id 布局ID
     * @return 视图
     */
    public View findViewById(int id) {
        if (mDragBackLayout != null) {
            // findViewById不是直接ContentView, 而是在DragBackLayout中寻找.
            return mDragBackLayout.findViewById(id);
        }
        return null;
    }

    /**
     * 获取布局
     *
     * @return 布局
     */
    public DragBackLayout getDragBackLayout() {
        return mDragBackLayout;
    }
}

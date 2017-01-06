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
        mActivity = activity;
    }

    @SuppressWarnings("deprecation")
    public void onActivityCreate() {
        mActivity.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mActivity.getWindow().getDecorView().setBackgroundDrawable(null);

        final ViewGroup vg = null;
        mDragBackLayout = (DragBackLayout)
                LayoutInflater.from(mActivity).inflate(R.layout.drag_back_layout, vg);

        mDragBackLayout.addDragListener(new DragBackLayout.DragListener() {
            @Override
            public void onScrollStateChange(int state, float scrollPercent) {
            }

            @Override
            public void onEdgeTouch(int edgeFlag) {
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

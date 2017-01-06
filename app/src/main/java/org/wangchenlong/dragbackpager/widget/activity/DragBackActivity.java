
package org.wangchenlong.dragbackpager.widget.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.wangchenlong.dragbackpager.widget.dragback.DragBackHelper;
import org.wangchenlong.dragbackpager.widget.dragback.DragBackInterface;
import org.wangchenlong.dragbackpager.widget.dragback.DragBackLayout;
import org.wangchenlong.dragbackpager.widget.dragback.Utils;

public class DragBackActivity extends AppCompatActivity implements DragBackInterface {
    private DragBackHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHelper = new DragBackHelper(this);
        mHelper.onActivityCreate();
        mHelper.getDragBackLayout().setEdgeTrackingEnabled(getDragMode());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mHelper.onPostCreate();
    }

    @Override
    public View findViewById(int id) {
        View v = super.findViewById(id);
        if (v == null && mHelper != null)
            return mHelper.findViewById(id);
        return v;
    }

    // 获取滑动模式, 默认左侧滑动
    protected int getDragMode() {
        return DragBackLayout.EDGE_LEFT;
    }

    @Override
    public DragBackLayout getDragBackLayout() {
        return mHelper.getDragBackLayout();
    }

    @Override
    public void setDragBackEnable(boolean enable) {
        getDragBackLayout().setEnableGesture(enable);
    }

    @Override
    public void scrollToFinishActivity() {
        Utils.convertActivityToTranslucent(this);
        getDragBackLayout().scrollToFinishActivity(); // 自动滑动关闭
    }
}

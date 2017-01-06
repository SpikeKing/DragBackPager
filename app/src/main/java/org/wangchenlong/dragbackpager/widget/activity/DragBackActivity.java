
package org.wangchenlong.dragbackpager.widget.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.wangchenlong.dragbackpager.widget.dragback.DragBackHelper;
import org.wangchenlong.dragbackpager.widget.dragback.DragBackInterface;
import org.wangchenlong.dragbackpager.widget.dragback.DragBackLayout;
import org.wangchenlong.dragbackpager.widget.dragback.Utils;

/**
 * 滑动退出的基类, 集成基类即可使用, 或者把基类的方法移至已有基类
 */
public class DragBackActivity extends AppCompatActivity implements DragBackInterface {
    private DragBackHelper mHelper; // 滑动后退的代理

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHelper = new DragBackHelper(this); // 初始化滑动管理器
        mHelper.onActivityCreate(); // 执行创建过程

        // 设置滑动后退模式, 可以选择上下左右和全部, 默认左侧滑动退出
        mHelper.getDragBackLayout().setEdgeTrackingEnabled(getDragMode());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mHelper.onPostCreate(); // 执行顶层页面的切换
    }

    @Override
    public View findViewById(int id) {
        // 重写findViewById, 替换为DragBackLayout的布局
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
        return mHelper.getDragBackLayout(); // 获取回退布局
    }

    @Override
    public void setDragBackEnable(boolean enable) {
        getDragBackLayout().setEnableGesture(enable); // 是否支持手势, 添加开关
    }

    @Override
    public void scrollToFinishActivity() {
        Utils.convertActivityToTranslucent(this);  // 把Activity转换为透明样式
        getDragBackLayout().scrollToFinishActivity(); // 自动滑动关闭
    }
}

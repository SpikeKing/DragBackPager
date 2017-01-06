package org.wangchenlong.dragbackpager.widget.dragback;

/**
 * 后退页面接口, 必要的实现, 供顶层调用
 *
 * @author C.L. Wang
 */
public interface DragBackInterface {
    /**
     * 获取滑动后退的顶层布局, 因为实现手势功能, 所以改变顶层布局
     */
    DragBackLayout getDragBackLayout(); // 获取滑动后退的布局

    /**
     * 是否启动滑动后退效果
     */
    void setDragBackEnable(boolean enable);

    /**
     * 执行滑动关闭页面, 拥有效果
     */
    void scrollToFinishActivity();
}

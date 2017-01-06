package org.wangchenlong.dragbackpager.widget.dragback;

/**
 * 后退页面接口
 */
public interface DragBackInterface {
    /**
     * @return the DragBackLayout associated with this activity.
     */
    DragBackLayout getDragBackLayout();

    void setDragBackEnable(boolean enable);

    /**
     * Scroll out contentView and finish the activity
     */
    void scrollToFinishActivity();
}

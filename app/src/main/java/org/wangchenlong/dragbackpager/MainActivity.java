package org.wangchenlong.dragbackpager;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.ImageView;

import org.wangchenlong.dragbackpager.widget.activity.DragBackActivity;
import org.wangchenlong.dragbackpager.widget.dragback.DragBackLayout;

/**
 * 主页集成滑动后退的类, 左侧滑动后退
 *
 * @author wangchenlong
 */
public class MainActivity extends DragBackActivity {

    public static int NUM = 0; // 当前ID递增

    // 图片属性
    public static int[] IMAGES = new int[]{
            R.drawable.taeyeon,
            R.drawable.tiffany,
            R.drawable.yoona,
            R.drawable.jessica,
            R.drawable.seohyun
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 创建NUM+1;
        ImageView iv = (ImageView) findViewById(R.id.main_iv_bkg);
        iv.setImageResource(getImageDrawable(NUM++ % IMAGES.length));

        // 更新标题
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(String.valueOf("Page No. " + NUM));
        }

        getDragBackLayout().addDragListener(new DragBackLayout.DragListener() {
            @Override
            public void onScrollStateChange(int state, float scrollPercent) {
                if (state == DragBackLayout.STATE_SETTLING) {
                    NUM--; // 关闭NUM-1
                }
            }

            @Override
            public void onEdgeTouch(int edgeFlag) {
            }

            @Override
            public void onScrollOverThreshold() {
            }
        });
    }

    /**
     * 获取背景图片
     *
     * @param index 索引
     * @return 图片资源
     */
    private @DrawableRes int getImageDrawable(int index) {
        return IMAGES[index];
    }

    /**
     * 启动新页面
     *
     * @param view 被点击的View
     */
    public void newActivity(View view) {
        startActivity(new Intent(MainActivity.this, MainActivity.class));
    }

    @Override public void onBackPressed() {
        scrollToFinishActivity(); // 后退使用滑动效果
    }
}

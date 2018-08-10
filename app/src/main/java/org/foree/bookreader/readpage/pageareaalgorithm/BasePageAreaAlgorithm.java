package org.foree.bookreader.readpage.pageareaalgorithm;

/**
 * @author foree
 * @date 2018/8/10
 * @description 翻页触摸区域算法超类
 * 图形表示法： x表示上一页，-表示菜单，o表示下一页
 */
public abstract class BasePageAreaAlgorithm {
    private float width, height;

    /**
     * 当前坐标是否在菜单点击区域
     *
     * @param x 屏幕x坐标
     * @param y 屏幕y坐标
     * @return
     */
    boolean isMenuArea(float x, float y) {
        checkArgument();
        return false;
    }

    /**
     * 传入坐标是否在上一页点击区域
     *
     * @param x 屏幕x坐标
     * @param y 屏幕y坐标
     * @return
     */
    boolean isPreArea(float x, float y) {
        checkArgument();
        return false;
    }

    /**
     * 传入坐标是否在下一页点击区域
     *
     * @param x
     * @param y
     * @return
     */
    boolean isNextArea(float x, float y) {
        checkArgument();
        return false;
    }

    /**
     * 用于在屏幕方向，屏幕区域变化时更新尺寸
     *
     * @param width  在当前时间点，当前方向屏幕的宽度
     * @param height 在当前时间点，当前方向屏幕高度
     */
    public void initScreenSize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    private void checkArgument() {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width:" + width + " or height:" + height + " must be more than zero");
        }
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}

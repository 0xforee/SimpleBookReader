package org.foree.bookreader.readpage;

/**
 * @author foree
 * @date 2018/8/10
 * @description 翻页触摸区域算法超类
 */
public abstract class BasePageAreaAlgorithm {
    int width, height;

    public BasePageAreaAlgorithm(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * 当前坐标是否在菜单点击区域
     * @param x 屏幕x坐标
     * @param y 屏幕y坐标
     * @return
     */
    abstract boolean isMenuArea(int x, int y);

    /**
     * 传入坐标是否在上一页点击区域
     * @param x 屏幕x坐标
     * @param y 屏幕y坐标
     * @return
     */
    abstract boolean isPreArea(int x, int y);

    /**
     * 传入坐标是否在下一页点击区域
     * @param x
     * @param y
     * @return
     */
    abstract boolean isNextArea(int x, int y);
}

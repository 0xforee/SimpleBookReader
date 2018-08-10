package org.foree.bookreader.readpage.pageareaalgorithm;

/**
 * @author foree
 * @date 2018/8/11
 * @description 算法C: 屏幕height 5分，屏幕width 4分
 * 图形表示法： x表示上一页，-表示菜单，o表示下一页
 * - - - -
 * x o o o
 * x o o o
 * x o o o
 * x o o o
 */
class PageAreaAlgorithmC extends BasePageAreaAlgorithm {
    /**
     * 定义菜单事件响应区域
     *
     * @param x 屏幕x坐标
     * @param y 屏幕y坐标
     * @return
     */
    @Override
    boolean isMenuArea(float x, float y) {
        super.isMenuArea(x, y);
        if( y < getHeight() * 1 / 5){
            return true;
        }
        return false;
    }

    /**
     * 定义上一页事件响应区域
     *
     * @param x 屏幕x坐标
     * @param y 屏幕y坐标
     * @return
     */
    @Override
    boolean isPreArea(float x, float y) {
        super.isPreArea(x, y);
        if( y > getHeight() * 1 / 5 && x < getWidth() * 1 / 4){
            return true;
        }
        return false;
    }

    /**
     * 定义下一页事件响应区域
     *
     * @param x
     * @param y
     * @return
     */
    @Override
    boolean isNextArea(float x, float y) {
        super.isNextArea(x, y);
        if(x > getWidth() * 1 / 4 && y > getHeight() * 1 / 5){
            return true;
        }
        return false;
    }
}

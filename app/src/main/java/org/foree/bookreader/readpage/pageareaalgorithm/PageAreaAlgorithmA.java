package org.foree.bookreader.readpage.pageareaalgorithm;

/**
 * @author foree
 * @date 2018/8/10
 * @description 算法A：屏幕height 5分，width 3分
 * x表示上一页，-表示菜单，o表示下一页
 * x x o
 * x - o
 * x - o
 * x - o
 * x o o
 */
class PageAreaAlgorithmA extends BasePageAreaAlgorithm {

    @Override
    boolean isMenuArea(float x, float y) {
        super.isMenuArea(x, y);
        // 取横5分竖3分屏幕的区域
        if (x > getWidth() * 1 / 3 && x < getWidth() * 2 / 3) {
            if (y > getHeight() * 1 / 5 && y < getHeight() * 4 / 5) {
                return true;
            }
        }
        return false;
    }

    @Override
    boolean isPreArea(float x, float y) {
        super.isPreArea(x, y);
        if (x < getWidth() * 1 / 3 ||
                (x > getWidth() * 1 / 3 && x < getWidth() * 2 / 3 && y < getHeight() * 1 / 5)) {
            return true;
        }
        return false;
    }

    @Override
    boolean isNextArea(float x, float y) {
        super.isNextArea(x, y);
        if (x > getWidth() * 2 / 3 ||
                (x > getWidth() * 1 / 3 && x < getWidth() * 2 / 3 && y > getHeight() * 4 / 5)) {
            return true;
        }
        return false;
    }
}

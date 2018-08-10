package org.foree.bookreader.readpage.pageareaalgorithm;

/**
 * @author foree
 * @date 2018/8/10
 * @description 算法B: 屏幕height 三分
 * x
 * -
 * o
 */
class PageAreaAlgorithmB extends BasePageAreaAlgorithm {

    @Override
    boolean isMenuArea(float x, float y) {
        super.isMenuArea(x, y);
        if (y > getHeight() * 1 / 3 && y < getHeight() * 2 / 3) {
            return true;
        }
        return false;
    }

    @Override
    boolean isPreArea(float x, float y) {
        if( y < getHeight() * 1 / 3){
            return true;
        }
        return false;
    }

    @Override
    boolean isNextArea(float x, float y) {
        if( y > getHeight() * 2 / 3){
            return true;
        }
        return false;
    }
}

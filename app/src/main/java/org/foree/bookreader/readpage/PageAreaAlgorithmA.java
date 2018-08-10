package org.foree.bookreader.readpage;

/**
 * @author foree
 * @date 2018/8/10
 * @description 算法A
 */
public class PageAreaAlgorithmA extends BasePageAreaAlgorithm {
    public PageAreaAlgorithmA(int width, int height) {
        super(width, height);
    }

    @Override
    boolean isMenuArea(int x, int y) {
        return false;
    }

    @Override
    boolean isPreArea(int x, int y) {
        return false;
    }

    @Override
    boolean isNextArea(int x, int y) {
        return false;
    }
}

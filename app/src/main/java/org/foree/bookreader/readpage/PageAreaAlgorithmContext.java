package org.foree.bookreader.readpage;

/**
 * @author foree
 * @date 2018/8/10
 * @description
 */
public class PageAreaAlgorithmContext {
    private BasePageAreaAlgorithm algorithm;
    private int width;
    private int height;

    public PageAreaAlgorithmContext(int width, int height) {


    }

    public void updateAlgorithm(String type){
        switch (type) {
            case "A":
                algorithm = new PageAreaAlgorithmA();
                break;
            case "B":
                algorithm = new PageAreaAlgorithmB();
                break;
            default:
                break;
        }
    }

    public boolean isMenuArea(int x, int y) {
        return algorithm.isMenuArea(x, y);
    }

    public boolean isPreArea(int x, int y) {
        return algorithm.isPreArea(x, y);
    }

    public boolean isNextAre(int x, int y) {
        return algorithm.isNextArea(x, y);
    }
}

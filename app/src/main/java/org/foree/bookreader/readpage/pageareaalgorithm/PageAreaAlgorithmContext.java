package org.foree.bookreader.readpage.pageareaalgorithm;

/**
 * @author foree
 * @date 2018/8/10
 * @description
 */
public class PageAreaAlgorithmContext {
    private static final String TAG = "PageAreaAlgorithmContext";
    private BasePageAreaAlgorithm algorithm;

    public enum ALGORITHM {
        /**
         * 枚举触摸区域算法可以使用的算法
         */
        A("A"),
        B("B"),
        C("C");

        private String type;
        ALGORITHM(String type){
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
    private float width;
    private float height;

    public PageAreaAlgorithmContext(ALGORITHM type) {
        updateAlgorithm(type);
    }

    public void updateAlgorithm(ALGORITHM type){
        try {

            Class<?> clazz = Class.forName(getClass().getPackage().getName() + ".PageAreaAlgorithm" + type.getType());
            algorithm = (BasePageAreaAlgorithm) clazz.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        algorithm.initScreenSize(width, height);
    }

    public boolean isMenuArea(float x, float y) {
        return algorithm.isMenuArea(x, y);
    }

    public boolean isPreArea(float x, float y) {
        return algorithm.isPreArea(x, y);
    }

    public boolean isNextAre(float x, float y) {
        return algorithm.isNextArea(x, y);
    }

    public void updateScreenSize(float width, float height){
        this.width = width;
        this.height = height;
        if(algorithm != null){
            algorithm.initScreenSize(width, height);
        }
    }
}

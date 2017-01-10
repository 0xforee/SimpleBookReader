package org.foree.bookreader;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void convertUrl2Id(){
        String url = "/0_168/2512063.html";
        String url2 = "http://m.bxwx9.org/b/98/98289//0_168/2512063.html";
        String id = null;

        String[] subString = url2.split("/|\\.");
        id = subString[subString.length-2];

        assertEquals(id, "2512063");
    }
}
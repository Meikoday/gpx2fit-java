package com.yangyang5214.gpx2fit;

import org.junit.Test;


public class AppTest {

    @Test
    public void testAppId() {
        byte[] appId = new byte[]{
                0x1, 0x1, 0x1, 0x1,
                0x2, 0x2, 0x2, 0x2,
                0x3, 0x3, 0x3, 0x3,
                0x4, 0x4, 0x4, 0x4,
        };
        String s = new String(appId);
        System.out.println(s);
        System.out.println(s.length());


        byte[] bytes = "gpxtgpxtgpxtgpxt".getBytes();
        System.out.println(bytes.length);

    }

}
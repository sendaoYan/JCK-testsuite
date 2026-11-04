/*
 * @test
 * @summary javax.imageio.ImageIO conformance: behavioral.
 * @run main ImageIOTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ImageIOTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        String[] rn = javax.imageio.ImageIO.getReaderFormatNames();
        check(rn != null && rn.length > 0, "getReaderFormatNames non-empty");
        String[] wm = javax.imageio.ImageIO.getWriterFormatNames();
        check(wm != null && wm.length > 0, "getWriterFormatNames non-empty");
        check(javax.imageio.ImageIO.getReaderMIMETypes() != null, "getReaderMIMETypes");
        check(javax.imageio.ImageIO.getWriterMIMETypes() != null, "getWriterMIMETypes");
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(4, 4, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        check(javax.imageio.ImageIO.write(img, "png", bos), "write png");
        java.awt.image.BufferedImage back = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(bos.toByteArray()));
        check(back != null && back.getWidth() == 4 && back.getHeight() == 4, "read png");
        javax.imageio.stream.ImageInputStream iis = javax.imageio.ImageIO.createImageInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
        check(iis != null, "createImageInputStream");
        iis.close();
        javax.imageio.stream.ImageOutputStream ios = javax.imageio.ImageIO.createImageOutputStream(new java.io.ByteArrayOutputStream());
        check(ios != null, "createImageOutputStream");
        ios.close();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

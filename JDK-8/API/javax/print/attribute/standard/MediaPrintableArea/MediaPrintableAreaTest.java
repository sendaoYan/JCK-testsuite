/*
 * @test
 * @summary javax.print.attribute.standard.MediaPrintableArea conformance: printable area attribute.
 * @run main MediaPrintableAreaTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class MediaPrintableAreaTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        MediaPrintableArea v = new MediaPrintableArea(10f, 20f, 200f, 250f, MediaPrintableArea.MM);
        check(Math.abs(v.getX(MediaPrintableArea.MM) - 10f) < 0.001f, "getX");
        check(Math.abs(v.getY(MediaPrintableArea.MM) - 20f) < 0.001f, "getY");
        check(Math.abs(v.getWidth(MediaPrintableArea.MM) - 200f) < 0.001f, "getWidth");
        check(Math.abs(v.getHeight(MediaPrintableArea.MM) - 250f) < 0.001f, "getHeight");
        float[] pa = v.getPrintableArea(MediaPrintableArea.MM);
        check(pa.length == 4 && Math.abs(pa[0] - 10f) < 0.001f && Math.abs(pa[2] - 200f) < 0.001f, "getPrintableArea");
        check(v.getCategory() == MediaPrintableArea.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new MediaPrintableArea(10f, 20f, 200f, 250f, MediaPrintableArea.MM)), "equals");
        check(v.hashCode() == new MediaPrintableArea(10f, 20f, 200f, 250f, MediaPrintableArea.MM).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

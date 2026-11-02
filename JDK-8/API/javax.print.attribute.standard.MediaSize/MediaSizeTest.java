/*
 * @test
 * @summary javax.print.attribute.standard.MediaSize conformance: media size class.
 * @run main MediaSizeTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class MediaSizeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        MediaSize v = new MediaSize(210f, 297f, MediaSize.MM);
        check(Math.abs(v.getX(MediaSize.MM) - 210f) < 0.001f, "getX");
        check(Math.abs(v.getY(MediaSize.MM) - 297f) < 0.001f, "getY");
        check(MediaSize.ISO.A4.getMediaSizeName() == MediaSizeName.ISO_A4, "ISO_A4 name");
        check(MediaSize.getMediaSizeForName(MediaSizeName.ISO_A4) != null, "getMediaSizeForName");
        check(v.getCategory() == MediaSize.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new MediaSize(210f, 297f, MediaSize.MM)), "equals");
        check(v.hashCode() == new MediaSize(210f, 297f, MediaSize.MM).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.print.attribute.standard.MediaSizeName conformance: media enum attribute.
 * @run main MediaSizeNameTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class MediaSizeNameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        MediaSizeName v1 = MediaSizeName.ISO_A4;
        check(v1 instanceof MediaSizeName, "ISO_A4 instanceof MediaSizeName");
        check(v1.getCategory() == Media.class, "getCategory");
        check(v1.getName() != null && v1.getName().length() > 0, "getName non-empty");
        check(v1.toString() != null && v1.toString().length() > 0, "toString non-empty");
        check(v1.getValue() >= 0, "getValue >= 0");
        MediaSizeName v2 = MediaSizeName.NA_LETTER;
        check(v1.getValue() != v2.getValue(), "distinct getValue");
        check(!v1.toString().equals(v2.toString()), "distinct toString");
        check(v1.equals(MediaSizeName.ISO_A4), "equals same constant");
        check(!v1.equals(v2), "not equals other");
        check(v1.hashCode() == MediaSizeName.ISO_A4.hashCode(), "hashCode");
        check(v1.clone() != null, "clone");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

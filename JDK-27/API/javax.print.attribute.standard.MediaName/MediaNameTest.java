/*
 * @test
 * @summary javax.print.attribute.standard.MediaName conformance: media enum attribute.
 * @run main MediaNameTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class MediaNameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        MediaName v1 = MediaName.NA_LETTER_WHITE;
        check(v1 instanceof MediaName, "NA_LETTER_WHITE instanceof MediaName");
        check(v1.getCategory() == Media.class, "getCategory");
        check(v1.getName() != null && v1.getName().length() > 0, "getName non-empty");
        check(v1.toString() != null && v1.toString().length() > 0, "toString non-empty");
        check(v1.getValue() >= 0, "getValue >= 0");
        MediaName v2 = MediaName.ISO_A4_WHITE;
        check(v1.getValue() != v2.getValue(), "distinct getValue");
        check(!v1.toString().equals(v2.toString()), "distinct toString");
        check(v1.equals(MediaName.NA_LETTER_WHITE), "equals same constant");
        check(!v1.equals(v2), "not equals other");
        check(v1.hashCode() == MediaName.NA_LETTER_WHITE.hashCode(), "hashCode");
        check(v1.clone() != null, "clone");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

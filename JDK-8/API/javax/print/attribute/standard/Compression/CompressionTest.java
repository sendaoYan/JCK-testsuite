/*
 * @test
 * @summary javax.print.attribute.standard.Compression conformance: enum attribute.
 * @run main CompressionTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class CompressionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        Compression v1 = Compression.NONE;
        check(v1 instanceof Compression, "NONE instanceof Compression");
        check(v1.getCategory() == Compression.class, "getCategory");
        check(v1.getName() != null && v1.getName().length() > 0, "getName non-empty");
        check(v1.toString() != null && v1.toString().length() > 0, "toString non-empty");
        check(v1.getValue() >= 0, "getValue >= 0");
        Compression v2 = Compression.DEFLATE;
        check(v1.getValue() != v2.getValue(), "distinct getValue");
        check(!v1.toString().equals(v2.toString()), "distinct toString");
        check(v1.equals(Compression.NONE), "equals same constant");
        check(!v1.equals(v2), "not equals other");
        check(v1.hashCode() == Compression.NONE.hashCode(), "hashCode");
        check(v1.clone() != null, "clone");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

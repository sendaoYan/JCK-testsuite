/*
 * @test
 * @summary javax.print.attribute.standard.Chromaticity conformance: enum attribute.
 * @run main ChromaticityTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class ChromaticityTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        Chromaticity v1 = Chromaticity.MONOCHROME;
        check(v1 instanceof Chromaticity, "MONOCHROME instanceof Chromaticity");
        check(v1.getCategory() == Chromaticity.class, "getCategory");
        check(v1.getName() != null && v1.getName().length() > 0, "getName non-empty");
        check(v1.toString() != null && v1.toString().length() > 0, "toString non-empty");
        check(v1.getValue() >= 0, "getValue >= 0");
        Chromaticity v2 = Chromaticity.COLOR;
        check(v1.getValue() != v2.getValue(), "distinct getValue");
        check(!v1.toString().equals(v2.toString()), "distinct toString");
        check(v1.equals(Chromaticity.MONOCHROME), "equals same constant");
        check(!v1.equals(v2), "not equals other");
        check(v1.hashCode() == Chromaticity.MONOCHROME.hashCode(), "hashCode");
        check(v1.clone() != null, "clone");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.print.attribute.standard.Fidelity conformance: enum attribute.
 * @run main FidelityTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class FidelityTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        Fidelity v1 = Fidelity.FIDELITY_TRUE;
        check(v1 instanceof Fidelity, "FIDELITY_TRUE instanceof Fidelity");
        check(v1.getCategory() == Fidelity.class, "getCategory");
        check(v1.getName() != null && v1.getName().length() > 0, "getName non-empty");
        check(v1.toString() != null && v1.toString().length() > 0, "toString non-empty");
        check(v1.getValue() >= 0, "getValue >= 0");
        Fidelity v2 = Fidelity.FIDELITY_FALSE;
        check(v1.getValue() != v2.getValue(), "distinct getValue");
        check(!v1.toString().equals(v2.toString()), "distinct toString");
        check(v1.equals(Fidelity.FIDELITY_TRUE), "equals same constant");
        check(!v1.equals(v2), "not equals other");
        check(v1.hashCode() == Fidelity.FIDELITY_TRUE.hashCode(), "hashCode");
        check(v1.clone() != null, "clone");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

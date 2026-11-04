/*
 * @test
 * @summary javax.print.attribute.standard.NumberOfDocuments conformance: integer attribute.
 * @run main NumberOfDocumentsTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class NumberOfDocumentsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        NumberOfDocuments v = new NumberOfDocuments(10);
        check(v.getValue() == 10, "getValue");
        check(v.getCategory() == NumberOfDocuments.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new NumberOfDocuments(10)), "equals");
        check(!v.equals(new NumberOfDocuments(20)), "not equals");
        check(v.hashCode() == new NumberOfDocuments(10).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

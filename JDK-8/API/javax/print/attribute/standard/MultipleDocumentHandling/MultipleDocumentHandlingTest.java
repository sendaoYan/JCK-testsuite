/*
 * @test
 * @summary javax.print.attribute.standard.MultipleDocumentHandling conformance: enum attribute.
 * @run main MultipleDocumentHandlingTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class MultipleDocumentHandlingTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        MultipleDocumentHandling v1 = MultipleDocumentHandling.SINGLE_DOCUMENT;
        check(v1 instanceof MultipleDocumentHandling, "SINGLE_DOCUMENT instanceof MultipleDocumentHandling");
        check(v1.getCategory() == MultipleDocumentHandling.class, "getCategory");
        check(v1.getName() != null && v1.getName().length() > 0, "getName non-empty");
        check(v1.toString() != null && v1.toString().length() > 0, "toString non-empty");
        check(v1.getValue() >= 0, "getValue >= 0");
        MultipleDocumentHandling v2 = MultipleDocumentHandling.SEPARATE_DOCUMENTS_UNCOLLATED_COPIES;
        check(v1.getValue() != v2.getValue(), "distinct getValue");
        check(!v1.toString().equals(v2.toString()), "distinct toString");
        check(v1.equals(MultipleDocumentHandling.SINGLE_DOCUMENT), "equals same constant");
        check(!v1.equals(v2), "not equals other");
        check(v1.hashCode() == MultipleDocumentHandling.SINGLE_DOCUMENT.hashCode(), "hashCode");
        check(v1.clone() != null, "clone");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

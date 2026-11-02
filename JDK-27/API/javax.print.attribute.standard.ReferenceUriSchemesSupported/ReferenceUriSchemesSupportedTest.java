/*
 * @test
 * @summary javax.print.attribute.standard.ReferenceUriSchemesSupported conformance: enum attribute.
 * @run main ReferenceUriSchemesSupportedTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class ReferenceUriSchemesSupportedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        ReferenceUriSchemesSupported v1 = ReferenceUriSchemesSupported.FTP;
        check(v1 instanceof ReferenceUriSchemesSupported, "FTP instanceof ReferenceUriSchemesSupported");
        check(v1.getCategory() == ReferenceUriSchemesSupported.class, "getCategory");
        check(v1.getName() != null && v1.getName().length() > 0, "getName non-empty");
        check(v1.toString() != null && v1.toString().length() > 0, "toString non-empty");
        check(v1.getValue() >= 0, "getValue >= 0");
        ReferenceUriSchemesSupported v2 = ReferenceUriSchemesSupported.HTTP;
        check(v1.getValue() != v2.getValue(), "distinct getValue");
        check(!v1.toString().equals(v2.toString()), "distinct toString");
        check(v1.equals(ReferenceUriSchemesSupported.FTP), "equals same constant");
        check(!v1.equals(v2), "not equals other");
        check(v1.hashCode() == ReferenceUriSchemesSupported.FTP.hashCode(), "hashCode");
        check(v1.clone() != null, "clone");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

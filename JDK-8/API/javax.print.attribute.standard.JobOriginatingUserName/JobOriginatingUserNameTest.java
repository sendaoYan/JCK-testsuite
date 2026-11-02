/*
 * @test
 * @summary javax.print.attribute.standard.JobOriginatingUserName conformance: text attribute.
 * @run main JobOriginatingUserNameTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class JobOriginatingUserNameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        JobOriginatingUserName v = new JobOriginatingUserName("sample-text", java.util.Locale.US);
        check("sample-text".equals(v.getValue()), "getValue");
        check(java.util.Locale.US.equals(v.getLocale()), "getLocale");
        check(v.getCategory() == JobOriginatingUserName.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new JobOriginatingUserName("sample-text", java.util.Locale.US)), "equals");
        check(v.hashCode() == new JobOriginatingUserName("sample-text", java.util.Locale.US).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.print.attribute.standard.RequestingUserName conformance: text attribute.
 * @run main RequestingUserNameTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class RequestingUserNameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        RequestingUserName v = new RequestingUserName("sample-text", java.util.Locale.US);
        check("sample-text".equals(v.getValue()), "getValue");
        check(java.util.Locale.US.equals(v.getLocale()), "getLocale");
        check(v.getCategory() == RequestingUserName.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new RequestingUserName("sample-text", java.util.Locale.US)), "equals");
        check(v.hashCode() == new RequestingUserName("sample-text", java.util.Locale.US).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

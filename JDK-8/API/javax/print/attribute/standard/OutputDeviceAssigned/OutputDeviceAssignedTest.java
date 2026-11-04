/*
 * @test
 * @summary javax.print.attribute.standard.OutputDeviceAssigned conformance: text attribute.
 * @run main OutputDeviceAssignedTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class OutputDeviceAssignedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        OutputDeviceAssigned v = new OutputDeviceAssigned("sample-text", java.util.Locale.US);
        check("sample-text".equals(v.getValue()), "getValue");
        check(java.util.Locale.US.equals(v.getLocale()), "getLocale");
        check(v.getCategory() == OutputDeviceAssigned.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new OutputDeviceAssigned("sample-text", java.util.Locale.US)), "equals");
        check(v.hashCode() == new OutputDeviceAssigned("sample-text", java.util.Locale.US).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

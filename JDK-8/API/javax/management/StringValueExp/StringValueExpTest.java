/*
 * @test
 * @summary javax.management.StringValueExp conformance: value access, toString
 *          quoting, apply() returning self, and setMBeanServer no-op.
 * @run main StringValueExpTest
 */

import javax.management.ObjectName;
import javax.management.StringValueExp;

public class StringValueExpTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        StringValueExp e = new StringValueExp("hello");
        check("hello".equals(e.getValue()), "getValue");
        check("'hello'".equals(e.toString()), "toString quoted");
        check(e.toString() != null, "toString");
        check(new StringValueExp().getValue() == null, "no-arg null value");
        e.setMBeanServer(null);

        check(e.apply(new ObjectName("d:type=x")) == e, "apply returns self");
        check(new StringValueExp("it's").toString().equals("'it''s'"), "toString escapes quote");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

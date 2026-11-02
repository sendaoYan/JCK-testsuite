/*
 * @test
 * @summary javax.management.Attribute conformance: name/value pair.
 * @run main AttributeTest
 */

import javax.management.*;
public class AttributeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Attribute a = new Attribute("Count", Integer.valueOf(5));
        check("Count".equals(a.getName()), "name");
        check(Integer.valueOf(5).equals(a.getValue()), "value");
        check(a.equals(new Attribute("Count", Integer.valueOf(5))), "equals");
        check(!a.equals(new Attribute("Count", Integer.valueOf(6))), "not equals different value");
        check(a.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

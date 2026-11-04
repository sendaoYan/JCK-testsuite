/*
 * @test
 * @summary javax.management.ObjectName conformance: parsing, key properties, patterns, compareTo.
 * @run main ObjectNameTest
 */

import javax.management.*;
public class ObjectNameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        ObjectName n = new ObjectName("d:type=x,name=y");
        check("d".equals(n.getDomain()), "domain");
        check("x".equals(n.getKeyProperty("type")), "key property");
        check(n.getKeyPropertyList().size() == 2, "key property list size");
        check(!n.isPattern(), "not a pattern");
        ObjectName p = new ObjectName("d:type=*");
        check(p.isPattern() && p.isPropertyPattern(), "property pattern");
        check(n.equals(new ObjectName("d:type=x,name=y")), "equals");
        check(n.compareTo(new ObjectName("d:type=x,name=y")) == 0, "compareTo");
        check(new ObjectName(n.getCanonicalName()).equals(n), "canonical name");
        check("x".equals(ObjectName.unquote(ObjectName.quote("x"))), "quote/unquote");
        check(n.apply(n), "apply self");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

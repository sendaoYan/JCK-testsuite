/*
 * @test
 * @summary javax.management.AttributeList conformance: add/set/asList.
 * @run main AttributeListTest
 */

import javax.management.*;
public class AttributeListTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        AttributeList l = new AttributeList();
        Attribute a = new Attribute("Count", Integer.valueOf(1));
        l.add(a);
        check(l.size() == 1, "size after add");
        check(l.asList().get(0) == a, "asList element");
        Attribute b = new Attribute("Count", Integer.valueOf(2));
        l.set(0, b);
        check(l.asList().get(0) == b, "set element");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary jdk.dynalink.beans.StaticClass conformance: static class wrapper.
 * @run main StaticClassTest
 */

import jdk.dynalink.beans.*;

public class StaticClassTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        StaticClass sc = StaticClass.forClass(String.class);
        check(sc.getRepresentedClass() == String.class, "represented class");
        check(sc.toString().contains("String"), "toString");
        check(sc.equals(StaticClass.forClass(String.class)), "equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

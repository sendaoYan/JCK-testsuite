/*
 * @test
 * @summary javax.print.attribute.HashAttributeSet conformance: attribute set implementation.
 * @run main HashAttributeSetTest
 */

import java.lang.reflect.*;


public class HashAttributeSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        javax.print.attribute.HashAttributeSet set = new javax.print.attribute.HashAttributeSet();
        check(set.isEmpty() && set.size() == 0, "initially empty");
        javax.print.attribute.standard.Copies cp = new javax.print.attribute.standard.Copies(3);
        check(set.add(cp), "add");
        check(set.containsKey(javax.print.attribute.standard.Copies.class), "containsKey");
        check(set.containsValue(cp), "containsValue");
        check(set.size() == 1, "size");
        check(set.get(javax.print.attribute.standard.Copies.class) == cp, "get");
        check(set.toArray().length == 1, "toArray");
        javax.print.attribute.HashAttributeSet other = new javax.print.attribute.HashAttributeSet(cp);
        check(set.equals(other) && set.hashCode() == other.hashCode(), "equals/hashCode");
        javax.print.attribute.standard.JobName jn = new javax.print.attribute.standard.JobName("x", java.util.Locale.US);
        javax.print.attribute.HashAttributeSet more = new javax.print.attribute.HashAttributeSet(jn);
        check(set.addAll(more), "addAll");
        check(set.size() == 2, "size after addAll");
        check(set.remove(javax.print.attribute.standard.Copies.class), "remove(Class)");
        check(set.remove(jn), "remove(Attribute)");
        check(set.isEmpty(), "empty after remove");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

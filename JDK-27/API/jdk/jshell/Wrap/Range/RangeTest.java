/*
 * @test
 * @summary jdk.jshell.Wrap.Range conformance: wrap range reflection.
 * @run main/othervm --add-opens=jdk.jshell/jdk.jshell=ALL-UNNAMED RangeTest
 */

import java.lang.reflect.*;

public class RangeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    // ---- deep contract: Range value shape ----
    static void testShape() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.Wrap$Range");
        check(c.getDeclaredFields().length >= 2, "begin/end fields");
        java.util.Set<String> names = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) names.add(f.getName());
        check(names.contains("begin") && names.contains("end"), "begin/end named fields");
        check(c.getDeclaredFields()[0].getType() == int.class, "int coordinates");
        // two package-private ctors: (int,int) and (String)
        java.lang.reflect.Constructor<?> ctor = c.getDeclaredConstructor(int.class, int.class);
        ctor.setAccessible(true);
        Object r1 = ctor.newInstance(1, 5);
        Object r2 = ctor.newInstance(1, 5);
        check(!r1.equals(r2), "identity equality (no equals override)");
        check(r1.toString().equals("Range[1,5)"), "toString format Range[begin,end)");
        check(r1.equals(r1), "equals self");
        check(!ctor.newInstance(2, 5).toString().equals(r1.toString()), "different begin different toString");
        // length()/isEmpty() helpers
        java.lang.reflect.Method len = c.getDeclaredMethod("length"); len.setAccessible(true);
        java.lang.reflect.Method emp = c.getDeclaredMethod("isEmpty"); emp.setAccessible(true);
        check((int) len.invoke(r1) == 4, "length is end-begin");
        check((boolean) emp.invoke(ctor.newInstance(3, 3)), "empty when begin==end");
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.Wrap$Range");
        check(!c.isInterface() && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getMethod("toString") != null, "toString method");
        testShape();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

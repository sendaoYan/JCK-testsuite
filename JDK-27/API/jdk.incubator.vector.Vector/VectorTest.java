/*
 * @test
 * @summary jdk.incubator.vector.Vector conformance: white-box structure + black-box behavior.
 * @modules jdk.incubator.vector
 * @run main VectorTest
 */

import jdk.incubator.vector.*;
import java.lang.reflect.*;

public class VectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.incubator.vector.Vector");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(hasMethod(c, "species"), "species");
        check(hasMethod(c, "elementType"), "elementType");
        check(hasMethod(c, "length"), "length");
        check(hasMethod(c, "shape"), "shape");
        check(hasMethod(c, "broadcast"), "broadcast");
        check(hasMethod(c, "add"), "add");
        check(hasMethod(c, "reduceLanesToLong"), "reduceLanesToLong");
    }

    static void testBlackBox() {
        Vector<Integer> v = IntVector.broadcast(IntVector.SPECIES_256, 7);
        int n = v.length();
        check(n > 0, "length positive");
        check(v.elementType() == int.class, "elementType");
        check(v.elementSize() == 32, "elementSize");
        check(v.shape() != null, "shape non-null");
        check(v.species() == IntVector.SPECIES_256, "species");
        Vector<Integer> w = v.broadcast(1);
        check(w.length() == n, "broadcast length");
        Vector<Integer> sum = v.add(w);
        check(((IntVector) sum).lane(0) == 8, "add lane 0");
        check(((IntVector) sum).lane(n - 1) == 8, "add lane last");
        long red = v.reduceLanesToLong(VectorOperators.ADD);
        check(red == 7L * n, "reduceLanesToLong ADD");
        check(v.eq(v.broadcast(7)).allTrue(), "eq self allTrue");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

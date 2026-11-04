/*
 * @test
 * @summary jdk.incubator.vector.ByteVector conformance: white-box structure + black-box behavior.
 * @modules jdk.incubator.vector
 * @run main ByteVectorTest
 */

import jdk.incubator.vector.*;
import java.lang.reflect.*;

public class ByteVectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.incubator.vector.ByteVector");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Vector.class.isAssignableFrom(c), "extends Vector");
        check(ByteVector.SPECIES_64 != null && ByteVector.SPECIES_128 != null, "species constants");
        check(ByteVector.SPECIES_64.elementType() == byte.class, "element type byte");
        check(hasMethod(c, "broadcast"), "broadcast");
        check(hasMethod(c, "add"), "add");
        check(hasMethod(c, "fromArray"), "fromArray");
    }

    static void testBlackBox() {
        VectorSpecies<Byte> sp = ByteVector.SPECIES_256;
        int n = sp.length();
        check(n > 0, "species length positive");
        ByteVector a = ByteVector.broadcast(sp, (byte) 5);
        ByteVector b = ByteVector.broadcast(sp, (byte) 3);
        check(a.lane(0) == (byte) 5, "broadcast lane");
        ByteVector sum = a.add(b);
        check(sum.lane(0) == (byte) 8 && sum.lane(n - 1) == (byte) 8, "add lanes");
        byte[] arr = new byte[n];
        sum.intoArray(arr, 0);
        check(arr[0] == (byte) 8 && arr[n - 1] == (byte) 8, "intoArray round-trip");
        ByteVector back = ByteVector.fromArray(sp, arr, 0);
        check(back.equals(sum), "fromArray round-trip equals");
        int reduced = a.reduceLanes(VectorOperators.ADD);
        check(reduced == (byte) (5 * n), "reduceLanes ADD");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

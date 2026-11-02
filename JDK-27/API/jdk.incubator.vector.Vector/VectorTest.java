/*
 * @test
 * @summary jdk.incubator.vector.Vector conformance: vector abstract operations.
 * @modules jdk.incubator.vector
 * @run main VectorTest
 */

import jdk.incubator.vector.*;

public class VectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Vector<Integer> v = IntVector.broadcast(IntVector.SPECIES_PREFERRED, 7);
        check(v.length() > 0, "length");
        check(v.elementType() == int.class, "elementType");
        check(v.elementSize() == 32, "elementSize");
        check(v.shape() != null, "shape");
        check(v.species() != null, "species");
        check(v.broadcast(1).length() > 0, "broadcast");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

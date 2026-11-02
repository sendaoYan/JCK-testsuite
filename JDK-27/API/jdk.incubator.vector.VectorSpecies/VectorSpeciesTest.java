/*
 * @test
 * @summary jdk.incubator.vector.VectorSpecies conformance: species properties.
 * @modules jdk.incubator.vector
 * @run main VectorSpeciesTest
 */

import jdk.incubator.vector.*;

public class VectorSpeciesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        VectorSpecies<Integer> sp = IntVector.SPECIES_PREFERRED;
        check(sp.elementType() == int.class, "elementType");
        check(sp.elementSize() == 32, "elementSize");
        check(sp.length() > 0, "length");
        check(sp.vectorBitSize() > 0, "vectorBitSize");
        check(sp.vectorShape() != null, "vectorShape");
        check(sp.withShape(sp.vectorShape()) != null, "withShape");
        check(sp.maskAll(true).allTrue(), "maskAll");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary jdk.incubator.vector.VectorSpecies conformance: white-box structure + black-box behavior.
 * @modules jdk.incubator.vector
 * @run main VectorSpeciesTest
 */

import jdk.incubator.vector.*;
import java.lang.reflect.*;

public class VectorSpeciesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.incubator.vector.VectorSpecies");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "elementType"), "elementType");
        check(hasMethod(c, "elementSize"), "elementSize");
        check(hasMethod(c, "length"), "length");
        check(hasMethod(c, "vectorBitSize"), "vectorBitSize");
        check(hasMethod(c, "vectorShape"), "vectorShape");
        check(hasMethod(c, "withShape"), "withShape");
        check(hasMethod(c, "maskAll"), "maskAll");
    }

    static void testBlackBox() {
        VectorSpecies<Integer> sp = IntVector.SPECIES_256;
        check(sp.elementType() == int.class, "elementType int");
        check(sp.elementSize() == 32, "elementSize 32");
        check(sp.length() == sp.vectorBitSize() / 32, "length == bitSize/elementSize");
        check(sp.vectorBitSize() == 256, "vectorBitSize 256");
        check(sp.vectorShape() == VectorShape.S_256_BIT, "vectorShape S_256_BIT");
        VectorSpecies<Integer> widened = sp.withShape(VectorShape.S_512_BIT);
        check(widened.length() > sp.length(), "withShape widens length");
        check(sp.withShape(VectorShape.S_128_BIT).length() < sp.length(), "withShape shrinks length");
        check(sp.maskAll(true).allTrue(), "maskAll allTrue");
        check(((IntVector) sp.broadcast(3)).lane(0) == 3, "broadcast lane");
        check(sp.fromArray(new int[sp.length()], 0).length() == sp.length(), "fromArray length");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

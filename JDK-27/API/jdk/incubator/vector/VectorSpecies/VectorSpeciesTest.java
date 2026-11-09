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


    static void testDeepen() {
        check(VectorSpecies.of(int.class, VectorShape.S_256_BIT) == IntVector.SPECIES_256, "of(int,256)==SPECIES_256");
        check(VectorSpecies.of(double.class, VectorShape.S_128_BIT) == DoubleVector.SPECIES_128, "of(double,128)");
        check(VectorSpecies.of(byte.class, VectorShape.S_64_BIT) == ByteVector.SPECIES_64, "of(byte,64)");
        check(VectorSpecies.ofPreferred(int.class) == IntVector.SPECIES_PREFERRED, "ofPreferred");
        // ofLargestShape tracks the platform's max vector width (512 on AVX-512
        // x64, 128 on aarch64), so compare against S_Max_BIT rather than 512.
        check(VectorSpecies.ofLargestShape(int.class).vectorShape().vectorBitSize()
                == VectorShape.S_Max_BIT.vectorBitSize(), "ofLargestShape matches S_Max_BIT bit size");
        check(IntVector.SPECIES_256.withLanes(int.class) == IntVector.SPECIES_256, "withLanes same type identity");
        check(IntVector.SPECIES_256.withLanes(byte.class).length() == 32, "withLanes byte widens lanes");
        check(IntVector.SPECIES_256.loopBound(10) == 8, "loopBound(10)==8");
        check(IntVector.SPECIES_64.loopBound(8) == 8, "loopBound exact multiple");
        check(IntVector.SPECIES_256.partLimit(VectorSpecies.of(long.class, VectorShape.S_256_BIT), false) == 0, "partLimit int->long 256");
        check(IntVector.zero(IntVector.SPECIES_256).lane(0) == 0, "zero vector");
        check(IntVector.SPECIES_128.toString().equals("Species[int, 4, S_128_BIT]"), "toString exact");
        check(IntVector.SPECIES_128.equals(VectorSpecies.of(int.class, VectorShape.S_128_BIT)), "equals");
        check(IntVector.SPECIES_128.hashCode() == VectorSpecies.of(int.class, VectorShape.S_128_BIT).hashCode(), "hashCode consistent");
        boolean uoe = false;
        try { VectorSpecies.of(boolean.class, VectorShape.S_64_BIT); } catch (UnsupportedOperationException e) {
            uoe = e.getMessage().equals("Bad vector element type: boolean (should be a primitive type such as byte.class with a known bit-size)");
        }
        check(uoe, "of(boolean) UOE exact message");
        boolean uoe2 = false;
        try { VectorSpecies.of(String.class, VectorShape.S_64_BIT); } catch (UnsupportedOperationException e) {
            uoe2 = e.getMessage().contains("java.lang.String");
        }
        check(uoe2, "of(String) UOE names the type");
        VectorMask<Integer> m = IntVector.SPECIES_128.maskAll(true).indexInRange(1, 3);
        check(m.toString().equals("Mask[TT..]"), "maskAll().indexInRange");
        check(IntVector.SPECIES_128.vectorBitSize() / IntVector.SPECIES_128.elementSize()
                == IntVector.SPECIES_128.length(), "bitSize/elementSize == length");
    }

    public static void main(String[] args) throws Exception {
        testDeepen();

        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

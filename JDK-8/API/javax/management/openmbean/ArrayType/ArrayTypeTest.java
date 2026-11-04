/*
 * @test
 * @summary javax.management.openmbean.ArrayType conformance: dimension/element/primitive.
 * @run main ArrayTypeTest
 */

import javax.management.openmbean.*;
public class ArrayTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        ArrayType<?> at = new ArrayType(1, SimpleType.STRING);
        check(at.getDimension() == 1, "dimension");
        check(at.getElementOpenType() == SimpleType.STRING, "element type");
        check(!at.isPrimitiveArray(), "not primitive");
        ArrayType<?> prim = ArrayType.getPrimitiveArrayType(int[].class);
        check(prim.isPrimitiveArray(), "primitive array");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

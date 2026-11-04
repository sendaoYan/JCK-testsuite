/*
 * @test
 * @summary java.lang.invoke.TypeDescriptor.OfField conformance: white-box + black-box behavior, nested array chains, primitive matrix, ClassDesc parity and a user implementation.
 * @run main OfFieldTest
 */

import java.lang.invoke.TypeDescriptor;
import java.lang.invoke.TypeDescriptor.OfField;

public class OfFieldTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.TypeDescriptor$OfField");
        check(c.isInterface(), "is an interface");
        check(c.getInterfaces().length == 1
              && c.getInterfaces()[0] == TypeDescriptor.class, "extends TypeDescriptor");
        for (String m : new String[]{"isArray", "isPrimitive", "componentType", "arrayType"}) {
            boolean found = false;
            for (java.lang.reflect.Method mm : c.getDeclaredMethods())
                if (mm.getName().equals(m)) found = true;
            check(found, "declares " + m);
        }
        check(Class.class.isAssignableFrom(Class.class)
              && OfField.class.isAssignableFrom(Class.class), "Class implements OfField");
        check(OfField.class.isAssignableFrom(
                      Class.forName("java.lang.constant.ClassDesc")),
              "ClassDesc implements OfField");
        check(OfField.class.isAssignableFrom(
                      Class.forName("java.lang.invoke.MethodType")) == false,
              "MethodType does not implement OfField");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Class as OfField: reference types
        @SuppressWarnings("unchecked")
        OfField<Class<?>> str = (OfField<Class<?>>) (Object) String.class;
        check(!str.isArray() && !str.isPrimitive(), "String is plain");
        check(str.componentType() == null, "componentType null for plain class");
        @SuppressWarnings("unchecked")
        OfField<Class<?>> strArr = (OfField<Class<?>>) (Object) String[].class;
        check(strArr.isArray() && !strArr.isPrimitive(), "String[] is array");
        check(strArr.componentType() == String.class, "componentType String");

        // arrayType() lifts one dimension
        check(str.arrayType() == String[].class, "arrayType String[]");
        check(strArr.arrayType() == String[][].class, "arrayType String[][]");

        // primitives
        @SuppressWarnings("unchecked")
        OfField<Class<?>> i = (OfField<Class<?>>) (Object) int.class;
        check(i.isPrimitive() && !i.isArray(), "int is primitive");
        check(i.componentType() == null, "primitive has no component");
        check(i.arrayType() == int[].class, "int arrayType");
        @SuppressWarnings("unchecked")
        OfField<Class<?>> v = (OfField<Class<?>>) (Object) void.class;
        check(v.isPrimitive(), "void counts as primitive");

        // descriptors chain through arrayType
        check(i.arrayType().descriptorString().equals("[I"), "array descriptor");
        check(strArr.descriptorString().equals("[Ljava/lang/String;"), "array ref descriptor");

        // ClassDesc as OfField behaves identically
        java.lang.constant.ClassDesc cd = java.lang.constant.ClassDesc.of("java.lang.String");
        @SuppressWarnings("unchecked")
        OfField<?> cdOf = (OfField<?>) cd;
        check(!cdOf.isArray() && !cdOf.isPrimitive(), "ClassDesc plain");
        OfField<?> cdArr = cdOf.arrayType();
        check(cdArr.isArray() && cdArr.descriptorString().equals("[Ljava/lang/String;"),
              "ClassDesc arrayType descriptor");
    }

    // ---- nested array chains: componentType/arrayType are mutual inverses ----
    static void testArrayChains() {
        @SuppressWarnings("unchecked")
        OfField<Class<?>> i2 = (OfField<Class<?>>) (Object) int[][].class;
        check(i2.isArray(), "int[][] is an array");
        check(i2.componentType() == int[].class, "int[][] componentType is int[]");
        check(i2.arrayType() == int[][][].class, "int[][] arrayType is int[][][]");
        check(i2.descriptorString().equals("[[I"), "int[][] descriptor");
        @SuppressWarnings("unchecked")
        OfField<Class<?>> i3 = (OfField<Class<?>>) (Object) int[][][].class;
        check(i3.componentType() == int[][].class, "int[][][] componentType is int[][]");
        @SuppressWarnings("unchecked")
        OfField<Class<?>> wrapper = (OfField<Class<?>>) (Object) Integer.class;
        check(!wrapper.isPrimitive(), "wrapper class is not primitive");
        check(wrapper.arrayType() == Integer[].class, "Integer arrayType is Integer[]");
        check(wrapper.arrayType().descriptorString().equals("[Ljava/lang/Integer;"),
                "Integer[] descriptor");
        @SuppressWarnings("unchecked")
        OfField<Class<?>> nested = (OfField<Class<?>>) (Object) String[][][].class;
        check(nested.componentType().componentType().componentType() == String.class,
                "three componentType steps reach String");
        check(nested.arrayType().descriptorString().equals("[[[[Ljava/lang/String;"),
                "String[][][] arrayType descriptor");
    }

    // ---- primitive matrix through the OfField view ----
    static void testPrimitiveMatrix() {
        Class<?>[] prims = {byte.class, short.class, char.class, boolean.class,
                int.class, long.class, float.class, double.class, void.class};
        String[] primDesc = {"B", "S", "C", "Z", "I", "J", "F", "D", "V"};
        Class<?>[] arrOf = {byte[].class, short[].class, char[].class, boolean[].class,
                int[].class, long[].class, float[].class, double[].class};
        for (int i = 0; i < prims.length; i++) {
            @SuppressWarnings("unchecked")
            OfField<Class<?>> p = (OfField<Class<?>>) (Object) prims[i];
            check(p.isPrimitive(), prims[i] + " isPrimitive");
            check(!p.isArray(), prims[i] + " not an array");
            check(p.componentType() == null, prims[i] + " componentType null");
            check(p.descriptorString().equals(primDesc[i]), prims[i] + " descriptor");
            if (i < arrOf.length) {
                check(p.arrayType() == arrOf[i], prims[i] + " arrayType is " + arrOf[i]);
            }
        }
    }

    // ---- ClassDesc parity: componentType descends one dimension ----
    static void testClassDescParity() {
        java.lang.constant.ClassDesc cd = java.lang.constant.ClassDesc.of("java.lang.String");
        @SuppressWarnings("unchecked")
        OfField<?> cdOf = (OfField<?>) cd;
        OfField<?> cdArr = cdOf.arrayType();
        check(cdArr.componentType().descriptorString().equals("Ljava/lang/String;"),
                "ClassDesc componentType descends one dimension");
        @SuppressWarnings("unchecked")
        OfField<?> cdPrim = (OfField<?>) (Object) java.lang.constant.ClassDesc.ofDescriptor("I");
        check(cdPrim.isPrimitive(), "ClassDesc of I is primitive");
        check(cdPrim.componentType() == null, "primitive ClassDesc has no component");
        @SuppressWarnings("unchecked")
        OfField<?> cdVoid = (OfField<?>) (Object) java.lang.constant.ClassDesc.ofDescriptor("V");
        check(cdVoid.descriptorString().equals("V"), "ClassDesc V descriptor");
    }

    // ---- a user implementation honoring the full contract ----
    static final class Box implements OfField<Box> {
        private final boolean arr;
        private final boolean prim;
        private final Box comp;
        private final String desc;
        Box(boolean arr, boolean prim, Box comp, String desc) {
            this.arr = arr; this.prim = prim; this.comp = comp; this.desc = desc;
        }
        @Override public String descriptorString() { return desc; }
        @Override public boolean isArray() { return arr; }
        @Override public boolean isPrimitive() { return prim; }
        @Override public Box componentType() { return arr ? comp : null; }
        @Override public Box arrayType() { return new Box(true, false, this, "[" + desc); }
    }

    static void testUserImplementation() {
        Box i = new Box(false, true, null, "I");
        check(i.descriptorString().equals("I"), "user impl descriptor");
        check(i.isPrimitive() && !i.isArray(), "user impl flags");
        Box ia = i.arrayType();
        check(ia.isArray() && !ia.isPrimitive(), "user impl arrayType flags");
        check(ia.descriptorString().equals("[I"), "user impl array descriptor");
        check(ia.componentType() == i, "user impl componentType inverse");
        check(ia.componentType().descriptorString().equals("I"), "user impl inverse descriptor");
        Box iaa = ia.arrayType();
        check(iaa.descriptorString().equals("[[I"), "user impl two dimensions");
        check(iaa.componentType() == ia, "user impl two-step inverse");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testArrayChains();
        testPrimitiveMatrix();
        testClassDescParity();
        testUserImplementation();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

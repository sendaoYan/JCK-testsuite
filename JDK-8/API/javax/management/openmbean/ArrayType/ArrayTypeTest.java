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
        testDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened: full construction family / primitive table / isValue matrix / ser / negatives ----
    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but nothing was thrown");
    }

    static void checkThrowsExact(Class<? extends Throwable> expected, String message, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                if (!message.equals(t.getMessage()))
                    throw new AssertionError(msg + ": message <" + t.getMessage() + "> != <" + message + ">");
                passed++;
                return;
            }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but nothing was thrown");
    }

    @SuppressWarnings("unchecked")
    static <T> T roundtrip(T o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        T copy = (T) ois.readObject();
        ois.close();
        return copy;
    }

    static void testDeep() throws Exception {
        // white-box: public, extends OpenType, static factories
        Class<?> c = ArrayType.class;
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "white-box public");
        check(OpenType.class.isAssignableFrom(c), "white-box extends OpenType");
        check(c.getMethod("getArrayType", OpenType.class) != null, "static getArrayType(OpenType)");
        check(c.getMethod("getPrimitiveArrayType", Class.class) != null, "static getPrimitiveArrayType(Class)");

        // unidimensional wrapper array via static factory
        ArrayType<?> at = ArrayType.getArrayType(SimpleType.STRING);
        check(at.getDimension() == 1, "string array dimension 1");
        check(at.getElementOpenType() == SimpleType.STRING, "string element type");
        check(!at.isPrimitiveArray(), "string array not primitive");
        check(at.isArray(), "isArray true");
        check(at.getClassName().equals("[Ljava.lang.String;"), "string array className");
        check(at.getTypeName().equals("[Ljava.lang.String;"), "string array typeName==className");
        check(at.getDescription().equals("1-dimension array of java.lang.String"), "string array description");

        // all 8 primitive array types: exact className / element SimpleType / dimension / flags
        Class<?>[] prims = {int[].class, long[].class, boolean[].class, double[].class,
                byte[].class, short[].class, char[].class, float[].class};
        String[] primNames = {"[I", "[J", "[Z", "[D", "[B", "[S", "[C", "[F"};
        String[] primDescs = {"int", "long", "boolean", "double", "byte", "short", "char", "float"};
        SimpleType<?>[] primElems = {SimpleType.INTEGER, SimpleType.LONG, SimpleType.BOOLEAN, SimpleType.DOUBLE,
                SimpleType.BYTE, SimpleType.SHORT, SimpleType.CHARACTER, SimpleType.FLOAT};
        for (int i = 0; i < prims.length; i++) {
            ArrayType<?> t = ArrayType.getPrimitiveArrayType(prims[i]);
            check(t.getClassName().equals(primNames[i]), "prim className " + primNames[i]);
            check(t.getTypeName().equals(primNames[i]), "prim typeName " + primNames[i]);
            check(t.getElementOpenType() == primElems[i], "prim element " + primNames[i]);
            check(t.getDimension() == 1 && t.isPrimitiveArray(), "prim dim/flag " + primNames[i]);
            check(t.getDescription().equals("1-dimension array of " + primDescs[i]), "prim desc " + primNames[i]);
            check(t.equals(ArrayType.getPrimitiveArrayType(prims[i])), "prim equals " + primNames[i]);
            check(t.hashCode() == ArrayType.getPrimitiveArrayType(prims[i]).hashCode(), "prim hashCode " + primNames[i]);
        }
        // exact toString for both forms
        check(at.toString().equals("javax.management.openmbean.ArrayType(name=[Ljava.lang.String;,"
                + "dimension=1,elementType=javax.management.openmbean.SimpleType(name=java.lang.String),"
                + "primitiveArray=false)"), "wrapper toString exact");
        check(ArrayType.getPrimitiveArrayType(int[].class).toString().equals(
                "javax.management.openmbean.ArrayType(name=[I,dimension=1,"
                + "elementType=javax.management.openmbean.SimpleType(name=java.lang.Integer),"
                + "primitiveArray=true)"), "primitive toString exact");

        // (SimpleType, boolean=false) constructor equals getArrayType path (non-primitive form)
        ArrayType<?> viaFalse = new ArrayType<>(SimpleType.STRING, false);
        check(viaFalse.getDimension() == 1, "(STRING,false) dimension 1");
        check(!viaFalse.isPrimitiveArray(), "(STRING,false) not primitive");
        check(viaFalse.equals(ArrayType.getArrayType(SimpleType.STRING)), "(STRING,false) equals getArrayType");
        check(viaFalse.hashCode() == ArrayType.getArrayType(SimpleType.STRING).hashCode(),
                "(STRING,false) hashCode equal");
        // (SimpleType, true) equals getPrimitiveArrayType path
        ArrayType<?> viaTrue = new ArrayType<>(SimpleType.LONG, true);
        check(viaTrue.isPrimitiveArray() && viaTrue.getClassName().equals("[J"), "(LONG,true) primitive [J");
        check(viaTrue.equals(ArrayType.getPrimitiveArrayType(long[].class)), "(LONG,true) equals factory");

        // multi-dimensional via ctor and nesting
        ArrayType<?> at2 = new ArrayType<>(2, SimpleType.INTEGER);
        check(at2.getDimension() == 2 && at2.getElementOpenType() == SimpleType.INTEGER
                && !at2.isPrimitiveArray(), "2-dim Integer");
        check(at2.getClassName().equals("[[Ljava.lang.Integer;"), "2-dim className");
        check(at2.getDescription().equals("2-dimension array of java.lang.Integer"), "2-dim desc");
        ArrayType<?> nested = ArrayType.getArrayType(at);
        check(nested.getDimension() == 2 && nested.getElementOpenType() == SimpleType.STRING
                && nested.getClassName().equals("[[Ljava.lang.String;"), "nested via getArrayType");
        // nesting primitive arrays adds dimensions and stays primitive
        ArrayType<?> primNested = new ArrayType<>(2, ArrayType.getPrimitiveArrayType(int[].class));
        check(primNested.getDimension() == 3, "(2, prim int[]) total dimension 3");
        check(primNested.isPrimitiveArray(), "(2, prim int[]) stays primitive");
        check(primNested.getClassName().equals("[[[I"), "(2, prim int[]) className [[[I");
        check(primNested.equals(ArrayType.getPrimitiveArrayType(int[][][].class)), "equals int[][][] form");
        check(ArrayType.getArrayType(ArrayType.getPrimitiveArrayType(int[].class))
                .equals(ArrayType.getPrimitiveArrayType(int[][].class)), "getArrayType(prim) equals int[][] form");

        // isValue matrices
        check(at.isValue(new String[]{"a", "b"}), "isValue String[]");
        check(at.isValue(new String[0]), "isValue empty String[]");
        check(at.isValue(new String[]{"a", null}), "isValue String[] with null element");
        check(!at.isValue(new Object[]{"a"}), "rejects Object[] holding String");
        check(!at.isValue(new String[][]{{"a"}}), "rejects wrong dimension");
        check(!at.isValue(new Integer[]{1}), "rejects Integer[]");
        check(!at.isValue(null), "rejects null");
        check(at2.isValue(new Integer[2][3]), "2-dim accepts Integer[][]");
        check(!at2.isValue(new Integer[]{1}), "2-dim rejects 1-dim");
        check(!at2.isValue(new Object[][]{{1}}), "2-dim rejects Object[][]");
        check(!at2.isValue(null), "2-dim rejects null");
        ArrayType<?> pt = ArrayType.getPrimitiveArrayType(int[].class);
        check(pt.isValue(new int[]{1, 2}), "prim isValue int[]");
        check(!pt.isValue(new Integer[]{1, 2}), "prim rejects Integer[]");
        check(!pt.isValue(null), "prim rejects null");
        check(!pt.isValue(new int[0][0]), "prim rejects int[][]");

        // equals / hashCode across construction paths
        check(at.equals(ArrayType.getArrayType(SimpleType.STRING)), "equals independent instances");
        check(at.hashCode() == ArrayType.getArrayType(SimpleType.STRING).hashCode(), "hashCode equal");
        check(!at.equals(at2), "different dimension not equal");
        check(!at.equals(pt), "different element not equal");
        check(!at.equals(null), "equals null false");
        check(!at.equals("x"), "equals non-ArrayType false");
        check(!viaTrue.equals(ArrayType.getArrayType(SimpleType.LONG)), "primitive vs wrapper not equal");

        // serialization round-trips
        check(roundtrip(at).equals(at), "wrapper ser roundtrip");
        check(roundtrip(pt).equals(pt), "primitive ser roundtrip");
        check(roundtrip(at2).equals(at2), "2-dim ser roundtrip");
        check(roundtrip(nested).equals(nested), "nested ser roundtrip");

        // negatives: exact exception types and messages (verified on this JDK)
        checkThrowsExact(IllegalArgumentException.class,
                "Value of argument dimension must be greater than 0",
                () -> new ArrayType<>(0, SimpleType.STRING), "dimension 0");
        checkThrowsExact(IllegalArgumentException.class,
                "Value of argument dimension must be greater than 0",
                () -> new ArrayType<>(-1, SimpleType.STRING), "dimension -1");
        checkThrows(NullPointerException.class,
                () -> new ArrayType<>(1, (OpenType<?>) null), "null element type ctor");
        checkThrows(NullPointerException.class,
                () -> ArrayType.getArrayType(null), "getArrayType(null)");
        checkThrowsExact(OpenDataException.class, "Element type is not primitive: java.lang.String",
                () -> new ArrayType<>(SimpleType.STRING, true), "primitive flag on non-primitive");
        checkThrowsExact(IllegalArgumentException.class, "arrayClass must be an array",
                () -> ArrayType.getPrimitiveArrayType(String.class), "non-array class");
        checkThrowsExact(IllegalArgumentException.class, "arrayClass must be an array",
                () -> ArrayType.getPrimitiveArrayType(int.class), "primitive non-array class");
        checkThrowsExact(IllegalArgumentException.class, "component type of the array must be a primitive type",
                () -> ArrayType.getPrimitiveArrayType(String[].class), "non-primitive component");
    }
}

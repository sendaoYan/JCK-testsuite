/*
 * @test
 * @summary javax.management.openmbean.ArrayType conformance: white-box structure + black-box behavior.
 * @run main ArrayTypeTest
 */

import java.lang.reflect.Modifier;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

public class ArrayTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void checkThrows(java.lang.Class<? extends Throwable> type, String exactMessage, Runnable r, String label) {
        try {
            r.run();
            throw new AssertionError(label + ": expected " + type.getSimpleName());
        } catch (Throwable e) {
            if (e instanceof AssertionError) throw (AssertionError) e;
            check(type.isInstance(e), label + ": type " + e.getClass().getName());
            if (exactMessage != null) check(exactMessage.equals(e.getMessage()),
                label + ": message <" + e.getMessage() + "> != <" + exactMessage + ">");
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.ArrayType");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(OpenType.class.isAssignableFrom(c), "extends OpenType");
        java.lang.reflect.Method m = c.getMethod("getArrayType", OpenType.class);
        check(m != null, "static getArrayType(OpenType)");
        java.lang.reflect.Method p = c.getMethod("getPrimitiveArrayType", java.lang.Class.class);
        check(p != null, "static getPrimitiveArrayType(Class)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // unidimensional non-primitive array of String via static factory
        ArrayType<String[]> at = ArrayType.getArrayType(SimpleType.STRING);
        check(at.getDimension() == 1, "string array dimension");
        check(at.getElementOpenType() == SimpleType.STRING, "string element type");
        check(!at.isPrimitiveArray(), "string array not primitive");
        check(at.isArray(), "isArray true");
        check(at.getClassName().equals("[Ljava.lang.String;"), "string array className");
        check(at.getTypeName().equals("[Ljava.lang.String;"), "string array typeName");
        check(at.getDescription().equals("1-dimension array of java.lang.String"), "string array description");
        check(at.isValue(new String[]{"a", "b"}), "isValue String[]");
        check(at.isValue(new String[0]), "isValue empty String[]");
        check(!at.isValue(null), "reject null");
        check(!at.isValue(new Integer[]{1}), "reject Integer[]");

        // primitive int array via static factory
        ArrayType<int[]> pt = ArrayType.getPrimitiveArrayType(int[].class);
        check(pt.getDimension() == 1, "int array dimension");
        check(pt.getElementOpenType() == SimpleType.INTEGER, "int element type");
        check(pt.isPrimitiveArray(), "int array primitive");
        check(pt.getClassName().equals("[I"), "int array className");
        check(pt.getDescription().equals("1-dimension array of int"), "int array description");
        check(pt.isValue(new int[]{1, 2}), "isValue int[]");
        check(!pt.isValue(new Integer[]{1, 2}), "reject Integer[] for primitive");

        // constructor with dimension and elementType
        ArrayType<?> at2 = new ArrayType<>(2, SimpleType.INTEGER);
        check(at2.getDimension() == 2, "2-dim dimension");
        check(at2.getElementOpenType() == SimpleType.INTEGER, "2-dim element");
        check(!at2.isPrimitiveArray(), "2-dim non-primitive");
        check(at2.getClassName().equals("[[Ljava.lang.Integer;"), "2-dim className");
        check(at2.getDescription().equals("2-dimension array of java.lang.Integer"), "2-dim description");
        check(at2.isValue(new Integer[2][3]), "isValue 2-dim Integer[][]");

        // nested / multidimensional via chained getArrayType
        ArrayType<String[][]> nested = ArrayType.getArrayType(at);
        check(nested.getDimension() == 2, "nested dimension 2");
        check(nested.getElementOpenType() == SimpleType.STRING, "nested element String");
        check(nested.getClassName().equals("[[Ljava.lang.String;"), "nested className");
        check(nested.getDescription().equals("2-dimension array of java.lang.String"), "nested description");
        ArrayType<String[][][]> nested3 = ArrayType.getArrayType(nested);
        check(nested3.getDimension() == 3, "nested dimension 3");
        check(nested3.getElementOpenType() == SimpleType.STRING, "nested3 element");
        check(nested3.getClassName().equals("[[[Ljava.lang.String;"), "nested3 className");
        check(nested3.getDescription().equals("3-dimension array of java.lang.String"), "nested3 description");

        // multi-dimensional primitive
        ArrayType<int[][][]> p3 = ArrayType.getPrimitiveArrayType(int[][][].class);
        check(p3.getDimension() == 3, "primitive 3-dim dimension");
        check(p3.getElementOpenType() == SimpleType.INTEGER, "primitive 3-dim element");
        check(p3.isPrimitiveArray(), "primitive 3-dim primitive flag");
        check(p3.getClassName().equals("[[[I"), "primitive 3-dim className");
        check(p3.getDescription().equals("3-dimension array of int"), "primitive 3-dim description");

        // constructor combining dimension onto an existing ArrayType (dimension addition)
        ArrayType<?> combo = new ArrayType<>(2, at); // 2 + 1 = 3 dims of String
        check(combo.getDimension() == 3, "combo dimension 3");
        check(combo.getElementOpenType() == SimpleType.STRING, "combo element String");

        // primitive constructor
        ArrayType<?> primCtor = new ArrayType<>(SimpleType.LONG, true);
        check(primCtor.isPrimitiveArray(), "primitive ctor primitive flag");
        check(primCtor.getElementOpenType() == SimpleType.LONG, "primitive ctor element LONG");
        check(primCtor.getClassName().equals("[J"), "primitive ctor className");
        check(primCtor.getDescription().equals("1-dimension array of long"), "primitive ctor description");

        // equals / hashCode
        check(at.equals(ArrayType.getArrayType(SimpleType.STRING)), "equals independent instances");
        check(at.hashCode() == ArrayType.getArrayType(SimpleType.STRING).hashCode(), "hashCode equal");
        check(!at.equals(at2), "different dimension not equal");
        check(!at.equals(pt), "different element not equal");
        check(!at.equals(null), "equals null false");
        check(!at.equals("x"), "equals non-ArrayType false");
        check(!primCtor.equals(ArrayType.getArrayType(SimpleType.LONG)), "primitive vs wrapper not equal");

        // toString contains class name + dimension + elementType
        check(at.toString().startsWith("javax.management.openmbean.ArrayType("), "toString prefix");
        check(at.toString().contains("dimension=1"), "toString dimension");
        check(at.toString().contains("elementType=" + SimpleType.STRING), "toString elementType");

        // exceptions (exact messages verified against JDK-27)
        checkThrows(IllegalArgumentException.class, "Value of argument dimension must be greater than 0",
            () -> { try { new ArrayType<>(0, SimpleType.STRING); } catch (Exception e) { throw sneaky(e); } }, "dim 0");
        checkThrows(OpenDataException.class, "Element type is not primitive: java.lang.String",
            () -> { try { new ArrayType<>(SimpleType.STRING, true); } catch (Exception e) { throw sneaky(e); } }, "primitive non-primitive");
        checkThrows(IllegalArgumentException.class, "arrayClass must be an array",
            () -> ArrayType.getPrimitiveArrayType(String.class), "non-array class");
        checkThrows(IllegalArgumentException.class, "component type of the array must be a primitive type",
            () -> ArrayType.getPrimitiveArrayType(String[].class), "non-primitive component");
    }

    // ---- deepened: primitive family table / construction-path equivalence / ser / NPEs ----
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
        // all 8 primitive array types: exact className / element / dim / flags / equals
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
        check(ArrayType.getArrayType(SimpleType.STRING).toString().equals(
                "javax.management.openmbean.ArrayType(name=[Ljava.lang.String;,dimension=1,"
                + "elementType=javax.management.openmbean.SimpleType(name=java.lang.String),"
                + "primitiveArray=false)"), "wrapper toString exact");
        check(ArrayType.getPrimitiveArrayType(int[].class).toString().equals(
                "javax.management.openmbean.ArrayType(name=[I,dimension=1,"
                + "elementType=javax.management.openmbean.SimpleType(name=java.lang.Integer),"
                + "primitiveArray=true)"), "primitive toString exact");

        // (SimpleType, false) equals the getArrayType path; (SimpleType, true) equals primitive factory
        ArrayType<?> viaFalse = new ArrayType<>(SimpleType.STRING, false);
        check(viaFalse.getDimension() == 1 && !viaFalse.isPrimitiveArray(), "(STRING,false) 1-dim wrapper");
        check(viaFalse.equals(ArrayType.getArrayType(SimpleType.STRING)), "(STRING,false) equals getArrayType");
        check(viaFalse.hashCode() == ArrayType.getArrayType(SimpleType.STRING).hashCode(),
                "(STRING,false) hashCode equal");
        check(new ArrayType<>(SimpleType.LONG, true).equals(ArrayType.getPrimitiveArrayType(long[].class)),
                "(LONG,true) equals primitive factory");
        // nesting primitive arrays adds dimensions and stays primitive
        ArrayType<?> primNested = new ArrayType<>(2, ArrayType.getPrimitiveArrayType(int[].class));
        check(primNested.getDimension() == 3, "(2, prim int[]) total dimension 3");
        check(primNested.isPrimitiveArray(), "(2, prim int[]) stays primitive");
        check(primNested.getClassName().equals("[[[I"), "(2, prim int[]) className [[[I");
        check(primNested.equals(ArrayType.getPrimitiveArrayType(int[][][].class)), "equals int[][][] form");
        check(ArrayType.getArrayType(ArrayType.getPrimitiveArrayType(int[].class))
                .equals(ArrayType.getPrimitiveArrayType(int[][].class)), "getArrayType(prim) equals int[][] form");

        // isValue extras: null element accepted in wrapper arrays; Object[] rejected; wrong dim rejected
        ArrayType<?> at = ArrayType.getArrayType(SimpleType.STRING);
        check(at.isValue(new String[]{"a", null}), "String[] with null element valid");
        check(!at.isValue(new Object[]{"a"}), "Object[] holding String rejected");
        check(!at.isValue(new String[][]{{"a"}}), "wrong dimension rejected");
        check(!ArrayType.getPrimitiveArrayType(int[].class).isValue(new int[0][0]), "prim rejects int[][]");

        // serialization round-trips
        check(roundtrip(at).equals(at), "wrapper ser roundtrip");
        check(roundtrip(ArrayType.getPrimitiveArrayType(int[].class))
                .equals(ArrayType.getPrimitiveArrayType(int[].class)), "primitive ser roundtrip");

        // additional negatives: type-only NPEs (message differs across JDKs) and exact IAEs
        checkThrows(NullPointerException.class, null,
                () -> { try { new ArrayType<>(1, (OpenType<?>) null); } catch (Exception e) { throw sneaky(e); } },
                "null element type ctor");
        checkThrows(NullPointerException.class, null,
                () -> { try { ArrayType.getArrayType(null); } catch (Exception e) { throw sneaky(e); } },
                "getArrayType(null)");
        checkThrows(IllegalArgumentException.class, "Value of argument dimension must be greater than 0",
                () -> { try { new ArrayType<>(-1, SimpleType.STRING); } catch (Exception e) { throw sneaky(e); } },
                "dimension -1");
        checkThrows(IllegalArgumentException.class, "arrayClass must be an array",
                () -> { try { ArrayType.getPrimitiveArrayType(int.class); } catch (Exception e) { throw sneaky(e); } },
                "primitive non-array class");
    }

    @SuppressWarnings("unchecked")
    static <E extends Throwable> RuntimeException sneaky(Throwable t) throws E { throw (E) t; }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

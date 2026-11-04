/*
 * @test
 * @summary javax.management.openmbean.SimpleType conformance: white-box structure + black-box behavior.
 * @run main SimpleTypeTest
 */

import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import javax.management.ObjectName;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

public class SimpleTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.SimpleType");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(OpenType.class.isAssignableFrom(c), "extends OpenType");
        java.lang.reflect.Field[] fields = c.getDeclaredFields();
        int constants = 0;
        for (java.lang.reflect.Field f : fields) {
            if (Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())
                    && f.getType() == SimpleType.class) constants++;
        }
        check(constants == 14, "14 public SimpleType constants");
        java.lang.reflect.Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1 && Modifier.isPrivate(ctors[0].getModifiers()), "single private ctor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // className == typeName == description for every predefined constant
        SimpleType<?>[] all = {
            SimpleType.VOID, SimpleType.BOOLEAN, SimpleType.CHARACTER, SimpleType.BYTE,
            SimpleType.SHORT, SimpleType.INTEGER, SimpleType.LONG, SimpleType.FLOAT,
            SimpleType.DOUBLE, SimpleType.STRING, SimpleType.BIGDECIMAL, SimpleType.BIGINTEGER,
            SimpleType.DATE, SimpleType.OBJECTNAME
        };
        String[] classNames = {
            "java.lang.Void", "java.lang.Boolean", "java.lang.Character", "java.lang.Byte",
            "java.lang.Short", "java.lang.Integer", "java.lang.Long", "java.lang.Float",
            "java.lang.Double", "java.lang.String", "java.math.BigDecimal", "java.math.BigInteger",
            "java.util.Date", "javax.management.ObjectName"
        };
        for (int i = 0; i < all.length; i++) {
            SimpleType<?> t = all[i];
            check(classNames[i].equals(t.getClassName()), "className " + classNames[i]);
            check(t.getClassName().equals(t.getTypeName()), "typeName==className " + classNames[i]);
            check(t.getClassName().equals(t.getDescription()), "description==className " + classNames[i]);
            check(!t.isArray(), "not array " + classNames[i]);
        }

        // isValue is exact-class match, and null is never a value
        check(SimpleType.INTEGER.isValue(5), "INTEGER isValue int");
        check(SimpleType.INTEGER.isValue(Integer.valueOf(5)), "INTEGER isValue Integer");
        check(!SimpleType.INTEGER.isValue(5L), "INTEGER rejects Long");
        check(!SimpleType.INTEGER.isValue("5"), "INTEGER rejects String");
        check(!SimpleType.INTEGER.isValue(null), "INTEGER rejects null");
        check(SimpleType.STRING.isValue("hello"), "STRING isValue");
        check(SimpleType.BOOLEAN.isValue(Boolean.TRUE), "BOOLEAN isValue");
        check(SimpleType.CHARACTER.isValue('x'), "CHARACTER isValue");
        check(SimpleType.BYTE.isValue((byte) 1), "BYTE isValue");
        check(SimpleType.SHORT.isValue((short) 2), "SHORT isValue");
        check(SimpleType.LONG.isValue(3L), "LONG isValue");
        check(SimpleType.FLOAT.isValue(1.5f), "FLOAT isValue");
        check(SimpleType.DOUBLE.isValue(2.5d), "DOUBLE isValue");
        check(SimpleType.BIGDECIMAL.isValue(new BigDecimal("3.14")), "BIGDECIMAL isValue");
        check(SimpleType.BIGINTEGER.isValue(new BigInteger("42")), "BIGINTEGER isValue");
        check(SimpleType.DATE.isValue(new Date(0L)), "DATE isValue");
        check(SimpleType.OBJECTNAME.isValue(new ObjectName("d:k=v")), "OBJECTNAME isValue");
        check(!SimpleType.VOID.isValue(null), "VOID rejects null");

        // equals: two SimpleType instances are equal iff same className
        check(SimpleType.INTEGER.equals(SimpleType.INTEGER), "INTEGER equals self");
        check(!SimpleType.INTEGER.equals(SimpleType.STRING), "INTEGER != STRING");
        check(!SimpleType.INTEGER.equals(null), "equals null false");
        check(!SimpleType.INTEGER.equals("java.lang.Integer"), "equals non-SimpleType false");
        check(SimpleType.INTEGER.hashCode() == SimpleType.INTEGER.hashCode(), "hashCode stable");
        check(SimpleType.INTEGER.hashCode() == "java.lang.Integer".hashCode(), "hashCode == className.hashCode");
        check(SimpleType.INTEGER.hashCode() != SimpleType.STRING.hashCode(), "distinct hashCodes");

        // toString format: javax.management.openmbean.SimpleType(name=<className>)
        check(SimpleType.INTEGER.toString().equals(
                "javax.management.openmbean.SimpleType(name=java.lang.Integer)"), "INTEGER toString");
        check(SimpleType.STRING.toString().equals(
                "javax.management.openmbean.SimpleType(name=java.lang.String)"), "STRING toString");
        check(SimpleType.INTEGER.toString().equals(SimpleType.INTEGER.toString()), "toString stable");

        // constants are canonical singletons under deserialization semantics: equals + hashCode pair
        check(SimpleType.INTEGER.equals(SimpleType.INTEGER) && SimpleType.INTEGER == SimpleType.INTEGER, "singleton identity");
    }

    // ---- deepened: singleton serialization / full toString & hashCode table ----
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
        SimpleType<?>[] all = {
            SimpleType.VOID, SimpleType.BOOLEAN, SimpleType.CHARACTER, SimpleType.BYTE,
            SimpleType.SHORT, SimpleType.INTEGER, SimpleType.LONG, SimpleType.FLOAT,
            SimpleType.DOUBLE, SimpleType.STRING, SimpleType.BIGDECIMAL, SimpleType.BIGINTEGER,
            SimpleType.DATE, SimpleType.OBJECTNAME
        };
        String[] cn = {
            "java.lang.Void", "java.lang.Boolean", "java.lang.Character", "java.lang.Byte",
            "java.lang.Short", "java.lang.Integer", "java.lang.Long", "java.lang.Float",
            "java.lang.Double", "java.lang.String", "java.math.BigDecimal", "java.math.BigInteger",
            "java.util.Date", "javax.management.ObjectName"
        };
        // serialization round-trip returns the SAME instance (public readResolve singleton)
        for (int i = 0; i < all.length; i++)
            check(roundtrip(all[i]) == all[i], "readResolve singleton " + cn[i]);
        check(roundtrip(SimpleType.INTEGER).hashCode() == SimpleType.INTEGER.hashCode(), "roundtrip hashCode");
        // public readResolve declared
        boolean readResolvePublic = false;
        for (java.lang.reflect.Method m : SimpleType.class.getMethods())
            if ("readResolve".equals(m.getName()) && Modifier.isPublic(m.getModifiers()))
                readResolvePublic = true;
        check(readResolvePublic, "public readResolve declared");
        // full toString + hashCode table
        for (int i = 0; i < all.length; i++) {
            check(all[i].toString().equals("javax.management.openmbean.SimpleType(name=" + cn[i] + ")"),
                    "toString exact " + cn[i]);
            check(all[i].hashCode() == cn[i].hashCode(), "hashCode==className.hashCode " + cn[i]);
        }
        java.util.Set<Integer> hashes = new java.util.HashSet<Integer>();
        for (int i = 0; i < all.length; i++) hashes.add(Integer.valueOf(all[i].hashCode()));
        check(hashes.size() == 14, "hashCodes pairwise distinct");
        // extra isValue negatives
        check(!SimpleType.INTEGER.isValue((short) 5), "INTEGER rejects Short");
        check(!SimpleType.STRING.isValue(new StringBuilder("x")), "STRING rejects StringBuilder");
        check(!SimpleType.OBJECTNAME.isValue("d:k=v"), "OBJECTNAME rejects String");
        check(!SimpleType.VOID.isValue(Void.class), "VOID rejects Void.class");
        check(!SimpleType.INTEGER.isValue(Number.class), "INTEGER rejects Number.class");
        check(!roundtrip(SimpleType.INTEGER).equals(SimpleType.STRING), "roundtrip preserves type identity");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

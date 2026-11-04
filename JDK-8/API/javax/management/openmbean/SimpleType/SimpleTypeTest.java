/*
 * @test
 * @summary javax.management.openmbean.SimpleType conformance: isValue and constants.
 * @run main SimpleTypeTest
 */

import javax.management.openmbean.*;
public class SimpleTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        check(SimpleType.STRING.isValue("x"), "STRING value");
        check(!SimpleType.STRING.isValue(Integer.valueOf(1)), "STRING rejects integer");
        check(SimpleType.INTEGER.isValue(Integer.valueOf(5)), "INTEGER value");
        check(SimpleType.INTEGER.equals(SimpleType.INTEGER), "equals");
        check(!SimpleType.INTEGER.equals(SimpleType.STRING), "not equals different type");
        check(SimpleType.VOID != null && SimpleType.OBJECTNAME != null, "constants");
        testDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened: constants family / singleton semantics / serialization ----
    static final SimpleType<?>[] ALL = {
        SimpleType.VOID, SimpleType.BOOLEAN, SimpleType.CHARACTER, SimpleType.BYTE,
        SimpleType.SHORT, SimpleType.INTEGER, SimpleType.LONG, SimpleType.FLOAT,
        SimpleType.DOUBLE, SimpleType.STRING, SimpleType.BIGDECIMAL, SimpleType.BIGINTEGER,
        SimpleType.DATE, SimpleType.OBJECTNAME
    };
    static final String[] CN = {
        "java.lang.Void", "java.lang.Boolean", "java.lang.Character", "java.lang.Byte",
        "java.lang.Short", "java.lang.Integer", "java.lang.Long", "java.lang.Float",
        "java.lang.Double", "java.lang.String", "java.math.BigDecimal", "java.math.BigInteger",
        "java.util.Date", "javax.management.ObjectName"
    };

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
        Class<?> c = SimpleType.class;
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers())
                && java.lang.reflect.Modifier.isFinal(c.getModifiers()), "white-box: public final class");
        check(OpenType.class.isAssignableFrom(c), "white-box: extends OpenType");
        java.lang.reflect.Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1 && java.lang.reflect.Modifier.isPrivate(ctors[0].getModifiers()),
                "white-box: single private ctor");
        int constants = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields())
            if (java.lang.reflect.Modifier.isStatic(f.getModifiers())
                    && java.lang.reflect.Modifier.isFinal(f.getModifiers())
                    && f.getType() == SimpleType.class) constants++;
        check(constants == 14, "white-box: 14 static final SimpleType constants");
        boolean readResolvePublic = false;
        for (java.lang.reflect.Method m : c.getMethods())
            if ("readResolve".equals(m.getName()) && java.lang.reflect.Modifier.isPublic(m.getModifiers()))
                readResolvePublic = true;
        check(readResolvePublic, "white-box: public readResolve declared");

        // full constant family: className / typeName==className / description==className / !isArray / toString / hashCode
        for (int i = 0; i < ALL.length; i++) {
            check(ALL[i].getClassName().equals(CN[i]), "className " + CN[i]);
            check(ALL[i].getTypeName().equals(CN[i]), "typeName==className " + CN[i]);
            check(ALL[i].getDescription().equals(CN[i]), "description==className " + CN[i]);
            check(!ALL[i].isArray(), "not array " + CN[i]);
            check(ALL[i].toString().equals("javax.management.openmbean.SimpleType(name=" + CN[i] + ")"),
                    "toString exact " + CN[i]);
            check(ALL[i].hashCode() == CN[i].hashCode(), "hashCode==className.hashCode " + CN[i]);
        }
        java.util.Set<Integer> hashes = new java.util.HashSet<Integer>();
        for (int i = 0; i < ALL.length; i++) hashes.add(Integer.valueOf(ALL[i].hashCode()));
        check(hashes.size() == 14, "hashCodes pairwise distinct");

        // singleton semantics: serialization round-trip returns the SAME instance (public readResolve)
        for (int i = 0; i < ALL.length; i++)
            check(roundtrip(ALL[i]) == ALL[i], "readResolve singleton after serialization " + CN[i]);
        check(SimpleType.INTEGER == SimpleType.INTEGER, "static constant reference identity");
        check(!roundtrip(SimpleType.INTEGER).equals(SimpleType.STRING), "roundtrip preserves identity of type");
        check(roundtrip(SimpleType.INTEGER).hashCode() == SimpleType.INTEGER.hashCode(), "roundtrip hashCode");

        // isValue precise (exact runtime class) matrix
        check(SimpleType.INTEGER.isValue(5), "isValue autoboxed int");
        check(!SimpleType.INTEGER.isValue(5L), "isValue rejects Long");
        check(!SimpleType.INTEGER.isValue((short) 5), "isValue rejects Short");
        check(!SimpleType.INTEGER.isValue(null), "isValue rejects null");
        check(SimpleType.STRING.isValue(""), "STRING empty string valid");
        check(!SimpleType.STRING.isValue(new java.lang.StringBuilder("x")), "STRING rejects StringBuilder");
        check(SimpleType.BOOLEAN.isValue(false), "BOOLEAN boxed");
        check(SimpleType.CHARACTER.isValue('a'), "CHARACTER boxed");
        check(SimpleType.BYTE.isValue((byte) 7), "BYTE boxed");
        check(SimpleType.SHORT.isValue((short) 7), "SHORT boxed");
        check(SimpleType.LONG.isValue(7L), "LONG boxed");
        check(SimpleType.FLOAT.isValue(7.5f), "FLOAT boxed");
        check(SimpleType.DOUBLE.isValue(7.5d), "DOUBLE boxed");
        check(SimpleType.BIGDECIMAL.isValue(new java.math.BigDecimal("1.5")), "BIGDECIMAL");
        check(SimpleType.BIGINTEGER.isValue(new java.math.BigInteger("7")), "BIGINTEGER");
        check(SimpleType.DATE.isValue(new java.util.Date(0L)), "DATE");
        check(SimpleType.OBJECTNAME.isValue(new javax.management.ObjectName("d:k=v")), "OBJECTNAME");
        check(!SimpleType.OBJECTNAME.isValue("d:k=v"), "OBJECTNAME rejects String");
        check(!SimpleType.VOID.isValue(null), "VOID rejects null");
        check(!SimpleType.VOID.isValue(Void.class), "VOID rejects Void.class");
        check(!SimpleType.INTEGER.isValue(Number.class), "INTEGER rejects Number.class");

        // equals family
        check(SimpleType.INTEGER.equals(SimpleType.INTEGER), "equals self");
        check(!SimpleType.INTEGER.equals(SimpleType.LONG), "INTEGER != LONG");
        check(!SimpleType.INTEGER.equals(null), "equals null false");
        check(!SimpleType.INTEGER.equals("java.lang.Integer"), "equals non-SimpleType false");
        check(SimpleType.INTEGER.equals(roundtrip(SimpleType.INTEGER)), "equals after roundtrip");
        check(SimpleType.INTEGER.toString().equals(SimpleType.INTEGER.toString()), "toString stable");
    }
}

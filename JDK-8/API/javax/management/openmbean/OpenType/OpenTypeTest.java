/*
 * @test
 * @summary javax.management.openmbean.OpenType conformance: abstract class reflection.
 * @run main OpenTypeTest
 */

public class OpenTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenType");
        check(!c.isInterface(), "not an interface");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Class.forName("java.io.Serializable").isAssignableFrom(c), "implements/extends java.io.Serializable");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "getClassName"), "declares getClassName");
        check(hasMethod(c, "getDescription"), "declares getDescription");
        testDeep(c);
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened: allowed classnames / abstract contract / concrete-subtype evidence ----
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

    static void testDeep(Class<?> c) throws Exception {
        // ALLOWED_CLASSNAMES_LIST: exact 16-entry content, unmodifiable; deprecated array mirrors it
        String[] expected = {
            "java.lang.Void", "java.lang.Boolean", "java.lang.Character", "java.lang.Byte",
            "java.lang.Short", "java.lang.Integer", "java.lang.Long", "java.lang.Float",
            "java.lang.Double", "java.lang.String", "java.math.BigDecimal", "java.math.BigInteger",
            "java.util.Date", "javax.management.ObjectName",
            "javax.management.openmbean.CompositeData", "javax.management.openmbean.TabularData"
        };
        java.util.List<String> allowed = javax.management.openmbean.OpenType.ALLOWED_CLASSNAMES_LIST;
        check(allowed.size() == 16, "allowed list size 16");
        for (String e : expected) check(allowed.contains(e), "allowed contains " + e);
        check(java.util.Arrays.equals(allowed.toArray(new String[0]),
                javax.management.openmbean.OpenType.ALLOWED_CLASSNAMES), "deprecated array mirrors list");
        try {
            allowed.set(0, "x");
            check(false, "ALLOWED_CLASSNAMES_LIST must be unmodifiable");
        } catch (java.lang.UnsupportedOperationException e) {
            passed++;
        }

        // javap verified (JDK-8 == JDK-27): no public isValid / getDisplayName / setDisplayName
        check(!hasMethod(c, "isValid"), "no public isValid");
        check(!hasMethod(c, "getDisplayName"), "no public getDisplayName");
        check(!hasMethod(c, "setDisplayName"), "no public setDisplayName");

        // abstract methods: isValue/equals/hashCode/toString
        check(java.lang.reflect.Modifier.isAbstract(
                c.getMethod("isValue", Object.class).getModifiers()), "isValue abstract");
        check(java.lang.reflect.Modifier.isAbstract(
                c.getMethod("equals", Object.class).getModifiers()), "equals abstract");
        check(java.lang.reflect.Modifier.isAbstract(
                c.getMethod("hashCode").getModifiers()), "hashCode abstract");
        check(java.lang.reflect.Modifier.isAbstract(
                c.getMethod("toString").getModifiers()), "toString abstract");

        // exactly two declared ctors; (String,String,String) is protected and unusable directly
        check(c.getDeclaredConstructors().length == 2, "two declared ctors");
        java.lang.reflect.Constructor<?> prot =
                c.getDeclaredConstructor(String.class, String.class, String.class);
        check(java.lang.reflect.Modifier.isProtected(prot.getModifiers()), "protected (String,String,String) ctor");
        // abstract classes cannot be instantiated; the mechanism differs by JDK:
        // JDK-8 opens the protected ctor and Constructor.newInstance fails with InstantiationException;
        // JDK-9+ refuses setAccessible with InaccessibleObjectException (module java.management
        // does not open the package to the unnamed module) -- either way no instance is produced
        try {
            prot.setAccessible(true);
            try {
                prot.newInstance("a", "b", "c");
                check(false, "abstract instantiation must fail");
            } catch (java.lang.reflect.InvocationTargetException e) {
                check(e.getCause() instanceof java.lang.InstantiationException, "InstantiationException cause");
            } catch (java.lang.InstantiationException e) {
                // Constructor.newInstance throws InstantiationException directly for abstract classes
                passed++;
            }
        } catch (RuntimeException e) {
            if ("java.lang.reflect.InaccessibleObjectException".equals(e.getClass().getName())) {
                passed++;
            } else {
                throw e;
            }
        }

        // concrete-subtype evidence for the inherited contract
        javax.management.openmbean.OpenType<?> simple = javax.management.openmbean.SimpleType.STRING;
        javax.management.openmbean.OpenType<?> array =
                javax.management.openmbean.ArrayType.getArrayType(javax.management.openmbean.SimpleType.STRING);
        javax.management.openmbean.CompositeType ct = new javax.management.openmbean.CompositeType(
                "CT", "d", new String[]{"a"}, new String[]{"da"},
                new javax.management.openmbean.OpenType[]{javax.management.openmbean.SimpleType.STRING});
        javax.management.openmbean.TabularType tt = new javax.management.openmbean.TabularType(
                "TT", "td", ct, new String[]{"a"});
        check(simple.getClassName().equals("java.lang.String")
                && simple.getTypeName().equals("java.lang.String")
                && simple.getDescription().equals("java.lang.String") && !simple.isArray(),
                "SimpleType inherited getters");
        check(array.getClassName().equals("[Ljava.lang.String;") && array.isArray(), "ArrayType inherited getters");
        check(ct.getClassName().equals("javax.management.openmbean.CompositeData")
                && ct.getTypeName().equals("CT") && ct.getDescription().equals("d") && !ct.isArray(),
                "CompositeType inherited getters");
        check(tt.getClassName().equals("javax.management.openmbean.TabularData")
                && tt.getTypeName().equals("TT") && tt.getDescription().equals("td") && !tt.isArray(),
                "TabularType inherited getters");
        // isValue(null) false across all concrete types
        check(!simple.isValue(null) && !array.isValue(null) && !ct.isValue(null) && !tt.isValue(null),
                "isValue(null) false for all concrete types");
        // Serializable via subclasses: SimpleType round-trips to the same singleton
        check(roundtrip(simple) == javax.management.openmbean.SimpleType.STRING, "SimpleType ser singleton");
        check(roundtrip(ct).equals(ct), "CompositeType ser roundtrip");
        check(roundtrip(tt).equals(tt), "TabularType ser roundtrip");
        check(roundtrip(array).equals(array), "ArrayType ser roundtrip");
        // cross-type equals always false
        check(!simple.equals(array) && !array.equals(ct) && !ct.equals(tt), "cross-type equals false");
        check(!simple.equals(null), "equals null false");
    }
}

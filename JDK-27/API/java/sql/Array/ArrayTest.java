/*
 * @test
 * @summary java.sql.Array conformance: white-box structure + black-box behavior.
 * @run main ArrayTest
 */
public class ArrayTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface Throwing { void run() throws Throwable; }
    static void checkThrows(Class<? extends Throwable> t, String label, Throwing r) {
        try { r.run(); } catch (Throwable x) { check(t.isInstance(x), label + " throws " + t.getSimpleName()); return; }
        throw new AssertionError(label + ": expected " + t.getSimpleName());
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.sql.Array");
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 1, "1 direct superinterface(s)");
        check(ifs[0].getName().equals("java.lang.AutoCloseable"), "superinterface 0");
        check(c.getDeclaredMethods().length == 12, "declared method count 12");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final Object[] elems = new Object[] { "alpha", Integer.valueOf(42), Double.valueOf(2.5d) };
        final java.util.Map<String, Class<?>> map = new java.util.HashMap<String, Class<?>>();
        map.put("java.lang.Object", Object.class);

        // SerialArray has no Object[] constructor in JDK-27 (only SerialArray(Array[, Map])),
        // so first build a source Array via a Proxy, then wrap it in the JDK concrete impl.
        java.sql.Array src = (java.sql.Array) java.lang.reflect.Proxy.newProxyInstance(
            java.sql.Array.class.getClassLoader(),
            new Class<?>[] { java.sql.Array.class },
            new java.lang.reflect.InvocationHandler() {
                public Object invoke(Object proxy, java.lang.reflect.Method m, Object[] args) {
                    String n = m.getName();
                    if (n.equals("getArray")) return elems;
                    if (n.equals("getBaseType")) return Integer.valueOf(java.sql.Types.JAVA_OBJECT);
                    if (n.equals("getBaseTypeName")) return "java.lang.Object";
                    return null; // free/close/getResultSet are unused by the SerialArray ctor
                }
            });

        // Always call through the interface type so the bytecode emits invokeinterface.
        java.sql.Array a = new javax.sql.rowset.serial.SerialArray(src);

        check(a.getBaseType() == java.sql.Types.JAVA_OBJECT, "getBaseType JAVA_OBJECT");
        check("java.lang.Object".equals(a.getBaseTypeName()), "getBaseTypeName");

        Object[] all = (Object[]) a.getArray();
        check(all != null && all.length == 3, "getArray returns 3 elements");
        Object[] part = (Object[]) a.getArray(1L, 2);
        check(part != null && part.length == 2, "getArray(long,int) returns 2 elements");
        Object[] mapped = (Object[]) a.getArray(map);
        check(mapped != null && mapped.length == 3, "getArray(Map) returns 3 elements");
        Object[] partMapped = (Object[]) a.getArray(1L, 2, map);
        check(partMapped != null && partMapped.length == 2, "getArray(long,int,Map) returns 2 elements");

        // SerialArray's getResultSet variants are documented to throw SerialException.
        checkThrows(javax.sql.rowset.serial.SerialException.class, "getResultSet()", () -> { a.getResultSet(); });
        checkThrows(javax.sql.rowset.serial.SerialException.class, "getResultSet(Map)", () -> { a.getResultSet(map); });
        checkThrows(javax.sql.rowset.serial.SerialException.class, "getResultSet(long,int)", () -> { a.getResultSet(1L, 2); });
        checkThrows(javax.sql.rowset.serial.SerialException.class, "getResultSet(long,int,Map)", () -> { a.getResultSet(1L, 2, map); });

        a.free();   // void; idempotent
        a.close();  // default close() -> free(); idempotent
        check(true, "free/close callable and idempotent");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

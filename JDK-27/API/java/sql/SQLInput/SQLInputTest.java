/*
 * @test
 * @summary java.sql.SQLInput conformance: white-box structure + black-box behavior.
 * @run main SQLInputTest
 */
public class SQLInputTest {
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
        Class<?> c = Class.forName("java.sql.SQLInput");
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getInterfaces().length == 0, "no direct superinterface");
        check(c.getDeclaredMethods().length == 28, "declared method count 28");
    }

    static Object defaultReturn(Class<?> rt) {
        if (rt == void.class) return null;
        if (!rt.isPrimitive()) return null;
        if (rt == boolean.class) return Boolean.FALSE;
        if (rt == byte.class) return Byte.valueOf((byte) 0);
        if (rt == short.class) return Short.valueOf((short) 0);
        if (rt == char.class) return Character.valueOf('\0');
        if (rt == int.class) return Integer.valueOf(0);
        if (rt == long.class) return Long.valueOf(0L);
        if (rt == float.class) return Float.valueOf(0f);
        if (rt == double.class) return Double.valueOf(0d);
        return null;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final java.util.Map<String, Object> rets = new java.util.HashMap<String, Object>();
        rets.put("readString/0", "hello");
        rets.put("readBoolean/0", Boolean.TRUE);
        rets.put("readByte/0", Byte.valueOf((byte) 5));
        rets.put("readShort/0", Short.valueOf((short) 9));
        rets.put("readInt/0", Integer.valueOf(7));
        rets.put("readLong/0", Long.valueOf(8L));
        rets.put("readFloat/0", Float.valueOf(2.5f));
        rets.put("readDouble/0", Double.valueOf(1.5d));
        rets.put("readBigDecimal/0", new java.math.BigDecimal("1.25"));
        rets.put("readBytes/0", new byte[] { 1, 2 });
        rets.put("readObject/0", "obj");
        rets.put("wasNull/0", Boolean.FALSE);
        rets.put("readNString/0", "nstr");
        rets.put("readObject/1", "typed");

        final java.util.List<String> called = new java.util.ArrayList<String>();
        java.sql.SQLInput si = (java.sql.SQLInput) java.lang.reflect.Proxy.newProxyInstance(
            java.sql.SQLInput.class.getClassLoader(),
            new Class<?>[] { java.sql.SQLInput.class },
            new java.lang.reflect.InvocationHandler() {
                public Object invoke(Object proxy, java.lang.reflect.Method m, Object[] args) {
                    called.add(m.getName() + "/" + m.getParameterCount());
                    String key = m.getName() + "/" + m.getParameterCount();
                    if (rets.containsKey(key)) return rets.get(key);
                    return defaultReturn(m.getReturnType());
                }
            });

        check("hello".equals(si.readString()), "readString");
        check(si.readBoolean(), "readBoolean");
        check(si.readByte() == (byte) 5, "readByte");
        check(si.readShort() == (short) 9, "readShort");
        check(si.readInt() == 7, "readInt");
        check(si.readLong() == 8L, "readLong");
        check(si.readFloat() == 2.5f, "readFloat");
        check(si.readDouble() == 1.5d, "readDouble");
        check(new java.math.BigDecimal("1.25").equals(si.readBigDecimal()), "readBigDecimal");
        check(java.util.Arrays.equals(new byte[] { 1, 2 }, si.readBytes()), "readBytes");
        check(si.readDate() == null, "readDate shape null");
        check(si.readTime() == null, "readTime shape null");
        check(si.readTimestamp() == null, "readTimestamp shape null");
        check(si.readCharacterStream() == null, "readCharacterStream shape null");
        check(si.readAsciiStream() == null, "readAsciiStream shape null");
        check(si.readBinaryStream() == null, "readBinaryStream shape null");
        check("obj".equals(si.readObject()), "readObject");
        check(si.readRef() == null, "readRef shape null");
        check(si.readBlob() == null, "readBlob shape null");
        check(si.readClob() == null, "readClob shape null");
        check(si.readArray() == null, "readArray shape null");
        check(!si.wasNull(), "wasNull false");
        check(si.readURL() == null, "readURL shape null");
        check(si.readNClob() == null, "readNClob shape null");
        check("nstr".equals(si.readNString()), "readNString");
        check(si.readSQLXML() == null, "readSQLXML shape null");
        check(si.readRowId() == null, "readRowId shape null");
        check("typed".equals(si.readObject(String.class)), "readObject(Class)");

        check(called.size() == 28, "all 28 interface methods dispatched");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

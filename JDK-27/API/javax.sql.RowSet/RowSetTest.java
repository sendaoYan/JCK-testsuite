/*
 * @test
 * @summary javax.sql.RowSet conformance: white-box structure + black-box behavior.
 * @run main RowSetTest
 */
public class RowSetTest {
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
        Class<?> c = Class.forName("javax.sql.RowSet");
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 1, "1 direct superinterface(s)");
        check(ifs[0].getName().equals("java.sql.ResultSet"), "superinterface 0");
        check(c.getDeclaredMethods().length == 115, "declared method count 115");
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
        rets.put("getCommand", "SELECT 1");
        rets.put("getDataSourceName", "jdbc/example");
        rets.put("getEscapeProcessing", Boolean.TRUE);
        rets.put("getMaxFieldSize", Integer.valueOf(999));
        rets.put("getMaxRows", Integer.valueOf(42));
        rets.put("getPassword", "secret");
        rets.put("getQueryTimeout", Integer.valueOf(7));
        rets.put("getTransactionIsolation", Integer.valueOf(java.sql.Connection.TRANSACTION_SERIALIZABLE));
        rets.put("getTypeMap", java.util.Collections.emptyMap());
        rets.put("getUrl", "jdbc:example:db");
        rets.put("getUsername", "user");
        rets.put("isReadOnly", Boolean.TRUE);

        final java.util.List<String> called = new java.util.ArrayList<String>();
        javax.sql.RowSet rs = (javax.sql.RowSet) java.lang.reflect.Proxy.newProxyInstance(
            javax.sql.RowSet.class.getClassLoader(),
            new Class<?>[] { javax.sql.RowSet.class },
            new java.lang.reflect.InvocationHandler() {
                public Object invoke(Object proxy, java.lang.reflect.Method m, Object[] args) {
                    called.add(m.getName() + "/" + m.getParameterCount());
                    if (rets.containsKey(m.getName())) return rets.get(m.getName());
                    return defaultReturn(m.getReturnType());
                }
            });

        java.io.InputStream is = null;
        java.io.Reader rd = null;
        java.sql.Blob blob = null;
        java.sql.Clob clob = null;
        java.sql.NClob nclob = null;
        java.sql.Array arr = null;
        java.sql.Ref ref = null;
        java.sql.RowId rowid = null;
        java.sql.SQLXML sxml = null;
        java.net.URL url = null;
        java.util.Calendar cal = null;
        java.util.Map<String, Class<?>> map = null;
        java.math.BigDecimal bd = null;
        java.sql.Date date = null;
        java.sql.Time time = null;
        java.sql.Timestamp ts = null;
        javax.sql.RowSetListener listener = null;

        rs.addRowSetListener(listener);
        rs.clearParameters();
        rs.execute();
        check("SELECT 1".equals(rs.getCommand()), "getCommand");
        check("jdbc/example".equals(rs.getDataSourceName()), "getDataSourceName");
        check(rs.getEscapeProcessing(), "getEscapeProcessing");
        check(rs.getMaxFieldSize() == 999, "getMaxFieldSize");
        check(rs.getMaxRows() == 42, "getMaxRows");
        check("secret".equals(rs.getPassword()), "getPassword");
        check(rs.getQueryTimeout() == 7, "getQueryTimeout");
        check(rs.getTransactionIsolation() == java.sql.Connection.TRANSACTION_SERIALIZABLE, "getTransactionIsolation");
        java.util.Map<?, ?> tm = rs.getTypeMap();
        check(tm != null && tm.isEmpty(), "getTypeMap");
        check("jdbc:example:db".equals(rs.getUrl()), "getUrl");
        check("user".equals(rs.getUsername()), "getUsername");
        check(rs.isReadOnly(), "isReadOnly");
        rs.removeRowSetListener(listener);

        rs.setArray(1, arr);
        rs.setAsciiStream(1, is);
        rs.setAsciiStream(1, is, 100);
        rs.setAsciiStream("c", is);
        rs.setAsciiStream("c", is, 100);
        rs.setBigDecimal(1, bd);
        rs.setBigDecimal("c", bd);
        rs.setBinaryStream(1, is);
        rs.setBinaryStream(1, is, 100);
        rs.setBinaryStream("c", is);
        rs.setBinaryStream("c", is, 100);
        rs.setBlob(1, is);
        rs.setBlob(1, is, 100L);
        rs.setBlob(1, blob);
        rs.setBlob("c", is);
        rs.setBlob("c", is, 100L);
        rs.setBlob("c", blob);
        rs.setBoolean(1, true);
        rs.setBoolean("c", true);
        rs.setByte(1, (byte) 1);
        rs.setByte("c", (byte) 1);
        rs.setBytes(1, new byte[] { 1 });
        rs.setBytes("c", new byte[] { 1 });
        rs.setCharacterStream(1, rd);
        rs.setCharacterStream(1, rd, 100);
        rs.setCharacterStream("c", rd);
        rs.setCharacterStream("c", rd, 100);
        rs.setClob(1, rd);
        rs.setClob(1, rd, 100L);
        rs.setClob(1, clob);
        rs.setClob("c", rd);
        rs.setClob("c", rd, 100L);
        rs.setClob("c", clob);
        rs.setCommand("SELECT 1");
        rs.setConcurrency(java.sql.ResultSet.CONCUR_READ_ONLY);
        rs.setDataSourceName("ds");
        rs.setDate(1, date);
        rs.setDate(1, date, cal);
        rs.setDate("c", date);
        rs.setDate("c", date, cal);
        rs.setDouble(1, 1.5d);
        rs.setDouble("c", 1.5d);
        rs.setEscapeProcessing(true);
        rs.setFloat(1, 1.5f);
        rs.setFloat("c", 1.5f);
        rs.setInt(1, 1);
        rs.setInt("c", 1);
        rs.setLong(1, 1L);
        rs.setLong("c", 1L);
        rs.setMaxFieldSize(999);
        rs.setMaxRows(42);
        rs.setNCharacterStream(1, rd);
        rs.setNCharacterStream(1, rd, 100L);
        rs.setNCharacterStream("c", rd);
        rs.setNCharacterStream("c", rd, 100L);
        rs.setNClob(1, rd);
        rs.setNClob(1, rd, 100L);
        rs.setNClob(1, nclob);
        rs.setNClob("c", rd);
        rs.setNClob("c", rd, 100L);
        rs.setNClob("c", nclob);
        rs.setNString(1, "n");
        rs.setNString("c", "n");
        rs.setNull(1, java.sql.Types.VARCHAR);
        rs.setNull(1, java.sql.Types.VARCHAR, "VARCHAR");
        rs.setNull("c", java.sql.Types.VARCHAR);
        rs.setNull("c", java.sql.Types.VARCHAR, "VARCHAR");
        rs.setObject(1, new Object());
        rs.setObject(1, new Object(), java.sql.Types.VARCHAR);
        rs.setObject(1, new Object(), java.sql.Types.VARCHAR, 20);
        rs.setObject("c", new Object());
        rs.setObject("c", new Object(), java.sql.Types.VARCHAR);
        rs.setObject("c", new Object(), java.sql.Types.VARCHAR, 20);
        rs.setPassword("secret");
        rs.setQueryTimeout(7);
        rs.setReadOnly(true);
        rs.setRef(1, ref);
        rs.setRowId(1, rowid);
        rs.setRowId("c", rowid);
        rs.setSQLXML(1, sxml);
        rs.setSQLXML("c", sxml);
        rs.setShort(1, (short) 1);
        rs.setShort("c", (short) 1);
        rs.setString(1, "s");
        rs.setString("c", "s");
        rs.setTime(1, time);
        rs.setTime(1, time, cal);
        rs.setTime("c", time);
        rs.setTime("c", time, cal);
        rs.setTimestamp(1, ts);
        rs.setTimestamp(1, ts, cal);
        rs.setTimestamp("c", ts);
        rs.setTimestamp("c", ts, cal);
        rs.setTransactionIsolation(java.sql.Connection.TRANSACTION_SERIALIZABLE);
        rs.setType(java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE);
        rs.setTypeMap(map);
        rs.setURL(1, url);
        rs.setUrl("jdbc:example:db");
        rs.setUsername("user");

        check(called.size() == 115, "all 115 declared RowSet methods dispatched");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

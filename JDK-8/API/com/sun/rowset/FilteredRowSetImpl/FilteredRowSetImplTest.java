/*
 * @test
 * @summary com.sun.rowset.FilteredRowSetImpl conformance: hierarchy and declared members via reflection.
 * @run main FilteredRowSetImplTest
 */

public class FilteredRowSetImplTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.rowset.FilteredRowSetImpl", false, null);
        check(c.getName().equals("com.sun.rowset.FilteredRowSetImpl"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.rowset.WebRowSetImpl"), "superclass com.sun.rowset.WebRowSetImpl");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("java.io.Serializable"), "implements java.io.Serializable");
        check(in.contains("java.lang.Cloneable"), "implements java.lang.Cloneable");
        check(in.contains("javax.sql.rowset.FilteredRowSet"), "implements javax.sql.rowset.FilteredRowSet");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("absolute"), "declares absolute");
        check(ms.contains("getFilter"), "declares getFilter");
        check(ms.contains("insertRow"), "declares insertRow");
        check(ms.contains("internalFirst"), "declares internalFirst");
        check(ms.contains("internalLast"), "declares internalLast");
        check(ms.contains("internalNext"), "declares internalNext");
        check(ms.contains("internalPrevious"), "declares internalPrevious");
        check(ms.contains("moveToInsertRow"), "declares moveToInsertRow");
        check(ms.contains("readObject"), "declares readObject");
        check(ms.contains("relative"), "declares relative");
        check(ms.contains("setFilter"), "declares setFilter");
        check(ms.contains("updateAsciiStream"), "declares updateAsciiStream");
        check(ms.contains("updateBigDecimal"), "declares updateBigDecimal");
        check(ms.contains("updateBinaryStream"), "declares updateBinaryStream");
        check(ms.contains("updateBoolean"), "declares updateBoolean");
        check(ms.contains("updateByte"), "declares updateByte");
        check(ms.contains("updateBytes"), "declares updateBytes");
        check(ms.contains("updateCharacterStream"), "declares updateCharacterStream");
        check(ms.contains("updateDate"), "declares updateDate");
        check(ms.contains("updateDouble"), "declares updateDouble");
        check(ms.contains("updateFloat"), "declares updateFloat");
        check(ms.contains("updateInt"), "declares updateInt");
        check(ms.contains("updateLong"), "declares updateLong");
        check(ms.contains("updateObject"), "declares updateObject");
        check(ms.contains("updateShort"), "declares updateShort");
        check(ms.contains("updateString"), "declares updateString");
        check(ms.contains("updateTime"), "declares updateTime");
        check(ms.contains("updateTimestamp"), "declares updateTimestamp");

        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary com.sun.rowset.FilteredRowSetImpl conformance: internal class reflection.
 * @modules java.sql.rowset/com.sun.rowset:+open
 * @run main FilteredRowSetImplTest
 */
public class FilteredRowSetImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.rowset.FilteredRowSetImpl", false, FilteredRowSetImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.rowset.FilteredRowSetImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.rowset.WebRowSetImpl"), "superclass");
        {
            java.lang.Class<?>[] arr = c.getInterfaces();
            String[] n = new String[arr.length];
            for (int i = 0; i < arr.length; i++) n[i] = arr[i].getName();
            java.util.Arrays.sort(n);
            check(String.join(";", n).equals("java.io.Serializable;java.lang.Cloneable;javax.sql.rowset.FilteredRowSet"), "interfaces");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                int mod = m.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(m.getName());
            }
            check(String.join(";", s).equals("absolute;getFilter;insertRow;internalFirst;internalLast;internalNext;internalPrevious;moveToInsertRow;relative;setFilter;updateAsciiStream;updateBigDecimal;updateBinaryStream;updateBoolean;updateByte;updateBytes;updateCharacterStream;updateDate;updateDouble;updateFloat;updateInt;updateLong;updateObject;updateShort;updateString;updateTime;updateTimestamp"), "methods");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                int mod = f.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(f.getName());
            }
            check(String.join(";", s).equals(""), "fields");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}

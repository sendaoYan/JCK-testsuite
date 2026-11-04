/*
 * @test
 * @summary com.sun.rowset.internal.SyncResolverImpl conformance: internal class reflection.
 * @modules java.sql.rowset/com.sun.rowset.internal:+open
 * @run main SyncResolverImplTest
 */
public class SyncResolverImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.rowset.internal.SyncResolverImpl", false, SyncResolverImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.rowset.internal.SyncResolverImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.rowset.CachedRowSetImpl"), "superclass");
        {
            java.lang.Class<?>[] arr = c.getInterfaces();
            String[] n = new String[arr.length];
            for (int i = 0; i < arr.length; i++) n[i] = arr[i].getName();
            java.util.Arrays.sort(n);
            check(String.join(";", n).equals("javax.sql.rowset.spi.SyncResolver"), "interfaces");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                int mod = m.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(m.getName());
            }
            check(String.join(";", s).equals("absolute;acceptChanges;afterLast;beforeFirst;cancelRowUpdates;clearWarnings;clone;close;columnUpdated;commit;createCopy;createCopyNoConstraints;createCopySchema;createShared;deleteRow;execute;findColumn;first;getArray;getAsciiStream;getBigDecimal;getBinaryStream;getBlob;getBoolean;getByte;getBytes;getCharacterStream;getClob;getConflictValue;getConnection;getCurrentRow;getCursorName;getDate;getDouble;getFloat;getInt;getKeyColumns;getLong;getMatchColumnIndexes;getMatchColumnNames;getMetaData;getObject;getOriginal;getOriginalRow;getPageSize;getRef;getRow;getRowSetWarnings;getShort;getStatement;getStatus;getString;getSyncProvider;getTableName;getTime;getTimestamp;getURL;getUnicodeStream;getWarnings;insertRow;internalFirst;internalLast;internalNext;internalPrevious;isAfterLast;isBeforeFirst;isFirst;isLast;last;moveToCurrentRow;moveToInsertRow;next;nextConflict;nextPage;populate;previous;previousConflict;previousPage;refreshRow;relative;release;removeCurrentRow;restoreOriginal;rollback;rowDeleted;rowInserted;rowSetPopulated;rowUpdated;setCommand;setKeyColumns;setMatchColumn;setMetaData;setOriginal;setOriginalRow;setPageSize;setResolvedValue;setSyncProvider;setTableName;size;toCollection;undoDelete;undoInsert;undoUpdate;unsetMatchColumn;updateArray;updateAsciiStream;updateBigDecimal;updateBinaryStream;updateBlob;updateBoolean;updateByte;updateBytes;updateCharacterStream;updateClob;updateDate;updateDouble;updateFloat;updateInt;updateLong;updateNCharacterStream;updateNull;updateObject;updateRef;updateRow;updateShort;updateString;updateTime;updateTimestamp;wasNull"), "methods");
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

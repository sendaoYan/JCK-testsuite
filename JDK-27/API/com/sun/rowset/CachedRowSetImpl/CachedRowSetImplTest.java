/*
 * @test
 * @summary com.sun.rowset.CachedRowSetImpl conformance: internal class reflection.
 * @modules java.sql.rowset/com.sun.rowset:+open
 * @run main CachedRowSetImplTest
 */
public class CachedRowSetImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.rowset.CachedRowSetImpl", false, CachedRowSetImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.rowset.CachedRowSetImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.sql.rowset.BaseRowSet"), "superclass");
        {
            java.lang.Class<?>[] arr = c.getInterfaces();
            String[] n = new String[arr.length];
            for (int i = 0; i < arr.length; i++) n[i] = arr[i].getName();
            java.util.Arrays.sort(n);
            check(String.join(";", n).equals("java.io.Serializable;java.lang.Cloneable;javax.sql.RowSet;javax.sql.RowSetInternal;javax.sql.rowset.CachedRowSet"), "interfaces");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                int mod = m.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(m.getName());
            }
            check(String.join(";", s).equals("absolute;acceptChanges;afterLast;beforeFirst;cancelRowUpdates;clearWarnings;clone;close;columnUpdated;commit;createCopy;createCopyNoConstraints;createCopySchema;createShared;deleteRow;execute;findColumn;first;getArray;getAsciiStream;getBigDecimal;getBinaryStream;getBlob;getBoolean;getByte;getBytes;getCharacterStream;getClob;getConnection;getCurrentRow;getCursorName;getDate;getDouble;getFloat;getHoldability;getInt;getKeyColumns;getLong;getMatchColumnIndexes;getMatchColumnNames;getMetaData;getNCharacterStream;getNClob;getNString;getObject;getOriginal;getOriginalRow;getPageSize;getRef;getRow;getRowId;getRowSetWarnings;getSQLXML;getShort;getStatement;getString;getSyncProvider;getTableName;getTime;getTimestamp;getURL;getUnicodeStream;getWarnings;insertRow;internalFirst;internalLast;internalNext;internalPrevious;isAfterLast;isBeforeFirst;isClosed;isFirst;isLast;isWrapperFor;last;moveToCurrentRow;moveToInsertRow;next;nextPage;populate;previous;previousPage;refreshRow;relative;release;removeCurrentRow;restoreOriginal;rollback;rowDeleted;rowInserted;rowSetPopulated;rowUpdated;setAsciiStream;setBigDecimal;setBinaryStream;setBlob;setBoolean;setByte;setBytes;setCharacterStream;setClob;setCommand;setDate;setDouble;setFloat;setInt;setKeyColumns;setLong;setMatchColumn;setMetaData;setNCharacterStream;setNClob;setNString;setNull;setObject;setOriginal;setOriginalRow;setPageSize;setRowId;setRowInserted;setSQLXML;setShort;setString;setSyncProvider;setTableName;setTime;setTimestamp;setURL;size;toCollection;undoDelete;undoInsert;undoUpdate;unsetMatchColumn;unwrap;updateArray;updateAsciiStream;updateBigDecimal;updateBinaryStream;updateBlob;updateBoolean;updateByte;updateBytes;updateCharacterStream;updateClob;updateDate;updateDouble;updateFloat;updateInt;updateLong;updateNCharacterStream;updateNClob;updateNString;updateNull;updateObject;updateRef;updateRow;updateRowId;updateSQLXML;updateShort;updateString;updateTime;updateTimestamp;wasNull"), "methods");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                int mod = f.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(f.getName());
            }
            check(String.join(";", s).equals("resBundle"), "fields");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}

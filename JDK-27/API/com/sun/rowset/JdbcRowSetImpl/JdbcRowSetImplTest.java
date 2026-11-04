/*
 * @test
 * @summary com.sun.rowset.JdbcRowSetImpl conformance: internal class reflection.
 * @modules java.sql.rowset/com.sun.rowset:+open
 * @run main JdbcRowSetImplTest
 */
public class JdbcRowSetImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.rowset.JdbcRowSetImpl", false, JdbcRowSetImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.rowset.JdbcRowSetImpl"), "name");
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
            check(String.join(";", n).equals("javax.sql.rowset.JdbcRowSet;javax.sql.rowset.Joinable"), "interfaces");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                int mod = m.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(m.getName());
            }
            check(String.join(";", s).equals("absolute;afterLast;beforeFirst;cancelRowUpdates;checkState;clearWarnings;close;commit;deleteRow;execute;findColumn;first;getArray;getAsciiStream;getAutoCommit;getBigDecimal;getBinaryStream;getBlob;getBoolean;getByte;getBytes;getCharacterStream;getClob;getConcurrency;getConnection;getCursorName;getDatabaseMetaData;getDate;getDouble;getFetchDirection;getFloat;getHoldability;getInt;getLong;getMatchColumnIndexes;getMatchColumnNames;getMetaData;getNCharacterStream;getNClob;getNString;getObject;getParameterMetaData;getPreparedStatement;getRef;getResultSet;getRow;getRowId;getRowSetWarnings;getSQLXML;getShort;getStatement;getString;getTime;getTimestamp;getType;getURL;getUnicodeStream;getWarnings;initMetaData;insertRow;isAfterLast;isBeforeFirst;isClosed;isFirst;isLast;isWrapperFor;last;moveToCurrentRow;moveToInsertRow;next;prepare;previous;refreshRow;relative;rollback;rowDeleted;rowInserted;rowUpdated;setAsciiStream;setAutoCommit;setBigDecimal;setBinaryStream;setBlob;setBoolean;setByte;setBytes;setCharacterStream;setClob;setCommand;setConcurrency;setConnection;setDataSourceName;setDate;setDouble;setFetchDirection;setFetchSize;setFloat;setInt;setLong;setMatchColumn;setNCharacterStream;setNClob;setNString;setNull;setObject;setParams;setPassword;setPreparedStatement;setProperties;setResultSet;setRowId;setSQLXML;setShort;setString;setTime;setTimestamp;setType;setURL;setUrl;setUsername;unsetMatchColumn;unwrap;updateArray;updateAsciiStream;updateBigDecimal;updateBinaryStream;updateBlob;updateBoolean;updateByte;updateBytes;updateCharacterStream;updateClob;updateDate;updateDouble;updateFloat;updateInt;updateLong;updateNCharacterStream;updateNClob;updateNString;updateNull;updateObject;updateRef;updateRow;updateRowId;updateSQLXML;updateShort;updateString;updateTime;updateTimestamp;wasNull"), "methods");
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

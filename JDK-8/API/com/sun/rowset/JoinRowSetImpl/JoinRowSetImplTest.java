/*
 * @test
 * @summary com.sun.rowset.JoinRowSetImpl conformance: hierarchy and declared members via reflection.
 * @run main JoinRowSetImplTest
 */

public class JoinRowSetImplTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.rowset.JoinRowSetImpl", false, null);
        check(c.getName().equals("com.sun.rowset.JoinRowSetImpl"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.rowset.WebRowSetImpl"), "superclass com.sun.rowset.WebRowSetImpl");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("javax.sql.rowset.JoinRowSet"), "implements javax.sql.rowset.JoinRowSet");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("absolute"), "declares absolute");
        check(ms.contains("acceptChanges"), "declares acceptChanges");
        check(ms.contains("addRowSet"), "declares addRowSet");
        check(ms.contains("addRowSetListener"), "declares addRowSetListener");
        check(ms.contains("afterLast"), "declares afterLast");
        check(ms.contains("beforeFirst"), "declares beforeFirst");
        check(ms.contains("cancelRowUpdates"), "declares cancelRowUpdates");
        check(ms.contains("checkforMatchColumn"), "declares checkforMatchColumn");
        check(ms.contains("clearWarnings"), "declares clearWarnings");
        check(ms.contains("close"), "declares close");
        check(ms.contains("columnUpdated"), "declares columnUpdated");
        check(ms.contains("createCopySchema"), "declares createCopySchema");
        check(ms.contains("createWebRowSet"), "declares createWebRowSet");
        check(ms.contains("deleteRow"), "declares deleteRow");
        check(ms.contains("execute"), "declares execute");
        check(ms.contains("findColumn"), "declares findColumn");
        check(ms.contains("first"), "declares first");
        check(ms.contains("getArray"), "declares getArray");
        check(ms.contains("getAsciiStream"), "declares getAsciiStream");
        check(ms.contains("getBigDecimal"), "declares getBigDecimal");
        check(ms.contains("getBinaryStream"), "declares getBinaryStream");
        check(ms.contains("getBlob"), "declares getBlob");
        check(ms.contains("getBoolean"), "declares getBoolean");
        check(ms.contains("getByte"), "declares getByte");
        check(ms.contains("getBytes"), "declares getBytes");
        check(ms.contains("getCharacterStream"), "declares getCharacterStream");
        check(ms.contains("getClob"), "declares getClob");
        check(ms.contains("getCursorName"), "declares getCursorName");
        check(ms.contains("getDate"), "declares getDate");
        check(ms.contains("getDouble"), "declares getDouble");
        check(ms.contains("getFloat"), "declares getFloat");
        check(ms.contains("getInt"), "declares getInt");
        check(ms.contains("getJoinType"), "declares getJoinType");
        check(ms.contains("getKeyColumns"), "declares getKeyColumns");
        check(ms.contains("getLong"), "declares getLong");
        check(ms.contains("getMetaData"), "declares getMetaData");
        check(ms.contains("getObject"), "declares getObject");
        check(ms.contains("getOriginal"), "declares getOriginal");
        check(ms.contains("getOriginalRow"), "declares getOriginalRow");
        check(ms.contains("getRef"), "declares getRef");
        check(ms.contains("getRow"), "declares getRow");
        check(ms.contains("getRowSetNames"), "declares getRowSetNames");
        check(ms.contains("getRowSets"), "declares getRowSets");
        check(ms.contains("getShort"), "declares getShort");
        check(ms.contains("getStatement"), "declares getStatement");
        check(ms.contains("getString"), "declares getString");
        check(ms.contains("getSyncProvider"), "declares getSyncProvider");
        check(ms.contains("getTime"), "declares getTime");
        check(ms.contains("getTimestamp"), "declares getTimestamp");
        check(ms.contains("getURL"), "declares getURL");
        check(ms.contains("getUnicodeStream"), "declares getUnicodeStream");
        check(ms.contains("getWarnings"), "declares getWarnings");
        check(ms.contains("getWhereClause"), "declares getWhereClause");
        check(ms.contains("initJOIN"), "declares initJOIN");
        check(ms.contains("insertRow"), "declares insertRow");
        check(ms.contains("isAfterLast"), "declares isAfterLast");
        check(ms.contains("isBeforeFirst"), "declares isBeforeFirst");
        check(ms.contains("isFirst"), "declares isFirst");
        check(ms.contains("isLast"), "declares isLast");
        check(ms.contains("last"), "declares last");
        check(ms.contains("moveToCurrentRow"), "declares moveToCurrentRow");
        check(ms.contains("moveToInsertRow"), "declares moveToInsertRow");
        check(ms.contains("next"), "declares next");
        check(ms.contains("previous"), "declares previous");
        check(ms.contains("readObject"), "declares readObject");
        check(ms.contains("readXml"), "declares readXml");
        check(ms.contains("refreshRow"), "declares refreshRow");
        check(ms.contains("relative"), "declares relative");
        check(ms.contains("removeRowSetListener"), "declares removeRowSetListener");
        check(ms.contains("rowDeleted"), "declares rowDeleted");
        check(ms.contains("rowInserted"), "declares rowInserted");
        check(ms.contains("rowUpdated"), "declares rowUpdated");
        check(ms.contains("setJoinType"), "declares setJoinType");
        check(ms.contains("setKeyColumns"), "declares setKeyColumns");
        check(ms.contains("setMetaData"), "declares setMetaData");
        check(ms.contains("setOriginalRow"), "declares setOriginalRow");
        check(ms.contains("setSyncProvider"), "declares setSyncProvider");
        check(ms.contains("size"), "declares size");
        check(ms.contains("supportsCrossJoin"), "declares supportsCrossJoin");
        check(ms.contains("supportsFullJoin"), "declares supportsFullJoin");
        check(ms.contains("supportsInnerJoin"), "declares supportsInnerJoin");
        check(ms.contains("supportsLeftOuterJoin"), "declares supportsLeftOuterJoin");
        check(ms.contains("supportsRightOuterJoin"), "declares supportsRightOuterJoin");
        check(ms.contains("toCachedRowSet"), "declares toCachedRowSet");
        check(ms.contains("toCollection"), "declares toCollection");
        check(ms.contains("updateArray"), "declares updateArray");
        check(ms.contains("updateAsciiStream"), "declares updateAsciiStream");
        check(ms.contains("updateBigDecimal"), "declares updateBigDecimal");
        check(ms.contains("updateBinaryStream"), "declares updateBinaryStream");
        check(ms.contains("updateBlob"), "declares updateBlob");
        check(ms.contains("updateBoolean"), "declares updateBoolean");
        check(ms.contains("updateByte"), "declares updateByte");
        check(ms.contains("updateBytes"), "declares updateBytes");
        check(ms.contains("updateCharacterStream"), "declares updateCharacterStream");
        check(ms.contains("updateClob"), "declares updateClob");
        check(ms.contains("updateDate"), "declares updateDate");
        check(ms.contains("updateDouble"), "declares updateDouble");
        check(ms.contains("updateFloat"), "declares updateFloat");
        check(ms.contains("updateInt"), "declares updateInt");
        check(ms.contains("updateLong"), "declares updateLong");
        check(ms.contains("updateNull"), "declares updateNull");
        check(ms.contains("updateObject"), "declares updateObject");
        check(ms.contains("updateRef"), "declares updateRef");
        check(ms.contains("updateRow"), "declares updateRow");
        check(ms.contains("updateShort"), "declares updateShort");
        check(ms.contains("updateString"), "declares updateString");
        check(ms.contains("updateTime"), "declares updateTime");
        check(ms.contains("updateTimestamp"), "declares updateTimestamp");
        check(ms.contains("wasNull"), "declares wasNull");
        check(ms.contains("writeXml"), "declares writeXml");

        System.out.println("PASS (" + passed + " assertions)");
    }
}

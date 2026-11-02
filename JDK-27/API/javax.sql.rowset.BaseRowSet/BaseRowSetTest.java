/*
 * @test
 * @summary javax.sql.rowset.BaseRowSet conformance: abstract class shape and parameter behavior
 * @run main BaseRowSetTest
 */

import java.lang.reflect.Modifier;

public class BaseRowSetTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.sql.rowset.BaseRowSet");
        check(!c.isInterface() && java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(c.getInterfaces().length == 2, "implements Serializable and Cloneable");
        check(c.getDeclaredMethods().length == 130, "declared method count 130");
        check(javax.sql.rowset.BaseRowSet.UNICODE_STREAM_PARAM == 0, "UNICODE_STREAM_PARAM == 0");
        check(javax.sql.rowset.BaseRowSet.BINARY_STREAM_PARAM == 1, "BINARY_STREAM_PARAM == 1");
        check(javax.sql.rowset.BaseRowSet.ASCII_STREAM_PARAM == 2, "ASCII_STREAM_PARAM == 2");
        javax.sql.rowset.BaseRowSet b = new javax.sql.rowset.BaseRowSet() {};
        check(b.isReadOnly() == false, "isReadOnly default false");
        b.setReadOnly(true);
        check(b.isReadOnly() == true, "setReadOnly/isReadOnly");
        b.setShowDeleted(true);
        check(b.getShowDeleted() == true, "setShowDeleted/getShowDeleted");
        b.setUsername("u");
        check("u".equals(b.getUsername()), "setUsername/getUsername");
        b.setPassword("p");
        check("p".equals(b.getPassword()), "setPassword/getPassword");
        b.setUrl("jdbc:test");
        check("jdbc:test".equals(b.getUrl()), "setUrl/getUrl");
        b.setType(java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE);
        check(b.getType() == java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE, "setType/getType");
        b.setConcurrency(java.sql.ResultSet.CONCUR_READ_ONLY);
        check(b.getConcurrency() == java.sql.ResultSet.CONCUR_READ_ONLY, "setConcurrency/getConcurrency");
        b.setFetchSize(10);
        check(b.getFetchSize() == 10, "setFetchSize/getFetchSize");
        b.setMaxRows(100);
        check(b.getMaxRows() == 100, "setMaxRows/getMaxRows");
        check(b.getParams() != null, "getParams non-null");        System.out.println("PASS (" + passed + " assertions)");
    }
}

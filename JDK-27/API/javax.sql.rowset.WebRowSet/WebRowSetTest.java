/*
 * @test
 * @summary javax.sql.rowset.WebRowSet conformance: interface shape, supertypes and constants
 * @run main WebRowSetTest
 */

import java.lang.reflect.Modifier;

public class WebRowSetTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.sql.rowset.WebRowSet");
        check(c != null, "loads");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 1, "1 direct superinterface(s)");
        check(ifs[0].getName().equals("javax.sql.rowset.CachedRowSet"), "superinterface 0");
        check(c.getDeclaredMethods().length == 6, "declared method count 6");
        java.lang.reflect.Field f_PUBLIC_XML_SCHEMA = c.getField("PUBLIC_XML_SCHEMA");
        check("--//Oracle Corporation//XSD Schema//EN".equals(f_PUBLIC_XML_SCHEMA.get(null)), "PUBLIC_XML_SCHEMA value");
        java.lang.reflect.Field f_SCHEMA_SYSTEM_ID = c.getField("SCHEMA_SYSTEM_ID");
        check("http://java.sun.com/xml/ns/jdbc/webrowset.xsd".equals(f_SCHEMA_SYSTEM_ID.get(null)), "SCHEMA_SYSTEM_ID value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

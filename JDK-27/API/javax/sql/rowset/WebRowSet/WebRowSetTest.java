/*
 * @test
 * @summary javax.sql.rowset.WebRowSet conformance: white-box structure + black-box behavior.
 * @run main WebRowSetTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetMetaDataImpl;
import javax.sql.rowset.RowSetProvider;
import javax.sql.rowset.WebRowSet;

public class WebRowSetTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static WebRowSet fill(RowSetFactory f) throws Exception {
        WebRowSet wrs = f.createWebRowSet();
        RowSetMetaDataImpl md = new RowSetMetaDataImpl();
        md.setColumnCount(2);
        md.setColumnName(1, "ID");
        md.setColumnType(1, Types.INTEGER);
        md.setColumnName(2, "NAME");
        md.setColumnType(2, Types.VARCHAR);
        wrs.setMetaData(md);
        wrs.moveToInsertRow(); wrs.updateInt(1, 1); wrs.updateString(2, "Alice"); wrs.insertRow();
        wrs.moveToInsertRow(); wrs.updateInt(1, 2); wrs.updateString(2, "Bob"); wrs.insertRow();
        wrs.moveToInsertRow(); wrs.updateInt(1, 3); wrs.updateString(2, "Carol"); wrs.insertRow();
        wrs.moveToCurrentRow();
        wrs.beforeFirst();
        return wrs;
    }

    static List<Object[]> readRows(ResultSet rs) throws Exception {
        List<Object[]> rows = new ArrayList<>();
        rs.beforeFirst();
        while (rs.next()) {
            rows.add(new Object[]{ rs.getInt(1), rs.getString(2) });
        }
        return rows;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
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
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        RowSetFactory f = RowSetProvider.newFactory();
        WebRowSet wrs = f.createWebRowSet();
        check(wrs != null, "createWebRowSet non-null");
        check(wrs instanceof WebRowSet, "instanceof WebRowSet");
        check(wrs instanceof CachedRowSet, "instanceof CachedRowSet");
        check(wrs instanceof javax.sql.rowset.Joinable, "instanceof Joinable");
        check(wrs.getClass().getName().equals("com.sun.rowset.WebRowSetImpl"), "impl class name");

        wrs = fill(f);
        check(wrs.size() == 3, "size 3 after fill");

        // OutputStream -> InputStream roundtrip
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        wrs.writeXml(baos);
        byte[] xml = baos.toByteArray();
        check(xml.length > 0, "writeXml(OutputStream) produced bytes");
        WebRowSet back = f.createWebRowSet();
        back.readXml(new ByteArrayInputStream(xml));
        check(back.size() == 3, "readXml(InputStream) size 3");
        List<Object[]> rows = readRows(back);
        check(rows.size() == 3 && ((Integer) rows.get(0)[0]) == 1 && "Alice".equals(rows.get(0)[1])
                && "Carol".equals(rows.get(2)[1]), "readXml data preserved");
        check(back.getMetaData().getColumnCount() == 2, "readXml metadata preserved");

        // Writer -> Reader roundtrip
        StringWriter sw = new StringWriter();
        wrs.writeXml(sw);
        String xmlText = sw.toString();
        check(xmlText.contains("webRowSet"), "writeXml(Writer) contains webRowSet");
        WebRowSet back2 = f.createWebRowSet();
        back2.readXml(new StringReader(xmlText));
        check(back2.size() == 3, "readXml(Reader) size 3");
        List<Object[]> rows2 = readRows(back2);
        check(rows2.size() == 3 && ((Integer) rows2.get(1)[0]) == 2 && "Bob".equals(rows2.get(1)[1]),
                "readXml(Reader) data preserved");

        // writeXml(ResultSet, OutputStream) roundtrip from a source rowset
        CachedRowSet src = fill(f);
        ByteArrayOutputStream baos3 = new ByteArrayOutputStream();
        WebRowSet wrs3 = f.createWebRowSet();
        wrs3.writeXml(src, baos3);
        WebRowSet back3 = f.createWebRowSet();
        back3.readXml(new ByteArrayInputStream(baos3.toByteArray()));
        check(back3.size() == 3, "writeXml(ResultSet,OutputStream) -> readXml size 3");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

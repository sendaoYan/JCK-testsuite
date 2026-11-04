/*
 * @test
 * @summary javax.sql.rowset.spi.XmlWriter conformance: white-box structure + black-box behavior.
 * @run main XmlWriterTest
 */

import java.io.StringWriter;
import java.io.Writer;
import java.sql.SQLException;
import javax.sql.RowSetInternal;
import javax.sql.RowSetWriter;
import javax.sql.rowset.WebRowSet;
import javax.sql.rowset.spi.XmlWriter;

public class XmlWriterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sql.rowset.spi.XmlWriter");
        check(c != null, "loads");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 1, "1 direct superinterface(s)");
        check(ifs[0].getName().equals("javax.sql.RowSetWriter"), "superinterface 0");
        check(c.getDeclaredMethods().length == 1, "declared method count 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final boolean[] called = new boolean[1];
        XmlWriter xw = new XmlWriter() {
            public boolean writeData(RowSetInternal caller) throws SQLException {
                return true;
            }
            public void writeXML(WebRowSet caller, Writer writer) throws SQLException {
                called[0] = true;
                try {
                    writer.write("<rowset/>");
                } catch (java.io.IOException e) {
                    throw new SQLException(e);
                }
            }
        };

        check(xw instanceof RowSetWriter, "XmlWriter is a RowSetWriter");

        // writeData inherited from RowSetWriter is callable and returns true.
        check(xw.writeData(null) == true, "writeData returns true");

        // writeXML writes the XML representation into the supplied Writer.
        StringWriter out = new StringWriter();
        xw.writeXML(null, out);
        check(called[0], "writeXML was invoked");
        check("<rowset/>".equals(out.toString()), "writeXML wrote expected content into Writer");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

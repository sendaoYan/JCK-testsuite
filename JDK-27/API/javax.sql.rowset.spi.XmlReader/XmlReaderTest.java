/*
 * @test
 * @summary javax.sql.rowset.spi.XmlReader conformance: white-box structure + black-box behavior.
 * @run main XmlReaderTest
 */

import java.io.Reader;
import java.io.StringReader;
import java.sql.SQLException;
import javax.sql.RowSetInternal;
import javax.sql.RowSetReader;
import javax.sql.rowset.WebRowSet;
import javax.sql.rowset.spi.XmlReader;

public class XmlReaderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sql.rowset.spi.XmlReader");
        check(c != null, "loads");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 1, "1 direct superinterface(s)");
        check(ifs[0].getName().equals("javax.sql.RowSetReader"), "superinterface 0");
        check(c.getDeclaredMethods().length == 1, "declared method count 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final StringBuilder captured = new StringBuilder();
        final boolean[] called = new boolean[1];
        XmlReader xr = new XmlReader() {
            public void readData(RowSetInternal caller) throws SQLException {
                // RowSetReader.readData is exercised separately and does nothing here.
            }
            public void readXML(WebRowSet caller, Reader reader) throws SQLException {
                called[0] = true;
                char[] buf = new char[64];
                try {
                    int n;
                    while ((n = reader.read(buf)) != -1) {
                        captured.append(buf, 0, n);
                    }
                } catch (java.io.IOException e) {
                    throw new SQLException(e);
                }
            }
        };

        check(xr instanceof RowSetReader, "XmlReader is a RowSetReader");

        // readData inherited from RowSetReader is callable.
        xr.readData(null);

        // readXML consumes the supplied Reader and preserves its content.
        String xml = "<rowset><data>hello</data></rowset>";
        xr.readXML(null, new StringReader(xml));
        check(called[0], "readXML was invoked");
        check(xml.equals(captured.toString()), "readXML consumed Reader content verbatim");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

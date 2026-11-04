/*
 * @test
 * @summary javax.print.DocFlavor conformance: doc flavor class.
 * @run main DocFlavorTest
 */

import java.lang.reflect.*;
import javax.print.DocFlavor;

public class DocFlavorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasIf(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    public static void main(String[] a) throws Exception {
        DocFlavor f = new DocFlavor("text/plain; charset=utf-8", "java.lang.String");
        check("text".equals(f.getMediaType()), "getMediaType");
        check("plain".equals(f.getMediaSubtype()), "getMediaSubtype");
        check("java.lang.String".equals(f.getRepresentationClassName()), "getRepresentationClassName");
        check("utf-8".equals(f.getParameter("charset")), "getParameter");
        check(f.getMimeType() != null && f.getMimeType().length() > 0, "getMimeType non-empty");
        DocFlavor f2 = new DocFlavor("text/plain; charset=utf-8", "java.lang.String");
        check(f.equals(f2) && f.hashCode() == f2.hashCode(), "equals/hashCode");
        check(f.toString() != null, "toString");
        check(javax.print.DocFlavor.hostEncoding != null, "hostEncoding");
        Class<?> c = Class.forName("javax.print.DocFlavor");
        check(hasIf(c, "java.io.Serializable"), "Serializable");
        check(hasIf(c, "java.lang.Cloneable"), "Cloneable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

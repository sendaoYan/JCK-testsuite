/*
 * @test
 * @summary javax.xml.transform.sax.TransformerHandler conformance: interface hierarchy and methods.
 * @run main TransformerHandlerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class TransformerHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.transform.sax.TransformerHandler");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "org.xml.sax.ContentHandler"), "implements org.xml.sax.ContentHandler");
        check(has(ifs, "org.xml.sax.ext.LexicalHandler"), "implements org.xml.sax.ext.LexicalHandler");
        check(has(ifs, "org.xml.sax.DTDHandler"), "implements org.xml.sax.DTDHandler");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setResult"), "has method setResult");
        check(names.contains("setSystemId"), "has method setSystemId");
        check(names.contains("getSystemId"), "has method getSystemId");
        check(names.contains("getTransformer"), "has method getTransformer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

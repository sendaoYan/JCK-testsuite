/*
 * @test
 * @summary org.w3c.dom.css.CSS2Properties conformance: reflection shape checks.
 * @run main CSS2PropertiesTest
 */

import java.lang.reflect.*;

public class CSS2PropertiesTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.w3c.dom.css.CSS2Properties");
        check(c != null, "org.w3c.dom.css.CSS2Properties loads");
        check(c.getName().equals("org.w3c.dom.css.CSS2Properties"), "binary name is org.w3c.dom.css.CSS2Properties");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.css.CSS2Properties is public");
        check(c.isInterface(), "org.w3c.dom.css.CSS2Properties is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getAzimuth"), "declares getAzimuth");
        check(declares(c, "setAzimuth"), "declares setAzimuth");
        check(declares(c, "getBackground"), "declares getBackground");
        check(declares(c, "setBackground"), "declares setBackground");
        check(declares(c, "getBackgroundAttachment"), "declares getBackgroundAttachment");
        check(declares(c, "setBackgroundAttachment"), "declares setBackgroundAttachment");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: CSS2Properties shape-lock (whole generated accessor family) ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("CSS", "2.0") && impl.getFeature("CSS", "2.0") == null, "CSS module unimplemented: hasFeature false, getFeature null");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.css.CSS2Properties.class.getDeclaredMethods()) ms.add(m.getName());
        check(ms.size() >= 200, "approximately 240 get/set accessors declared");
        for (String n : new String[]{"getAzimuth", "setAzimuth", "getColor", "setColor", "getMargin", "setMargin", "getZIndex", "setZIndex", "getFontSize", "setBackgroundColor"})
            check(ms.contains(n), n + " declared");
        check(org.w3c.dom.css.CSS2Properties.class.isInterface(), "CSS2Properties is an interface");
        boolean paired = true;
        for (String p : new String[]{"Background", "Border", "Font", "Padding", "Position"})
            paired = paired && ms.contains("get" + p) == ms.contains("set" + p) || !ms.contains("get" + p);
        check(paired, "accessors come in get/set pairs");
    }
}

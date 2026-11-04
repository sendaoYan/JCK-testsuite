/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.BinderImpl conformance: class hierarchy and members.
 * @run main BinderImplTest
 */

public class BinderImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.BinderImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.BinderImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("javax.xml.bind.Binder".equals(c.getSuperclass().getName()), "extends javax.xml.bind.Binder");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("associativeUnmarshal"), "has method associativeUnmarshal");
        check(mn.contains("createOutput"), "has method createOutput");
        check(mn.contains("excludeProperty"), "has method excludeProperty");
        check(mn.contains("getEventHandler"), "has method getEventHandler");
        check(mn.contains("getJAXBNode"), "has method getJAXBNode");
        check(mn.contains("getMarshaller"), "has method getMarshaller");
        check(mn.contains("getProperty"), "has method getProperty");
        check(mn.contains("getSchema"), "has method getSchema");
        check(mn.contains("getUnmarshaller"), "has method getUnmarshaller");
        check(mn.contains("getXMLNode"), "has method getXMLNode");
        check(mn.contains("marshal"), "has method marshal");
        check(mn.contains("setEventHandler"), "has method setEventHandler");
        check(mn.contains("setProperty"), "has method setProperty");
        check(mn.contains("setSchema"), "has method setSchema");
        check(mn.contains("unmarshal"), "has method unmarshal");
        check(mn.contains("updateJAXB"), "has method updateJAXB");
        check(mn.contains("updateXML"), "has method updateXML");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

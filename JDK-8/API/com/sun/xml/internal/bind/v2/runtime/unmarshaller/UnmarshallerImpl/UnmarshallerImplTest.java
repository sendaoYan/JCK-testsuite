/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.unmarshaller.UnmarshallerImpl conformance: class hierarchy and members.
 * @run main UnmarshallerImplTest
 */

public class UnmarshallerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.unmarshaller.UnmarshallerImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.unmarshaller.UnmarshallerImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("javax.xml.bind.helpers.AbstractUnmarshallerImpl".equals(c.getSuperclass().getName()), "extends javax.xml.bind.helpers.AbstractUnmarshallerImpl");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 2, "direct interface count");
        check(ifs.contains("java.io.Closeable"), "has interface java.io.Closeable");
        check(ifs.contains("javax.xml.bind.ValidationEventHandler"), "has interface javax.xml.bind.ValidationEventHandler");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("close"), "has method close");
        check(mn.contains("createUnmarshalException"), "has method createUnmarshalException");
        check(mn.contains("createUnmarshallerHandler"), "has method createUnmarshallerHandler");
        check(mn.contains("finalize"), "has method finalize");
        check(mn.contains("getAdapter"), "has method getAdapter");
        check(mn.contains("getAttachmentUnmarshaller"), "has method getAttachmentUnmarshaller");
        check(mn.contains("getBeanInfo"), "has method getBeanInfo");
        check(mn.contains("getContext"), "has method getContext");
        check(mn.contains("getEventHandler"), "has method getEventHandler");
        check(mn.contains("getListener"), "has method getListener");
        check(mn.contains("getProperty"), "has method getProperty");
        check(mn.contains("getSchema"), "has method getSchema");
        check(mn.contains("getUnmarshallerHandler"), "has method getUnmarshallerHandler");
        check(mn.contains("getXMLReader"), "has method getXMLReader");
        check(mn.contains("handleEvent"), "has method handleEvent");
        check(mn.contains("handleStreamException"), "has method handleStreamException");
        check(mn.contains("hasEventHandler"), "has method hasEventHandler");
        check(mn.contains("isValidating"), "has method isValidating");
        check(mn.contains("needsInterning"), "has method needsInterning");
        check(mn.contains("setAdapter"), "has method setAdapter");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.MarshallerImpl conformance: class hierarchy and members.
 * @run main MarshallerImplTest
 */

public class MarshallerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.MarshallerImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.MarshallerImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("javax.xml.bind.helpers.AbstractMarshallerImpl".equals(c.getSuperclass().getName()), "extends javax.xml.bind.helpers.AbstractMarshallerImpl");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("javax.xml.bind.ValidationEventHandler"), "has interface javax.xml.bind.ValidationEventHandler");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("checkBoolean"), "has method checkBoolean");
        check(mn.contains("checkString"), "has method checkString");
        check(mn.contains("cleanUp"), "has method cleanUp");
        check(mn.contains("createEscapeHandler"), "has method createEscapeHandler");
        check(mn.contains("createPostInitAction"), "has method createPostInitAction");
        check(mn.contains("createWriter"), "has method createWriter");
        check(mn.contains("createXmlOutput"), "has method createXmlOutput");
        check(mn.contains("getAdapter"), "has method getAdapter");
        check(mn.contains("getAttachmentMarshaller"), "has method getAttachmentMarshaller");
        check(mn.contains("getContext"), "has method getContext");
        check(mn.contains("getEscapeHandler"), "has method getEscapeHandler");
        check(mn.contains("getListener"), "has method getListener");
        check(mn.contains("getProperty"), "has method getProperty");
        check(mn.contains("getSchema"), "has method getSchema");
        check(mn.contains("handleEvent"), "has method handleEvent");
        check(mn.contains("marshal"), "has method marshal");
        check(mn.contains("postwrite"), "has method postwrite");
        check(mn.contains("prewrite"), "has method prewrite");
        check(mn.contains("setAdapter"), "has method setAdapter");
        check(mn.contains("setAttachmentMarshaller"), "has method setAttachmentMarshaller");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

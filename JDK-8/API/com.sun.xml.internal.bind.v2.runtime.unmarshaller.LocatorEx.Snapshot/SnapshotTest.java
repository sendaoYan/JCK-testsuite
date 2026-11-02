/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.unmarshaller.LocatorEx.Snapshot conformance: class hierarchy and members.
 * @run main SnapshotTest
 */

public class SnapshotTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.unmarshaller.LocatorEx$Snapshot", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.unmarshaller.LocatorEx$Snapshot"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 2, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.bind.v2.runtime.unmarshaller.LocatorEx"), "has interface com.sun.xml.internal.bind.v2.runtime.unmarshaller.LocatorEx");
        check(ifs.contains("javax.xml.bind.ValidationEventLocator"), "has interface javax.xml.bind.ValidationEventLocator");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getColumnNumber"), "has method getColumnNumber");
        check(mn.contains("getLineNumber"), "has method getLineNumber");
        check(mn.contains("getLocation"), "has method getLocation");
        check(mn.contains("getNode"), "has method getNode");
        check(mn.contains("getObject"), "has method getObject");
        check(mn.contains("getOffset"), "has method getOffset");
        check(mn.contains("getPublicId"), "has method getPublicId");
        check(mn.contains("getSystemId"), "has method getSystemId");
        check(mn.contains("getURL"), "has method getURL");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.soap.name.NameImpl conformance: class hierarchy and members.
 * @run main NameImplTest
 */

public class NameImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.soap.name.NameImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.soap.name.NameImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("javax.xml.soap.Name"), "has interface javax.xml.soap.Name");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("convertToName"), "has method convertToName");
        check(mn.contains("convertToQName"), "has method convertToQName");
        check(mn.contains("copyElementName"), "has method copyElementName");
        check(mn.contains("create"), "has method create");
        check(mn.contains("createBody1_1Name"), "has method createBody1_1Name");
        check(mn.contains("createBody1_2Name"), "has method createBody1_2Name");
        check(mn.contains("createCodeSubcode1_2Name"), "has method createCodeSubcode1_2Name");
        check(mn.contains("createDetail1_1Name"), "has method createDetail1_1Name");
        check(mn.contains("createEnvelope1_1Name"), "has method createEnvelope1_1Name");
        check(mn.contains("createEnvelope1_2Name"), "has method createEnvelope1_2Name");
        check(mn.contains("createFault1_1Name"), "has method createFault1_1Name");
        check(mn.contains("createFault1_2Name"), "has method createFault1_2Name");
        check(mn.contains("createFaultElement1_1Name"), "has method createFaultElement1_1Name");
        check(mn.contains("createFromQualifiedName"), "has method createFromQualifiedName");
        check(mn.contains("createFromTagAndUri"), "has method createFromTagAndUri");
        check(mn.contains("createFromTagName"), "has method createFromTagName");
        check(mn.contains("createFromUnqualifiedName"), "has method createFromUnqualifiedName");
        check(mn.contains("createHeader1_1Name"), "has method createHeader1_1Name");
        check(mn.contains("createHeader1_2Name"), "has method createHeader1_2Name");
        check(mn.contains("createNotUnderstood1_2Name"), "has method createNotUnderstood1_2Name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

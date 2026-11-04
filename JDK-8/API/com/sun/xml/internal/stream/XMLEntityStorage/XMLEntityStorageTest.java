/*
 * @test
 * @summary com.sun.xml.internal.stream.XMLEntityStorage conformance: class hierarchy and members.
 * @run main XMLEntityStorageTest
 */

public class XMLEntityStorageTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.XMLEntityStorage", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.XMLEntityStorage"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addExternalEntity"), "has method addExternalEntity");
        check(mn.contains("addInternalEntity"), "has method addInternalEntity");
        check(mn.contains("addUnparsedEntity"), "has method addUnparsedEntity");
        check(mn.contains("endExternalSubset"), "has method endExternalSubset");
        check(mn.contains("expandSystemId"), "has method expandSystemId");
        check(mn.contains("fixURI"), "has method fixURI");
        check(mn.contains("getEntity"), "has method getEntity");
        check(mn.contains("getEntityKeys"), "has method getEntityKeys");
        check(mn.contains("getEntitySize"), "has method getEntitySize");
        check(mn.contains("getUserDir"), "has method getUserDir");
        check(mn.contains("hasEntities"), "has method hasEntities");
        check(mn.contains("isDeclaredEntity"), "has method isDeclaredEntity");
        check(mn.contains("isEntityDeclInExternalSubset"), "has method isEntityDeclInExternalSubset");
        check(mn.contains("isExternalEntity"), "has method isExternalEntity");
        check(mn.contains("isUnparsedEntity"), "has method isUnparsedEntity");
        check(mn.contains("reset"), "has method reset");
        check(mn.contains("startExternalSubset"), "has method startExternalSubset");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

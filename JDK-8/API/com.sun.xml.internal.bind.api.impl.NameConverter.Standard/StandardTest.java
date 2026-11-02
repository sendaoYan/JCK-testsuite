/*
 * @test
 * @summary com.sun.xml.internal.bind.api.impl.NameConverter.Standard conformance: class hierarchy and members.
 * @run main StandardTest
 */

public class StandardTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.api.impl.NameConverter$Standard", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.api.impl.NameConverter$Standard"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.bind.api.impl.NameUtil".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.bind.api.impl.NameUtil");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.bind.api.impl.NameConverter"), "has interface com.sun.xml.internal.bind.api.impl.NameConverter");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("capitalize"), "has method capitalize");
        check(mn.contains("combine"), "has method combine");
        check(mn.contains("removeIllegalIdentifierChars"), "has method removeIllegalIdentifierChars");
        check(mn.contains("reverse"), "has method reverse");
        check(mn.contains("toClassName"), "has method toClassName");
        check(mn.contains("toConstantName"), "has method toConstantName");
        check(mn.contains("toInterfaceName"), "has method toInterfaceName");
        check(mn.contains("toPackageName"), "has method toPackageName");
        check(mn.contains("toPropertyName"), "has method toPropertyName");
        check(mn.contains("toVariableName"), "has method toVariableName");
        check(mn.contains("toWordList"), "has method toWordList");
        check(mn.contains("tokenize"), "has method tokenize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

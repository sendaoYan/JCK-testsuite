/*
 * @test
 * @summary com.sun.xml.internal.stream.buffer.AbstractCreator conformance: class hierarchy and members.
 * @run main AbstractCreatorTest
 */

public class AbstractCreatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.buffer.AbstractCreator", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.buffer.AbstractCreator"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.stream.buffer.AbstractCreatorProcessor".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.stream.buffer.AbstractCreatorProcessor");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createBuffer"), "has method createBuffer");
        check(mn.contains("getXMLStreamBuffer"), "has method getXMLStreamBuffer");
        check(mn.contains("increaseTreeCount"), "has method increaseTreeCount");
        check(mn.contains("peekAtContentObject"), "has method peekAtContentObject");
        check(mn.contains("resizeContentCharacters"), "has method resizeContentCharacters");
        check(mn.contains("resizeContentObjects"), "has method resizeContentObjects");
        check(mn.contains("resizeStructure"), "has method resizeStructure");
        check(mn.contains("resizeStructureStrings"), "has method resizeStructureStrings");
        check(mn.contains("setBuffer"), "has method setBuffer");
        check(mn.contains("setHasInternedStrings"), "has method setHasInternedStrings");
        check(mn.contains("setXMLStreamBuffer"), "has method setXMLStreamBuffer");
        check(mn.contains("storeContentCharacters"), "has method storeContentCharacters");
        check(mn.contains("storeContentCharactersCopy"), "has method storeContentCharactersCopy");
        check(mn.contains("storeContentObject"), "has method storeContentObject");
        check(mn.contains("storeContentString"), "has method storeContentString");
        check(mn.contains("storeStructure"), "has method storeStructure");
        check(mn.contains("storeStructureString"), "has method storeStructureString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary com.sun.xml.internal.stream.buffer.AbstractProcessor conformance: abstract class hierarchy and members.
 * @run main AbstractProcessorTest
 */

public class AbstractProcessorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.buffer.AbstractProcessor", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.buffer.AbstractProcessor"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check("com.sun.xml.internal.stream.buffer.AbstractCreatorProcessor".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.stream.buffer.AbstractCreatorProcessor");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getAIIState"), "has method getAIIState");
        check(mn.contains("getEIIState"), "has method getEIIState");
        check(mn.contains("getNIIState"), "has method getNIIState");
        check(mn.contains("getPrefixFromQName"), "has method getPrefixFromQName");
        check(mn.contains("getQName"), "has method getQName");
        check(mn.contains("peekStructure"), "has method peekStructure");
        check(mn.contains("readContentCharactersBuffer"), "has method readContentCharactersBuffer");
        check(mn.contains("readContentCharactersCopy"), "has method readContentCharactersCopy");
        check(mn.contains("readContentObject"), "has method readContentObject");
        check(mn.contains("readContentString"), "has method readContentString");
        check(mn.contains("readEiiState"), "has method readEiiState");
        check(mn.contains("readFromNextStructure"), "has method readFromNextStructure");
        check(mn.contains("readStructure"), "has method readStructure");
        check(mn.contains("readStructure16"), "has method readStructure16");
        check(mn.contains("readStructureString"), "has method readStructureString");
        check(mn.contains("setBuffer"), "has method setBuffer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

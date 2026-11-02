/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.vocab.Vocabulary conformance: abstract class hierarchy and members.
 * @run main VocabularyTest
 */

public class VocabularyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.vocab.Vocabulary", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.vocab.Vocabulary"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getExternalVocabularyURI"), "has method getExternalVocabularyURI");
        check(mn.contains("hasExternalVocabulary"), "has method hasExternalVocabulary");
        check(mn.contains("hasInitialVocabulary"), "has method hasInitialVocabulary");
        check(mn.contains("setExternalVocabularyURI"), "has method setExternalVocabularyURI");
        check(mn.contains("setInitialReadOnlyVocabulary"), "has method setInitialReadOnlyVocabulary");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

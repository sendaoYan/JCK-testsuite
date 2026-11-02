/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.vocab.ParserVocabulary conformance: class hierarchy and members.
 * @run main ParserVocabularyTest
 */

public class ParserVocabularyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.vocab.ParserVocabulary", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.vocab.ParserVocabulary"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.fastinfoset.vocab.Vocabulary".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.fastinfoset.vocab.Vocabulary");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addToNameTable"), "has method addToNameTable");
        check(mn.contains("addToTable"), "has method addToTable");
        check(mn.contains("clear"), "has method clear");
        check(mn.contains("convertVocabulary"), "has method convertVocabulary");
        check(mn.contains("getIntegerValueFromProperty"), "has method getIntegerValueFromProperty");
        check(mn.contains("setInitialVocabulary"), "has method setInitialVocabulary");
        check(mn.contains("setReadOnlyVocabulary"), "has method setReadOnlyVocabulary");
        check(mn.contains("setReferencedVocabulary"), "has method setReferencedVocabulary");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

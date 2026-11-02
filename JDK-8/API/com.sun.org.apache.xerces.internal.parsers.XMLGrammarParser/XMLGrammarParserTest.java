/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.parsers.XMLGrammarParser conformance: abstract class hierarchy and members.
 * @run main XMLGrammarParserTest
 */
public class XMLGrammarParserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.parsers.XMLGrammarParser", false, XMLGrammarParserTest.class.getClassLoader());
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.parsers.XMLParser"), "superclass com.sun.org.apache.xerces.internal.parsers.XMLParser");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary org.xml.sax.helpers.ParserFactory conformance: static makeParser factory.
 * @run main ParserFactoryTest
 */


public class ParserFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        java.lang.reflect.Method m = org.xml.sax.helpers.ParserFactory.class.getMethod("makeParser");
        check(m != null && java.lang.reflect.Modifier.isStatic(m.getModifiers()), "static makeParser");
        check(org.xml.sax.Parser.class.isAssignableFrom(m.getReturnType()), "returns Parser");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

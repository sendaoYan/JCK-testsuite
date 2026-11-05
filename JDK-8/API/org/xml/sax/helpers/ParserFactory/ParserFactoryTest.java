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
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral ParserFactory contract (deprecated SAX1 bootstrap) ----
    static void deepContract() throws Exception {
        boolean deprecated = org.xml.sax.helpers.ParserFactory.class.getAnnotation(Deprecated.class) != null;
        boolean is8 = System.getProperty("java.version").startsWith("1.8");
        check(is8 ? !deprecated : deprecated, "ParserFactory deprecation annotation differs by JDK (8: javadoc-only, 27+: @Deprecated present; observed=" + deprecated + ")");
        boolean npe = false;
        String msg = null;
        try { org.xml.sax.helpers.ParserFactory.makeParser(); }
        catch (NullPointerException e) { npe = true; msg = e.getMessage(); }
        check(npe && msg != null && msg.contains("sax.parser"), "makeParser() without org.xml.sax.parser -> NPE mentioning sax.parser (msg=" + msg + ")");
        boolean cnf = false;
        try { org.xml.sax.helpers.ParserFactory.makeParser("no.such.Class"); }
        catch (ClassNotFoundException e) { cnf = true; }
        check(cnf, "makeParser(unknown class) -> ClassNotFoundException");
    }
}

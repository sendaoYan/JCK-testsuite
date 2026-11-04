/*
 * @test
 * @summary jdk.javadoc.internal.html.HtmlTag conformance: internal enum reflection.
 * @modules jdk.javadoc/jdk.javadoc.internal.html:+open
 * @run main HtmlTagTest
 */
public class HtmlTagTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.javadoc.internal.html.HtmlTag", false, HtmlTagTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == true, "isEnum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(c.getDeclaredMethods().length == 10, "10 declared methods");
        check(c.getDeclaredFields().length == 99, "99 declared fields");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { if (f.isEnumConstant()) ec.add(f.getName()); }
        check(ec.size() == 92, "92 enum constants");
        check(ec.contains("A"), "enum constant A");
        check(ec.contains("ABBR"), "enum constant ABBR");
        check(ec.contains("ACRONYM"), "enum constant ACRONYM");
        check(ec.contains("ADDRESS"), "enum constant ADDRESS");
        check(ec.contains("ARTICLE"), "enum constant ARTICLE");
        check(ec.contains("ASIDE"), "enum constant ASIDE");
        check(ec.contains("B"), "enum constant B");
        check(ec.contains("BDI"), "enum constant BDI");
        check(ec.contains("BIG"), "enum constant BIG");
        check(ec.contains("BLOCKQUOTE"), "enum constant BLOCKQUOTE");
        check(ec.contains("BODY"), "enum constant BODY");
        check(ec.contains("BR"), "enum constant BR");
        check(ec.contains("BUTTON"), "enum constant BUTTON");
        check(ec.contains("CAPTION"), "enum constant CAPTION");
        check(ec.contains("CENTER"), "enum constant CENTER");
        check(ec.contains("CITE"), "enum constant CITE");
        check(ec.contains("CODE"), "enum constant CODE");
        check(ec.contains("COL"), "enum constant COL");
        check(ec.contains("COLGROUP"), "enum constant COLGROUP");
        check(ec.contains("DD"), "enum constant DD");
        check(ec.contains("DEL"), "enum constant DEL");
        check(ec.contains("DETAILS"), "enum constant DETAILS");
        check(ec.contains("DFN"), "enum constant DFN");
        check(ec.contains("DIV"), "enum constant DIV");
        check(ec.contains("DL"), "enum constant DL");
        check(ec.contains("DT"), "enum constant DT");
        check(ec.contains("EM"), "enum constant EM");
        check(ec.contains("FIGCAPTION"), "enum constant FIGCAPTION");
        check(ec.contains("FIGURE"), "enum constant FIGURE");
        check(ec.contains("FONT"), "enum constant FONT");
        check(ec.contains("FOOTER"), "enum constant FOOTER");
        check(ec.contains("FRAME"), "enum constant FRAME");
        check(ec.contains("FRAMESET"), "enum constant FRAMESET");
        check(ec.contains("H1"), "enum constant H1");
        check(ec.contains("H2"), "enum constant H2");
        check(ec.contains("H3"), "enum constant H3");
        check(ec.contains("H4"), "enum constant H4");
        check(ec.contains("H5"), "enum constant H5");
        check(ec.contains("H6"), "enum constant H6");
        check(ec.contains("HEAD"), "enum constant HEAD");
        check(ec.contains("HEADER"), "enum constant HEADER");
        check(ec.contains("HR"), "enum constant HR");
        check(ec.contains("HTML"), "enum constant HTML");
        check(ec.contains("I"), "enum constant I");
        check(ec.contains("IFRAME"), "enum constant IFRAME");
        check(ec.contains("IMG"), "enum constant IMG");
        check(ec.contains("INPUT"), "enum constant INPUT");
        check(ec.contains("INS"), "enum constant INS");
        check(ec.contains("KBD"), "enum constant KBD");
        check(ec.contains("LABEL"), "enum constant LABEL");
        check(ec.contains("LI"), "enum constant LI");
        check(ec.contains("LINK"), "enum constant LINK");
        check(ec.contains("MAIN"), "enum constant MAIN");
        check(ec.contains("MARK"), "enum constant MARK");
        check(ec.contains("MENU"), "enum constant MENU");
        check(ec.contains("META"), "enum constant META");
        check(ec.contains("NAV"), "enum constant NAV");
        check(ec.contains("NOFRAMES"), "enum constant NOFRAMES");
        check(ec.contains("NOSCRIPT"), "enum constant NOSCRIPT");
        check(ec.contains("OL"), "enum constant OL");
        check(ec.contains("OPTION"), "enum constant OPTION");
        check(ec.contains("P"), "enum constant P");
        check(ec.contains("PRE"), "enum constant PRE");
        check(ec.contains("Q"), "enum constant Q");
        check(ec.contains("S"), "enum constant S");
        check(ec.contains("SAMP"), "enum constant SAMP");
        check(ec.contains("SCRIPT"), "enum constant SCRIPT");
        check(ec.contains("SECTION"), "enum constant SECTION");
        check(ec.contains("SELECT"), "enum constant SELECT");
        check(ec.contains("SMALL"), "enum constant SMALL");
        check(ec.contains("SPAN"), "enum constant SPAN");
        check(ec.contains("STRIKE"), "enum constant STRIKE");
        check(ec.contains("STRONG"), "enum constant STRONG");
        check(ec.contains("STYLE"), "enum constant STYLE");
        check(ec.contains("SUB"), "enum constant SUB");
        check(ec.contains("SUMMARY"), "enum constant SUMMARY");
        check(ec.contains("SUP"), "enum constant SUP");
        check(ec.contains("TABLE"), "enum constant TABLE");
        check(ec.contains("TBODY"), "enum constant TBODY");
        check(ec.contains("TD"), "enum constant TD");
        check(ec.contains("TEMPLATE"), "enum constant TEMPLATE");
        check(ec.contains("TFOOT"), "enum constant TFOOT");
        check(ec.contains("TH"), "enum constant TH");
        check(ec.contains("THEAD"), "enum constant THEAD");
        check(ec.contains("TIME"), "enum constant TIME");
        check(ec.contains("TITLE"), "enum constant TITLE");
        check(ec.contains("TR"), "enum constant TR");
        check(ec.contains("TT"), "enum constant TT");
        check(ec.contains("U"), "enum constant U");
        check(ec.contains("UL"), "enum constant UL");
        check(ec.contains("VAR"), "enum constant VAR");
        check(ec.contains("WBR"), "enum constant WBR");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

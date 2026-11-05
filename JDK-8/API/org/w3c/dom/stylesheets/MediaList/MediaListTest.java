/*
 * @test
 * @summary org.w3c.dom.stylesheets.MediaList conformance: reflection shape checks.
 * @run main MediaListTest
 */

import java.lang.reflect.*;

public class MediaListTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.w3c.dom.stylesheets.MediaList");
        check(c != null, "org.w3c.dom.stylesheets.MediaList loads");
        check(c.getName().equals("org.w3c.dom.stylesheets.MediaList"), "binary name is org.w3c.dom.stylesheets.MediaList");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.stylesheets.MediaList is public");
        check(c.isInterface(), "org.w3c.dom.stylesheets.MediaList is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getMediaText"), "declares getMediaText");
        check(declares(c, "setMediaText"), "declares setMediaText");
        check(declares(c, "getLength"), "declares getLength");
        check(declares(c, "item"), "declares item");
        check(declares(c, "deleteMedium"), "declares deleteMedium");
        check(declares(c, "appendMedium"), "declares appendMedium");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: MediaList contract (behavioral mini-list) ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("StyleSheets", "2.0") && impl.getFeature("StyleSheets", "2.0") == null, "StyleSheets module unimplemented");
        final java.util.List<String> media = new java.util.ArrayList<String>(java.util.Arrays.asList("screen", "print"));
        org.w3c.dom.stylesheets.MediaList ml = new org.w3c.dom.stylesheets.MediaList() {
            public String getMediaText() { return media.isEmpty() ? "" : media.toString().replace("[", "").replace("]", "").replace(", ", ","); }
            public void setMediaText(String t) { media.clear(); media.addAll(java.util.Arrays.asList(t.split(","))); }
            public int getLength() { return media.size(); }
            public String item(int i) { return i >= 0 && i < media.size() ? media.get(i) : null; }
            public void deleteMedium(String m) { media.remove(m); }
            public void appendMedium(String m) { media.add(m); }
        };
        check(ml.getLength() == 2 && "screen".equals(ml.item(0)) && "print".equals(ml.item(1)), "initial entries in order");
        ml.appendMedium("handheld");
        check(ml.getLength() == 3 && "handheld".equals(ml.item(2)), "appendMedium appends at the end");
        ml.deleteMedium("screen");
        check(ml.getLength() == 2 && "print".equals(ml.item(0)), "deleteMedium removes by value");
        check(ml.item(-1) == null && ml.item(9) == null, "out-of-range item -> null");
        check(ml.getMediaText().contains("print"), "media text joins the entries");
        ml.setMediaText("all");
        check(ml.getLength() == 1 && "all".equals(ml.item(0)), "setMediaText replaces the list");
    }
}

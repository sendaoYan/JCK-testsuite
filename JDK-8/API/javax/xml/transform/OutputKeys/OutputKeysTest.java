/*
 * @test
 * @summary javax.xml.transform.OutputKeys conformance: output keys constants.
 * @run main OutputKeysTest
 */


public class OutputKeysTest {
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
        check("method".equals(javax.xml.transform.OutputKeys.METHOD), "METHOD");
        check("version".equals(javax.xml.transform.OutputKeys.VERSION), "VERSION");
        check("encoding".equals(javax.xml.transform.OutputKeys.ENCODING), "ENCODING");
        check("omit-xml-declaration".equals(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION), "OMIT_XML_DECLARATION");
        check("indent".equals(javax.xml.transform.OutputKeys.INDENT), "INDENT");
        testRemainingConstants();
        testClassSurface();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // deepening: remaining constants + class surface
    static void testRemainingConstants() {
        check("standalone".equals(javax.xml.transform.OutputKeys.STANDALONE), "STANDALONE");
        check("cdata-section-elements".equals(javax.xml.transform.OutputKeys.CDATA_SECTION_ELEMENTS),
              "CDATA_SECTION_ELEMENTS");
        check("media-type".equals(javax.xml.transform.OutputKeys.MEDIA_TYPE), "MEDIA_TYPE");
        check("doctype-public".equals(javax.xml.transform.OutputKeys.DOCTYPE_PUBLIC), "DOCTYPE_PUBLIC");
        check("doctype-system".equals(javax.xml.transform.OutputKeys.DOCTYPE_SYSTEM), "DOCTYPE_SYSTEM");
        String[] all = {
            javax.xml.transform.OutputKeys.METHOD,
            javax.xml.transform.OutputKeys.VERSION,
            javax.xml.transform.OutputKeys.ENCODING,
            javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION,
            javax.xml.transform.OutputKeys.INDENT,
            javax.xml.transform.OutputKeys.STANDALONE,
            javax.xml.transform.OutputKeys.CDATA_SECTION_ELEMENTS,
            javax.xml.transform.OutputKeys.MEDIA_TYPE,
            javax.xml.transform.OutputKeys.DOCTYPE_PUBLIC,
            javax.xml.transform.OutputKeys.DOCTYPE_SYSTEM,
        };
        boolean allStrings = true;
        java.util.Set<String> seen = new java.util.HashSet<String>();
        for (String s : all) {
            if (s == null || s.length() == 0) allStrings = false;
            if (!seen.add(s)) allStrings = false; // duplicates would be a spec violation
        }
        check(allStrings, "all 10 OutputKeys constants non-empty and pairwise distinct");
    }

    static void testClassSurface() throws Exception {
        Class<?> c = Class.forName("javax.xml.transform.OutputKeys");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "OutputKeys public");
        check(!java.lang.reflect.Modifier.isFinal(c.getModifiers()), "OutputKeys not final (spec form)");
        boolean privateCtor = true;
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            if (!java.lang.reflect.Modifier.isPrivate(k.getModifiers())) privateCtor = false;
        }
        check(privateCtor, "OutputKeys constructor(s) private (constant-holder class)");
    }
}

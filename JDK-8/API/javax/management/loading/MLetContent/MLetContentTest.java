/*
 * @test
 * @summary javax.management.loading.MLetContent conformance: concrete class reflection.
 * @run main MLetContentTest
 */

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.loading.MLetContent;

public class MLetContentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.loading.MLetContent");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(hasMethod(c, "getAttributes"), "declares getAttributes");
        check(hasMethod(c, "getCode"), "declares getCode");
        check(hasMethod(c, "getCodeBase"), "declares getCodeBase");
        // ---- deepened: constructor + full getter round-trip ----
        testConstructorAndGetters();
        // ---- deepened: codebase resolution matrix ----
        testCodeBaseResolution();
        // ---- deepened: immutable views + key-casing quirk ----
        testImmutabilityAndKeyCasing();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened: constructor + full getter round-trip ----
    static void testConstructorAndGetters() throws Exception {
        // single public constructor: (URL, Map, List, List) as verified by javap
        Class<?> c = Class.forName("javax.management.loading.MLetContent");
        check(c.getConstructors().length == 1, "exactly one public constructor");
        Class<?>[] pt = c.getConstructors()[0].getParameterTypes();
        check(pt.length == 4, "constructor has 4 parameters");
        check(pt[0] == URL.class && pt[1] == Map.class && pt[2] == List.class && pt[3] == List.class,
                "constructor signature (URL, Map, List, List)");

        Map<String,String> attrs = new HashMap<String,String>();
        attrs.put("codebase", "cb");
        attrs.put("archive", "a.jar,b.jar");
        attrs.put("code", "com.example.Foo.class");
        attrs.put("object", "serFoo.ser");
        attrs.put("name", "FooMBean");
        attrs.put("version", "1.0");
        List<String> types = new ArrayList<String>(Arrays.asList("java.lang.String", "int"));
        List<String> values = new ArrayList<String>(Arrays.asList("x", "3"));
        URL doc = new URL("http://example.com/docs/mlet.html");
        MLetContent mc = new MLetContent(doc, attrs, types, values);

        // attribute-keyed getters map to lowercase MLET tag attributes
        check("a.jar,b.jar".equals(mc.getJarFiles()), "getJarFiles reads archive");
        check("com.example.Foo.class".equals(mc.getCode()), "getCode reads code");
        check("serFoo.ser".equals(mc.getSerializedObject()), "getSerializedObject reads object");
        check("FooMBean".equals(mc.getName()), "getName reads name");
        check("1.0".equals(mc.getVersion()), "getVersion reads version");
        // document base is the very URL passed in
        check(mc.getDocumentBase() == doc, "getDocumentBase is same URL instance");
        // param lists round-trip content and order
        check(mc.getParameterTypes().equals(types), "getParameterTypes content+order");
        check(mc.getParameterValues().equals(values), "getParameterValues content+order");
        check(mc.getParameterTypes().size() == 2, "parameter list size");
        // attributes map round-trips content
        check("a.jar,b.jar".equals(mc.getAttributes().get("archive")),
                "getAttributes carries original entries");
        check(mc.getAttributes().size() == 6, "getAttributes entry count");
    }

    // ---- deepened: codebase resolution matrix ----
    static void testCodeBaseResolution() throws Exception {
        URL doc = new URL("http://example.com/docs/mlet.html");
        List<String> empty = Collections.emptyList();

        // relative codebase without trailing '/': '/' appended, resolved against document
        Map<String,String> m1 = new HashMap<String,String>();
        m1.put("codebase", "cb");
        check(new MLetContent(doc, m1, empty, empty).getCodeBase()
                .equals(new URL("http://example.com/docs/cb/")),
                "relative codebase 'cb' -> doc-dir + cb/");

        // trailing '/' kept as-is
        Map<String,String> m2 = new HashMap<String,String>();
        m2.put("codebase", "cb/");
        check(new MLetContent(doc, m2, empty, empty).getCodeBase()
                .equals(new URL("http://example.com/docs/cb/")),
                "codebase 'cb/' unchanged resolution");

        // absolute codebase wins over document location
        Map<String,String> m3 = new HashMap<String,String>();
        m3.put("codebase", "http://other.com/x/");
        check(new MLetContent(doc, m3, empty, empty).getCodeBase()
                .equals(new URL("http://other.com/x/")),
                "absolute codebase used verbatim");

        // no codebase attribute: fall back to the document URL's directory
        Map<String,String> m4 = new HashMap<String,String>();
        check(new MLetContent(doc, m4, empty, empty).getCodeBase()
                .equals(new URL("http://example.com/docs/")),
                "missing codebase falls back to document directory");

        // file: document URL, missing codebase -> parent directory of the file
        // (trailing '/' preserved: substring up to and including the last '/')
        URL fdoc = new File("/aa/bb/mlet.txt").toURI().toURL();
        check(new MLetContent(fdoc, new HashMap<String,String>(), empty, empty).getCodeBase()
                .equals(new URL(fdoc, "/aa/bb/")),
                "file: document falls back to its directory");

        // root-level document "/mlet.txt": directory part is "/" -> root URL
        URL rootDoc = new URL("http://example.com/mlet.txt");
        check(new MLetContent(rootDoc, new HashMap<String,String>(), empty, empty).getCodeBase()
                .equals(new URL("http://example.com/")),
                "root-level document resolves to site root");
        // empty-path document: no '/' in file part -> the document URL itself
        URL bareDoc = new URL("http://example.com");
        check(new MLetContent(bareDoc, new HashMap<String,String>(), empty, empty).getCodeBase()
                .equals(new URL("http://example.com")),
                "empty-path document falls back to the document URL itself");
    }

    // ---- deepened: immutable views + key-casing quirk ----
    static void testImmutabilityAndKeyCasing() throws Exception {
        URL doc = new URL("http://example.com/docs/mlet.html");
        Map<String,String> attrs = new HashMap<String,String>();
        attrs.put("archive", "a.jar");
        List<String> types = new ArrayList<String>(Arrays.asList("int"));
        List<String> values = new ArrayList<String>(Arrays.asList("1"));
        MLetContent mc = new MLetContent(doc, attrs, types, values);

        // attributes/param lists are unmodifiable views
        try { mc.getAttributes().put("extra", "v"); check(false, "attributes put should fail"); }
        catch (UnsupportedOperationException e) { passed++; }
        try { mc.getParameterTypes().add("long"); check(false, "types add should fail"); }
        catch (UnsupportedOperationException e) { passed++; }
        try { mc.getParameterValues().remove(0); check(false, "values remove should fail"); }
        catch (UnsupportedOperationException e) { passed++; }
        // mutating the source collections afterwards does not disturb the content
        // already read (views wrap the ORIGINAL lists: same content reference)
        check(mc.getParameterTypes().equals(Arrays.asList("int")),
                "param types content stable");

        // key-casing quirk: getters look up lowercase keys only
        Map<String,String> upper = new HashMap<String,String>();
        upper.put("ARCHIVE", "a.jar");
        upper.put("Code", "Foo");
        MLetContent up = new MLetContent(doc, upper, types, values);
        check(up.getJarFiles() == null, "uppercase ARCHIVE not seen by getJarFiles");
        check(up.getCode() == null, "mixed-case Code not seen by getCode");
        // missing keys -> null (no exception)
        Map<String,String> none = new HashMap<String,String>();
        MLetContent empty = new MLetContent(doc, none, types, values);
        check(empty.getJarFiles() == null, "missing archive -> null");
        check(empty.getCode() == null, "missing code -> null");
        check(empty.getSerializedObject() == null, "missing object -> null");
        check(empty.getName() == null, "missing name -> null");
        check(empty.getVersion() == null, "missing version -> null");
    }
}


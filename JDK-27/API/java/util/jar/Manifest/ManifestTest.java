/*
 * @test
 * @summary java.util.jar.Manifest conformance: white-box structure + black-box behavior.
 * @run main ManifestTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class ManifestTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Exception; }

    static void expectThrows(Class<? extends Throwable> type, ThrowingRunnable action) {
        try {
            action.run();
            check(false, "expected " + type.getSimpleName());
        } catch (Throwable t) {
            check(type.isInstance(t),
                  "expected " + type.getSimpleName() + ", got " + t.getClass().getSimpleName());
        }
    }

    static boolean hasConstructor(Class<?> c, Class<?>... params) {
        try { c.getConstructor(params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.jar.Manifest");
        check(!c.isInterface() && java.lang.reflect.Modifier.isPublic(c.getModifiers()),
              "Manifest is public concrete class");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(hasConstructor(c), "Manifest()");
        check(hasConstructor(c, InputStream.class), "Manifest(InputStream)");
        check(hasConstructor(c, Manifest.class), "Manifest(Manifest)");
        check(hasMethod(c, "getMainAttributes"), "getMainAttributes()");
        check(hasMethod(c, "getEntries"), "getEntries()");
        check(hasMethod(c, "getAttributes", String.class), "getAttributes(String)");
        check(hasMethod(c, "clear"), "clear()");
        check(hasMethod(c, "write", java.io.OutputStream.class), "write(OutputStream)");
        check(hasMethod(c, "read", InputStream.class), "read(InputStream)");
        check(hasMethod(c, "equals", Object.class), "equals(Object)");
        check(hasMethod(c, "hashCode"), "hashCode()");
        check(hasMethod(c, "clone"), "clone()");
    }

    // ---- Part 2: black-box (behavioral) ----

    static void testEmpty() {
        Manifest m = new Manifest();
        check(m.getMainAttributes().isEmpty(), "empty main attributes");
        check(m.getEntries().isEmpty(), "no entries");
        check(m.getAttributes("anything") == null, "getAttributes unknown null");
        check(m.getAttributes(null) == null, "getAttributes(null) null");
    }

    static void testMainAndEntries() {
        Manifest m = new Manifest();
        Attributes main = m.getMainAttributes();
        main.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        main.put(Attributes.Name.MAIN_CLASS, "com.example.Main");
        check("1.0".equals(m.getMainAttributes().getValue(Attributes.Name.MANIFEST_VERSION)),
              "set/get main attribute");
        check("com.example.Main".equals(m.getMainAttributes().getValue("Main-Class")),
              "main attribute via string");

        Attributes sec = new Attributes();
        sec.put(Attributes.Name.SEALED, "true");
        sec.put(Attributes.Name.CONTENT_TYPE, "application/x");
        m.getEntries().put("com/example/", sec);
        check(m.getEntries().size() == 1, "one entry section");
        check(m.getAttributes("com/example/") == sec, "getAttributes returns stored section");
        check("true".equals(m.getAttributes("com/example/").getValue(Attributes.Name.SEALED)),
              "entry attribute value");
    }

    static void testWriteReadRoundTrip() throws Exception {
        Manifest m = new Manifest();
        m.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        m.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_TITLE, "My App");
        Attributes sec = new Attributes();
        sec.put(Attributes.Name.SEALED, "true");
        m.getEntries().put("pkg/", sec);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        m.write(bout);
        byte[] raw = bout.toByteArray();
        String text = new String(raw, StandardCharsets.UTF_8);
        check(text.contains("Manifest-Version: 1.0\r\n"), "write emits CRLF version line");
        check(text.contains("Name: pkg/\r\n"), "write emits Name section");

        Manifest m2 = new Manifest(new ByteArrayInputStream(raw));
        check("1.0".equals(m2.getMainAttributes().getValue(Attributes.Name.MANIFEST_VERSION)),
              "read back version");
        check("My App".equals(m2.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_TITLE)),
              "read back main attr");
        check("true".equals(m2.getAttributes("pkg/").getValue(Attributes.Name.SEALED)),
              "read back entry attr");
        check(m.equals(m2), "equals after round-trip");
        check(m.hashCode() == m2.hashCode(), "hashCode after round-trip");
    }

    static void testCopyAndClone() {
        Manifest m = new Manifest();
        m.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        Attributes sec = new Attributes();
        sec.put(Attributes.Name.CONTENT_TYPE, "text/plain");
        m.getEntries().put("a/", sec);

        Manifest copy = new Manifest(m);
        check(copy.equals(m) && copy.hashCode() == m.hashCode(), "copy equals original");
        copy.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "X");
        check(m.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS) == null,
              "copy main attributes independent");

        Manifest clone = (Manifest) m.clone();
        check(clone != m && clone.equals(m) && clone.hashCode() == m.hashCode(),
              "clone equals original and distinct");
        clone.clear();
        check(m.getEntries().size() == 1 && clone.getEntries().isEmpty(),
              "clone.clear independent of original");
    }

    static void testReadMerges() throws Exception {
        Manifest source = new Manifest();
        source.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        Attributes a = new Attributes();
        a.put(Attributes.Name.SEALED, "true");
        source.getEntries().put("a/", a);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        source.write(bout);

        Manifest target = new Manifest();
        Attributes b = new Attributes();
        b.put(Attributes.Name.CONTENT_TYPE, "text/plain");
        target.getEntries().put("b/", b);

        target.read(new ByteArrayInputStream(bout.toByteArray()));
        check("1.0".equals(target.getMainAttributes().getValue(Attributes.Name.MANIFEST_VERSION)),
              "read merges main attributes");
        check(target.getEntries().size() == 2, "read merges entries (2 total)");
        check("true".equals(target.getAttributes("a/").getValue(Attributes.Name.SEALED)),
              "read-added entry present");
        check("text/plain".equals(target.getAttributes("b/").getValue(Attributes.Name.CONTENT_TYPE)),
              "pre-existing entry preserved");
    }

    static void testMalformedInput() {
        final byte[] bad = "badheader\n".getBytes(StandardCharsets.UTF_8);
        expectThrows(java.io.IOException.class, () -> new Manifest(new ByteArrayInputStream(bad)));
    }

    static void testClearAndToString() {
        Manifest m = new Manifest();
        m.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        m.getEntries().put("x/", new Attributes());
        m.clear();
        check(m.getMainAttributes().isEmpty(), "clear main attributes");
        check(m.getEntries().isEmpty(), "clear entries");
        check(m.toString() != null, "toString non-null");
    }

    static void testBlackBox() throws Exception {
        testEmpty();
        testMainAndEntries();
        testWriteReadRoundTrip();
        testCopyAndClone();
        testReadMerges();
        testMalformedInput();
        testClearAndToString();
    }


    // ===================== 契约深化：72 字节续行/畸形消息/clone 深拷贝/write 幂等/字节级回写 =====================
    static void testContracts() throws Exception {
        // 72 字节折行续行（值以单空格开头的行拼接）
        StringBuilder sb = new StringBuilder("Manifest-Version: 1.0\r\n");
        StringBuilder longVal = new StringBuilder();
        for (int i = 0; i < 120; i++) longVal.append('v');
        sb.append("Long-Attr: ").append(longVal).append("\r\n");
        Manifest m = new Manifest(new java.io.ByteArrayInputStream(sb.toString().getBytes("UTF-8")));
        check(m.getMainAttributes().getValue("Long-Attr").length() == 120, "continuation joins to 120 chars");
        // 畸形（无冒号）-> IOException "invalid header field"（27 消息多 "(line 1)"，前缀一致）
        try {
            new Manifest(new java.io.ByteArrayInputStream("no-colon-line\r\n".getBytes("UTF-8")));
            check(false, "malformed manifest throws IOException");
        } catch (java.io.IOException e) {
            check(e.getMessage() != null && e.getMessage().startsWith("invalid header field"),
                  "malformed message prefix: " + e.getMessage());
        }
        // clone / 拷贝构造深拷贝
        Manifest orig = new Manifest();
        orig.getMainAttributes().putValue("Manifest-Version", "1.0");
        orig.getMainAttributes().putValue("X-Key", "1");
        Attributes sec = new Attributes();
        sec.putValue("Sealed", "true");
        orig.getEntries().put("pkg/", sec);
        Manifest cloned = (Manifest) orig.clone();
        check(cloned.equals(orig) && cloned.hashCode() == orig.hashCode(), "clone equals/hashCode");
        orig.getMainAttributes().putValue("X-Key", "2");
        check("1".equals(cloned.getMainAttributes().getValue("X-Key")), "clone main attrs independent");
        check(cloned.getEntries().get("pkg/") == orig.getEntries().get("pkg/"),
              "clone shares section Attributes objects (shallow) but maps distinct");
        check(cloned.getEntries() != orig.getEntries(), "clone entries map is distinct");
        Manifest copied = new Manifest(orig);
        check(copied.equals(orig), "copy ctor equals");
        orig.getEntries().remove("pkg/");
        check(copied.getEntries().containsKey("pkg/"), "copy ctor deep");
        // write -> read -> write 幂等（规范化输出字节一致）
        java.io.ByteArrayOutputStream w1 = new java.io.ByteArrayOutputStream();
        orig.write(w1);
        Manifest reread = new Manifest(new java.io.ByteArrayInputStream(w1.toByteArray()));
        java.io.ByteArrayOutputStream w2 = new java.io.ByteArrayOutputStream();
        reread.write(w2);
        check(java.util.Arrays.equals(w1.toByteArray(), w2.toByteArray()), "write idempotent bytes");
        check(reread.equals(orig), "round-trip equals");
        // read(null) NPE / Manifest(InputStream) null NPE
        try {
            new Manifest((java.io.InputStream) null);
            check(false, "Manifest(null) NPE");
        } catch (NullPointerException e) { check(true, "Manifest(null) NPE"); }
        Manifest m2 = new Manifest();
        try {
            m2.read(null);
            check(false, "read(null) NPE");
        } catch (NullPointerException e) { check(true, "read(null) NPE"); }
        // clear() 清空主属性与条目
        Manifest cl = new Manifest(orig);
        cl.clear();
        check(cl.getMainAttributes().isEmpty() && cl.getEntries().isEmpty(), "clear empties both");
        // write(null) NPE
        try {
            orig.write(null);
            check(false, "write(null) NPE");
        } catch (NullPointerException e) { check(true, "write(null) NPE"); }
        // equals 自反/与非 Manifest false
        check(orig.equals(orig), "equals reflexive");
        check(!orig.equals(new Object()), "equals non-Manifest false");
    }

    // ===================== 契约深化（第二层）：Attributes.Name 常量 / 大小写不敏感 / Attributes 集合视图与 clone =====================
    static void testAttributesContracts() {
        // Name 常量 toString 权威值
        check("Manifest-Version".equals(Attributes.Name.MANIFEST_VERSION.toString()), "MANIFEST_VERSION name");
        check("Main-Class".equals(Attributes.Name.MAIN_CLASS.toString()), "MAIN_CLASS name");
        check("Sealed".equals(Attributes.Name.SEALED.toString()), "SEALED name");
        check("Content-Type".equals(Attributes.Name.CONTENT_TYPE.toString()), "CONTENT_TYPE name");
        check("Class-Path".equals(Attributes.Name.CLASS_PATH.toString()), "CLASS_PATH name");
        check("Implementation-Title".equals(Attributes.Name.IMPLEMENTATION_TITLE.toString()), "IMPLEMENTATION_TITLE name");
        check("Specification-Version".equals(Attributes.Name.SPECIFICATION_VERSION.toString()), "SPECIFICATION_VERSION name");

        // 大小写不敏感查找：getValue(String) / get(Name) / containsKey / remove
        Attributes a = new Attributes();
        a.putValue("Manifest-Version", "1.0");
        a.putValue("Sealed", "true");
        check("1.0".equals(a.getValue("manifest-version")), "getValue case-insensitive");
        check("true".equals(a.getValue("SEALED")), "getValue upper case-insensitive");
        check("1.0".equals(a.get(new Attributes.Name("manifest-version"))), "get(Name) case-insensitive");
        check(a.containsKey(new Attributes.Name("sealed")), "containsKey case-insensitive");
        check("true".equals(a.remove(new Attributes.Name("sealed"))), "remove case-insensitive returns value");
        check(a.size() == 1, "size after case-insensitive remove");

        // 大小写不敏感 equals/hashCode（条数不同则不相等）
        Attributes b = new Attributes();
        b.putValue("manifest-VERSION", "1.0");
        b.putValue("sealed", "true");
        check(!a.equals(b), "size mismatch not equal");

        Attributes a2 = new Attributes();
        a2.putValue("manifest-VERSION", "1.0");
        check(a.equals(a2) && a.hashCode() == a2.hashCode(), "equals/hashCode case-insensitive");

        // Attributes.clone 独立但相等
        Attributes c = (Attributes) a.clone();
        check(c != a && c.equals(a) && c.hashCode() == a.hashCode(), "Attributes clone distinct/equal");

        // putValue/getValue round-trip、集合视图与 clear
        Attributes v = new Attributes();
        v.putValue("X-Key", "v1");
        check("v1".equals(v.getValue("x-key")), "putValue/getValue round-trip");
        check(v.size() == 1 && v.containsValue("v1"), "size/containsValue");
        check(v.entrySet().size() == 1 && v.values().size() == 1 && v.keySet().size() == 1,
              "entrySet/values/keySet sizes");
        v.clear();
        check(v.isEmpty(), "Attributes clear empties");
    }

    public static void main(String[] args) throws Exception {
        testContracts();
        testAttributesContracts();
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

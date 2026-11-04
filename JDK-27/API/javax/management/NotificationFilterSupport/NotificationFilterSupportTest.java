/*
 * @test
 * @summary javax.management.NotificationFilterSupport conformance: white-box structure + black-box behavior.
 * @run main NotificationFilterSupportTest
 */

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class NotificationFilterSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.NotificationFilterSupport");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(NotificationFilter.class.isAssignableFrom(c), "implements NotificationFilter");
        check(hasMethod(c, "enableType"), "has enableType");
        check(hasMethod(c, "disableType"), "has disableType");
        check(hasMethod(c, "disableAllTypes"), "has disableAllTypes");
        check(hasMethod(c, "getEnabledTypes"), "has getEnabledTypes");
        check(hasMethod(c, "isNotificationEnabled"), "has isNotificationEnabled");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        NotificationFilterSupport f = new NotificationFilterSupport();
        check(f.getEnabledTypes().isEmpty(), "starts empty");

        f.enableType("type.one");
        check(f.getEnabledTypes().size() == 1 && f.getEnabledTypes().contains("type.one"), "enableType tracks");
        check(f.isNotificationEnabled(new Notification("type.one", "s", 1L)), "matches enabled type");
        check(!f.isNotificationEnabled(new Notification("type.two", "s", 1L)), "does not match other type");

        f.enableType("type.two");
        check(f.isNotificationEnabled(new Notification("type.one", "s", 1L))
                && f.isNotificationEnabled(new Notification("type.two", "s", 1L)), "two types enabled");
        check(!f.isNotificationEnabled(new Notification("type.three", "s", 1L)), "third type disabled");

        // case-sensitive
        check(!f.isNotificationEnabled(new Notification("TYPE.ONE", "s", 1L)), "case-sensitive");

        f.disableType("type.one");
        check(!f.isNotificationEnabled(new Notification("type.one", "s", 1L)), "disabled type no longer matches");
        check(f.isNotificationEnabled(new Notification("type.two", "s", 1L)), "other type still enabled");

        f.disableAllTypes();
        check(f.getEnabledTypes().isEmpty(), "disableAllTypes clears");
        check(!f.isNotificationEnabled(new Notification("type.two", "s", 1L)), "nothing matches after clear");

        boolean threw = false;
        try { f.enableType(null); } catch (IllegalArgumentException ex) { threw = true; }
        check(threw, "enableType(null) throws IllegalArgumentException");
    }

    // ---- deepened (batch d): prefix semantics locked / live vector / null / serial form ----

    static void testPrefixSemanticsLocked() {
        // matching is String.startsWith on the enabled prefix (no wildcard, no suffix)
        NotificationFilterSupport f = new NotificationFilterSupport();
        f.enableType("jck.");
        check(f.isNotificationEnabled(new Notification("jck.x", "s", 1L)), "prefix 'jck.' matches 'jck.x'");
        check(f.isNotificationEnabled(new Notification("jck.", "s", 1L)), "prefix matches the exact type too");
        check(!f.isNotificationEnabled(new Notification("jck", "s", 1L)), "shorter type is not matched");
        check(!f.isNotificationEnabled(new Notification("other.jck.x", "s", 1L)),
                "no substring/suffix semantics");

        // '*' is a literal character, not a wildcard
        NotificationFilterSupport g = new NotificationFilterSupport();
        g.enableType("jck.*");
        check(!g.isNotificationEnabled(new Notification("jck.x", "s", 1L)), "'*' is literal (no wildcard)");
        check(g.isNotificationEnabled(new Notification("jck.*", "s", 1L)), "literal 'jck.*' matches itself");

        // empty prefix enables every non-null type
        NotificationFilterSupport e = new NotificationFilterSupport();
        e.enableType("");
        check(e.isNotificationEnabled(new Notification("anything", "s", 1L)),
                "empty prefix matches every type");
        check(!e.isNotificationEnabled(new Notification(null, "s", 1L)),
                "null type is always rejected");

        // enabling a longer prefix does not enable a shorter one
        NotificationFilterSupport h = new NotificationFilterSupport();
        h.enableType("jck.one.two");
        check(!h.isNotificationEnabled(new Notification("jck.one", "s", 1L)),
                "enabling a longer prefix does not match a shorter type");
        check(h.isNotificationEnabled(new Notification("jck.one.two.three", "s", 1L)),
                "longer type still matches the enabled prefix");
    }

    static void testNullNotificationNpe() {
        NotificationFilterSupport f = new NotificationFilterSupport();
        f.enableType("a");
        try {
            f.isNotificationEnabled(null);
            check(false, "isNotificationEnabled(null) must throw");
        } catch (NullPointerException ex) {
            check(true, "isNotificationEnabled(null) -> NPE (impl dereferences getType)");
        }
    }

    static void testGetEnabledTypesIsLive() {
        NotificationFilterSupport f = new NotificationFilterSupport();
        f.enableType("a");
        Vector<String> v = f.getEnabledTypes();
        check(v.size() == 1 && "a".equals(v.get(0)), "getEnabledTypes content");
        v.add("b");   // live Vector: the write reaches the filter's set
        check(f.getEnabledTypes().size() == 2, "vector write-through visible in getEnabledTypes");
        check(f.isNotificationEnabled(new Notification("b.c", "s", 1L)),
                "getEnabledTypes returns the live Vector (write-through to matching)");
    }

    static void testDisableTypeNoThrow() {
        NotificationFilterSupport f = new NotificationFilterSupport();
        f.disableType("never-enabled");
        check(f.getEnabledTypes().isEmpty(), "disableType of a never-enabled type is a no-op");
        f.disableType(null);
        check(f.getEnabledTypes().isEmpty(), "disableType(null) tolerated");
        f.enableType("a");
        f.disableType(null);
        check(f.getEnabledTypes().size() == 1, "disableType(null) does not disturb enabled types");
    }

    static void testSerializationDeep() throws Exception {
        NotificationFilterSupport f = new NotificationFilterSupport();
        f.enableType("jck.one");
        f.enableType("jck.two");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(f);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        NotificationFilterSupport back = (NotificationFilterSupport) ois.readObject();
        ois.close();
        check(back.getEnabledTypes().size() == 2, "round trip preserves enabled set size");
        check(back.getEnabledTypes().contains("jck.one") && back.getEnabledTypes().contains("jck.two"),
                "round trip preserves enabled set content");
        check(back.isNotificationEnabled(new Notification("jck.one.x", "s", 1L)),
                "round trip filter still matches enabled prefix");
        check(!back.isNotificationEnabled(new Notification("jck.three", "s", 1L)),
                "round trip filter still rejects other types");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testPrefixSemanticsLocked();
        testNullNotificationNpe();
        testGetEnabledTypesIsLive();
        testDisableTypeNoThrow();
        testSerializationDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

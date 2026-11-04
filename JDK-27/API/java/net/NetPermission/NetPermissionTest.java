/*
 * @test
 * @summary java.net.NetPermission conformance: name/actions.
 * @run main NetPermissionTest
 */

import java.net.NetPermission;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Enumeration;

public class NetPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- original white-box assertions (preserved verbatim) ----
    static void testOriginalWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.NetPermission");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(java.security.BasicPermission.class.isAssignableFrom(c), "extends BasicPermission");
        check(c.getConstructor(String.class) != null, "ctor(String)");
        check(c.getConstructor(String.class, String.class) != null, "ctor(String,String)");
    }

    // ---- original black-box assertions (preserved verbatim) ----
    static void testOriginalBlackBox() {
        NetPermission p = new NetPermission("setDefaultAuthenticator");
        check(p.getName().equals("setDefaultAuthenticator"), "name");
        check(p.getActions().equals(""), "empty actions default");

        NetPermission p2 = new NetPermission("allowHttpTrace", "perm");
        check(p2.getName().equals("allowHttpTrace"), "name with actions");
        check(p2.getActions().equals(""), "actions ignored (BasicPermission)");

        check(p.implies(new NetPermission("setDefaultAuthenticator")), "implies same name");
        check(!p.implies(new NetPermission("allowHttpTrace")), "not implies different name");

        check(p.equals(new NetPermission("setDefaultAuthenticator")), "equals");
        check(p.hashCode() == new NetPermission("setDefaultAuthenticator").hashCode(), "hashCode");

        try {
            new NetPermission("");
            check(false, "empty name must throw");
        } catch (IllegalArgumentException e) {
            passed++;
        }
        try {
            new NetPermission(null);
            check(false, "null name must throw");
        } catch (NullPointerException e) {
            passed++;
        }
    }

    // ---- white-box structure (javap -public, JDK-27) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.NetPermission");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(java.security.BasicPermission.class.isAssignableFrom(c), "extends BasicPermission");
        check(c.getSuperclass() == java.security.BasicPermission.class, "direct BasicPermission subclass");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(c.getConstructor(String.class) != null, "ctor(String)");
        check(c.getConstructor(String.class, String.class) != null, "ctor(String,String)");
        check(c.getMethod("getActions") != null, "getActions");
        check(c.getMethod("implies", Permission.class) != null, "implies");
        check(c.getMethod("newPermissionCollection") != null, "newPermissionCollection");
    }

    // ---- documented permission-name matrix (no whitelist: any name constructs) ----
    static void testDocumentedNames() {
        String[] names = {
            "setDefaultAuthenticator", "requestPasswordAuthentication", "specifyStreamHandler",
            "setProxySelector", "getProxySelector", "setCookieHandler", "getCookieHandler",
            "setResponseCache", "getResponseCache",
        };
        for (String n : names) {
            NetPermission p = new NetPermission(n);
            check(n.equals(p.getName()), "getName " + n);
            check("".equals(p.getActions()), "getActions empty for " + n);
        }
        // arbitrary names are accepted (NetPermission performs no whitelist check)
        String[] others = {"anyRandomName", "foo", "a.b.c", "set", "get*", "a*b"};
        for (String n : others) {
            check(n.equals(new NetPermission(n).getName()), "unregistered name constructs: " + n);
        }
    }

    // ---- actions argument is accepted but ignored (BasicPermission) ----
    static void testActionsIgnored() {
        NetPermission a = new NetPermission("x");
        NetPermission b = new NetPermission("x", "");
        NetPermission c = new NetPermission("x", "read");
        NetPermission d = new NetPermission("x", "read,write,connect");
        check("".equals(a.getActions()), "1-arg actions empty");
        check("".equals(b.getActions()), "empty actions stays empty");
        check("".equals(c.getActions()), "actions discarded");
        check("".equals(d.getActions()), "multi actions discarded");
        check(a.equals(b), "equals across action strings (1)");
        check(a.equals(c), "equals across action strings (2)");
        check(a.equals(d), "equals across action strings (3)");
        check(a.hashCode() == d.hashCode(), "hashCode ignores actions");
        check(a.implies(c), "implies ignores actions");
    }

    // ---- wildcard semantics: "*" and "prefix.*" only (BasicPermission; probed) ----
    static void testWildcardSemantics() {
        check(new NetPermission("*").implies(new NetPermission("setDefaultAuthenticator")), "* implies any name");
        check(new NetPermission("*").implies(new NetPermission("a.b.c")), "* implies dotted name");
        check(new NetPermission("*").implies(new NetPermission("*")), "* implies *");
        check(!new NetPermission("x").implies(new NetPermission("*")), "plain name never implies *");
        check(new NetPermission("a.*").implies(new NetPermission("a.b")), "a.* implies a.b");
        check(new NetPermission("a.*").implies(new NetPermission("a.b.c")), "a.* implies a.b.c (multi-level)");
        check(!new NetPermission("a.*").implies(new NetPermission("a")), "a.* does not imply a");
        check(!new NetPermission("a.*").implies(new NetPermission("ab")), "a.* does not imply ab");
        check(new NetPermission("a.*").implies(new NetPermission("a.*")), "wildcard implies itself");
        check(new NetPermission("a.*").implies(new NetPermission("a.b.*")), "a.* implies a.b.*");
        check(!new NetPermission("a.b.*").implies(new NetPermission("a.*")), "narrower wildcard does not imply wider");
        // 'set*' is NOT a wildcard for BasicPermission: only "*" or ".*" endings count
        check(!new NetPermission("set*").implies(new NetPermission("setDefaultAuthenticator")), "set* is a literal name");
        check(new NetPermission("set*").implies(new NetPermission("set*")), "set* implies itself (literal)");
        // plain names
        check(new NetPermission("a").implies(new NetPermission("a")), "same name implies");
        check(!new NetPermission("a").implies(new NetPermission("b")), "different name not implies");
        check(!new NetPermission("Set").implies(new NetPermission("set")), "names are case-sensitive");
    }

    // ---- error paths (probed: BasicPermission.init messages) ----
    static void testErrors() {
        try {
            new NetPermission(null);
            check(false, "null name must throw");
        } catch (NullPointerException e) {
            check("name can't be null".equals(e.getMessage()), "null name message");
        }
        try {
            new NetPermission(null, "act");
            check(false, "null name 2-arg must throw");
        } catch (NullPointerException e) {
            check("name can't be null".equals(e.getMessage()), "null name 2-arg message");
        }
        try {
            new NetPermission("");
            check(false, "empty name must throw");
        } catch (IllegalArgumentException e) {
            check("name can't be empty".equals(e.getMessage()), "empty name message");
        }
        try {
            new NetPermission("", "act");
            check(false, "empty name 2-arg must throw");
        } catch (IllegalArgumentException e) {
            check("name can't be empty".equals(e.getMessage()), "empty name 2-arg message");
        }
        // implies(null) is false by Permission contract
        check(!new NetPermission("a").implies(null), "implies(null) false");
        // implies of an unrelated permission type is false
        check(!new NetPermission("a").implies(new java.net.SocketPermission("localhost:80", "connect")),
            "implies other type false");
    }

    // ---- equals/hashCode/toString ----
    static void testEqualsHashCode() {
        NetPermission p = new NetPermission("setDefaultAuthenticator");
        NetPermission q = new NetPermission("setDefaultAuthenticator");
        check(p.equals(q), "equal names equal");
        check(q.equals(p), "symmetric");
        check(p.hashCode() == q.hashCode(), "equal => equal hashCode");
        check(p.hashCode() == p.hashCode(), "hashCode stable");
        check(!p.equals(new NetPermission("setProxySelector")), "different names unequal");
        check(!p.equals(null), "equals(null) false");
        check(!p.equals("setDefaultAuthenticator"), "not equal to String");
        check(p.equals(p), "reflexive");
        String s = p.toString();
        check(s != null && s.contains("java.net.NetPermission") && s.contains("setDefaultAuthenticator"),
            "toString contains class and name: " + s);
    }

    // ---- serialization round-trip ----
    static void testSerialization() throws Exception {
        NetPermission orig = new NetPermission("setProxySelector", "ignored");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(orig);
        Object back = new java.io.ObjectInputStream(
            new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof NetPermission, "round-trip type");
        NetPermission rt = (NetPermission) back;
        check(orig.equals(rt), "round-trip equals");
        check(orig.hashCode() == rt.hashCode(), "round-trip hashCode");
        check("setProxySelector".equals(rt.getName()), "round-trip name");
        check("".equals(rt.getActions()), "round-trip actions");
        check(rt.implies(new NetPermission("setProxySelector")), "round-trip implies");
        check(!rt.implies(new NetPermission("other")), "round-trip non-implies");
        // wildcard survives serialization
        NetPermission w = new NetPermission("*");
        bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(w);
        NetPermission wrt = (NetPermission) new java.io.ObjectInputStream(
            new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(wrt.implies(new NetPermission("anything")), "wildcard survives round-trip");
    }

    // ---- PermissionCollection from newPermissionCollection ----
    static void testCollection() throws Exception {
        PermissionCollection pc = new NetPermission("a").newPermissionCollection();
        check(pc != null, "collection non-null");
        check(!pc.isReadOnly(), "collection writable");
        pc.add(new NetPermission("a"));
        pc.add(new NetPermission("b.*"));
        check(pc.implies(new NetPermission("a")), "collection implies member");
        check(pc.implies(new NetPermission("a", "actions")), "collection implies member ignoring actions");
        check(pc.implies(new NetPermission("b.c")), "collection implies wildcard member");
        check(!pc.implies(new NetPermission("c")), "collection does not imply non-member");
        check(!pc.implies(new java.net.SocketPermission("localhost:80", "connect")),
            "collection implies other type false");
        Enumeration<Permission> en = pc.elements();
        int n = 0;
        while (en.hasMoreElements()) { en.nextElement(); n++; }
        check(n == 2, "elements count");
        pc.setReadOnly();
        check(pc.isReadOnly(), "readonly after setReadOnly");
        try {
            pc.add(new NetPermission("d"));
            check(false, "add to readonly must throw");
        } catch (SecurityException e) {
            passed++;
        }
    }

    public static void main(String[] args) throws Exception {
        testOriginalWhiteBox();
        testOriginalBlackBox();
        testWhiteBox();
        testDocumentedNames();
        testActionsIgnored();
        testWildcardSemantics();
        testErrors();
        testEqualsHashCode();
        testSerialization();
        testCollection();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

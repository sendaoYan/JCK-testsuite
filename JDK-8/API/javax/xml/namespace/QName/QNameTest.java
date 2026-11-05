/*
 * @test
 * @summary javax.xml.namespace.QName conformance: constructors, null semantics, accessors, equals/hashCode, valueOf, toString, Serializable.
 * @run main QNameTest
 */

import javax.xml.namespace.QName;

import java.io.Serializable;

public class QNameTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Thrower { void run() throws Exception; }

    static boolean isIae(Thrower t) {
        try { t.run(); return false; }
        catch (IllegalArgumentException e) { return true; }
        catch (Exception e) { return false; }
    }

    public static void main(String[] args) throws Exception {
        // ---- 3-arg constructor ----
        QName q = new QName("urn:x", "local", "pre");
        check("urn:x".equals(q.getNamespaceURI()), "getNamespaceURI");
        check("local".equals(q.getLocalPart()), "getLocalPart");
        check("pre".equals(q.getPrefix()), "getPrefix");
        check("{urn:x}local".equals(q.toString()), "3-arg toString");

        // ---- 2-arg constructor (no prefix) ----
        QName q2 = new QName("urn:x", "local");
        check("".equals(q2.getPrefix()), "no-prefix constructor prefix empty");
        check("{urn:x}local".equals(q2.toString()), "2-arg toString");

        // ---- 1-arg constructor (local part only) ----
        QName q3 = new QName("local");
        check("".equals(q3.getNamespaceURI()), "local-only namespace empty");
        check("local".equals(q3.getLocalPart()), "local-only local part");
        check("local".equals(q3.toString()), "local-only toString");

        // ---- null semantics ----
        QName nullNs = new QName(null, "local");
        check("".equals(nullNs.getNamespaceURI()), "null namespace -> empty string");
        check(isIae(() -> new QName("urn:x", null)), "null localPart -> IAE");
        check(isIae(() -> new QName((String) null)), "null local-only -> IAE");

        // ---- equals / hashCode (prefix is ignored) ----
        check(q.equals(q2), "equals ignores prefix");
        check(q.hashCode() == q2.hashCode(), "hashCode ignores prefix");
        check(!q.equals(q3), "equals false for different local/ns");
        check(q.equals(new QName("urn:x", "local", "other")), "equals across different prefixes");

        // ---- valueOf ----
        QName v = QName.valueOf("{urn:x}local");
        check("urn:x".equals(v.getNamespaceURI()) && "local".equals(v.getLocalPart()), "valueOf with ns");
        QName v2 = QName.valueOf("plain");
        check("plain".equals(v2.getLocalPart()) && "".equals(v2.getNamespaceURI()), "valueOf without braces");
        check(isIae(() -> QName.valueOf(null)), "valueOf(null) -> IAE");

        // ---- Serializable ----
        check(q instanceof Serializable, "QName is Serializable");

        testValueOfMatrix();
        testNullAndEmptyMatrix();
        testSerializationRoundTrip();
        testImmutability();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ================= deepening: valueOf parse matrix =================
    static void testValueOfMatrix() {
        // namespace + prefix-looking local part: local keeps the colon, prefix stays ""
        QName prefixed = QName.valueOf("{urn:x}p:y");
        check("urn:x".equals(prefixed.getNamespaceURI()), "valueOf '{urn:x}p:y' ns");
        check("p:y".equals(prefixed.getLocalPart()), "valueOf '{urn:x}p:y' local part keeps colon");
        check("".equals(prefixed.getPrefix()), "valueOf never carries a prefix");

        // multiple closing braces: first '}' splits
        QName multi = QName.valueOf("{a}}b");
        check("a".equals(multi.getNamespaceURI()) && "}b".equals(multi.getLocalPart()),
              "valueOf('{a}}b') splits at first '}'");

        // no-namespace forms are accepted verbatim
        check("".equals(QName.valueOf("y").getNamespaceURI()) && "y".equals(QName.valueOf("y").getLocalPart()),
              "valueOf('y') empty ns");
        check("".equals(QName.valueOf("}").getNamespaceURI()) && "}".equals(QName.valueOf("}").getLocalPart()),
              "valueOf('}') empty ns, local '}'");
        check("".equals(QName.valueOf("").getNamespaceURI()) && "".equals(QName.valueOf("").getLocalPart()),
              "valueOf('') empty ns and local (impl accepts, locked)");

        // malformed: missing closing brace
        String[] bad = {"{x", "{"};
        for (String s : bad) {
            boolean iae = false;
            String msg = null;
            try { QName.valueOf(s); } catch (IllegalArgumentException e) { iae = true; msg = e.getMessage(); }
            check(iae, "valueOf('" + s + "') -> IllegalArgumentException");
            check(msg != null && msg.contains("missing closing") && msg.contains(s),
                  "valueOf('" + s + "') message quotes input and reason");
        }

        // empty namespace with brace form is rejected
        String[] emptyNs = {"{}", "{}x"};
        for (String s : emptyNs) {
            boolean iae = false;
            String msg = null;
            try { QName.valueOf(s); } catch (IllegalArgumentException e) { iae = true; msg = e.getMessage(); }
            check(iae, "valueOf('" + s + "') -> IllegalArgumentException (empty ns in brace form)");
            check(msg != null && msg.contains("NULL_NS_URI"),
                  "valueOf('" + s + "') message cites NULL_NS_URI");
        }

        // namespace-only form yields empty local part (impl accepts, locked)
        QName nsOnly = QName.valueOf("{ns}");
        check("ns".equals(nsOnly.getNamespaceURI()) && "".equals(nsOnly.getLocalPart()),
              "valueOf('{ns}') empty local part (impl accepts, locked)");

        // null -> IllegalArgumentException (valueOf contract)
        boolean iaeNull = false;
        try { QName.valueOf(null); } catch (IllegalArgumentException e) { iaeNull = true; }
        check(iaeNull, "valueOf(null) -> IllegalArgumentException");

        // valueOf result equals constructor result for identical ns/local
        check(QName.valueOf("{urn:x}local").equals(new QName("urn:x", "local")),
              "valueOf result equals constructor result");
    }

    static void testNullAndEmptyMatrix() {
        // null prefix in 3-arg ctor -> IAE (not NPE; locked on both JDKs)
        boolean iaePrefix = false;
        String prefixMsg = null;
        try { new QName("urn:x", "local", null); }
        catch (IllegalArgumentException e) { iaePrefix = true; prefixMsg = e.getMessage(); }
        catch (NullPointerException e) { /* recorded alternative */ }
        check(iaePrefix, "3-arg null prefix -> IllegalArgumentException");
        check(prefixMsg != null && prefixMsg.contains("prefix"), "null prefix message mentions prefix");

        // null namespace in 3-arg ctor is normalized to ""
        QName nullNs = new QName(null, "local", "p");
        check("".equals(nullNs.getNamespaceURI()), "3-arg null ns -> empty string");
        check("p".equals(nullNs.getPrefix()), "3-arg prefix preserved");

        // null localPart in 3-arg ctor -> IAE
        boolean iaeLocal = false;
        String localMsg = null;
        try { new QName("urn:x", null, "p"); } catch (IllegalArgumentException e) { iaeLocal = true; localMsg = e.getMessage(); }
        check(iaeLocal, "3-arg null localPart -> IllegalArgumentException");
        check(localMsg != null && localMsg.contains("local part"), "null localPart message mentions local part");

        // empty-string namespace is legal
        QName emptyNs = new QName("", "x");
        check("".equals(emptyNs.getNamespaceURI()) && "x".equals(emptyNs.getLocalPart()), "empty ns legal");

        // empty-string localPart is ACCEPTED by the constructors (only null is rejected; locked on both JDKs)
        QName emptyLocal = new QName("");
        check("".equals(emptyLocal.getLocalPart()) && "".equals(emptyLocal.getNamespaceURI()),
              "new QName('') accepted with empty ns and local (locked)");
        QName emptyLocal2 = new QName("urn:x", "");
        check("urn:x".equals(emptyLocal2.getNamespaceURI()) && "".equals(emptyLocal2.getLocalPart()),
              "new QName(ns, '') accepted (locked)");
        // empty local never equals a non-empty local
        check(!emptyLocal2.equals(new QName("urn:x", "local")), "empty local not equal to non-empty");
    }

    static void testSerializationRoundTrip() throws Exception {
        QName original = new QName("urn:x", "local", "pfx");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.close();
        java.io.ObjectInputStream oin = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        Object back = oin.readObject();
        check(back instanceof QName, "deserialized object is a QName");
        QName dq = (QName) back;
        check(back.equals(original), "serialization round-trip equals");
        check("urn:x".equals(dq.getNamespaceURI()) && "local".equals(dq.getLocalPart())
              && "pfx".equals(dq.getPrefix()),
              "round-trip preserves ns/local/prefix fields exactly");
        check(dq.hashCode() == original.hashCode(), "round-trip hashCode equal");
        check(dq.toString().equals(original.toString()), "round-trip toString equal");
        check(dq.getClass() == QName.class, "deserialized class is QName itself (no proxy class in stream)");

        // default-prefix instance round-trips too
        QName plain = new QName("solo");
        bos.reset();
        java.io.ObjectOutputStream oos2 = new java.io.ObjectOutputStream(bos);
        oos2.writeObject(plain);
        oos2.close();
        java.io.ObjectInputStream oin2 = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        QName plain2 = (QName) oin2.readObject();
        check(plain2.equals(plain) && "".equals(plain2.getPrefix()), "default ns/prefix round-trip");

        // stable serialVersionUID
        long svuid = java.io.ObjectStreamClass.lookup(QName.class).getSerialVersionUID();
        check(svuid == -9120448754896609940L, "serialVersionUID locked (" + svuid + ")");
    }

    static void testImmutability() throws Exception {
        // no public setters of any kind
        boolean hasSetter = false;
        for (java.lang.reflect.Method m : QName.class.getMethods())
            if (m.getName().startsWith("set") || m.getName().startsWith("clear")) hasSetter = true;
        check(!hasSetter, "no public setters (immutable value type)");
        // equals/hashCode are final: semantics cannot be altered by subclassing
        check(java.lang.reflect.Modifier.isFinal(QName.class.getMethod("equals", Object.class).getModifiers()),
              "equals is final");
        check(java.lang.reflect.Modifier.isFinal(QName.class.getMethod("hashCode").getModifiers()),
              "hashCode is final");
        // class itself is NOT final (subclassing allowed, equality still value-based)
        check(!java.lang.reflect.Modifier.isFinal(QName.class.getModifiers()), "QName class not final");
        // equals against foreign types and null
        check(!new QName("urn:x", "local").equals(null), "equals(null) false");
        check(!new QName("urn:x", "local").equals("{urn:x}local"), "equals(String) false");
        // toString never shows the prefix
        check("{urn:x}local".equals(new QName("urn:x", "local", "pfx").toString()),
              "toString omits prefix");
        check("local".equals(new QName(null, "local").toString()), "toString without ns is bare local");
        // cross-prefix equality contract once more with hashCodes
        QName a = new QName("urn:x", "local", "p1");
        QName b = new QName("urn:x", "local", "p2");
        check(a.equals(b) && b.equals(a), "equals symmetric across prefixes");
        check(a.hashCode() == b.hashCode(), "hashCode ignores prefix");
        // different ns or local breaks equality
        check(!a.equals(new QName("urn:y", "local", "p1")), "different ns breaks equals");
        check(!a.equals(new QName("urn:x", "other", "p1")), "different local breaks equals");
    }
}

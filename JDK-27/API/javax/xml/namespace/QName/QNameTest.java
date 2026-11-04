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

        System.out.println("PASS (" + passed + " assertions)");
    }
}

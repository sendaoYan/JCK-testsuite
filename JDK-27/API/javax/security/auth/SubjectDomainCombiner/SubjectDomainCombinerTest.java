/*
 * @test
 * @summary javax.security.auth.SubjectDomainCombiner conformance: white-box structure + black-box behavior.
 * @run main SubjectDomainCombinerTest
 */

import java.lang.reflect.Modifier;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import javax.security.auth.Subject;
import javax.security.auth.SubjectDomainCombiner;
import javax.security.auth.x500.X500Principal;

public class SubjectDomainCombinerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.auth.SubjectDomainCombiner");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(java.security.DomainCombiner.class.isAssignableFrom(c), "implements DomainCombiner");
        check(c.isAnnotationPresent(Deprecated.class), "deprecated");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Subject sub = new Subject();
        sub.getPrincipals().add(new X500Principal("CN=Alice"));
        SubjectDomainCombiner sdc = new SubjectDomainCombiner(sub);
        check(sdc.getSubject() == sub, "getSubject returns associated subject");

        // null subject rejected
        boolean npe = false;
        try { new SubjectDomainCombiner(null); } catch (NullPointerException e) { npe = true; }
        check(npe, "null subject throws NullPointerException");

        // no current domains -> assigned domains returned directly
        ProtectionDomain[] assigned = { dynamicPD("http://assigned.invalid") };
        ProtectionDomain[] r0 = sdc.combine(null, assigned);
        check(r0 == assigned, "combine(null, assigned) returns assigned reference");
        ProtectionDomain[] r1 = sdc.combine(new ProtectionDomain[0], assigned);
        check(r1 == assigned, "combine(empty, assigned) returns assigned reference");
        check(sdc.combine(null, null) == null, "combine(null, null) returns null");

        // dynamic current domains get principals attached; assigned domains appended
        ProtectionDomain cur = dynamicPD("http://current.invalid");
        ProtectionDomain[] current = { cur };
        ProtectionDomain[] combined = sdc.combine(current, assigned);
        check(combined.length == current.length + assigned.length, "combined length = current + assigned");
        check(combined[1] == assigned[0], "assigned domain preserved by reference");
        check(combined[0] != cur, "dynamic current domain replaced");
        check(combined[0].getCodeSource() == cur.getCodeSource(), "codeSource preserved");
        check(combined[0].getPrincipals().length == 1, "combined domain has one principal");
        check("CN=Alice".equals(combined[0].getPrincipals()[0].getName()), "combined domain carries subject principal");

        // read-only subject also attaches principals
        Subject ro = new Subject();
        ro.getPrincipals().add(new X500Principal("CN=Bob"));
        ro.setReadOnly();
        SubjectDomainCombiner roSdc = new SubjectDomainCombiner(ro);
        ProtectionDomain[] roCombined = roSdc.combine(current, null);
        check(roCombined.length == 1 && roCombined[0] != cur, "read-only combine replaces current");
        check("CN=Bob".equals(roCombined[0].getPrincipals()[0].getName()), "read-only combined carries principal");

        // current-only combine with no assigned domains
        ProtectionDomain[] onlyCurrent = sdc.combine(current, null);
        check(onlyCurrent.length == 1, "combine(current, null) length = current");
    }

    static ProtectionDomain dynamicPD(String url) throws Exception {
        CodeSource cs = new CodeSource(new URL(url), (java.security.cert.Certificate[]) null);
        return new ProtectionDomain(cs, null, null, null);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

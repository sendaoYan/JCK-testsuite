/*
 * @test
 * @summary javax.naming.LinkException conformance: white-box structure + black-box behavior.
 * @run main LinkExceptionTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.naming.CompositeName;
import javax.naming.LinkException;
import javax.naming.NamingException;
import javax.naming.Name;
import javax.naming.LinkLoopException;
import javax.naming.MalformedLinkException;

public class LinkExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = LinkException.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == NamingException.class, "extends NamingException");
        for (String m : new String[] {"getLinkResolvedName", "setLinkResolvedName", "getLinkRemainingName",
                "setLinkRemainingName", "getLinkResolvedObj", "setLinkResolvedObj"})
            check(method(c, m), "declares " + m);
        check(c.getMethod("toString", boolean.class) != null, "toString(boolean)");
        check(javax.naming.LinkLoopException.class.getSuperclass() == c, "LinkLoopException subclass");
        check(javax.naming.MalformedLinkException.class.getSuperclass() == c, "MalformedLinkException subclass");
    }
    static boolean method(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        LinkException le = new LinkException("lex");
        check(le.getMessage().equals("lex"), "message");

        le.setLinkResolvedName(new CompositeName("lr"));
        le.setLinkRemainingName(new CompositeName("lm"));
        le.setLinkResolvedObj("lo");
        check(le.getLinkResolvedName().get(0).equals("lr"), "link resolved name");
        check(le.getLinkRemainingName().get(0).equals("lm"), "link remaining name");
        check("lo".equals(le.getLinkResolvedObj()), "link resolved obj");

        // toString exposes link parts alongside the base message
        String s = le.toString();
        check(s.contains("javax.naming.LinkException: lex"), "header");
        check(s.contains("Link Remaining Name: 'lm'"), "link remaining in toString");

        // independent from the non-link resolved/remaining state
        le.setRemainingName(new CompositeName("m"));
        check(!le.getRemainingName().equals(le.getLinkRemainingName()), "two tracks independent");
        check(le instanceof NamingException, "NamingException hierarchy");

        // setLinkRemainingName clones the argument
        CompositeName arg = new CompositeName("x/y");
        le.setLinkRemainingName(arg);
        check(le.getLinkRemainingName() != arg, "clone on set");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dExcConstructors();
        dExcNameChain();
        dExcRootCause();
        dExcStringForms();
        dExcSerialization();
        dExcTriggerLINKRICH();

        System.out.println("deep: LinkException contracts OK");
    }

    static void dExcConstructors() throws Exception {
        javax.naming.LinkException e = new javax.naming.LinkException();
        check(e.getMessage() == null, "no-arg message null");
        check(e.getExplanation() == null, "no-arg explanation null");
        check(e.getRemainingName() == null && e.getResolvedName() == null && e.getResolvedObj() == null,
                "no-arg name/obj fields null");
        check(e.getRootCause() == null && e.getCause() == null, "no-arg cause null");
        javax.naming.LinkException e2 = new javax.naming.LinkException("boom");
        check("boom".equals(e2.getMessage()), "(String) message");
        check("boom".equals(e2.getExplanation()), "explanation == message");
        javax.naming.LinkException eNull = new javax.naming.LinkException(null);
        check(eNull.getMessage() == null, "(null) message null");
        check(eNull.toString().contains("LinkException"), "toString contains simple name");
        check(e2.fillInStackTrace() == e2 && "boom".equals(e2.getMessage()), "fillInStackTrace keeps message");
        check(e instanceof java.io.Serializable, "Serializable");
        check(e instanceof javax.naming.NamingException, "NamingException hierarchy");
    }

    static void dExcNameChain() throws Exception {
        javax.naming.LinkException e = new javax.naming.LinkException("chain");
        javax.naming.CompositeName rem = new javax.naming.CompositeName("a/b");
        e.setRemainingName(rem);
        check(e.getRemainingName().equals(rem), "remainingName round trip");
        rem.add("mutated");
        check(e.getRemainingName().size() == 2, "setRemainingName clones (independent)");
        javax.naming.CompositeName res = new javax.naming.CompositeName("x/y");
        e.setResolvedName(res);
        check(e.getResolvedName().equals(res), "resolvedName round trip");
        e.setResolvedName(null);
        check(e.getResolvedName() == null, "setResolvedName(null) clears");
        e.setRemainingName(null);
        check(e.getRemainingName() == null, "setRemainingName(null) clears");
        Object obj = new Object();
        e.setResolvedObj(obj);
        check(e.getResolvedObj() == obj, "resolvedObj same instance");
        e.appendRemainingComponent("z");
        check("z".equals(e.getRemainingName().get(0)), "appendRemainingComponent from null creates name");
        javax.naming.CompositeName base = new javax.naming.CompositeName("a/b");
        e.setRemainingName(base);
        e.appendRemainingComponent("c");
        check("a/b/c".equals(e.getRemainingName().toString()), "appendRemainingComponent appends");
        javax.naming.CompositeName tail = new javax.naming.CompositeName("q/r");
        e.appendRemainingName(tail);
        check("a/b/c/q/r".equals(e.getRemainingName().toString()), "appendRemainingName appends");
        e.appendRemainingName(null);
        check("a/b/c/q/r".equals(e.getRemainingName().toString()), "appendRemainingName(null) no-op");
        e.appendRemainingComponent(null);
        check("a/b/c/q/r".equals(e.getRemainingName().toString()), "appendRemainingComponent(null) no-op");
        javax.naming.CompositeName before = new javax.naming.CompositeName("k");
        e.setRemainingName(before);
        check(e.getRemainingName().equals(before), "reset remainingName");
        check(before.size() == 1, "no side effect on argument");
    }

    static void dExcRootCause() throws Exception {
        javax.naming.LinkException e = new javax.naming.LinkException("root");
        RuntimeException rt = new RuntimeException("cause");
        e.setRootCause(rt);
        check(e.getRootCause() == rt, "rootCause same instance");
        check(e.getCause() == rt, "getCause mirrors rootCause");
        e.setRootCause(null);
        check(e.getRootCause() == null && e.getCause() == null, "setRootCause(null) clears");
        e.setRootCause(e);
        check(e.getRootCause() == null, "setRootCause(this) no-op");
        IllegalStateException ill = new IllegalStateException("x");
        e.setRootCause(ill);
        check(e.getCause() == ill, "re-set rootCause");
        javax.naming.LinkException e2 = new javax.naming.LinkException("init");
        Exception c1 = new Exception("c1");
        Exception c2 = new Exception("c2");
        check(e2.initCause(c1) == e2, "initCause returns this");
        check(e2.getRootCause() == c1 && e2.getCause() == c1, "initCause mirrors to rootCause");
        boolean threw = false;
        try { e2.initCause(c2); } catch (IllegalStateException ex) { threw = true; }
        check(threw, "initCause twice -> IllegalStateException");
        check(e2.getRootCause() == c1, "rootCause unchanged after failed initCause");
    }

    static void dExcStringForms() throws Exception {
        javax.naming.LinkException e = new javax.naming.LinkException("msg");
        check(e.toString().contains("msg"), "toString contains message");
        check(!e.toString().contains("remaining name"), "no remaining name section when unset");
        e.setRemainingName(new javax.naming.CompositeName("r/s"));
        check(e.toString().contains("remaining name 'r/s'"), "toString contains remaining name");
        check(e.toString().contains("LinkException"), "toString contains class simple name");
        check(e.toString(false).equals(e.toString()), "toString(false) same as toString when resolvedObj null");
        e.setLinkResolvedObj("LRO");
        check(e.toString(true).contains("Link Resolved Object: LRO"),
                "toString(true) contains link resolved object");
        check(!e.toString(false).contains("Link Resolved Object"),
                "toString(false) omits link resolved object");
        check(!e.toString(false).contains("resolved object"), "toString(false) omits resolved object");
        e.setRootCause(new RuntimeException("rt"));
        check(e.toString().contains("[Root exception is"), "toString contains root exception");
        check(e.toString().indexOf("[Root exception is") >= 0
                && e.toString().indexOf("; remaining name") > e.toString().indexOf("[Root exception is"),
                "root exception segment precedes remaining name");
    }

    @SuppressWarnings("unchecked")
    static <T> T dSer(T o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oo = new java.io.ObjectOutputStream(bos)) { oo.writeObject(o); }
        try (java.io.ObjectInputStream oi = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            return (T) oi.readObject();
        }
    }

    static void dExcSerialization() throws Exception {
        javax.naming.LinkException e = new javax.naming.LinkException("ser");
        e.setRemainingName(new javax.naming.CompositeName("a/b"));
        e.setResolvedName(new javax.naming.CompositeName("c"));
        e.setResolvedObj("obj");
        RuntimeException rt = new RuntimeException("rootser");
        e.setRootCause(rt);
        javax.naming.LinkException back = dSer(e);
        check("ser".equals(back.getMessage()), "message survives serialization");
        check(back.getRemainingName().equals(new javax.naming.CompositeName("a/b")), "remainingName survives");
        check(back.getResolvedName().equals(new javax.naming.CompositeName("c")), "resolvedName survives");
        check("obj".equals(back.getResolvedObj()), "resolvedObj survives");
        check(back.getRootCause() != null && "rootser".equals(back.getRootCause().getMessage()),
                "rootCause survives");
        check(back instanceof javax.naming.LinkException, "deserialized type");
    }

    static void dExcTriggerLINKRICH() throws Exception {
        javax.naming.LinkException e = new javax.naming.LinkException();
        check(e.getMessage() == null, "no-arg message null");
        check(e.getLinkExplanation() == null, "default link explanation null");
        // linkExplanation is an independent field: no fallback to the message
        javax.naming.LinkException e2 = new javax.naming.LinkException("why");
        check(e2.getLinkExplanation() == null, "linkExplanation stays null after ctor");
        e2.setLinkExplanation("le");
        check("le".equals(e2.getLinkExplanation()), "setLinkExplanation round trip");
        javax.naming.LinkException e3 = new javax.naming.LinkException("msg");
        e3.setLinkExplanation("custom");
        check("custom".equals(e3.getLinkExplanation()), "explicit linkExplanation wins over message");
        javax.naming.CompositeName lrn = new javax.naming.CompositeName("p/1");
        javax.naming.CompositeName lmn = new javax.naming.CompositeName("p/2");
        e3.setLinkResolvedName(lrn);
        e3.setLinkRemainingName(lmn);
        e3.setLinkResolvedObj("obj");
        check(e3.getLinkResolvedName().equals(lrn) && e3.getLinkRemainingName().equals(lmn),
                "link names round trip");
        lrn.add("mut");
        check(e3.getLinkResolvedName().size() == 2, "setLinkResolvedName clones");
        String s = e3.toString();
        check(s.contains("msg"), "toString has message");
        check(s.contains("p/2"), "toString shows link remaining name");
        check(!s.contains("custom"), "toString omits linkExplanation");
        check(!s.contains("p/1"), "toString omits link resolved name");
        check(!s.contains("obj"), "plain toString omits link resolved object");
        check(e3.toString(true).contains("obj"), "toString(true) has link resolved object");
        check(e3.toString(true).contains("Link Resolved Object"), "toString(true) labels link object");
        check(e3.toString(false).contains("p/2"), "toString(false) still shows link remainder");
        check(e3.toString(false).equals(e3.toString()), "toString(false) equals toString()");
    }


}

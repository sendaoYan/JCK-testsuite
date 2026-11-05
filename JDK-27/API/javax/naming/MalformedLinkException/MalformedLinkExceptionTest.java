/*
 * @test
 * @summary javax.naming.MalformedLinkException conformance: white-box structure + black-box behavior.
 * @run main MalformedLinkExceptionTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import javax.naming.CompositeName;
import javax.naming.LinkException;
import javax.naming.MalformedLinkException;
import javax.naming.NamingException;

public class MalformedLinkExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = MalformedLinkException.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == LinkException.class, "extends LinkException");
        Constructor<?>[] cs = c.getDeclaredConstructors();
        boolean noArg = false, strArg = false;
        for (Constructor<?> k : cs) {
            if (k.getParameterCount() == 0) noArg = true;
            if (k.getParameterCount() == 1 && k.getParameterTypes()[0] == String.class) strArg = true;
        }
        check(noArg && strArg, "both ctors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MalformedLinkException mle = new MalformedLinkException("invalid link name");
        check(mle.getMessage().equals("invalid link name"), "message");
        try { throw mle; } catch (LinkException le) { check(le.getMessage().equals("invalid link name"), "caught as LinkException"); }
        mle.setLinkRemainingName(new CompositeName("bad/link"));
        check(mle.toString().contains("bad/link"), "link context in toString");
        check(new MalformedLinkException().getMessage() == null, "no-arg message null");
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
        dExcTriggerLINKFAM();

        System.out.println("deep: MalformedLinkException contracts OK");
    }

    static void dExcConstructors() throws Exception {
        javax.naming.MalformedLinkException e = new javax.naming.MalformedLinkException();
        check(e.getMessage() == null, "no-arg message null");
        check(e.getExplanation() == null, "no-arg explanation null");
        check(e.getRemainingName() == null && e.getResolvedName() == null && e.getResolvedObj() == null,
                "no-arg name/obj fields null");
        check(e.getRootCause() == null && e.getCause() == null, "no-arg cause null");
        javax.naming.MalformedLinkException e2 = new javax.naming.MalformedLinkException("boom");
        check("boom".equals(e2.getMessage()), "(String) message");
        check("boom".equals(e2.getExplanation()), "explanation == message");
        javax.naming.MalformedLinkException eNull = new javax.naming.MalformedLinkException(null);
        check(eNull.getMessage() == null, "(null) message null");
        check(eNull.toString().contains("MalformedLinkException"), "toString contains simple name");
        check(e2.fillInStackTrace() == e2 && "boom".equals(e2.getMessage()), "fillInStackTrace keeps message");
        check(e instanceof java.io.Serializable, "Serializable");
        check(e instanceof javax.naming.NamingException, "NamingException hierarchy");
    }

    static void dExcNameChain() throws Exception {
        javax.naming.MalformedLinkException e = new javax.naming.MalformedLinkException("chain");
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
        javax.naming.MalformedLinkException e = new javax.naming.MalformedLinkException("root");
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
        javax.naming.MalformedLinkException e2 = new javax.naming.MalformedLinkException("init");
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
        javax.naming.MalformedLinkException e = new javax.naming.MalformedLinkException("msg");
        check(e.toString().contains("msg"), "toString contains message");
        check(!e.toString().contains("remaining name"), "no remaining name section when unset");
        e.setRemainingName(new javax.naming.CompositeName("r/s"));
        check(e.toString().contains("remaining name 'r/s'"), "toString contains remaining name");
        check(e.toString().contains("MalformedLinkException"), "toString contains class simple name");
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
        javax.naming.MalformedLinkException e = new javax.naming.MalformedLinkException("ser");
        e.setRemainingName(new javax.naming.CompositeName("a/b"));
        e.setResolvedName(new javax.naming.CompositeName("c"));
        e.setResolvedObj("obj");
        RuntimeException rt = new RuntimeException("rootser");
        e.setRootCause(rt);
        javax.naming.MalformedLinkException back = dSer(e);
        check("ser".equals(back.getMessage()), "message survives serialization");
        check(back.getRemainingName().equals(new javax.naming.CompositeName("a/b")), "remainingName survives");
        check(back.getResolvedName().equals(new javax.naming.CompositeName("c")), "resolvedName survives");
        check("obj".equals(back.getResolvedObj()), "resolvedObj survives");
        check(back.getRootCause() != null && "rootser".equals(back.getRootCause().getMessage()),
                "rootCause survives");
        check(back instanceof javax.naming.MalformedLinkException, "deserialized type");
    }

    static void dExcTriggerLINKFAM() throws Exception {
        javax.naming.MalformedLinkException e = new javax.naming.MalformedLinkException("bad link");
        check(e instanceof javax.naming.LinkException, "LinkException family");
        javax.naming.CompositeName lrn = new javax.naming.CompositeName("l/a");
        javax.naming.CompositeName lmn = new javax.naming.CompositeName("l/b");
        e.setLinkResolvedName(lrn);
        e.setLinkRemainingName(lmn);
        e.setLinkResolvedObj("lo");
        e.setLinkExplanation("cycle detected");
        check(e.getLinkResolvedName().equals(lrn), "linkResolvedName round trip");
        check(e.getLinkRemainingName().equals(lmn), "linkRemainingName round trip");
        check("lo".equals(e.getLinkResolvedObj()), "linkResolvedObj round trip");
        check("cycle detected".equals(e.getLinkExplanation()), "linkExplanation round trip");
        String s = e.toString();
        check(s.contains("Link Remainder") || s.contains("l/b"), "toString contains link remaining");
    }


}

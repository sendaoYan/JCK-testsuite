/*
 * @test
 * @summary javax.naming.OperationNotSupportedException conformance: white-box structure + black-box behavior.
 * @run main OperationNotSupportedExceptionTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.naming.directory.BasicAttribute;

public class OperationNotSupportedExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = javax.naming.OperationNotSupportedException.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == javax.naming.NamingException.class, "extends NamingException");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        Constructor<?>[] cs = c.getDeclaredConstructors();
        boolean noArg = false, strArg = false;
        for (Constructor<?> k : cs) {
            if (k.getParameterCount() == 0) noArg = true;
            if (k.getParameterCount() == 1 && k.getParameterTypes()[0] == String.class) strArg = true;
        }
        check(noArg, "has no-arg ctor");
        check(strArg, "has String ctor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        OperationNotSupportedException withMsg = new OperationNotSupportedException("reason");
        check(withMsg.getMessage().equals("reason"), "message preserved");
        OperationNotSupportedException noMsg = new OperationNotSupportedException();
        check(noMsg.getMessage() == null, "no-arg message null");
        withMsg.setRemainingName(new CompositeName("a/b"));
        check(withMsg.getRemainingName().toString().equals("a/b"), "remaining name round trip");
        withMsg.setResolvedObj("obj");
        check("obj".equals(withMsg.getResolvedObj()), "resolved obj");
        String ts = withMsg.toString();
        check(ts.contains("javax.naming.OperationNotSupportedException") && ts.contains("reason"), "toString naming");
        javax.naming.OperationNotSupportedException onse = new javax.naming.OperationNotSupportedException("no referrals on this provider");
        check(onse instanceof NamingException && onse.getMessage().contains("referrals"), "scenario");
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
        dExcTriggerONS();

        System.out.println("deep: OperationNotSupportedException contracts OK");
    }

    static void dExcConstructors() throws Exception {
        javax.naming.OperationNotSupportedException e = new javax.naming.OperationNotSupportedException();
        check(e.getMessage() == null, "no-arg message null");
        check(e.getExplanation() == null, "no-arg explanation null");
        check(e.getRemainingName() == null && e.getResolvedName() == null && e.getResolvedObj() == null,
                "no-arg name/obj fields null");
        check(e.getRootCause() == null && e.getCause() == null, "no-arg cause null");
        javax.naming.OperationNotSupportedException e2 = new javax.naming.OperationNotSupportedException("boom");
        check("boom".equals(e2.getMessage()), "(String) message");
        check("boom".equals(e2.getExplanation()), "explanation == message");
        javax.naming.OperationNotSupportedException eNull = new javax.naming.OperationNotSupportedException(null);
        check(eNull.getMessage() == null, "(null) message null");
        check(eNull.toString().contains("OperationNotSupportedException"), "toString contains simple name");
        check(e2.fillInStackTrace() == e2 && "boom".equals(e2.getMessage()), "fillInStackTrace keeps message");
        check(e instanceof java.io.Serializable, "Serializable");
        check(e instanceof javax.naming.NamingException, "NamingException hierarchy");
    }

    static void dExcNameChain() throws Exception {
        javax.naming.OperationNotSupportedException e = new javax.naming.OperationNotSupportedException("chain");
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
        javax.naming.OperationNotSupportedException e = new javax.naming.OperationNotSupportedException("root");
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
        javax.naming.OperationNotSupportedException e2 = new javax.naming.OperationNotSupportedException("init");
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
        javax.naming.OperationNotSupportedException e = new javax.naming.OperationNotSupportedException("msg");
        check(e.toString().contains("msg"), "toString contains message");
        check(!e.toString().contains("remaining name"), "no remaining name section when unset");
        e.setRemainingName(new javax.naming.CompositeName("r/s"));
        check(e.toString().contains("remaining name 'r/s'"), "toString contains remaining name");
        check(e.toString().contains("OperationNotSupportedException"), "toString contains class simple name");
        check(e.toString(false).equals(e.toString()), "toString(false) same as toString when resolvedObj null");
        Object o = new Object();
        e.setResolvedObj(o);
        check(e.toString(true).contains("resolved object"), "toString(true) contains resolved object");
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
        javax.naming.OperationNotSupportedException e = new javax.naming.OperationNotSupportedException("ser");
        e.setRemainingName(new javax.naming.CompositeName("a/b"));
        e.setResolvedName(new javax.naming.CompositeName("c"));
        e.setResolvedObj("obj");
        RuntimeException rt = new RuntimeException("rootser");
        e.setRootCause(rt);
        javax.naming.OperationNotSupportedException back = dSer(e);
        check("ser".equals(back.getMessage()), "message survives serialization");
        check(back.getRemainingName().equals(new javax.naming.CompositeName("a/b")), "remainingName survives");
        check(back.getResolvedName().equals(new javax.naming.CompositeName("c")), "resolvedName survives");
        check("obj".equals(back.getResolvedObj()), "resolvedObj survives");
        check(back.getRootCause() != null && "rootser".equals(back.getRootCause().getMessage()),
                "rootCause survives");
        check(back instanceof javax.naming.OperationNotSupportedException, "deserialized type");
    }

    static void dExcTriggerONS() throws Exception {
        // BasicAttribute schema access is a documented OperationNotSupportedException source
        try {
            new javax.naming.directory.BasicAttribute("cn").getAttributeDefinition();
            throw new AssertionError("expected OperationNotSupportedException");
        } catch (javax.naming.OperationNotSupportedException e) {
            check(e.getMessage() != null, "message present");
        }
        try {
            new javax.naming.directory.BasicAttribute("cn").getAttributeSyntaxDefinition();
            throw new AssertionError("expected OperationNotSupportedException");
        } catch (javax.naming.OperationNotSupportedException e) {
            check(true, "syntax definition throws OperationNotSupportedException");
        }
    }


}

/*
 * @test
 * @summary javax.naming.NoInitialContextException conformance: message and hierarchy.
 * @run main NoInitialContextExceptionTest
 */



import javax.naming.CompositeName;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.naming.spi.NamingManager;
public class NoInitialContextExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.NoInitialContextException e = new javax.naming.NoInitialContextException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof javax.naming.NamingException, "is NamingException");
        javax.naming.NoInitialContextException e0 = new javax.naming.NoInitialContextException();
        check(e0.getMessage() == null, "no-arg message null");
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
        dExcTriggerNOICTX();

        System.out.println("deep: NoInitialContextException contracts OK");
    }

    static void dExcConstructors() throws Exception {
        javax.naming.NoInitialContextException e = new javax.naming.NoInitialContextException();
        check(e.getMessage() == null, "no-arg message null");
        check(e.getExplanation() == null, "no-arg explanation null");
        check(e.getRemainingName() == null && e.getResolvedName() == null && e.getResolvedObj() == null,
                "no-arg name/obj fields null");
        check(e.getRootCause() == null && e.getCause() == null, "no-arg cause null");
        javax.naming.NoInitialContextException e2 = new javax.naming.NoInitialContextException("boom");
        check("boom".equals(e2.getMessage()), "(String) message");
        check("boom".equals(e2.getExplanation()), "explanation == message");
        javax.naming.NoInitialContextException eNull = new javax.naming.NoInitialContextException(null);
        check(eNull.getMessage() == null, "(null) message null");
        check(eNull.toString().contains("NoInitialContextException"), "toString contains simple name");
        check(e2.fillInStackTrace() == e2 && "boom".equals(e2.getMessage()), "fillInStackTrace keeps message");
        check(e instanceof java.io.Serializable, "Serializable");
        check(e instanceof javax.naming.NamingException, "NamingException hierarchy");
    }

    static void dExcNameChain() throws Exception {
        javax.naming.NoInitialContextException e = new javax.naming.NoInitialContextException("chain");
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
        javax.naming.NoInitialContextException e = new javax.naming.NoInitialContextException("root");
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
        javax.naming.NoInitialContextException e2 = new javax.naming.NoInitialContextException("init");
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
        javax.naming.NoInitialContextException e = new javax.naming.NoInitialContextException("msg");
        check(e.toString().contains("msg"), "toString contains message");
        check(!e.toString().contains("remaining name"), "no remaining name section when unset");
        e.setRemainingName(new javax.naming.CompositeName("r/s"));
        check(e.toString().contains("remaining name 'r/s'"), "toString contains remaining name");
        check(e.toString().contains("NoInitialContextException"), "toString contains class simple name");
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
        javax.naming.NoInitialContextException e = new javax.naming.NoInitialContextException("ser");
        e.setRemainingName(new javax.naming.CompositeName("a/b"));
        e.setResolvedName(new javax.naming.CompositeName("c"));
        e.setResolvedObj("obj");
        RuntimeException rt = new RuntimeException("rootser");
        e.setRootCause(rt);
        javax.naming.NoInitialContextException back = dSer(e);
        check("ser".equals(back.getMessage()), "message survives serialization");
        check(back.getRemainingName().equals(new javax.naming.CompositeName("a/b")), "remainingName survives");
        check(back.getResolvedName().equals(new javax.naming.CompositeName("c")), "resolvedName survives");
        check("obj".equals(back.getResolvedObj()), "resolvedObj survives");
        check(back.getRootCause() != null && "rootser".equals(back.getRootCause().getMessage()),
                "rootCause survives");
        check(back instanceof javax.naming.NoInitialContextException, "deserialized type");
    }

    static void dExcTriggerNOICTX() throws Exception {
        // No factory configured: NamingManager reports the missing property name
        boolean threw = false;
        try {
            javax.naming.spi.NamingManager.getInitialContext(new java.util.Hashtable<Object, Object>());
        } catch (javax.naming.NoInitialContextException e) {
            threw = true;
            check(e.getMessage() != null && e.getMessage().contains("java.naming.factory.initial"),
                    "message names the missing property");
        }
        check(threw, "empty env -> NoInitialContextException");
        threw = false;
        java.util.Hashtable<Object, Object> bad = new java.util.Hashtable<>();
        bad.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, "no.such.Factory");
        try {
            javax.naming.spi.NamingManager.getInitialContext(bad);
        } catch (javax.naming.NoInitialContextException e) {
            threw = true;
            check(e.getMessage().contains("no.such.Factory"), "message names unloadable class");
            check(e.getRootCause() != null, "root cause carries ClassNotFoundException");
        }
        check(threw, "unloadable factory -> NoInitialContextException");
    }


}

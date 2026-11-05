/*
 * @test
 * @summary javax.naming.NamingException conformance: full contract coverage.
 *          constructors, resolved/remaining name (clone semantics), resolved
 *          obj, explanation, appendRemainingComponent/Name (null no-op),
 *          rootCause/cause chain (setRootCause(this) no-op, initCause, getCause
 *          alias), and toString incl. root cause + remaining name.
 * @run main NamingExceptionTest
 */

import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.CompoundName;

public class NamingExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() {
        NamingException e = new NamingException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof Exception, "is Exception");
        check(e.getRemainingName() == null, "remainingName null");
        check(e.getRootCause() == null, "rootCause null");
        Exception cause = new Exception("cause");
        e.setRootCause(cause);
        check(e.getRootCause() == cause, "setRootCause");
        NamingException e0 = new NamingException();
        check(e0.getMessage() == null, "no-arg message null");
    }

    static void testResolvedRemainingNames() throws Exception {
        NamingException e = new NamingException();
        Name resolved = new CompositeName("a/b");
        e.setResolvedName(resolved);
        check(e.getResolvedName().equals(resolved), "setResolvedName");
        check(e.getResolvedName() != resolved, "setResolvedName clones");

        Name remaining = new CompositeName("c/d");
        e.setRemainingName(remaining);
        check(e.getRemainingName().equals(remaining), "setRemainingName");
        check(e.getRemainingName() != remaining, "setRemainingName clones");

        e.setResolvedName(null);
        check(e.getResolvedName() == null, "setResolvedName(null)");
        e.setRemainingName(null);
        check(e.getRemainingName() == null, "setRemainingName(null)");
    }

    static void testResolvedObjAndExplanation() {
        NamingException e = new NamingException();
        Object obj = new Object();
        e.setResolvedObj(obj);
        check(e.getResolvedObj() == obj, "setResolvedObj/getResolvedObj");
        check(e.getExplanation() == null, "explanation null by default");
    }

    static void testAppendRemaining() throws Exception {
        NamingException e = new NamingException();
        e.appendRemainingComponent(null);
        check(e.getRemainingName() == null, "appendRemainingComponent(null) no-op");
        e.appendRemainingComponent("x");
        e.appendRemainingComponent("y");
        check("x/y".equals(e.getRemainingName().toString()), "appendRemainingComponent appends");

        NamingException e2 = new NamingException();
        e2.appendRemainingName(null);
        check(e2.getRemainingName() == null, "appendRemainingName(null) no-op");
        e2.appendRemainingName(new CompositeName("a/b"));
        e2.appendRemainingName(new CompositeName("c"));
        check("a/b/c".equals(e2.getRemainingName().toString()), "appendRemainingName appends");
    }

    static void testRootCauseChain() {
        NamingException e = new NamingException("boom");
        e.setRootCause(e);
        check(e.getRootCause() == null, "setRootCause(this) no-op");
        check(e.getCause() == null, "getCause alias null");

        Exception root = new Exception("root");
        e.setRootCause(root);
        check(e.getRootCause() == root && e.getCause() == root, "rootCause and cause alias");

        NamingException e2 = new NamingException("x");
        e2.initCause(root);
        check(e2.getCause() == root, "initCause sets cause");
        check(e2.getRootCause() == root, "initCause sets rootCause");
    }

    static void testToString() throws Exception {
        NamingException e = new NamingException("boom");
        e.setRootCause(new Exception("root"));
        e.setRemainingName(new CompositeName("a/b"));
        String s = e.toString();
        check(s.contains("boom"), "toString message");
        check(s.contains("Root exception is"), "toString root cause");
        check(s.contains("remaining name 'a/b'"), "toString remaining name");
        check(e.toString(true) != null, "toString(detail)");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testResolvedRemainingNames();
        testResolvedObjAndExplanation();
        testAppendRemaining();
        testRootCauseChain();
        testToString();
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dNeAppendChain();
        dNeCauseChain();
        dNeStringForms();
        dNeSubclassFields();
        dNeSerialization();
        System.out.println("deep: NamingException contracts OK");
    }

    static void dNeAppendChain() throws Exception {
        // appendRemainingComponent creates a CompositeName when none exists
        javax.naming.NamingException e = new javax.naming.NamingException("chain");
        e.appendRemainingComponent("a");
        check("a".equals(e.getRemainingName().toString()), "appendRemainingComponent creates name");
        e.appendRemainingComponent("b");
        check("a/b".equals(e.getRemainingName().toString()), "appendRemainingComponent appends");
        e.appendRemainingComponent(null);
        check("a/b".equals(e.getRemainingName().toString()), "appendRemainingComponent(null) no-op");
        // appendRemainingName: null no-op, empty-remaining clones, non-empty adds in place
        javax.naming.NamingException e2 = new javax.naming.NamingException();
        e2.appendRemainingName(null);
        check(e2.getRemainingName() == null, "appendRemainingName(null) no-op");
        javax.naming.Name src = new javax.naming.CompositeName("x/y");
        e2.appendRemainingName(src);
        check("x/y".equals(e2.getRemainingName().toString()), "appendRemainingName clones when empty");
        src.add("mutated");
        check("x/y".equals(e2.getRemainingName().toString()), "cloned copy is detached from source");
        e2.appendRemainingName(new javax.naming.CompositeName("z"));
        check("x/y/z".equals(e2.getRemainingName().toString()), "appendRemainingName adds components");
        // appendRemainingName with a CompoundName into a Composite remaining fails
        java.util.Properties p = new java.util.Properties();
        p.setProperty("jndi.syntax.direction", "left_to_right");
        p.setProperty("jndi.syntax.separator", ".");
        javax.naming.CompoundName compound = new javax.naming.CompoundName("m.n", p);
        boolean iae = false;
        try { e2.appendRemainingName(compound); }
        catch (IllegalArgumentException x) { iae = true; }
        check(iae, "appendRemainingName(compound into composite) -> IllegalArgumentException");
    }

    static void dNeCauseChain() {
        javax.naming.NamingException e = new javax.naming.NamingException("cause-chain");
        check(e.getCause() == null && e.getRootCause() == null, "no cause by default");
        IllegalStateException rc = new IllegalStateException("rc");
        e.setRootCause(rc);
        check(e.getRootCause() == rc, "setRootCause stores");
        check(e.getCause() == rc, "getCause aliases getRootCause");
        // self-cause is silently ignored, previous value retained
        e.setRootCause(e);
        check(e.getRootCause() == rc, "setRootCause(this) ignored");
        // setRootCause(null) clears
        e.setRootCause(null);
        check(e.getRootCause() == null, "setRootCause(null) clears");
        // initCause updates both views
        IllegalStateException ic = new IllegalStateException("ic");
        javax.naming.NamingException e2 = new javax.naming.NamingException();
        javax.naming.NamingException ret = (javax.naming.NamingException) e2.initCause(ic);
        check(ret == e2, "initCause returns this");
        check(e2.getRootCause() == ic, "initCause updates rootCause");
        check(e2.getCause() == ic, "initCause updates cause");
    }

    static void dNeStringForms() throws Exception {
        javax.naming.NamingException e = new javax.naming.NamingException("boom");
        check("javax.naming.NamingException: boom".equals(e.toString()), "toString class + explanation");
        javax.naming.NamingException bare = new javax.naming.NamingException();
        check("javax.naming.NamingException".equals(bare.toString()), "toString without explanation");
        e.setRootCause(new IllegalStateException("rc"));
        check(e.toString().contains(" [Root exception is java.lang.IllegalStateException: rc]"),
                "toString embeds root exception");
        e.appendRemainingComponent("a");
        check(e.toString().endsWith("; remaining name 'a'"), "toString appends remaining name");
        // toString(boolean): resolved object only when detail is requested
        e.setResolvedObj("ROBJ");
        check(e.toString(false).equals(e.toString()), "toString(false) == toString()");
        check(e.toString(true).equals(e.toString() + "; resolved object ROBJ"),
                "toString(true) appends resolved object");
        javax.naming.NamingException noObj = new javax.naming.NamingException("x");
        check(noObj.toString(true).equals(noObj.toString()), "toString(true) without obj == toString()");
        check("boom".equals(e.getExplanation()), "explanation == message");
    }

    static class DeepNamingException extends javax.naming.NamingException {
        DeepNamingException(String m) { super(m); }
        javax.naming.Name peekRemaining() { return remainingName; }
        javax.naming.Name peekResolved() { return resolvedName; }
        Object peekResolvedObj() { return resolvedObj; }
    }

    static void dNeSubclassFields() {
        // protected fields are visible to subclasses and default to null
        DeepNamingException d = new DeepNamingException("sub");
        check(d.peekRemaining() == null && d.peekResolved() == null && d.peekResolvedObj() == null,
                "protected fields default null");
        d.setResolvedObj(new Object());
        check(d.peekResolvedObj() != null, "resolvedObj visible to subclass");
        // subclass keeps the NamingException contract
        check(d.toString().contains("DeepNamingException") && d.toString().endsWith("sub"),
                "subclass toString names itself");
    }

    static void dNeSerialization() throws Exception {
        javax.naming.NamingException e = new javax.naming.NamingException("ser");
        e.setRemainingName(new javax.naming.CompositeName("r/s"));
        e.setResolvedName(new javax.naming.CompositeName("p"));
        e.setResolvedObj("OBJ");
        e.setRootCause(new IllegalStateException("deep"));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) { oos.writeObject(e); }
        javax.naming.NamingException back;
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            back = (javax.naming.NamingException) ois.readObject();
        }
        check("ser".equals(back.getMessage()), "message survives");
        check("r/s".equals(back.getRemainingName().toString()), "remainingName survives");
        check("p".equals(back.getResolvedName().toString()), "resolvedName survives");
        check("OBJ".equals(back.getResolvedObj()), "resolvedObj survives");
        check(back.getRootCause() instanceof IllegalStateException
                && "deep".equals(back.getRootCause().getMessage()), "rootCause survives");
        // serialVersionUID locked across JDK versions
        check(java.io.ObjectStreamClass.lookup(javax.naming.NamingException.class)
                .getSerialVersionUID() == -1299181962103167177L, "serialVersionUID locked");
    }

}

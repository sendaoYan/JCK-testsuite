/*
 * @test
 * @summary javax.naming.NamingException conformance: white-box structure + black-box behavior.
 * @run main NamingExceptionTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.CompoundName;

public class NamingExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = NamingException.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == Exception.class, "extends Exception");
        for (String m : new String[] {"getResolvedName", "getRemainingName", "getResolvedObj",
                "setResolvedName", "setRemainingName", "setResolvedObj", "appendRemainingComponent",
                "appendRemainingName", "getRootCause", "setRootCause", "getCause", "initCause",
                "getExplanation", "toString"})
            check(method(c, m), "declares " + m);
        check(c.getMethod("toString", boolean.class) != null, "toString(boolean)");
        Constructor<?>[] cs = c.getDeclaredConstructors();
        boolean noArg = false, strArg = false;
        for (Constructor<?> k : cs) {
            if (k.getParameterCount() == 0) noArg = true;
            if (k.getParameterCount() == 1 && k.getParameterTypes()[0] == String.class) strArg = true;
        }
        check(noArg && strArg, "both ctors");
    }
    static boolean method(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        NamingException ne = new NamingException("boom");
        check(ne.getMessage().equals("boom"), "message");
        check(ne.getExplanation().equals("boom"), "explanation == message");
        check(new NamingException().getExplanation() == null, "null explanation");

        // resolved/remaining name and object plumbing
        Name src = new CompositeName("a/b");
        ne.setRemainingName(src);
        check(ne.getRemainingName().toString().equals("a/b"), "remaining set");
        check(ne.getRemainingName() != src, "remaining name is a clone");
        ne.setResolvedName(new CompositeName("r"));
        check(ne.getResolvedName().get(0).equals("r"), "resolved set");
        ne.setResolvedObj("o");
        check("o".equals(ne.getResolvedObj()), "resolved obj");
        ne.setRemainingName(null);
        check(ne.getRemainingName() == null, "null remaining clears");

        // append chains
        NamingException ap = new NamingException();
        ap.appendRemainingComponent("x");
        ap.appendRemainingComponent("y");
        check(ap.getRemainingName().toString().equals("x/y"), "appendRemainingComponent");
        ap.appendRemainingName(new CompositeName("p/q"));
        check(ap.getRemainingName().toString().equals("x/y/p/q"), "appendRemainingName");

        // root cause interplay with Throwable cause
        RuntimeException rc = new RuntimeException("rc");
        ne.setRootCause(rc);
        check(ne.getRootCause() == rc, "rootCause stored");
        check(ne.getCause() == rc, "cause mirrors rootCause");
        NamingException ic = new NamingException("i");
        ic.initCause(rc);
        check(ic.getRootCause() == rc && ic.getCause() == rc, "initCause sets rootCause");
        try { ic.initCause(new RuntimeException("second")); throw new AssertionError("double initCause"); }
        catch (IllegalStateException expected) { check(true, "initCause twice throws ISE"); }
        check(new NamingException().getRootCause() == null, "no rootCause by default");

        // toString composition
        NamingException t = new NamingException("m");
        t.setRemainingName(new CompositeName("a/b"));
        t.setRootCause(rc);
        String s = t.toString();
        check(s.contains("javax.naming.NamingException: m"), "toString header");
        check(s.contains("[Root exception is java.lang.RuntimeException: rc]"), "toString root exception");
        check(s.contains("remaining name 'a/b'"), "toString remaining name");
        String brief = t.toString(false);
        check(brief.contains("m"), "toString(false) keeps message");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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

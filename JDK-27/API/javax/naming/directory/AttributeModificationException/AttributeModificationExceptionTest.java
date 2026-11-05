/*
 * @test
 * @summary javax.naming.directory.AttributeModificationException conformance: white-box structure + black-box behavior.
 * @run main AttributeModificationExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.CompositeName;
import javax.naming.NamingException;
import javax.naming.directory.AttributeModificationException;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.ModificationItem;
import java.util.Arrays;
import javax.naming.directory.DirContext;

public class AttributeModificationExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.directory.AttributeModificationException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == NamingException.class, "extends NamingException");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 0), "no-arg ctor");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 1 && x.getParameterTypes()[0] == String.class), "ctor(String)");
        for (String m : new String[]{"setUnexecutedModifications", "getUnexecutedModifications", "toString"}) {
            boolean found = false;
            for (Method mm : c.getMethods()) if (mm.getName().equals(m)) found = true;
            check(found, m + " present");
        }
    }

    static void testBlackBox() {
        AttributeModificationException e = new AttributeModificationException("failed");
        check("failed".equals(e.getMessage()), "getMessage");
        check(e.getUnexecutedModifications() == null, "unexecuted default null");

        ModificationItem[] items = {
            new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("cn", "x")),
            new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute("sn", "y"))
        };
        e.setUnexecutedModifications(items);
        ModificationItem[] got = e.getUnexecutedModifications();
        check(got != null, "unexecuted not null");
        check(got.length == 2, "unexecuted length");
        check(got[0].getModificationOp() == DirContext.ADD_ATTRIBUTE, "item0 op");
        check(got[1].getModificationOp() == DirContext.REMOVE_ATTRIBUTE, "item1 op");
        check("cn".equals(got[0].getAttribute().getID()), "item0 attr id");

        AttributeModificationException e2 = new AttributeModificationException();
        check(e2.getMessage() == null, "null message");
        check(e2 instanceof NamingException, "instanceof NamingException");
        check(e.toString().contains("failed"), "toString contains message");
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
        dExcTriggerATTRMOD();

        System.out.println("deep: AttributeModificationException contracts OK");
    }

    static void dExcConstructors() throws Exception {
        javax.naming.directory.AttributeModificationException e = new javax.naming.directory.AttributeModificationException();
        check(e.getMessage() == null, "no-arg message null");
        check(e.getExplanation() == null, "no-arg explanation null");
        check(e.getRemainingName() == null && e.getResolvedName() == null && e.getResolvedObj() == null,
                "no-arg name/obj fields null");
        check(e.getRootCause() == null && e.getCause() == null, "no-arg cause null");
        javax.naming.directory.AttributeModificationException e2 = new javax.naming.directory.AttributeModificationException("boom");
        check("boom".equals(e2.getMessage()), "(String) message");
        check("boom".equals(e2.getExplanation()), "explanation == message");
        javax.naming.directory.AttributeModificationException eNull = new javax.naming.directory.AttributeModificationException(null);
        check(eNull.getMessage() == null, "(null) message null");
        check(eNull.toString().contains("AttributeModificationException"), "toString contains simple name");
        check(e2.fillInStackTrace() == e2 && "boom".equals(e2.getMessage()), "fillInStackTrace keeps message");
        check(e instanceof java.io.Serializable, "Serializable");
        check(e instanceof javax.naming.NamingException, "NamingException hierarchy");
    }

    static void dExcNameChain() throws Exception {
        javax.naming.directory.AttributeModificationException e = new javax.naming.directory.AttributeModificationException("chain");
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
        javax.naming.directory.AttributeModificationException e = new javax.naming.directory.AttributeModificationException("root");
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
        javax.naming.directory.AttributeModificationException e2 = new javax.naming.directory.AttributeModificationException("init");
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
        javax.naming.directory.AttributeModificationException e = new javax.naming.directory.AttributeModificationException("msg");
        check(e.toString().contains("msg"), "toString contains message");
        check(!e.toString().contains("remaining name"), "no remaining name section when unset");
        e.setRemainingName(new javax.naming.CompositeName("r/s"));
        check(e.toString().contains("remaining name 'r/s'"), "toString contains remaining name");
        check(e.toString().contains("AttributeModificationException"), "toString contains class simple name");
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
        javax.naming.directory.AttributeModificationException e = new javax.naming.directory.AttributeModificationException("ser");
        e.setRemainingName(new javax.naming.CompositeName("a/b"));
        e.setResolvedName(new javax.naming.CompositeName("c"));
        e.setResolvedObj("obj");
        RuntimeException rt = new RuntimeException("rootser");
        e.setRootCause(rt);
        javax.naming.directory.AttributeModificationException back = dSer(e);
        check("ser".equals(back.getMessage()), "message survives serialization");
        check(back.getRemainingName().equals(new javax.naming.CompositeName("a/b")), "remainingName survives");
        check(back.getResolvedName().equals(new javax.naming.CompositeName("c")), "resolvedName survives");
        check("obj".equals(back.getResolvedObj()), "resolvedObj survives");
        check(back.getRootCause() != null && "rootser".equals(back.getRootCause().getMessage()),
                "rootCause survives");
        check(back instanceof javax.naming.directory.AttributeModificationException, "deserialized type");
    }

    static void dExcTriggerATTRMOD() throws Exception {
        javax.naming.directory.AttributeModificationException e =
                new javax.naming.directory.AttributeModificationException("mod failed");
        check(e.getUnexecutedModifications() == null, "default unexecuted null");
        javax.naming.directory.ModificationItem[] items = {
            new javax.naming.directory.ModificationItem(javax.naming.directory.DirContext.ADD_ATTRIBUTE,
                    new javax.naming.directory.BasicAttribute("cn")),
            new javax.naming.directory.ModificationItem(javax.naming.directory.DirContext.REMOVE_ATTRIBUTE,
                    new javax.naming.directory.BasicAttribute("sn")),
        };
        e.setUnexecutedModifications(items);
        check(java.util.Arrays.equals(items, e.getUnexecutedModifications()), "unexecuted round trip same order");
        check(e.getUnexecutedModifications().length == 2, "unexecuted length");
        check(e.toString().contains("mod failed"), "toString message");
        e.setUnexecutedModifications(null);
        check(e.getUnexecutedModifications() == null, "clear unexecuted");
    }


}

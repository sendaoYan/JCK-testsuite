/*
 * @test
 * @summary jdk.dynalink.NoSuchDynamicMethodException conformance: white-box structure + black-box behavior.
 * @run main NoSuchDynamicMethodExceptionTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class NoSuchDynamicMethodExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.NoSuchDynamicMethodException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(RuntimeException.class.isAssignableFrom(c), "extends RuntimeException");
        check(hasMethod(c, "getMessage"), "getMessage");

    }

    static void testBlackBox() throws Exception {
        NoSuchDynamicMethodException e = new NoSuchDynamicMethodException("msg");
        check(e.getMessage().equals("msg"), "message");
        check(e instanceof RuntimeException, "runtime exception");
        NoSuchDynamicMethodException e2 = new NoSuchDynamicMethodException("second");
        check(e2.getMessage().equals("second"), "second message");
        check(!e2.getMessage().equals(e.getMessage()), "distinct instances");

    }


    // ---- shared fixtures for the deep contract sections ----
    public static class POJO {
        public int field = 7;
        public String getName() { return "name-" + field; }
        public String greet(String who) { return "hi " + who; }
    }
    static DynamicLinker beanLinker() {
        DynamicLinkerFactory f = new DynamicLinkerFactory();
        f.setPrioritizedLinker(new jdk.dynalink.beans.BeansLinker());
        f.setFallbackLinkers();
        return f.createLinker();
    }
    static SimpleRelinkableCallSite site(MethodHandles.Lookup lk, Operation op, MethodType mt) {
        return new SimpleRelinkableCallSite(new CallSiteDescriptor(lk, op, mt));
    }

    static void testDeepContract() throws Throwable {
        NoSuchDynamicMethodException e = new NoSuchDynamicMethodException("msg");
        check(e instanceof RuntimeException, "RuntimeException");
        check(e.getMessage().equals("msg"), "message kept");
        check(e.getCause() == null, "no cause channel");
        check(java.io.ObjectStreamClass.lookup(NoSuchDynamicMethodException.class).getSerialVersionUID() == 1L,
              "serialVersionUID is 1");
        // the engine throws it for an unlinkable call site, with the descriptor as the message
        DynamicLinker linker = beanLinker();
        SimpleRelinkableCallSite cs = site(MethodHandles.lookup(),
            StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).named("missing"),
            MethodType.methodType(Object.class, Object.class));
        linker.link(cs);
        try {
            cs.getTarget().invoke((Object) new POJO());
            check(false, "unlinkable site must throw");
        } catch (NoSuchDynamicMethodException thrown) {
            check(thrown.getMessage().startsWith("GET:PROPERTY:missing(Object)Object@"), "message is the descriptor: " + thrown.getMessage());
        }
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

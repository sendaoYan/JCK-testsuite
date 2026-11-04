/*
 * @test
 * @summary java.awt.image.ImagingOpException conformance: white-box structure plus black-box behavior.
 * @run main ImagingOpExceptionTest
 */

import java.awt.image.ImagingOpException;
import java.lang.reflect.*;

public class ImagingOpExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.ImagingOpException");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(RuntimeException.class.isAssignableFrom(c), "extends RuntimeException");
        check(!Exception.class.isAssignableFrom(c) || RuntimeException.class.isAssignableFrom(c), "unchecked");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ImagingOpException e = new ImagingOpException("bad op");
        check(e.getMessage().equals("bad op"), "getMessage");
        check(e instanceof RuntimeException, "instanceof RuntimeException");
        check(e instanceof Throwable, "instanceof Throwable");
        check(e.getCause() == null, "no cause by default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        passed += ImagingOpExceptionDeepContract.run();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class ImagingOpExceptionDeepContract {
    // Deepened contract coverage ported from the JDK-8 mirror (JDK-27 divergences adjusted).
    static int passed;



    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }



    static void throwIt() {   // no throws clause needed
        throw new ImagingOpException("unchecked");
    }

    static int run() throws Exception {

                Class<?> c = Class.forName("java.awt.image.ImagingOpException");
                check(!c.isInterface() && !c.isEnum(), "is class");
                check(c.getSuperclass().getName().equals("java.lang.RuntimeException"), "superclass java.lang.RuntimeException");
                check(c.getInterfaces().length == 0, "no direct interfaces");
                check(c.getDeclaredConstructors().length >= 1, "has constructors");
                // Message-only constructor.
                ImagingOpException e = new ImagingOpException("op failed");
                check("op failed".equals(e.getMessage()), "message preserved");
                check(e.getCause() == null, "no cause by default");
                // Unchecked: no throws clause needed.
                boolean threw = false;
                try { throwIt(); } catch (ImagingOpException ex) { threw = true; }
                check(threw, "thrown without throws declaration (unchecked)");
                // Serializable round trip keeps the message.
                java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
                try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
                    oos.writeObject(e);
                }
                Object back = new java.io.ObjectInputStream(
                        new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
                check(back instanceof ImagingOpException, "round trip type");
                check("op failed".equals(((ImagingOpException) back).getMessage()),
                      "round trip message preserved");

        return passed;
    }
}

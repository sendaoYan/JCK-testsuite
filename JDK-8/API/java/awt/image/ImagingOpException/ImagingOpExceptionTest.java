/*
 * @test
 * @summary java.awt.image.ImagingOpException conformance: class reflection + message contract.
 * @run main ImagingOpExceptionTest
 */

import java.awt.image.ImagingOpException;

public class ImagingOpExceptionTest {
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

    public static void main(String[] args) throws Exception {
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
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void throwIt() {   // no throws clause needed
        throw new ImagingOpException("unchecked");
    }
}

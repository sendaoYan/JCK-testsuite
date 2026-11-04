/*
 * @test
 * @summary java.awt.image.RasterFormatException conformance: class reflection + message contract.
 * @run main RasterFormatExceptionTest
 */

import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;

public class RasterFormatExceptionTest {
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
        Class<?> c = Class.forName("java.awt.image.RasterFormatException");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.RuntimeException"), "superclass java.lang.RuntimeException");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        // Message-only constructor.
        RasterFormatException e = new RasterFormatException("bad grid");
        check("bad grid".equals(e.getMessage()), "message preserved");
        check(e.getCause() == null, "no cause by default");
        // Unchecked: can be thrown without a throws clause.
        boolean threw = false;
        try { throwIt(); } catch (RasterFormatException ex) { threw = true; }
        check(threw, "thrown without throws declaration (unchecked)");
        // Real trigger: a child raster that does not fit the parent.
        BufferedImage bi = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);
        threw = false;
        try { bi.getSubimage(1, 1, 5, 5); }
        catch (RasterFormatException ex) { threw = true; }
        check(threw, "getSubimage beyond bounds -> RasterFormatException");
        // Serializable like every Throwable.
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(new RasterFormatException("ser"));
        }
        Object back = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof RasterFormatException, "round trip type");
        check("ser".equals(((RasterFormatException) back).getMessage()), "round trip message");
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void throwIt() {   // no throws clause needed
        throw new RasterFormatException("unchecked");
    }
}

/*
 * @test
 * @summary javax.imageio.spi.RegisterableService conformance: white-box structure + black-box behavior.
 * @run main RegisterableServiceTest
 */

import java.lang.reflect.Modifier;
import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.RegisterableService;
import javax.imageio.spi.ServiceRegistry;

public class RegisterableServiceTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.spi.RegisterableService");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface(), "is interface");
        check(!c.isAnnotation(), "not annotation");
        check(!c.isEnum(), "not enum");
        check(Modifier.isPublic(mods), "public");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getDeclaredMethods().length == 2, "two declared methods");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // A concrete IIOServiceProvider is also a RegisterableService.
        RegisterableService svc =
            (RegisterableService) ImageIO.getImageWritersByFormatName("png").next().getOriginatingProvider();
        ServiceRegistry reg = IIORegistry.getDefaultInstance();
        svc.onRegistration(reg, ImageWriterSpi.class);
        check(true, "onRegistration returns normally");
        svc.onDeregistration(reg, ImageWriterSpi.class);
        check(true, "onDeregistration returns normally");

        // direct functional use of the interface
        final int[] calls = new int[2];
        RegisterableService rs = new RegisterableService() {
            public void onRegistration(ServiceRegistry r, Class<?> c) { calls[0]++; }
            public void onDeregistration(ServiceRegistry r, Class<?> c) { calls[1]++; }
        };
        rs.onRegistration(reg, ImageWriterSpi.class);
        rs.onDeregistration(reg, ImageWriterSpi.class);
        check(calls[0] == 1, "onRegistration dispatched once");
        check(calls[1] == 1, "onDeregistration dispatched once");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.imageio.metadata.IIOMetadataController conformance: white-box structure + black-box behavior.
 * @run main IIOMetadataControllerTest
 */

import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataController;

public class IIOMetadataControllerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class RecordingController implements IIOMetadataController {
        final boolean result;
        int calls;
        IIOMetadata received;
        RecordingController(boolean result) { this.result = result; }
        public boolean activate(IIOMetadata metadata) {
            calls++;
            received = metadata;
            return result;
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.metadata.IIOMetadataController");
        check(c != null, "loads");
        check(c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public modifier");
        check(Modifier.isAbstract(c.getModifiers()), "abstract modifier");
        Method m = c.getMethod("activate", IIOMetadata.class);
        check(m.getReturnType() == boolean.class, "activate returns boolean");
        check(Modifier.isAbstract(m.getModifiers()), "activate is abstract");
        // single abstract method -> usable as a lambda/functional interface
        check(c.getMethods().length == 1, "exactly one public method");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        IIOMetadata md = createPngMetadata();

        RecordingController cfalse = new RecordingController(false);
        check(cfalse.activate(md) == false, "activate returns false as configured");
        check(cfalse.calls == 1 && cfalse.received == md, "activate invoked once with the metadata");

        RecordingController ctrue = new RecordingController(true);
        check(ctrue.activate(md) == true, "activate returns true as configured");
        check(ctrue.calls == 1 && ctrue.received == md, "activate invoked once with the metadata (true)");

        // Functional-interface style: the interface has a single abstract method.
        IIOMetadataController lambda = m -> true;
        check(lambda.activate(md) == true, "lambda controller returns true");
        lambda = m -> false;
        check(lambda.activate(md) == false, "lambda controller returns false");
    }

    static IIOMetadata createPngMetadata() throws Exception {
        BufferedImage bi = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        ImageTypeSpecifier its = ImageTypeSpecifier.createFromRenderedImage(bi);
        ImageWriter w = ImageIO.getImageWritersByFormatName("png").next();
        return w.getDefaultImageMetadata(its, null);
    }

    // ---- Part 3: recording contract, exception relay, wiring details (probed) ----
    static class CountingController implements IIOMetadataController {
        int calls;
        IIOMetadata last;
        public boolean activate(IIOMetadata metadata) { calls++; last = metadata; return calls < 2; }
    }
    static void testDeepBehavior() throws Exception {
        IIOMetadata md = createPngMetadata();

        // repeated activation and return-value variation driven by implementation state
        CountingController cc = new CountingController();
        check(cc.activate(md), "first activation returns true");
        check(!cc.activate(md), "second activation returns false");
        check(cc.calls == 2, "both activations recorded");
        check(cc.last == md, "last activation recorded the metadata");

        // activate(null) simply reaches the implementation
        RecordingController nullArg = new RecordingController(true);
        check(nullArg.activate(null), "activate(null) reaches the implementation");
        check(nullArg.calls == 1 && nullArg.received == null, "activate(null) recorded as null");

        // controller instances are independent
        RecordingController r1 = new RecordingController(false);
        RecordingController r2 = new RecordingController(false);
        r1.activate(md);
        check(r1.calls == 1 && r2.calls == 0, "controller instances independent");

        // exception relay: the exact exception object propagates
        final RuntimeException marker = new RuntimeException("marker");
        IIOMetadataController throwing = m -> { throw marker; };
        try {
            throwing.activate(md);
            check(false, "throwing controller should propagate");
        } catch (RuntimeException e) {
            check(e == marker, "exact marker exception relayed by activate");
        }

        // wiring: activateController passes the metadata object itself
        final IIOMetadata self = md;
        final IIOMetadata[] received = new IIOMetadata[1];
        final int[] calls = new int[1];
        md.setController(m -> { calls[0]++; received[0] = m; return false; });
        check(md.getController() != null && md.hasController(), "controller installed");
        check(md.activateController() == false, "activateController relays false");
        check(calls[0] == 1, "wired controller invoked exactly once");
        check(received[0] == self, "activateController passes this");
        // exception thrown inside a wired controller propagates out of activateController
        md.setController(throwing);
        try {
            md.activateController();
            check(false, "exception should propagate through activateController");
        } catch (RuntimeException e) {
            check(e == marker, "marker relayed through activateController");
        }
        md.setController(null);
        check(!md.hasController(), "controller cleared");
        // a controller may be shared by several metadata objects
        IIOMetadata md2 = createPngMetadata();
        RecordingController shared = new RecordingController(true);
        md.setController(shared);
        md2.setController(shared);
        md.activateController();
        md2.activateController();
        check(shared.calls == 2, "shared controller counts every activation");
        md.setController(null);
        md2.setController(null);

        // structural extras
        Class<?> c = IIOMetadataController.class;
        check(c.getDeclaredAnnotations().length == 0, "no annotations on the interface");
        check(!c.isAnnotationPresent(java.lang.FunctionalInterface.class),
              "not marked @FunctionalInterface (still SAM-shaped)");
        check(c.getMethods().length == 1, "exactly one public method overall");
        IIOMetadataController anon = new IIOMetadataController() {
            public boolean activate(IIOMetadata m) { return false; }
        };
        check(anon instanceof IIOMetadataController, "anonymous implementation allowed");
        check(!anon.activate(md), "anonymous implementation behaves");
        check(c.getDeclaredFields().length == 0, "no fields on the interface");
        java.lang.reflect.Method act = c.getMethod("activate", IIOMetadata.class);
        check(act.getDeclaringClass() == c, "activate declared by the interface itself");
        check(act.getParameterTypes().length == 1, "activate takes exactly one parameter");
        check(act.getExceptionTypes().length == 0, "activate declares no checked exceptions");
        // method-reference implementation form
        IIOMetadataController mr = IIOMetadataControllerTest::alwaysTrue;
        check(mr.activate(md), "method-reference controller works");
        // default-controller channel on a plugin metadata object
        check(md.getDefaultController() == null, "png metadata has no default controller");
        md.setController(md.getDefaultController());
        check(!md.hasController(), "restoring null default leaves no controller");
    }

    static boolean alwaysTrue(IIOMetadata m) { return true; }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

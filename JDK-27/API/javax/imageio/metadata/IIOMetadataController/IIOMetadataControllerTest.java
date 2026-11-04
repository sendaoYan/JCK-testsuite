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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

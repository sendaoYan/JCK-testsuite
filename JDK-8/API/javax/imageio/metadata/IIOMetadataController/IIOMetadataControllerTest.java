/*
 * @test
 * @summary javax.imageio.metadata.IIOMetadataController conformance: interface hierarchy and methods.
 * @run main IIOMetadataControllerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataController;

public class IIOMetadataControllerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    /** Controller that records every activation. */
    static class RecordingController implements IIOMetadataController {
        final boolean result;
        int calls;
        IIOMetadata lastReceived;
        RecordingController(boolean result) { this.result = result; }
        public boolean activate(IIOMetadata metadata) {
            calls++;
            lastReceived = metadata;
            return result;
        }
    }

    /** Controller that relays an exception unchanged. */
    static class ThrowingController implements IIOMetadataController {
        final RuntimeException marker = new RuntimeException("marker");
        public boolean activate(IIOMetadata metadata) { throw marker; }
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.metadata.IIOMetadataController");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("activate"), "method activate");
        testStructure(c);
        testBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testStructure(Class<?> c) throws Exception {
        check(!c.isAnnotation(), "not an annotation");
        check(!c.isEnum(), "not an enum");
        check(c.getDeclaredFields().length == 0, "no fields");
        check(c.getDeclaredMethods().length == 1, "exactly one declared method");
        Method m = c.getMethod("activate", IIOMetadata.class);
        check(Modifier.isPublic(m.getModifiers()), "activate public");
        check(Modifier.isAbstract(m.getModifiers()), "activate abstract");
        check(m.getReturnType() == boolean.class, "activate returns boolean");
        check(m.getParameterTypes().length == 1, "activate has one parameter");
        check(m.getParameterTypes()[0] == IIOMetadata.class, "activate takes IIOMetadata");
        check(m.getExceptionTypes().length == 0, "activate declares no checked exceptions");
        check(c.getMethods().length == 1, "exactly one public method overall");
        check(c.getDeclaredAnnotations().length == 0, "no annotations on the interface");
        check(!c.isAnnotationPresent(java.lang.FunctionalInterface.class),
              "not marked @FunctionalInterface (still SAM-shaped)");
        // SAM shape: usable as the target of a lambda expression
        IIOMetadataController lambda = md -> true;
        check(lambda != null, "lambda instance creatable");
        check(lambda instanceof IIOMetadataController, "lambda instance of the interface");
        IIOMetadataController anon = new IIOMetadataController() {
            public boolean activate(IIOMetadata m) { return false; }
        };
        check(anon instanceof IIOMetadataController, "anonymous implementation allowed");
        check(anon.activate(null) == false, "anonymous implementation behaves");
    }

    static IIOMetadata pngMetadata() throws Exception {
        BufferedImage bi = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        ImageTypeSpecifier its = ImageTypeSpecifier.createFromRenderedImage(bi);
        ImageWriter w = ImageIO.getImageWritersByFormatName("png").next();
        return w.getDefaultImageMetadata(its, null);
    }

    static void testBehavior() throws Exception {
        IIOMetadata md = pngMetadata();

        // recording implementations in both result configurations
        RecordingController falseCtrl = new RecordingController(false);
        check(falseCtrl.calls == 0, "fresh controller not yet called");
        check(falseCtrl.activate(md) == false, "activate returns false as configured");
        check(falseCtrl.calls == 1 && falseCtrl.lastReceived == md, "activate recorded the metadata instance");
        check(falseCtrl.activate(md) == false && falseCtrl.calls == 2, "activate callable repeatedly");
        RecordingController trueCtrl = new RecordingController(true);
        check(trueCtrl.activate(md), "activate returns true as configured");
        check(trueCtrl.calls == 1 && trueCtrl.lastReceived == md, "true controller recorded the call");

        // activate(null) simply reaches the implementation (the interface imposes no null contract)
        RecordingController nullArg = new RecordingController(true);
        check(nullArg.activate(null), "activate(null) reaches the implementation");
        check(nullArg.calls == 1 && nullArg.lastReceived == null, "activate(null) recorded as null");

        // two controller instances keep independent state
        RecordingController c1 = new RecordingController(false);
        RecordingController c2 = new RecordingController(false);
        c1.activate(md);
        check(c1.calls == 1 && c2.calls == 0, "controller instances independent");

        // lambda-based implementations (SAM interface)
        IIOMetadataController yes = m -> true;
        IIOMetadataController no = m -> false;
        check(yes.activate(md), "lambda controller true");
        check(!no.activate(md), "lambda controller false");

        // exception relay: the exact exception object propagates from activate
        ThrowingController tc = new ThrowingController();
        try {
            tc.activate(md);
            check(false, "throwing controller should propagate");
        } catch (RuntimeException e) {
            check(e == tc.marker, "exact marker exception relayed");
        }

        // wiring through IIOMetadata
        IIOMetadata md2 = pngMetadata();
        final IIOMetadata self = md2;
        RecordingController wired = new RecordingController(true);
        md2.setController(wired);
        check(md2.getController() == wired, "setController/getController identity");
        check(md2.hasController(), "hasController true after wiring");
        check(md2.activateController(), "activateController relays controller result");
        check(wired.calls == 1, "wired controller invoked exactly once");
        check(wired.lastReceived == self, "activateController passes the metadata object itself");
        // exception thrown inside a wired controller propagates out of activateController
        md2.setController(tc);
        try {
            md2.activateController();
            check(false, "exception should propagate through activateController");
        } catch (RuntimeException e) {
            check(e == tc.marker, "marker relayed through activateController");
        }
        md2.setController(null);
        check(!md2.hasController() && md2.getController() == null, "controller cleared");
        try {
            md2.activateController();
            check(false, "activateController without controller should throw");
        } catch (IllegalStateException e) {
            check(true, "activateController without controller throws IllegalStateException");
        }
        // controllers are plain objects: two metadata objects may share one
        IIOMetadata md3 = pngMetadata();
        md2.setController(wired);
        md3.setController(wired);
        md2.activateController();
        md3.activateController();
        check(wired.calls == 3, "shared controller counts every activation");
    }
}

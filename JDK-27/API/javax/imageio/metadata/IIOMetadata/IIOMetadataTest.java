/*
 * @test
 * @summary javax.imageio.metadata.IIOMetadata conformance: white-box structure + black-box behavior.
 * @run main IIOMetadataTest
 */

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataController;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.stream.ImageInputStream;
import org.w3c.dom.Node;

public class IIOMetadataTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void expectThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        boolean thrown = false;
        try { r.run(); } catch (Throwable t) {
            thrown = true;
            check(expected.isAssignableFrom(t.getClass()),
                  msg + " expected " + expected.getSimpleName() + " but got " + t.getClass().getSimpleName());
        }
        check(thrown, msg + " should throw " + expected.getSimpleName());
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.metadata.IIOMetadata");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is not interface");
        check(!c.isAnnotation(), "is not annotation");
        check(!c.isEnum(), "is not enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isAbstract(mods), "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"),
              "superclass is Object");
        // protected state fields
        String[] fields = {"standardFormatSupported", "nativeMetadataFormatName",
                           "nativeMetadataFormatClassName", "extraMetadataFormatNames",
                           "extraMetadataFormatClassNames", "defaultController", "controller"};
        for (String name : fields) {
            Field f = c.getDeclaredField(name);
            check(Modifier.isProtected(f.getModifiers()), "field " + name + " is protected");
        }
        // abstract methods
        check(Modifier.isAbstract(c.getMethod("isReadOnly").getModifiers()), "isReadOnly abstract");
        check(Modifier.isAbstract(c.getMethod("getAsTree", String.class).getModifiers()), "getAsTree abstract");
        check(Modifier.isAbstract(c.getMethod("mergeTree", String.class, Node.class).getModifiers()), "mergeTree abstract");
        check(Modifier.isAbstract(c.getMethod("reset").getModifiers()), "reset abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        IIOMetadata md = createPngMetadata();

        // format name discovery
        String nativeName = md.getNativeMetadataFormatName();
        check(nativeName != null && nativeName.length() > 0, "native format name non-empty");
        check(md.isStandardMetadataFormatSupported(), "standard format supported");
        check(md.getExtraMetadataFormatNames() == null, "no extra format names");

        String[] names = md.getMetadataFormatNames();
        check(names != null && names.length >= 2, "at least two format names");
        boolean hasNative = false, hasStandard = false;
        for (String n : names) {
            if (nativeName.equals(n)) hasNative = true;
            if (IIOMetadataFormatImpl.standardMetadataFormatName.equals(n)) hasStandard = true;
        }
        check(hasNative, "format names contain native");
        check(hasStandard, "format names contain javax_imageio_1.0");

        // standard format descriptor
        IIOMetadataFormat stdFmt = md.getMetadataFormat(IIOMetadataFormatImpl.standardMetadataFormatName);
        check(stdFmt != null, "standard format descriptor non-null");
        check(IIOMetadataFormatImpl.standardMetadataFormatName.equals(stdFmt.getRootName()),
              "standard format root name");

        // native format descriptor (reflection-based getInstance loading)
        IIOMetadataFormat natFmt = md.getMetadataFormat(nativeName);
        check(natFmt != null, "native format descriptor non-null");
        check(nativeName.equals(natFmt.getRootName()), "native format root name");

        // getAsTree returns a DOM node
        Node stdTree = md.getAsTree(IIOMetadataFormatImpl.standardMetadataFormatName);
        check(stdTree != null, "standard tree non-null");
        check(IIOMetadataFormatImpl.standardMetadataFormatName.equals(stdTree.getNodeName()),
              "standard tree root node name");
        Node nativeTree = md.getAsTree(nativeName);
        check(nativeTree != null, "native tree non-null");

        // setFromTree / getAsTree round-trip
        md.setFromTree(IIOMetadataFormatImpl.standardMetadataFormatName, stdTree);
        Node stdTree2 = md.getAsTree(IIOMetadataFormatImpl.standardMetadataFormatName);
        check(stdTree2 != null, "standard tree after setFromTree non-null");
        md.setFromTree(nativeName, nativeTree);
        check(md.getAsTree(nativeName) != null, "native tree after setFromTree non-null");

        // read-only state + reset (png writer metadata is writable)
        check(md.isReadOnly() == false, "png writer metadata is not read-only");
        md.reset();

        // controller lifecycle
        check(md.getController() == null, "no controller initially");
        check(!md.hasController(), "hasController false initially");
        check(md.getDefaultController() == null, "no default controller");
        md.setController(null);
        check(md.getController() == null, "setController(null) ok");
        expectThrows(IllegalStateException.class, md::activateController,
                     "activateController with no controller");

        IIOMetadataController falseCtrl = m -> false;
        md.setController(falseCtrl);
        check(md.getController() == falseCtrl, "set/get controller identity");
        check(md.hasController(), "hasController true after set");
        check(md.activateController() == false, "activateController returns false from controller");

        md.setController(m -> true);
        check(md.activateController() == true, "activateController returns true from controller");

        md.setController(null);
        check(md.getController() == null && !md.hasController(), "controller cleared");

        // image reader metadata path
        IIOMetadata readerMd = createReaderMetadata();
        check(readerMd != null, "reader getImageMetadata non-null");
        check(readerMd.getNativeMetadataFormatName() != null, "reader native format non-null");
        String[] rnames = readerMd.getMetadataFormatNames();
        boolean rHasStandard = false;
        for (String n : rnames) {
            if (IIOMetadataFormatImpl.standardMetadataFormatName.equals(n)) rHasStandard = true;
        }
        check(rHasStandard, "reader format names contain standard");
        Node rTree = readerMd.getAsTree(readerMd.getNativeMetadataFormatName());
        check(rTree != null, "reader native tree non-null");
    }

    static IIOMetadata createPngMetadata() throws Exception {
        BufferedImage bi = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        ImageTypeSpecifier its = ImageTypeSpecifier.createFromRenderedImage(bi);
        ImageWriter w = ImageIO.getImageWritersByFormatName("png").next();
        return w.getDefaultImageMetadata(its, null);
    }

    static IIOMetadata createReaderMetadata() throws Exception {
        BufferedImage bi = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(bi, "png", bos);
        ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(bos.toByteArray()));
        ImageReader r = ImageIO.getImageReadersByFormatName("png").next();
        try {
            r.setInput(iis, false);
            return r.getImageMetadata(0);
        } finally {
            r.dispose();
            iis.close();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

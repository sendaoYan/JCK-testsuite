/*
 * @test
 * @summary javax.imageio.metadata.IIOMetadata conformance: abstract hierarchy and methods.
 * @run main IIOMetadataTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataController;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class IIOMetadataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Throwable expectThrows(String expectedClassName, ThrowingRunnable r, String m) {
        try { r.run(); }
        catch (Throwable t) {
            check(t.getClass().getName().equals(expectedClassName),
                  m + " expected " + expectedClassName + " but got " + t.getClass().getName());
            return t;
        }
        check(false, m + " should throw " + expectedClassName);
        return null;
    }
    interface ThrowingRunnable { void run() throws Exception; }

    static final String STD = IIOMetadataFormatImpl.standardMetadataFormatName;

    /** Concrete subclass used to probe the protected constructor and default method semantics. */
    static class ProbeMetadata extends IIOMetadata {
        int resetCount, mergeCount;
        String lastFormat;
        Node lastRoot;
        ProbeMetadata(boolean std, String nat, String natCls, String[] extraN, String[] extraC) {
            super(std, nat, natCls, extraN, extraC);
        }
        public boolean isReadOnly() { return false; }
        public Node getAsTree(String formatName) { return new IIOMetadataNode(formatName); }
        public void mergeTree(String formatName, Node root) { mergeCount++; lastFormat = formatName; lastRoot = root; }
        public void reset() { resetCount++; }
    }

    /** IIOMetadataFormat implementation loadable through the extra-format class-name channel. */
    public static class GoodFmt extends IIOMetadataFormatImpl {
        static final GoodFmt INSTANCE = new GoodFmt();
        GoodFmt() { super("goodroot", CHILD_POLICY_EMPTY); }
        public static IIOMetadataFormat getInstance() { return INSTANCE; }
        public boolean canNodeAppear(String elementName, ImageTypeSpecifier imageType) { return true; }
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.metadata.IIOMetadata");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("isStandardMetadataFormatSupported"), "method isStandardMetadataFormatSupported");
        check(dm.contains("isReadOnly"), "method isReadOnly");
        check(dm.contains("getNativeMetadataFormatName"), "method getNativeMetadataFormatName");
        check(dm.contains("getExtraMetadataFormatNames"), "method getExtraMetadataFormatNames");
        check(dm.contains("getMetadataFormatNames"), "method getMetadataFormatNames");
        check(dm.contains("getMetadataFormat"), "method getMetadataFormat");
        check(dm.contains("getAsTree"), "method getAsTree");
        check(dm.contains("mergeTree"), "method mergeTree");
        check(dm.contains("setFromTree"), "method setFromTree");
        check(dm.contains("reset"), "method reset");
        check(dm.contains("setController"), "method setController");
        check(dm.contains("getController"), "method getController");
        check(dm.contains("getDefaultController"), "method getDefaultController");
        check(dm.contains("hasController"), "method hasController");
        check(dm.contains("activateController"), "method activateController");
        check(Modifier.isAbstract(c.getMethod("isReadOnly").getModifiers()), "isReadOnly abstract");
        check(Modifier.isAbstract(c.getMethod("getAsTree", String.class).getModifiers()), "getAsTree abstract");
        check(Modifier.isAbstract(c.getMethod("mergeTree", String.class, Node.class).getModifiers()), "mergeTree abstract");
        check(Modifier.isAbstract(c.getMethod("reset").getModifiers()), "reset abstract");
        // protected constructors
        boolean protectedCtor = false;
        for (Constructor<?> k : c.getDeclaredConstructors()) {
            if (Modifier.isProtected(k.getModifiers())) protectedCtor = true;
        }
        check(protectedCtor, "has protected constructor");
        testPngWriterMetadata();
        testStandardChain();
        testCtorValidation();
        testFormatLookup();
        testControllerWiring();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static IIOMetadata pngWriterMetadata() throws Exception {
        BufferedImage bi = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        ImageTypeSpecifier its = ImageTypeSpecifier.createFromRenderedImage(bi);
        ImageWriter w = ImageIO.getImageWritersByFormatName("png").next();
        return w.getDefaultImageMetadata(its, null);
    }

    // PNG writer default metadata: format name discovery + mergeTree/setFromTree error matrix (probed)
    static void testPngWriterMetadata() throws Exception {
        IIOMetadata md = pngWriterMetadata();
        final String nat = md.getNativeMetadataFormatName();
        check("javax_imageio_png_1.0".equals(nat), "png native format name");
        check(md.isStandardMetadataFormatSupported(), "png standard format supported");
        check(md.getExtraMetadataFormatNames() == null, "png no extra formats");
        String[] names = md.getMetadataFormatNames();
        check(names != null && names.length == 2, "png two format names");
        check(nat.equals(names[0]), "native name listed first");
        check(STD.equals(names[1]), "standard name listed second");
        IIOMetadataFormat stdFmt = md.getMetadataFormat(STD);
        check(stdFmt != null && stdFmt == IIOMetadataFormatImpl.getStandardFormatInstance(),
              "standard format is the shared singleton");
        IIOMetadataFormat natFmt = md.getMetadataFormat(nat);
        check(natFmt != null && nat.equals(natFmt.getRootName()), "native format descriptor");

        final Node stdTree = md.getAsTree(STD);
        check(STD.equals(stdTree.getNodeName()), "standard tree root name");
        check(md.getAsTree(nat) != null, "native tree non-null");

        // error matrix (exact classes probed on jdk8u372-b07)
        expectThrows("java.lang.IllegalArgumentException", () -> md.getAsTree("bogus_format"),
                     "getAsTree unknown format");
        expectThrows("java.lang.NullPointerException", () -> md.getAsTree(null), "getAsTree null format");
        expectThrows("java.lang.IllegalArgumentException", () -> md.mergeTree("bogus_format", stdTree),
                     "mergeTree unknown format");
        expectThrows("java.lang.NullPointerException", () -> md.mergeTree(null, stdTree), "mergeTree null format");
        expectThrows("java.lang.IllegalArgumentException", () -> md.mergeTree(STD, null), "mergeTree null root");
        expectThrows("java.lang.IllegalArgumentException", () -> md.setFromTree("bogus_format", stdTree),
                     "setFromTree unknown format");
        expectThrows("java.lang.NullPointerException", () -> md.setFromTree(null, stdTree), "setFromTree null format");
        expectThrows("java.lang.IllegalArgumentException", () -> md.setFromTree(STD, null), "setFromTree null root");
        expectThrows("java.lang.IllegalArgumentException", () -> md.getMetadataFormat(null), "getMetadataFormat null");
        expectThrows("java.lang.IllegalArgumentException", () -> md.getMetadataFormat("bogus_format"),
                     "getMetadataFormat unknown");

        // wrong root node name -> IIOInvalidTreeException carrying the offending node
        final IIOMetadataNode wrongRoot = new IIOMetadataNode("WRONG");
        IIOInvalidTreeException bad = (IIOInvalidTreeException) expectThrows(
            "javax.imageio.metadata.IIOInvalidTreeException",
            () -> md.mergeTree(STD, wrongRoot), "mergeTree standard wrong root");
        check(bad.getOffendingNode() == wrongRoot, "offending node is the bad root");
        IIOInvalidTreeException badNat = (IIOInvalidTreeException) expectThrows(
            "javax.imageio.metadata.IIOInvalidTreeException",
            () -> md.mergeTree(nat, wrongRoot), "mergeTree native wrong root");
        check(badNat.getOffendingNode() == wrongRoot, "native offending node");

        // mergeTree/setFromTree round-trips
        md.reset();
        IIOMetadataNode tree = new IIOMetadataNode(STD);
        IIOMetadataNode dim = new IIOMetadataNode("Dimension");
        IIOMetadataNode par = new IIOMetadataNode("PixelAspectRatio");
        par.setAttribute("value", "2.0");
        dim.appendChild(par);
        tree.appendChild(dim);
        md.mergeTree(STD, tree);
        Node back = md.getAsTree(STD);
        Node dimBack = null;
        for (Node ch = back.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
            if ("Dimension".equals(ch.getNodeName())) dimBack = ch;
        }
        check(dimBack != null, "Dimension present after merge");
        check("2.0".equals(((Element) dimBack.getFirstChild()).getAttribute("value")),
              "merged PixelAspectRatio readable via getAsTree");
        Node natTree = md.getAsTree(nat);
        md.mergeTree(nat, natTree);
        check(md.getAsTree(nat) != null, "native self-merge round-trip");
        md.setFromTree(STD, md.getAsTree(STD));
        check(md.getAsTree(STD) != null, "setFromTree standard round-trip");
        check(!md.isReadOnly(), "png writer metadata writable");

        // subclass default wiring of setFromTree: reset() then mergeTree(format, root)
        ProbeMetadata pm = new ProbeMetadata(true, null, null, null, null);
        IIOMetadataNode myTree = new IIOMetadataNode("custom");
        pm.setFromTree("fmt", myTree);
        check(pm.resetCount == 1, "setFromTree invoked reset once");
        check(pm.mergeCount == 1, "setFromTree invoked mergeTree once");
        check("fmt".equals(pm.lastFormat), "format forwarded to mergeTree");
        check(pm.lastRoot == myTree, "root forwarded to mergeTree");
        check("anything".equals(pm.getAsTree("anything").getNodeName()), "subclass getAsTree returns its own tree");
    }

    // full standard-metadata chain: ImageIO.write a PNG, read it back, inspect the standard tree
    static void testStandardChain() throws Exception {
        BufferedImage bi = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        check(ImageIO.write(bi, "png", bos), "write png to memory");
        ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(bos.toByteArray()));
        ImageReader r = ImageIO.getImageReadersByFormatName("png").next();
        try {
            r.setInput(iis, false);
            IIOMetadata md = r.getImageMetadata(0);
            check(md != null, "reader getImageMetadata non-null");
            check("javax_imageio_png_1.0".equals(md.getNativeMetadataFormatName()), "reader native format");
            check(md.isStandardMetadataFormatSupported(), "reader standard format supported");
            Node std = md.getAsTree(STD);
            check(STD.equals(std.getNodeName()), "standard tree root javax_imageio_1.0");
            // deterministic structure for an 8x8 TYPE_INT_RGB PNG (probed)
            Node chroma = null, compression = null, data = null, dimension = null, transparency = null;
            int childCount = 0;
            for (Node ch = std.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
                childCount++;
                String nm = ch.getNodeName();
                if ("Chroma".equals(nm)) chroma = ch;
                else if ("Compression".equals(nm)) compression = ch;
                else if ("Data".equals(nm)) data = ch;
                else if ("Dimension".equals(nm)) dimension = ch;
                else if ("Transparency".equals(nm)) transparency = ch;
            }
            check(childCount == 5, "standard tree has 5 top-level children");
            check(chroma != null, "Chroma subtree present");
            check(compression != null, "Compression subtree present");
            check(data != null, "Data subtree present");
            check(dimension != null, "Dimension subtree present");
            check(transparency != null, "Transparency subtree present");
            Element colorSpace = (Element) childByName(chroma, "ColorSpaceType");
            Element numChannels = (Element) childByName(chroma, "NumChannels");
            check(colorSpace != null, "Chroma/ColorSpaceType present");
            check("RGB".equals(colorSpace.getAttribute("name")), "ColorSpaceType name RGB");
            check(numChannels != null, "Chroma/NumChannels present");
            check("3".equals(numChannels.getAttribute("value")), "NumChannels value 3");
            Element blackIsZero = (Element) childByName(chroma, "BlackIsZero");
            check(blackIsZero != null && "TRUE".equals(blackIsZero.getAttribute("value")), "BlackIsZero TRUE");
            Element compName = (Element) childByName(compression, "CompressionTypeName");
            check(compName != null && "deflate".equals(compName.getAttribute("value")), "CompressionTypeName deflate");
            Element lossless = (Element) childByName(compression, "Lossless");
            check(lossless != null && "TRUE".equals(lossless.getAttribute("value")), "Lossless TRUE");
            Element sampleFormat = (Element) childByName(data, "SampleFormat");
            check(sampleFormat != null && "UnsignedIntegral".equals(sampleFormat.getAttribute("value")),
                  "SampleFormat UnsignedIntegral");
            Element bits = (Element) childByName(data, "BitsPerSample");
            check(bits != null && "8 8 8".equals(bits.getAttribute("value")), "BitsPerSample 8 8 8");
            Element par = (Element) childByName(dimension, "PixelAspectRatio");
            check(par != null && "1.0".equals(par.getAttribute("value")), "PixelAspectRatio 1.0");
            Element orient = (Element) childByName(dimension, "ImageOrientation");
            check(orient != null && "Normal".equals(orient.getAttribute("value")), "ImageOrientation Normal");
            Element alpha = (Element) childByName(transparency, "Alpha");
            check(alpha != null && "none".equals(alpha.getAttribute("value")), "Alpha none");
            check(childByName(chroma, "Palette") == null, "no Palette for RGB png");
            // reader metadata is writable too: reset + native tree round-trip
            check(!md.isReadOnly(), "reader metadata writable");
            md.reset();
            check(md.getAsTree(nat0(md)) != null, "native tree after reset");
        } finally {
            r.dispose();
            iis.close();
        }
    }

    static String nat0(IIOMetadata md) { return md.getNativeMetadataFormatName(); }

    static Node childByName(Node parent, String name) {
        for (Node ch = parent.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
            if (name.equals(ch.getNodeName())) return ch;
        }
        return null;
    }

    // protected-constructor argument validation (4 IAE variants probed)
    static void testCtorValidation() throws Exception {
        expectThrows("java.lang.IllegalArgumentException",
                     () -> new ProbeMetadata(true, null, null, new String[0], new String[0]),
                     "extra format names of length 0");
        expectThrows("java.lang.IllegalArgumentException",
                     () -> new ProbeMetadata(true, null, null, new String[]{"x"}, null),
                     "extra names with null class names");
        expectThrows("java.lang.IllegalArgumentException",
                     () -> new ProbeMetadata(true, null, null, new String[]{"x", "y"}, new String[]{"c1"}),
                     "extra names/class names length mismatch");
        expectThrows("java.lang.IllegalArgumentException",
                     () -> new ProbeMetadata(true, null, null, null, new String[]{"c"}),
                     "class names without names");
        ProbeMetadata none = new ProbeMetadata(false, null, null, null, null);
        check(none.getMetadataFormatNames() == null, "no formats -> null names array");
        check(none.getExtraMetadataFormatNames() == null, "no extra formats -> null");
        check(!none.isStandardMetadataFormatSupported(), "standard unsupported as constructed");
        expectThrows("java.lang.IllegalArgumentException", () -> none.getMetadataFormat(STD),
                     "standard format unsupported");
    }

    // format lookup through native and extra channels
    static void testFormatLookup() throws Exception {
        ProbeMetadata withNat = new ProbeMetadata(false, "nat", "no.such.NatFormat", null, null);
        String[] n1 = withNat.getMetadataFormatNames();
        check(n1 != null && n1.length == 1 && "nat".equals(n1[0]), "native-only names array");
        expectThrows("java.lang.IllegalStateException", () -> withNat.getMetadataFormat("nat"),
                     "unloadable native format class");
        ProbeMetadata withExtra = new ProbeMetadata(true, null, null,
            new String[]{"good"}, new String[]{"IIOMetadataTest$GoodFmt"});
        String[] n2 = withExtra.getMetadataFormatNames();
        check(n2 != null && n2.length == 2, "standard+extra names length");
        check(STD.equals(n2[0]) && "good".equals(n2[1]), "standard first then extra");
        check(java.util.Arrays.equals(withExtra.getExtraMetadataFormatNames(), new String[]{"good"}),
              "extra names array contents");
        IIOMetadataFormat gf = withExtra.getMetadataFormat("good");
        check(gf == GoodFmt.getInstance(), "extra format loaded via static getInstance");
        check("goodroot".equals(gf.getRootName()), "extra format root name");
        expectThrows("java.lang.IllegalArgumentException", () -> withExtra.getMetadataFormat("nat"),
                     "unknown format for extra-only metadata");
    }

    // controller wiring (probed: activateController delegates activate(this))
    static void testControllerWiring() throws Exception {
        IIOMetadata md = pngWriterMetadata();
        check(md.getDefaultController() == null, "png metadata has no default controller");
        check(md.getController() == null, "no controller initially");
        check(!md.hasController(), "hasController false");
        md.setController(null);
        check(md.getController() == null, "setController(null) allowed");
        expectThrows("java.lang.IllegalStateException", () -> md.activateController(),
                     "activateController without controller");
        final IIOMetadata self = md;
        final IIOMetadata[] received = new IIOMetadata[1];
        final int[] calls = new int[1];
        md.setController(new IIOMetadataController() {
            public boolean activate(IIOMetadata m) { calls[0]++; received[0] = m; return false; }
        });
        check(md.hasController(), "hasController true after set");
        check(md.getController() != null, "controller installed");
        check(md.activateController() == false, "activateController relays false");
        check(calls[0] == 1, "controller invoked once");
        check(received[0] == self, "activateController passes this");
        md.setController(new IIOMetadataController() {
            public boolean activate(IIOMetadata m) { return true; }
        });
        check(md.activateController() == true, "activateController relays true");
        md.setController(null);
        check(!md.hasController(), "controller cleared");
        expectThrows("java.lang.IllegalStateException", () -> md.activateController(),
                     "activateController after clearing");
    }
}

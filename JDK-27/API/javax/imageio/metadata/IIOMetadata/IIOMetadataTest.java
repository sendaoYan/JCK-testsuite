/*
 * @test
 * @summary javax.imageio.metadata.IIOMetadata conformance: white-box structure + black-box behavior.
 * @run main IIOMetadataTest
 */

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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

    static final String STD = IIOMetadataFormatImpl.standardMetadataFormatName;

    /** Concrete subclass probing the protected constructor and default setFromTree wiring. */
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

    /** Format implementation loadable through the extra-format class-name channel. */
    public static class GoodFmt extends IIOMetadataFormatImpl {
        static final GoodFmt INSTANCE = new GoodFmt();
        GoodFmt() { super("goodroot", CHILD_POLICY_EMPTY); }
        public static IIOMetadataFormat getInstance() { return INSTANCE; }
        public boolean canNodeAppear(String elementName, ImageTypeSpecifier imageType) { return true; }
    }

    static Node childByName(Node parent, String name) {
        for (Node ch = parent.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
            if (name.equals(ch.getNodeName())) return ch;
        }
        return null;
    }

    // mergeTree/setFromTree/getAsTree error matrix on real PNG metadata (probed on jdk-27.0.1)
    static void testMergeTreeErrors() throws Exception {
        IIOMetadata md = createPngMetadata();
        final String nat = md.getNativeMetadataFormatName();
        final Node stdTree = md.getAsTree(STD);
        expectThrows(IllegalArgumentException.class, () -> md.getAsTree("bogus_format"),
                     "getAsTree unknown format");
        expectThrows(NullPointerException.class, () -> md.getAsTree(null), "getAsTree null format");
        expectThrows(IllegalArgumentException.class, () -> md.mergeTree("bogus_format", stdTree),
                     "mergeTree unknown format");
        expectThrows(NullPointerException.class, () -> md.mergeTree(null, stdTree), "mergeTree null format");
        expectThrows(IllegalArgumentException.class, () -> md.mergeTree(STD, null), "mergeTree null root");
        expectThrows(IllegalArgumentException.class, () -> md.setFromTree("bogus_format", stdTree),
                     "setFromTree unknown format");
        expectThrows(NullPointerException.class, () -> md.setFromTree(null, stdTree), "setFromTree null format");
        expectThrows(IllegalArgumentException.class, () -> md.setFromTree(STD, null), "setFromTree null root");
        expectThrows(IllegalArgumentException.class, () -> md.getMetadataFormat(null), "getMetadataFormat null");
        expectThrows(IllegalArgumentException.class, () -> md.getMetadataFormat("bogus_format"),
                     "getMetadataFormat unknown");
        // wrong root node -> IIOInvalidTreeException with the offending node attached
        final IIOMetadataNode wrongRoot = new IIOMetadataNode("WRONG");
        try {
            md.mergeTree(STD, wrongRoot);
            check(false, "mergeTree standard wrong root should throw");
        } catch (IIOInvalidTreeException e) {
            check(e.getOffendingNode() == wrongRoot, "offending node is the bad root");
            check(e.getMessage() != null && e.getMessage().contains("javax_imageio_1.0"), "message mentions root");
        }
        try {
            md.mergeTree(nat, wrongRoot);
            check(false, "mergeTree native wrong root should throw");
        } catch (IIOInvalidTreeException e) {
            check(e.getOffendingNode() == wrongRoot, "native offending node");
        }
        // merging an unknown child element is silently ignored (probed)
        IIOMetadataNode withUnknown = new IIOMetadataNode(STD);
        withUnknown.appendChild(new IIOMetadataNode("BogusChild"));
        md.mergeTree(STD, withUnknown);
        check(md.getAsTree(STD) != null, "merge with unknown child does not throw");
        // merge -> getAsTree value round-trip
        md.reset();
        IIOMetadataNode tree = new IIOMetadataNode(STD);
        IIOMetadataNode dim = new IIOMetadataNode("Dimension");
        IIOMetadataNode par = new IIOMetadataNode("PixelAspectRatio");
        par.setAttribute("value", "2.0");
        dim.appendChild(par);
        tree.appendChild(dim);
        md.mergeTree(STD, tree);
        Node back = md.getAsTree(STD);
        Node dimBack = childByName(back, "Dimension");
        check(dimBack != null, "Dimension present after merge");
        check("2.0".equals(((Element) childByName(dimBack, "PixelAspectRatio")).getAttribute("value")),
              "merged PixelAspectRatio readable via getAsTree");
        Node natTree = md.getAsTree(nat);
        md.mergeTree(nat, natTree);
        check(md.getAsTree(nat) != null, "native self-merge round-trip");
    }

    // deterministic structure of the reader standard metadata chain
    static void testStandardChainStructure() throws Exception {
        IIOMetadata md = createReaderMetadata();
        Node std = md.getAsTree(STD);
        check(STD.equals(std.getNodeName()), "standard tree root javax_imageio_1.0");
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
        check("RGB".equals(((Element) childByName(chroma, "ColorSpaceType")).getAttribute("name")),
              "ColorSpaceType name RGB");
        check("3".equals(((Element) childByName(chroma, "NumChannels")).getAttribute("value")), "NumChannels 3");
        check("TRUE".equals(((Element) childByName(chroma, "BlackIsZero")).getAttribute("value")), "BlackIsZero TRUE");
        check("deflate".equals(((Element) childByName(compression, "CompressionTypeName")).getAttribute("value")),
              "CompressionTypeName deflate");
        check("TRUE".equals(((Element) childByName(compression, "Lossless")).getAttribute("value")), "Lossless TRUE");
        check("UnsignedIntegral".equals(((Element) childByName(data, "SampleFormat")).getAttribute("value")),
              "SampleFormat UnsignedIntegral");
        check("8 8 8".equals(((Element) childByName(data, "BitsPerSample")).getAttribute("value")), "BitsPerSample 8 8 8");
        check("1.0".equals(((Element) childByName(dimension, "PixelAspectRatio")).getAttribute("value")),
              "PixelAspectRatio 1.0");
        check("Normal".equals(((Element) childByName(dimension, "ImageOrientation")).getAttribute("value")),
              "ImageOrientation Normal");
        check("none".equals(((Element) childByName(transparency, "Alpha")).getAttribute("value")), "Alpha none");
        check(childByName(chroma, "Palette") == null, "no Palette for RGB png");
    }

    // protected-constructor validation and format lookup channels (probed)
    static void testCtorAndFormatLookup() throws Exception {
        expectThrows(IllegalArgumentException.class,
                     () -> new ProbeMetadata(true, null, null, new String[0], new String[0]),
                     "extra format names of length 0");
        expectThrows(IllegalArgumentException.class,
                     () -> new ProbeMetadata(true, null, null, new String[]{"x"}, null),
                     "extra names with null class names");
        expectThrows(IllegalArgumentException.class,
                     () -> new ProbeMetadata(true, null, null, new String[]{"x", "y"}, new String[]{"c1"}),
                     "extra names/class names length mismatch");
        expectThrows(IllegalArgumentException.class,
                     () -> new ProbeMetadata(true, null, null, null, new String[]{"c"}),
                     "class names without names");
        ProbeMetadata none = new ProbeMetadata(false, null, null, null, null);
        check(none.getMetadataFormatNames() == null, "no formats -> null names array");
        check(!none.isStandardMetadataFormatSupported(), "standard unsupported as constructed");
        expectThrows(IllegalArgumentException.class, () -> none.getMetadataFormat(STD),
                     "standard format unsupported");
        ProbeMetadata withNat = new ProbeMetadata(false, "nat", "no.such.NatFormat", null, null);
        String[] n1 = withNat.getMetadataFormatNames();
        check(n1 != null && n1.length == 1 && "nat".equals(n1[0]), "native-only names array");
        expectThrows(IllegalStateException.class, () -> withNat.getMetadataFormat("nat"),
                     "unloadable native format class");
        ProbeMetadata withExtra = new ProbeMetadata(true, null, null,
            new String[]{"good"}, new String[]{"IIOMetadataTest$GoodFmt"});
        String[] n2 = withExtra.getMetadataFormatNames();
        check(n2 != null && n2.length == 2 && STD.equals(n2[0]) && "good".equals(n2[1]),
              "standard first then extra");
        check(java.util.Arrays.equals(withExtra.getExtraMetadataFormatNames(), new String[]{"good"}),
              "extra names array contents");
        IIOMetadataFormat gf = withExtra.getMetadataFormat("good");
        check(gf == GoodFmt.getInstance(), "extra format loaded via static getInstance");
        check("goodroot".equals(gf.getRootName()), "extra format root name");
        // setFromTree default wiring: reset() then mergeTree(format, root)
        ProbeMetadata pm = new ProbeMetadata(true, null, null, null, null);
        IIOMetadataNode myTree = new IIOMetadataNode("custom");
        pm.setFromTree("fmt", myTree);
        check(pm.resetCount == 1, "setFromTree invoked reset once");
        check(pm.mergeCount == 1, "setFromTree invoked mergeTree once");
        check("fmt".equals(pm.lastFormat), "format forwarded to mergeTree");
        check(pm.lastRoot == myTree, "root forwarded to mergeTree");
        // protected constructor exists and is protected
        boolean protectedCtor = false;
        for (Constructor<?> k : IIOMetadata.class.getDeclaredConstructors()) {
            if (Modifier.isProtected(k.getModifiers())) protectedCtor = true;
        }
        check(protectedCtor, "has protected constructor");
    }

    // controller identity wiring
    static void testControllerIdentity() throws Exception {
        IIOMetadata md = createPngMetadata();
        check(md.getDefaultController() == null, "png metadata has no default controller");
        final IIOMetadata self = md;
        final IIOMetadata[] received = new IIOMetadata[1];
        md.setController(m -> { received[0] = m; return true; });
        check(md.activateController(), "activateController relays true");
        check(received[0] == self, "activateController passes this");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testMergeTreeErrors();
        testStandardChainStructure();
        testCtorAndFormatLookup();
        testControllerIdentity();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

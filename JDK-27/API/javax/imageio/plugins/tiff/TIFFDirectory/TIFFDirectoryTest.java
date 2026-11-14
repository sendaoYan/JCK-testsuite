/*
 * @test
 * @summary javax.imageio.plugins.tiff.TIFFDirectory conformance: white-box structure + black-box behavior.
 * @run main TIFFDirectoryTest
 */

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.plugins.tiff.BaselineTIFFTagSet;
import javax.imageio.plugins.tiff.TIFFDirectory;
import javax.imageio.plugins.tiff.TIFFField;
import javax.imageio.plugins.tiff.TIFFTag;
import javax.imageio.plugins.tiff.TIFFTagSet;

public class TIFFDirectoryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.plugins.tiff.TIFFDirectory");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isAbstract(mods) == false, "not abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        check(c.getInterfaces().length == 1 && c.getInterfaces()[0].getName().equals("java.lang.Cloneable"),
              "implements Cloneable");
        check(c.getConstructors().length == 1, "one public constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        BaselineTIFFTagSet base = BaselineTIFFTagSet.getInstance();
        TIFFDirectory dir = new TIFFDirectory(new TIFFTagSet[]{ base }, null);

        // parent tag / tag sets
        check(dir.getParentTag() == null, "root directory parent null");
        TIFFTagSet[] tagSets = dir.getTagSets();
        check(tagSets.length == 1 && tagSets[0] == base, "initial tag sets contain baseline");
        check(dir.getTag(256) == base.getTag("ImageWidth"), "getTag(256) resolves ImageWidth");
        check(dir.getTag(99999) == null, "unknown tag null");

        // add/remove tag sets
        TIFFTagSet custom = new TIFFTagSet(new ArrayList<TIFFTag>());
        dir.addTagSet(custom);
        check(dir.getTagSets().length == 2, "addTagSet increases count");
        dir.removeTagSet(custom);
        check(dir.getTagSets().length == 1, "removeTagSet decreases count");

        // fields
        TIFFTag widthTag = base.getTag("ImageWidth");
        TIFFTag lengthTag = base.getTag("ImageLength");
        TIFFField w = new TIFFField(widthTag, 8L);
        TIFFField h = new TIFFField(lengthTag, 6L);

        check(dir.getNumTIFFFields() == 0, "initial field count 0");
        dir.addTIFFField(w);
        check(dir.getNumTIFFFields() == 1, "field count 1 after add");
        check(dir.containsTIFFField(256), "contains ImageWidth field");
        check(dir.getTIFFField(256) == w, "getTIFFField(256) returns added field");
        check(dir.getTIFFField(999) == null, "getTIFFField unknown null");

        dir.addTIFFField(h);
        check(dir.getNumTIFFFields() == 2, "field count 2 after second add");
        TIFFField[] fields = dir.getTIFFFields();
        check(fields.length == 2, "getTIFFFields length 2");
        check(fields[0].getTagNumber() == 256 && fields[1].getTagNumber() == 257,
              "getTIFFFields ordered by increasing tag number");

        dir.removeTIFFField(256);
        check(!dir.containsTIFFField(256), "field removed");
        check(dir.getNumTIFFFields() == 1, "field count 1 after remove");
        dir.removeTIFFFields();
        check(dir.getNumTIFFFields() == 0, "removeTIFFFields clears all");

        // metadata round-trip: directory -> metadata -> directory
        dir.addTIFFField(new TIFFField(widthTag, 8L));
        IIOMetadata meta = dir.getAsMetadata();
        check(meta != null, "getAsMetadata non-null");
        TIFFDirectory restored = TIFFDirectory.createFromMetadata(meta);
        check(restored != null, "createFromMetadata non-null");
        TIFFField rf = restored.getTIFFField(256);
        check(rf != null && rf.getAsInt(0) == 8, "metadata round-trip preserves ImageWidth value");

        // clone independence
        TIFFDirectory clone = dir.clone();
        check(clone.getNumTIFFFields() == dir.getNumTIFFFields(), "clone has same field count");
        clone.addTIFFField(new TIFFField(lengthTag, 6L));
        check(dir.getNumTIFFFields() == 1, "original unaffected by clone mutation");
    }

    // ---- Part 3 (deepened): exact-exception helpers ----
    interface ThrowingRunnable { void run() throws Exception; }
    static void expectExact(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
            check(false, msg + " should throw " + expected.getSimpleName() + " but nothing was thrown");
        } catch (Throwable t) {
            check(t.getClass() == expected,
                  msg + " expected exactly " + expected.getName() + " but got " + t.getClass().getName());
        }
    }

    // Minimal IIOMetadata that only supports the standard format; used to prove
    // createFromMetadata rejects metadata lacking the TIFF native format.
    static class StdOnlyMetadata extends IIOMetadata {
        org.w3c.dom.Node root;
        StdOnlyMetadata(org.w3c.dom.Node r) { root = r; }
        public boolean isReadOnly() { return false; }
        public org.w3c.dom.Node getAsTree(String formatName) { return root; }
        public void mergeTree(String formatName, org.w3c.dom.Node root) { }
        public void reset() { }
    }

    // ---- Part 3 (deepened): exception matrix, defensive copies, replace semantics ----
    static void testDeepened() throws Exception {
        BaselineTIFFTagSet base = BaselineTIFFTagSet.getInstance();
        TIFFTag widthTag = base.getTag("ImageWidth");
        TIFFTag lengthTag = base.getTag("ImageLength");

        // constructor nulls (probed on JDK-27)
        expectExact(NullPointerException.class,
            () -> new TIFFDirectory(null, null), "null tagSets array NPE");

        // add/remove tag set nulls and no-ops (probed)
        TIFFDirectory d = new TIFFDirectory(new TIFFTagSet[]{ base }, null);
        expectExact(NullPointerException.class, () -> d.addTagSet(null), "addTagSet null NPE");
        expectExact(NullPointerException.class, () -> d.removeTagSet(null), "removeTagSet null NPE");
        d.addTagSet(base); // duplicate: silently ignored (probed)
        check(d.getTagSets().length == 1, "duplicate addTagSet no-op");
        TIFFTagSet empty = new TIFFTagSet(new ArrayList<TIFFTag>());
        d.removeTagSet(empty); // not present: no-op (probed)
        check(d.getTagSets().length == 1, "removeTagSet absent no-op");
        // removing every tag set is permitted
        TIFFDirectory lone = new TIFFDirectory(new TIFFTagSet[]{ base }, null);
        lone.removeTagSet(base);
        check(lone.getTagSets().length == 0, "removeTagSet can empty the array");
        check(lone.getTag(256) == null, "no tag sets -> no tag lookup");

        // field nulls and no-ops (probed)
        expectExact(NullPointerException.class, () -> d.addTIFFField(null), "addTIFFField null NPE");
        d.removeTIFFField(12345); // absent: no-op
        check(d.getNumTIFFFields() == 0, "removeTIFFField absent no-op");
        check(!d.containsTIFFField(12345), "containsTIFFField absent false");

        // adding a field with the same tag number replaces the previous entry (probed)
        d.addTIFFField(new TIFFField(widthTag, 8L));
        d.addTIFFField(new TIFFField(widthTag, 99L));
        check(d.getNumTIFFFields() == 1, "same-number add replaces");
        check(d.getTIFFField(256).getAsLong(0) == 99L, "replacement value visible");

        // getTIFFFields / getTagSets are defensive copies (probed)
        TIFFField[] arr = d.getTIFFFields();
        arr[0] = null;
        check(d.getTIFFFields()[0] != null, "getTIFFFields defensive copy");
        TIFFTagSet[] ts = d.getTagSets();
        ts[0] = null;
        check(d.getTagSets()[0] != null, "getTagSets defensive copy");

        // getAsMetadata: fresh instance each call, TIFF native format (probed)
        IIOMetadata m1 = d.getAsMetadata();
        IIOMetadata m2 = d.getAsMetadata();
        check(m1 != m2, "getAsMetadata new instance per call");
        boolean hasTiff = false, hasStd = false;
        for (String f : m1.getMetadataFormatNames()) {
            if (f.equals("javax_imageio_tiff_image_1.0")) hasTiff = true;
            if (f.equals("javax_imageio_1.0")) hasStd = true;
        }
        check(hasTiff, "metadata exposes TIFF native format");
        check(hasStd, "metadata exposes standard format");
        org.w3c.dom.Node tree = m1.getAsTree("javax_imageio_tiff_image_1.0");
        check(tree.getNodeName().equals("javax_imageio_tiff_image_1.0"), "native tree root name");
        check(tree.getChildNodes().getLength() == 1
              && tree.getChildNodes().item(0).getNodeName().equals("TIFFIFD"),
              "native tree has TIFFIFD child");
        expectExact(IllegalArgumentException.class,
            () -> m1.getAsTree("bogus_format"), "getAsTree unknown format");

        // createFromMetadata null and non-TIFF metadata (probed)
        expectExact(NullPointerException.class,
            () -> TIFFDirectory.createFromMetadata(null), "createFromMetadata null NPE");
        StdOnlyMetadata std = new StdOnlyMetadata(new IIOMetadataNode("javax_imageio_1.0"));
        expectExact(IllegalArgumentException.class,
            () -> TIFFDirectory.createFromMetadata(std), "non-TIFF metadata IAE");

        // empty directory metadata round-trip (probed: zero fields preserved)
        TIFFDirectory emptyDir = new TIFFDirectory(new TIFFTagSet[]{ base }, null);
        check(TIFFDirectory.createFromMetadata(emptyDir.getAsMetadata()).getNumTIFFFields() == 0,
              "empty directory round-trip");

        // clone independence extends to tag sets (probed)
        TIFFDirectory orig = new TIFFDirectory(new TIFFTagSet[]{ base }, null);
        orig.addTIFFField(new TIFFField(widthTag, 8L));
        TIFFDirectory clone = orig.clone();
        clone.addTIFFField(new TIFFField(lengthTag, 6L));
        check(orig.getNumTIFFFields() == 1, "clone field mutation isolated");
        check(clone.getTIFFField(256).getAsLong(0) == 8L, "clone copied fields");
        clone.removeTagSet(base);
        check(orig.getTagSets().length == 1, "clone tag-set mutation isolated");
        clone.removeTIFFFields();
        check(orig.getNumTIFFFields() == 1, "clone removeTIFFFields isolated");

        // parent tag: non-IFD-pointer tag is stored and returned verbatim (probed)
        TIFFTag plain = new TIFFTag("Plain", 300, 1 << TIFFTag.TIFF_SHORT);
        TIFFDirectory withParent = new TIFFDirectory(new TIFFTagSet[]{ base }, plain);
        check(withParent.getParentTag() == plain, "parent tag stored verbatim");

        // ordering invariant: fields always sorted by increasing tag number
        TIFFDirectory ord = new TIFFDirectory(new TIFFTagSet[]{ base }, null);
        ord.addTIFFField(new TIFFField(lengthTag, 1L));     // 257
        ord.addTIFFField(new TIFFField(widthTag, 2L));      // 256
        ord.addTIFFField(new TIFFField(base.getTag("BitsPerSample"), TIFFTag.TIFF_SHORT, 1, new char[]{8})); // 258
        TIFFField[] fo = ord.getTIFFFields();
        check(fo.length == 3, "three fields");
        check(fo[0].getTagNumber() == 256 && fo[1].getTagNumber() == 257 && fo[2].getTagNumber() == 258,
              "insertion-order-independent tag sorting");
    }

    // ---- Part 3 (deepened): end-to-end write/read round-trip through real TIFF codec ----
    static void testRoundTrip() throws Exception {
        BaselineTIFFTagSet base = BaselineTIFFTagSet.getInstance();
        java.io.File tmp = null;
        try {
            int w = 6, h = 4;
            java.awt.image.BufferedImage src =
                new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_BYTE_GRAY);
            for (int y = 0; y < h; y++)
                for (int x = 0; x < w; x++)
                    src.setRGB(x, y, ((x * 40 + y * 10) & 0xFF) << 16);
            javax.imageio.ImageWriter writer =
                javax.imageio.ImageIO.getImageWritersByFormatName("tiff").next();
            javax.imageio.ImageWriteParam prm = writer.getDefaultWriteParam();
            javax.imageio.metadata.IIOMetadata streamMeta = writer.getDefaultStreamMetadata(prm);
            javax.imageio.metadata.IIOMetadata imageMeta =
                writer.getDefaultImageMetadata(new javax.imageio.ImageTypeSpecifier(src), prm);
            TIFFDirectory dir = TIFFDirectory.createFromMetadata(imageMeta);
            int baseCount = dir.getNumTIFFFields();
            check(baseCount > 0, "default image metadata carries tags");
            dir.addTIFFField(new TIFFField(base.getTag("DocumentName"),
                TIFFTag.TIFF_ASCII, 1, new String[]{"tiffdir-roundtrip"}));
            javax.imageio.metadata.IIOMetadata custom = dir.getAsMetadata();
            tmp = new java.io.File(System.getProperty("java.io.tmpdir"), "imgio-tiff-TIFFDirectory-" + Long.toHexString(System.nanoTime()) + ".tiff");
            javax.imageio.stream.ImageOutputStream ios =
                javax.imageio.ImageIO.createImageOutputStream(tmp);
            try {
                writer.setOutput(ios);
                writer.write(streamMeta, new javax.imageio.IIOImage(src, null, custom), prm);
            } finally {
                ios.close();
                writer.dispose();
            }
            javax.imageio.stream.ImageInputStream iis =
                javax.imageio.ImageIO.createImageInputStream(tmp);
            javax.imageio.ImageReader reader = javax.imageio.ImageIO.getImageReaders(iis).next();
            reader.setInput(iis, true, false);
            try {
                java.awt.image.BufferedImage out = reader.read(0);
                check(out.getWidth() == w && out.getHeight() == h, "round-trip dimensions");
                boolean eq = true;
                for (int y = 0; y < h && eq; y++)
                    for (int x = 0; x < w && eq; x++)
                        if (src.getRGB(x, y) != out.getRGB(x, y)) eq = false;
                check(eq, "round-trip pixels equal");
                javax.imageio.metadata.IIOMetadata readMeta = reader.getImageMetadata(0);
                TIFFDirectory back = TIFFDirectory.createFromMetadata(readMeta);
                TIFFField doc = back.getTIFFField(269);
                check(doc != null && doc.getAsString(0).equals("tiffdir-roundtrip"),
                      "custom ASCII tag survives write/read");
                check(back.getTIFFField(256).getAsLong(0) == w, "ImageWidth after round-trip");
                check(back.getTIFFField(257).getAsLong(0) == h, "ImageLength after round-trip");
                // stream metadata exposes the TIFF stream format
                javax.imageio.metadata.IIOMetadata sm = reader.getStreamMetadata();
                check(sm != null, "stream metadata non-null");
                boolean hasStreamFormat = false;
                for (String f : sm.getMetadataFormatNames())
                    if (f.equals("javax_imageio_tiff_stream_1.0")) hasStreamFormat = true;
                check(hasStreamFormat, "stream metadata TIFF format");
                // reader default param is a TIFFImageReadParam
                check(reader.getDefaultReadParam()
                      instanceof javax.imageio.plugins.tiff.TIFFImageReadParam,
                      "TIFF reader default param class");
            } finally {
                reader.dispose();
                iis.close();
            }
        } finally {
            if (tmp != null) tmp.delete();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepened();
        testRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

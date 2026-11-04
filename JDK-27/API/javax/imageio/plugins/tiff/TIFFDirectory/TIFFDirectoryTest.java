/*
 * @test
 * @summary javax.imageio.plugins.tiff.TIFFDirectory conformance: white-box structure + black-box behavior.
 * @run main TIFFDirectoryTest
 */

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import javax.imageio.metadata.IIOMetadata;
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

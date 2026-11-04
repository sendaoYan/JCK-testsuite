/*
 * @test
 * @summary javax.imageio.plugins.tiff.TIFFField conformance: white-box structure + black-box behavior.
 * @run main TIFFFieldTest
 */

import java.lang.reflect.Modifier;
import javax.imageio.plugins.tiff.BaselineTIFFTagSet;
import javax.imageio.plugins.tiff.TIFFDirectory;
import javax.imageio.plugins.tiff.TIFFField;
import javax.imageio.plugins.tiff.TIFFTag;
import javax.imageio.plugins.tiff.TIFFTagSet;

public class TIFFFieldTest {
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
        Class<?> c = Class.forName("javax.imageio.plugins.tiff.TIFFField");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isFinal(mods), "final modifier");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        check(c.getInterfaces().length == 1 && c.getInterfaces()[0].getName().equals("java.lang.Cloneable"),
              "implements Cloneable");
        check(c.getConstructors().length == 4, "four public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        int shortAndLong = (1 << TIFFTag.TIFF_SHORT) | (1 << TIFFTag.TIFF_LONG);
        TIFFTag tagA = new TIFFTag("MyTag", 300, shortAndLong);

        // single-value constructor selects SHORT for small value
        TIFFField small = new TIFFField(tagA, 42L);
        check(small.getTag() == tagA, "small field tag");
        check(small.getTagNumber() == 300, "small field tag number");
        check(small.getType() == TIFFTag.TIFF_SHORT, "small value stored as SHORT");
        check(small.getCount() == 1, "small field count 1");
        check(small.getAsInt(0) == 42, "small getAsInt");
        check(small.getAsLong(0) == 42L, "small getAsLong");
        check(small.getValueAsString(0).equals("42"), "small getValueAsString");

        // single-value constructor selects LONG for large value
        TIFFField big = new TIFFField(tagA, 70000L);
        check(big.getType() == TIFFTag.TIFF_LONG, "large value stored as LONG");
        check(big.getAsLong(0) == 70000L, "big getAsLong");
        check(big.getAsFloat(0) == 70000.0f, "big getAsFloat");
        check(big.getAsDouble(0) == 70000.0, "big getAsDouble");

        // direct construction with SHORT char[] data
        TIFFField shorts = new TIFFField(tagA, TIFFTag.TIFF_SHORT, 3, new char[]{10, 20, 30});
        check(shorts.getCount() == 3, "shorts count 3");
        check(shorts.getType() == TIFFTag.TIFF_SHORT, "shorts type SHORT");
        check(shorts.getData() instanceof char[], "shorts data is char[]");
        check(shorts.getAsChars()[1] == 20, "shorts getAsChars");
        check(shorts.getAsInts().length == 3 && shorts.getAsInts()[2] == 30, "shorts getAsInts");
        check(shorts.getAsInt(1) == 20, "shorts getAsInt(index)");
        check(shorts.isIntegral(), "SHORT field is integral");

        // 3-arg constructor creates zero array of the right type
        TIFFField empty = new TIFFField(tagA, TIFFTag.TIFF_LONG, 2);
        check(empty.getCount() == 2, "3-arg ctor count");
        check(empty.getAsLongs().length == 2 && empty.getAsLongs()[0] == 0L, "3-arg ctor zero long[]");

        // ASCII field
        TIFFTag asciiTag = new TIFFTag("ImageDescription", 270, 1 << TIFFTag.TIFF_ASCII);
        TIFFField ascii = new TIFFField(asciiTag, TIFFTag.TIFF_ASCII, 1, new String[]{"hello"});
        check(ascii.getCount() == 1, "ascii count");
        check(ascii.getAsString(0).equals("hello"), "ascii getAsString");
        check(ascii.getValueAsString(0).equals("hello"), "ascii getValueAsString");
        check(!ascii.isIntegral(), "ASCII field not integral");

        // type name round-trip
        check(TIFFField.getTypeName(TIFFTag.TIFF_LONG).equals("Long"), "getTypeName LONG");
        check(TIFFField.getTypeName(TIFFTag.TIFF_SHORT).equals("Short"), "getTypeName SHORT");
        check(TIFFField.getTypeName(TIFFTag.TIFF_IFD_POINTER).equals("IFDPointer"), "getTypeName IFDPointer");
        check(TIFFField.getTypeByName("Long") == TIFFTag.TIFF_LONG, "getTypeByName Long");
        check(TIFFField.getTypeByName("Short") == TIFFTag.TIFF_SHORT, "getTypeByName Short");
        check(TIFFField.getTypeByName("NoSuchType") == -1, "getTypeByName unknown -1");

        // createArrayForType
        check(TIFFField.createArrayForType(TIFFTag.TIFF_SHORT, 3) instanceof char[], "createArray SHORT");
        check(TIFFField.createArrayForType(TIFFTag.TIFF_DOUBLE, 3) instanceof double[], "createArray DOUBLE");
        check(TIFFField.createArrayForType(TIFFTag.TIFF_IFD_POINTER, 1) instanceof long[], "createArray IFD_POINTER");

        // IFD pointer field with directory
        TIFFTag ifdTag = new TIFFTag("ExifIFDPointer", 34665,
                (1 << TIFFTag.TIFF_LONG) | (1 << TIFFTag.TIFF_IFD_POINTER), 1);
        TIFFDirectory dir = new TIFFDirectory(new TIFFTagSet[]{ BaselineTIFFTagSet.getInstance() }, null);
        TIFFField ifdField = new TIFFField(ifdTag, TIFFTag.TIFF_LONG, 12345L, dir);
        check(ifdField.hasDirectory(), "IFD field has directory");
        check(ifdField.getDirectory() == dir, "IFD field directory reference");
        check(ifdField.getAsLong(0) == 12345L, "IFD field offset");
        check(ifdField.getCount() == 1, "IFD field count 1");

        // invalid construction
        expectThrows(NullPointerException.class, () -> new TIFFField(null, 42L), "null tag (value ctor)");
        expectThrows(IllegalArgumentException.class, () -> new TIFFField(tagA, -1L), "negative value");
        expectThrows(IllegalArgumentException.class,
            () -> new TIFFField(tagA, TIFFTag.TIFF_BYTE, 1, new byte[]{1}), "BYTE not allowed for tagA");
        expectThrows(IllegalArgumentException.class,
            () -> new TIFFField(tagA, TIFFTag.TIFF_SHORT, 2, new char[]{1}), "wrong data array length");
        expectThrows(IllegalArgumentException.class, () -> TIFFField.getTypeName(99), "getTypeName out of range");
    }

    // ---- Part 3 (deepened): exact-exception probe helper ----
    static void expectExact(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
            check(false, msg + " should throw " + expected.getSimpleName() + " but nothing was thrown");
        } catch (Throwable t) {
            check(t.getClass() == expected,
                  msg + " expected exactly " + expected.getName() + " but got " + t.getClass().getName());
        }
    }

    static void checkMsg(Class<? extends Throwable> expected, String needle, ThrowingRunnable r, String msg) {
        try {
            r.run();
            check(false, msg + " should throw " + expected.getSimpleName() + " but nothing was thrown");
        } catch (Throwable t) {
            check(t.getClass() == expected && t.getMessage() != null && t.getMessage().contains(needle),
                  msg + " got " + t.getClass().getName() + " (" + t.getMessage() + ")");
        }
    }

    // ---- Part 3 (deepened): value ctor boundaries, narrowing, array views ----
    static void testValueBoundaries() throws Exception {
        int shortLong = (1 << TIFFTag.TIFF_SHORT) | (1 << TIFFTag.TIFF_LONG);
        TIFFTag tag = new TIFFTag("MyTag", 300, shortLong);

        // SHORT/LONG selection boundary (probed on JDK-27)
        check(new TIFFField(tag, 0L).getType() == TIFFTag.TIFF_SHORT, "0 stored as SHORT");
        check(new TIFFField(tag, 65535L).getType() == TIFFTag.TIFF_SHORT, "65535 stored as SHORT");
        TIFFField big = new TIFFField(tag, 65536L);
        check(big.getType() == TIFFTag.TIFF_LONG, "65536 stored as LONG");
        TIFFField max = new TIFFField(tag, 0xFFFFFFFFL);
        check(max.getType() == TIFFTag.TIFF_LONG, "0xFFFFFFFF stored as LONG");
        check(max.getAsLong(0) == 0xFFFFFFFFL, "0xFFFFFFFF as long");
        // implicit narrowing: getAsInt truncates the long to low 32 bits (probed)
        check(max.getAsInt(0) == -1, "0xFFFFFFFF narrows to -1 via getAsInt");
        check(max.getAsFloat(0) == 4.294967295E9f, "0xFFFFFFFF as float");
        check(max.getAsDouble(0) == 4.294967295E9, "0xFFFFFFFF as double");

        // single-value ctor for an IFD-pointer-only tag: value 34665 < 65536 selects
        // SHORT which the tag does not allow -> IllegalArgumentException (probed)
        TIFFTagSet empty = new TIFFTagSet(new java.util.ArrayList<TIFFTag>());
        TIFFTag ifdTag = new TIFFTag("P", 34665, empty);
        expectExact(IllegalArgumentException.class,
            () -> new TIFFField(ifdTag, 34665L), "SHORT not allowed for IFD-only tag");

        // IFD-pointer ctor null directory -> NullPointerException "dir == null" (probed)
        checkMsg(NullPointerException.class, "dir == null",
            () -> new TIFFField(ifdTag, TIFFTag.TIFF_LONG, 1L, null), "IFD ctor null dir");

        // TIFF_IFD_POINTER typed field with directory (probed: getType 13, not integral)
        TIFFDirectory dir = new TIFFDirectory(new TIFFTagSet[]{ BaselineTIFFTagSet.getInstance() }, null);
        TIFFField ifdF = new TIFFField(ifdTag, TIFFTag.TIFF_IFD_POINTER, 123L, dir);
        check(ifdF.getType() == TIFFTag.TIFF_IFD_POINTER, "IFD field type 13");
        check(ifdF.hasDirectory() && ifdF.getDirectory() == dir, "IFD field directory");
        check(ifdF.getAsLong(0) == 123L, "IFD field offset value");
        check(!ifdF.isIntegral(), "IFD_POINTER not integral");

        // signed types round-trip negatives (probed)
        TIFFTag any = new TIFFTag("Any", 301, 0x3ffe); // all types 1..13
        TIFFField sshort = new TIFFField(any, TIFFTag.TIFF_SSHORT, 1, new short[]{-2});
        check(sshort.getAsInt(0) == -2, "SSHORT -2 getAsInt");
        check(sshort.getAsLong(0) == -2L, "SSHORT -2 getAsLong");
        check(sshort.getValueAsString(0).equals("-2"), "SSHORT -2 value string");
        TIFFField slong = new TIFFField(any, TIFFTag.TIFF_SLONG, 1, new int[]{-7});
        check(slong.getAsLong(0) == -7L, "SLONG -7 getAsLong");
        check(slong.getValueAsString(0).equals("-7"), "SLONG -7 value string");
        TIFFField sbyte = new TIFFField(any, TIFFTag.TIFF_SBYTE, 2, new byte[]{-3, 5});
        check(sbyte.getAsBytes()[0] == -3 && sbyte.getAsBytes()[1] == 5, "SBYTE bytes verbatim");
        check(sbyte.getAsInt(0) == -3, "SBYTE getAsInt sign-extended");

        // FLOAT / DOUBLE round-trip
        TIFFField fl = new TIFFField(any, TIFFTag.TIFF_FLOAT, 2, new float[]{1.5f, -2.5f});
        check(fl.getAsFloats()[0] == 1.5f && fl.getAsFloats()[1] == -2.5f, "FLOAT round-trip");
        check(fl.getAsFloat(1) == -2.5f, "getAsFloat(index)");
        check(fl.getAsDouble(0) == 1.5, "FLOAT promoted to double");
        TIFFField db = new TIFFField(any, TIFFTag.TIFF_DOUBLE, 1, new double[]{0.25});
        check(db.getAsDoubles()[0] == 0.25, "DOUBLE round-trip");
        check(db.getAsDouble(0) == 0.25, "getAsDouble(index)");
        check(db.getValueAsString(0).equals("0.25"), "DOUBLE value string");

        // RATIONAL / SRATIONAL index and array accessors
        TIFFField rat = new TIFFField(any, TIFFTag.TIFF_RATIONAL, 2, new long[][]{{1L, 3L}, {5L, 7L}});
        check(rat.getAsRationals().length == 2, "RATIONAL array length");
        check(rat.getAsRationals()[0][0] == 1L && rat.getAsRationals()[0][1] == 3L, "RATIONAL[0]");
        check(rat.getAsRational(1)[0] == 5L && rat.getAsRational(1)[1] == 7L, "getAsRational(1)");
        check(rat.getValueAsString(0).equals("1/3"), "RATIONAL value string 1/3");
        TIFFField srat = new TIFFField(any, TIFFTag.TIFF_SRATIONAL, 1, new int[][]{{-9, 4}});
        check(srat.getAsSRationals().length == 1, "SRATIONAL array length");
        check(srat.getAsSRational(0)[0] == -9 && srat.getAsSRational(0)[1] == 4, "getAsSRational(0)");
        check(srat.getValueAsString(0).equals("-9/4"), "SRATIONAL value string -9/4");

        // count 0 field is legal (probed)
        TIFFField zero = new TIFFField(any, TIFFTag.TIFF_SHORT, 0, new char[0]);
        check(zero.getCount() == 0, "count 0 stored");
        check(zero.getAsInts().length == 0, "count 0 empty array view");

        // isIntegral matrix (probed)
        check(new TIFFField(any, TIFFTag.TIFF_BYTE, 1, new byte[]{9}).isIntegral(), "BYTE integral");
        check(new TIFFField(any, TIFFTag.TIFF_SHORT, 1, new char[]{9}).isIntegral(), "SHORT integral");
        check(new TIFFField(any, TIFFTag.TIFF_LONG, 1, new long[]{9}).isIntegral(), "LONG integral");
        check(new TIFFField(any, TIFFTag.TIFF_SLONG, 1, new int[]{9}).isIntegral(), "SLONG integral");
        check(new TIFFField(any, TIFFTag.TIFF_DOUBLE, 1, new double[]{9}).isIntegral() == false,
              "DOUBLE not integral");
        check(new TIFFField(any, TIFFTag.TIFF_ASCII, 1, new String[]{"x"}).isIntegral() == false,
              "ASCII not integral");

        // mismatched getAsXxx accessors throw ClassCastException (probed on JDK-27)
        TIFFField lf = new TIFFField(any, TIFFTag.TIFF_LONG, 2, new long[]{1L, 2L});
        expectExact(ClassCastException.class, () -> lf.getAsBytes(), "getAsBytes on LONG");
        expectExact(ClassCastException.class, () -> lf.getAsShorts(), "getAsShorts on LONG");
        expectExact(ClassCastException.class, () -> lf.getAsChars(), "getAsChars on LONG");
        expectExact(ClassCastException.class, () -> lf.getAsFloats(), "getAsFloats on LONG");
        expectExact(ClassCastException.class, () -> lf.getAsDoubles(), "getAsDoubles on LONG");
        expectExact(ClassCastException.class, () -> lf.getAsRationals(), "getAsRationals on LONG");
        expectExact(ClassCastException.class, () -> lf.getAsSRationals(), "getAsSRationals on LONG");
        expectExact(ClassCastException.class, () -> lf.getAsString(0), "getAsString on LONG");

        // getAsLong on ASCII parses text -> NumberFormatException for non-numeric (probed)
        TIFFField ascii = new TIFFField(any, TIFFTag.TIFF_ASCII, 1, new String[]{"42"});
        check(ascii.getAsLong(0) == 42L, "getAsLong parses numeric ASCII");
        TIFFField badAscii = new TIFFField(any, TIFFTag.TIFF_ASCII, 1, new String[]{"hi"});
        expectExact(NumberFormatException.class, () -> badAscii.getAsLong(0), "getAsLong on non-numeric ASCII");

        // index bounds -> ArrayIndexOutOfBoundsException (probed)
        expectExact(ArrayIndexOutOfBoundsException.class, () -> lf.getAsInt(-1), "getAsInt(-1)");
        expectExact(ArrayIndexOutOfBoundsException.class, () -> lf.getAsInt(2), "getAsInt(2)");
        expectExact(ArrayIndexOutOfBoundsException.class,
            () -> new TIFFField(any, TIFFTag.TIFF_ASCII, 1, new String[]{"x"}).getAsString(1),
            "getAsString(1)");
    }

    // ---- Part 3 (deepened): static helpers, native node, clone, createFromMetadataNode ----
    static void testStaticsAndNode() throws Exception {
        TIFFTag any = new TIFFTag("Any", 301, 0x3ffe);

        // type name table for all 13 types (probed)
        String[] names = {"Byte", "Ascii", "Short", "Long", "Rational", "SByte", "Undefined",
                          "SShort", "SLong", "SRational", "Float", "Double", "IFDPointer"};
        for (int i = 1; i <= 13; i++) {
            check(TIFFField.getTypeName(i).equals(names[i - 1]), "getTypeName(" + i + ") == " + names[i - 1]);
            check(TIFFField.getTypeByName(names[i - 1]) == i, "getTypeByName(" + names[i - 1] + ") == " + i);
        }
        check(TIFFField.getTypeByName("NoSuchType") == -1, "getTypeByName unknown -1");
        check(TIFFField.getTypeByName("") == -1, "getTypeByName empty -1");
        expectExact(NullPointerException.class, () -> TIFFField.getTypeByName(null), "getTypeByName(null)");
        expectExact(IllegalArgumentException.class, () -> TIFFField.getTypeName(0), "getTypeName(0)");
        expectExact(IllegalArgumentException.class, () -> TIFFField.getTypeName(14), "getTypeName(14)");

        // createArrayForType for all 13 types (probed)
        check(TIFFField.createArrayForType(TIFFTag.TIFF_BYTE, 2) instanceof byte[], "array BYTE");
        check(TIFFField.createArrayForType(TIFFTag.TIFF_ASCII, 2) instanceof String[], "array ASCII");
        check(TIFFField.createArrayForType(TIFFTag.TIFF_SHORT, 2) instanceof char[], "array SHORT");
        check(TIFFField.createArrayForType(TIFFTag.TIFF_LONG, 2) instanceof long[], "array LONG");
        check(TIFFField.createArrayForType(TIFFTag.TIFF_RATIONAL, 2) instanceof long[][], "array RATIONAL");
        check(TIFFField.createArrayForType(TIFFTag.TIFF_SBYTE, 2) instanceof byte[], "array SBYTE");
        check(TIFFField.createArrayForType(TIFFTag.TIFF_UNDEFINED, 2) instanceof byte[], "array UNDEFINED");
        check(TIFFField.createArrayForType(TIFFTag.TIFF_SSHORT, 2) instanceof short[], "array SSHORT");
        check(TIFFField.createArrayForType(TIFFTag.TIFF_SLONG, 2) instanceof int[], "array SLONG");
        check(TIFFField.createArrayForType(TIFFTag.TIFF_SRATIONAL, 2) instanceof int[][], "array SRATIONAL");
        check(TIFFField.createArrayForType(TIFFTag.TIFF_FLOAT, 2) instanceof float[], "array FLOAT");
        check(TIFFField.createArrayForType(TIFFTag.TIFF_DOUBLE, 2) instanceof double[], "array DOUBLE");
        check(TIFFField.createArrayForType(TIFFTag.TIFF_IFD_POINTER, 1) instanceof long[], "array IFD_POINTER");
        // IFD_POINTER arrays must have count 1; RATIONAL/SRATIONAL count >= 1 (probed)
        expectExact(IllegalArgumentException.class,
            () -> TIFFField.createArrayForType(TIFFTag.TIFF_IFD_POINTER, 2), "IFD_POINTER count 2");
        expectExact(IllegalArgumentException.class,
            () -> TIFFField.createArrayForType(TIFFTag.TIFF_RATIONAL, 0), "RATIONAL count 0");
        expectExact(IllegalArgumentException.class,
            () -> TIFFField.createArrayForType(TIFFTag.TIFF_SRATIONAL, 0), "SRATIONAL count 0");
        check(((long[][]) TIFFField.createArrayForType(TIFFTag.TIFF_RATIONAL, 3)).length == 3
              && ((long[][]) TIFFField.createArrayForType(TIFFTag.TIFF_RATIONAL, 3))[0].length == 2,
              "RATIONAL array is long[count][2]");
        check(((byte[]) TIFFField.createArrayForType(TIFFTag.TIFF_BYTE, 0)).length == 0,
              "createArray count 0 length");
        expectExact(IllegalArgumentException.class,
            () -> TIFFField.createArrayForType(99, 1), "createArrayForType bad type");
        expectExact(IllegalArgumentException.class,
            () -> TIFFField.createArrayForType(TIFFTag.TIFF_BYTE, -1), "createArrayForType negative count");

        // getAsNativeNode: element name "TIFFField", attributes number/name, typed children (probed)
        TIFFField lf = new TIFFField(any, TIFFTag.TIFF_LONG, 2, new long[]{7L, 9L});
        org.w3c.dom.Node n = lf.getAsNativeNode();
        check(n.getNodeName().equals("TIFFField"), "native node name TIFFField");
        org.w3c.dom.NamedNodeMap attrs = n.getAttributes();
        check(attrs.getNamedItem("number").getNodeValue().equals("301"), "number attribute");
        check(attrs.getNamedItem("name").getNodeValue().equals("Any"), "name attribute");
        org.w3c.dom.NodeList kids = n.getChildNodes();
        check(kids.getLength() == 1 && kids.item(0).getNodeName().equals("TIFFLongs"),
              "LONG wrapper child TIFFLongs");
        org.w3c.dom.NodeList vals = kids.item(0).getChildNodes();
        check(vals.getLength() == 2, "two TIFFLong children");
        check(vals.item(0).getNodeName().equals("TIFFLong"), "child element TIFFLong");
        check(vals.item(0).getAttributes().getNamedItem("value").getNodeValue().equals("7"), "value attr 7");
        check(vals.item(1).getAttributes().getNamedItem("value").getNodeValue().equals("9"), "value attr 9");

        // wrapper/element names per type (probed)
        String[][] pairs = {
            {Integer.toString(TIFFTag.TIFF_SHORT), "TIFFShorts", "TIFFShort"},
            {Integer.toString(TIFFTag.TIFF_BYTE), "TIFFBytes", "TIFFByte"},
            {Integer.toString(TIFFTag.TIFF_FLOAT), "TIFFFloats", "TIFFFloat"},
            {Integer.toString(TIFFTag.TIFF_DOUBLE), "TIFFDoubles", "TIFFDouble"},
        };
        Object[] data = {
            new char[]{7}, new byte[]{7}, new float[]{1.5f}, new double[]{0.25},
        };
        for (int i = 0; i < pairs.length; i++) {
            TIFFField f = new TIFFField(any, Integer.parseInt(pairs[i][0]), 1, data[i]);
            org.w3c.dom.Node nd = f.getAsNativeNode().getChildNodes().item(0);
            check(nd.getNodeName().equals(pairs[i][1]), pairs[i][1] + " wrapper");
            check(nd.getChildNodes().item(0).getNodeName().equals(pairs[i][2]), pairs[i][2] + " element");
        }
        // ASCII child carries the raw string (probed)
        TIFFField af = new TIFFField(any, TIFFTag.TIFF_ASCII, 1, new String[]{"abc"});
        org.w3c.dom.Node asciiKid = af.getAsNativeNode().getChildNodes().item(0);
        check(asciiKid.getNodeName().equals("TIFFAsciis"), "ASCII wrapper TIFFAsciis");
        check(asciiKid.getChildNodes().item(0).getAttributes().getNamedItem("value").getNodeValue().equals("abc"),
              "ASCII value attribute");
        // RATIONAL value formatting "num/den" (probed)
        TIFFField rf = new TIFFField(any, TIFFTag.TIFF_RATIONAL, 1, new long[][]{{1L, 2L}});
        org.w3c.dom.Node ratKid = rf.getAsNativeNode().getChildNodes().item(0).getChildNodes().item(0);
        check(ratKid.getNodeName().equals("TIFFRational"), "RATIONAL element");
        check(ratKid.getAttributes().getNamedItem("value").getNodeValue().equals("1/2"), "RATIONAL value 1/2");
        // UNDEFINED renders a single flat element (probed)
        TIFFField uf = new TIFFField(any, TIFFTag.TIFF_UNDEFINED, 2, new byte[]{9, 9});
        org.w3c.dom.Node undKid = uf.getAsNativeNode().getChildNodes().item(0);
        check(undKid.getNodeName().equals("TIFFUndefined"), "UNDEFINED flat element");
        check(undKid.getAttributes().getNamedItem("value").getNodeValue().equals("9,9"), "UNDEFINED comma values");

        // clone is a deep copy: mutating the clone's array leaves the original intact (probed)
        TIFFField orig = new TIFFField(any, TIFFTag.TIFF_LONG, 2, new long[]{7L, 9L});
        TIFFField cl = orig.clone();
        check(cl != orig, "clone distinct object");
        check(cl.getTag() == orig.getTag(), "clone shares tag reference");
        check(cl.getType() == orig.getType() && cl.getCount() == orig.getCount(), "clone type/count");
        cl.getAsLongs()[0] = 100;
        check(orig.getAsLongs()[0] == 7L, "clone data array is a deep copy");

        // createFromMetadataNode round-trip (probed)
        TIFFTagSet set = new TIFFTagSet(java.util.Arrays.asList(any));
        TIFFField back = TIFFField.createFromMetadataNode(set, lf.getAsNativeNode());
        check(back.getType() == TIFFTag.TIFF_LONG, "from-node type");
        check(back.getCount() == 2, "from-node count");
        check(back.getAsLong(0) == 7L && back.getAsLong(1) == 9L, "from-node values");
        check(back.getTag() == any, "from-node resolves registered tag");
        // null tagSet accepted; tag gets UNKNOWN_TAG_NAME (probed)
        TIFFField anon = TIFFField.createFromMetadataNode(null, lf.getAsNativeNode());
        check(anon.getTag().getName().equals(TIFFTag.UNKNOWN_TAG_NAME), "null tagSet -> UnknownTag name");
        check(anon.getTag().getNumber() == 301, "UnknownTag keeps number");
        // tagSet without the tag also yields UnknownTag (probed)
        TIFFField miss = TIFFField.createFromMetadataNode(
            new TIFFTagSet(new java.util.ArrayList<TIFFTag>()), lf.getAsNativeNode());
        check(miss.getTag().getName().equals(TIFFTag.UNKNOWN_TAG_NAME), "unregistered tag -> UnknownTag");
        // negative cases (probed exact types)
        expectExact(IllegalArgumentException.class,
            () -> TIFFField.createFromMetadataNode(set, null), "null node");
        expectExact(IllegalArgumentException.class,
            () -> TIFFField.createFromMetadataNode(set, new javax.imageio.metadata.IIOMetadataNode("NotTIFFField")),
            "wrong node name");
        javax.imageio.metadata.IIOMetadataNode bad = new javax.imageio.metadata.IIOMetadataNode("TIFFField");
        bad.setAttribute("number", "9999");
        expectExact(IllegalArgumentException.class,
            () -> TIFFField.createFromMetadataNode(set, bad), "unresolvable tag number");
    }

    // ---- Part 3 (deepened): constructor exception matrix (exact, probed) ----
    static void testCtorExceptions() throws Exception {
        int shortLong = (1 << TIFFTag.TIFF_SHORT) | (1 << TIFFTag.TIFF_LONG);
        TIFFTag tag = new TIFFTag("MyTag", 300, shortLong);
        TIFFTag any = new TIFFTag("Any", 301, 0x3ffe);

        expectExact(NullPointerException.class, () -> new TIFFField(null, 42L), "null tag value ctor");
        expectExact(NullPointerException.class,
            () -> new TIFFField(null, TIFFTag.TIFF_SHORT, 1, new char[]{1}), "null tag data ctor");
        expectExact(NullPointerException.class,
            () -> new TIFFField(null, TIFFTag.TIFF_SHORT, 1), "null tag 3-arg ctor");
        expectExact(IllegalArgumentException.class, () -> new TIFFField(tag, -1L), "negative value");
        expectExact(IllegalArgumentException.class,
            () -> new TIFFField(tag, TIFFTag.TIFF_BYTE, 1, new byte[]{1}), "type not allowed");
        expectExact(IllegalArgumentException.class,
            () -> new TIFFField(any, 99, 1, new char[]{1}), "type 99 unknown");
        expectExact(IllegalArgumentException.class,
            () -> new TIFFField(any, 0, 1, new char[]{1}), "type 0 unknown");
        expectExact(IllegalArgumentException.class,
            () -> new TIFFField(any, 14, 1, new long[]{1}), "type 14 unknown");
        expectExact(IllegalArgumentException.class,
            () -> new TIFFField(any, TIFFTag.TIFF_SHORT, 2, new char[]{1}), "count mismatch");
        expectExact(IllegalArgumentException.class,
            () -> new TIFFField(any, TIFFTag.TIFF_SHORT, -1, new char[]{1}), "negative count");
        expectExact(NullPointerException.class,
            () -> new TIFFField(any, TIFFTag.TIFF_SHORT, 1, (Object) null), "null data");
        expectExact(IllegalArgumentException.class,
            () -> new TIFFField(any, TIFFTag.TIFF_SHORT, 1, new int[]{1}), "wrong array class");
        expectExact(IllegalArgumentException.class,
            () -> new TIFFField(any, TIFFTag.TIFF_ASCII, 2, new String[]{"x"}), "ASCII count mismatch");
        expectExact(IllegalArgumentException.class,
            () -> new TIFFField(any, TIFFTag.TIFF_SHORT, 0, new char[1]), "count 0 with nonzero array");
        expectExact(IllegalArgumentException.class,
            () -> new TIFFField(any, 99, 1), "3-arg ctor unknown type");
        expectExact(IllegalArgumentException.class,
            () -> new TIFFField(any, TIFFTag.TIFF_SHORT, -1), "3-arg ctor negative count");
        // 3-arg ctor allocates zero-filled arrays of the right type
        TIFFField empty = new TIFFField(any, TIFFTag.TIFF_LONG, 2);
        check(empty.getAsLongs().length == 2 && empty.getAsLongs()[1] == 0L, "3-arg zero long[]");
        check(new TIFFField(any, TIFFTag.TIFF_DOUBLE, 3).getAsDoubles().length == 3, "3-arg double[] len 3");
        check(new TIFFField(any, TIFFTag.TIFF_ASCII, 2).getData() instanceof String[], "3-arg String[] len");
        check(new TIFFField(any, TIFFTag.TIFF_RATIONAL, 2).getAsRationals().length == 2, "3-arg rational len");
        check(new TIFFField(any, TIFFTag.TIFF_UNDEFINED, 4).getAsBytes().length == 4, "3-arg undefined len");
        // count 0 with empty array is legal (probed)
        check(new TIFFField(any, TIFFTag.TIFF_UNDEFINED, 0, new byte[0]).getCount() == 0, "count 0 undefined");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testValueBoundaries();
        testStaticsAndNode();
        testCtorExceptions();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

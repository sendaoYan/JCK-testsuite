/*
 * @test
 * @summary javax.swing.text.html.parser.DTDConstants conformance: white-box structure + black-box behavior.
 * @run main DTDConstantsTest
 */

import java.lang.reflect.Modifier;

import javax.swing.text.html.parser.DTDConstants;

public class DTDConstantsTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.parser.DTDConstants");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // content types
        check(DTDConstants.CDATA == 1, "CDATA");
        check(DTDConstants.ENTITY == 2, "ENTITY");
        check(DTDConstants.ENTITIES == 3, "ENTITIES");
        check(DTDConstants.ID == 4, "ID");
        check(DTDConstants.IDREF == 5, "IDREF");
        check(DTDConstants.IDREFS == 6, "IDREFS");
        check(DTDConstants.NAME == 7, "NAME");
        check(DTDConstants.NAMES == 8, "NAMES");
        check(DTDConstants.NMTOKEN == 9, "NMTOKEN");
        check(DTDConstants.NMTOKENS == 10, "NMTOKENS");
        check(DTDConstants.NOTATION == 11, "NOTATION");
        check(DTDConstants.NUMBER == 12, "NUMBER");
        check(DTDConstants.NUMBERS == 13, "NUMBERS");
        check(DTDConstants.NUTOKEN == 14, "NUTOKEN");
        check(DTDConstants.NUTOKENS == 15, "NUTOKENS");
        check(DTDConstants.RCDATA == 16, "RCDATA");
        check(DTDConstants.EMPTY == 17, "EMPTY");
        check(DTDConstants.MODEL == 18, "MODEL");
        check(DTDConstants.ANY == 19, "ANY");

        // attribute modifiers
        check(DTDConstants.FIXED == 1, "FIXED");
        check(DTDConstants.REQUIRED == 2, "REQUIRED");
        check(DTDConstants.CURRENT == 3, "CURRENT");
        check(DTDConstants.CONREF == 4, "CONREF");
        check(DTDConstants.IMPLIED == 5, "IMPLIED");

        // entity types
        check(DTDConstants.PUBLIC == 10, "PUBLIC");
        check(DTDConstants.SDATA == 11, "SDATA");
        check(DTDConstants.PI == 12, "PI");
        check(DTDConstants.STARTTAG == 13, "STARTTAG");
        check(DTDConstants.ENDTAG == 14, "ENDTAG");
        check(DTDConstants.MS == 15, "MS");
        check(DTDConstants.MD == 16, "MD");
        check(DTDConstants.SYSTEM == 17, "SYSTEM");

        // flags
        check(DTDConstants.GENERAL == 65536, "GENERAL");
        check(DTDConstants.DEFAULT == 131072, "DEFAULT");
        check(DTDConstants.PARAMETER == 262144, "PARAMETER");
    }

    // ---- Part 3: deep contract coverage ----

    static void testDeepConstantShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.parser.DTDConstants");
        java.lang.reflect.Field[] fs = c.getFields();
        check(fs.length == 35, "exactly 35 constants, got " + fs.length);
        for (java.lang.reflect.Field f : fs) {
            int m = f.getModifiers();
            check(java.lang.reflect.Modifier.isPublic(m)
                  && java.lang.reflect.Modifier.isStatic(m)
                  && java.lang.reflect.Modifier.isFinal(m),
                  "constant " + f.getName() + " is public static final");
            check(f.getType() == int.class, "constant " + f.getName() + " is int");
        }
    }

    static void testDeepConstantRelations() {
        // content types form the contiguous range 1..19
        int[] contentTypeValues = {
            DTDConstants.CDATA, DTDConstants.ENTITY, DTDConstants.ENTITIES, DTDConstants.ID,
            DTDConstants.IDREF, DTDConstants.IDREFS, DTDConstants.NAME, DTDConstants.NAMES,
            DTDConstants.NMTOKEN, DTDConstants.NMTOKENS, DTDConstants.NOTATION, DTDConstants.NUMBER,
            DTDConstants.NUMBERS, DTDConstants.NUTOKEN, DTDConstants.NUTOKENS, DTDConstants.RCDATA,
            DTDConstants.EMPTY, DTDConstants.MODEL, DTDConstants.ANY,
        };
        java.util.HashSet<Integer> seen = new java.util.HashSet<Integer>();
        for (int v : contentTypeValues) {
            check(v >= 1 && v <= 19, "content type value " + v + " in 1..19");
            check(seen.add(v), "content type value " + v + " unique");
        }
        // attribute modifiers form the contiguous range 1..5
        check(DTDConstants.FIXED < DTDConstants.REQUIRED && DTDConstants.REQUIRED < DTDConstants.CURRENT
              && DTDConstants.CURRENT < DTDConstants.CONREF && DTDConstants.CONREF < DTDConstants.IMPLIED,
              "modifiers are ordered FIXED<REQUIRED<CURRENT<CONREF<IMPLIED");
        // entity types occupy 10..17, disjoint from each other
        int[] entityTypes = {DTDConstants.PUBLIC, DTDConstants.SDATA, DTDConstants.PI,
                             DTDConstants.STARTTAG, DTDConstants.ENDTAG, DTDConstants.MS,
                             DTDConstants.MD, DTDConstants.SYSTEM};
        java.util.HashSet<Integer> eseen = new java.util.HashSet<Integer>();
        for (int v : entityTypes) {
            check(v >= 10 && v <= 17, "entity type value " + v + " in 10..17");
            check(eseen.add(v), "entity type value " + v + " unique");
        }
        // the three high-bit flags are powers of two and pairwise combinable without collision
        check(Integer.bitCount(DTDConstants.GENERAL) == 1, "GENERAL is a power of two");
        check(Integer.bitCount(DTDConstants.DEFAULT) == 1, "DEFAULT is a power of two");
        check(Integer.bitCount(DTDConstants.PARAMETER) == 1, "PARAMETER is a power of two");
        check((DTDConstants.GENERAL | DTDConstants.PARAMETER) == (65536 | 262144),
              "GENERAL|PARAMETER combines");
        // flag bits never collide with the small type values
        check((DTDConstants.GENERAL & 0xFFFF) == 0 && (DTDConstants.DEFAULT & 0xFFFF) == 0
              && (DTDConstants.PARAMETER & 0xFFFF) == 0, "flags live above the 16-bit type range");
        // implementors see the constants as their interface
        check(new javax.swing.text.html.parser.AttributeList("x") instanceof DTDConstants,
              "AttributeList implements DTDConstants");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepConstantShape();
        testDeepConstantRelations();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

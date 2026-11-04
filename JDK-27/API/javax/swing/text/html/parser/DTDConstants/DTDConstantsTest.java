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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

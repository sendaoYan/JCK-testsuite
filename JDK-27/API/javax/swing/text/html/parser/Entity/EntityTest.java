/*
 * @test
 * @summary javax.swing.text.html.parser.Entity conformance: white-box structure + black-box behavior.
 * @run main EntityTest
 */

import java.lang.reflect.Modifier;

import javax.swing.text.html.parser.DTD;
import javax.swing.text.html.parser.DTDConstants;
import javax.swing.text.html.parser.Entity;
import javax.swing.text.html.parser.ParserDelegator;

public class EntityTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.parser.Entity");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.text.html.parser.DTDConstants"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // static name2type converter
        check(Entity.name2type("PUBLIC") == DTDConstants.PUBLIC, "name2type PUBLIC");
        check(Entity.name2type("CDATA") == DTDConstants.CDATA, "name2type CDATA");
        check(Entity.name2type("SDATA") == DTDConstants.SDATA, "name2type SDATA");
        check(Entity.name2type("STARTTAG") == DTDConstants.STARTTAG, "name2type STARTTAG");
        check(Entity.name2type("ENDTAG") == DTDConstants.ENDTAG, "name2type ENDTAG");
        check(Entity.name2type("unknown") == DTDConstants.CDATA, "name2type unknown -> CDATA");

        // entities from the loaded html32 DTD
        new ParserDelegator();
        DTD dtd = DTD.getDTD("html32");

        Entity amp = dtd.getEntity("amp");
        check(amp != null, "amp entity non-null");
        check("amp".equals(amp.getName()), "amp name");
        check(amp.getType() == DTDConstants.CDATA, "amp type CDATA");
        check(amp.isGeneral(), "amp isGeneral");
        check(!amp.isParameter(), "amp isParameter false");
        check("&".equals(amp.getString()), "amp string &");
        check(amp.getData().length == 1 && amp.getData()[0] == '&', "amp data char");

        check(">".equals(dtd.getEntity("gt").getString()), "gt string >");
        check("<".equals(dtd.getEntity("lt").getString()), "lt string <");
        check(dtd.getEntity("no-such-entity") == null, "unknown entity null");

        // constructor: general entity
        char[] xdata = new char[] { 'x' };
        Entity gen = new Entity("foo", DTDConstants.GENERAL | DTDConstants.CDATA, xdata);
        check("foo".equals(gen.getName()), "ctor name");
        check(gen.getType() == DTDConstants.CDATA, "ctor getType masks to CDATA");
        check(gen.isGeneral(), "ctor isGeneral");
        check(!gen.isParameter(), "ctor isParameter false");
        check("x".equals(gen.getString()), "ctor getString");
        check(gen.getData() == xdata, "ctor getData returns backing array");

        // constructor: parameter entity
        Entity par = new Entity("pct", DTDConstants.PARAMETER | DTDConstants.CDATA, new char[] { '%' });
        check(par.isParameter(), "parameter entity isParameter");
        check(!par.isGeneral(), "parameter entity isGeneral false");
        check(par.getType() == DTDConstants.CDATA, "parameter entity type CDATA");
        check("%".equals(par.getString()), "parameter entity string");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

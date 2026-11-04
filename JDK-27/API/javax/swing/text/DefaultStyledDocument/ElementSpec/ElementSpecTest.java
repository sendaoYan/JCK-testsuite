/*
 * @test
 * @summary javax.swing.text.DefaultStyledDocument.ElementSpec conformance: white-box structure + black-box behavior.
 * @run main ElementSpecTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.DefaultStyledDocument.ElementSpec;
import javax.swing.text.SimpleAttributeSet;

public class ElementSpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.DefaultStyledDocument$ElementSpec");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Type/direction constants.
        check(ElementSpec.StartTagType == 1, "StartTagType 1");
        check(ElementSpec.EndTagType == 2, "EndTagType 2");
        check(ElementSpec.ContentType == 3, "ContentType 3");
        check(ElementSpec.JoinPreviousDirection == 4, "JoinPreviousDirection 4");
        check(ElementSpec.JoinNextDirection == 5, "JoinNextDirection 5");
        check(ElementSpec.OriginateDirection == 6, "OriginateDirection 6");
        check(ElementSpec.JoinFractureDirection == 7, "JoinFractureDirection 7");

        SimpleAttributeSet attr = new SimpleAttributeSet();
        attr.addAttribute("k", "v");
        char[] txt = "hello".toCharArray();
        ElementSpec spec = new ElementSpec(attr, ElementSpec.ContentType, txt, 1, 3);

        check(spec.getType() == ElementSpec.ContentType, "getType ContentType");
        check(spec.getDirection() == ElementSpec.OriginateDirection, "default direction Originate");
        check(spec.getAttributes() == attr, "getAttributes returns supplied set");
        check(spec.getOffset() == 1, "getOffset");
        check(spec.getLength() == 3, "getLength");
        char[] arr = spec.getArray();
        check(arr != null && arr.length == 5, "getArray returns full array copy");
        check(arr[0] == 'h' && arr[4] == 'o', "getArray content");

        // getArray returns a defensive copy.
        arr[0] = 'X';
        char[] again = spec.getArray();
        check(again[0] == 'h', "getArray is a defensive copy");

        // type/direction mutation round-trip.
        spec.setType(ElementSpec.StartTagType);
        check(spec.getType() == ElementSpec.StartTagType, "setType round-trip");
        spec.setDirection(ElementSpec.JoinNextDirection);
        check(spec.getDirection() == ElementSpec.JoinNextDirection, "setDirection round-trip");

        // toString reflects type/direction/length.
        check("StartTag:JoinNext:3".equals(spec.toString()), "toString StartTag JoinNext");

        // markup-only constructor.
        ElementSpec markup = new ElementSpec(attr, ElementSpec.StartTagType);
        check(markup.getArray() == null, "markup spec has null array");
        check(markup.getLength() == 0 && markup.getOffset() == 0, "markup spec zero length/offset");
        check("StartTag:Originate:0".equals(markup.toString()), "markup toString");

        // length-only constructor.
        ElementSpec lenSpec = new ElementSpec(null, ElementSpec.ContentType, 7);
        check(lenSpec.getLength() == 7, "length constructor");
        check(lenSpec.getArray() == null && lenSpec.getAttributes() == null, "length constructor null array/attrs");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.swing.text.DefaultStyledDocument.ElementSpec conformance: type/direction constants, constructors, array views, mutators (headless-safe).
 * @run main ElementSpecTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;

public class ElementSpecTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.DefaultStyledDocument$ElementSpec", false, ElementSpecTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setType"), "method setType");
        check(names.contains("getType"), "method getType");
        check(names.contains("setDirection"), "method setDirection");
        check(names.contains("getDirection"), "method getDirection");
        check(names.contains("getAttributes"), "method getAttributes");
        check(names.contains("getArray"), "method getArray");
        check(names.contains("getOffset"), "method getOffset");
        check(names.contains("getLength"), "method getLength");

        // spec constants
        Field[] fields = c.getFields();
        int typeConsts = 0, dirConsts = 0;
        for (Field f : fields) {
            if (f.getName().endsWith("Type")) typeConsts++;
            if (f.getName().endsWith("Direction")) dirConsts++;
        }
        check(typeConsts == 3, "three type constants");
        check(dirConsts == 4, "four direction constants");
        check(DefaultStyledDocument.ElementSpec.StartTagType == 1, "StartTagType 1");
        check(DefaultStyledDocument.ElementSpec.EndTagType == 2, "EndTagType 2");
        check(DefaultStyledDocument.ElementSpec.ContentType == 3, "ContentType 3");
        check(DefaultStyledDocument.ElementSpec.JoinPreviousDirection == 4, "JoinPreviousDirection 4");
        check(DefaultStyledDocument.ElementSpec.JoinNextDirection == 5, "JoinNextDirection 5");
        check(DefaultStyledDocument.ElementSpec.OriginateDirection == 6, "OriginateDirection 6");
        check(DefaultStyledDocument.ElementSpec.JoinFractureDirection == 7, "JoinFractureDirection 7");

        testConstructorsAndViews();
        testMutators();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testConstructorsAndViews() {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        javax.swing.text.StyleConstants.setBold(attrs, true);

        // two-argument form: no content array
        DefaultStyledDocument.ElementSpec tag =
                new DefaultStyledDocument.ElementSpec(attrs, DefaultStyledDocument.ElementSpec.StartTagType);
        check(tag.getType() == DefaultStyledDocument.ElementSpec.StartTagType, "tag type");
        check(tag.getDirection() == DefaultStyledDocument.ElementSpec.OriginateDirection, "default direction Originate");
        check(tag.getAttributes() == attrs, "attributes identity");
        check(tag.getArray() == null, "no array by default");
        check(tag.getOffset() == 0 && tag.getLength() == 0, "zero offset/length without array");

        // three-argument form: length without an array
        DefaultStyledDocument.ElementSpec sized =
                new DefaultStyledDocument.ElementSpec(attrs, DefaultStyledDocument.ElementSpec.ContentType, 12);
        check(sized.getLength() == 12, "explicit length");
        check(sized.getArray() == null, "still no array");

        // array form (JDK-27): the constructor defensively copies the buffer, so
        // getArray() returns a snapshot, not the caller's array. (JDK-8 kept the
        // caller's array by reference.)
        char[] data = "0123456789".toCharArray();
        DefaultStyledDocument.ElementSpec content =
                new DefaultStyledDocument.ElementSpec(attrs, DefaultStyledDocument.ElementSpec.ContentType, data, 2, 5);
        check(content.getArray() != data, "array defensively copied (JDK-27)");
        check(content.getOffset() == 2, "window offset");
        check(content.getLength() == 5, "window length");
        check(content.getArray().length == data.length, "copy preserves the full buffer length");
        check(content.getArray()[2] == '2', "window starts at the given offset");
        // mutating the source is NOT visible through the spec
        data[2] = 'X';
        check(content.getArray()[2] == '2', "copy is isolated from later source mutation");
        data[2] = '2';
    }

    static void testMutators() {
        DefaultStyledDocument.ElementSpec spec =
                new DefaultStyledDocument.ElementSpec(null, DefaultStyledDocument.ElementSpec.EndTagType);
        check(spec.getAttributes() == null, "null attributes tolerated");
        check(spec.getType() == DefaultStyledDocument.ElementSpec.EndTagType, "end tag type");
        spec.setType(DefaultStyledDocument.ElementSpec.ContentType);
        check(spec.getType() == DefaultStyledDocument.ElementSpec.ContentType, "setType");
        spec.setDirection(DefaultStyledDocument.ElementSpec.JoinNextDirection);
        check(spec.getDirection() == DefaultStyledDocument.ElementSpec.JoinNextDirection, "setDirection");
        spec.setDirection(DefaultStyledDocument.ElementSpec.JoinFractureDirection);
        check(spec.getDirection() == DefaultStyledDocument.ElementSpec.JoinFractureDirection, "direction reassigned");
        check(spec.toString().contains("content") || spec.toString() != null, "toString present");
    }
}

/*
 * @test
 * @summary jdk.internal.util.xml.impl.XMLStreamWriterImpl conformance: type shape via reflection (headless-safe).
 * @run main XMLStreamWriterImplTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class XMLStreamWriterImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.util.xml.impl.XMLStreamWriterImpl", false, XMLStreamWriterImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasInterface(c, "jdk.internal.util.xml.XMLStreamWriter"), "implements jdk.internal.util.xml.XMLStreamWriter");
        check(hasMethod(c, "close"), "declares close");
        check(hasMethod(c, "flush"), "declares flush");
        check(hasMethod(c, "setDoIndent"), "declares setDoIndent");
        check(hasMethod(c, "writeAttribute"), "declares writeAttribute");
        check(hasMethod(c, "writeCData"), "declares writeCData");
        check(hasMethod(c, "writeCharacters"), "declares writeCharacters");
        check(hasMethod(c, "writeDTD"), "declares writeDTD");
        check(hasMethod(c, "writeEmptyElement"), "declares writeEmptyElement");
        check(hasField(c, "AMPERSAND"), "declares field AMPERSAND");
        check(hasField(c, "CLOSE_EMPTY_ELEMENT"), "declares field CLOSE_EMPTY_ELEMENT");
        check(hasField(c, "CLOSE_END_TAG"), "declares field CLOSE_END_TAG");
        check(hasField(c, "CLOSE_START_TAG"), "declares field CLOSE_START_TAG");
        check(hasField(c, "DOUBLEQUOT"), "declares field DOUBLEQUOT");
        check(hasField(c, "ENCODING_PREFIX"), "declares field ENCODING_PREFIX");
        check(hasField(c, "END_CDATA"), "declares field END_CDATA");
        check(hasField(c, "OPEN_END_TAG"), "declares field OPEN_END_TAG");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

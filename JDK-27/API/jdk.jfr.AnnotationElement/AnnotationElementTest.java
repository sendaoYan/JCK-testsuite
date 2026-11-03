/*
 * @test
 * @summary jdk.jfr.AnnotationElement conformance: white-box structure + black-box construction/read.
 * @run main AnnotationElementTest
 */

import jdk.jfr.*;
import java.lang.reflect.*;
import java.util.Map;

public class AnnotationElementTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.AnnotationElement");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(hasMethod(c, "getTypeName"), "getTypeName");
        check(hasMethod(c, "getValue"), "getValue");
        check(hasMethod(c, "hasValue"), "hasValue");
        check(hasMethod(c, "getValues"), "getValues");
        check(hasMethod(c, "getValueDescriptors"), "getValueDescriptors");
    }

    static void testBlackBox() {
        AnnotationElement ae = new AnnotationElement(Label.class, "my-label");
        check("jdk.jfr.Label".equals(ae.getTypeName()), "type name");
        check(ae.hasValue("value"), "has value");
        check("my-label".equals(ae.getValue("value")), "value");
        check(ae.getValueDescriptors() != null, "value descriptors");
        check(ae.getAnnotationElements() != null, "annotation elements");
        check(ae.getValues() != null, "values");
        AnnotationElement ae2 = new AnnotationElement(Label.class, Map.of("value", "default"));
        check("default".equals(ae2.getValue("value")), "map value");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

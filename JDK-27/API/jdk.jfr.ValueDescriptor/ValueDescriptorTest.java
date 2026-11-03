/*
 * @test
 * @summary jdk.jfr.ValueDescriptor conformance: white-box structure + black-box construction/read.
 * @run main ValueDescriptorTest
 */

import jdk.jfr.*;
import java.util.List;
import java.lang.reflect.*;

public class ValueDescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.ValueDescriptor");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(hasMethod(c, "getName"), "getName");
        check(hasMethod(c, "getTypeName"), "getTypeName");
        check(hasMethod(c, "getLabel"), "getLabel");
        check(hasMethod(c, "isArray"), "isArray");
        check(hasMethod(c, "getFields"), "getFields");
    }

    static void testBlackBox() {
        ValueDescriptor vd = new ValueDescriptor(String.class, "name", List.of(new AnnotationElement(Label.class, "Name")));
        check("name".equals(vd.getName()), "name");
        check("java.lang.String".equals(vd.getTypeName()), "type name");
        check("Name".equals(vd.getLabel()), "label");
        check(vd.getAnnotationElements() != null && vd.getAnnotationElements().size() >= 1, "annotation elements");
        check(!vd.isArray(), "not array");
        check(vd.getFields() != null, "fields list");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary jdk.jfr.ValueDescriptor conformance: construct and read name/type/label.
 * @run main ValueDescriptorTest
 */

import jdk.jfr.*;
import java.util.List;

public class ValueDescriptorTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        ValueDescriptor vd = new ValueDescriptor(String.class, "name", List.of(new AnnotationElement(Label.class, "Name")));
        check("name".equals(vd.getName()), "name");
        check("java.lang.String".equals(vd.getTypeName()), "type name");
        check("Name".equals(vd.getLabel()), "label");
        check(vd.getAnnotationElements() != null && vd.getAnnotationElements().size() >= 1, "annotation elements");
        check(!vd.isArray(), "not array");
        check(vd.getFields() != null, "fields list");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary jdk.jfr.AnnotationElement conformance: construct from annotation class and read values.
 * @run main AnnotationElementTest
 */

import jdk.jfr.*;

public class AnnotationElementTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        AnnotationElement ae = new AnnotationElement(Label.class, "my-label");
        check("jdk.jfr.Label".equals(ae.getTypeName()), "type name");
        check(ae.hasValue("value"), "has value");
        check("my-label".equals(ae.getValue("value")), "value");
        check(ae.getValueDescriptors() != null, "value descriptors");
        check(ae.getAnnotationElements() != null, "annotation elements");
        check(ae.getValues() != null, "values");
        AnnotationElement ae2 = new AnnotationElement(Label.class, java.util.Map.of("value", "default"));
        check("default".equals(ae2.getValue("value")), "map value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

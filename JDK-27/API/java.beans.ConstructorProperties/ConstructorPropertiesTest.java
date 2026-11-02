/*
 * @test
 * @summary java.beans.ConstructorProperties conformance: annotation shape.
 * @run main ConstructorPropertiesTest
 */

import java.beans.ConstructorProperties;

public class ConstructorPropertiesTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        check(ConstructorProperties.class.isAnnotation(), "annotation");
        check(ConstructorProperties.class.getAnnotation(java.lang.annotation.Retention.class).value() == java.lang.annotation.RetentionPolicy.RUNTIME, "runtime retention");
        check(ConstructorProperties.class.getMethod("value").getReturnType() == String[].class, "String[] value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

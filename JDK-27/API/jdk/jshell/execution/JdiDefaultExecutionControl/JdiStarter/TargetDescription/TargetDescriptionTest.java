/*
 * @test
 * @summary jdk.jshell.execution.JdiDefaultExecutionControl.JdiStarter.TargetDescription conformance: target description record.
 * @run main TargetDescriptionTest
 */

import java.lang.reflect.*;

public class TargetDescriptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.execution.JdiDefaultExecutionControl$JdiStarter$TargetDescription");
        check(!c.isInterface() && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isRecord(), "is record");
        check(c.getRecordComponents().length >= 2, "record components");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

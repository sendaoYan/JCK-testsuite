/*
 * @test
 * @summary jdk.jshell.execution.JdiDefaultExecutionControl.JdiStarter.TargetDescription conformance: target description record.
 * @run main TargetDescriptionTest
 */

import java.lang.reflect.*;

public class TargetDescriptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- deep contract: record shape (vm, process) with accessors ----
    static void testDeepShape() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.execution.JdiDefaultExecutionControl$JdiStarter$TargetDescription");
        java.lang.reflect.RecordComponent[] comps = c.getRecordComponents();
        check(comps.length == 2, "two record components");
        check(comps[0].getName().equals("vm") && comps[0].getType().getName().equals("com.sun.jdi.VirtualMachine"),
              "component vm: VirtualMachine");
        check(comps[1].getName().equals("process") && comps[1].getType().getName().equals("java.lang.Process"),
              "component process: Process");
        check(comps[0].getAccessor().getName().equals("vm"), "accessor vm()");
        check(comps[1].getAccessor().getName().equals("process"), "accessor process()");
        // canonical constructor matches the component order
        java.lang.reflect.Constructor<?> cc = c.getDeclaredConstructor(com.sun.jdi.VirtualMachine.class, Process.class);
        check(cc != null, "canonical constructor (VirtualMachine, Process)");
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.execution.JdiDefaultExecutionControl$JdiStarter$TargetDescription");
        check(!c.isInterface() && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isRecord(), "is record");
        check(c.getRecordComponents().length >= 2, "record components");
        testDeepShape();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

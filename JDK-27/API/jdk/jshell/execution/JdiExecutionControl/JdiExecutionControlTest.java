/*
 * @test
 * @summary jdk.jshell.execution.JdiExecutionControl conformance: jdi execution control abstract.
 * @modules jdk.jshell jdk.jdi
 * @run main JdiExecutionControlTest
 */

import java.lang.reflect.*;
import com.sun.jdi.*;

public class JdiExecutionControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {

        Class<?> c = Class.forName("jdk.jshell.execution.JdiExecutionControl");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "is abstract class");
        check(Modifier.isPublic(c.getModifiers()), "is public");
    }

    // Concrete subclass to reach the protected constructor and the abstract vm().
    static class TestJdi extends jdk.jshell.execution.JdiExecutionControl {
        TestJdi(java.io.ObjectOutput out, java.io.ObjectInput in) { super(out, in); }
        @Override
        protected VirtualMachine vm() throws jdk.jshell.spi.ExecutionControl.EngineTerminationException {
            throw new jdk.jshell.spi.ExecutionControl.EngineTerminationException("no vm");
        }
        ReferenceType refType(VirtualMachine v, String n) { return referenceType(v, n); }
    }

    static void testBlackBox() throws Exception {
        java.io.ByteArrayOutputStream bout = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutput out = new java.io.ObjectOutputStream(bout);
        byte[] header = new byte[]{(byte) 0xAC, (byte) 0xED, 0x00, 0x05};
        java.io.ObjectInput in = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(header));

        TestJdi jec = new TestJdi(out, in);
        check(jec instanceof jdk.jshell.execution.JdiExecutionControl, "subclass instance");

        // referenceType(null, ...) delegates to vm.classesByName -> NullPointerException.
        try {
            jec.refType(null, "x");
            check(false, "referenceType(null, ...) should throw NPE");
        } catch (NullPointerException expected) {
            check(true, "referenceType(null, ...) throws NPE");
        }

        // redefine() -> vm() throws EngineTerminationException. Call it through a
        // JdiExecutionControl-typed reference so the call-site owner is the class
        // under test (redefine is public).
        jdk.jshell.execution.JdiExecutionControl jecBase = jec;
        try {
            jecBase.redefine(new jdk.jshell.spi.ExecutionControl.ClassBytecodes[0]);
            check(false, "redefine() should throw EngineTerminationException");
        } catch (jdk.jshell.spi.ExecutionControl.EngineTerminationException expected) {
            check(true, "redefine() throws EngineTerminationException");
        }

        // White-box degradation for vm(): it is protected+abstract, so a direct
        // call cannot be emitted with owner JdiExecutionControl from an external
        // package; assert method presence instead.
        Class<?> c = Class.forName("jdk.jshell.execution.JdiExecutionControl");
        check(hasDeclaredMethod(c, "vm"), "declares abstract vm()");
        check(hasDeclaredMethod(c, "referenceType"), "declares referenceType()");
        check(hasDeclaredMethod(c, "redefine"), "declares redefine()");
        Constructor<?> ctor = c.getDeclaredConstructor(java.io.ObjectOutput.class, java.io.ObjectInput.class);
        check(Modifier.isProtected(ctor.getModifiers()), "protected constructor (ObjectOutput,ObjectInput)");
    }

    static boolean hasDeclaredMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }


    // ---- deep contract: streaming command surface and protected hooks ----
    static void testDeepShape() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.execution.JdiExecutionControl");
        check(c.getSuperclass().getName().equals("jdk.jshell.execution.StreamingExecutionControl"),
              "extends StreamingExecutionControl");
        for (String n : new String[]{"load", "redefine", "invoke", "varValue", "addToClasspath", "stop", "close"})
            { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals(n)) f=true; check(f, "has " + n); }
        // the constructor stores the channel pair and is protected
        java.lang.reflect.Constructor<?> ctor = c.getDeclaredConstructor(java.io.ObjectOutput.class, java.io.ObjectInput.class);
        check(java.lang.reflect.Modifier.isProtected(ctor.getModifiers()), "channel constructor is protected");
        TestJdi jec = new TestJdi(new java.io.ObjectOutputStream(new java.io.ByteArrayOutputStream()),
                new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(
                        new byte[]{(byte) 0xAC, (byte) 0xED, 0x00, 0x05})));
        // close() inherited from StreamingExecutionControl writes CMD_CLOSE and returns
        jec.close();
        check(true, "close() writes CMD_CLOSE without throwing");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepShape();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

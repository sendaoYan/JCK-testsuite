/*
 * @test
 * @summary javax.rmi.CORBA.ValueHandlerMultiFormat conformance: interface face + JDK stream-format capability.
 * @run main ValueHandlerMultiFormatTest
 */

import java.io.Serializable;
import java.lang.reflect.*;
import javax.rmi.CORBA.Util;
import javax.rmi.CORBA.ValueHandler;
import javax.rmi.CORBA.ValueHandlerMultiFormat;

public class ValueHandlerMultiFormatTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static boolean hasOwn(Class<?> c, String name, Class<?>... params) {
        try { c.getDeclaredMethod(name, params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.rmi.CORBA.ValueHandlerMultiFormat");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Class.forName("javax.rmi.CORBA.ValueHandler").isAssignableFrom(c),
                "implements/extends javax.rmi.CORBA.ValueHandler");
        check(c.getInterfaces().length == 1
                && c.getInterfaces()[0] == Class.forName("javax.rmi.CORBA.ValueHandler"),
                "exactly one super-interface: ValueHandler");
        check(hasMethod(c, "getMaximumStreamFormatVersion"), "declares getMaximumStreamFormatVersion");
        check(hasMethod(c, "writeValue"), "declares writeValue");
        check(c.getDeclaredMethods().length == 2, "exactly 2 declared methods");
        // exact signatures
        check(hasOwn(c, "getMaximumStreamFormatVersion"), "byte getMaximumStreamFormatVersion()");
        check(c.getDeclaredMethod("getMaximumStreamFormatVersion").getReturnType() == byte.class,
                "getMaximumStreamFormatVersion returns byte");
        check(hasOwn(c, "writeValue", org.omg.CORBA.portable.OutputStream.class, Serializable.class, byte.class),
                "void writeValue(OutputStream, Serializable, byte)");
        for (Method m : c.getDeclaredMethods()) {
            check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()),
                    "method " + m.getName() + " is public abstract");
        }
    }

    // ---- Part 2: black-box (the real JDK handler) ----
    static void testBlackBox() throws Exception {
        ValueHandler vh = Util.createValueHandler();
        check(vh instanceof ValueHandlerMultiFormat, "JDK handler implements the multi-format extension");
        ValueHandlerMultiFormat mf = (ValueHandlerMultiFormat) vh;
        // this JDK supports both GIOP stream format versions 1 and 2
        byte max = mf.getMaximumStreamFormatVersion();
        check(max >= 1 && max <= 2, "maximum stream format version within the defined range (got " + max + ")");
        check(max == (byte) 2, "this JDK reports maximum stream format version 2");
        // the inherited single-format API remains available on the same object
        check(mf.getRMIRepositoryID(String.class) != null, "inherited getRMIRepositoryID available");
        check(mf.isCustomMarshaled(String.class) == false, "inherited isCustomMarshaled available");
        // writeValue(3-arg) exists alongside the inherited 2-arg form on the same instance
        check(hasOwn(mf.getClass(), "writeValue", org.omg.CORBA.portable.OutputStream.class,
                        Serializable.class, byte.class)
                        || mf.getClass().getMethod("writeValue", org.omg.CORBA.portable.OutputStream.class,
                        Serializable.class, byte.class) != null,
                "concrete handler declares the 3-arg writeValue");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

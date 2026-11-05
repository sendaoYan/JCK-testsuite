/*
 * @test
 * @summary javax.rmi.CORBA.ValueHandler conformance: interface face + real JDK handler contracts.
 * @run main ValueHandlerTest
 */

import java.io.*;
import java.lang.reflect.*;
import org.omg.SendingContext.RunTime;
import javax.rmi.CORBA.Util;
import javax.rmi.CORBA.ValueHandler;
import javax.rmi.CORBA.ValueHandlerMultiFormat;

public class ValueHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static boolean hasOwn(Class<?> c, String name, Class<?>... params) {
        try { c.getDeclaredMethod(name, params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    /** value class with custom writeObject: must be reported as custom marshaled */
    public static class CustomValue implements Serializable {
        private void writeObject(ObjectOutputStream out) {}
        private void readObject(ObjectInputStream in) {}
    }

    public static class PlainValue implements Serializable {}

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.rmi.CORBA.ValueHandler");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 0, "no super-interfaces");
        check(hasMethod(c, "getRMIRepositoryID"), "declares getRMIRepositoryID");
        check(hasMethod(c, "getRunTimeCodeBase"), "declares getRunTimeCodeBase");
        check(hasMethod(c, "isCustomMarshaled"), "declares isCustomMarshaled");
        check(c.getDeclaredMethods().length == 6, "exactly 6 declared methods");
        // exact signatures
        check(hasOwn(c, "writeValue", org.omg.CORBA.portable.OutputStream.class, Serializable.class),
                "void writeValue(OutputStream, Serializable)");
        check(hasOwn(c, "readValue", org.omg.CORBA.portable.InputStream.class, int.class, Class.class,
                String.class, RunTime.class),
                "Serializable readValue(InputStream, int, Class, String, RunTime)");
        check(hasOwn(c, "getRMIRepositoryID", Class.class), "String getRMIRepositoryID(Class)");
        check(hasOwn(c, "isCustomMarshaled", Class.class), "boolean isCustomMarshaled(Class)");
        check(hasOwn(c, "writeReplace", Serializable.class), "Serializable writeReplace(Serializable)");
        for (Method m : c.getDeclaredMethods()) {
            check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()),
                    "method " + m.getName() + " is public abstract");
        }
    }

    // ---- Part 2: black-box (the real JDK handler from Util.createValueHandler) ----
    static void testBlackBox() throws Exception {
        ValueHandler vh = Util.createValueHandler();
        check(vh != null, "createValueHandler returns a handler");
        check(vh == Util.createValueHandler(), "createValueHandler returns a shared instance");
        check(vh instanceof ValueHandlerMultiFormat, "the JDK handler also implements ValueHandlerMultiFormat");

        // getRMIRepositoryID: special IDL mapping for java.lang.String
        check("IDL:omg.org/CORBA/WStringValue:1.0".equals(vh.getRMIRepositoryID(String.class)),
                "String maps to the IDL WStringValue repository id");
        // ... and the RMI:<class>:<SUID> form for ordinary serializable classes
        String repObject = vh.getRMIRepositoryID(Object.class);
        check(repObject.startsWith("RMI:java.lang.Object:"), "Object repository id prefix");
        check(repObject.length() == "RMI:java.lang.Object:".length() + 16,
                "repository id carries the 16-hex-digit SUID");
        check("RMI:java.lang.Object:0000000000000000".equals(repObject),
                "Object has no explicit SUID so zero hash is used");
        String repCustom = vh.getRMIRepositoryID(CustomValue.class);
        check(repCustom.startsWith("RMI:") && repCustom.contains("CustomValue"),
                "own value class repository id uses the RMI: form");
        check(repCustom.contains("\\U0024"),
                "nested-class $ in repository ids is escaped as \\U0024");
        // serializable value classes carry a second member hash after the SUID
        String repPlain = vh.getRMIRepositoryID(PlainValue.class);
        check(repPlain.startsWith("RMI:") && repPlain.length() > "RMI:".length() + 16 + 1 + 16,
                "plain value class repository id carries SUID plus member hash");
        // stable/deterministic
        check(vh.getRMIRepositoryID(String.class).equals(vh.getRMIRepositoryID(String.class)),
                "getRMIRepositoryID is deterministic");

        // isCustomMarshaled: only classes with writeObject/readObject customization
        check(!vh.isCustomMarshaled(String.class), "String is not custom marshaled");
        check(!vh.isCustomMarshaled(PlainValue.class), "plain Serializable class is not custom marshaled");
        check(vh.isCustomMarshaled(CustomValue.class), "class with writeObject is custom marshaled");
        // a non-serializable class has no ObjectStreamClass -> NPE (implementation behavior)
        try {
            vh.isCustomMarshaled(ValueHandlerTest.class);
            check(false, "expected NPE for isCustomMarshaled(non-serializable)");
        } catch (NullPointerException expected) {
            check(true, "isCustomMarshaled(non-serializable) -> NPE");
        }

        // writeReplace: identity for values that do not declare replaceable semantics
        PlainValue pv = new PlainValue();
        check(vh.writeReplace(pv) == pv, "writeReplace passes a plain value through");
        check(vh.writeReplace("s") instanceof String, "writeReplace of a String stays a String");

        // getRunTimeCodeBase: a non-null RunTime servicing the local codebase
        RunTime rt = vh.getRunTimeCodeBase();
        check(rt != null, "getRunTimeCodeBase non-null");
        check(rt instanceof RunTime, "code base implements org.omg.SendingContext.RunTime");
        check(vh.getRunTimeCodeBase() == rt, "getRunTimeCodeBase is stable across calls");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

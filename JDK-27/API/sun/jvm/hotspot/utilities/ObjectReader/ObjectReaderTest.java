/*
 * @test
 * @summary sun.jvm.hotspot.utilities.ObjectReader conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.utilities:+open
 * @run main ObjectReaderTest
 */
import java.lang.reflect.*;

public class ObjectReaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasField(Class<?> c, String n) {
        for (Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.jvm.hotspot.utilities.ObjectReader", false, ObjectReaderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "readObject"), "method readObject");
        check(hasMethod(c, "readMethod"), "method readMethod");
        check(hasMethod(c, "javaLangString"), "method javaLangString");
        check(hasMethod(c, "readInstance"), "method readInstance");
        check(hasMethod(c, "readPrimitiveArray"), "method readPrimitiveArray");
        check(hasMethod(c, "readObjectArray"), "method readObjectArray");
        check(hasMethod(c, "javaUtilHashtableEntry"), "method javaUtilHashtableEntry");
        check(hasMethod(c, "javaUtilHashtable"), "method javaUtilHashtable");
        check(hasMethod(c, "javaUtilProperties"), "method javaUtilProperties");
        check(hasMethod(c, "getFromObjTable"), "method getFromObjTable");
        check(hasMethod(c, "getHashtable"), "method getHashtable");
        check(hasMethod(c, "readClass"), "method readClass");
        check(hasMethod(c, "getDefaultPrimitiveValue"), "method getDefaultPrimitiveValue");
        check(hasMethod(c, "putIntoObjTable"), "method putIntoObjTable");
        check(hasMethod(c, "readConstructor"), "method readConstructor");
        check(hasMethod(c, "getParamTypes"), "method getParamTypes");
        check(hasMethod(c, "isRobust"), "method isRobust");
        check(hasMethod(c, "readMethodOrConstructor"), "method readMethodOrConstructor");
        check(hasMethod(c, "readField"), "method readField");
        check(hasField(c, "javaLangString"), "field javaLangString");
        check(hasField(c, "javaUtilHashtableEntry"), "field javaUtilHashtableEntry");
        check(hasField(c, "javaUtilHashtable"), "field javaUtilHashtable");
        check(hasField(c, "javaUtilProperties"), "field javaUtilProperties");
        check(hasField(c, "cl"), "field cl");
        check(hasField(c, "oopToObjMap"), "field oopToObjMap");
        check(hasField(c, "fieldMap"), "field fieldMap");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

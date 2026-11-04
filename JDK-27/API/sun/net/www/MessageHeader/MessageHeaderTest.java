/*
 * @test
 * @summary sun.net.www.MessageHeader conformance: internal package reflection.
 * @modules java.base/sun.net.www:+open
 * @run main MessageHeaderTest
 */
public class MessageHeaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.MessageHeader", false, MessageHeaderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "add"), "method add");
        check(hasMethod(c, "canonicalID"), "method canonicalID");
        check(hasMethod(c, "filterAndAddHeaders"), "method filterAndAddHeaders");
        check(hasMethod(c, "filterNTLMResponses"), "method filterNTLMResponses");
        check(hasMethod(c, "findNextValue"), "method findNextValue");
        check(hasMethod(c, "findValue"), "method findValue");
        check(hasMethod(c, "getHeaderNamesInList"), "method getHeaderNamesInList");
        check(hasMethod(c, "getHeaders"), "method getHeaders");
        check(hasMethod(c, "getKey"), "method getKey");
        check(hasMethod(c, "getValue"), "method getValue");
        check(hasMethod(c, "mergeHeader"), "method mergeHeader");
        check(hasMethod(c, "multiValueIterator"), "method multiValueIterator");
        check(hasMethod(c, "parseHeader"), "method parseHeader");
        check(hasMethod(c, "prepend"), "method prepend");
        check(hasMethod(c, "print"), "method print");
        check(hasMethod(c, "remove"), "method remove");
        check(hasMethod(c, "reset"), "method reset");
        check(hasMethod(c, "set"), "method set");
        check(hasMethod(c, "setIfNotSet"), "method setIfNotSet");
        check(hasMethod(c, "toString"), "method toString");
        check(hasField(c, "keys"), "field keys");
        check(hasField(c, "maxHeaderSize"), "field maxHeaderSize");
        check(hasField(c, "nkeys"), "field nkeys");
        check(hasField(c, "size"), "field size");
        check(hasField(c, "values"), "field values");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

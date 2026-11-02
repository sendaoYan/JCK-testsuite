/*
 * @test
 * @summary sun.nio.cs.CharsetMapping conformance: internal package reflection.
 * @modules java.base/sun.nio.cs:+open
 * @run main CharsetMappingTest
 */
public class CharsetMappingTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.cs.CharsetMapping", false, CharsetMappingTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "decodeComposite"), "method decodeComposite");
        check(hasMethod(c, "decodeDouble"), "method decodeDouble");
        check(hasMethod(c, "decodeSingle"), "method decodeSingle");
        check(hasMethod(c, "decodeSurrogate"), "method decodeSurrogate");
        check(hasMethod(c, "encodeChar"), "method encodeChar");
        check(hasMethod(c, "encodeComposite"), "method encodeComposite");
        check(hasMethod(c, "encodeSurrogate"), "method encodeSurrogate");
        check(hasMethod(c, "get"), "method get");
        check(hasMethod(c, "isCompositeBase"), "method isCompositeBase");
        check(hasField(c, "MAP_COMPOSITE"), "field MAP_COMPOSITE");
        check(hasField(c, "MAP_DOUBLEBYTE1"), "field MAP_DOUBLEBYTE1");
        check(hasField(c, "MAP_DOUBLEBYTE2"), "field MAP_DOUBLEBYTE2");
        check(hasField(c, "MAP_INDEXC2B"), "field MAP_INDEXC2B");
        check(hasField(c, "MAP_SINGLEBYTE"), "field MAP_SINGLEBYTE");
        check(hasField(c, "MAP_SUPPLEMENT"), "field MAP_SUPPLEMENT");
        check(hasField(c, "MAP_SUPPLEMENT_C2B"), "field MAP_SUPPLEMENT_C2B");
        check(hasField(c, "UNMAPPABLE_DECODING"), "field UNMAPPABLE_DECODING");
        check(hasField(c, "UNMAPPABLE_ENCODING"), "field UNMAPPABLE_ENCODING");
        check(hasField(c, "b1MaxDB1"), "field b1MaxDB1");
        check(hasField(c, "b1MaxDB2"), "field b1MaxDB2");
        check(hasField(c, "b1MinDB1"), "field b1MinDB1");
        check(hasField(c, "b1MinDB2"), "field b1MinDB2");
        check(hasField(c, "b2Max"), "field b2Max");
        check(hasField(c, "b2Min"), "field b2Min");
        check(hasField(c, "b2cComp"), "field b2cComp");
        check(hasField(c, "b2cDB1"), "field b2cDB1");
        check(hasField(c, "b2cDB2"), "field b2cDB2");
        check(hasField(c, "b2cSB"), "field b2cSB");
        check(hasField(c, "b2cSupp"), "field b2cSupp");
        check(hasField(c, "bb"), "field bb");
        check(hasField(c, "c2b"), "field c2b");
        check(hasField(c, "c2bComp"), "field c2bComp");
        check(hasField(c, "c2bIndex"), "field c2bIndex");
        check(hasField(c, "c2bSupp"), "field c2bSupp");
        check(hasField(c, "comparatorBytes"), "field comparatorBytes");
        check(hasField(c, "comparatorCP"), "field comparatorCP");
        check(hasField(c, "comparatorComp"), "field comparatorComp");
        check(hasField(c, "dbSegSize"), "field dbSegSize");
        check(hasField(c, "off"), "field off");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary jdk.internal.util.xml.impl.Parser conformance: internal abstract class reflection.
 * @modules java.base/jdk.internal.util.xml.impl:+open
 * @run main ParserTest
 */
public class ParserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.util.xml.impl.Parser", false, ParserTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.util.xml.impl.Parser"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 60, "declared methods >= 60");
        check(hasM(ms, "bflash"), "method bflash");
        check(hasM(ms, "bflash_ws"), "method bflash_ws");
        check(hasM(ms, "bkch"), "method bkch");
        check(hasM(ms, "chtyp"), "method chtyp");
        check(hasM(ms, "cleanup"), "method cleanup");
        check(hasM(ms, "comm"), "method comm");
        check(hasM(ms, "del"), "method del");
        check(hasM(ms, "docType"), "method docType");
        check(hasM(ms, "eqstr"), "method eqstr");
        check(hasM(ms, "find"), "method find");
        check(hasM(ms, "getch"), "method getch");
        check(hasM(ms, "init"), "method init");
        check(hasM(ms, "name"), "method name");
        check(hasM(ms, "newPrefix"), "method newPrefix");
        check(hasM(ms, "notDecl"), "method notDecl");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 54, "declared fields >= 54");
        check(hasF(fs, "BUFFSIZE_PARSER"), "field BUFFSIZE_PARSER");
        check(hasF(fs, "BUFFSIZE_READER"), "field BUFFSIZE_READER");
        check(hasF(fs, "EOS"), "field EOS");
        check(hasF(fs, "EV_CDAT"), "field EV_CDAT");
        check(hasF(fs, "EV_COMM"), "field EV_COMM");
        check(hasF(fs, "EV_DTD"), "field EV_DTD");
        check(hasF(fs, "EV_ELM"), "field EV_ELM");
        check(hasF(fs, "EV_ELME"), "field EV_ELME");
        check(hasF(fs, "EV_ELMS"), "field EV_ELMS");
        check(hasF(fs, "EV_ENT"), "field EV_ENT");
        check(hasF(fs, "EV_NULL"), "field EV_NULL");
        check(hasF(fs, "EV_PI"), "field EV_PI");
        check(hasF(fs, "EV_TEXT"), "field EV_TEXT");
        check(hasF(fs, "EV_WSPC"), "field EV_WSPC");
        check(hasF(fs, "FAULT"), "field FAULT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

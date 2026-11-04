/*
 * @test
 * @summary jdk.internal.reflect.ConstantPool.Tag conformance: internal package reflection.
 * @modules java.base/jdk.internal.reflect:+open
 * @run main TagTest
 */
public class TagTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.reflect.ConstantPool$Tag", false, TagTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "valueOf"), "method valueOf");
        check(hasMethod(c, "values"), "method values");
        check(hasField(c, "$VALUES"), "field $VALUES");
        check(hasField(c, "CLASS"), "field CLASS");
        check(hasField(c, "DOUBLE"), "field DOUBLE");
        check(hasField(c, "DYNAMIC"), "field DYNAMIC");
        check(hasField(c, "FIELDREF"), "field FIELDREF");
        check(hasField(c, "FLOAT"), "field FLOAT");
        check(hasField(c, "INTEGER"), "field INTEGER");
        check(hasField(c, "INTERFACEMETHODREF"), "field INTERFACEMETHODREF");
        check(hasField(c, "INVALID"), "field INVALID");
        check(hasField(c, "INVOKEDYNAMIC"), "field INVOKEDYNAMIC");
        check(hasField(c, "LONG"), "field LONG");
        check(hasField(c, "METHODHANDLE"), "field METHODHANDLE");
        check(hasField(c, "METHODREF"), "field METHODREF");
        check(hasField(c, "METHODTYPE"), "field METHODTYPE");
        check(hasField(c, "NAMEANDTYPE"), "field NAMEANDTYPE");
        check(hasField(c, "STRING"), "field STRING");
        check(hasField(c, "UTF8"), "field UTF8");
        check(hasField(c, "tagCode"), "field tagCode");
        check(hasEnumConstant(c, "CLASS"), "enum CLASS");
        check(hasEnumConstant(c, "DOUBLE"), "enum DOUBLE");
        check(hasEnumConstant(c, "DYNAMIC"), "enum DYNAMIC");
        check(hasEnumConstant(c, "FIELDREF"), "enum FIELDREF");
        check(hasEnumConstant(c, "FLOAT"), "enum FLOAT");
        check(hasEnumConstant(c, "INTEGER"), "enum INTEGER");
        check(hasEnumConstant(c, "INTERFACEMETHODREF"), "enum INTERFACEMETHODREF");
        check(hasEnumConstant(c, "INVALID"), "enum INVALID");
        check(hasEnumConstant(c, "INVOKEDYNAMIC"), "enum INVOKEDYNAMIC");
        check(hasEnumConstant(c, "LONG"), "enum LONG");
        check(hasEnumConstant(c, "METHODHANDLE"), "enum METHODHANDLE");
        check(hasEnumConstant(c, "METHODREF"), "enum METHODREF");
        check(hasEnumConstant(c, "METHODTYPE"), "enum METHODTYPE");
        check(hasEnumConstant(c, "NAMEANDTYPE"), "enum NAMEANDTYPE");
        check(hasEnumConstant(c, "STRING"), "enum STRING");
        check(hasEnumConstant(c, "UTF8"), "enum UTF8");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

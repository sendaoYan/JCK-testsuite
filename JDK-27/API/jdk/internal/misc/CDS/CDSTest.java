/*
 * @test
 * @summary jdk.internal.misc.CDS conformance: internal package reflection.
 * @modules java.base/jdk.internal.misc:+open
 * @run main CDSTest
 */
public class CDSTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.misc.CDS", false, CDSTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "defineArchivedModules"), "method defineArchivedModules");
        check(hasMethod(c, "getRandomSeedForDumping"), "method getRandomSeedForDumping");
        check(hasMethod(c, "initializeFromArchive"), "method initializeFromArchive");
        check(hasMethod(c, "isDumpingAOTLinkedClasses"), "method isDumpingAOTLinkedClasses");
        check(hasMethod(c, "isDumpingArchive"), "method isDumpingArchive");
        check(hasMethod(c, "isDumpingStaticArchive"), "method isDumpingStaticArchive");
        check(hasMethod(c, "isLoggingLambdaFormInvokers"), "method isLoggingLambdaFormInvokers");
        check(hasMethod(c, "isSingleThreadVM"), "method isSingleThreadVM");
        check(hasMethod(c, "isUsingArchive"), "method isUsingArchive");
        check(hasMethod(c, "keepAlive"), "method keepAlive");
        check(hasMethod(c, "logLambdaFormInvoker"), "method logLambdaFormInvoker");
        check(hasMethod(c, "logSpeciesType"), "method logSpeciesType");
        check(hasMethod(c, "needsClassInitBarrier"), "method needsClassInitBarrier");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "BASIC_FORMS_HOLDER_CLASS_NAME"), "field BASIC_FORMS_HOLDER_CLASS_NAME");
        check(hasField(c, "DELEGATING_HOLDER_CLASS_NAME"), "field DELEGATING_HOLDER_CLASS_NAME");
        check(hasField(c, "DIRECT_HOLDER_CLASS_NAME"), "field DIRECT_HOLDER_CLASS_NAME");
        check(hasField(c, "INVOKERS_HOLDER_CLASS_NAME"), "field INVOKERS_HOLDER_CLASS_NAME");
        check(hasField(c, "IS_DUMPING_AOT_LINKED_CLASSES"), "field IS_DUMPING_AOT_LINKED_CLASSES");
        check(hasField(c, "IS_DUMPING_ARCHIVE"), "field IS_DUMPING_ARCHIVE");
        check(hasField(c, "IS_DUMPING_METHOD_HANDLES"), "field IS_DUMPING_METHOD_HANDLES");
        check(hasField(c, "IS_DUMPING_STATIC_ARCHIVE"), "field IS_DUMPING_STATIC_ARCHIVE");
        check(hasField(c, "IS_LOGGING_LAMBDA_FORM_INVOKERS"), "field IS_LOGGING_LAMBDA_FORM_INVOKERS");
        check(hasField(c, "IS_USING_ARCHIVE"), "field IS_USING_ARCHIVE");
        check(hasField(c, "configStatus"), "field configStatus");
        check(hasField(c, "excludeFlags"), "field excludeFlags");
        check(hasField(c, "keepAliveList"), "field keepAliveList");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

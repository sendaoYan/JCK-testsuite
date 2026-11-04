/*
 * @test
 * @summary jdk.internal.vm.Continuation conformance: internal package reflection.
 * @modules java.base/jdk.internal.vm:+open
 * @run main ContinuationTest
 */
public class ContinuationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.vm.Continuation", false, ContinuationTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getCurrentContinuation"), "method getCurrentContinuation");
        check(hasMethod(c, "getParent"), "method getParent");
        check(hasMethod(c, "getScope"), "method getScope");
        check(hasMethod(c, "getStackTrace"), "method getStackTrace");
        check(hasMethod(c, "isDone"), "method isDone");
        check(hasMethod(c, "isPinned"), "method isPinned");
        check(hasMethod(c, "isPreempted"), "method isPreempted");
        check(hasMethod(c, "onContinue"), "method onContinue");
        check(hasMethod(c, "onPinned"), "method onPinned");
        check(hasMethod(c, "pin"), "method pin");
        check(hasMethod(c, "run"), "method run");
        check(hasMethod(c, "stackWalker"), "method stackWalker");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "tryPreempt"), "method tryPreempt");
        check(hasMethod(c, "unpin"), "method unpin");
        check(hasMethod(c, "wrapWalk"), "method wrapWalk");
        check(hasMethod(c, "yield"), "method yield");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "JLA"), "field JLA");
        check(hasField(c, "MOUNTED_OFFSET"), "field MOUNTED_OFFSET");
        check(hasField(c, "PRESERVE_SCOPED_VALUE_CACHE"), "field PRESERVE_SCOPED_VALUE_CACHE");
        check(hasField(c, "U"), "field U");
        check(hasField(c, "child"), "field child");
        check(hasField(c, "done"), "field done");
        check(hasField(c, "mounted"), "field mounted");
        check(hasField(c, "parent"), "field parent");
        check(hasField(c, "preempted"), "field preempted");
        check(hasField(c, "scope"), "field scope");
        check(hasField(c, "scopedValueCache"), "field scopedValueCache");
        check(hasField(c, "tail"), "field tail");
        check(hasField(c, "target"), "field target");
        check(hasField(c, "yieldInfo"), "field yieldInfo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary sun.jvm.hotspot.ui.classbrowser.HTMLGenerator conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.ui.classbrowser:+open
 * @run main HTMLGeneratorTest
 */
import java.lang.reflect.*;

public class HTMLGeneratorTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.ui.classbrowser.HTMLGenerator", false, HTMLGeneratorTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.runtime.ClassConstants"), "interface sun.jvm.hotspot.runtime.ClassConstants");
        check(hasMethod(c, "parseAddress"), "method parseAddress");
        check(hasMethod(c, "genHTMLForJavaStackTrace"), "method genHTMLForJavaStackTrace");
        check(hasMethod(c, "genHTMLForAddress"), "method genHTMLForAddress");
        check(hasMethod(c, "genHTML"), "method genHTML");
        check(hasMethod(c, "genHTMLForMessage"), "method genHTMLForMessage");
        check(hasMethod(c, "escapeHTMLSpecialChars"), "method escapeHTMLSpecialChars");
        check(hasMethod(c, "genBaseHref"), "method genBaseHref");
        check(hasMethod(c, "genKlassHref"), "method genKlassHref");
        check(hasMethod(c, "genKlassTitle"), "method genKlassTitle");
        check(hasMethod(c, "genMethodModifierString"), "method genMethodModifierString");
        check(hasMethod(c, "genMethodNameAndSignature"), "method genMethodNameAndSignature");
        check(hasMethod(c, "genMethodHref"), "method genMethodHref");
        check(hasMethod(c, "genMethodTitle"), "method genMethodTitle");
        check(hasMethod(c, "genMethodLink"), "method genMethodLink");
        check(hasMethod(c, "genKlassLink"), "method genKlassLink");
        check(hasMethod(c, "genNMethodHref"), "method genNMethodHref");
        check(hasMethod(c, "genNMethodTitle"), "method genNMethodTitle");
        check(hasMethod(c, "genConstantPoolTitle"), "method genConstantPoolTitle");
        check(hasMethod(c, "genHTMLTableForConstantPool"), "method genHTMLTableForConstantPool");
        check(hasMethod(c, "genHTMLErrorMessage"), "method genHTMLErrorMessage");
        check(hasMethod(c, "genConstantPoolHref"), "method genConstantPoolHref");
        check(hasMethod(c, "genNMethodLink"), "method genNMethodLink");
        check(hasMethod(c, "createBytecodeDisassembler"), "method createBytecodeDisassembler");
        check(hasMethod(c, "genConstantPoolLink"), "method genConstantPoolLink");
        check(hasMethod(c, "genHTMLForRawDisassembly"), "method genHTMLForRawDisassembly");
        check(hasMethod(c, "addressToLong"), "method addressToLong");
        check(hasMethod(c, "genPCHref"), "method genPCHref");
        check(hasMethod(c, "genMultPCHref"), "method genMultPCHref");
        check(hasMethod(c, "genScope"), "method genScope");
        check(hasMethod(c, "genScObjInfo"), "method genScObjInfo");
        check(hasMethod(c, "genMethodAndKlassLink"), "method genMethodAndKlassLink");
        check(hasMethod(c, "genHTMLForLocals"), "method genHTMLForLocals");
        check(hasMethod(c, "genHTMLForExpressions"), "method genHTMLForExpressions");
        check(hasMethod(c, "genHTMLForMonitors"), "method genHTMLForMonitors");
        check(hasMethod(c, "genOopMapInfo"), "method genOopMapInfo");
        check(hasMethod(c, "genHTMLForOopMap"), "method genHTMLForOopMap");
        check(hasMethod(c, "locationAsString"), "method locationAsString");
        check(hasMethod(c, "genHTMLForScopeValues"), "method genHTMLForScopeValues");
        check(hasMethod(c, "genCodeBlobTitle"), "method genCodeBlobTitle");
        check(hasMethod(c, "genInterpreterCodeletTitle"), "method genInterpreterCodeletTitle");
        check(hasMethod(c, "genInterpreterCodeletLinkPageHref"), "method genInterpreterCodeletLinkPageHref");
        check(hasMethod(c, "genDumpKlassesHref"), "method genDumpKlassesHref");
        check(hasMethod(c, "genDumpKlassesTitle"), "method genDumpKlassesTitle");
        check(hasMethod(c, "genDumpKlassesLink"), "method genDumpKlassesLink");
        check(hasMethod(c, "genHTMLListForKlassNames"), "method genHTMLListForKlassNames");
        check(hasMethod(c, "genHTMLListForMethods"), "method genHTMLListForMethods");
        check(hasMethod(c, "genHTMLListForFields"), "method genHTMLListForFields");
        check(hasMethod(c, "genFieldModifierString"), "method genFieldModifierString");
        check(hasMethod(c, "genKlassHierarchyHref"), "method genKlassHierarchyHref");
        check(hasMethod(c, "genKlassHierarchyTitle"), "method genKlassHierarchyTitle");
        check(hasMethod(c, "genHTMLListForSubKlasses"), "method genHTMLListForSubKlasses");
        check(hasMethod(c, "genDumpKlassHref"), "method genDumpKlassHref");
        check(hasMethod(c, "genDumpKlassLink"), "method genDumpKlassLink");
        check(hasMethod(c, "genHTMLListForInterfaces"), "method genHTMLListForInterfaces");
        check(hasMethod(c, "getOopAtAddress"), "method getOopAtAddress");
        check(hasMethod(c, "genJavaStackTraceTitle"), "method genJavaStackTraceTitle");
        check(hasMethod(c, "getKlassAtAddress"), "method getKlassAtAddress");
        check(hasMethod(c, "getMethodAtAddress"), "method getMethodAtAddress");
        check(hasMethod(c, "genInterpreterCodeletLinksPage"), "method genInterpreterCodeletLinksPage");
        check(hasMethod(c, "genHTMLForKlassHierarchy"), "method genHTMLForKlassHierarchy");
        check(hasMethod(c, "getConstantPoolAtAddress"), "method getConstantPoolAtAddress");
        check(hasMethod(c, "genDumpKlass"), "method genDumpKlass");
        check(hasMethod(c, "genHTMLForWait"), "method genHTMLForWait");
        check(hasMethod(c, "createSymbolFinder"), "method createSymbolFinder");
        check(hasMethod(c, "genSafepointInfo"), "method genSafepointInfo");
        check(hasMethod(c, "genHTMLForKlassNames"), "method genHTMLForKlassNames");
        check(hasMethod(c, "genHTMLForMethodNames"), "method genHTMLForMethodNames");
        check(hasMethod(c, "genHTMLForFieldNames"), "method genHTMLForFieldNames");
        check(hasMethod(c, "genKlassHierarchyLink"), "method genKlassHierarchyLink");
        check(hasMethod(c, "longToAddress"), "method longToAddress");
        check(hasMethod(c, "genHTMLForHyperlink"), "method genHTMLForHyperlink");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary com.sun.xml.internal.ws.resources.TubelineassemblyMessages conformance: reflection (kind, superclass, interfaces, methods).
 * @run main TubelineassemblyMessagesTest
 */

import java.lang.reflect.Modifier;


public class TubelineassemblyMessagesTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.resources.TubelineassemblyMessages", false, TubelineassemblyMessagesTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.resources.TubelineassemblyMessages"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("MASM_0001_DEFAULT_CFG_FILE_NOT_FOUND"), "method MASM_0001_DEFAULT_CFG_FILE_NOT_FOUND");
        check(mn.contains("MASM_0002_DEFAULT_CFG_FILE_LOCATED"), "method MASM_0002_DEFAULT_CFG_FILE_LOCATED");
        check(mn.contains("MASM_0003_DEFAULT_CFG_FILE_NOT_LOADED"), "method MASM_0003_DEFAULT_CFG_FILE_NOT_LOADED");
        check(mn.contains("MASM_0004_NO_TUBELINES_SECTION_IN_DEFAULT_CFG_FILE"), "method MASM_0004_NO_TUBELINES_SECTION_IN_DEFAULT_CFG_FILE");
        check(mn.contains("MASM_0005_NO_DEFAULT_TUBELINE_IN_DEFAULT_CFG_FILE"), "method MASM_0005_NO_DEFAULT_TUBELINE_IN_DEFAULT_CFG_FILE");
        check(mn.contains("MASM_0006_APP_CFG_FILE_LOCATED"), "method MASM_0006_APP_CFG_FILE_LOCATED");
        check(mn.contains("MASM_0007_APP_CFG_FILE_NOT_FOUND"), "method MASM_0007_APP_CFG_FILE_NOT_FOUND");
        check(mn.contains("MASM_0008_INVALID_URI_REFERENCE"), "method MASM_0008_INVALID_URI_REFERENCE");
        check(mn.contains("MASM_0009_CANNOT_FORM_VALID_URL"), "method MASM_0009_CANNOT_FORM_VALID_URL");
        check(mn.contains("MASM_0010_ERROR_READING_CFG_FILE_FROM_LOCATION"), "method MASM_0010_ERROR_READING_CFG_FILE_FROM_LOCATION");
        check(mn.contains("MASM_0011_LOADING_RESOURCE"), "method MASM_0011_LOADING_RESOURCE");
        check(mn.contains("MASM_0012_LOADING_VIA_SERVLET_CONTEXT"), "method MASM_0012_LOADING_VIA_SERVLET_CONTEXT");
        check(mn.contains("MASM_0013_ERROR_INVOKING_SERVLET_CONTEXT_METHOD"), "method MASM_0013_ERROR_INVOKING_SERVLET_CONTEXT_METHOD");
        check(mn.contains("MASM_0014_UNABLE_TO_LOAD_CLASS"), "method MASM_0014_UNABLE_TO_LOAD_CLASS");
        check(mn.contains("MASM_0015_CLASS_DOES_NOT_IMPLEMENT_INTERFACE"), "method MASM_0015_CLASS_DOES_NOT_IMPLEMENT_INTERFACE");
        check(mn.contains("MASM_0016_UNABLE_TO_INSTANTIATE_TUBE_FACTORY"), "method MASM_0016_UNABLE_TO_INSTANTIATE_TUBE_FACTORY");
        check(mn.contains("MASM_0017_UNABLE_TO_LOAD_TUBE_FACTORY_CLASS"), "method MASM_0017_UNABLE_TO_LOAD_TUBE_FACTORY_CLASS");
        check(mn.contains("MASM_0018_MSG_LOGGING_SYSTEM_PROPERTY_SET_TO_VALUE"), "method MASM_0018_MSG_LOGGING_SYSTEM_PROPERTY_SET_TO_VALUE");
        check(mn.contains("MASM_0019_MSG_LOGGING_SYSTEM_PROPERTY_ILLEGAL_VALUE"), "method MASM_0019_MSG_LOGGING_SYSTEM_PROPERTY_ILLEGAL_VALUE");
        check(mn.contains("MASM_0020_ERROR_CREATING_URI_FROM_GENERATED_STRING"), "method MASM_0020_ERROR_CREATING_URI_FROM_GENERATED_STRING");
        check(mn.contains("localizableMASM_0001_DEFAULT_CFG_FILE_NOT_FOUND"), "method localizableMASM_0001_DEFAULT_CFG_FILE_NOT_FOUND");
        check(mn.contains("localizableMASM_0002_DEFAULT_CFG_FILE_LOCATED"), "method localizableMASM_0002_DEFAULT_CFG_FILE_LOCATED");
        check(mn.contains("localizableMASM_0003_DEFAULT_CFG_FILE_NOT_LOADED"), "method localizableMASM_0003_DEFAULT_CFG_FILE_NOT_LOADED");
        check(mn.contains("localizableMASM_0004_NO_TUBELINES_SECTION_IN_DEFAULT_CFG_FILE"), "method localizableMASM_0004_NO_TUBELINES_SECTION_IN_DEFAULT_CFG_FILE");
        check(mn.contains("localizableMASM_0005_NO_DEFAULT_TUBELINE_IN_DEFAULT_CFG_FILE"), "method localizableMASM_0005_NO_DEFAULT_TUBELINE_IN_DEFAULT_CFG_FILE");
        check(mn.contains("localizableMASM_0006_APP_CFG_FILE_LOCATED"), "method localizableMASM_0006_APP_CFG_FILE_LOCATED");
        check(mn.contains("localizableMASM_0007_APP_CFG_FILE_NOT_FOUND"), "method localizableMASM_0007_APP_CFG_FILE_NOT_FOUND");
        check(mn.contains("localizableMASM_0008_INVALID_URI_REFERENCE"), "method localizableMASM_0008_INVALID_URI_REFERENCE");
        check(mn.contains("localizableMASM_0009_CANNOT_FORM_VALID_URL"), "method localizableMASM_0009_CANNOT_FORM_VALID_URL");
        check(mn.contains("localizableMASM_0010_ERROR_READING_CFG_FILE_FROM_LOCATION"), "method localizableMASM_0010_ERROR_READING_CFG_FILE_FROM_LOCATION");
        check(mn.contains("localizableMASM_0011_LOADING_RESOURCE"), "method localizableMASM_0011_LOADING_RESOURCE");
        check(mn.contains("localizableMASM_0012_LOADING_VIA_SERVLET_CONTEXT"), "method localizableMASM_0012_LOADING_VIA_SERVLET_CONTEXT");
        check(mn.contains("localizableMASM_0013_ERROR_INVOKING_SERVLET_CONTEXT_METHOD"), "method localizableMASM_0013_ERROR_INVOKING_SERVLET_CONTEXT_METHOD");
        check(mn.contains("localizableMASM_0014_UNABLE_TO_LOAD_CLASS"), "method localizableMASM_0014_UNABLE_TO_LOAD_CLASS");
        check(mn.contains("localizableMASM_0015_CLASS_DOES_NOT_IMPLEMENT_INTERFACE"), "method localizableMASM_0015_CLASS_DOES_NOT_IMPLEMENT_INTERFACE");
        check(mn.contains("localizableMASM_0016_UNABLE_TO_INSTANTIATE_TUBE_FACTORY"), "method localizableMASM_0016_UNABLE_TO_INSTANTIATE_TUBE_FACTORY");
        check(mn.contains("localizableMASM_0017_UNABLE_TO_LOAD_TUBE_FACTORY_CLASS"), "method localizableMASM_0017_UNABLE_TO_LOAD_TUBE_FACTORY_CLASS");
        check(mn.contains("localizableMASM_0018_MSG_LOGGING_SYSTEM_PROPERTY_SET_TO_VALUE"), "method localizableMASM_0018_MSG_LOGGING_SYSTEM_PROPERTY_SET_TO_VALUE");
        check(mn.contains("localizableMASM_0019_MSG_LOGGING_SYSTEM_PROPERTY_ILLEGAL_VALUE"), "method localizableMASM_0019_MSG_LOGGING_SYSTEM_PROPERTY_ILLEGAL_VALUE");
        check(mn.contains("localizableMASM_0020_ERROR_CREATING_URI_FROM_GENERATED_STRING"), "method localizableMASM_0020_ERROR_CREATING_URI_FROM_GENERATED_STRING");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

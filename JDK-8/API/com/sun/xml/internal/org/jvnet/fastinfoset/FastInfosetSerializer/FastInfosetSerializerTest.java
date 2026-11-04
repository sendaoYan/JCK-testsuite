/*
 * @test
 * @summary com.sun.xml.internal.org.jvnet.fastinfoset.FastInfosetSerializer conformance: interface hierarchy and members.
 * @run main FastInfosetSerializerTest
 */

public class FastInfosetSerializerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.org.jvnet.fastinfoset.FastInfosetSerializer", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.org.jvnet.fastinfoset.FastInfosetSerializer"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getAttributeValueMapMemoryLimit"), "has method getAttributeValueMapMemoryLimit");
        check(mn.contains("getCharacterContentChunkMapMemoryLimit"), "has method getCharacterContentChunkMapMemoryLimit");
        check(mn.contains("getCharacterEncodingScheme"), "has method getCharacterEncodingScheme");
        check(mn.contains("getIgnoreComments"), "has method getIgnoreComments");
        check(mn.contains("getIgnoreDTD"), "has method getIgnoreDTD");
        check(mn.contains("getIgnoreProcesingInstructions"), "has method getIgnoreProcesingInstructions");
        check(mn.contains("getIgnoreWhiteSpaceTextContent"), "has method getIgnoreWhiteSpaceTextContent");
        check(mn.contains("getMaxAttributeValueSize"), "has method getMaxAttributeValueSize");
        check(mn.contains("getMaxCharacterContentChunkSize"), "has method getMaxCharacterContentChunkSize");
        check(mn.contains("getMinAttributeValueSize"), "has method getMinAttributeValueSize");
        check(mn.contains("getMinCharacterContentChunkSize"), "has method getMinCharacterContentChunkSize");
        check(mn.contains("getRegisteredEncodingAlgorithms"), "has method getRegisteredEncodingAlgorithms");
        check(mn.contains("getVocabularyApplicationData"), "has method getVocabularyApplicationData");
        check(mn.contains("reset"), "has method reset");
        check(mn.contains("setAttributeValueMapMemoryLimit"), "has method setAttributeValueMapMemoryLimit");
        check(mn.contains("setCharacterContentChunkMapMemoryLimit"), "has method setCharacterContentChunkMapMemoryLimit");
        check(mn.contains("setCharacterEncodingScheme"), "has method setCharacterEncodingScheme");
        check(mn.contains("setExternalVocabulary"), "has method setExternalVocabulary");
        check(mn.contains("setIgnoreComments"), "has method setIgnoreComments");
        check(mn.contains("setIgnoreDTD"), "has method setIgnoreDTD");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

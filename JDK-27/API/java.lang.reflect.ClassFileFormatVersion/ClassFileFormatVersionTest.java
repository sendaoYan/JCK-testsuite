/*
 * @test
 * @summary java.lang.reflect.ClassFileFormatVersion conformance: enum latest/major/runtimeVersion/fromMajor.
 * @run main ClassFileFormatVersionTest
 */
import java.lang.reflect.ClassFileFormatVersion;

public class ClassFileFormatVersionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(ClassFileFormatVersion.class.isEnum(), "is enum");
        ClassFileFormatVersion latest = ClassFileFormatVersion.latest();
        check(latest.major() >= 65, "latest major >= 65");
        check(latest.runtimeVersion().feature() == Runtime.version().feature(), "runtimeVersion matches");
        check(ClassFileFormatVersion.fromMajor(latest.major()) == latest, "fromMajor roundtrip");
        check(ClassFileFormatVersion.valueOf(Runtime.version()) == latest, "valueOf(Runtime.version)");
        check(ClassFileFormatVersion.valueOf("RELEASE_8") == ClassFileFormatVersion.RELEASE_8, "valueOf RELEASE_8");
        check(ClassFileFormatVersion.RELEASE_8.major() == 52, "RELEASE_8 major 52");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

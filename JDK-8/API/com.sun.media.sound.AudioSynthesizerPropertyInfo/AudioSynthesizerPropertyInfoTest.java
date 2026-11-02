/*
 * @test
 * @summary com.sun.media.sound.AudioSynthesizerPropertyInfo conformance: value-object data holder.
 * @run main AudioSynthesizerPropertyInfoTest
 */

import com.sun.media.sound.AudioSynthesizerPropertyInfo;

public class AudioSynthesizerPropertyInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        AudioSynthesizerPropertyInfo p = new AudioSynthesizerPropertyInfo("name", Integer.valueOf(3));
        check("name".equals(p.name), "name field");
        check(Integer.valueOf(3).equals(p.value), "value field");
        check(p.valueClass == Integer.class, "valueClass derived");
        check(p.description == null, "description null");
        check(p.choices == null, "choices null");

        p.description = "desc";
        p.choices = new Object[] { Integer.valueOf(1), Integer.valueOf(2) };
        check("desc".equals(p.description), "description settable");
        check(p.choices.length == 2, "choices settable");

        System.out.println("PASS (" + passed + " assertions)");
    }
}

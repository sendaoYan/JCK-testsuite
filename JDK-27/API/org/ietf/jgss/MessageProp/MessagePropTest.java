/*
 * @test
 * @summary org.ietf.jgss.MessageProp conformance: white-box structure + black-box behavior.
 * @run main MessagePropTest
 */

import org.ietf.jgss.*;
import java.lang.reflect.*;

public class MessagePropTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasCtor(Class<?> c, Class<?>... p) { try { c.getDeclaredConstructor(p); return true; } catch (Exception e) { return false; } }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("org.ietf.jgss.MessageProp");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "is class not interface");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(hasCtor(c, boolean.class), "ctor(boolean)");
        check(hasCtor(c, int.class, boolean.class), "ctor(int,boolean)");
        String[] names = { "getQOP", "getPrivacy", "setQOP", "setPrivacy", "isDuplicateToken",
                "isOldToken", "isUnseqToken", "isGapToken", "getMinorStatus", "getMinorString",
                "setSupplementaryStates" };
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // boolean ctor: QOP 0, privacy as given, all supplementary reset
        MessageProp mp = new MessageProp(true);
        check(mp.getQOP() == 0, "boolean ctor default QOP 0");
        check(mp.getPrivacy(), "boolean ctor privacy true");
        check(!mp.isDuplicateToken() && !mp.isOldToken() && !mp.isUnseqToken() && !mp.isGapToken(),
                "boolean ctor supplementary states reset");
        check(mp.getMinorStatus() == 0, "boolean ctor minorStatus 0");
        check(mp.getMinorString() == null, "boolean ctor minorString null");

        check(!new MessageProp(false).getPrivacy(), "boolean ctor privacy false");

        // int+boolean ctor
        MessageProp mp2 = new MessageProp(2, true);
        check(mp2.getQOP() == 2, "int ctor QOP");
        check(mp2.getPrivacy(), "int ctor privacy");

        // setQOP/setPrivacy round trip
        mp2.setQOP(0);
        mp2.setPrivacy(false);
        check(mp2.getQOP() == 0 && !mp2.getPrivacy(), "setters round trip");
        mp2.setQOP(7);
        mp2.setPrivacy(true);
        check(mp2.getQOP() == 7 && mp2.getPrivacy(), "setters second round trip");

        // setSupplementaryStates full combination round trip
        mp.setSupplementaryStates(true, false, true, false, 3, "minor");
        check(mp.isDuplicateToken(), "duplicate token true");
        check(!mp.isOldToken(), "old token false");
        check(mp.isUnseqToken(), "unseq token true");
        check(!mp.isGapToken(), "gap token false");
        check(mp.getMinorStatus() == 3, "getMinorStatus");
        check("minor".equals(mp.getMinorString()), "getMinorString");

        // reset back to all-false via setSupplementaryStates
        mp.setSupplementaryStates(false, false, false, false, 0, null);
        check(!mp.isDuplicateToken() && !mp.isOldToken() && !mp.isUnseqToken() && !mp.isGapToken(),
                "supplementary states reset to false");
        check(mp.getMinorStatus() == 0, "minorStatus reset to 0");
        check(mp.getMinorString() == null, "minorString reset to null");

        // the complementary combination
        mp.setSupplementaryStates(false, true, false, true, 4, "swap");
        check(!mp.isDuplicateToken(), "duplicate token false after swap");
        check(mp.isOldToken(), "old token true after swap");
        check(!mp.isUnseqToken(), "unseq token false after swap");
        check(mp.isGapToken(), "gap token true after swap");
        check(mp.getMinorStatus() == 4, "minorStatus after swap");
        check("swap".equals(mp.getMinorString()), "minorString after swap");

        // all four supplementary flags simultaneously true
        MessageProp mp3 = new MessageProp(0, false);
        mp3.setSupplementaryStates(true, true, true, true, 9, "all");
        check(mp3.isDuplicateToken() && mp3.isOldToken() && mp3.isUnseqToken() && mp3.isGapToken(),
                "all supplementary states true");
        check(mp3.getMinorStatus() == 9 && "all".equals(mp3.getMinorString()), "minor status/string");

        // setSupplementaryStates does not disturb QOP/privacy and vice versa
        check(mp3.getQOP() == 0 && !mp3.getPrivacy(), "QOP/privacy unchanged by setSupplementaryStates");
        mp3.setQOP(5);
        check(mp3.isDuplicateToken(), "supplementary states unchanged by setQOP");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

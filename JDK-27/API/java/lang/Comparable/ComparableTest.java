/*
 * @test
 * @summary java.lang.Comparable API conformance: compareTo sign (negative/zero/positive),
 *          antisymmetry of Integer/String natural ordering, self-comparison == 0, and
 *          consistency with Collections.sort.
 * @run main ComparableTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ComparableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testInteger() {
        Integer a = Integer.valueOf(3), b = Integer.valueOf(7), c = Integer.valueOf(3);
        check(a.compareTo(b) < 0, "3.compareTo(7) < 0");
        check(b.compareTo(a) > 0, "7.compareTo(3) > 0");
        check(a.compareTo(c) == 0, "3.compareTo(3) == 0");
        check(a.compareTo(a) == 0, "self compareTo == 0");
        check(Integer.compare(a, b) == a.compareTo(b), "Integer.compare consistent");
    }

    static void testString() {
        check("apple".compareTo("banana") < 0, "\"apple\".compareTo(\"banana\") < 0");
        check("banana".compareTo("apple") > 0, "\"banana\".compareTo(\"apple\") > 0");
        check("same".compareTo("same") == 0, "equal strings compareTo == 0");
    }

    static void testSort() {
        List<Integer> xs = new ArrayList<>(Arrays.asList(3, 1, 4, 1, 5, 9, 2, 6));
        Collections.sort(xs);
        check(xs.equals(Arrays.asList(1, 1, 2, 3, 4, 5, 6, 9)), "Collections.sort natural order");
    }

    static final class Version implements Comparable<Version> {
        final int major, minor;
        Version(int m, int n) { major = m; minor = n; }
        public int compareTo(Version o) {
            int d = major - o.major;
            return d != 0 ? d : minor - o.minor;
        }
        public String toString() { return major + "." + minor; }
    }

    static void testCustomComparable() {
        List<Version> vs = new ArrayList<>(Arrays.asList(
            new Version(2, 1), new Version(1, 9), new Version(2, 0), new Version(1, 0)));
        Collections.sort(vs);
        check(vs.toString().equals("[1.0, 1.9, 2.0, 2.1]"), "Version natural sort");
        check(new Version(1, 9).compareTo(new Version(2, 0)) < 0, "1.9 < 2.0");
        check(new Version(2, 0).compareTo(new Version(2, 1)) < 0, "2.0 < 2.1");
        check(new Version(2, 1).compareTo(new Version(2, 1)) == 0, "equal versions");
    }

    static void testNullAndClassCast() {
        check("a".compareTo("b") == -1, "String compareTo exact -1");
        try {
            "a".compareTo(null);
            throw new AssertionError("String.compareTo(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
        Comparable c = Integer.valueOf(1);
        try {
            c.compareTo("x");
            throw new AssertionError("Integer.compareTo(String): expected ClassCastException");
        } catch (ClassCastException expected) {
            passed++;
        }
    }

    static void testSignProperties() {
        String a = "apple", b = "banana", c = "cherry";
        check(a.compareTo(b) < 0 && b.compareTo(c) < 0, "transitive order");
        check(a.compareTo(c) < 0, "transitive a<c");
        check(Integer.signum(a.compareTo(b)) == -Integer.signum(b.compareTo(a)), "antisymmetry");
        check(a.compareTo(b) == java.util.Comparator.<String>naturalOrder().compare(a, b),
              "naturalOrder consistent");
        check("10".compareTo("9") < 0, "lexicographic (not numeric)");
        check("a".compareTo("A") > 0, "case-sensitive: 'a'(97) > 'A'(65)");
    }

    // ---------- white-box: structural / reflection ----------
    static boolean hasMethod(Class<?> c, String name, Class<?>... params) {
        try {
            c.getMethod(name, params);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Comparable");
        check(c.isInterface(), "Comparable is an interface");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "Comparable abstract");
        check(!java.lang.reflect.Modifier.isFinal(c.getModifiers()), "Comparable not final");
        check(c.getTypeParameters().length == 1, "Comparable<T> has one type parameter");
        check(hasMethod(c, "compareTo", Object.class), "abstract compareTo(Object)");
    }

    // ---------- black-box: behavioral composition ----------
    static void testBlackBox() {
        testInteger();
        testString();
        testSort();
        testCustomComparable();
        testNullAndClassCast();
        testSignProperties();
    }

    // ==================== 契约深化（2026-11）====================

    /** JDK-8 侧与 27 侧共用同一源文件时的版本分支（两侧均可编译）。 */
    static final boolean MODERN = !System.getProperty("java.version").startsWith("1.");

    static int sgn(int v) { return v < 0 ? -1 : (v > 0 ? 1 : 0); }

    /** 自造按 (x,y) 字典序比较的 Comparable：全矩阵样本。 */
    static final class Point implements Comparable<Point> {
        final int x, y;
        Point(int x, int y) { this.x = x; this.y = y; }
        public int compareTo(Point o) {
            int d = x - o.x;
            return d != 0 ? d : y - o.y;
        }
        public String toString() { return "(" + x + "," + y + ")"; }
    }

    /** 白盒补充：Comparable 恰 1 个公共方法且为唯一抽象方法（函数式形态）。 */
    static void testWhiteBoxCounts() throws Exception {
        Class<?> c = Class.forName("java.lang.Comparable");
        check(c.getTypeParameters().length == 1 && "T".equals(c.getTypeParameters()[0].getName()),
              "Comparable<T>: one type parameter named T");
        int total = 0, abstractCount = 0;
        for (java.lang.reflect.Method m : c.getMethods()) {
            total++;
            if (java.lang.reflect.Modifier.isAbstract(m.getModifiers())) abstractCount++;
        }
        check(total == 1, "Comparable exposes exactly 1 public method, got " + total);
        check(abstractCount == 1, "exactly 1 abstract method");
        java.lang.reflect.Method cm = c.getMethod("compareTo", Object.class);
        check(cm.getReturnType() == int.class, "compareTo returns int");
        check(cm.getParameterCount() == 1, "compareTo takes 1 parameter");
    }

    /** sgn(compare(x,y)) == -sgn(compare(y,x)) 全对称抽检表（Integer/String/Character）。 */
    static void testSgnAntisymmetryTable() {
        Integer[] ints = { Integer.valueOf(-5), Integer.valueOf(0), Integer.valueOf(7) };
        for (Integer x : ints) {
            for (Integer y : ints) {
                check(sgn(x.compareTo(y)) == -sgn(y.compareTo(x)),
                      "Integer sgn antisymmetry for " + x + "," + y);
            }
        }
        String[] strs = { "a", "ab", "b" };
        for (String x : strs) {
            for (String y : strs) {
                check(sgn(x.compareTo(y)) == -sgn(y.compareTo(x)),
                      "String sgn antisymmetry for " + x + "," + y);
            }
        }
        Character[] chars = { Character.valueOf('A'), Character.valueOf('B'), Character.valueOf('a') };
        for (Character x : chars) {
            for (Character y : chars) {
                check(sgn(x.compareTo(y)) == -sgn(y.compareTo(x)),
                      "Character sgn antisymmetry for " + x + "," + y);
            }
        }
    }

    /** 传递性：x<y 且 y<z ⇒ x<z（三内置 + 自造类型）。 */
    static void testTransitivity() {
        check(Integer.valueOf(1).compareTo(2) < 0, "Integer 1<2");
        check(Integer.valueOf(2).compareTo(5) < 0, "Integer 2<5");
        check(Integer.valueOf(1).compareTo(5) < 0, "transitivity: 1<5");
        check("a".compareTo("ab") < 0, "String a<ab");
        check("ab".compareTo("b") < 0, "String ab<b");
        check("a".compareTo("b") < 0, "transitivity: a<b");
        check(Character.valueOf('A').compareTo('B') < 0, "Character A<B");
        check(Character.valueOf('B').compareTo('a') < 0, "Character B<a");
        check(Character.valueOf('A').compareTo('a') < 0, "transitivity: A<a");
        check(new Point(1, 0).compareTo(new Point(2, 0)) < 0, "Point (1,0)<(2,0)");
        check(new Point(2, 0).compareTo(new Point(2, 5)) < 0, "Point (2,0)<(2,5)");
        check(new Point(1, 0).compareTo(new Point(2, 5)) < 0, "transitivity: (1,0)<(2,5)");
    }

    /** 自反性：compare(x,x)==0（四类样本）。 */
    static void testReflexivity() {
        check(Integer.valueOf(-42).compareTo(Integer.valueOf(-42)) == 0, "Integer self == 0");
        check("same".compareTo("same") == 0, "String self == 0");
        check(Character.valueOf('x').compareTo(Character.valueOf('x')) == 0, "Character self == 0");
        Point p = new Point(3, 4);
        check(p.compareTo(p) == 0, "Point self == 0 (same instance)");
        check(new Point(3, 4).compareTo(new Point(3, 4)) == 0, "Point equal-value == 0");
    }

    /** 与 equals 一致性（Integer 强一致）：compare==0 ⇔ equals true；缓存内外两档。 */
    static void testEqualsConsistency() {
        Integer five1 = Integer.valueOf(5), five2 = Integer.valueOf(5);
        check(five1.compareTo(five2) == 0, "Integer(5)==Integer(5) compare 0");
        check(five1.equals(five2), "compare==0 => equals true");
        check(five1.equals(five2) && five1.compareTo(five2) == 0, "equals true => compare 0");
        Integer cached1 = Integer.valueOf(127), cached2 = Integer.valueOf(127);
        check(cached1 == cached2, "127 cached: same instance");
        check(cached1.compareTo(cached2) == 0 && cached1.equals(cached2), "cached pair consistent");
        Integer big1 = Integer.valueOf(1000), big2 = new Integer(1000);
        check(big1 != big2, "1000 beyond cache: distinct instances");
        check(big1.compareTo(big2) == 0, "value-based compare 0 despite distinct identity");
        check(big1.equals(big2), "equals true for distinct instances of same value");
        Integer three = Integer.valueOf(3), seven = Integer.valueOf(7);
        check(!three.equals(seven), "equals false");
        check(three.compareTo(seven) != 0, "equals false => compare != 0");
        check(three.compareTo(seven) < 0, "3 < 7");
    }

    /** compareTo(null) → NullPointerException（三内置；消息按版本分支锁定）。 */
    static void testCompareToNull() {
        try {
            Integer.valueOf(1).compareTo(null);
            throw new AssertionError("Integer.compareTo(null): expected NullPointerException");
        } catch (NullPointerException e) {
            passed++;
            if (MODERN) {
                check("Cannot read field \"value\" because \"anotherInteger\" is null".equals(e.getMessage()),
                      "JDK-27 helpful NPE message for Integer.compareTo(null)");
            } else {
                check(e.getMessage() == null, "JDK-8 NPE carries no message");
            }
        }
        try {
            "a".compareTo(null);
            throw new AssertionError("String.compareTo(null): expected NullPointerException");
        } catch (NullPointerException e) {
            passed++;
            if (MODERN) {
                check("Cannot read field \"value\" because \"anotherString\" is null".equals(e.getMessage()),
                      "JDK-27 helpful NPE message for String.compareTo(null)");
            } else {
                check(e.getMessage() == null, "JDK-8 NPE carries no message");
            }
        }
        try {
            Character.valueOf('a').compareTo(null);
            throw new AssertionError("Character.compareTo(null): expected NullPointerException");
        } catch (NullPointerException e) {
            passed++;
            if (MODERN) {
                check("Cannot read field \"value\" because \"anotherCharacter\" is null".equals(e.getMessage()),
                      "JDK-27 helpful NPE message for Character.compareTo(null)");
            } else {
                check(e.getMessage() == null, "JDK-8 NPE carries no message");
            }
        }
    }

    /** Character 比较是码位差：'a'-'B'==31 精确。 */
    static void testCharacterExactDiff() {
        check('a' - 'B' == 31, "numeric char arithmetic 'a'-'B' == 31");
        check(Character.compare('a', 'B') == 31, "Character.compare('a','B') == 31");
        check(Character.valueOf('a').compareTo(Character.valueOf('B')) == 31,
              "instance compareTo('a','B') == 31");
        check(Character.valueOf('B').compareTo(Character.valueOf('a')) == -31, "reversed == -31");
        check(Character.compare('a', 'a') == 0, "equal chars compare 0");
        check(Character.compare('\u0000', '\uFFFF') < 0, "MIN_VALUE < MAX_VALUE");
        check(Character.MIN_VALUE == '\u0000' && Character.MAX_VALUE == '\uFFFF', "char bounds");
    }

    /** Integer 极值与静态 compare 一致性。 */
    static void testIntegerExtremes() {
        check(Integer.valueOf(Integer.MIN_VALUE).compareTo(Integer.valueOf(Integer.MAX_VALUE)) < 0,
              "MIN < MAX");
        check(Integer.valueOf(Integer.MAX_VALUE).compareTo(Integer.valueOf(Integer.MIN_VALUE)) > 0,
              "MAX > MIN");
        check(Integer.valueOf(Integer.MIN_VALUE).compareTo(Integer.valueOf(Integer.MIN_VALUE)) == 0,
              "MIN self == 0");
        check(Integer.valueOf(Integer.MAX_VALUE).compareTo(Integer.valueOf(Integer.MAX_VALUE)) == 0,
              "MAX self == 0");
        check(Integer.compare(Integer.MIN_VALUE, Integer.MAX_VALUE)
                  == Integer.valueOf(Integer.MIN_VALUE).compareTo(Integer.valueOf(Integer.MAX_VALUE)),
              "static compare matches instance compareTo");
        check(Integer.compare(0, -1) > 0, "0 > -1");
        check(Integer.compare(-1, 0) < 0, "-1 < 0");
        check(Integer.signum(Integer.valueOf(-3).compareTo(Integer.valueOf(3))) == -1,
              "signum of compare is -1");
    }

    /** String 字典序精确差：前缀规则、首字符差、大小写。 */
    static void testStringLexicographicExact() {
        check("ab".compareTo("abc") == -1, "prefix rule: ab vs abc == -1 (length diff)");
        check("abc".compareTo("abd") == -1, "first differing char: abc vs abd == -1");
        check("".compareTo("a") == -1, "empty is smallest: \"\" vs a == -1");
        check("b".compareTo("") == 1, "nonempty vs empty == 1");
        check("10".compareTo("9") == -8, "lexicographic not numeric: '1'-'9' == -8");
        check("a".compareTo("A") == 32, "case-sensitive: 'a'-'A' == 32");
        check("A".compareTo("a") == -32, "reversed case == -32");
        check("apple".compareTo("apple") == 0, "equal strings == 0");
        check("banana".compareTo("apple") > 0, "banana > apple");
        check("apple".compareTo("banana") == -1, "apple vs banana == -1 exact");
        check("java".compareTo("javac") == -1, "java vs javac == -1 (length diff 1)");
    }

    /** 自造 Comparable 全矩阵：3 值 9 格符号 + 排序总序。 */
    static void testCustomComparableMatrix() {
        Point[] vals = { new Point(1, 1), new Point(2, 1), new Point(2, 2) };
        int[][] expectedSgn = {
            { 0, -1, -1 },
            { 1,  0, -1 },
            { 1,  1,  0 },
        };
        for (int i = 0; i < vals.length; i++) {
            for (int j = 0; j < vals.length; j++) {
                check(sgn(vals[i].compareTo(vals[j])) == expectedSgn[i][j],
                      "Point matrix[" + i + "][" + j + "] sign " + expectedSgn[i][j]);
            }
        }
        java.util.List<Point> ps = new ArrayList<>(Arrays.asList(
            new Point(2, 2), new Point(1, 1), new Point(2, 1)));
        Collections.sort(ps);
        check(ps.get(0).compareTo(new Point(1, 1)) == 0, "sorted first is (1,1)");
        check(ps.get(1).compareTo(new Point(2, 1)) == 0, "sorted second is (2,1)");
        check(ps.get(2).compareTo(new Point(2, 2)) == 0, "sorted third is (2,2)");
        check(ps.toString().equals("[(1,1), (2,1), (2,2)]"), "sorted order exact");
    }

    /** 与 Comparator.naturalOrder 一致（Integer/String 双样本）。 */
    static void testNaturalOrderConsistency() {
        java.util.Comparator<Integer> natInt = java.util.Comparator.<Integer>naturalOrder();
        check(natInt.compare(3, 7) == Integer.valueOf(3).compareTo(Integer.valueOf(7)),
              "naturalOrder matches Integer.compareTo");
        java.util.Comparator<String> natStr = java.util.Comparator.<String>naturalOrder();
        check(natStr.compare("apple", "banana") == "apple".compareTo("banana"),
              "naturalOrder matches String.compareTo");
        Integer[] xs = { 5, 1, 3 };
        java.util.Arrays.sort(xs, natInt);
        check(java.util.Arrays.equals(xs, new Integer[] { 1, 3, 5 }), "sort via naturalOrder");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testWhiteBoxCounts();
        testBlackBox();
        testSgnAntisymmetryTable();
        testTransitivity();
        testReflexivity();
        testEqualsConsistency();
        testCompareToNull();
        testCharacterExactDiff();
        testIntegerExtremes();
        testStringLexicographicExact();
        testCustomComparableMatrix();
        testNaturalOrderConsistency();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

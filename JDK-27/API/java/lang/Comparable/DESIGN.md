# java.lang.Comparable 测试方案

## 1. API 功能（按方法分组）

- 单一抽象方法：`int compareTo(T o)`（返回负/零/正；泛型 `Comparable<T>`，擦除为 `compareTo(Object)`）。
- 语义契约：自反（`x.compareTo(x)==0`）、反对称（符号相反）、传递（`a<b && b<c => a<c`）、与 `equals` 一致（自然序）。
- 配套：`Comparator.naturalOrder()` 复用 `compareTo`。

## 2. Java 用户如何使用

- 标准类型：`Integer`/`String` 天然 `Comparable`；`"apple".compareTo("banana") < 0`。
- 自定义类型：`class Version implements Comparable<Version>` 实现 `compareTo`，交给 `Collections.sort(List)` 排序。
- 组合链路：`List<Version> vs = ...; Collections.sort(vs);` → 断言有序；`Comparator.naturalOrder().compare(a,b) == a.compareTo(b)`。

## 3. 开源代码实际用法调研

来源：JDK 测试（权威）+ 通用认知（标注）。

- JDK 大量类实现 `Comparable`（`String`、`Integer`、`BigDecimal`、`java.time` 各类型等），`Collections.sort`/`TreeMap`/`TreeSet`/`Arrays.sort` 依赖自然序。
- `test/jdk` 中 `CharSequence/Comparison.java` 也通过 `CharSequence.compare` 内部走 `Comparable.compareTo` 快速路径。
- 自定义业务类型（版本号、排序键）普遍实现 `Comparable` 以支持稳定自然排序。

## 4. 测试用例设计 + 自动化实现

- 正常链路：`Integer`/`String` 的 `compareTo` 符号 → 自定义 `Version` 实现 → `Collections.sort` 断言自然序 → `Comparator.naturalOrder()` 一致性。
- 边界/异常：`compareTo(null)`（NPE）；跨类型（`Comparable c = Integer.valueOf(1); c.compareTo("x")`）抛 `ClassCastException`。
- 不变量：反对称（`signum(a.compareTo(b)) == -signum(b.compareTo(a))`）、传递、字典序（`"10".compareTo("9")<0`）、大小写敏感（`"a".compareTo("A")>0`）。
- 自动化实现：`ComparableTest.testWhiteBox()`（反射：接口/抽象/`compareTo(Object)`/单类型参数）+ `testBlackBox()`（`testInteger`、`testString`、`testSort`、`testCustomComparable`、`testNullAndClassCast`、`testSignProperties`）。

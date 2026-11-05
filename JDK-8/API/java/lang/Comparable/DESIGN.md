# java.lang.Comparable 测试设计案（JDK-8）

> 覆盖目标：**契约数学**，不是"比一下大小"的冒烟。重点补：白盒（恰 1 个抽象方法 compareTo——
> 函数式形态）、sgn 反对称全对称抽检表（Integer/String/Character 各 3 值 9 对）、传递性三连、
> 自反性、与 equals 一致性（Integer 缓存内外两档）、compareTo(null) → NPE（三内置，消息按
> 版本分支锁定）、Character 'a'-'B'==31 精确差、Integer 极值、String 字典序精确差、自造
> Comparable 全矩阵（3 值 9 格符号）+ 排序总序、Comparator.naturalOrder 一致。

## 1. API 提供了哪些功能

- **接口** `Comparable<T>`：唯一方法 `int compareTo(T o)`；
  契约：sgn(compare(x,y)) == -sgn(compare(y,x))；传递性；自反 compare(x,x)==0；
  强烈建议与 equals 一致（compare(x,y)==0 ⇒ x.equals(y)——Integer/String/Character 满足，
  BigDecimal 是经典反例，归 java.math 批）。
- compareTo(null) → NullPointerException（未 null 检查时自然抛出，非可选）。

## 2. Java 用户如何使用

- 实现自然序：`class Version implements Comparable<Version>`；
- 依赖类库：`Collections.sort`/`Arrays.sort`/`TreeMap`/`PriorityQueue` 均调 compareTo。

```java
Collections.sort(list);                 // 元素自然序
if (a.compareTo(b) < 0) { ... }
```

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/Comparable.html`）：sgn 规则四条公理。
- **JLS §3.10 / Character 数值**：char 的数值是其码位（'a'=97、'B'=66、差 31）。
- **OpenJDK 8u-dev** `Integer.compareTo`/`String.compareTo` 源码（前者数值差、后者逐字符+长度差）。

## 4. 测试用例设计（映射到 ComparableTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：1 公共方法、唯一抽象 compareTo、类型参数 T | testWhiteBox |
| P0 | sgn 反对称全表（Integer/String/Character × 9 对） | testSgnAntisymmetryTable |
| P0 | 传递性（1<2<5 ⇒ 1<5；三内置 + 自造） | testTransitivity |
| P0 | 自反 compare(x,x)==0 | testReflexivity |
| P0 | equals 一致性：缓存档 127 ==、非缓存档 new Integer(1000) != 但 compare 0 | testEqualsConsistency |
| P0 | compareTo(null) → NPE（消息 8 侧 null / 27 侧 helpful，MODERN 分支） | testCompareToNull |
| P0 | Character 码位差：'a'-'B'==31、reverse -31、MIN<MAX | testCharacterExactDiff |
| P1 | Integer 极值：MIN/MAX 全对 + 静态 compare 一致 | testIntegerExtremes |
| P1 | String 字典序精确差：前缀 -1、'1'-'9'==-8、'a'-'A'==32 | testStringLexicographicExact |
| P1 | 自造 Point 全矩阵 9 格符号 + 排序总序 | testCustomComparableMatrix |
| P2 | Comparator.naturalOrder 与实例方法一致 | testNaturalOrderConsistency |
| P0 | Collections.sort 自然序（原有） | testSort |

## 易错规范细节

- **compareTo(null) 消息分版本**：JDK-8 NPE 无消息；JDK-27 helpful 消息
  `Cannot read field "value" because "anotherInteger" is null`（String/Character 同构）——
  同一源文件用 `MODERN = !System.getProperty("java.version").startsWith("1.")` 分支锁定。
- **equals 一致性分缓存档**：Integer.valueOf(127) 同实例（缓存）；`new Integer(1000)` 不同
  实例但 compareTo==0——compare 是值语义不是同一性。
- **String.compareTo 的两个规则**：先逐字符差（'1'-'9' = -8，"10"<"9" 字典序非数值序）；
  前缀相同时长度差（"ab" vs "abc" = -1）。
- **char 比较即码位比较**：'a'(97) - 'B'(66) = 31 精确可断言。

# java.text.Collator 测试方案

## 1. API 功能（按方法分组）

- 常量：强度 `PRIMARY=0`/`SECONDARY=1`/`TERTIARY=2`/`IDENTICAL=3`；分解 `NO_DECOMPOSITION=0`/`CANONICAL_DECOMPOSITION=1`/`FULL_DECOMPOSITION=2`。
- 工厂：`getInstance()`、`getInstance(Locale)`、`getAvailableLocales()`。
- 比较：抽象 `compare(String,String)`、`compare(Object,Object)`（实现 Comparator）、`equals(String,String)`。
- 排序键：抽象 `getCollationKey(String)`。
- 属性：`get/setStrength`、`get/setDecomposition`。
- 契约：`clone`、`equals(Object)`、抽象 `hashCode`。

## 2. Java 用户如何使用

- 工厂实例化：`Collator.getInstance(Locale.US)` 获得按 locale 排序的比较器。
- 典型调用：`col.compare("a","b")` 判断排序顺序；`col.setStrength(Collator.PRIMARY)` 忽略大小写；`col.getCollationKey(s)` 得到可反复比较的排序键。
- 组合链路：工厂实例化 → `compare` 排序 → `setStrength` 控制大小写/重音敏感度 → `getCollationKey` 生成排序键并 `compareTo` → `setDecomposition` 控制分解 → `clone`/`equals`。

## 3. 开源代码实际用法调研（来源明确）

- JDK 源码 `java.text.RuleBasedCollator` 继承 Collator，实现基于规则的 `compare`/`getCollationKey`。
- JDK 测试 `test/jdk/java/text/Collator/*.java`：断言各 locale 的 compare 结果、strength 行为与排序键。
- 通用 i18n：`Collections.sort(list, Collator.getInstance(locale))` 做本地化字符串排序。

## 4. 测试用例设计 + 自动化实现

实现位置：`CollatorTest.java`（`testWhiteBox()` 白盒 + `testCompare()`/`testStrength()`/`testCollationKey()`/`testDecompositionAndContract()` 黑盒）。

- 正常链路：`compare` 的 <0/==0/>0 与 `equals(String,String)`、`compare(Object,Object)`。
- 强度链路：默认 TERTIARY 大小写敏感，`setStrength(PRIMARY)` 后 `"a"=="A"`，SECONDARY/IDENTICAL 回读。
- 排序键链路：相等字符串键相等、键 `compareTo` 顺序与 `compare` 一致。
- 分解链路：`setDecomposition` NO/CANONICAL/FULL 回读。
- 契约：`clone` 相等、`hashCode` 相等、自反相等。
- 健壮性：固定 Locale.US、无墙钟/随机/全局副作用。headless-safe，不加 `@key`。

## 契约深化（本批新增，两侧同构断言）

- Strength 矩阵（PRIMARY 忽略重音+大小写/SECONDARY 区分重音/TERTIARY 区分大小写/IDENTICAL 码点敏感，é 预组 vs e+U+0301 在 IDENTICAL+NO_DECOMPOSITION 下 !=0（实测 132），+CANONICAL 下 ==0）
- 默认 TERTIARY/NO_DECOMPOSITION；decomposition 可切换（CANONICAL 折叠组合序列）
- getCollationKey 与 compare 一致性 + toByteArray 确定性；getCollationKey(null)→null
- compare(null,·)/getInstance(null)→NPE；equals 含 strength；getAvailableLocales

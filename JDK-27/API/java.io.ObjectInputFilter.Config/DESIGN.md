# java.io.ObjectInputFilter.Config 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- `createFilter(String pattern)`：按 `;` 分隔的模式串生成过滤器——类/包通配（`java.lang.*`、`java.lang.String`）、`!` 前缀拒绝、`*` 兜底；限额 `maxdepth/maxrefs/maxbytes/maxarray=N`。
- `getSerialFilter()`/`setSerialFilter(...)`：JVM 全局过滤器（本测试只读不写，避免全局副作用）。
- `getSerialFilterFactory()`/`setSerialFilterFactory(BinaryOperator)`：JVM 全局过滤器工厂（默认 BuiltinFilterFactory）。

## 2. Java 用户如何使用
- 启动参数 `-Djdk.serialFilter='com.app.*;!*'` 或运行时 `Config.createFilter(...)` + `ois.setObjectInputFilter(f)`。
- 限额模式：`createFilter("maxdepth=5;maxrefs=10000")` 防深度/引用炸弹。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- `ObjectInputFilter.Config.createFilter("!*" )` 与模式/限额矩阵 — `jdk25u-dev/test/jdk/java/io/Serializable/serialFilter/SerialFilterFunctionTest.java`（全局模式、限额、filter factory 的官方测试）。
- filter factory 设置/读取 — `jdk25u-dev/test/jdk/java/io/Serializable/serialFilter/SerialFilterFactoryTest.java`。
- 解析语义（类模式、限额先于类模式评估、负计数拒绝、`toString` 回显模式串）— `jdk25u-dev/src/java.base/share/classes/java/io/ObjectInputFilter.java`（Config.Global.checkInput 与 Global pattern 解析）。

## 4. 测试用例设计 + 自动化实现（指向 ConfigTest.java 黑盒 `testBlackBox`）
- 白盒: static final 嵌套类、唯一私有构造、createFilter/getSerialFilter/getSerialFilterFactory 静态存在。
- 黑盒场景:
  1. 类模式矩阵：`java.lang.*`/`java.util.*` 允许、`!*` 拒绝、primitive→UNDECIDED、无类→UNDECIDED。
  2. 精确类名（无 `!*` 兜底）未命中→UNDECIDED。
  3. maxarray 对 arrayLength 的边界（>N 拒、=N 未决）。
  4. maxdepth/maxrefs/maxbytes 各自边界（含"恰等于"未决）。
  5. 负计数 FilterInfo → REJECTED。
  6. 组合模式：限额先于类模式（depth 超限覆盖类允许）。
  7. toString 回显模式串。
  8. 坏模式 `bogus=value` → IAE("unknown limit")；裸单词是类模式（不抛）。
  9. getSerialFilterFactory 为 builtin；getSerialFilter 只读检查（**不写全局**）。
  10. 接线：`!*` 模式令 readObject 抛 InvalidClassException。
- 健壮性: 不调用 setSerialFilter/setSerialFilterFactory（JVM 全局状态，避免污染并跑）；全部用受控 FilterInfo 断言，确定性强。

产出：`ConfigTest.java`（白盒 5 + 黑盒 ~24 断言）。

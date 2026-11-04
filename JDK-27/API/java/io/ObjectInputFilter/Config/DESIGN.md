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

## 深化追加（契约级）

新增维度（`testEmptyOrNullPatterns`…`testFactoryAccessorsReadonly`，断言 30 → 71）：
- **空/ null 模式**：createFilter("") 与 createFilter(";") 返回 **null**（文档化行为，非抛异常）；createFilter(null) → NPE `pattern`。
- **畸形模式矩阵（实测消息）**：`maxdepth=abc` → NumberFormatException `For input string: "abc"`；`maxdepth=-1`/`maxrefs=-5` → IAE `negative limit: <n>`；`/java.base` → IAE 含 `module name is missing` 且回显模式；`java.base/` → IAE 含 `class or package missing`；`.*` → IAE 含 `package missing`；`bogus=value` → IAE `unknown limit`（原有）。
- **模块模式**：`java.base/java.lang.String`、`java.base/*`、`java.base/java.lang.*` 对 String 均 ALLOWED；错误模块前缀 `nomodule/...` → UNDECIDED。
- **首匹配胜出（顺序不对称）**：`String;!*` → String=ALLOWED/Long=REJECTED；`!*;String` → 两者均 REJECTED。
- **`*` 范围**：匹配普通引用类；**不**匹配数组类（int[].class）、原始类（long.class）、void；`[I` 与 `java.lang.String[]` 形式均不匹配数组（记录行为）。
- **嵌套名形式**：二进制 `$` 名（java.util.Map$Entry）匹配；点号名（java.util.Map.Entry）不匹配。
- **重复限制**：`maxdepth=2;maxdepth=9` → 有效限制为**最后一个**（depth 3/5 通过、10 拒绝），toString 原样回显两段。
- **空白/大小写/取反**：` java.lang.String ` 不去空白（不匹配，toString 原样含空格）；模式大小写敏感；`!java.lang.String` 仅拒 String 其余 UNDECIDED；前导点 `.example.Class` 合法但永不匹配普通类名。
- **maxarray 适用条件**：arrayLength==-1（非数组或描述符阶段）时 maxarray 不参与判定（UNDECIDED）。
- **只读访问器**：getSerialFilterFactory().getClass().getTypeName() == `java.io.ObjectInputFilter$Config$BuiltinFilterFactory`；getSerialFilter() 只读观察（null 或过滤器实例）。

负例矩阵：
| 模式 | 异常 | 消息子串 |
|---|---|---|
| null | NullPointerException | `pattern` |
| maxdepth=abc | NumberFormatException | `For input string: "abc"` |
| maxdepth=-1 / maxrefs=-5 | IllegalArgumentException | `negative limit: <n>` |
| /java.base | IllegalArgumentException | `module name is missing` + 回显 |
| java.base/ | IllegalArgumentException | `class or package missing` |
| .* | IllegalArgumentException | `package missing` |
| bogus=value | IllegalArgumentException | `unknown limit` |

硬性约束：**绝不调用 setSerialFilter/setSerialFilterFactory**（全局状态）；JDK-8 无此类，仅 JDK-27 存在。

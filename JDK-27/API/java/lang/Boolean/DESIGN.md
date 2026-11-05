# java.lang.Boolean 测试方案（DESIGN）

## 1. API 功能（按方法分组，签名取自 JDK-27 javap）

- **常量**：`TRUE`、`FALSE`（单例）、`TYPE=boolean.class`。
- **工厂/解析**：`valueOf(boolean)`、`valueOf(String)`、`parseBoolean(String)`、`getBoolean(String)`。
- **对象**：`booleanValue`、`toString(boolean)`/`toString()`、`equals`、`hashCode`（1231/1237）、`hashCode(boolean)`。
- **比较/逻辑**：`compare`、`compareTo`、`logicalAnd`、`logicalOr`、`logicalXor`。

## 2. Java 用户如何使用

- **构造/工厂**：`Boolean.valueOf(true)` 恒返回 `TRUE` 单例；`Boolean.parseBoolean("true")` 大小写不敏感、其余（含 null）返回 false。
- **典型调用**：配置开关 `Boolean.parseBoolean(System.getProperty("debug"))`；逻辑组合 `Boolean.logicalAnd/Or/Xor`；排序 `Boolean.compare(false,true)<0`。
- **真实组合链路**：`valueOf(parseBoolean(s)).booleanValue()` 往返；`getBoolean(prop) → valueOf → compareTo/equals/hashCode`；装箱 `boolean b = boxed`。

## 3. 开源代码实际用法调研（标注来源）

- **JDK 源码（~/git/jdk25u-dev）**：
  - `src/java.base/share/classes/java/lang/invoke/MethodHandleStatics.java:69,72,74,76` 用 `Boolean.parseBoolean(getProperty(...))` 读取 `DEBUG_METHOD_HANDLE_NAMES`、`TRACE_INTERPRETER` 等调试开关。
  - `src/java.base/share/classes/java/lang/Boolean.java` 自身定义 `parseBoolean`/`valueOf` 单例语义。
- **JDK 测试（test/jdk）**：`test/jdk/java/lang/Boolean/` 覆盖 `parseBoolean`/`valueOf`/`getBoolean`。
- **Apache Commons Lang**（`org.apache.commons.lang3.BooleanUtils`）：`BooleanUtils.toBoolean(String)` 支持 `true/false/yes/no/on/off/1/0`，底层补充 JDK `Boolean.parseBoolean` 仅识别 "true" 的局限；`and/or/xor` 与 `Boolean.logical*` 同语义。
- **Spring Framework**（`org.springframework.util.StringUtils` / 转换器）：属性绑定布尔时使用 `Boolean.parseBoolean`/`Boolean.valueOf` 语义。
- **Netty**（配置解析）：常见 `Boolean.parseBoolean(...)` 读 TCP_NODELAY/keep-alive/debug 等开关（来源：Netty 配置与示例代码）。

## 4. 测试用例设计 + 自动化实现（→ BooleanTest.java）

- **正常**：`parseBoolean`（"true"/"TRUE"/"True" 为真，其余 false）；`valueOf` 单例；`toString`；`hashCode` 1231/1237。
- **组合**：`valueOf(parseBoolean(s)).booleanValue()`；`logicalAnd/Or/Xor`；`compare/compareTo`（false<true）；装箱/拆箱。
- **边界**：`parseBoolean(null)==false`（不抛 NPE）；`getBoolean` 缺省/非法值返回 false。
- **异常**：无（该类的 API 都不抛异常，测试需显式断言这一点）。
- **健壮性**：用例确定、无全局副作用——不依赖墙钟/随机数/哈希迭代顺序/默认时区/地区；`System.setProperty`（`getBoolean`）用 `try/finally` 恢复；无文件/端口/线程/`System.exit`，适配多次运行、并跑、跨平台。

## 契约深化（2026-11）

### API 功能（本轮新增维度）
- **parseBoolean 精确语义**：仅忽略大小写的 "true" 为 true——`"TRue"/"tRUe"` true，`"true1"/"1"/"0"/" true"/"true "/"null"` 全 false（**不 trim**）；`parseBoolean(null)==false` 永不抛异常。
- **valueOf(String)** 与 parseBoolean 同规则返回 TRUE/FALSE 单例（`" true"`→FALSE 因无 trim）。
- **compare 精确值**：compare(false,true)==-1、compare(true,false)==+1、同值 0；compareTo 返回值精确（-1/0/1）；compareTo(null)→NPE。
- **单例与装箱**：`new Boolean(true) != Boolean.TRUE` 但 equals（构造器 9+ 弃用仍保留）；TRUE/FALSE 恒等；装箱 `Boolean b=null` 在条件中拆箱 → NPE。
- **logicalAnd/Or/Xor 全真值表**（2×2×3 组合 + Java 运算符等价断言）+ compare 真值表。
- **getBoolean 生命周期**：setProperty("true"/"TRUE"/"false"/"0") 各值路径 + clearProperty + 从未设置；try/finally 保存还原原值。
- **hashCode/toString**：1231/1237 双侧、静态与实例一致；toString 拼接、String.valueOf 一致。

### 使用方式
`parseBoolean`/`valueOf(String)`（永不抛）→ `booleanValue`/装箱拆箱 → `compare`/`logicalXxx` → `getBoolean`（系统属性，try/finally 还原）。

### 参考来源
- Oracle javadoc（parseBoolean 仅 equalsIgnoreCase("true")；TRUE/FALSE 单例；hashCode 1231/1237）。
- 双侧实测（Temurin 8u372 / 27.0.1）：compareTo(null) NPE、拆箱 NPE 行为一致（消息文本不同，仅锁类型）。

### 测试用例设计 + 易错规范细节
- 新增方法：`testParseMatrix`（10 parse + 6 valueOf + 5 往返）、`testCompareExact`（7 断言 + NPE）、`testSingletonAndBoxing`（13 断言 + 4×4 循环 + 拆箱 NPE）、`testLogicalMatrixFull`（16 断言）、`testGetBooleanLifecycle`（7 断言）、`testToStringForms`（4 断言）。
- 易错点：**parseBoolean 不做 trim**（" true" → false）；valueOf(String) 与 parseBoolean 完全同规则；系统属性键用专属前缀 lcB.bool.* 并在 finally 中还原原值。

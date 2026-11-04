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

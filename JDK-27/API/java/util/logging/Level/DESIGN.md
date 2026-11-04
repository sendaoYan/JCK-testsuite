# java.util.logging.Level 测试方案

白盒结构断言在 `LevelTest.testWhiteBox()`，黑盒行为断言在 `LevelTest.testBlackBox()`。

## 1. API 功能

- **常量（public static final）**：`OFF/SEVERE/WARNING/INFO/CONFIG/FINE/FINER/FINEST/ALL`，各带一个 `intValue`（OFF=Integer.MAX_VALUE，ALL=Integer.MIN_VALUE，中间 1000→300 递减）。
- **构造器（protected）**：`Level(String,int)`、`Level(String,int,String)`，供自定义等级子类扩展。
- **查询/转换**：`getName()`、`intValue()`、`toString()`、`getLocalizedName()`、`getResourceBundleName()`。
- **静态解析**：`parse(String)` 按名称/整数值/本地化名解析，未知值抛 `IllegalArgumentException`。
- **对象方法**：`equals(Object)`（按 intValue）、`hashCode()`。

## 2. Java 用户如何使用

- 直接引用标准常量：`logger.log(Level.WARNING, "x")`、`handler.setLevel(Level.INFO)`。
- 解析配置文件中的字符串：`Level.parse("WARNING")`、`Level.parse("800")`。
- 自定义等级：`class MyLevel extends Level { MyLevel(String n,int v){ super(n,v);} }`。
- 比较/判等：`if (rec.getLevel().intValue() >= Level.INFO.intValue())`、`Level.INFO.equals(Level.parse("800"))`。

## 3. 开源代码实际用法调研

> WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev` 与自带测试 `test/jdk`，逐条标注来源。

- `test/jdk/java/util/logging/LoggerSubclass.java:53`：`logger.isLoggable(Level.WARNING)` —— 用标准常量做等级判断。
- `test/jdk/java/util/logging/LoggerSubclass.java:64`：`equal(l.getLevel(), Level.WARNING)` —— getLevel 与常量比较。
- `test/jdk/java/util/logging/LogRecordThreadIdTest.java:50-52`：`new LogRecord(Level.INFO, "record")` —— 构造日志记录时传入标准等级。
- `src/java.logging/share/classes/java/util/logging/Level.java:484`：`public static synchronized Level parse(String name)` —— 定义处。

## 4. 测试用例设计 + 自动化实现

- **正常**：9 个标准常量 `intValue` 精确值 + 严格单调排序；`getName/toString` 精确断言；`parse("INFO")==INFO`、`parse("800")==INFO` 往返。
- **组合**：`getLocalizedName()` 非空、标准等级 `getResourceBundleName()` 非空。
- **边界/异常**：`parse("bogus")`、`parse("")` 抛 `IllegalArgumentException`；`equals(null)`/`equals("INFO")` 为 false；`equals` 按值判等、`hashCode` 一致性。
- **自定义等级**：`MyLevel extends Level` 验证 `getName/intValue/toString/getResourceBundleName/equals`。
- **健壮性**：固定常量与字符串，无墙钟/随机/文件/网络/全局状态。

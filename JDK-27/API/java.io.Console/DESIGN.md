# java.io.Console 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 获取：`System.console()`（非终端环境返回 null；构造器包私有）。
- 输出：`writer()`、`format/printf(String/Object..., Locale...)`、`flush()`。
- 输入：`reader()`、`readLine(...)`、`readPassword(...)`（char[]，阅后即焚）。
- 元数据：`charset()`（JDK 17+）、`isTerminal()`（JDK 22+）。

## 2. Java 用户如何使用
- CLI 交互：console()!=null 时 printf 提示 + readPassword 读密钥；为 null 回退 stdin/stdout 管道模式。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- 包私有构造与单例装配 — `jdk25u-dev/src/java.base/share/classes/java/io/Console.java:111,548,567`（Console()、isTerminal、instantiateConsole）。
- 官方测试 — `jdk25u-dev/test/jdk/java/io/System/console` 相关用例。

## 4. 测试用例设计 + 自动化实现（ConsoleTest.testBlackBox）
1. jtreg 重定向环境断言 `System.console()==null`（重复调用稳定）。
2. 白盒：无 public 构造器；writer→PrintWriter、reader→Reader、readPassword→char[]、charset→Charset、isTerminal→boolean；readLine/format/printf 的普通与 Locale 重载齐全；printf 返回 Console（链式）。
- 健壮性: headless 断言不依赖终端（jtreg 管道环境恒 null）；不输出任何内容。

产出：`ConsoleTest.java`（白盒 + 黑盒共 26 断言）。

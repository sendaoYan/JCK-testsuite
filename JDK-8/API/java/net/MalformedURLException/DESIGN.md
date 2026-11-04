# java.net.MalformedURLException 测试设计案（JDK-8）

## 1. API 功能

- URL 字符串格式非法（无协议/未知协议/端口非法/嵌套 jar 等）时抛出的受检异常。
- 构造器仅 2 个：`MalformedURLException()`、`MalformedURLException(String)`（探针确认两侧一致，无 cause 重载）。
- 继承 `java.io.IOException`；Serializable（serialVersionUID = -182787522200415866，探针双侧实测一致）。

## 2. 使用方式

`new URL(spec)` 及各构造器在解析失败时抛出；调用方按 IOException 家族捕获。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/net/MalformedURLException.html）。
- OpenJDK `~/git/jdk8u-dev/jdk/test/java/net/URL/`（各异常用例的消息文本）。
- 探针：/tmp/probe-B/PMue.java（双侧运行）。

## 4. 测试用例设计（MalformedURLExceptionTest.java，45 断言）

| 测试点 | 方法 | 断言要点 |
|---|---|---|
| 构造器 | testConstructors | (String) getMessage/getLocalizedMessage；() 双 null；getDeclaredConstructors().length == 2 |
| 层级 | testHierarchy | instanceof IOException/Exception；specific catch 优先；可作 cause 嵌套 |
| 序列化 | testSerialization | **serialVersionUID == -182787522200415866**；message/null message/cause/suppressed 全部 round-trip 保留；复活对象仍可 catch |
| cause/suppressed 语义 | testCauseSemantics | initCause 返回 this、一次性（二次 ISE）、自引用 IAE；addSuppressed/getSuppressed；toString 精确形式；fillInStackTrace 返回 this |
| URL 实抛消息矩阵 | testThrownByURL | "unknown protocol: X"、"no protocol: X"、"Invalid port number :-2"、"Nested JAR URLs are not supported"、null spec → MUE（非 NPE） |

## 易错规范细节

- 只有两个构造器（无 (String,Throwable)/(Throwable) 重载）——任务书原记 4 个为误。
- `new URL((String) null)` 抛 MUE 而非 NPE（两侧一致，消息文本两侧不同故只断类型）。
- serialVersionUID 为公开稳定契约，两侧一致可精确断言。

## 版本差异（JDK-8 vs JDK-27）

无行为差异；仅 null spec 的 MUE 消息文本不同（JDK-8 "null"，JDK-27 helpful NPE 文本）。

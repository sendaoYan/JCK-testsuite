# MalformedURLException 设计文档

## 一、API 功能

URL 字符串格式非法时抛出的异常。

## 二、Java 用户如何使用

用户在 new URL(spec) 时捕获。

## 三、开源代码实际用法调研

所有解析用户输入 URL 的代码都需要处理。

## 四、测试用例设计

继承 IOException；构造器；new URL("http://example.com:abc/") 触发。

## 缺口分析与深化（批次 B，2026-11-05）

原版 9 断言。深化后 45 断言（与 JDK-8 侧同构），新增：

| 深化点 | 方法 | 断言要点 |
|---|---|---|
| 构造器精确枚举 | testConstructors | getDeclaredConstructors().length == 2（仅 () 与 (String)，探针确认） |
| 序列化 | testSerialization | **serialVersionUID == -182787522200415866**（与 JDK-8 一致）；message/null message/cause/suppressed round-trip 全保留 |
| cause/suppressed | testCauseSemantics | initCause 返回 this 且一次性（二次 ISE）、自引用 IAE；addSuppressed；toString "java.net.MalformedURLException: m"；fillInStackTrace 返回 this |
| URL 实抛消息矩阵 | testThrownByURL | "unknown protocol: nosuchproto"、"no protocol: relative/path"、"Invalid port number :-2"、"Nested JAR URLs are not supported"、null spec → MUE（非 NPE，消息为 helpful NPE 文本——与 JDK-8 的 "null" 不同，仅断类型） |

## 版本差异（JDK-8 vs JDK-27）

null spec 的 MUE 消息：JDK-8 "null" vs JDK-27 "Cannot invoke \"String.length()\" because \"spec\" is null"；行为（异常类型）两侧一致。

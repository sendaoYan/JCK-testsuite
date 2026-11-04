# HttpRetryException 设计文档

## 一、API 功能

HTTP 重试时抛出的异常，携带响应码与原因短语。

## 二、Java 用户如何使用

用户在 URLConnection 客户端设置响应缓存重试逻辑时捕获。

## 三、开源代码实际用法调研

HTTP 缓存重试场景使用。

## 四、测试用例设计

继承 IOException；构造(int responseCode, String detail) 与带 location 构造；getResponseCode/getReason/getLocation。

---

## 批次 C 深化（URLConnection 簇）

> 原测试仅 ~4 断言的消息冒烟。本批补齐：构造器矩阵（含 null 消息/reason）、精确
> serialVersionUID、序列化 round-trip（消息/字段保持）、cause/suppressed 状态机、
> 作为 IOException 的捕获互通。responseCode()/getReason()/getLocation() 的 null 与边界码矩阵是本批重点。

## 1. API 功能

IOException 子类，携带 HTTP 重试上下文：responseCode()、getReason()（==getMessage）、
getLocation()（2 参构造为 null）。构造器 (String reason,int code) 与 (String,int,String location)；
code 无范围校验（0/-1/MAX_VALUE 均接受）；三字段均参与序列化。

## 2. 使用方式

HTTP 客户端在收到需要重定向/重试的响应且无法自动处理时抛出；调用方读 responseCode 与
location 决定重试目标。

## 3. 参考来源

- Oracle javadoc；ObjectStreamClass.lookup 实测 serialVersionUID（双 JDK 一致）。
- 探针 ProbeC.java I 节 + ProbeC4。

## 4. 测试用例设计（映射到 HttpRetryExceptionTest.java）

| 测试方法 | 覆盖点 |
|---|---|
| testOriginal（保留） | 原有消息/层次断言 |
| testWhiteBox | public、直接超类 IOException、构造器集合（无 (String,Throwable)）、Serializable |
| testBlackBox | 消息矩阵（null/非 null）、getMessage、toString、getCause 默认 null + initCause 一次、getSuppressed 空 |
| testThrowableState | 堆栈非空、addSuppressed 顺序、fillInStackTrace 返回 this、IOException/Exception 捕获互通、带 cause 序列化 round-trip |
| testSerialization | serialVersionUID 精确值（-9186022286469111381L）、round-trip 消息保持、responseCode/reason/location 三字段保持（含 null reason/location 用例） |
| testBlackBox | 两个构造器、getter 矩阵、getMessage==reason、null reason/location、任意 code（0/-1/MAX_VALUE）、空串 reason/location |

易错规范细节：serialVersionUID 是规范常量（-9186022286469111381），两侧一致可精确断言；round-trip 经
Throwable 自定义 readObject，cause 也保持。
（双侧行为一致，serialVersionUID 相同。）
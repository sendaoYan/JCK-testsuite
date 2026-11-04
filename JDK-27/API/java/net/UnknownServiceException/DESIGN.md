# UnknownServiceException 设计文档

## 一、API 功能

URLConnection 请求了不支持的协议服务(如未知输入/输出)时抛出的异常。

## 二、Java 用户如何使用

用户在 URL.openConnection().getOutputStream() 等不支持的协议操作捕获。

## 三、开源代码实际用法调研

协议不支持特定操作的场景。

## 四、测试用例设计

继承 IOException；构造器。

---

## 批次 C 深化（URLConnection 簇）

> 原测试仅 ~4 断言的消息冒烟。本批补齐：构造器矩阵（含 null 消息/reason）、精确
> serialVersionUID、序列化 round-trip（消息/字段保持）、cause/suppressed 状态机、
> 作为 IOException 的捕获互通。

## 1. API 功能

IOException 子类：URLConnection.getInputStream()/getOutputStream() 默认实现抛出
（消息 "protocol doesn't support input"/"protocol doesn't support output"）。构造器 () 与 (String)。

## 2. 使用方式

协议处理器声明不支持输入/输出流时抛出；测试断言 URLConnection 最小子类的默认流行为（见 URLConnectionTest）。
本文件聚焦异常对象自身契约与序列化稳定性。

## 3. 参考来源

- Oracle javadoc；ObjectStreamClass.lookup 实测 serialVersionUID（双 JDK 一致）。
- 探针 ProbeC.java I 节 + ProbeC4。

## 4. 测试用例设计（映射到 UnknownServiceExceptionTest.java）

| 测试方法 | 覆盖点 |
|---|---|
| testOriginal（保留） | 原有消息/层次断言 |
| testWhiteBox | public、直接超类 IOException、构造器集合（无 (String,Throwable)）、Serializable |
| testBlackBox | 消息矩阵（null/非 null）、getMessage、toString、getCause 默认 null + initCause 一次、getSuppressed 空 |
| testThrowableState | 堆栈非空、addSuppressed 顺序、fillInStackTrace 返回 this、IOException/Exception 捕获互通、带 cause 序列化 round-trip |
| testSerialization | serialVersionUID 精确值（-4169033248853639508L）、round-trip 消息保持 |

易错规范细节：serialVersionUID 是规范常量（-4169033248853639508），两侧一致可精确断言；round-trip 经
Throwable 自定义 readObject，cause 也保持。
（双侧行为一致，serialVersionUID 相同。）
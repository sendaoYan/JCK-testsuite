# URLStreamHandlerFactory 设计文档

## 一、API 功能

工厂接口，根据 protocol 字符串返回对应的 URLStreamHandler。

## 二、Java 用户如何使用

用户实现 createURLStreamHandler(String) 并通过 URL.setURLStreamHandlerFactory 注册全局协议。

## 三、开源代码实际用法调研

某些框架注册自定义协议(如 classpath:)时实现此接口。

## 四、测试用例设计

验证为单抽象方法接口；实现一个返回 null/自定义 handler 的工厂并调用其 SAM 方法。

---

## 批次 C 深化（URLConnection 簇）

> 原测试 ~5 断言。本批补齐：接口结构、记录式工厂、**URL.setURLStreamHandlerFactory 一次
> 安装 + 二次 Error("factory already defined")**、经工厂的自定义协议 URL 全组件解析、
> getDefaultPort 覆写生效、字面量 IP equals/hashCode、未知协议 MUE、内建协议回退不受影响。

## 1. API 功能

单方法工厂接口：URLStreamHandler createURLStreamHandler(String protocol)。URL 构造时按
协议查 handler：缓存 → 工厂（返回 null 则回退内建 file/http/jar/...）。未知协议 →
MalformedURLException("unknown protocol: X")。安装仅一次。

## 2. 使用方式

URL.setURLStreamHandlerFactory(...) 注册自定义协议处理器；返回 null 保持内建行为。

## 3. 参考来源

- Oracle javadoc；OpenJDK URL.getURLStreamHandler。
- 探针 ProbeC.java H 节。

## 4. 测试用例设计（映射到 URLStreamHandlerFactoryTest.java）

| 测试方法 | 覆盖点 |
|---|---|
| testWhiteBox | 单方法结构、lambda 可用 |
| testRecordingFactory | 协议记录顺序、null 协议记录 |
| testRegisterAndUse（最后跑） | 注册后 chfproto:// URL 全组件（host/port/userInfo/authority/file/query/ref）、默认端口覆写 99、toExternalForm 重建、openConnection 委托、相对解析、127.0.0.1 equals/hashCode/异端口不等、工厂被咨询、nosuchproto → MUE 含 "unknown protocol"、file:/http: 内建仍可用、二次注册 → Error 精确消息 |
| testBlackBox（保留） | 原有冒烟 |
（双侧行为一致。）
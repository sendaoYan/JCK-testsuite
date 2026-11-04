# java.net.ContentHandler 设计文档（批次 C 深化）

> 原测试 ~8 断言。本批补齐：单参抽象方法记录契约（连接按身份传递、null 允许）、
> **双参默认实现的精确语义**（每次恰好转发一次单参、数组顺序首个 isInstance 命中、超类匹配、
> 无命中/空数组 → null、含 null 元素 → NPE）、双参覆写接收精确数组身份。

## 1. API 功能

内容处理器：唯一抽象方法 getContent(URLConnection)；getContent(URLConnection, Class[]) 是
**具体默认方法**：`obj = getContent(urlc); for (c : classes) if (c.isInstance(obj)) return obj; return null;`
——每次调用恰好触发一次单参调用；匹配按数组顺序首个命中（含超类）；无匹配/空数组返回 null；
数组含 null 元素在 Class.isInstance 处 NPE（消息文本 JDK 8/27 不同，仅断言类型）。

## 2. 使用方式

URLConnection.getContent() 经 ContentHandlerFactory 找到处理器后调用单参形式；
getContent(Class[]) 由调用方给出可接受类型列表。

## 3. 参考来源

- Oracle javadoc；OpenJDK ContentHandler.java（默认实现逐行核对）。
- 探针 ProbeC.java G 节。

## 4. 测试用例设计（映射到 ContentHandlerTest.java）

| 测试方法 | 覆盖点 |
|---|---|
| testWhiteBox | 单参 abstract、双参 concrete（默认实现） |
| testSingleArgContract | 返回值、调用计数、连接按身份（==）传递、null 连接照传、多次调用记录最新连接 |
| testTwoArgDefaultDelegation | 精确匹配/超类 CharSequence/Object 匹配/无匹配 null/空数组 null/首个命中顺序/每次恰好一次单参转发/null 元素 NPE |
| testTwoArgOverride | 覆写双参：连接与类数组按身份接收（**必须用裸 Class[]，ContentHandler 未泛型化**） |
| testBlackBox（保留） | 原有冒烟 |
（双参 null 元素 NPE 的消息文本不同，仅断言类型；其余一致。）
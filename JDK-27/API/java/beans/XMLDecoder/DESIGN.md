# java.beans.XMLDecoder 测试设计案

> 覆盖目标：完整契约（AutoCloseable、五构造器、bean/数组/List/Map/String/Integer/null round-trip、owner、异常监听器、坏输入）。

## 1. API 功能
- 构造：`(InputStream)`、`(InputStream, owner)`、`(InputStream, owner, ExceptionListener)`、`(InputStream, owner, ExceptionListener, ClassLoader)`、`(InputSource)`。
- 读：`readObject`；owner：`setOwner`/`getOwner`；异常监听器：`setExceptionListener`/`getExceptionListener`；`close`。
- 静态：`createHandler(owner, listener, cl)` 返回 SAX `DefaultHandler`。

## 2. 使用方式
- 读取 `XMLEncoder` 产生的 XML 还原对象图。

## 3. 参考来源
- Java SE 8/27 javadoc（`java/beans/XMLDecoder.html`）。
- OpenJDK `test/jdk/java/beans/XMLDecoder`；JCK `api/java_beans/XMLDecoder`。

## 4. 测试用例设计
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | AutoCloseable 结构 | testStructure |
| P0 | bean/数组/List/Map/String/Integer/null round-trip | testRoundTrip |
| P0 | 五构造器 + setOwner/setExceptionListener + createHandler | testConstructorsAndOwner |
| P1 | 空/垃圾输入 readObject 抛 ArrayIndexOutOfBoundsException | testBadInput |

## 关键规范细节
- 空输入或非法 XML 在 `readObject` 时抛 `ArrayIndexOutOfBoundsException`（JDK8 消息为 "0"，JDK27 为 "Index 0 out of bounds for length 0"——只断言类型不断言消息）。
- 默认 ExceptionListener 会把编码异常打到 stderr 并继续；可注入自定义监听器。

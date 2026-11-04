# javax.net.ssl.SNIServerName 黑盒行为测试设计

## 1. API 功能
抽象类，SNI 服务器名基类：
- `SNIServerName(int type, byte[] encoded)`：protected 构造，type<0 抛 IAE，encoded null 抛 NPE，encoded 被 clone。
- `getType()`、`getEncoded()`（返回 clone）。
- `equals`/`hashCode`/`toString` 基于 type + encoded。

## 2. Java 用户如何使用
继承实现具体 SNI 类型；`new SNIHostName(...)` 是唯一标准子类。

## 3. 开源代码实际用法调研
JDK `test/jdk/javax/net/ssl/ServerName/` 覆盖 SNI 解析与匹配；RFC 6066 定义服务器名扩展，OpenSSL/Netty 的 SNI 处理同构。

## 4. 测试用例设计 + 自动化实现
黑盒：getType/getEncoded 往返 + encoded 防御性拷贝；equals/hashCode 一致性；负 type 抛 IAE；null encoded 抛 NPE。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。

# javax.net.ssl.SNIHostName 黑盒行为测试设计

## 1. API 功能
final 类，host_name 类型（type=0）SNI：
- `SNIHostName(String hostname)`：经 `IDN.toASCII(hostname, USE_STD3_ASCII_RULES)` 编码；空串/尾点/非法 IDN 抛 IAE，null 抛 NPE；大小写保留。
- `SNIHostName(byte[] encoded)`：接受 US-ASCII 或 UTF-8 编码。
- `getAsciiName()`；`equals`/`hashCode` 大小写不敏感；`toString` 形如 `type=host_name (0), value=...`。
- `static createSNIMatcher(String regex)`：创建匹配器。

## 2. Java 用户如何使用
`new SNIHostName("www.example.com")` 放入 `SSLParameters.setServerNames`；服务端用 `createSNIMatcher(".*example.com")` 匹配。

## 3. 开源代码实际用法调研
JDK `test/jdk/javax/net/ssl/ServerName/BestEffortOnLazyConnected.java` 等用 SNIHostName 配置客户端 SNI；RFC 6066/3490 规定 host_name 编码；Netty `SniHandler` 等价。

## 4. 测试用例设计 + 自动化实现
黑盒：getAsciiName 大小写保留；byte[] 构造；equals/hashCode 大小写不敏感；负例 equals；空串/尾点/空格/非法 IDN 抛 IAE；null 抛 NPE。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。

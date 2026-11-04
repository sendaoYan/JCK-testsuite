# SSLSocket 契约测试设计（JDK-8）

## 1. API 功能
javax.net.ssl.SSLSocket：SSL socket（握手/会话/监听器/配置面）。

## 2. 使用方式
- 通过 `javax.net.ssl` 公共 API 直接调用；证书材料为内嵌固定 PKCS12（alias=server, CN=localhost, SAN DNS:localhost + IP:127.0.0.1），口令 `passphrase`。
- 网络一律回环：`createServerSocket(0, backlog, InetAddress.getLoopbackAddress())` + `127.0.0.1`/`localhost` 连接；accept 线程 join(8000)。

## 3. 参考来源
- Oracle javadoc（javax.net.ssl / javax.net）。
- OpenJDK 回归测试目录 `test/jdk/javax/net/`（仅借鉴思路，代码自写）。
- 双侧行为以 jdk8u372-b07 与 jdk-27.0.1.0.2+1 实测为准。

## 4. 测试用例设计
- 白盒：反射断言类/接口修饰符、方法面、构造器计数、常量值。
- 黑盒：真实握手 echo + HCE 双端触发 + SSLParameters SNI 往返 + pinned TLSv1.2 对。 无效套件/协议 IAE；addHandshakeCompletedListener(null) IAE；握手后 setUseClientMode IAE；钉 TLSv1.2 协商。
- 负例矩阵：非法参数逐项断言精确异常类型（IAE/NPE/IllegalStateException 等），使用 expect 包装返回异常 SimpleName。
- 正例统一 `static int passed; static void check(boolean,String)`，结束打印 `PASS (n assertions)`。

## 5. JDK-8 版本差异
- 8u372 默认启用 TLSv1.3（8u261+ 回移）；测试按 contains 断言而非全集。

# ExtendedSSLSession 契约测试设计（JDK-8）

## 1. API 功能
javax.net.ssl.ExtendedSSLSession：扩展会话（签名算法/SNI 请求查询；27 加 exportKeyingMaterial*）。

## 2. 使用方式
- 通过 `javax.net.ssl` 公共 API 直接调用；证书材料为内嵌固定 PKCS12（alias=server, CN=localhost, SAN DNS:localhost + IP:127.0.0.1），口令 `passphrase`。
- 网络一律回环：`createServerSocket(0, backlog, InetAddress.getLoopbackAddress())` + `127.0.0.1`/`localhost` 连接；accept 线程 join(8000)。

## 3. 参考来源
- Oracle javadoc（javax.net.ssl / javax.net）。
- OpenJDK 回归测试目录 `test/jdk/javax/net/`（仅借鉴思路，代码自写）。
- 双侧行为以 jdk8u372-b07 与 jdk-27.0.1.0.2+1 实测为准。

## 4. 测试用例设计
- 白盒：反射断言类/接口修饰符、方法面、构造器计数、常量值。
- 黑盒：真实握手双向断言：本地/对端签名算法、SNI 送达服务端、（27）密钥材料导出矩阵。 客户端会话回显其请求的 SNI；服务端 getRequestedServerNames 收到客户端 SNI；8 无 exportKeyingMaterial*（版本差异）。
- 负例矩阵：非法参数逐项断言精确异常类型（IAE/NPE/IllegalStateException 等），使用 expect 包装返回异常 SimpleName。
- 正例统一 `static int passed; static void check(boolean,String)`，结束打印 `PASS (n assertions)`。

## 5. JDK-8 版本差异
- 无 exportKeyingMaterial*（JDK-24 加入）；8u372 已启用 TLSv1.3。

# KeyManagerFactory 契约测试设计（JDK-8）

## 1. API 功能
javax.net.ssl.KeyManagerFactory：KeyManager 工厂（SunX509/NewSunX509/PKIX；X509 名不存在）。

## 2. 使用方式
- 通过 `javax.net.ssl` 公共 API 直接调用；证书材料为内嵌固定 PKCS12（alias=server, CN=localhost, SAN DNS:localhost + IP:127.0.0.1），口令 `passphrase`。
- 网络一律回环：`createServerSocket(0, backlog, InetAddress.getLoopbackAddress())` + `127.0.0.1`/`localhost` 连接；accept 线程 join(8000)。

## 3. 参考来源
- Oracle javadoc（javax.net.ssl / javax.net）。
- OpenJDK 回归测试目录 `test/jdk/javax/net/`（仅借鉴思路，代码自写）。
- 双侧行为以 jdk8u372-b07 与 jdk-27.0.1.0.2+1 实测为准。

## 4. 测试用例设计
- 白盒：反射断言类/接口修饰符、方法面、构造器计数、常量值。
- 黑盒：算法/提供者矩阵 + 未 init ISE + 口令负例 + 未知提供者 NSPException。 错误口令 UnrecoverableKeyException；空 keystore 仍返回 1 个 manager；getKeyManagers 每次新数组同实例。
- 负例矩阵：非法参数逐项断言精确异常类型（IAE/NPE/IllegalStateException 等），使用 expect 包装返回异常 SimpleName。
- 正例统一 `static int passed; static void check(boolean,String)`，结束打印 `PASS (n assertions)`。

## 5. JDK-8 版本差异
- NewSunX509 与 PKIX 别名双侧均可实例化；X509 均不可。

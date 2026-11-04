# java.rmi.Naming 行为测试设计

## 1. API 功能
`Naming` 是纯静态工具类（final、私有构造器），提供对 RMI 注册表的命名绑定操作：
- `lookup(String)`、`bind(String, Remote)`、`rebind(String, Remote)`、`unbind(String)`、`list(String)`。
- 名称格式为 `//host:port/name`（可省略 host/port，默认 localhost:1099）。
- 内部先 `parseURL` 解析（非法 URL 抛 `MalformedURLException`），再 `LocateRegistry.getRegistry` 取 stub（不连接），再调用 registry 方法。

## 2. Java 用户如何使用
- `Remote obj = Naming.lookup("//registry-host:1099/MyService");`
- `Naming.rebind("//localhost:1099/MyService", impl);`
- 关键行为：`getRegistry` 不建立连接；`bind/rebind` 传 null 对象在连接前抛 `NullPointerException`；空路径名（`//host:port/`）的 lookup 直接返回 registry stub 而不连接。

## 3. 开源代码实际用法调研
- JDK 测试 `test/jdk/java/rmi/Naming/legalRegistryNames/LegalRegistryNames.java`、`RmiIsNoScheme.java`、`DefaultRegistryPort.java`、`LookupNameWithColon.java`、`UnderscoreHost.java` 覆盖 URL 解析边界。
- 类定义 `src/java.rmi/share/classes/java/rmi/Naming.java`。

## 4. 测试用例设计 + 自动化实现
黑盒部分（NamingTest.testBlackBox，全部不真启动注册表）：
- `lookup("rmi:foo")` / `lookup("http://localhost/x")` 抛 `MalformedURLException`（纯解析、无网络）。
- `lookup("//localhost:0/")` 返回非 null Remote（空名直接返回 stub，不连接）。
- `bind/rebind("//localhost:0/x", null)` 抛 `NullPointerException`（连接前）。
- `list("rmi:foo")` / `unbind("rmi:foo")` 抛 `MalformedURLException`。
- `lookup("//localhost:0/x")` 抛 `ConnectException`（连接被拒，端口 0 被修正为 1099；实际可容忍 NotBoundException/RemoteException）。
白盒部分（testWhiteBox）：final 类、私有构造器、5 个 public static 方法。

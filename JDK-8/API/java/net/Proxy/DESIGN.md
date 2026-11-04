# Proxy 设计文档

## 一、API 功能

`java.net.Proxy` 表示一个代理设置：类型（`Type.DIRECT` / `Type.HTTP` / `Type.SOCKS`）+ 可选地址（`InetSocketAddress`，可为 unresolved）。非 final 类，`equals`/`hashCode` 为 final，不可变值对象。嵌套枚举 `Proxy.Type` 三常量（DIRECT=0, HTTP=1, SOCKS=2），另见 `Proxy/Type/DESIGN.md`。

## 二、Java 用户如何使用

- `Proxy.NO_PROXY` 常量表示"直连"；`new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port))` 构造代理。
- 传给 `Socket(Proxy)`、`URLConnection.openConnection(Proxy)`、从 `ProxySelector.select()` 结果中读取。
- `type()`/`address()` 读取；放进 `HashSet`/`List` 做去重/回退链。

## 三、开源代码实际用法调研（参考来源）

- OpenJDK jdk8u `java.net.Proxy` 源码：构造器校验 `(type == Type.DIRECT) || !(sa instanceof InetSocketAddress)` 则抛 IAE；`equals`/`hashCode` final。
- OpenJDK jtreg `test/java/net/Proxy/*`。
- javap -public 导出的公开面（字段/构造器/方法签名）。

## 四、测试用例设计（82 断言）

- **白盒**（javap）：public、非 final、NO_PROXY public static final、单构造器、`type()`/`address()` 返回类型、equals/hashCode final。
- **NO_PROXY**：单例身份（`==`）、type DIRECT、address null、toString `"DIRECT"`、equals 自反/与常量。
- **构造与存取器**：精确 toString `"HTTP @ /127.0.0.1:8080"`、`"SOCKS @ /127.0.0.1:1080"`；unresolved 地址保留。
- **构造错误**：DIRECT+地址 IAE；HTTP/SOCKS+null IAE；自定义 `SocketAddress` 子类 IAE "not compatible"。
- **equals/hashCode 契约**：对称、相等⇒hashCode 相等、类型/主机/端口任一不同即不等、unresolved 对。
- **集合语义**：HashSet 去重、contains。
- **Type 联动**：values 顺序、ordinal 递增。

## 五、易错规范细节（探针实证）

- `new Proxy(null, addr)` **不抛异常**（构造器只检查 `type==DIRECT` 与地址类型，null type 通过）：`type()` 返回 null、toString 为 `"null @ ..."`、equals 正常、**`hashCode()` 抛 NPE**。
- `new Proxy(null, null)` 抛 IAE（因 null 不是 InetSocketAddress）。
- NO_PROXY 与任何 HTTP/SOCKS 代理互不相等；地址用 unresolved 形式比较主机名+端口。
- toString 中的地址部分即 `InetSocketAddress.toString()`：JDK-8 下 unresolved 形如 `proxy.example.com:3128`（JDK-13+ 变为 `host/<unresolved>:port`，见 JDK-27 侧差异）。

## 六、版本差异（JDK-8 vs JDK-27）

- `InetSocketAddress.toString()` 对 unresolved 地址：JDK-8 `"proxy.example.com:3128"`；JDK-13+（含 27）`"proxy.example.com/<unresolved>:3128"`。其余 Proxy 语义两侧完全一致（本套两侧断言仅此一处不同）。

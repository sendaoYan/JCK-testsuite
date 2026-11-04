# Proxy 设计文档

## 一、API 功能

表示代理设置：类型(DIRECT/HTTP/SOCKS)与可选地址。

## 二、Java 用户如何使用

用户用 Proxy.NO_PROXY 或 new Proxy(Proxy.Type.HTTP, addr) 传给 Socket/URL.openConnection。

## 三、开源代码实际用法调研

需要代理访问外网的客户端使用。

## 四、测试用例设计

NO_PROXY 为 DIRECT 且 address==null；Type 枚举；构造/equals/hashCode/toString。

## 五、缺口分析（批次 H 深化追加）

原测试仅 27 断言（白盒 8 + 黑盒 19）。深化至 **102 断言**（jtreg Passed），新增覆盖：

- NO_PROXY 单例身份（`==`）、toString 精确串、与 String/null 的 equals 负例。
- 构造器精确 toString：`"HTTP @ /127.0.0.1:8080"`、`"SOCKS @ /127.0.0.1:1080"`。
- `new Proxy(null, addr)` 不抛（legacy 怪癖）：type() null、`"null @ ..."`、equals 正常、hashCode() NPE。
- DIRECT/null 地址、SOCKS null 地址、自定义 SocketAddress 子类 IAE（消息含 "not compatible"）。
- equals/hashCode 对称/端口/主机/类型差异矩阵、unresolved 地址对、HashSet 去重。
- Type ordinal 顺序联动。

## 六、版本差异（JDK-27 实证）

- unresolved 地址 toString：JDK-13+ 为 `host/<unresolved>:port`（JDK-8 为 `host:port`），本侧按 JDK-27 精确断言。
- 其余 Proxy 语义与 JDK-8 逐条一致（同套 252/…共享断言双 JDK 验证）。

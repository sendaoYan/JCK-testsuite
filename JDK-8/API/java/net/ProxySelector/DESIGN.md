# ProxySelector 设计文档

## 一、API 功能

`java.net.ProtocolHandler` 使用的代理选择器 SPI：`abstract List<Proxy> select(URI)`、`abstract void connectFailed(URI, SocketAddress, IOException)`，静态 `getDefault()`/`setDefault(ProxySelector)`。JDK-8 无工厂方法（`of(InetSocketAddress)` 为 JDK-9+ 新增）。

## 二、Java 用户如何使用

- 安装全局策略：继承 ProxySelector 实现两个抽象方法，`ProxySelector.setDefault(sel)`。
- 协议栈对每个 URL 调 `select(uri)` 取代理列表（按序回退）；连接失败调 `connectFailed(uri, sa, ioe)` 反馈（默认实现据此做失败缓存）。
- `setDefault(null)` 清除（getDefault() 变 null，协议栈不自动重装内置选择器）。

## 三、开源代码实际用法调研（参考来源）

OpenJDK jdk8u `sun.net.spi.DefaultProxySelector`（select 对无代理属性环境返回 `[Proxy.NO_PROXY]`；`connectFailed(null,null,null)` 抛 IAE "Arguments can't be null."；`select(null)` IAE "URI can't be null."）；javap 公开面。

## 四、测试用例设计（73 断言）

- **白盒**：abstract、两个抽象方法、getDefault/setDefault static、JDK-8 上 `of` 不存在（反射 NoSuchMethod 断言）。
- **默认选择器**：非 null、未配置代理属性时对 http/https/ftp 均返回单元素 `Proxy.NO_PROXY`（属性守卫防误报）、`select(null)` IAE。
- **子类契约**：RecordingSelector 记录 select/connectFailed 的实参身份（URI/SocketAddress/IOException 均同一引用透传）；返回可变/空/多元素回退列表皆合法且顺序保留。
- **setDefault 往返**：setDefault→getDefault 身份、二次 setDefault 替换、`setDefault(null)`→null、finally 恢复。
- **默认选择器 URI 矩阵**：4 种 URI 形态 DIRECT；connectFailed 仅记账不影响后续 select。
- **异常透明**：子类抛出的异常对象原样传播。

## 五、易错规范细节（探针实证）

- 环境变量 `http_proxy`/`https_proxy` **不影响** Java：Linux 上 JVM 只认系统属性，本机探测确认 select 返回 `[DIRECT]`；测试仍加属性守卫。
- `select(null)`/`connectFailed(null,null,null)` 抛 **IllegalArgumentException**（消息 "URI can't be null." / "Arguments can't be null."）——不是 NPE。
- JDK-8 默认选择器返回**可变** ArrayList（JDK-27 起 immutable，见差异）。
- 不做任何真实网络连接：select 全部用 127.0.0.1/localhost 字面 URI。

## 六、版本差异（JDK-8 vs JDK-27）

| 行为 | JDK-8 | JDK-27 |
|---|---|---|
| `ProxySelector.of(InetSocketAddress)` | 无 | 有（of(null)→[DIRECT]，of(addr)→[HTTP @ addr]，列表 immutable） |
| 默认 select 返回列表 | 可变 | immutable（add 抛 UnsupportedOperationException） |
| `select(null)` 消息 | "URI can't be null." | 相同（两侧一致） |

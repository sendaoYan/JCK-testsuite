# ProxySelector 设计文档

## 一、API 功能

抽象类，根据目标 URI 选择代理列表。

## 二、Java 用户如何使用

用户实现 select(URI)/connectFailed 并通过 ProxySelector.setDefault 注册全局选择器。

## 三、开源代码实际用法调研

JDK 内建 sun.net.spi.DefaultProxySelector 默认实现；框架自定义代理路由。

## 四、测试用例设计

getDefault/setDefault 往返；匿名子类实现 select 返回列表、connectFailed 被调用。

## 五、缺口分析（批次 H 深化追加）

原测试仅 19 断言。深化至 **99 断言**（jtreg Passed），新增覆盖：

- `of()` 工厂矩阵：of(null)→单元素 NO_PROXY 身份、of(addr) http/https 同一 HTTP 代理、of(unresolved) 保持 unresolved、of() 返回列表 **immutable**（add 抛 UnsupportedOperationException）、of(null).connectFailed no-op、of(addr).connectFailed(null,null,null) IAE。
- 默认选择器：无代理属性时 http/https/ftp 均 `[Proxy.NO_PROXY]`（属性守卫）、默认列表 **immutable（JDK-27）**、`select(null)` IAE 精确消息 `"URI can't be null."`、connectFailed 仅记账。
- 子类契约：select/connectFailed 实参身份透传（URI/SocketAddress/IOException 同引用）、可变/空/多元素回退列表、顺序保留。
- setDefault 往返：二次替换、setDefault(null)→null（不自动重装内置）、恢复。
- 异常透明：子类异常对象原样传播。

## 六、版本差异（JDK-27 实证）

- `of(InetSocketAddress)` 为 JDK-9+ 新增（JDK-8 无，反射验证）。
- 默认 select 返回列表：JDK-8 可变 / JDK-27 immutable。
- `select(null)` 消息两侧一致（"URI can't be null."）。

# URLStreamHandler 设计文档

## 一、API 功能

URL 协议处理器抽象类，负责协议相关 URL 解析(parseURL)与打开连接(openConnection)，供 URL 类回调。

## 二、Java 用户如何使用

用户自定义协议时继承 URLStreamHandler 并实现 openConnection，配合 URLStreamHandlerFactory 或带 handler 的 URL 构造器。

## 三、开源代码实际用法调研

JDK 内建 http/https/ftp/file/jar 处理器均派生自此；自定义 schema 的框架亦常实现。

## 四、测试用例设计

子类暴露 openConnection/getDefaultPort；默认 getDefaultPort 返回 -1；用带 handler 的 URL 构造验证 parseURL 拆分 protocol/host/port/path。

---

## 批次 C 深化（URLConnection 簇）

> 原测试 ~10 断言。本批补齐：默认 parseURL 全组件分解、相对/绝对/网络路径/仅查询/仅片段
> 上下文解析、toExternalForm 重建、字面量 IP 的 equals/hashCode/sameFile/hostsEqual（避免
> DNS）、getHostAddress、getDefaultPort 默认/覆写、openConnection(URL,Proxy) 默认 UOE、
> setURL 8/5 参（协议不可重置！）、protected 成员可见性矩阵。

## 1. API 功能

协议处理器抽象基类。protected 方法族：openConnection(URL)/(URL,Proxy)（后者默认抛
UnsupportedOperationException("Method not implemented.")）、parseURL/setURL（**setURL 明确
忽略 protocol 参数**——源码注释 "ensure that no one can reset the protocol on a given URL"）、
getDefaultPort（默认 -1）、equals/hashCode/sameFile（ref 参与 equals、不参与 sameFile；
hostsEqual 经 InetAddress 比较——**会触发 DNS，测试只用字面量 IP**）、toExternalForm、
getHostAddress（host 为空串时 null）。

## 2. 使用方式

new URL(null, spec, handler) 显式注入 handler 触发默认 parseURL；工厂
（URLStreamHandlerFactory）按协议提供 handler；子类覆写 getDefaultPort 后 URL.getDefaultPort()
生效。

## 3. 参考来源

- Oracle javadoc；OpenJDK URLStreamHandler.java / URL.java（setURL 的协议保护）。
- 探针 ProbeC.java H/J 节 + ProbeC9/C10（相对形式与空 host）。

## 4. 测试用例设计（映射到 URLStreamHandlerTest.java）

| 测试方法 | 覆盖点 |
|---|---|
| testWhiteBox | 15 个 protected 成员逐一可见性/抽象性核对 |
| testDefaultParseURL | `myproto://u:p@h.example:81/a/b?x=1#f` 全 9 组件、无端口 -1、getDefaultPort 默认 -1、覆写 99 经 URL.getDefaultPort() 生效 |
| testContextResolution | `../d?y=2#new` → /a/d、同目录、绝对路径、绝对 spec 换协议/host、`//alt/q` 网络路径保协议、仅查询 `?z=9` → path 补斜杠 `/a/b/`（双 JDK 实测特例）、仅片段保 path/query 换 ref |
| testToExternalForm | 全组件重建、URL.toExternalForm 委托、空 host 形式（`myproto:/path`，getHost()==""）、无 ref/query |
| testEquality | 127.0.0.1 字面量：equals/hashCode 一致性、path/port/host/ref 任一不同 → 不等、ref 不参与 sameFile、hostsEqual 同/异、getHostAddress 字面量/空 host→null |
| testOpenConnection | 子类返回值透传（null 与哨兵）、openConnection(URL,Proxy) 默认 UOE |
| testSetURL | 8 参/5 参（废弃）改 host/port/file/ref/userInfo/path/query；**协议参数被忽略**（保持 myproto）——源码级契约 |
| testBlackBox（保留） | 原有黑盒冒烟 |

易错规范细节：
- **setURL 两个重载都不改 protocol**（"ensure that no one can reset the protocol"）。
- equals/hashCode/getHostAddress/hostsEqual 对非字面量 host 会做 **DNS 解析**——网络纪律要求测试一律用字面量 IP。
- 空 host（`myproto:///path`）的 getHost() 返回 **""** 而非 null；getHostAddress 返回 null。
- 仅查询相对 spec（`?z=9`）解析后 path 为 `/a/b/`（目录加斜杠），仅片段 spec 保留原 path+query。
（无双侧行为差异；仅 JDK-27 侧 URL 构造器为 deprecated 警告，不影响断言。）
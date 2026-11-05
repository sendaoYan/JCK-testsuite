# org.ietf.jgss.GSSContext 测试设计

## 1. API 功能
GSSContext 封装 GSS-API 安全上下文（接口）。因建立上下文需要真实 Kerberos 认证，本用例只做接口形态（白盒）+ 常量值（黑盒），不发起认证、不联网。
- 常量：`DEFAULT_LIFETIME=0`、`INDEFINITE_LIFETIME=Integer.MAX_VALUE`。
- 方法族：建立（`initSecContext`/`acceptSecContext`）、状态（`isEstablished`/`getLifetime`/`getSrcName`/`getTargName`/`getMech`/`isInitiator`/`isTransferable`/`isProtReady`）、消息保护（`wrap`/`unwrap`/`getMIC`/`verifyMIC`/`getWrapSizeLimit`）、选项请求与查询（`requestMutualAuth` 等）、`dispose`/`export`/`setChannelBinding`/`getDelegCred`。

## 2. Java 用户如何使用
真实用法（JDK test/jdk 模式）：
```java
GSSManager m = GSSManager.getInstance();
GSSContext ctx = m.createContext(peerName, mech, cred, GSSContext.DEFAULT_LIFETIME);
byte[] token = ctx.initSecContext(new byte[0], 0, 0);
if (ctx.isEstablished()) { ... }
ctx.dispose();
```
本用例不建立真实上下文，仅断言接口常量与方法形态。

## 3. 开源代码实际用法调研
- `test/jdk/sun/security/krb5/auto/Context.java`：`me.x = m.createContext(..., GSSContext.DEFAULT_LIFETIME)`，随后 `me.x.wrap(input, 0, input.length, p1)`、`me.x.unwrap(...)`、`me.x.getMIC(...)`。
- JDK 源码 `org/ietf/jgss/GSSContext.java`：接口定义与常量声明。
- JDK 源码 `sun/security/jgss/GSSContextImpl.java`：默认实现。
（来源：`~/git/jdk25u-dev`。）

## 4. 测试用例设计 + 自动化实现
白盒（`GSSContextTest.testWhiteBox()`）：public interface、无父接口、两个 `public static final int` 字段、34 个方法名齐全。
黑盒（`testBlackBox()`）：断言 `DEFAULT_LIFETIME==0`、`INDEFINITE_LIFETIME==Integer.MAX_VALUE`、两常量互异。

## 5. 深化补充（本轮契约深化）
新增：DEFAULT/INDEFINITE 生命周期哨兵精确值、request* 全族签名、未建立上下文状态位
（isEstablished/isProtReady false）、setChannelBinding/getChannelBinding 往返、
未建立即 wrap/getMIC → GSSException(NO_CONTEXT) 负例。

# org.ietf.jgss.GSSContext 测试设计（JDK-8 侧）

## 1. API 功能
接口：安全上下文。
- 生命周期哨兵：`DEFAULT_LIFETIME(0)`、`INDEFINITE_LIFETIME(Integer.MAX_VALUE)`。
- 状态方法：`isEstablished/isTransferable/getSrcName/getTargName/getLifetime/` `getMech/isProtReady/getConfState`；
- 配置方法：`requestMutualAuth/requestReplayDet/requestSequenceDet/requestCredDeleg/
  requestAnonymity/requestConf/requestInteg/requestLifetime/requestChannelBinding` + `setChannelBinding`。

## 2. Java 用户如何使用
```java
GSSContext ctx = mgr.createContext(peer, mech, cred, GSSContext.DEFAULT_LIFETIME);
ctx.requestMutualAuth(true);
byte[] tok = ctx.initSecContext(new byte[0], 0, 0);   // 循环到达 isEstablished()
```
## 3. 开源代码实际用法调研
- JDK-8 源码 `jdk/src/share/classes/org/ietf/jgss/*.java`（本仓 `~/git/jdk8u-dev`）：常量定义、构造器校验与 `GSSException` 错误码映射的权威实现。
- JDK 测试 `test/sun/security/krb5/`：以 `Oid`/`GSSManager` 离线构造机制对象与名称，是本包的主要真实用户。
- 本环境离线（无 KDC），黑盒用例只覆盖离线可达路径，在线路径以白盒形态锁为主。
## 4. 测试用例设计 + 自动化实现
- 白盒：接口面 + 两个生命周期哨兵精确值 + 全部 request*/setChannelBinding 签名。
- 黑盒：未建立上下文的 `isEstablished()==false`；`isProtReady()==false`；
  通道绑定 setter 与 getter 的往返；wrap/MIC 未建立即用 → GSSException(NO_CONTEXT) 的负例通道。
- 实现见 `GSSContextTest.java`。

# org.ietf.jgss.GSSCredential 测试设计（JDK-8 侧）

## 1. API 功能
接口：机制凭据的抽象。
- 用途哨兵：`INITIATE_AND_ACCEPT(0)/INITIATE_ONLY(1)/ACCEPT_ONLY(2)`。
- 生命周期哨兵：`INDEFINITE_LIFETIME`（Integer.MAX_VALUE）、`DEFAULT_LIFETIME`(0)。
- 方法：`getRemainingLifetime/getRemainingInitLifetime/getRemainingAcceptLifetime/getUsage/
  getUsage(Oid)/add(GSSName,int,Oid,int,int)/getMechs()`。

## 2. Java 用户如何使用
```java
GSSCredential c = mgr.createCredential(GSSCredential.INITIATE_ONLY);
if (c.getUsage() == GSSCredential.INITIATE_ONLY) { ctx = mgr.createContext(peer, mech, c, 0); }
```
## 3. 开源代码实际用法调研
- JDK-8 源码 `jdk/src/share/classes/org/ietf/jgss/*.java`（本仓 `~/git/jdk8u-dev`）：常量定义、构造器校验与 `GSSException` 错误码映射的权威实现。
- JDK 测试 `test/sun/security/krb5/`：以 `Oid`/`GSSManager` 离线构造机制对象与名称，是本包的主要真实用户。
- 本环境离线（无 KDC），黑盒用例只覆盖离线可达路径，在线路径以白盒形态锁为主。
## 4. 测试用例设计 + 自动化实现
- 白盒：接口面 + 用途/生命周期常量哨兵精确值锁定（0/1/2、0 与 Integer.MAX_VALUE）。
- 黑盒：离线创建的默认凭据 usage 为 INITIATE_AND_ACCEPT 或 INITIATE_ONLY（实现观察值锁定）、
  剩余生命周期为哨兵值之一；getMechs 离线非空。
- 实现见 `GSSCredentialTest.java`。

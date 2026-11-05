# org.ietf.jgss.GSSException 测试设计（JDK-8 侧）

## 1. API 功能
GSS-API 统一异常：major 码（本包常量表）+ minor 码（机制私有）。
- 构造器：`GSSException(int major)`、`GSSException(int major, int minor, String msg)`。
- 读取：`getMajor/getMajorMessage/getMinor/getMinorString`；`toString`/`getMessage` 拼接两通道信息。

## 2. Java 用户如何使用
```java
try { ctx.init(...); }
catch (GSSException e) {
    if (e.getMajor() == GSSException.BAD_MECH) { ... }
    log(e.getMajorString() + "/" + e.getMinorString());
}
```
## 3. 开源代码实际用法调研
- JDK-8 源码 `jdk/src/share/classes/org/ietf/jgss/*.java`（本仓 `~/git/jdk8u-dev`）：常量定义、构造器校验与 `GSSException` 错误码映射的权威实现。
- JDK 测试 `test/sun/security/krb5/`：以 `Oid`/`GSSManager` 离线构造机制对象与名称，是本包的主要真实用户。
- 本环境离线（无 KDC），黑盒用例只覆盖离线可达路径，在线路径以白盒形态锁为主。
## 4. 测试用例设计 + 自动化实现
- 白盒：public 类、extends Exception、两构造器 + 六方法；常量表（BAD_MECH/ BAD_NAME/ BAD_NAMETYPE/
  BAD_BINDINGS/ BAD_STATUS/ BAD_MIC/ CONTEXT_EXPIRED/ CREDENTIALS_EXPIRED/ NO_CRED/ DEFECTIVE_CREDENTIAL/
  DEFECTIVE_TOKEN/ FAILURE/ INVALID_TOKEN/ INVALID_CONTEXT/ NO_CONTEXT/ BAD_QOP/ NAME_NOT_MN/
  DUPLICATE_TOKEN/ OLD_TOKEN/ UNSEQ_TOKEN/ GAP_TOKEN 等）逐一与 JDK-8 精确值锁定。
- 黑盒：major 通道精确 message；minor 默认 0/null；自定义 msg 覆盖 majorMessage 参与getMessage；
  未知 major 码的消息降级；toString 含 major/minor 两段。
- 实现见 `GSSExceptionTest.java`。

# Authenticator 设计文档

## 一、API 功能

`java.net.Authenticator` 抽象类：认证回调 SPI。静态入口 `requestPasswordAuthentication(...)`（JDK-8 有 5/6/8 参三个重载）把请求上下文写入实例再回调 protected `getPasswordAuthentication()`；8 个 protected final/protected getter 在回调内读取上下文；`setDefault` 安装全局实例。嵌套枚举 `RequestorType`（PROXY/SERVER），另见 `Authenticator/RequestorType/DESIGN.md`。

## 二、Java 用户如何使用

- 继承并覆写 `getPasswordAuthentication()`，按 getRequestingHost/Port/Protocol/Prompt/Scheme/Site/URL/RequestorType 决定凭据，返回 `PasswordAuthentication` 或 null（放弃）。
- `Authenticator.setDefault(auth)` 全局安装一次；协议栈遇 401/407/认证挑战时自动回调。

## 三、开源代码实际用法调研（参考来源）

OpenJDK jdk8u `java.net.Authenticator` 源码（`theAuthenticator` 静态字段、synchronized setDefault、reset() 清上下文、getPasswordAuthentication 基类返回 null）；javap 导出面。

## 四、测试用例设计（100 断言）

- **白盒**：abstract、无抽象方法（`new Authenticator(){}` 合法）、5/6/8 参静态重载存在、setDefault synchronized、JDK-8 上 getDefault/requestPasswordAuthenticationInstance/9 参静态/getRequestingKeyStore 均不存在（反射断言）、8 个 getter 存在。
- **上下文传播**：RecordingAuth 记录全部 getter——5 参 host null/rt 默认 SERVER；6 参带 host；8 参 URL+PROXY 透传；静态方法返回回调产生的同一对象。
- **null/异常路径**：无默认→返回 null；回调返回 null→透传；回调抛异常→异常对象原样传播；setDefault(null) 复位后再装可用。
- **粘滞状态**：回调外读 getter 得到最后上下文；从未被调用的实例 host null/port 0。
- **边界传播**：port -1/0/65536/-5、null host/site/prompt/protocol/scheme 均不校验直接可见；5 参调用把 rt 重置 SERVER。
- **结果身份**：回调每次新建则返回最新实例；固定实例则每次同一。

## 五、易错规范细节（探针实证）

- 三个静态重载最终都走同一 `requestPasswordAuthentication` 落到**已安装默认**实例；8 参重载的 URL/RequestorType 为 null 时 getter 返回 null（不抛）。
- 静态调用在**无默认**时立刻返回 null，不抛异常。
- `setDefault(null)` 合法且 idempotent；之后再 setDefault 恢复工作。
- 所有参数（端口越界、负数、null 字符串）不校验——契约只定义回传，不定义校验。
- finally 中必须 `setDefault(null)`，否则污染同 JVM 后续测试。

## 六、版本差异（JDK-8 vs JDK-27）

| API/行为 | JDK-8 | JDK-27 |
|---|---|---|
| `getDefault()` | 无 | 有（初始 null，往返/复位） |
| `requestPasswordAuthenticationInstance`（8 参实例方法） | 无 | 有：直接回调 this、不触碰默认、全上下文传播 |
| 9 参静态 `requestPasswordAuthentication(Authenticator, ...)` | 无 | 有：显式 authenticator 参与者，null→返回 null |
| `getRequestingKeyStore()` | 无 | 也无（JDK 9–24 有，JDK-27 已移除） |

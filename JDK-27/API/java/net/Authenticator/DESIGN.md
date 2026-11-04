# Authenticator 设计文档

## 一、API 功能

抽象类，为需要认证的连接提供用户名/密码凭据。

## 二、Java 用户如何使用

用户继承 Authenticator 重写 getPasswordAuthentication()，用 Authenticator.setDefault 注册，供 URL/HttpURLConnection 认证。

## 三、开源代码实际用法调研

需要 HTTP Basic/代理认证的客户端注册默认认证器。

## 四、测试用例设计

setDefault/getDefault；子类 requestPasswordAuthentication 返回 PasswordAuthentication；getRequestingHost 等上下文。

## 五、缺口分析（批次 H 深化追加）

原测试仅 12 断言。深化至 **121 断言**（jtreg Passed），新增覆盖：

- JDK-9+ 面完整：getDefault 初始 null/往返/复位；`requestPasswordAuthenticationInstance`（实例方法）——直接回调 this、传播 host/port/URL/rt、**不触碰默认**、基类（未覆写）返回 null；9 参静态显式 authenticator（null authenticator→null 结果、不安装默认）；`getRequestingKeyStore` 在 JDK-27 已移除（JDK 9–24 存在，反射断言缺席）。
- 5/6/8 参静态的上下文传播矩阵（RecordingAuth 记录全部 8 getter）；5 参默认 rt=SERVER、8 参 PROXY/SERVER 翻转。
- null 结果/无默认→null/异常对象透传/setDefault(null) 复位重装。
- 边界：port -1/0/65536/-5、null host/site/prompt/protocol/scheme 不校验直接可见。
- 粘滞状态：未调用实例 host null/port 0。
- 结果身份：固定实例 vs 每次新建。

## 六、版本差异（JDK-27 实证）

见上表：getDefault/instance 方法/9 参静态为 JDK-9+；getRequestingKeyStore 在 9–24 存在、27 移除。5/6/8 参上下文语义与 JDK-8 逐条一致。

# Authenticator.RequestorType 设计文档（JDK-27）

## 一、API 功能

`java.net.Authenticator.RequestorType` 枚举认证请求方：`PROXY`（0）、`SERVER`（1）。final public static 嵌套枚举，纯标记。

## 二、Java 用户如何使用

回调内 `getRequestorType()` 区分 407（PROXY）与 401（SERVER）；JDK-9+ 实例方法 `requestPasswordAuthenticationInstance(..., RequestorType)` 显式传入。

## 三、开源代码实际用法调研（参考来源）

OpenJDK `java.net.Authenticator.RequestorType` 源码；javap；Enum 通用契约。

## 四、测试用例设计（38 断言，jtreg Passed）

- 枚举矩阵：values 长度 2/顺序 PROXY,SERVER/name/ordinal/valueOf/EnumSet/values() 新克隆。
- 枚举契约：compareTo、sort、身份 equals、declaringClass、toString=name。
- 负例：valueOf("BOGUS") IAE、valueOf("proxy") IAE、valueOf(null) NPE。
- 白盒：isEnum/superclass/final/public/static、字段反射。

## 五、易错规范细节

- PROXY 在前；5/6 参静态请求把 rt 重置为 SERVER。
- 与 JDK-8 无任何行为差异。

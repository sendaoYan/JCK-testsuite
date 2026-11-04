# Authenticator.RequestorType 设计文档

## 一、API 功能

`java.net.Authenticator.RequestorType` 枚举认证请求方类型：`PROXY`（ordinal 0，代理要求认证）、`SERVER`（1，源服务器要求认证）。final public 嵌套 static 枚举。

## 二、Java 用户如何使用

在 `Authenticator` 子类的 `getPasswordAuthentication()` 里调 `getRequestorType()` 区分 407（PROXY）与 401（SERVER），选择不同凭据；8 参静态请求方法显式传入。

## 三、开源代码实际用法调研（参考来源）

OpenJDK jdk8u `java.net.Authenticator.RequestorType` 源码；javap；Enum 通用契约。

## 四、测试用例设计（40 断言）

- **枚举矩阵**：values 长度 2/顺序 PROXY,SERVER/name/ordinal/valueOf/EnumSet/values() 新克隆。
- **枚举契约**：compareTo、sort、equals 身份、declaringClass、toString=name。
- **负例**：valueOf("BOGUS") IAE 消息含名字；valueOf("proxy") IAE；valueOf(null) NPE。
- **白盒**：isEnum/superclass/final/public/static、字段反射、getEnumConstants。

## 五、易错规范细节

- PROXY 在前（ordinal 0）：JDK-8 的 5/6 参静态重载把 RequestorType 重置为 SERVER 而非 PROXY。
- 纯标记枚举，无字段/方法体。

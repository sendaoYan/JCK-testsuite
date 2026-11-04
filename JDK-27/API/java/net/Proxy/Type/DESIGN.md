# Proxy.Type 设计文档（JDK-27）

## 一、API 功能

`java.net.Proxy.Type` 枚举代理协议类型：`DIRECT`（0）、`HTTP`（1）、`SOCKS`（2）。final public static 嵌套枚举，纯标记。

## 二、Java 用户如何使用

`new Proxy(Proxy.Type.HTTP, addr)`；`proxy.type()` 判定；`valueOf` 解析配置。

## 三、开源代码实际用法调研（参考来源）

OpenJDK `java.net.Proxy.Type` 源码；javap；Enum 通用契约。

## 四、测试用例设计（47 断言，jtreg Passed）

- 枚举矩阵：values 顺序/名称/ordinal/valueOf 三名/EnumSet/values() 新克隆。
- 枚举契约：compareTo 自然序、sort、身份 equals、declaringClass、instanceof Enum。
- 负例：valueOf("BOGUS") IAE 消息含名字、valueOf("direct") IAE、valueOf(null) NPE。
- 白盒：isEnum/superclass Enum/final/public/static、字段反射、getEnumConstants。

## 五、易错规范细节

- values() 返回防御性克隆；valueOf 大小写敏感。
- 与 JDK-8 无任何行为差异。

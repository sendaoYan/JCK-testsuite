# Proxy.Type 设计文档

## 一、API 功能

`java.net.Proxy.Type` 枚举代理协议类型，三个常量：`DIRECT`（ordinal 0）、`HTTP`（1）、`SOCKS`（2）。final、public、嵌套 static。

## 二、Java 用户如何使用

`new Proxy(Proxy.Type.HTTP, addr)` 指定类型；`proxy.type()` 判定；`Proxy.Type.valueOf("SOCKS")` 反序列化/配置解析。

## 三、开源代码实际用法调研（参考来源）

OpenJDK jdk8u `java.net.Proxy.Type` 源码；javap 导出枚举面；Enum 通用契约（compareTo/ordinal/values 克隆）。

## 四、测试用例设计（49 断言）

- **枚举矩阵**：values() 长度/顺序/名称/ordinal、valueOf 三个名字、EnumSet.allOf、`values()` 每次返回新克隆。
- **枚举契约**：compareTo 自然序、Collections.sort、equals 身份、非 String、getDeclaringClass、instanceof Enum。
- **负例**：`valueOf("BOGUS")` IAE 且消息含常量名；`valueOf("direct")` IAE（大小写敏感）；`valueOf(null)` NPE。
- **白盒**：isEnum、superclass=Enum、final、public、字段反射、getEnumConstants、`Enum.valueOf` 泛型静态等价。

## 五、易错规范细节

- `values()` 返回防御性克隆：两次调用数组身份不同，修改副本不影响后续结果。
- `valueOf` 严格大小写敏感；消息形如 `No enum constant java.net.Proxy.Type.BOGUS`。
- 枚举无 body 方法、无字段、无构造器参数——纯标记枚举。

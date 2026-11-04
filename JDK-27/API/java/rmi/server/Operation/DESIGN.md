# java.rmi.server.Operation 行为测试设计

## 1. API 功能
- 构造器：`Operation(String op)`。
- 查询/转换：`getOperation()` 返回方法名；`toString()` 返回方法名。
- 状态：整类 `@Deprecated`（JDK1.1 stub/skeleton 遗留，无替换）。

## 2. Java 用户如何使用
- 历史遗留：JDK1.1 stub/skeleton 用它描述方法；现代 RMI 不再需要。
- `new Operation("echo").getOperation()` 返回 `"echo"`。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.rmi/share/classes/java/rmi/server/Operation.java`：`toString()` 直接返回 `operation` 字段。
- 该类仅存于兼容层，与 `RemoteRef.newCall(RemoteObject, Operation[], int, long)` 配合用于旧式 stub。
- 现代 RMI 已用方法 hash 取代 Operation 数组。

## 4. 测试用例设计 + 自动化实现
- 白盒：public、具体类、父类 Object、`@Deprecated`。
- 黑盒：构造 + `getOperation()`/`toString()` 回显；空串与 null 输入；未重写 equals（身份相等）。
- 实现见 `OperationTest.java`。

# java.rmi.server.Skeleton 行为测试设计

## 1. API 功能
- 接口（`@Deprecated`），方法：`Operation[] getOperations()`、`void dispatch(Remote, RemoteCall, int opnum, long hash)`。
- JDK1.1 skeleton 分派入口；现代 RMI 不用。

## 2. Java 用户如何使用
- 历史遗留：`dispatch` 收到操作号与 hash 后分派到具体方法。
- `getOperations` 返回该对象支持的操作列表。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.rmi/share/classes/java/rmi/server/Skeleton.java`：接口 + `@Deprecated`。
- JDK 测试 `test/jdk/java/rmi/registry/nonLocalRegistry/NonLocalSkeletonTest.java`：旧式 skeleton 场景。
- 与 `Operation`、`RemoteCall`、`SkeletonNotFoundException` 构成 JDK1.1 协议层。

## 4. 测试用例设计 + 自动化实现
- 白盒：public interface、无父接口、`@Deprecated`、`getOperations`/`dispatch` 签名。
- 黑盒：匿名实现；`getOperations` 返回数组；`dispatch` 回调被触发并校验入参（Remote 对象、opnum、hash）。
- 实现见 `SkeletonTest.java`。

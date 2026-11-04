# java.rmi.dgc.DGC 行为测试设计

## 1. API 功能
`DGC` 是分布式垃圾回收的远程接口，`extends Remote`。
- `Lease dirty(ObjID[], long, Lease)`：请求/续租引用。
- `void clean(ObjID[], long, VMID, boolean)`：通知引用不再需要。

## 2. Java 用户如何使用
- 由 RMI 运行时内部实现；应用代码一般不自定义 DGC，仅通过远程对象导出间接使用。

## 3. 开源代码实际用法调研
- 类定义 `src/java.rmi/share/classes/java/rmi/dgc/DGC.java`。
- JDK 测试 `test/jdk/java/rmi/transport/dgcDeadLock/TestImpl.java` 涉及 DGC 引用。
- `sun.rmi.transport.DGCImpl` 实现该接口（`src/java.rmi/share/classes/sun/rmi/transport/DGCImpl.java`）。

## 4. 测试用例设计 + 自动化实现
黑盒部分（DGCTest.testBlackBox）：本地实现类 `instanceof Remote`、`dirty` 返回传入 Lease、`clean` 可调用。
白盒部分（testWhiteBox）：接口、extends Remote、方法 dirty/clean 形态。

# java.rmi.dgc.Lease 行为测试设计

## 1. API 功能
`Lease` 表示一个租约（VMID + 时长），final、Serializable。
- 构造器 `Lease(VMID, long)`（VMID 可为 null）。
- `VMID getVMID()`、`long getValue()`。
- 未覆写 equals/hashCode，按身份比较。

## 2. Java 用户如何使用
- DGC 中 `new Lease(vmid, 600000L)` 表示给某 VM 的 10 分钟租约。

## 3. 开源代码实际用法调研
- 类定义 `src/java.rmi/share/classes/java/rmi/dgc/Lease.java`。
- JDK 测试 `test/jdk/java/rmi/server/Unreferenced/leaseCheckInterval/LeaseCheckInterval.java` 使用 Lease。

## 4. 测试用例设计 + 自动化实现
黑盒部分（LeaseTest.testBlackBox）：getVMID 返回同一实例、getValue；null VMID/0/负值；equals 自反但同 VMID+value 的两 Lease 不相等（身份语义）、非 Lease 不相等；序列化往返保留 value 与 VMID。
白盒部分（testWhiteBox）：final、Serializable、accessors getVMID/getValue。

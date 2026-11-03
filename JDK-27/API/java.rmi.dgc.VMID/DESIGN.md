# java.rmi.dgc.VMID 行为测试设计

## 1. API 功能
`VMID` 是跨 JVM 唯一的标识符，final、Serializable。
- 构造器 `VMID()` 生成唯一实例（随机 host 字节 + `UID`）。
- `static boolean isUnique()`（返回 true，已废弃）。
- `equals/hashCode` 基于 uid 与 addr 字节；`toString()` 为 16 位十六进制 host 段 + ":" + uid。

## 2. Java 用户如何使用
- DGC 用 `new VMID()` 标识客户端 VM。

## 3. 开源代码实际用法调研
- JDK 测试 `test/jdk/java/rmi/dgc/VMID/CheckVMID.java`（bug 4171370）验证 VMID 唯一性（用 SHA 哈希 host，而非 IP）。
- 类定义 `src/java.rmi/share/classes/java/rmi/dgc/VMID.java`。

## 4. 测试用例设计 + 自动化实现
黑盒部分（VMIDTest.testBlackBox）：toString 非空且含 ':'；isUnique() 为 true；hashCode 稳定；equals 自反、两新 VMID 不等、null/非 VMID 不等；两个新 VMID toString 不同；序列化往返后 equals/hashCode/toString 保持一致。
白盒部分（testWhiteBox）：final、Serializable、public 方法 equals/hashCode/isUnique/toString。

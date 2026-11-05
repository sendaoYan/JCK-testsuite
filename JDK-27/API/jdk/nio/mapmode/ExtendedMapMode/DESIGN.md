# jdk.nio.mapmode.ExtendedMapMode 契约设计

## 1. API 功能
`jdk.nio.mapmode.ExtendedMapMode`（模块 jdk.nio.mapmode）承载非易失性内存的映射模式：`READ_ONLY_SYNC`/`READ_WRITE_SYNC`。语义上对应 `FileChannel.map` 的同步映射（写直达持久化介质，绕过页缓存刷写协商），供 NVMe 持久内存上的堆外数据结构使用。

## 2. Java 用户如何使用
`FileChannel.map(ExtendedMapMode.READ_WRITE_SYNC, offset, size)`。使用前需保证平台支持（Linux `MAP_SYNC`），否则抛 UnsupportedOperationException；映射本身仍是 MappedByteBuffer，读取语义与普通只读/读写映射一致。

## 3. 开源代码实际用法调研
- OpenJDK jdk.nio.mapmode 自带单文件实现；Project Lana 的 PmemBasicIO 教程直接引用这两个常量。
- Intel pmem-java 绑定（已归档）采用同构常量对，社区后续统一收敛到本 API。
- lombok/graal 均不涉及；实际消费者是数据库与日志引擎（如早期 Kronos/NVMe 论文原型）的 fsync-free 写路径。

## 4. 测试用例设计+自动化实现
- 常量身份：两个常量互不相同、均非 FileChannel.MapMode 标准三模式（含 PRIVATE）、equals 语义退化为恒等。
- 类型面：均为 `java.nio.channels.FileChannel.MapMode` 实例（不是新子类），toString 精确等于常量名（READ_ONLY_SYNC / READ_WRITE_SYNC）。
- 自动化：jtreg `run main` + `check()`，无文件系统副作用（不真做 map，避免平台 MAP_SYNC 差异）。

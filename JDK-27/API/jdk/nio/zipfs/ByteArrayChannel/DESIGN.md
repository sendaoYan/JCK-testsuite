# jdk.nio.zipfs.ByteArrayChannel 契约设计

## 1. API 功能
包私有 `ByteArrayChannel implements SeekableByteChannel`，是 zipfs 内所有写路径的落盘缓冲：entry 数据先在内存里按 `ByteArray` 增长，close 时整体并入 zip 中央目录。两个包私有构造器 `(int initialCapacity, boolean readOnly)` 与 `(byte[] buf, boolean readOnly)`；`toByteArray()` 暴露快照。

## 2. Java 用户如何使用
外部代码不可直接使用；它实现完整 SeekableByteChannel 契约：write 推进 size 与 position、read 消费 position、position(long) 可回读、isOpen/close 驱动 ClosedChannelException，readonly 实例 write 抛 NonWritableChannelException。truncate 恒 UnsupportedOperationException（内存缓冲不支持收缩）。

## 3. 开源代码实际用法调研
- OpenJDK jdk.zipfs：ZipFileSystem.getOutputStream 对每个 entry new 一个 ByteArrayChannel，close 时写 entry 头 + 缓冲数据。
- XZ for Java、commons-compress 的内存 SeekableByteChannel（InMemorySeekableByteChannel）语义同构：grow-on-write、position 双语义（读指针=写指针）。

## 4. 测试用例设计+自动化实现
- 反射 `getDeclaredConstructor(int.class, boolean.class)` + setAccessible（@modules 带 `jdk.zipfs/jdk.nio.zipfs:+open`），实例化后驱动完整通道契约：空通道 size/position 为 0 → write 3 字节 size=position=3 → 回零 read 校验内容与再推进 → position(2) → truncate UOE → toByteArray 快照 → close 后 read 抛 ClosedChannelException。
- readonly 分支：wrap 构造器 + true，write 抛 NonWritableChannelException、read 返回被包裹内容、size 为缓冲长度。
- 自动化：jtreg `run main` + `check()`；全部确定性，无时钟/随机。

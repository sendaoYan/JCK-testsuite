# java.nio.channels.FileChannel.MapMode 设计文档（JDK-8）

## 1. API 功能

文件映射模式常量类型：READ_ONLY / READ_WRITE / PRIVATE 三个公共静态常量，供 FileChannel.map 使用。

（签名以 jdk8u372 javap -public 实测为准。）

## 2. Java 用户如何使用

- 常规用法：`FileChannel.map(MapMode.READ_WRITE, 0, len)` 得到 MappedByteBuffer；READ_ONLY 映射只读文件，
  READ_WRITE 写透文件，PRIVATE 写时复制（改动不落盘）。
- 组合用法：映射 + MappedByteBuffer.force() 持久化；MMAP 感的存储引擎（RocksDB/LMDB 类）经此入口。
- 失败路径：非读通道 READ_ONLY 映射 → NonWritableChannelException；非写通道 READ_WRITE/PRIVATE 映射 →
  NonWritableChannelException；越界 → IOException（实测 JDK-8 侧 READ_ONLY 越界即抛）。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- 常量枚举式入参：与 StandardOpenOption 一样作"能力开关"传递；
- 用法特点：几乎只经 FileChannel.map 间接触达，用户代码仅选择常量、不实例化（构造器私有）；
  测试侧以临时文件 + finally 清理保证无残留。

## 4. 测试用例设计 + 自动化实现

文件：`MapModeTest.java`（单文件，含 testMapMode() 基线与 A5 新增 testWhiteBox()/testBlackBox()，由 main 依次调用）。

设计：testMapMode 基线保留；A5 白盒校验二进制名/继承 Object/非枚举/恰 3 个公共常量/私有构造器
MapMode(String)/toString 有声明；黑盒校验三个常量 toString 精确值（"READ-ONLY"/"READ-WRITE"/"PRIVATE"）、
equals/hashCode 恒等语义（同一常量同一实例）、三种模式映射行为（PRIVATE 写不落盘、READ_ONLY 映射只读）。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 映射类用例全部 `Files.createTempDirectory` + finally 删除；PRIVATE 断言经文件字节数/内容核对。
- headless 安全，不加 @key；JDK-8 侧断言数：6→36 条。

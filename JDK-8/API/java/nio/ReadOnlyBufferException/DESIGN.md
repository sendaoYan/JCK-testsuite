# java.nio.ReadOnlyBufferException 测试方案

## 1. API 功能
- extends UnsupportedOperationException, no-arg 构造器, Serializable; 消息为 null

## 2. Java 用户如何使用
- 相对 get/put 越界 (Overflow/Underflow)、reset 无 mark (InvalidMark)、只读缓冲写 (ReadOnly) 时抛出

## 3. 开源代码实际用法调研
- jdk8u-dev: 这些异常继承自 UnsupportedOperationException, 仅默认构造器

## 4. 测试用例设计 + 自动化实现
- 白盒: 父类/构造器/Serializable/消息 null; 黑盒: 真实触发场景 + 失败后缓冲状态可观察(position 不变/数据未写)。实现于 ReadOnlyBufferExceptionTest.java

健壮性: 无墙钟/随机/哈希顺序/全局状态/文件/网络/线程依赖 (MappedByteBuffer 除外, 用临时文件
并在 finally 清理); 字节序断言显式 order(...) 固定布局不依赖平台 nativeOrder; 中文/emoji 用
Unicode 字面量固定; 固定输入跨平台一致.

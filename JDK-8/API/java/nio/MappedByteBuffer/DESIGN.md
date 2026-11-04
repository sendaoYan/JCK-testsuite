# java.nio.MappedByteBuffer 测试方案

## 1. API 功能
- extends ByteBuffer; force()/load()/isLoaded(); 经 FileChannel.map(MapMode,offset,size) 取得

## 2. Java 用户如何使用
- READ_ONLY/READ_WRITE/PRIVATE 三种映射; force() 刷写; slice/duplicate 保持映射类型

## 3. 开源代码实际用法调研
- jdk8u-dev MappedByteBuffer: force() 无参 (JDK-9+ 才加 force(int,int)); MapMode 含 PRIVATE

## 4. 测试用例设计 + 自动化实现
- 白盒: abstract/extends ByteBuffer/force+load+isLoaded 存在; 黑盒: 三映射读写与持久化、只读映射 put 抛 ReadOnlyBufferException、零长/偏移映射、RandomAccessFile 旧入口; 临时目录 finally 清理。实现于 MappedByteBufferTest.java

健壮性: 无墙钟/随机/哈希顺序/全局状态/文件/网络/线程依赖 (MappedByteBuffer 除外, 用临时文件
并在 finally 清理); 字节序断言显式 order(...) 固定布局不依赖平台 nativeOrder; 中文/emoji 用
Unicode 字面量固定; 固定输入跨平台一致.

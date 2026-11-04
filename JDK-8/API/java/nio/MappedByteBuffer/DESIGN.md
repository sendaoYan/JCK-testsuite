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


### A1 批次深化（追加）
- 映射缓冲区属性: isDirect=true/hasArray=false/array()/arrayOffset() 抛 UOE、load() 返回 this、
  isLoaded() 不抛、ro force() 返回 this 不抛、asReadOnlyBuffer 只读。
- map() 中立性: 不改变 channel.position; slice 仍为 direct 且仍是 MappedByteBuffer; 相对 get 游标推进。
- 参数矩阵(双侧实测一致): map(-1,8)/map(0,-1)/map(0,MAX+1) IAE、map(MAX+1,1) 与越 EOF 的
  map(READ_ONLY,250,10) 在只读通道上抛 IOException、关闭通道 ClosedChannelException、
  只读通道申请 READ_WRITE NonWritableChannelException。
- READ_WRITE 写回: put + force() 后以 Files.readAllBytes 对比验证落盘。
- PRIVATE fork: 自见写/兄弟映射不可见/文件不变; 与 heap ByteBuffer 同内容 equals; ro compact ROBE。
- JDK-27 独有(JDK-13+): force(int,int) 返回 this、(-1,4)/(0,-1)/(13,4)/(17,0) IOOBE、
  (16,0)/(0,16) 边界 OK、写回落盘验证、ro force(range) 不抛; JDK-8 侧反射记录 force(int,int) 缺失。
- 新增用例的临时文件均 finally 清理。断言数 20 -> 46 (JDK-8) / 21 -> 56 (JDK-27)。

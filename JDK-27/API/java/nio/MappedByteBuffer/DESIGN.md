# java.nio.MappedByteBuffer 设计文档

## 1. API 功能

内存映射缓冲区：force/load/isLoaded 与 position/limit 语义。

（签名以 JDK-27 javap 实测为准。）

## 2. Java 用户如何使用

- 常规用法：直接面向 `MappedByteBuffer` 编程，或经 Files/Channels/Selector 等门面 API 间接触达。
- 组合用法：与本包/相关包其它类型配合（如视图+Files.getAttribute、通道+Selector、选项+Files.move）。
- 生命周期：打开/获取 -> 使用 -> 关闭/释放；AutoCloseable 资源用 try-with-resources。
- 失败路径：通过本类型或配套异常类型精确报告失败。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- 大文件零拷贝（Kafka 索引、LMDB）
- 用法特点：优先走 java.nio 高层门面（Files/Channels），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`MappedByteBufferTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验方法面（含 force(int,int)）；黑盒验证 READ_ONLY 拒写、READ_WRITE 写后 force 落盘重读一致、PRIVATE 改动不落盘、零长/偏移映射、slice/duplicate 保持类型、RandomAccessFile 旧入口。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除；套接字/异步用例仅用
  本地回环地址与临时端口（ServerSocketChannel.open().bind(null)），守护线程 + join/超时等待。
- 平台相关能力（posix/dos/user/acl 视图）以 `supportedFileAttributeViews()`/supportsFileAttributeView
  守护，不支持时打印 NOTE 并跳过行为部分，不判失败；headless 安全，不加 @key。
- 本机实测断言数：22 条。


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

# java.nio.Buffer 测试方案

## 1. API 功能
- 游标查询: capacity()/position()/limit()/remaining()/hasRemaining()
- 游标设置: position(int)/limit(int); 抽象属性: isReadOnly()/hasArray()/array()/arrayOffset()/isDirect()
- 状态机: mark()/reset()/clear()/flip()/rewind(); 不变量 mark<=position<=limit<=capacity

## 2. Java 用户如何使用
- Buffer 抽象, 用户经 ByteBuffer.allocate(n) 等子类工厂取得实例
- 组合链路: allocate->put(相对)->flip()->get(相对)->clear()/rewind()
- mark 记下当前 position, reset() 回到该点; position/limit 调到小于 mark 时 mark 丢弃

## 3. 开源代码实际用法调研
- jdk8u-dev src/share/classes/java/nio/Buffer.java: mark=-1 表示未设, flip/clear/rewind 丢 mark, limit 小于 position 时钳制

## 4. 测试用例设计 + 自动化实现
- 白盒: 抽象类/游标方法 final/抽象属性存在; 黑盒: 四指针不变量、position/limit 合法+非法边界(负/>limit/>capacity)、mark/reset 与 mark 丢弃规则(位置/limit/flip/rewind/clear)、remaining 钳制、fluent 链式返回 this。实现于 BufferTest.java

健壮性: 无墙钟/随机/哈希顺序/全局状态/文件/网络/线程依赖 (MappedByteBuffer 除外, 用临时文件
并在 finally 清理); 字节序断言显式 order(...) 固定布局不依赖平台 nativeOrder; 中文/emoji 用
Unicode 字面量固定; 固定输入跨平台一致.

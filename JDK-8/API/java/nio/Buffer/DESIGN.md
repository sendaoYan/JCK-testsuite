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


### A1 批次深化（追加）
- 新增 state() 不变量检查助手 + 表驱动游标状态机 runCursorMachine(): 同一张 19 步状态表
  (填充→mark/reset→position/limit 边界移动→flip→读取→rewind→compact→clear + 3 条 mark 丢弃规则)
  在 4 种缓冲区上原样执行: heap ByteBuffer / direct ByteBuffer / CharBuffer / IntBuffer,
  每步重验 0<=mark<=position<=limit<=capacity 与 remaining()/hasRemaining() (游标契约多态一致性)。
- testPositionLimitBoundaries(): position/limit 全部合法值枚举 + 全部非法值(负、limit+1、capacity+1)、
  limit<position 钳制且丢弃 mark、limit==mark 保留 mark、mark<新limit<position 保留 mark、
  position==mark 保留 / position<mark 丢弃、零容量与满排空退化几何。
- 版本差异(反射记录): JDK-8 Buffer 无 slice()/duplicate()、游标方法 final、
  ByteBuffer.position(int)/flip() 返回 Buffer(无协变覆盖); JDK-9+ 起 Buffer 声明
  slice()/slice(int,int)/duplicate()、游标方法去 final、ByteBuffer 协变返回自身类型(JDK-27 侧逐一断言)。
- 断言数 40 -> 151 (JDK-8) / 54 -> 158 (JDK-27)。

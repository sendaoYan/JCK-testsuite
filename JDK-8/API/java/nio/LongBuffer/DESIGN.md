# java.nio.LongBuffer 测试方案

## 1. API 功能
- 工厂: allocate(int)/wrap(long[])/wrap(long[],int,int); 读写相对/绝对 get()/get(int)/put(long)/put(int,long) 与批量
- 复制/切片/压缩/只读: duplicate()/slice()/compact()/asReadOnlyBuffer(); 比较 equals/compareTo/hashCode

## 2. Java 用户如何使用
- wrap(long[]) 共享底层数组; 组合链路: wrap->put(绝对)->get(相对)->mark/reset->flip->get; duplicate/slice 独立游标共享存储; compact 压缩未读

## 3. 开源代码实际用法调研
- jdk8u-dev LongBuffer: 堆缓冲 hasArray true; equals/compareTo 按 remaining 逐元素 (Float/Double 用 Float.compare/Double.compare 语义: -0.0==0.0, NaN 最大)

## 4. 测试用例设计 + 自动化实现
- 白盒: 抽象/extends Buffer/Comparable/工厂存在; 黑盒: 相对/绝对/批量读写、wrap 范围与共享、duplicate/slice/compact、equals/compareTo、只读 put、异常矩阵、allocate(0)/(-1)、mark/reset。实现于 LongBufferTest.java

健壮性: 无墙钟/随机/哈希顺序/全局状态/文件/网络/线程依赖 (MappedByteBuffer 除外, 用临时文件
并在 finally 清理); 字节序断言显式 order(...) 固定布局不依赖平台 nativeOrder; 中文/emoji 用
Unicode 字面量固定; 固定输入跨平台一致.

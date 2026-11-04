# java.nio.CharBuffer 测试方案

## 1. API 功能
- 工厂: allocate(int)/wrap(char[])/wrap(char[],int,int)/wrap(CharSequence)/wrap(CharSequence,int,int)
- 读写: get()/get(int)/put(char)/put(int,char)/get(char[],..)/put(char[],..)/put(String)/put(String,int,int)
- CharSequence/Appendable/Readable: length()/charAt(int)/subSequence(int,int)/toString()/append(char)/append(CharSequence)/append(CharSequence,int,int)/read(CharBuffer)
- 复制/切片/压缩/只读/比较: duplicate()/slice()/compact()/asReadOnlyBuffer()/equals/compareTo/hashCode

## 2. Java 用户如何使用
- wrap(CharSequence) 返回只读 StringCharBuffer (无底层数组 hasArray false)
- 组合链路: wrap(String)->append/put->flip()->charAt/subSequence/toString; read(CharBuffer) 抽字符

## 3. 开源代码实际用法调研
- jdk8u-dev CharBuffer: wrap(CharSequence) 建 StringCharBuffer (hb==null, hasArray false); append(null) 追加 "null"; put(String,start,end) 为 [start,end) 区间

## 4. 测试用例设计 + 自动化实现
- 白盒: 抽象/extends Buffer/实现 Comparable+Appendable+CharSequence+Readable; 黑盒: CharSequence 面(length/charAt/subSequence/toString)、append 链、read 部分拷贝、put(String) 区间、视图共享、只读(String 与 char[] 两态 array())、异常矩阵。实现于 CharBufferTest.java

健壮性: 无墙钟/随机/哈希顺序/全局状态/文件/网络/线程依赖 (MappedByteBuffer 除外, 用临时文件
并在 finally 清理); 字节序断言显式 order(...) 固定布局不依赖平台 nativeOrder; 中文/emoji 用
Unicode 字面量固定; 固定输入跨平台一致.

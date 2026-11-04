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


### A1 批次深化（追加）
- read(CharBuffer) 语义: 读到 src 满或 Sequence 尾返回实际数、EOF 返回 -1、只读目标 ROBE。
- 批量 put 矩阵: put(String)/put(char[])/put(CharBuffer)/put(String,int,int)——注意
  put(String,start,end) 是 [start,end) 下标语义而非 (offset,length)(双侧实测一致)。
- CharSequence 契约: charAt/length/subSequence 相对 position、toString 为剩余区域、
  subSequence 返回 position=pos+start/limit=pos+end 的共享数组活视图、append 返回 this 且取
  [start,end) 子序列、wrap(csq,start,end) 越界矩阵(IOOBE)。
- order 语义: heap 分配/包装 CharBuffer 的 order == nativeOrder(x86_64 为 LITTLE_ENDIAN,
  jdk8u X-Buffer 模板注释佐证), byte buffer 视图继承创建者显式 order。
- 视图/compact: slice/duplicate/asReadOnly 独立游标+共享内容+只读矩阵、compact 搬移语义;
  equals 只看剩余区域、compareTo 按 char 值字典序(仅断符号, 不断言具体差值)。
- JDK-27 独有: mismatch(相等-1/差异下标/前缀/相对 position); JDK-8 侧反射记录缺失。
- 断言数 81 -> 152 (JDK-8) / 67 -> 144 (JDK-27)。

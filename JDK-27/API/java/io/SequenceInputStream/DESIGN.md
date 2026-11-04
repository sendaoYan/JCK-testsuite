# java.io.SequenceInputStream 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 构造：`(InputStream s1, InputStream s2)`、`(Enumeration<? extends InputStream> e)`。
- `read()`（当前流耗尽自动切换下一流）、`read(byte[],int,int)`（**单次调用不跨流边界**）、`available()`（仅当前流）、`close()`（关闭所有参与流）、`transferTo`。

## 2. Java 用户如何使用
- 串联多个输入（文件分卷、头+体拼接）当作单流读；Enumeration 构造配 Vector/Collections.enumeration。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- 两构造器与切换实现 — `jdk25u-dev/src/java.base/share/classes/java/io/SequenceInputStream.java`。
- bulk read 停在流边界（`n = in.read(b, off, len)` 后即返回）— 同文件 read(byte[],int,int)。

## 4. 测试用例设计 + 自动化实现（SequenceInputStreamTest.testBlackBox）
1. 双流构造顺序耗尽到 EOF；bulk read 首次只取第一流全部字节（不跨界）。
2. available 只反映当前流；空首流静默切换。
3. Enumeration 构造三流串联；空枚举即 EOF。
4. Tracked 子类证明 close 关闭**每一个**曾经 current 的流（含未读到的流）。
5. transferTo 输出全拼接。
- 健壮性: BAIS 组合，无线程。

产出：`SequenceInputStreamTest.java`（白盒 + 黑盒共 25 断言）。

## 深化追加（契约级）

本轮（批次 ioA）追加契约级断言（23 -> 91）：与 JDK-8 侧镜像（转换即关闭、继承 skip 全矩阵、null 构造时序、close 后状态、mark/reset、len==0 短路、枚举异常双路透传、失败子流 read/available、空中间流、单元素、急切首元素、300 字节内容、区域不跨界）+ JDK-27 差异与 transferTo。

### 版本差异（实测锁定，写入本侧断言）
- close 不中止：失败子流 close 抛出的 IOException 透传，但剩余流仍全部关闭；多个失败时后续异常 suppressed 挂在首个上（实测 suppressed==1）。
- read OOB 消息：Objects.checkFromIndexSize "Range [x, x + n) out of bounds for length y"（消息级断言）；ctor((Enumeration)null) NPE 带 ""this.e" is null"。
- transferTo（覆写）：部分消费跨边界传剩余（实测 4 字节 [2,3,4,5]）后 EOF；空枚举传 0；null out -> NPE（消息 "out"）；下沉异常透传；close 后返回 0。

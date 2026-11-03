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

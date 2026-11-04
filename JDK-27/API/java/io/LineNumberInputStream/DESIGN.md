# java.io.LineNumberInputStream 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- @Deprecated 字节级行计数 FilterInputStream：`(InputStream)`。
- `read()`（\r 投递为 '\n'、\r\n 折叠）、`read(byte[],int,int)`（折叠 \r\n 占 1 字节）、`getLineNumber()`/`setLineNumber()`。
- `available()` 隐藏 \r\n 配对的第二个字节；mark/reset 连行号回退；skip 跨行计数。

## 2. Java 用户如何使用
- 遗留代码的字节流行号追踪（Java 1.0 时代 API，新代码用 LineNumberReader）。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- 折叠与 available 修正实现 — `jdk25u-dev/src/java.base/share/classes/java/io/LineNumberInputStream.java`。

## 4. 测试用例设计 + 自动化实现（LineNumberInputStreamTest.testBlackBox）
1. read() 逐字节：\n、\r→'\n'、\r\n 折叠后 '\n'，各计一行；EOF。
2. bulk read 把 \r\n 折叠为 1 字节且两个终止符都投递 '\n'，行数 2。
3. available() 在 \r\n 前只报 \r（配对 \n 隐藏）——探针 P8 证实的计数语义。
4. setLineNumber(41) 延续计数；mark/reset 连行号回退；skip 跨终止符计数。
5. close 透传 BAIS（容忍 close，未读数据仍可读）——close 无强制失效。
- 健壮性: BAIS 固定数据；@Deprecated API 用引用不触发 removal 错误。

产出：`LineNumberInputStreamTest.java`（白盒 + 黑盒共 33 断言）。

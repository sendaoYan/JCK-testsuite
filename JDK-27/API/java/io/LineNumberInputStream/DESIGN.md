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

## 深化追加（契约级）

新增维度（全部经 JDK-27 实测锁定）：
- 构造 null 不抛 NPE；NPE 延迟到首次使用底层（read/close）
- available 公式（pushBack==-1 ? avail/2 : avail/2+1）："a\rb" 与 "ab\r\n" 序列验证
- bulk read 吞中途 IOException 返回部分计数；单字节 read() 透传
- read(b,off,len) 参数矩阵
- skip：负/0 → 0；跨 \r\n 与纯 \r 计行；越过 EOF 夹紧到合并后剩余（skip(99)=1）
- 行号深分支：负 setLineNumber、非终止符不加行、EOF 不加行、单独 \r 计行
- mark/reset 恢复行号+位置+pushBack 状态
- 逐字节合并语义与空流矩阵

负例矩阵：read on null underlying NPE / close on null underlying NPE / read(b,1,3) IOOBE / read(null,0,1) NPE / read() 透传底层 IOException。

版本差异记录：与 JDK-8 行为完全一致；27 侧越界 IOOBE 消息为 Range 风格、8 为 null（断类型级）。

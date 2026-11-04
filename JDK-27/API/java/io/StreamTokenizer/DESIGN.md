# java.io.StreamTokenizer 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 构造：`(Reader)`、`(InputStream)`（ISO 8859-1 假设）；公开状态 `ttype`/`sval`/`nval`。
- 默认语法（私有构造器装配）：字母+128..255 为 word、空白 0..' '、`commentChar('/')`、`quoteChar('"')`与`quoteChar('\'')`、`parseNumbers()`（数字、'-'负号、'.'小数点；**无指数**）。
- 配置 API：`resetSyntax`、`wordChars(lo,hi)`、`whitespaceChars`、`ordinaryChars/ordinaryChar`、`commentChar`、`quoteChar`、`parseNumbers`、`eolIsSignificant`、`slashSlashComments`、`slashStarComments`、`lowerCaseMode`。
- 读 API：`nextToken()`（返回 TT_WORD/TT_NUMBER/TT_EOL/TT_EOF/字符 ttype）、`pushBack()`、`lineno()`、`toString()`（Token[...], line n）。
- 常量：TT_EOF=-1、TT_EOL='\n'、TT_NUMBER=-2、TT_WORD=-3。

## 2. Java 用户如何使用
- 手写解析器：nextToken 循环按 ttype 分派（TT_WORD→sval、TT_NUMBER→nval、引号字符→sval 带 ttype=引号符）；配置语法子集后扫描配置文件/DSL。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- 默认语法装配（commentChar('/') 与两种 quoteChar 在私有构造器里）— `jdk25u-dev/src/java.base/share/classes/java/io/StreamTokenizer.java:188-196`。
- 注释与引号转义（octal \101、\t、\"、块注释跨行计行号）— 同文件 nextToken/readInWord 实现。
- 官方回归 — `jdk25u-dev/test/jdk/java/io/StreamTokenizer/`。

## 4. 测试用例设计 + 自动化实现（StreamTokenizerTest.testBlackBox，探针 ST1/ST2 先行）
1. 默认语法：单词/整数/负数/小数；EOF 后 lineno。
2. **默认 '/' 即注释符**（文档陷阱）：x/y 只剩 x。
3. 引号 token ttype=引号字符、sval=内容；转义 \"、\t、\\、八进制 \101→'A'；未闭合引号不出错。
4. ordinaryChar('\'') 关闭单引号引号语义。
5. 数字形态：.5→0.5、5.→5.0、007→7.0、1e3→1.0+word"e3"（无指数）。
6. eolIsSignificant：\n、\r、\r\n 各一个 TT_EOL 且行号递增；默认 false 时换行是空白但行号仍计。
7. slashSlash/slashStar 注释（块注释跨行推进 lineno）；commentChar('#') 只到行尾。
8. lowerCaseMode 只小写化 word 不动引号串。
9. pushBack 重复上一 token；wordChars/whitespaceChars/ordinaryChars 区间配置；resetSyntax 全普通（含空格）；parseNumbers 夺回 digit。
10. toString 四形态：Token[x]/Token[n=3.0]/Token[q]/Token['+']/Token[EOL]/Token[EOF]。
- 健壮性: 全部固定字符串（探针验证值）。

产出：`StreamTokenizerTest.java`（白盒 + 黑盒共 95 断言）。

## 深化追加（契约级）
追加内容（JDK-27 侧断言数 211，另一侧见对应 DESIGN）。所有行为断言均为双侧 JDK 实测后写入。
### serialVersionUID 实测值（ObjectStreamClass.lookup，双侧一致）
- `StreamTokenizer`：**不可序列化**（ObjectStreamClass.lookup 抛异常路径未用）
### 新增测试方法
1. testInitialState（TT_NOTHING=-4 初值 + 「Token[NOTHING], line 1」+ 四个 TT_ 常量值 + 首调用前 pushBack 无害）
2. testPushBackSemantics（一深标志非栈：2×/3× 均只重放一次；EOF 可无限重放；EOL/引号 token 重放；pushBack 后 toString）
3. testQuoteCloseRules（\n/\r 关闭未终结引号；空引号 sval==""；孤引号到 EOF）
4. testEscapeMatrix（\a=7 \v=11 \f \b \r \n \t；八进制 1/12/123；\128=\12+'8'；\0=NUL；\777 溢出=\77+'7'）
5. testHighChars（≥0x100 恒为 word 字符：resetSyntax 后依旧；wordChars 负值/>255 为 no-op；(0,1000) 截断）
6. testResetSyntax（256 字符全 ordinary 含 \n 作 token；lineno 冻结；whitespaceChars/parseNumbers 可在其上重建）
7. testEolSignificant（\r\n 合并为一个 EOL；空 \r\n 行独立 EOL）
8. testLinenoQuirks（块注释吞「\n\n」仅 +1 行；未闭合块注释 EOF 且行号不动；孤 \r 计行）
9. testSlashConfiguration（ordinaryChar('/') 停两种注释；slashSlashComments(true) 重新启用；// 止于 \r；commentChar 覆盖 quoteChar）
10. testNumberMatrix（孤 '.'=0.0、'.x'=0.0+word、'-0' 保 -0.0 位型、30 位整数塌缩 1.2345678901234566E29、尾数累除精度 1.7976931348623155、'3-4'=3.0/-4.0、孤 '-'=ordinary、NaN/Infinity 为 word、'0x1F'=0.0+"x1F"、数后引号、'007.5e2'=7.5+word、'..'、'5..6'、'+5'='+'+5.0）
11. testFlagSemantics（whitespaceChars 赋值语义抹掉数字/word 标志；wordChars OR 语义；quoteChar 叠加共存；ordinaryChar 撤销引号）
12. testNvalSvalState（number: sval 清空；word: nval 保留旧值；EOF: sval 清 nval 留；ordinary: 双清）
13. testLowercaseMapping（0x100→0x101 非_ascii 也小写化）
14. testInputStreamCtor（0xC3 字节 1:1 成 U+00C3 word 字符；构造器 1.1 起弃用）
15. testReaderPropagation（经 Reader.read() 单字符路径传播 IOException：首读与流中）

### 负例与精确异常（操作 → 类型 → 双侧一致 message 子串）
- 流中 Reader 异常经 read() 传播（读数组重载不被调用——实测路径）
- resetSyntax 后 \n 以 ordinary token 交付且 lineno 不前进

### JDK-8 vs JDK-27 差异
- 无（构造器面、message 文案、serialVersionUID、行为语义双侧一致）。
### 序列化往返清单
- 不适用（该类非 Serializable）。

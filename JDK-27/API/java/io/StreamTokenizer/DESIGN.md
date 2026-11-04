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

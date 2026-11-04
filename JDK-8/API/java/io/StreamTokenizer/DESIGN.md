# java.io.StreamTokenizer 测试方案 (DESIGN)

## 1. API 功能
- 构造：`(Reader)`、`(InputStream)`。
- 配置：`resetSyntax/wordChars/whitespaceChars/ordinaryChars/ordinaryChar/commentChar/quoteChar/parseNumbers/eolIsSignificant/slashStarComments/slashSlashComments/lowerCaseMode`。
- 读取：`nextToken()`、`pushBack()`、`lineno()`；字段 `ttype`/`sval`/`nval`；常量 `TT_EOF(-1)/TT_EOL('\n')/TT_NUMBER(-2)/TT_WORD(-3)`；`toString()`。

## 2. Java 用户如何使用
- 文本解析器按默认语法或自定义语法切分单词/数字/引号串/注释。

## 3. 开源代码实际用法调研
- 默认语法（commentChar '/', 双引号/单引号 quote）— `jdk8u-dev/jdk/src/share/classes/java/io/StreamTokenizer.java`。

## 4. 测试用例设计 + 自动化实现（StreamTokenizerTest.java，纯黑盒）
黑盒场景（17 组）:
1. 默认语法 word/number/负/小数/EOF/lineno。
2. 默认 '/' 注释到行尾。
3. 引号串 + `\\` `\"` `\n` `\t` 与八进制转义。
4. 未闭合引号取正文不报错。
5. ordinaryChar 关闭单引号引用。
6. 数字形态 .5 / 5. / 007 / 无指数（e3 保持 word）。
7. eolIsSignificant 下 `\n`/`\r`/`\r\n` 各一个 TT_EOL + lineno。
8. 默认 eolIsSignificant(false) 换行当空白但 lineno 仍计。
9. slashSlashComments/slashStarComments（块注释跨行）。
10. commentChar 自定义行注释。
11. lowerCaseMode 只作用于 word、不动引号串。
12. pushBack 重复上一 token。
13. wordChars/whitespaceChars 范围切分。
14. ordinaryChars 取消数字、parseNumbers 回收。
15. resetSyntax 全字符 ordinary（含空格）。
16. toString 六形态（word/number/quote/ordinary/EOL/EOF）。
17. 空输入立即 EOF。
- 健壮性: 内存 StringReader；无反射白盒。

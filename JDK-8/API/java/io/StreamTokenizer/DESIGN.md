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

## 深化追加（契约级）
追加内容（JDK-8 侧断言数 181，另一侧见对应 DESIGN）。所有行为断言均为双侧 JDK 实测后写入。
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

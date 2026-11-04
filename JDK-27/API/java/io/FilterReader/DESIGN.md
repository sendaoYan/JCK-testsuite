# java.io.FilterReader 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 抽象装饰基类：`protected Reader in`；`protected FilterReader(Reader)`。
- 透传 `read()`、`read(char[],int,int)`、`skip`、`ready`、`markSupported`、`mark`、`reset`、`close`。

## 2. Java 用户如何使用
- 字符流过滤器骨架：PushbackReader/BufferedReader 风格扩展；覆盖 read 做大小写转换/脱敏，其余委托。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- 透传实现与抽象性 — `jdk25u-dev/src/java.base/share/classes/java/io/FilterReader.java`。
- 官方子类样板 — `jdk25u-dev/src/java.base/share/classes/java/io/PushbackReader.java`（unread 用自身缓冲，其余透传）。

## 4. 测试用例设计 + 自动化实现（FilterReaderTest.testBlackBox）
1. 具体子类包装 StringReader：read/read(char[])/skip/ready 与源一致。
2. mark/reset 透传：mark→读→reset 回退（StringReader 支持 mark）。
3. close 透传后 read 抛 IOException（StringReader close 语义："Stream closed"）。
4. 子类覆盖 read() 后 bulk read 逐字符路由（Tracked 计数）。
- 健壮性: 纯内存。

产出：`FilterReaderTest.java`（白盒 + 黑盒共 25 断言）。

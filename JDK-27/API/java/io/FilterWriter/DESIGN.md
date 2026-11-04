# java.io.FilterWriter 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 抽象装饰基类：`protected Writer out`；`protected FilterWriter(Writer)`。
- 覆盖 `write(int)`、`write(char[],int,int)`、`write(String,int,int)` 全部透传；`flush()`、`close()` 继承 Writer（同步透传 out）。

## 2. Java 用户如何使用
- 字符输出过滤器骨架：覆盖 write 做转义/加密/审计，flush/close 委托。
- JDK 自身无直接子类（保留 API 完整性），用户库做过滤写。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- 三 write 透传实现 — `jdk25u-dev/src/java.base/share/classes/java/io/FilterWriter.java`。

## 4. 测试用例设计 + 自动化实现（FilterWriterTest.testBlackBox）
1. 具体子类包装 StringWriter：write(int)/write(char[])/write(String 区段) 内容精确一致。
2. append(CharSequence) 经 Writer 基类路由到 write(String)。
3. flush/close 透传（StringWriter 均无失败）；close 后 StringWriter 仍可读其缓冲。
- 健壮性: 纯内存。

产出：`FilterWriterTest.java`（白盒 + 黑盒共 17 断言）。

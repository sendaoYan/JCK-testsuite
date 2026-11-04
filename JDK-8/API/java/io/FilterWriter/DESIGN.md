# java.io.FilterWriter 测试方案 (DESIGN)

## 1. API 功能
- 抽象 Writer 装饰器基类；`protected Writer out` 字段与 `protected (Writer)` 构造。
- write(int)/write(char[],int,int)/write(String,int,int)/flush/close 委托给 `out`。

## 2. Java 用户如何使用
- 子类化 FilterWriter 做字符流过滤/转换，覆写 write 委托 + 加工。

## 3. 开源代码实际用法调研
- 委托实现 — `jdk8u-dev/jdk/src/share/classes/java/io/FilterWriter.java`。

## 4. 测试用例设计 + 自动化实现（FilterWriterTest.java，纯黑盒）
黑盒场景:
1. write(int)/write(char[],off,len)/write(String,off,len) 区域语义委托 → "acdY"。
2. append(char/CharSequence/CharSequence,off,len) 走 Writer 默认 → "QRSU"。
3. flush/close 委托计数。
4. 覆写 write(int) 的转大写装饰器 → "AB"。
5. 子类可达 protected `out`。
6. 底层 Writer 抛 IOException("sink") 原样传播。
- 健壮性: 内存 StringWriter；无反射白盒。

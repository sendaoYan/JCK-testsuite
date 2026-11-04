# java.io.FilterReader 测试方案 (DESIGN)

## 1. API 功能
- 抽象 Reader 装饰器基类；`protected Reader in` 字段与 `protected (Reader)` 构造。
- 继承 Reader 的 read/skip/ready/markSupported/mark/reset/close 均委托给 `in`（子类可覆写拦截）。

## 2. Java 用户如何使用
- 子类化 FilterReader 做字符流过滤/转换（如去元音、大小写转换），覆写 read 委托 + 加工。

## 3. 开源代码实际用法调研
- 委托实现 — `jdk8u-dev/jdk/src/share/classes/java/io/FilterReader.java`。

## 4. 测试用例设计 + 自动化实现（FilterReaderTest.java，纯黑盒）
黑盒场景:
1. 透传 read()/read(char[])/skip/ready/close 均委托并共享位置。
2. mark/reset/markSupported 委托计数。
3. 覆写 read 的子类去元音 "banana"→"bnn"（拦截）。
4. 子类可达 protected `in`。
5. 底层 Reader 抛 IOException("inner") 原样传播。
- 健壮性: 内存 StringReader；无反射白盒。

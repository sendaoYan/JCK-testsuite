# IO — 设计方案

## 1. API 功能
`java.lang.IO`（final，JDK 25 引入，JEP 495 简化控制台 I/O）提供静态方法：
- 输出：`print(Object)`、`println(Object)`、`println()`
- 输入：`readln()`、`readln(String prompt)`

## 2. Java 用户如何使用
- 教学/脚本式代码直接用 `IO.println/readln` 读写标准输入输出，替代 `System.out.println`/`System.in` 样板。
```java
IO.println("hello");
String name = IO.readln("name? ");
```

## 3. 开源代码实际用法调研
- JEP 495 面向初学者简化 I/O；JDK `test/jdk/java/lang/IO` 验证 `println/print`（`readln` 会阻塞标准输入，测试中不调用）。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.IO")` 验证 final、无公开构造器、五个静态方法存在。
黑盒：`println/print` 写入标准输出不抛（`readln` 阻塞输入，避免调用）。
对应 `<Simple>Test.java` 黑盒部分。

## 5. 契约深化（JDK-27 实测锁定）
- **方法面（javap 实测）**：仅 `println(Object)`/`println()`/`print(Object)`/`readln()`/`readln(String)` 五个静态方法；**不存在** console()/isTerminal()/printf/format/write（JEP 495 有意排除 printf）。任务书预报名的这些方法按实测修正。
- **输出走 System.out**（setOut 捕获矩阵证明）：println("X")→"X"+lineSeparator；println()→仅分隔符；print 不加分隔符；println((Object)null)→"null"+sep；println(42)（autobox）→"42"+sep；println(char[])→"[C@..."（Object.toString，区别于 PrintStream.println(char[])）。
- **readln 走 System.in**：首次调用缓存一个 BufferedReader（绑定当时的 System.in 实例），后续 setIn 失效——测试单流三读：readln(prompt) 返回首行且 prompt 经 print 输出；无换行尾行返回 "rest"；EOF 返回 **null**（非空串、不抛异常）。字符集取 stdin.encoding（缺省 UTF-8），测试仅用 ASCII 保持环境无关。

## 版本差异
JDK-8 无此包（java.lang.constant 是 12+、java.lang.IO 是 25+（JEP 495）、java.lang.foreign 是 19+ 预览 / 22+ 正式）；本目录测试仅 JDK-27，不镜像 JDK-8 侧。

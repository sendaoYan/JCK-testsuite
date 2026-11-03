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

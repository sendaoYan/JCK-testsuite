# java.io.Console 测试方案 (DESIGN)

## 1. API 功能
- 唯一工厂 `System.console()`（无 public 构造）：无控制终端时返回 null。
- 有终端时提供 `reader()/writer()/format/printf/readLine/readPassword/flush`。
- 注意：`charset()`(JDK 17+) 与 `isTerminal()`(JDK 22+) 为后续版本，JDK 8 不含。

## 2. Java 用户如何使用
- `Console c = System.console(); if (c != null) { String u = c.readLine("login: "); char[] pw = c.readPassword(); }`。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/Console.java`。

## 4. 测试用例设计 + 自动化实现（ConsoleTest.java，纯黑盒）
黑盒场景（jtreg 重定向 stdio，非 tty）:
1. System.console() 返回 null（无终端）。
2. 重复调用仍 null。
3. 标准流 in/out/err 仍存在。
- 健壮性: 无终端依赖；不申请真实终端，故不加 @key headful（纯反射之外的调用只在有终端时才可能，此处仅断言 null）。

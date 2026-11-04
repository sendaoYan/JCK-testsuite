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

## 深化追加（契约级，批次 ioG）

### 新增维度清单（4→39 断言）
1. **headless 工厂契约（主断言）**：jtreg 重定向 stdio 下 `System.console()==null`，重复调用稳定（`==` 同一 null）、标准三流仍存在。
2. **结构契约（JDK-8 特有基线）**：public **final** class（JDK-8 仍 final，JDK-17 起 final 被移除——版本差异）；具体类；实现 Flushable；**不**实现 AutoCloseable（无 close()）；非 Serializable；包名 java.io；所有声明构造器非 public（工厂独占创建权）。
3. **方法面（精确签名 + 返回类型，getMethod）**：writer()→PrintWriter、reader()→Reader、format(String,Object[])→Console、printf(String,Object[])→Console、readLine()→String、readLine(String,Object[])→String、readPassword()→char[]、readPassword(String,Object[])→char[]、flush()→void，全部 public。
4. **JDK-8 基线缺席锁定（版本差异）**：format/printf/readLine/readPassword 的 (Locale,String,Object[]) 重载与 charset()/isTerminal() 均以 NoSuchMethodException 断定缺席（JDK-17/17/17/17/17/22 引入）。
5. 按约定不构造实例（构造器包私有，反射实例化依赖实现细节）。

### 版本差异（本侧为基线）
| 成员 | JDK-8 | JDK-27 |
|---|---|---|
| class final | 是 | **否**（可被 mock 框架代理） |
| 声明构造器 | private 0 参 + 包私有 1 参 | 仅包私有 0 参 |
| format/printf/readLine/readPassword (Locale,...) | 无 | 有 |
| charset() / isTerminal() | 无 | 有 |

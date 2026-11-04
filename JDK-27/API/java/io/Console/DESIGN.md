# java.io.Console 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 获取：`System.console()`（非终端环境返回 null；构造器包私有）。
- 输出：`writer()`、`format/printf(String/Object..., Locale...)`、`flush()`。
- 输入：`reader()`、`readLine(...)`、`readPassword(...)`（char[]，阅后即焚）。
- 元数据：`charset()`（JDK 17+）、`isTerminal()`（JDK 22+）。

## 2. Java 用户如何使用
- CLI 交互：console()!=null 时 printf 提示 + readPassword 读密钥；为 null 回退 stdin/stdout 管道模式。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- 包私有构造与单例装配 — `jdk25u-dev/src/java.base/share/classes/java/io/Console.java:111,548,567`（Console()、isTerminal、instantiateConsole）。
- 官方测试 — `jdk25u-dev/test/jdk/java/io/System/console` 相关用例。

## 4. 测试用例设计 + 自动化实现（ConsoleTest.testBlackBox）
1. jtreg 重定向环境断言 `System.console()==null`（重复调用稳定）。
2. 白盒：无 public 构造器；writer→PrintWriter、reader→Reader、readPassword→char[]、charset→Charset、isTerminal→boolean；readLine/format/printf 的普通与 Locale 重载齐全；printf 返回 Console（链式）。
- 健壮性: headless 断言不依赖终端（jtreg 管道环境恒 null）；不输出任何内容。

产出：`ConsoleTest.java`（白盒 + 黑盒共 26 断言）。

## 深化追加（契约级，批次 ioG）

### 新增维度清单（19→70 断言）
1. **headless 工厂契约（主断言）**：jtreg 重定向 stdio 下 `System.console()==null`、跨调用稳定（== 同一 null）、标准三流独立存在、Console 类可反射枚举。
2. **结构契约（JDK-27 基线 + 版本差异锁定）**：public 具体 class 且 **不再 final**（JDK-8 仍 final，断言 `!Modifier.isFinal` 记录差异）；实现 Flushable；不实现 AutoCloseable（无 close()）；非 Serializable；**唯一声明构造器为包私有 0 参**（JDK-8 有 private 0 参 + 包私有 1 参两个——构造器面收窄）。
3. **完整 15 方法面（getMethod 精确签名 + 返回类型 + public 修饰符）**：writer()→PrintWriter、reader()→Reader、format(String,Object[])→Console、**format(Locale,String,Object[])→Console（JDK-17+）**、printf(String,Object[])→Console、**printf(Locale,String,Object[])→Console**、readLine()→String、readLine(String,Object[])→String、**readLine(Locale,String,Object[])→String**、readPassword()→char[]、readPassword(String,Object[])→char[]、**readPassword(Locale,String,Object[])→char[]**、flush()→void、**charset()→Charset（JDK-17+）**、**isTerminal()→boolean（JDK-22）**；所有 public 实例方法均由 Console 自声明。
4. 按约定不构造实例（不反射实例化包私有构造器，方法行为断言依赖真实终端，纯 headless 下唯一确定行为即 console()==null）。

### 版本差异（相对 JDK-8 基线）
| 成员 | JDK-8 | JDK-27 |
|---|---|---|
| class final | 是 | **否** |
| 声明构造器 | private 0 参 + 包私有 1 参 | 仅包私有 0 参 |
| (Locale,String,Object[]) 重载 ×4 | 无 | 有 |
| charset()/isTerminal() | 无 | 有 |

### 实测记录
两侧探针（反射枚举 + System.console()）在管道 stdio 下：console()=null、final 差异、构造器可见性差异、方法缺席/存在全部实证后写入断言。

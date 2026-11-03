# java.io.Reader 测试方案 (DESIGN)

## 1. API 功能
- 抽象 `read(char[],int,int)`/`close`；默认 `read()`/`read(char[])`/`read(CharBuffer)`；`skip`/`ready`/`mark`/`reset`/`markSupported`；`readAllLines`/`readAllAsString`/`transferTo(Writer)`；静态 `nullReader()` 与 `Reader.of(CharSequence)`；protected lock。

## 2. Java 用户如何使用
- 自定义字符源实现三参 read+close；`Reader.of(cs)` 把 CharSequence 变只读 Reader；nullReader 占位。

## 3. 开源代码实际用法调研
- 默认实现 — `jdk25u-dev/src/java.base/share/classes/java/io/Reader.java`（read()/read(char[])/read(CharBuffer)/skip/transferTo/readAllLines）。
- Reader.of 与 nullReader — 同文件静态工厂。
- 官方测试 — `jdk25u-dev/test/jdk/java/io/Reader/`。

## 4. 测试用例设计 + 自动化实现（ReaderTest.java）
- 白盒: abstract、Readable+Closeable、两抽象方法、lock、两个静态工厂。
- 黑盒场景（最小子类）:
  1. read()/read(char[]) 派生与 EOF。
  2. read(CharBuffer) 计数与内容。
  3. skip 正向、skip(-1) IAE。
  4. markSupported false、reset/mark 抛 "reset()/mark() not supported"、ready false。
  5. readAllLines/readAllAsString/transferTo。
  6. Reader.of 反映后续 CharSequence 修改（活视图）。
  7. nullReader 永空。
  8. StringReader 经基类引用。
- 健壮性: 全内存。

产出：`ReaderTest.java`（白盒 15 + 黑盒 29 断言）。

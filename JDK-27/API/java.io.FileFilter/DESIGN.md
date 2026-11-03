# java.io.FileFilter 测试方案 (DESIGN)

## 1. API 功能
- `accept(File)` 布尔谓词；`File.listFiles(FileFilter)` 用它筛目录项。

## 2. Java 用户如何使用
- 目录遍历按扩展名/类型筛选：`dir.listFiles(f -> f.getName().endsWith(".log"))`。

## 3. 开源代码实际用法调研
- 定义与用法 — `jdk25u-dev/src/java.base/share/classes/java/io/FileFilter.java`、`File.listFiles(FileFilter)`。
- 官方测试 — `jdk25u-dev/test/jdk/java/io/File/listFiles/`。

## 4. 测试用例设计 + 自动化实现（FileFilterTest.java）
- 白盒: 接口、accept 抽象返回 boolean、listFiles(FileFilter) 存在、非 Predicate。
- 黑盒场景（临时目录 3 文件+1 子目录）:
  1. 扩展名 lambda 过滤。
  2. 仅目录过滤（排除普通文件）。
  3. 手写 negation/and/or 组合。
  4. accept 收到的 File 有 parent。
  5. 无匹配 → 空数组非 null。
  6. accept-all 全量。
  7. 直接调用 accept。
  8. 谓词抛异常从 listFiles 传播。
- 健壮性: Files.createTempDirectory + finally 全删。

产出：`FileFilterTest.java`（白盒 5 + 黑盒 13 断言）。

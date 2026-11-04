# java.io.FilenameFilter 测试方案 (DESIGN)

## 1. API 功能
- `accept(File dir, String name)`；`File.list(FilenameFilter)`/`listFiles(FilenameFilter)` 使用之。

## 2. Java 用户如何使用
- 只关心文件名字符串的筛选（如 `*.log`），无需为每项构造 File 对象。

## 3. 开源代码实际用法调研
- 定义 — `jdk25u-dev/src/java.base/share/classes/java/io/FilenameFilter.java`。
- 官方测试 — `jdk25u-dev/test/jdk/java/io/File/list/`（dir/name 契约、无匹配空数组、缺失目录 null）。

## 4. 测试用例设计 + 自动化实现（FilenameFilterTest.java）
- 白盒: 接口、accept(File,String) 抽象、两个 list 方法存在。
- 黑盒场景（临时目录 3 文件+1 子目录）:
  1. String[] list 扩展名过滤。
  2. listFiles 同过滤返回 File[]。
  3. dir 与裸 name（无路径分隔符）契约。
  4. 前缀过滤 + 手写 and/or 组合。
  5. accept-all 含子目录共 4 项。
  6. 无匹配两种返回类型均为空数组。
  7. 直接调用 accept。
  8. 谓词抛异常传播。
  9. 缺失目录 → null。
- 健壮性: 临时目录 finally 全删。

产出：`FilenameFilterTest.java`（白盒 5 + 黑盒 14 断言）。

## 深化追加（契约级）

> 双侧探针先行，新增 testDirAndNameContract 等 8 个方法，断言 20 → 52。

### 新增维度清单（与 JDK-8 侧同构）
1. (dir,name) 契约：dir 恒等于被列目录；name 裸名可解析为真实子项；全条目各一次回调。
2. 调用计数 + 二次回调。
3. null 过滤器等价全列（list 与 listFiles）。
4. 普通文件/缺失 → null；空目录 len 0。
5. 匿名内部类双视图使用。
6. @FunctionalInterface + 单抽象方法。
7. String[]/File[] 两视图逐位一致。
8. 异常身份 == 传播。

### JDK-8 vs JDK-27 版本差异
- 无（双侧行为一致）。

# java.io.FileFilter 测试方案 (DESIGN)

## 1. API 功能
- 函数式接口 `boolean accept(File pathname)`；`File.listFiles(FileFilter)` 用它筛选目录项。
- 无匹配返回空数组（非 null）；过滤器抛异常原样传播。

## 2. Java 用户如何使用
- `dir.listFiles(f -> f.getName().endsWith(".txt"))` 按名/扩展名/可见性筛选；手工组合 not/and/or。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/FileFilter.java`；`File.listFiles(FileFilter)`。

## 4. 测试用例设计 + 自动化实现（FileFilterTest.java，纯黑盒）
黑盒场景（隔离临时目录 + 递归清理）:
1. 扩展名 lambda 筛选；结果全部匹配。
2. 目录专属过滤（File::isDirectory）。
3. 手工 not/and/or 组合。
4. accept 收到的 File 以目录为根。
5. 无匹配空数组；accept-all 返回全部条目。
6. 直接对 File 调用 accept 的真/假。
7. 过滤器抛异常从 listFiles 传播。
- 健壮性: 无硬编码分隔符；无反射白盒。

## 深化追加（契约级）

> 双侧探针先行，新增 testInvocationCounting 等 7 个方法，断言 14 → 32。

### 新增维度清单
1. **调用计数**：accept 对每个目录项恰好一次（含子目录项）；二次过滤重新全量回调且结果确定。
2. **平铺不递归**：listFiles(FileFilter) 只把直接子项交给过滤器，深层条目绝不出现。
3. **null 过滤器**：`listFiles((FileFilter)null)` 等价全列。
4. **非目录**：普通文件/缺失目录 → null；空目录 → len 0 数组。
5. **经典实现**：匿名内部类与静态嵌套类（SuffixFilter，带调用计数）照常工作；方法引用（File::isFile / File::isDirectory）。
6. **接口形状**：@FunctionalInterface 注解存在；自declared抽象方法恰 1 个。
7. **异常身份**：过滤器抛出的异常对象**原样**（==）从 listFiles 传播。

### JDK-8 vs JDK-27 版本差异
- 无（双侧行为一致）。

# java.io.FilenameFilter 测试方案 (DESIGN)

## 1. API 功能
- 函数式接口 `boolean accept(File dir, String name)`；`File.list(FilenameFilter)` 返回 String[]、`File.listFiles(FilenameFilter)` 返回 File[]。
- name 为不含路径分隔符的裸文件名；无匹配空数组；目录缺失返回 null。

## 2. Java 用户如何使用
- `dir.list((d, n) -> n.endsWith(".txt"))` 按前缀/扩展名筛选；手工组合。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/FilenameFilter.java`。

## 4. 测试用例设计 + 自动化实现（FilenameFilterTest.java，纯黑盒）
黑盒场景（隔离临时目录 + 递归清理）:
1. 扩展名过滤经 list() 与 listFiles()。
2. accept 收到 (dir, 裸名)，name 无路径分隔符。
3. 前缀过滤 + 手工 and/or 组合。
4. 目录条目也出现在列表中（accept-all 含目录）。
5. 无匹配空数组；直接 accept 真/假。
6. 过滤器抛异常传播；缺失目录 list 返回 null。
- 健壮性: 无硬编码分隔符；无反射白盒。

## 深化追加（契约级）

> 双侧探针先行，新增 testDirAndNameContract 等 8 个方法，断言 15 → 47。

### 新增维度清单
1. **(dir,name) 契约**：每次 accept 的 dir 参数**恒等于**被列目录（逐次断言）；name 为裸名（无路径分隔符）且能解析为真实子项；全部条目（文件+目录）各被回调一次。
2. **调用计数**：ExtFilter（静态嵌套类带计数）每项恰好一次；二次列目录重新回调。
3. **null 过滤器**：list(null) 与 listFiles((FilenameFilter)null) 均等价全列。
4. **非目录**：普通文件 list/listFiles → null；缺失 → null；空目录 len 0。
5. **经典实现**：匿名内部类同时用于 list（String[]）与 listFiles（File[]）。
6. **接口形状**：@FunctionalInterface + 单抽象方法。
7. **两视图一致**：同一过滤器下 String[] 名单与 File[] 名单逐位一致，File 以目录为根。
8. **异常身份**：过滤器抛出的异常对象原样（==）传播。

### JDK-8 vs JDK-27 版本差异
- 无（双侧行为一致）。

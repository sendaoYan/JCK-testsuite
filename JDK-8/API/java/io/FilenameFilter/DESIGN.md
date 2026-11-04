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

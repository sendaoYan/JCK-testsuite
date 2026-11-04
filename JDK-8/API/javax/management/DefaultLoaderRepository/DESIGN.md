# javax.management.DefaultLoaderRepository 契约测试设计（2026-11 新建）

## 1. API 功能
- deprecated 根仓库：静态 loadClass/loadClassWithout 聚合 tracked server 的 ClassLoaderRepository。

## 2. Java 用户典型用法
见 JDK-27 侧同目录设计与下节深化用例（双侧同代码）。

## 3. 参考来源
- Oracle javadoc: javax.management.DefaultLoaderRepository。
- OpenJDK `test/jdk/javax/management/` 对应目录（思路借鉴，代码自写）。
## 4. 测试用例设计
见下节深化清单。

## 深化（2026-11）：仓库语义闭环

### 新增覆盖（@Deprecated 根类，自身行为）
1. **零 tracked server**：loadClass/loadClassWithout → ClassNotFoundException（仓库只汇聚 findMBeanServer(null) 的 tracked server 的 ClassLoaderRepository）。
2. **untracked newMBeanServer 不贡献**：只建 untracked server 时依旧 CNFE（负例）。
3. **tracked server 存在时**：loadClass 解析 java.lang.String/javax.management.MBeanServer（类身份 ==）；loadClassWithout(null, name) 同效。
4. **release 后清空**：releaseMBeanServer 后再 loadClass 回到 CNFE。
5. **未知类**：CNFE 且消息含类名。
6. **结构**：两方法均 static；类 @Deprecated。

### 参考来源
- Oracle javadoc: DefaultLoaderRepository（deprecated 说明与查找顺序）。

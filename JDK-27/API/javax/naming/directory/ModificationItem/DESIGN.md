# javax.naming.directory.ModificationItem 设计案

## 1. API 功能
- 构造器：`ModificationItem(int mod_op, Attribute attr)`。
- 查询：`getModificationOp()`、`getAttribute()`、`toString()`。
- 实现 `Serializable`。操作码用 `DirContext.ADD_ATTRIBUTE`(1)/`REPLACE_ATTRIBUTE`(2)/`REMOVE_ATTRIBUTE`(3)。

## 2. Java 用户如何使用
典型：`new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("cn","Alice"))` → `getModificationOp()`/`getAttribute()` → 交给 `DirContext.modifyAttributes(name, ModificationItem[])` 批量执行目录修改。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/directory/ModificationItem.java`（toString 形如 "Replace attribute: cn: v"）。
- 来源：`~/git/jdk25u-dev/test/jdk` 目录/LDAP 修改用例以 `ModificationItem[]` 传参。
- 来源：JDK 内部 LDAP Provider 遍历 `ModificationItem[]` 生成协议操作。

## 4. 测试用例设计
黑盒断言（ModificationItemTest.testBlackBox）：
- 三种操作码（ADD/REPLACE/REMOVE）构造与 `getModificationOp()` 往返。
- `getAttribute()` 返回同一实例、可继续取 id/value。
- `toString()` 分别含 add/replace/remove。

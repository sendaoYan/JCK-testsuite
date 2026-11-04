# javax.naming.directory.SearchControls 设计案

## 1. API 功能
- 构造器：`SearchControls()`、`(int scope, long countlim, int timelim, String[] attrs, boolean retobj, boolean deref)`。
- 查询：`getSearchScope()`、`getTimeLimit()`、`getDerefLinkFlag()`、`getReturningObjFlag()`、`getCountLimit()`、`getReturningAttributes()`。
- 修改：对应的 setter（scope/timeLimit/derefLinkFlag/returningObjFlag/countLimit/returningAttributes）。
- 常量：`OBJECT_SCOPE=0`、`ONELEVEL_SCOPE=1`、`SUBTREE_SCOPE=2`。实现 `Serializable`。

## 2. Java 用户如何使用
典型：`SearchControls sc = new SearchControls()`（默认 ONELEVEL）→ `setSearchScope(SUBTREE_SCOPE)`/`setCountLimit(100)`/`setReturningAttributes(...)` → 传给 `DirContext.search(name, filter, sc)`。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/directory/SearchControls.java`。
- 来源：`~/git/jdk25u-dev/test/jdk` 目录搜索用例设置 scope/countLimit 后调用 search。

## 4. 测试用例设计
黑盒断言（SearchControlsTest.testBlackBox）：
- 默认值（scope=ONELEVEL、time=0、count=0、deref=false、retobj=false、attrs=null）。
- 各 setter/getter 往返。
- 6 参构造器全部字段往返。
- 三种 scope 往返。

# java.util.concurrent.StructuredTaskScope$Subtask$State 设计案

## 1. API 功能
子任务状态枚举（预览）：`UNAVAILABLE`/`SUCCESS`/`FAILED`。

## 2. Java 用户如何使用
`subtask.state()` 判断子任务尚未完成/成功/失败。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.StructuredTaskScope.Subtask.State`（JEP 480）；`test/jdk/java/util/concurrent/structured/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：enum + values/valueOf。
2. 黑盒主干：三常量 valueOf + FAILED 观察。
3. 契约深化：声明序 UNAVAILABLE/SUCCESS/FAILED、name() 三串、ordinal 0/1/2、getDeclaringClass、
   EnumSet 全集、valueOf("BOGUS") → IAE（testEnumContract）；三态观察（SUCCESS/FAILED 需谓词恒 false 等
   全部完成以避免"首个完成即取消"竞态；UNAVAILABLE 为被取消子任务）（testStatesObserved）。

实现：`StateTest.java`（21 断言）。

## 关键规范细节
- allUntil(sub -> true) 会在**首个**子任务完成即触发取消，多子任务混用时状态不定——观察 SUCCESS/FAILED
  需用恒 false 谓词等全部完成。

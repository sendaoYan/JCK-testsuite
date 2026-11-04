# DomainCombiner — 设计案

## 1. API 功能
单方法函数式接口：`ProtectionDomain[] combine(ProtectionDomain[] currentDomains, ProtectionDomain[] assignedDomains)`。AccessControlContext(acc, combiner) 附着后由其 getDomainCombiner 取回。

## 2. 使用方式
doPrivileged 跨上下文继承时合并当前域与指派域；实现方可恒等/丢弃/拼接/过滤。

## 3. 参考来源
- javadoc `java.security.DomainCombiner` / `AccessControlContext(AccessControlContext, DomainCombiner)`。
- javap 双侧。

## 4. 测试用例设计 + 易错规范细节
- 结构：单声明方法（函数式接口，lambda 可实现）、返回 ProtectionDomain[]、非 default、双参数组类型精确。
- 组合子矩阵：恒等 combiner 返回**调用方数组同一实例**；combine(null,null) 容忍（返回 null 不抛）；丢弃型恒空数组；拼接型保持元素顺序与身份（merged[0]==pd1 && merged[1]==pd2）。
- 实参透传：记录型按序收到 (1,null)→"1/-1"、(null,2)→"-1/2"。
- 附着：AccessControlContext(base, combiner).getDomainCombiner()==combiner；普通 context 的 getDomainCombiner()==null。
- 易错点：combine 可在任意线程被调用、数组可能为 null——实现必须自查；测试只断接口层契约，深访问控制路径由 AccessController/AccessControlContext 用例负责。

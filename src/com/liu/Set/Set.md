## Set源码分析
### Set继承图:
![](https://github.com/SinceNovember/Collections/blob/master/extendsimages/tup.jpg)
## `HashSet`
### 什么是`HashSet`
>HashSet实现Set接口，由哈希表（实际上是一个HashMap实例）支持。它不保证set 的迭代顺序；特别是它不保证该顺序恒久不变。此类允许使用null元素
### `HashSet`特点
>  1.存入的顺序与遍历输出的顺序不一定一致。
  2.存入的不允许重复。
  3.实现基于HashMap
### `HashSet`方法
  应为其基本上是调用HashMap，所有其方法都是HashMap中的。
## `LinkedHashSet`
### 什么是`LinkedHashSet`
>LinkedHashSet是hashSet的一个子类,也是用HashCode值来决定元素存储位置,但是LinkedHashSet同时用链表来维护元素的次序(元素的顺序总是与添加的顺序一致),这样看起来元素是以插入的顺序保存的.这样当遍历LinkedHashSet的时候,
LinkedHashSet就会按元素的添加顺序来访问集合里的元素.
### `LinkedHashSet`特点
  1.存入的顺序与遍历输出的顺序一致。
  2.存入的不允许重复。
  3.继承HashSet，底层使用LinkedHashMap
### `LinkedHashSet`方法
  应为其基本上是继承HashSet，但使用的是LinkedHashMap,方法与Set的方法差不多。
## `TreeSet`
### 什么是`TreeSet`
>TreeSet是SortedSet接口的唯一实现类，TreeSet可以确保集合元素处于排序状态。TreeSet支持两种排序方式，自然排序 和定制排序，其中自然排序为默认的排序方式。向TreeSet中加入的应该是同一个类的对象。
TreeSet判断两个对象不相等的方式是两个对象通过equals方法返回false，或者通过CompareTo方法比较没有返回0
自然排序
自然排序使用要排序元素的CompareTo（Object obj）方法来比较元素之间大小关系，然后将元素按照升序排列。
Java提供了一个Comparable接口，该接口里定义了一个compareTo(Object obj)方法，该方法返回一个整数值，实现了该接口的对象就可以比较大小。
obj1.compareTo(obj2)方法如果返回0，则说明被比较的两个对象相等，如果返回一个正数，则表明obj1大于obj2，如果是 负数，则表明obj1小于obj2。
如果我们将两个对象的equals方法总是返回true，则这两个对象的compareTo方法返回应该返回0
定制排序
自然排序是根据集合元素的大小，以升序排列，如果要定制排序，应该使用Comparator接口，实现 int compare(T o1,T o2)方法。
其底层是使用TreeMap实现，基本上都是使用TreeMap的方法。
### `TreeSet`特点
    1.TreeSet 是二叉树实现的.
    2.Treeset中的数据是自动排好序的.
    3.不允许放入null值.
### `TreeSet`方法
    TreeSet的方法基本上都是TreeMap中的方法，可查看代码。
## `之间的区别`
>1. HashSet是通过HashMap实现的,TreeSet是通过TreeMap实现的,只不过Set用的只是Map的key
2. Map的key和Set都有一个共同的特性就是集合的唯一性.TreeMap更是多了一个排序的功能.
3. hashCode和equal()是HashMap用的, 因为无需排序所以只需要关注定位和唯一性即可.

## Map源码分析
### Map关系图
![](https://github.com/SinceNovember/Collections/blob/master/extendsimages/Map.jpg)
## `HashMap`
### 什么`HashMap`
  基于哈希表的一个Map接口实现，存储的对象是一个键值对对象(Node实现(Map.Entry(key,value)接口),它根据键的hashCode值存储数据，大多数情况下可以直接定位到它的值，因而具有很快的访问速度，但遍历顺序却是不确定的。 HashMap最多只允许一条记录的键为null，允许多条记录的值为null。HashMap非线程安全，即任一时刻可以有多个线程同时写HashMap，可能会导致数据的不一致。如果需要满足线程安全，可以用 Collections的synchronizedMap方法使HashMap具有线程安全的能力，或者使用ConcurrentHashMap。<br>
### HashMap特点
1.底层实现是 链表数组，JDK 8 后又加了 红黑树<br>
2.实现了 Map 全部的方法<br>
3.key 用 Set 存放，所以想做到 key 不允许重复，key 对应的类需要重写 hashCode 和 equals 方法<br>
4.允许空键和空值（但空键只有一个，且放在第一位，下面会介绍）<br>
5.元素是无序的，而且顺序会不定时改变<br>
6.插入、获取的时间复杂度基本是 O(1)（前提是有适当的哈希函数，让元素分布在均匀的位置）<br>
7.遍历整个 Map 需要的时间与 桶(数组) 的长度成正比（因此初始化时 HashMap 的容量不宜太大）<br>
8.两个关键因子：初始容量、加载因子。<br>
### 内部实现
从结构实现来讲，HashMap是数组+链表+红黑树（JDK1.8增加了红黑树部分）实现的，如下如所示。
![](https://github.com/SinceNovember/Collections/blob/master/extendsimages/3.png)
###  分析HashMap的put方法
HashMap的put方法执行过程可以通过下图来理解:
![](https://github.com/SinceNovember/Collections/blob/master/extendsimages/5.png)<br>
1.判断键值对数组table[i]是否为空或为null，否则执行resize()进行扩容；<br>
2.根据键值key计算hash值得到插入的数组索引i，如果table[i]==null，直接新建节点添加，转向⑥，如果table[i]不为空，转向③；<br>
3.判断table[i]的首个元素是否和key一样，如果相同直接覆盖value，否则转向④，这里的相同指的是hashCode以及equals；<br>
4.判断table[i] 是否为treeNode，即table[i] 是否是红黑树，如果是红黑树，则直接在树中插入键值对，否则转向⑤；<br>
5.遍历table[i]，判断链表长度是否大于8，大于8的话把链表转换为红黑树，在红黑树中执行插入操作，否则进行链表的插入操作；遍历过程中若发现key已经存在直接覆盖value即可；<br>
6.插入成功后，判断实际存在的键值对数量size是否超多了最大容量threshold，如果超过，进行扩容。<br>
### 扩容机制分析
  扩容(resize)就是重新计算容量，向HashMap对象里不停的添加元素，而HashMap对象内部的数组无法装载更多的元素时，对象就需要扩大数组的长度，以便能装入更多的元素。当然Java里的数组是无法自动扩容的，方法是使用一个新的数组代替已有的容量小的数组，就像我们用一个小桶装水，如果想装更多的水，就得换大水桶。
  它同一位置上新元素总会被放在链表的头部位置；这样先放在一个索引上的元素终会被放到Entry链的尾部(如果发生了hash冲突的话），这一点和Jdk1.8有区别，下文详解。在旧数组中同一条Entry链上的元素，通过重新计算索引位置后，有可能被放到了新数组的不同位置上。
下面举个例子说明下扩容过程。假设了我们的hash算法就是简单的用key mod 一下表的大小（也就是数组的长度）。其中的哈希桶数组table的size=2， 所以key = 3、7、5，put顺序依次为 5、7、3。在mod 2以后都冲突在table[1]这里了。这里假设负载因子 loadFactor=1，即当键值对的实际大小size 大于 table的实际大小时进行扩容。接下来的三个步骤是哈希桶数组 resize成4，然后所有的Node重新rehash的过程。
![](https://github.com/SinceNovember/Collections/blob/master/extendsimages/6.png)<br>
  下面我们讲解下JDK1.8做了哪些优化。经过观测可以发现，我们使用的是2次幂的扩展(指长度扩为原来2倍)，所以，元素的位置要么是在原位置，要么是在原位置再移动2次幂的位置。看下图可以明白这句话的意思，n为table的长度，图（a）表示扩容前的key1和key2两种key确定索引位置的示例，图（b）表示扩容后key1和key2两种key确定索引位置的示例，其中hash1是key1对应的哈希与高位运算结果。
![](https://github.com/SinceNovember/Collections/blob/master/extendsimages/7.png)<br>
元素在重新计算hash之后，因为n变为2倍，那么n-1的mask范围在高位多1bit(红色)，因此新的index就会发生这样的变化：
![](https://github.com/SinceNovember/Collections/blob/master/extendsimages/10.png)<br>
因此，我们在扩充HashMap的时候，不需要像JDK1.7的实现那样重新计算hash，只需要看看原来的hash值新增的那个bit是1还是0就好了，是0的话索引没变，是1的话索引变成“原索引+oldCap”，可以看看下图为16扩充为32的resize示意图：
![](https://github.com/SinceNovember/Collections/blob/master/extendsimages/8.png)<br>
这个设计确实非常的巧妙，既省去了重新计算hash值的时间，而且同时，由于新增的1bit是0还是1可以认为是随机的，因此resize的过程，均匀的把之前的冲突的节点分散到新的bucket了。这一块就是JDK1.8新增的优化点。<br>
### Map接口方法
`int size()`：获取Map中Entry的长度。<br>
`boolean isEmpty()`;判断Map是否为空。<br>
`boolean containsKey(Object key)`;判断指定的key在Map中是否存在。<br>
`boolean containsValue(Object value)`;判断指定的value在Map中是否存在。<br>
`V get(Object key)`;通过指定的获取相应的值。<br>
`V put(K key, V value)`;添加Entry实体元素。<br>
`V remove(Object key)`;移除指定key的节点。<br> 
`void putAll(Map <? extends K,? extends V> m)`;将Map结合元素全部添加到此Map中。<br> 
`void clear()`;将Map中的元素全部清空。<br>
`boolean replace(K key, V oldValue, V newValue)`;将指定的key-value实体，的值替换为指定值。<br>
`Set<K> keySet();`返回Map中所有的key值存放到一个set中并返回。<br>
` Collection<V> values();`返回Map中所有的value值存放到一个Collection中并返回。<br>
`Set<Map.Entry<K, V>> entrySet();`返回Map中所有的Entry值存放到一个Set中并返回。<br>
 keySet与values以及entrySet中有个迭代器方法，其中包含：<br>
`hashNext()`;判断是否还有下一个实体，返回布尔值。<br>
`next()`;返回下一个实体。<br>
Map.Entry实体中具有几个方法:<br>
`K getkey()`;获取实体中的键。<br>
`V getValue()`;获取实体中的值。<br>
`V setValue(V value)`;设置实体中的值。<br>
`K setKey(K key)`;设置实体中的键。<br>
`boolean equals(Object o)`;判断对象与实体是否相似。<br>
`int hashCode()`;获取实体的hash值。<br>
HashMap中基本的public方法也差不多这几个，并且方法内容差不多。<br>
## `LinkedHashMap`
### 什么`LinkedHashMap`
  LinkedHashMap是Map接口的哈希表和链接列表实现，具有可预知的迭代顺序。此实现提供所有可选的映射操作，并允许使用null值和null键。此类不保证映射的顺序，特别是它不保证该顺序恒久不变。<br>
LinkedHashMap实现与HashMap的不同之处在于，后者维护着一个运行于所有条目的双重链接列表。此链接列表定义了迭代顺序，该迭代顺序可以是插入顺序或者是访问顺序。<br>
注意，此实现不是同步的。如果多个线程同时访问链接的哈希映射，而其中至少一个线程从结构上修改了该映射，则它必须保持外部同步。<br>
### `LinkedHashMap`的实现
   对于LinkedHashMap而言，它继承与HashMap、底层使用哈希表与双向链表来保存所有元素。其基本操作与父类HashMap相似，它通过重写父类相关的方法，来实现自己的链接列表特性。下面我们来分析LinkedHashMap的源代码：<br>
 1) Entry元素：<br>
LinkedHashMap采用的hash算法和HashMap相同，但是它重新定义了数组中保存的元素Entry，该Entry除了保存当前对象的引用外，还保存了其上一个元素before和下一个元素after的引用，从而在哈希表的基础上又构成了双向链接列表。<br>
   ![](https://github.com/SinceNovember/Collections/blob/master/extendsimages/linkedHashMap.jpg)
### `LinkedHashMap`的特点
  1.存储的顺序与插入的顺序一致<br>
  2.线程不安全<br>
  3.迭代的时候与输出的顺序一致<br>
### 重要方法分析
 `newNode`函数<br>
  此函数在HashMap类中也有实现，LinkedHashMap重写了该函数，所以当实际对象为LinkedHashMap，桶中结点类型为Node时，我们调用的是LinkedHashMap的newNode函数，而非HashMap的函数，newNode函数会在调用put函数时被调用。可以看到，除了新建一个结点之外，还把这个结点链接到双链表的末尾了，这个操作维护了插入顺序。<br>
  `afterNodeAccess`函数<br>
  此函数在很多函数（如put）中都会被回调，LinkedHashMap重写了HashMap中的此函数。若访问顺序为true，且访问的对象不是尾结点，则下面的图展示了访问前和访问后的状态，假设访问的结点为结点3:<br>
  ![](https://github.com/SinceNovember/Collections/blob/master/extendsimages/linkedhashmap2.jpg)<br>
  `transferLinks`函数<br>
  此函数用dst结点替换结点，示意图如下:<br>
  ![](https://github.com/SinceNovember/Collections/blob/master/extendsimages/linkedhashmap3.jpg)<br>
  其他的使用方法基本和HashMap差不多。
## `TreeMap`
### `TreeMap的基本概念：`
1.TreeMap集合是基于红黑树（Red-Black tree）的 NavigableMap实现。该集合最重要的特点就是可排序，该映射根据其键的自然顺序进行排序，或者根据创建映射时提供的 Comparator 进行排序，具体取决于使用的构造方法。这句话是什么意思呢？就是说TreeMap可以对添加进来的元素进行排序，可以按照默认的排序方式，也可以自己指定排序方式。<br>
2.根据上一条，我们要想使用TreeMap存储并排序我们自定义的类（如User类），那么必须自己定义比较机制：一种方式是User类去实现Java.lang.Comparable接口，并实现其compareTo()方法。另一种方式是写一个类（如MyCompatator）去实现java.util.Comparator接口，并实现compare()方法，然后将MyCompatator类实例对象作为TreeMap的构造方法参数进行传参。<br>
3.TreeMap的实现是红黑树算法的实现，应该了解红黑树的基本概念。<br>
### `红黑树简介 `
红黑树又称红-黑二叉树，它首先是一颗二叉树，它具体二叉树所有的特性。同时红黑树更是一颗自平衡的排序二叉树。<br> 
1、每个节点都只能是红色或者黑色 <br>
2、根节点是黑色 <br>
3、每个叶节点（NIL节点，空节点）是黑色的。 <br>
4、如果一个结点是红的，则它两个子节点都是黑的。也就是说在一条路径上不能出现相邻的两个红色结点。 <br>
5、从任一节点到其每个叶子的所有路径都包含相同数目的黑色节点。 <br>
如图:<br>
![](https://github.com/SinceNovember/Collections/blob/master/extendsimages/1111.jpg)<br>
#### `数据结构设计`
和一般的数据结构设计类似，我们用抽象数据类型表示红黑树的节点，使用指针保存节点之间的相互关系。<br> 
作为红黑树节点，其基本属性有：节点的颜色、左子节点指针、右子节点指针、父节点指针、节点的值。<br>
![](https://github.com/SinceNovember/Collections/blob/master/extendsimages/2222.jpg)<br>
#### `红黑树的插入操作`
红黑树的插入操作和查询操作有些类似，它按照二分搜索的方式递归寻找插入点。不过这里需要考虑边界条件——当树为空时需要特殊处理（这里未采用STL对树根节点实现的特殊技巧）。如果插入第一个节点，我们直接用树根记录这个节点，并设置为黑色，否则作递归查找插入（__insert操作）。<br>
默认插入的节点颜色都是红色，因为插入黑色节点会破坏根路径上的黑色节点总数，但即使如此，也会出现连续红色节点的情况。因此在一般的插入操作之后，出现红黑树约束条件不满足的情况（称为失去平衡）时，就必须要根据当前的红黑树的情况做相应的调整（__rebalance操作）。和AVL树的平衡调整通过旋转操作的实现类似，红黑树的调整操作一般都是通过旋转结合节点的变色操作来完成的。<br>
红黑树插入节点操作产生的不平衡来源于当前插入点和父节点的颜色冲突导致的（都是红色，违反规则2）。<br>
![](https://github.com/SinceNovember/Collections/blob/master/extendsimages/3333.jpg)<br>
如图4所示，由于节点插入之前红黑树是平衡的，因此可以断定祖父节点g必存在（规则1：根节点必须是黑色），且是黑色（规则2：不会有连续的红色节点），而叔父节点u颜色不确定，因此可以把问题分为两大类：<br>
1、叔父节点是黑色（若是空节点则默认为黑色）<br>
这种情况下通过旋转和变色操作可以使红黑树恢复平衡。但是考虑当前节点n和父节点p的位置又分为四种情况：<br>
A、n是p左子节点，p是g的左子节点。<br>
B、n是p右子节点，p是g的右子节点。<br>
C、n是p左子节点，p是g的右子节点。<br>
D、n是p右子节点，p是g的左子节点。<br>
情况A，B统一称为外侧插入，C，D统一称为内侧插入。之所以这样分类是因为同类的插入方式的解决方式是对称的，可以通过镜像的方法相似完成。<br>
首先考虑情况A：n是p左子节点，p是g的左子节点。针对该情况可以通过一次右旋转操作，并将p设为黑色，g设为红色完成重新平衡。<br>
![](https://github.com/SinceNovember/Collections/blob/master/extendsimages/zuocha.jpg)<br>
  右旋操作的步骤是：将p挂接在g节点原来的位置（如果g原是根节点，需要考虑边界条件），将p的右子树x挂到g的左子节点，再把g挂在p的右子节点上，完成右旋操作。这里将最终旋转结果的子树的根节点作为旋转轴（p节点），也就是说旋转轴在旋转结束后称为新子树的根节点！这里需要强调一下和STL的旋转操作的区别，STL的右旋操作的旋转轴视为旋转之前的子树根节点（g节点），不过这并不影响旋转操作的效果。<br>
  类比之下，情况B则需要使用左单旋操作来解决平衡问题，方法和情况A类似。<br>
![](https://github.com/SinceNovember/Collections/blob/master/extendsimages/youwaiqie.jpg)<br>
  接下来，考虑情况C：n是p左子节点，p是g的右子节点。针对该情况通过一次左旋，一次右旋操作（旋转轴都是n，注意不是p），并将n设为黑色，g设为红色完成重新平衡。<br>
  ![](https://github.com/SinceNovember/Collections/blob/master/extendsimages/zuoneiqie.jpg)<br>
  需要注意的是，由于此时新插入的节点是n，它的左右子树x，y都是空节点，但即使如此，旋转操作的结果需要将x，y新的位置设置正确（如果不把p和g的对应分支设置为空节点的话，就会破坏树的结构）。在之后的其他操作中，待旋转的节点n的左右子树可能就不是空节点了。<br>
  类比之下，情况D则需要使用一次右单旋，一次左单旋操作来解决平衡问题，方法和情况C类似。<br>
   ![](https://github.com/SinceNovember/Collections/blob/master/extendsimages/youneiqie.jpg)<br>
   2、叔父节点是红色<br>
  当叔父节点是红色时，则不能直接通过上述方式处理了（把前边的所有情况的u节点看作红色，会发现节点u和g是红色冲突的）。但是我们可以交换g与p，u节点的颜色完成当前冲突的解决。<br>
 ![](https://github.com/SinceNovember/Collections/blob/master/extendsimages/shufuhong.jpg)<br>
 但是仅仅这样做颜色交换是不够的，因为祖父节点g的父节点（记作gp）如果也是红色的话仍然会有冲突（g和gp是连续的红色，违反规则2）。为了解决这样的冲突，我们需要从当前插入点n向根节点root回溯两次。<br>
第一次回溯时处理所有拥有两个红色节点的节点，并按照图9中的方式交换父节点g与子节点p，u的颜色，并暂时忽略gp和p的颜色冲突。如果根节点的两个子节点也是这种情况，则在颜色交换完毕后重新将根节点设置为黑色。<br>
第二次回溯专门处理连续的红色节点冲突。由于经过第一遍的处理，在新插入点n的路径上一定不存在同为红色的兄弟节点了。而仍出现gp和p的红色冲突时，gp的兄弟节点（gu）可以断定为黑色，这样就回归前边讨论的叔父节点为黑色时的情况处理。<br>
 ![](https://github.com/SinceNovember/Collections/blob/master/extendsimages/xiaochu.jpg)
 由于发生冲突的两个红色节点位置可能是任意的，因此会出现上述的四种旋转情况。不过我们把靠近叶子的红色节点（g）看作新插入的节点，这样面对A，B情况则把p的父节点gp作为旋转轴，旋转后gp会是新子树的根，而面对C，D情况时把p作为旋转轴即可，旋转后p为新子树的根（因此可以把四种旋转方式封装起来）。
在第二次回溯时，虽然每次遇到红色冲突旋转后都会提升g和gp节点的位置（与根节点的距离减少），但是无论g和gp谁是新子树的根都不会影响新插入节点n到根节点root路径的回溯，而且一旦新子树的根到达根节点（parent指针为空）就可以停止回溯了。<br>
通过以上的树重新平衡策略可以完美地解决红黑树插入节点的平衡问题。<br><br>
#### `红黑树的删除操作`
由于红黑树就是二叉搜索树，因此节点的删除方式和二叉搜索树相同。不过红黑树删除操作的难点不在于节点的删除，而在于删除节点后的调整操作。因此红黑树的删除操作分为两步，首先确定被删除节点的位置，然后调整红黑树的平衡性。<br>
先考虑删除节点的位置，如果待删除节点拥有唯一子节点或没有子节点，则将该节点删除，并将其子节点（或空节点）代替自身的位置。如果待删除节点有两个子节点，则不能将该节点直接删除。而是从其右子树中选取最小值节点（或左子树的最大值节点）作为删除节点（该节点一定没有两个子节点了，否则还能取更小的值）。当然在删除被选取的节点之前，需要将被选取的节点的数据拷贝到原本需要删除的节点中。选定删除节点位置的情况如图11所示，这和二叉搜索树的节点删除完全相同。<br>
 ![](https://github.com/SinceNovember/Collections/blob/master/extendsimages/shanchu0.jpg)<br>
 图11中用红色标记的节点表示被选定的真正删除的节点（节点y）。其中绿色节点（yold）表示原本需要删除的节点，而由于它有两个子节点，因此删除y代替它，并且删除y之前需要将y的值拷贝到yold，注意这里如果是红黑树也不会改变yold的颜色！通过上述的方式，将所有的节点删除问题简化为独立后继（或者无后继）的节点删除问题。然后再考虑删除y后的红黑树平衡调整问题。由于删除y节点后，y的后继节点n会作为y的父节点p的孩子。因此在进行红黑树平衡调整时，n是p的子节点。<br>
下边考虑平衡性调整问题，首先考虑被删除节点y的颜色。如果y为红色，删除y后不会影响红黑树的平衡性，因此不需要做任何调整。如果y为黑色，则y所在的路径上的黑色节点总数减少1，红黑树失去平衡，需要调整。<br>
y为黑色时，再考虑节点n的颜色。如果n为红色，因为n是y的唯一后继，如果把n的颜色设置为黑色，那么就能恢复y之前所在路径的黑色节点的总数，调整完成。如果n也是黑色，则需要按照以下四个步骤来考虑。<br>
设p是n的父节点，w为n节点的兄弟节点。假定n是p的左子节点，n是p的右子节点情况可以镜像对称考虑。<br>
步骤1：若w为红色，则断定w的子节点（如果存在的话或者为空节点）和节点p必是黑色（规则2）。此时将w与p的颜色交换，并以w为旋转轴进行左旋转操作，最后将w设定为n的新兄弟节点（原来w的左子树x）。<br>
通过这样的转换，将原本红色的w节点情况转换为黑色w节点情况。若w原本就是黑色（或者空节点），则直接进入步骤2。<br>
 ![](https://github.com/SinceNovember/Collections/blob/master/extendsimages/shanchu1.jpg)<br>
 步骤2：无论步骤1是否得到处理，步骤2处理的总是黑色的w节点，此时再考虑w的两个子节点x，y的颜色情况。如果x，y都是黑色节点（或者是空节点，如果父节点w为空节点，认为x，y也都是空节点），此时将w的颜色设置为红色，并将n设定为n的父节点p。此时，如果n为红色，则直接设定n为黑色，调整结束。否则再次回到步骤1做相似的处理。注意节点n发生变化后需要重新设定节点w和p。<br>
考虑由于之前黑色节点删除导致n的路径上黑色节点数减1，因此可以把节点n看作拥有双重黑色的节点。通过此步骤将n节点上移，使得n与根节点距离减少，更极端的情况是当n成为根节点时，树就能恢复平衡了（因为根节点不在乎多一重黑色）。另外，在n的上移过程中可能通过后续的转换已经让树恢复平衡了。<br>
 ![](https://github.com/SinceNovember/Collections/blob/master/extendsimages/shanchu2.jpg)<br>
 步骤3：如果步骤2中的w的子节点不是全黑色，而是左红（x红）右黑（y黑）的话，将x设置为黑色，w设置为红色，并以节点x为旋转轴右旋转，最后将w设定为n的新兄弟（原来的x节点）。<br>
通过这样的转换，让原本w子节点左红右黑的情况转化为左黑右红的情况。若w的右子节点原本就是红色（左子节点颜色可黑可红），则直接进入步骤4。<br>
 ![](https://github.com/SinceNovember/Collections/blob/master/extendsimages/shanchu3.jpg)<br>
 步骤4：该步骤处理w右子节点y为红色的情况，此时w的左子节点x可黑可红。这时将w的右子节点y设置为黑色，并交换w与父节点p的颜色（w原为黑色，p颜色可黑可红），再以w为旋转轴左旋转，红黑树调整算法结束。<br>
通过该步骤的转换，可以彻底解决红黑树的平衡问题！该步骤的实质是利用左旋恢复节点n上的黑色节点总数，虽然p和w虽然交换了颜色，但它们都是n的祖先，因此n路径上的黑色节点数增加1。同时由于左旋，使得y路径上的黑色节点数减1，恰巧的是y的颜色为红，将y设置为黑便能恢复y节点路径上黑色节点的总数。<br>
 ![](https://github.com/SinceNovember/Collections/blob/master/extendsimages/shanchu4.jpg)<br>

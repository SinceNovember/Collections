package com.liu.Set;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.TreeMap;

public class TreeSet<E> extends AbstractSet<E>
implements NavigableSet<E>, Cloneable, java.io.Serializable
{
	 private transient NavigableMap<E,Object> m;// 使用NavigableMap对象的key来保存Set集合的元素
	 private static final Object PRESENT = new Object();//PRESENT会被当做Map的value与key构建成键值对
	  TreeSet(NavigableMap<E,Object> m) {// 将TreeMap赋值给 "NavigableMap对象m"
	        this.m = m;
	    }
	  // 不带参数的构造函数。创建一个空的TreeMap
	    //以自然排序方法创建一个新的TreeMap，再根据该TreeMap创建一个TreeSet
	    //使用该TreeMap的key来保存Set集合的元素
	  public TreeSet() {
	        this(new TreeMap<E,Object>());
	    }
	//以定制排序的方式创建一个新的TreeMap。根据该TreeMap创建一个TreeSet
	    //使用该TreeMap的key来保存set集合的元素
	  public TreeSet(Comparator<? super E> comparator) {
	        this(new TreeMap<>(comparator));
	    }
	  // 创建TreeSet，并将集合c中的全部元素都添加到TreeSet中
	  public TreeSet(Collection<? extends E> c) {
	        this();
	        addAll(c);  // 将集合c中的元素全部添加到TreeSet中
	    }
	  // 创建TreeSet，并将s中的全部元素都添加到TreeSet中
	  public TreeSet(SortedSet<E> s) {
	        this(s.comparator());
	        addAll(s);
	    }
	// 返回TreeSet的顺序排列的迭代器。
	    // 因为TreeSet时TreeMap实现的，所以这里实际上时返回TreeMap的“键集”对应的迭代器
	  public Iterator<E> iterator() {
	        return m.navigableKeySet().iterator();
	    }
	  // 返回TreeSet的逆序排列的迭代器。
	    // 因为TreeSet时TreeMap实现的，所以这里实际上时返回TreeMap的“键集”对应的迭代器
	  public Iterator<E> descendingIterator() {
	        return m.descendingKeySet().iterator();
	    }
	  //返回一个逆序的Set实际是通过逆序的TreeMap然后转变成TreeSet
	  public NavigableSet<E> descendingSet() {
	        return new TreeSet<>(m.descendingMap());
	    }
	// 返回TreeSet的大小
	  public int size() {
	        return m.size();
	    }
	  // 返回TreeSet是否为空
	  public boolean isEmpty() {
	        return m.isEmpty();
	    }
	// 返回TreeSet是否包含对象(o)
	  public boolean contains(Object o) {
	        return m.containsKey(o);
	    }
	  // 添加e到TreeSet中
	  public boolean add(E e) {
	        return m.put(e, PRESENT)==null;
	    }
	// 删除TreeSet中的对象o
	  public boolean remove(Object o) {
	        return m.remove(o)==PRESENT;
	    }
	// 清空TreeSet
	  public void clear() {
	        m.clear();
	    }
	  // 将集合c中的全部元素添加到TreeSet中
	  public  boolean addAll(Collection<? extends E> c) {
	        // Use linear-time version if applicable
	        if (m.size()==0 && c.size() > 0 &&
	            c instanceof SortedSet &&
	            m instanceof TreeMap) {
	        	//把C集合强制转换为SortedSet集合
	            SortedSet<? extends E> set = (SortedSet<? extends E>) c;
	            //把m集合强制转换为TreeMap集合
	            TreeMap<E,Object> map = (TreeMap<E, Object>) m;
	            Comparator<?> cc = set.comparator();
	            Comparator<? super E> mc = map.comparator();
	          //如果cc和mc两个Comparator相等
	            if (cc==mc || (cc != null && cc.equals(mc))) {
	            	//把Collection中所有元素添加成TreeMap集合的key
	                map.addAllForTreeSet(set, PRESENT);
	                return true;
	            }
	        }
	        return super.addAll(c);
	    }
	  // 返回子Set，实际上是通过TreeMap的subMap()实现的。
	  public NavigableSet<E> subSet(E fromElement, boolean fromInclusive,
              E toElement,   boolean toInclusive) {
		  		 return new TreeSet<>(m.subMap(fromElement, fromInclusive,
                   toElement,   toInclusive));
	  }
	  // 返回Set的头部，范围是：从头部到toElement。
	    // inclusive是是否包含toElement的标志
	  public NavigableSet<E> headSet(E toElement, boolean inclusive) {
	        return new TreeSet<>(m.headMap(toElement, inclusive));
	    }

	    // 返回Set的尾部，范围是：从fromElement到结尾。
	    // inclusive是是否包含fromElement的标志
	  public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
	        return new TreeSet<>(m.tailMap(fromElement, inclusive));
	    }
	  // 返回子Set。范围是：从fromElement(包括)到toElement(不包括)。
	  public SortedSet<E> subSet(E fromElement, E toElement) {
	        return subSet(fromElement, true, toElement, false);
	    }
	// 返回Set的头部，范围是：从头部到toElement(不包括)
	  public SortedSet<E> headSet(E toElement) {
	        return headSet(toElement, false);
	    }
	  // 返回Set的尾部，范围是：从fromElement到结尾(不包括)。
	  public SortedSet<E> tailSet(E fromElement) {
	        return tailSet(fromElement, true);
	    }
	  // 返回Set的比较器
	    public Comparator<? super E> comparator() {
	        return m.comparator();
	    }
	 // 返回Set的第一个元素
	    public E first() {
	        return m.firstKey();
	    }
	    // 返回Set的最后一个元素
	    public E last() {
	        return m.lastKey();
	    }
	    // 返回Set中小于e的最大元素
	    public E lower(E e) {
	        return m.lowerKey(e);
	    }
	 // 返回Set中大于/等于e的最小元素
	    public E floor(E e) {
	        return m.floorKey(e);
	    }
	    // 返回Set中大于/等于e的最小元素
	    public E ceiling(E e) {
	        return m.ceilingKey(e);
	    }
	 //   返回Set中大于e的最小元素
	    public E higher(E e) {
	        return m.higherKey(e);
	    }
	    // 获取第一个元素，并将该元素从TreeMap中删除。
	    public E pollFirst() {
	        Map.Entry<E,?> e = m.pollFirstEntry();
	        return (e == null) ? null : e.getKey();
	    }
	    // 获取最后一个元素，并将该元素从TreeMap中删除。
	    public E pollLast() {
	        Map.Entry<E,?> e = m.pollLastEntry();
	        return (e == null) ? null : e.getKey();
	    }
	    // 克隆一个TreeSet，并返回Object对象
	    @SuppressWarnings("unchecked")
	    public Object clone() {
	        TreeSet<E> clone;
	        try {
	            clone = (TreeSet<E>) super.clone();
	        } catch (CloneNotSupportedException e) {
	            throw new InternalError(e);
	        }

	        clone.m = new TreeMap<>(m);
	        return clone;
	    }
	 // java.io.Serializable的写入函数
	    // 将TreeSet的“比较器、容量，所有的元素值”都写入到输出流中
	    private void writeObject(java.io.ObjectOutputStream s)
	            throws java.io.IOException {

	            s.defaultWriteObject();

	         // 写入比较器
	            s.writeObject(m.comparator());

	            // 写入容量
	            s.writeInt(m.size());

	         // 写入“TreeSet中的每一个元素”
	            for (E e : m.keySet())
	                s.writeObject(e);
	        }
	    // java.io.Serializable的读取函数：根据写入方式读出
	    // 先将TreeSet的“比较器、容量、所有的元素值”依次读出
	    private void readObject(java.io.ObjectInputStream s)
	            throws java.io.IOException, ClassNotFoundException {
	            // Read in any hidden stuff
	            s.defaultReadObject();

	            // 从输入流中读取TreeSet的“比较器”
	            @SuppressWarnings("unchecked")
	                Comparator<? super E> c = (Comparator<? super E>) s.readObject();

	            // Create backing TreeMap
	            TreeMap<E,Object> tm = new TreeMap<>(c);
	            m = tm;

	            // 从输入流中读取TreeSet的“容量”
	            int size = s.readInt();
	         // 从输入流中读取TreeSet的“全部元素”
	            tm.readTreeSet(size, s, PRESENT);
	        }
	    public Spliterator<E> spliterator() {
	        return TreeMap.keySpliteratorFor(m);
	    }

	    private static final long serialVersionUID = -2479143000061671589L;

}

package com.liu.List;

import java.util.AbstractSequentialList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;



/*LinkedList 是一个继承于AbstractSequentialList的双向链表。它也可以被当作堆栈、队列或双端队列进行操作。
LinkedList 实现 List 接口，能对它进行队列操作。
LinkedList 实现 Deque 接口，即能将LinkedList当作双端队列使用。
LinkedList 实现了Cloneable接口，即覆盖了函数clone()，能克隆。
LinkedList 实现java.io.Serializable接口，这意味着LinkedList支持序列化，能通过序列化去传输。
LinkedList 是非同步的。
*/
public class LinkedList<E>
	extends AbstractSequentialList<E>
	implements List<E>, Deque<E>, Cloneable, java.io.Serializable {
	//容量
	 transient int size = 0;
	//首节点
	 transient Node<E> first;
	//尾节点
	 transient Node<E> last;
	//默认构造函数
	 private static class Node<E> {
	        E item;
	        Node<E> next;
	        Node<E> prev;

	        Node(Node<E> prev, E element, Node<E> next) {
	            this.item = element;
	            this.next = next;
	            this.prev = prev;
	        }
	    }
	//默认构造函数
	 public LinkedList() {
	    }
	 //通过一个集合初始化LinkedList，元素顺序有这个集合的迭代器返回顺序决定
	 public LinkedList(Collection<? extends E> c) {
	        this();
	        addAll(c);
	    }
	//使用对应参数作为第一个节点，内部使用
	 private void linkFirst(E e) {	 	
	        final Node<E> f = first;//得到首节点，赋值给f节点
	        final Node<E> newNode = new Node<>(null, e, f);//创建一个节点，指向原来的首节点
	        first = newNode; //把新的节点放在第一个节点上
	        if (f == null)
	            last = newNode;//如果之前首节点为空(size==0)，那么尾节点就是首节点
	        else
	            f.prev = newNode; //如果之前首节点不为空，之前的首节点的前一个节点为当前首节点，回指节点
	        size++; //长度+1
	        modCount++; //修改次数+1
	    } 
	  //使用对应参数作为尾节点
	 void linkLast(E e) {
	        final Node<E> l = last; //得到尾节点
	        final Node<E> newNode = new Node<>(l, e, null);//使用参数创建一个节点
	        last = newNode; //设置尾节点
	        if (l == null)
	            first = newNode; //如果之前尾节点为空(size==0)，首节点即尾节点
	        else
	            l.next = newNode;//如果之前尾节点不为空，之前的尾节点的后一个就是当前的尾节点
	        size++;
	        modCount++;
	    }
	 //在指定节点前插入节点，节点succ不能为空
	 void linkBefore(E e, Node<E> succ) {
	        // assert succ != null;
	        final Node<E> pred = succ.prev;//获取前一个节点，放在节点pred
	        final Node<E> newNode = new Node<>(pred, e, succ);//新建节点，前指像pred，后指向指定的节点
	        succ.prev = newNode;//将原节点的前指指向新建的节点
	        if (pred == null)
	            first = newNode;//如果前一个节点为null，新的节点就是首节点
	        else
	            pred.next = newNode;//原节点的前节点的后指指向新的节点
	        size++;
	        modCount++;
	    }
	//删除首节点并返回删除前首节点的值，内部使用
	 private E unlinkFirst(Node<E> f) {
	        // assert f == first && f != null;
	        final E element = f.item;//获取首节点的值
	        final Node<E> next = f.next;//得到下一个节点
	        f.item = null;
	        f.next = null; //  //便于垃圾回收期清理
	        first = next; //首节点的下一个节点成为新的首节点
	        if (next == null)
	            last = null;  //如果不存在下一个节点，则首尾都为null(空表)
	        else
	            next.prev = null;//如果存在下一个节点，那它向前指向null
	        size--;
	        modCount++;
	        return element;
	    }
	 //删除尾节点并返回删除前尾节点的值，内部使用
	    private E unlinkLast(Node<E> l) {
	        final E element = l.item;//获取值
	        final Node<E> prev = l.prev;//获取尾节点前一个节点
	        l.item = null;
	        l.prev = null;      //便于垃圾回收期清理
	        last = prev;        //前一个节点成为新的尾节点
	        if (prev == null)
	            first = null;   //如果前一个节点不存在，则首尾都为null(空表)
	        else
	            prev.next = null;//如果前一个节点存在，先后指向null
	        size--;
	        modCount++;
	        return element;
	    }
	  //删除指定节点并返回被删除的元素值
	    E unlink(Node<E> x) {
	        //获取当前值和前后节点
	        final E element = x.item;
	        final Node<E> next = x.next;
	        final Node<E> prev = x.prev;
	        if (prev == null) {
	            first = next;   //如果前一个节点为空(如当前节点为首节点)，后一个节点成为新的首节点
	        } else {
	            prev.next = next;//如果前一个节点不为空，那么他先后指向当前的下一个节点
	            x.prev = null;  //方便gc回收
	        }
	        if (next == null) {
	            last = prev;    //如果后一个节点为空(如当前节点为尾节点)，当前节点前一个成为新的尾节点
	        } else {
	            next.prev = prev;//如果后一个节点不为空，后一个节点向前指向当前的前一个节点
	            x.next = null;  //方便gc回收
	        }
	        x.item = null;      //方便gc回收
	        size--;
	        modCount++;
	        return element;
	    }
	    //获取第一个元素
	    public E getFirst() {
	        final Node<E> f = first;//得到首节点
	        if (f == null)          //如果为空，抛出异常
	            throw new NoSuchElementException();
	        return f.item;
	    }
	    //获取最后一个元素
	    public E getLast() {
	        final Node<E> l = last;//得到尾节点
	        if (l == null)          //如果为空，抛出异常
	            throw new NoSuchElementException();
	        return l.item;
	    }
	    //删除第一个元素并返回删除的元素
	    public E removeFirst() {
	        final Node<E> f = first;//得到第一个节点
	        if (f == null)          //如果为空，抛出异常
	            throw new NoSuchElementException();
	        return unlinkFirst(f);
	    }
	    //删除最后一个元素并返回删除的值
	    public E removeLast() {
	        final Node<E> l = last;//得到最后一个节点
	        if (l == null)          //如果为空，抛出异常
	            throw new NoSuchElementException();
	        return unlinkLast(l);
	    }
	    //添加元素作为第一个元素
	    public void addFirst(E e) {
	        linkFirst(e);
	    }
	  //店家元素作为最后一个元素
	    public void addLast(E e) {
	        linkLast(e);
	    }
	    public boolean contains(Object o) {
	        return indexOf(o) != -1;//返回指定元素的索引位置，不存在就返回-1，然后比较返回bool值
	    }
	  //返回列表长度
	    public int size() {
	        return size;
	    }
	  //添加一个元素，默认添加到末尾作为最后一个元素
	    public boolean add(E e) {
	        linkLast(e);
	        return true;
	    }
	    public boolean remove(Object o) {
	        //会根据是否为null分开处理。若值不是null，会用到对象的equals()方法
	        if (o == null) {
	            for (Node<E> x = first; x != null; x = x.next) {
	                if (x.item == null) {
	                    unlink(x);
	                    return true;
	                }
	            }
	        } else {
	            for (Node<E> x = first; x != null; x = x.next) {
	                if (o.equals(x.item)) {
	                    unlink(x);
	                    return true;
	                }
	            }
	        }
	        return false;
	    }
	  //添加指定集合的元素到列表，默认从最后开始添加
	    public boolean addAll(Collection<? extends E> c) {
	        return addAll(size, c);//size表示最后一个位置，可以理解为元素的位置分别为1~size
	    }
	    //从指定位置（而不是下标！下标即索引从0开始，位置可以看做从1开始，其实也是0）后面添加指定集合的元素到列表中，只要有至少一次添加就会返回true
	    //index换成position应该会更好理解，所以也就是从索引为index(position)的元素的前面索引为index-1的后面添加！
	    //当然位置可以为0啊，为0的时候就是从位置0(虽然它不存在)后面开始添加嘛，所以理所当前就是添加到第一个位置（位置1的前面）的前面了啊！
	    //比如列表：0 1 2 3，如果此处index=4(实际索引为3)，就是在元素3后面添加；如果index=3(实际索引为2)，就在元素2后面添加。
	    //原谅我的表达水平，我已经尽力解释了...
	    public boolean addAll(int index, Collection<? extends E> c) {
	        checkPositionIndex(index);  //检查索引是否正确（0<=index<=size）
	        Object[] a = c.toArray();   //得到元素数组
	        int numNew = a.length;      //得到元素个数
	        if (numNew == 0)            //若没有元素要添加，直接返回false
	            return false;
	        Node<E> pred, succ;
	        if (index == size) {    //如果是在末尾开始添加，当前节点后一个节点初始化为null，前一个节点为尾节点
	            succ = null;        //这里可以看做node(index)，不过index=size了（index最大只能是size-1），所以这里的succ只能=null，也方便后面判断
	            pred = last;        //这里看做noede(index-1)，当然实现是不能这么写的，看做这样只是为了好理解，所以就是在node(index-1的后面开始添加元素)
	        } else {                //如果不是从末尾开始添加，当前位置的节点为指定位置的节点，前一个节点为要添加的节点的前一个节点
	            succ = node(index); //添加好元素后(整个新加的)的后一个节点
	            pred = succ.prev;   //这里依然是node(index-1)
	        }
	        //遍历数组并添加到列表中
	        for (Object o : a) {
	            @SuppressWarnings("unchecked")
	            E e = (E) o;
	            Node<E> newNode = new Node<>(pred, e, null);//创建一个节点，向前指向上面得到的前节点
	            if (pred == null)
	                first = newNode;    //若果前节点为null，则新加的节点为首节点
	            else
	                pred.next = newNode;//如果存在前节点，前节点会向后指向新加的节点
	            pred = newNode;         //新加的节点成为前一个节点
	        }
	        if (succ == null) {
	            //pred.next = null  //加上这句也可以更好的理解
	            last = pred;        //如果是从最后开始添加的，则最后添加的节点成为尾节点
	        } else {
	            pred.next = succ;   //如果不是从最后开始添加的，则最后添加的节点向后指向之前得到的后续第一个节点
	            succ.prev = pred;   //当前，后续的第一个节点也应改为向前指向最后一个添加的节点
	        }
	        size += numNew;
	        modCount++;
	        return true;
	    }
	    //获取指定索引的节点的值
	    public E get(int index) {
	        checkElementIndex(index);
	        return node(index).item;
	    }
	  //修改指定索引的值并返回之前的值
	    public E set(int index, E element) {
	        checkElementIndex(index);
	        Node<E> x = node(index);
	        E oldVal = x.item;
	        x.item = element;
	        return oldVal;
	    }
	    //指定位置后面（即索引为这个值的元素的前面）添加元素
	    public void add(int index, E element) {
	        checkPositionIndex(index);
	        if (index == size)
	            linkLast(element);  //如果指定位置为最后，则添加到链表最后
	        else                    //如果指定位置不是最后，则添加到指定位置前
	            linkBefore(element, node(index));
	    }
	  //删除指定位置的元素，
	    public E remove(int index) {
	        checkElementIndex(index);
	        return unlink(node(index));
	    }
	  //检查索引是否超出范围，因为元素索引是0~size-1的，所以index必须满足0<=index<size
	    private boolean isElementIndex(int index) {
	        return index >= 0 && index < size;
	    }
	    //检查位置是否超出范围，index必须在index~size之间（含），如果超出，返回false
	    private boolean isPositionIndex(int index) {
	        return index >= 0 && index <= size;
	    }
	    //异常详情
	    private String outOfBoundsMsg(int index) {
	        return "Index: "+index+", Size: "+size;
	    }
	  //检查元素索引是否超出范围，若已超出，就抛出异常
	    private void checkElementIndex(int index) {
	        if (!isElementIndex(index))
	            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
	    }
	  //检查位置是否超出范围，若已超出，就抛出异常
	    private void checkPositionIndex(int index) {
	        if (!isPositionIndex(index))
	            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
	    }
	    //获取指定位置的节点
	    Node<E> node(int index) {
	        //如果位置索引小于列表长度的一半(或一半减一)，从前面开始遍历；否则，从后面开始遍历
	        if (index < (size >> 1)) {
	            Node<E> x = first;//index==0时不会循环，直接返回first
	            for (int i = 0; i < index; i++)
	                x = x.next;
	            return x;
	        } else {
	            Node<E> x = last;
	            for (int i = size - 1; i > index; i--)
	                x = x.prev;
	            return x;
	        }
	    }
	  //获取指定元素从first开始最后出现的索引，不存在就返回-1
	    //但实际查找是从last开始的
	    public int lastIndexOf(Object o) {
	        int index = size;
	        if (o == null) {
	            for (Node<E> x = last; x != null; x = x.prev) {
	                index--;
	                if (x.item == null)
	                    return index;
	            }
	        } else {
	            for (Node<E> x = last; x != null; x = x.prev) {
	                index--;
	                if (o.equals(x.item))
	                    return index;
	            }
	        }
	        return -1;
	    }
	    //提供普通队列和双向队列的功能，当然，也可以实现栈，FIFO，FILO
	    //出队（从前端），获得第一个元素，不存在会返回null，不会删除元素（节点）
	    public E peek() {
	        final Node<E> f = first;
	        return (f == null) ? null : f.item;
	    }
	  //出队（从前端），不删除元素，若为null会抛出异常而不是返回null
	    public E element() {
	        return getFirst();
	    }
	    //出队（从前端），如果不存在会返回null，存在的话会返回值并移除这个元素（节点）
	    public E poll() {
	        final Node<E> f = first;
	        return (f == null) ? null : unlinkFirst(f);
	    }
	  //出队（从前端），如果不存在会抛出异常而不是返回null，存在的话会返回值并移除这个元素（节点）
	    public E remove() {
	        return removeFirst();
	    }
	    //入队（从后端），始终返回true
	    public boolean offer(E e) {
	        return add(e);
	    }
	  //入队（从前端），始终返回true
	    public boolean offerFirst(E e) {
	        addFirst(e);
	        return true;
	    }
	    //入队（从后端），始终返回true
	    public boolean offerLast(E e) {
	        addLast(e);//linkLast(e)
	        return true;
	    }
	    //出队（从前端），获得第一个元素，不存在会返回null，不会删除元素（节点）
	    public E peekFirst() {
	        final Node<E> f = first;
	        return (f == null) ? null : f.item;
	     }
	    //出队（从后端），获得最后一个元素，不存在会返回null，不会删除元素（节点）
	    public E peekLast() {
	        final Node<E> l = last;
	        return (l == null) ? null : l.item;
	    }
	    //出队（从前端），获得第一个元素，不存在会返回null，会删除元素（节点）
	    public E pollFirst() {
	        final Node<E> f = first;
	        return (f == null) ? null : unlinkFirst(f);
	    }
	  //出队（从后端），获得最后一个元素，不存在会返回null，会删除元素（节点）
	    public E pollLast() {
	        final Node<E> l = last;
	        return (l == null) ? null : unlinkLast(l);
	    }
	  //入栈，从前面添加
	    public void push(E e) {
	        addFirst(e);
	    }
	  //出栈，返回栈顶元素，从前面移除（会删除）
	    public E pop() {
	        return removeFirst();
	    }
	    public boolean removeFirstOccurrence(Object o) {
	        return remove(o);
	    }
	    /**
	     * Removes the last occurrence of the specified element in this
	     * list (when traversing the list from head to tail).  If the list
	     * does not contain the element, it is unchanged.
	     *
	     * @param o element to be removed from this list, if present
	     * @return {@code true} if the list contained the specified element
	     * @since 1.6
	     */
	    public boolean removeLastOccurrence(Object o) {
	        if (o == null) {
	            for (Node<E> x = last; x != null; x = x.prev) {
	                if (x.item == null) {
	                    unlink(x);
	                    return true;
	                }
	            }
	        } else {
	            for (Node<E> x = last; x != null; x = x.prev) {
	                if (o.equals(x.item)) {
	                    unlink(x);
	                    return true;
	                }
	            }
	        }
	        return false;
	    }
	    public ListIterator<E> listIterator(int index) {
	        checkPositionIndex(index);
	        return new ListItr(index);
	    }
	    private class ListItr implements ListIterator<E> {
	    	// 最近一次返回的节点，也是当前持有的节点
	        private Node<E> lastReturned;
	        // 对下一个元素的引用
	        private Node<E> next;
	        // 下一个节点的index
	        private int nextIndex;
	        private int expectedModCount = modCount;
	        // 构造方法，接收一个index参数，返回一个ListItr对象
	        ListItr(int index) {
	            // assert isPositionIndex(index);
	            next = (index == size) ? null : node(index);
	            nextIndex = index;
	        }
	        // 根据nextIndex是否等于size判断时候还有下一个节点（也可以理解为是否遍历完了LinkedList）
	        public boolean hasNext() {
	            return nextIndex < size;
	        }
	     // 获取下一个元素
	        public E next() {
	            checkForComodification();
	            if (!hasNext())//不存在下一个节点，抛错
	                throw new NoSuchElementException();
	            lastReturned = next;// 设置最近一次返回的节点为next节点
	            next = next.next;// 将next“向后移动一位”
	            nextIndex++;
	            return lastReturned.item;// 返回lastReturned的元素
	        }
	        //判断前面是否还有前驱节点
	        public boolean hasPrevious() {
	            return nextIndex > 0;
	        }
	        //输出前一个节点
	        public E previous() {
	            checkForComodification();
	            if (!hasPrevious())
	                throw new NoSuchElementException();
	            lastReturned = next = (next == null) ? last : next.prev;
	            nextIndex--;
	            return lastReturned.item;
	        }
	        //接下来的位置
	        public int nextIndex() {
	            return nextIndex;
	        }
	        //前一个位置
	        public int previousIndex() {
	            return nextIndex - 1;
	        }
	     // 移除当前Iterator持有的节点
	        public void remove() {
	            checkForComodification();
	            if (lastReturned == null)
	                throw new IllegalStateException();
	            Node<E> lastNext = lastReturned.next;
	            unlink(lastReturned);
	            if (next == lastReturned)
	                next = lastNext;
	            else
	                nextIndex--;
	            lastReturned = null;
	            expectedModCount++;
	        }
	        // 修改当前节点的内容
	        public void set(E e) {
	            if (lastReturned == null)
	                throw new IllegalStateException();
	            checkForComodification();
	            lastReturned.item = e;
	        }
	        // 在当前持有节点后面插入新节点
	        public void add(E e) {
	            checkForComodification();
	            lastReturned = null;
	            if (next == null)
	                linkLast(e);
	            else
	                linkBefore(e, next);
	            nextIndex++;
	            expectedModCount++;
	        }
	        public void forEachRemaining(Consumer<? super E> action) {
	            Objects.requireNonNull(action);
	            while (modCount == expectedModCount && nextIndex < size) {
	                action.accept(next.item);
	                lastReturned = next;
	                next = next.next;
	                nextIndex++;
	            }
	            checkForComodification();
	        }
	        final void checkForComodification() {
	            if (modCount != expectedModCount)
	                throw new ConcurrentModificationException();
	        }
	    }
	  //返回迭代器
	    public Iterator<E> descendingIterator() {
	        return new DescendingIterator();
	    }
	    //因为采用链表实现，所以迭代器很简单,这是个方向迭代器
	    private class DescendingIterator implements Iterator<E> {
	        private final ListItr itr = new ListItr(size());
	        public boolean hasNext() {
	            return itr.hasPrevious();
	        }
	        public E next() {
	            return itr.previous();
	        }
	        public void remove() {
	            itr.remove();
	        }
	    }
	    @SuppressWarnings("unchecked")
	    private LinkedList<E> superClone() {
	        try {
	            return (LinkedList<E>) super.clone();
	        } catch (CloneNotSupportedException e) {
	            throw new InternalError(e);
	        }
	    }
	    // 调用父类的clone()方法初始化对象链表clone，将clone构造成一个空的双向循环链表
	    //，之后将header的下一个节点开始将逐个节点添加到clone中。最后返回克隆的clone对象
	    public Object clone() {
	        LinkedList<E> clone = superClone();
	        // Put clone into "virgin" state
	        clone.first = clone.last = null;
	        clone.size = 0;
	        clone.modCount = 0;
	        // Initialize clone with our elements
	        for (Node<E> x = first; x != null; x = x.next)
	            clone.add(x.item);
	        return clone;
	    }
	    //创建大小和LinkedList相等的数组result，遍历链表，将每个节点的元素element复制到数组中，返回数组。
	    public Object[] toArray() {
	        Object[] result = new Object[size];
	        int i = 0;
	        for (Node<E> x = first; x != null; x = x.next)
	            result[i++] = x.item;
	        return result;
	    }
	    @SuppressWarnings("unchecked")
	    /*先判断出入的数组a的大小是否足够，若大小不够则拓展。这里用到了发射的方法，重新实例化了一个大小为size的数组。之后将数组a赋值给数组result，遍历链表向result中添加的元素。
	    最后判断数组a的长度是否大于size，若大于则将size位置的内容设置为null。返回a。
	    从代码中可以看出，数组a的length小于等于size时，a中所有元素被覆盖，被拓展来的空间存储的内容都是null；
	    若数组a的length的length大于size，则0至size-1位置的内容被覆盖，size位置的元素被设置为null，size之后的元素不变。*/
	    public <T> T[] toArray(T[] a) {
	        if (a.length < size)
	            a = (T[])java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
	        int i = 0;
	        Object[] result = a;
	        for (Node<E> x = first; x != null; x = x.next)
	            result[i++] = x.item;
	        if (a.length > size)
	            a[size] = null;
	        return a;
	    }
	    private static final long serialVersionUID = 876323262645176354L;
	    private void writeObject(java.io.ObjectOutputStream s)
	            throws java.io.IOException {
	            // Write out any hidden serialization magic
	            s.defaultWriteObject();
	            // Write out size
	            s.writeInt(size);
	            // Write out all elements in the proper order.
	            for (Node<E> x = first; x != null; x = x.next)
	                s.writeObject(x.item);
	        }
	    @SuppressWarnings("unchecked")
	    private void readObject(java.io.ObjectInputStream s)
	        throws java.io.IOException, ClassNotFoundException {
	        // Read in any hidden serialization magic
	        s.defaultReadObject();
	        // Read in size
	        int size = s.readInt();
	        // Read in all elements in the proper order.
	        for (int i = 0; i < size; i++)
	            linkLast((E)s.readObject());
	    }
	    @Override
	    public Spliterator<E> spliterator() {
	        return new LLSpliterator<E>(this, -1, 0);
	    }
	    static final class LLSpliterator<E> implements Spliterator<E> {
	        static final int BATCH_UNIT = 1 << 10;  // batch array size increment
	        static final int MAX_BATCH = 1 << 25;  // max batch array size;
	        final LinkedList<E> list; // null OK unless traversed
	        Node<E> current;      // current node; null until initialized
	        int est;              // size estimate; -1 until first needed
	        int expectedModCount; // initialized when est set
	        int batch;            // batch size for splits
	        LLSpliterator(LinkedList<E> list, int est, int expectedModCount) {
	            this.list = list;
	            this.est = est;
	            this.expectedModCount = expectedModCount;
	        }
	        final int getEst() {
	            int s; // force initialization
	            final LinkedList<E> lst;
	            if ((s = est) < 0) {
	                if ((lst = list) == null)
	                    s = est = 0;
	                else {
	                    expectedModCount = lst.modCount;
	                    current = lst.first;
	                    s = est = lst.size;
	                }
	            }
	            return s;
	        }
	        public long estimateSize() { return (long) getEst(); }
	        public Spliterator<E> trySplit() {
	            Node<E> p;
	            int s = getEst();
	            if (s > 1 && (p = current) != null) {
	                int n = batch + BATCH_UNIT;
	                if (n > s)
	                    n = s;
	                if (n > MAX_BATCH)
	                    n = MAX_BATCH;
	                Object[] a = new Object[n];
	                int j = 0;
	                do { a[j++] = p.item; } while ((p = p.next) != null && j < n);
	                current = p;
	                batch = j;
	                est = s - j;
	                return Spliterators.spliterator(a, 0, j, Spliterator.ORDERED);
	            }
	            return null;
	        }
	        public void forEachRemaining(Consumer<? super E> action) {
	            Node<E> p; int n;
	            if (action == null) throw new NullPointerException();
	            if ((n = getEst()) > 0 && (p = current) != null) {
	                current = null;
	                est = 0;
	                do {
	                    E e = p.item;
	                    p = p.next;
	                    action.accept(e);
	                } while (p != null && --n > 0);
	            }
	            if (list.modCount != expectedModCount)
	                throw new ConcurrentModificationException();
	        }
	        public boolean tryAdvance(Consumer<? super E> action) {
	            Node<E> p;
	            if (action == null) throw new NullPointerException();
	            if (getEst() > 0 && (p = current) != null) {
	                --est;
	                E e = p.item;
	                current = p.next;
	                action.accept(e);
	                if (list.modCount != expectedModCount)
	                    throw new ConcurrentModificationException();
	                return true;
	            }
	            return false;
	        }
	        public int characteristics() {
	            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
	        }
	    }
	
}

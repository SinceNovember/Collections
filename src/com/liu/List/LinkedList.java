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



/*LinkedList ��һ���̳���AbstractSequentialList��˫��������Ҳ���Ա�������ջ�����л�˫�˶��н��в�����
LinkedList ʵ�� List �ӿڣ��ܶ������ж��в�����
LinkedList ʵ�� Deque �ӿڣ����ܽ�LinkedList����˫�˶���ʹ�á�
LinkedList ʵ����Cloneable�ӿڣ��������˺���clone()���ܿ�¡��
LinkedList ʵ��java.io.Serializable�ӿڣ�����ζ��LinkedList֧�����л�����ͨ�����л�ȥ���䡣
LinkedList �Ƿ�ͬ���ġ�
*/
public class LinkedList<E>
	extends AbstractSequentialList<E>
	implements List<E>, Deque<E>, Cloneable, java.io.Serializable {
	//����
	 transient int size = 0;
	//�׽ڵ�
	 transient Node<E> first;
	//β�ڵ�
	 transient Node<E> last;
	//Ĭ�Ϲ��캯��
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
	//Ĭ�Ϲ��캯��
	 public LinkedList() {
	    }
	 //ͨ��һ�����ϳ�ʼ��LinkedList��Ԫ��˳����������ϵĵ���������˳�����
	 public LinkedList(Collection<? extends E> c) {
	        this();
	        addAll(c);
	    }
	//ʹ�ö�Ӧ������Ϊ��һ���ڵ㣬�ڲ�ʹ��
	 private void linkFirst(E e) {	 	
	        final Node<E> f = first;//�õ��׽ڵ㣬��ֵ��f�ڵ�
	        final Node<E> newNode = new Node<>(null, e, f);//����һ���ڵ㣬ָ��ԭ�����׽ڵ�
	        first = newNode; //���µĽڵ���ڵ�һ���ڵ���
	        if (f == null)
	            last = newNode;//���֮ǰ�׽ڵ�Ϊ��(size==0)����ôβ�ڵ�����׽ڵ�
	        else
	            f.prev = newNode; //���֮ǰ�׽ڵ㲻Ϊ�գ�֮ǰ���׽ڵ��ǰһ���ڵ�Ϊ��ǰ�׽ڵ㣬��ָ�ڵ�
	        size++; //����+1
	        modCount++; //�޸Ĵ���+1
	    } 
	  //ʹ�ö�Ӧ������Ϊβ�ڵ�
	 void linkLast(E e) {
	        final Node<E> l = last; //�õ�β�ڵ�
	        final Node<E> newNode = new Node<>(l, e, null);//ʹ�ò�������һ���ڵ�
	        last = newNode; //����β�ڵ�
	        if (l == null)
	            first = newNode; //���֮ǰβ�ڵ�Ϊ��(size==0)���׽ڵ㼴β�ڵ�
	        else
	            l.next = newNode;//���֮ǰβ�ڵ㲻Ϊ�գ�֮ǰ��β�ڵ�ĺ�һ�����ǵ�ǰ��β�ڵ�
	        size++;
	        modCount++;
	    }
	 //��ָ���ڵ�ǰ����ڵ㣬�ڵ�succ����Ϊ��
	 void linkBefore(E e, Node<E> succ) {
	        // assert succ != null;
	        final Node<E> pred = succ.prev;//��ȡǰһ���ڵ㣬���ڽڵ�pred
	        final Node<E> newNode = new Node<>(pred, e, succ);//�½��ڵ㣬ǰָ��pred����ָ��ָ���Ľڵ�
	        succ.prev = newNode;//��ԭ�ڵ��ǰָָ���½��Ľڵ�
	        if (pred == null)
	            first = newNode;//���ǰһ���ڵ�Ϊnull���µĽڵ�����׽ڵ�
	        else
	            pred.next = newNode;//ԭ�ڵ��ǰ�ڵ�ĺ�ָָ���µĽڵ�
	        size++;
	        modCount++;
	    }
	//ɾ���׽ڵ㲢����ɾ��ǰ�׽ڵ��ֵ���ڲ�ʹ��
	 private E unlinkFirst(Node<E> f) {
	        // assert f == first && f != null;
	        final E element = f.item;//��ȡ�׽ڵ��ֵ
	        final Node<E> next = f.next;//�õ���һ���ڵ�
	        f.item = null;
	        f.next = null; //  //������������������
	        first = next; //�׽ڵ����һ���ڵ��Ϊ�µ��׽ڵ�
	        if (next == null)
	            last = null;  //�����������һ���ڵ㣬����β��Ϊnull(�ձ�)
	        else
	            next.prev = null;//���������һ���ڵ㣬������ǰָ��null
	        size--;
	        modCount++;
	        return element;
	    }
	 //ɾ��β�ڵ㲢����ɾ��ǰβ�ڵ��ֵ���ڲ�ʹ��
	    private E unlinkLast(Node<E> l) {
	        final E element = l.item;//��ȡֵ
	        final Node<E> prev = l.prev;//��ȡβ�ڵ�ǰһ���ڵ�
	        l.item = null;
	        l.prev = null;      //������������������
	        last = prev;        //ǰһ���ڵ��Ϊ�µ�β�ڵ�
	        if (prev == null)
	            first = null;   //���ǰһ���ڵ㲻���ڣ�����β��Ϊnull(�ձ�)
	        else
	            prev.next = null;//���ǰһ���ڵ���ڣ��Ⱥ�ָ��null
	        size--;
	        modCount++;
	        return element;
	    }
	  //ɾ��ָ���ڵ㲢���ر�ɾ����Ԫ��ֵ
	    E unlink(Node<E> x) {
	        //��ȡ��ǰֵ��ǰ��ڵ�
	        final E element = x.item;
	        final Node<E> next = x.next;
	        final Node<E> prev = x.prev;
	        if (prev == null) {
	            first = next;   //���ǰһ���ڵ�Ϊ��(�統ǰ�ڵ�Ϊ�׽ڵ�)����һ���ڵ��Ϊ�µ��׽ڵ�
	        } else {
	            prev.next = next;//���ǰһ���ڵ㲻Ϊ�գ���ô���Ⱥ�ָ��ǰ����һ���ڵ�
	            x.prev = null;  //����gc����
	        }
	        if (next == null) {
	            last = prev;    //�����һ���ڵ�Ϊ��(�統ǰ�ڵ�Ϊβ�ڵ�)����ǰ�ڵ�ǰһ����Ϊ�µ�β�ڵ�
	        } else {
	            next.prev = prev;//�����һ���ڵ㲻Ϊ�գ���һ���ڵ���ǰָ��ǰ��ǰһ���ڵ�
	            x.next = null;  //����gc����
	        }
	        x.item = null;      //����gc����
	        size--;
	        modCount++;
	        return element;
	    }
	    //��ȡ��һ��Ԫ��
	    public E getFirst() {
	        final Node<E> f = first;//�õ��׽ڵ�
	        if (f == null)          //���Ϊ�գ��׳��쳣
	            throw new NoSuchElementException();
	        return f.item;
	    }
	    //��ȡ���һ��Ԫ��
	    public E getLast() {
	        final Node<E> l = last;//�õ�β�ڵ�
	        if (l == null)          //���Ϊ�գ��׳��쳣
	            throw new NoSuchElementException();
	        return l.item;
	    }
	    //ɾ����һ��Ԫ�ز�����ɾ����Ԫ��
	    public E removeFirst() {
	        final Node<E> f = first;//�õ���һ���ڵ�
	        if (f == null)          //���Ϊ�գ��׳��쳣
	            throw new NoSuchElementException();
	        return unlinkFirst(f);
	    }
	    //ɾ�����һ��Ԫ�ز�����ɾ����ֵ
	    public E removeLast() {
	        final Node<E> l = last;//�õ����һ���ڵ�
	        if (l == null)          //���Ϊ�գ��׳��쳣
	            throw new NoSuchElementException();
	        return unlinkLast(l);
	    }
	    //���Ԫ����Ϊ��һ��Ԫ��
	    public void addFirst(E e) {
	        linkFirst(e);
	    }
	  //���Ԫ����Ϊ���һ��Ԫ��
	    public void addLast(E e) {
	        linkLast(e);
	    }
	    public boolean contains(Object o) {
	        return indexOf(o) != -1;//����ָ��Ԫ�ص�����λ�ã������ھͷ���-1��Ȼ��ȽϷ���boolֵ
	    }
	  //�����б���
	    public int size() {
	        return size;
	    }
	  //���һ��Ԫ�أ�Ĭ����ӵ�ĩβ��Ϊ���һ��Ԫ��
	    public boolean add(E e) {
	        linkLast(e);
	        return true;
	    }
	    public boolean remove(Object o) {
	        //������Ƿ�Ϊnull�ֿ�������ֵ����null�����õ������equals()����
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
	  //���ָ�����ϵ�Ԫ�ص��б�Ĭ�ϴ����ʼ���
	    public boolean addAll(Collection<? extends E> c) {
	        return addAll(size, c);//size��ʾ���һ��λ�ã��������ΪԪ�ص�λ�÷ֱ�Ϊ1~size
	    }
	    //��ָ��λ�ã��������±꣡�±꼴������0��ʼ��λ�ÿ��Կ�����1��ʼ����ʵҲ��0���������ָ�����ϵ�Ԫ�ص��б��У�ֻҪ������һ����Ӿͻ᷵��true
	    //index����positionӦ�û������⣬����Ҳ���Ǵ�����Ϊindex(position)��Ԫ�ص�ǰ������Ϊindex-1�ĺ�����ӣ�
	    //��Ȼλ�ÿ���Ϊ0����Ϊ0��ʱ����Ǵ�λ��0(��Ȼ��������)���濪ʼ��������������ǰ������ӵ���һ��λ�ã�λ��1��ǰ�棩��ǰ���˰���
	    //�����б�0 1 2 3������˴�index=4(ʵ������Ϊ3)��������Ԫ��3������ӣ����index=3(ʵ������Ϊ2)������Ԫ��2������ӡ�
	    //ԭ���ҵı��ˮƽ�����Ѿ�����������...
	    public boolean addAll(int index, Collection<? extends E> c) {
	        checkPositionIndex(index);  //��������Ƿ���ȷ��0<=index<=size��
	        Object[] a = c.toArray();   //�õ�Ԫ������
	        int numNew = a.length;      //�õ�Ԫ�ظ���
	        if (numNew == 0)            //��û��Ԫ��Ҫ��ӣ�ֱ�ӷ���false
	            return false;
	        Node<E> pred, succ;
	        if (index == size) {    //�������ĩβ��ʼ��ӣ���ǰ�ڵ��һ���ڵ��ʼ��Ϊnull��ǰһ���ڵ�Ϊβ�ڵ�
	            succ = null;        //������Կ���node(index)������index=size�ˣ�index���ֻ����size-1�������������succֻ��=null��Ҳ��������ж�
	            pred = last;        //���￴��noede(index-1)����Ȼʵ���ǲ�����ôд�ģ���������ֻ��Ϊ�˺���⣬���Ծ�����node(index-1�ĺ��濪ʼ���Ԫ��)
	        } else {                //������Ǵ�ĩβ��ʼ��ӣ���ǰλ�õĽڵ�Ϊָ��λ�õĽڵ㣬ǰһ���ڵ�ΪҪ��ӵĽڵ��ǰһ���ڵ�
	            succ = node(index); //��Ӻ�Ԫ�غ�(�����¼ӵ�)�ĺ�һ���ڵ�
	            pred = succ.prev;   //������Ȼ��node(index-1)
	        }
	        //�������鲢��ӵ��б���
	        for (Object o : a) {
	            @SuppressWarnings("unchecked")
	            E e = (E) o;
	            Node<E> newNode = new Node<>(pred, e, null);//����һ���ڵ㣬��ǰָ������õ���ǰ�ڵ�
	            if (pred == null)
	                first = newNode;    //����ǰ�ڵ�Ϊnull�����¼ӵĽڵ�Ϊ�׽ڵ�
	            else
	                pred.next = newNode;//�������ǰ�ڵ㣬ǰ�ڵ�����ָ���¼ӵĽڵ�
	            pred = newNode;         //�¼ӵĽڵ��Ϊǰһ���ڵ�
	        }
	        if (succ == null) {
	            //pred.next = null  //�������Ҳ���Ը��õ����
	            last = pred;        //����Ǵ����ʼ��ӵģ��������ӵĽڵ��Ϊβ�ڵ�
	        } else {
	            pred.next = succ;   //������Ǵ����ʼ��ӵģ��������ӵĽڵ����ָ��֮ǰ�õ��ĺ�����һ���ڵ�
	            succ.prev = pred;   //��ǰ�������ĵ�һ���ڵ�ҲӦ��Ϊ��ǰָ�����һ����ӵĽڵ�
	        }
	        size += numNew;
	        modCount++;
	        return true;
	    }
	    //��ȡָ�������Ľڵ��ֵ
	    public E get(int index) {
	        checkElementIndex(index);
	        return node(index).item;
	    }
	  //�޸�ָ��������ֵ������֮ǰ��ֵ
	    public E set(int index, E element) {
	        checkElementIndex(index);
	        Node<E> x = node(index);
	        E oldVal = x.item;
	        x.item = element;
	        return oldVal;
	    }
	    //ָ��λ�ú��棨������Ϊ���ֵ��Ԫ�ص�ǰ�棩���Ԫ��
	    public void add(int index, E element) {
	        checkPositionIndex(index);
	        if (index == size)
	            linkLast(element);  //���ָ��λ��Ϊ�������ӵ��������
	        else                    //���ָ��λ�ò����������ӵ�ָ��λ��ǰ
	            linkBefore(element, node(index));
	    }
	  //ɾ��ָ��λ�õ�Ԫ�أ�
	    public E remove(int index) {
	        checkElementIndex(index);
	        return unlink(node(index));
	    }
	  //��������Ƿ񳬳���Χ����ΪԪ��������0~size-1�ģ�����index��������0<=index<size
	    private boolean isElementIndex(int index) {
	        return index >= 0 && index < size;
	    }
	    //���λ���Ƿ񳬳���Χ��index������index~size֮�䣨�������������������false
	    private boolean isPositionIndex(int index) {
	        return index >= 0 && index <= size;
	    }
	    //�쳣����
	    private String outOfBoundsMsg(int index) {
	        return "Index: "+index+", Size: "+size;
	    }
	  //���Ԫ�������Ƿ񳬳���Χ�����ѳ��������׳��쳣
	    private void checkElementIndex(int index) {
	        if (!isElementIndex(index))
	            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
	    }
	  //���λ���Ƿ񳬳���Χ�����ѳ��������׳��쳣
	    private void checkPositionIndex(int index) {
	        if (!isPositionIndex(index))
	            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
	    }
	    //��ȡָ��λ�õĽڵ�
	    Node<E> node(int index) {
	        //���λ������С���б��ȵ�һ��(��һ���һ)����ǰ�濪ʼ���������򣬴Ӻ��濪ʼ����
	        if (index < (size >> 1)) {
	            Node<E> x = first;//index==0ʱ����ѭ����ֱ�ӷ���first
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
	  //��ȡָ��Ԫ�ش�first��ʼ�����ֵ������������ھͷ���-1
	    //��ʵ�ʲ����Ǵ�last��ʼ��
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
	    //�ṩ��ͨ���к�˫����еĹ��ܣ���Ȼ��Ҳ����ʵ��ջ��FIFO��FILO
	    //���ӣ���ǰ�ˣ�����õ�һ��Ԫ�أ������ڻ᷵��null������ɾ��Ԫ�أ��ڵ㣩
	    public E peek() {
	        final Node<E> f = first;
	        return (f == null) ? null : f.item;
	    }
	  //���ӣ���ǰ�ˣ�����ɾ��Ԫ�أ���Ϊnull���׳��쳣�����Ƿ���null
	    public E element() {
	        return getFirst();
	    }
	    //���ӣ���ǰ�ˣ�����������ڻ᷵��null�����ڵĻ��᷵��ֵ���Ƴ����Ԫ�أ��ڵ㣩
	    public E poll() {
	        final Node<E> f = first;
	        return (f == null) ? null : unlinkFirst(f);
	    }
	  //���ӣ���ǰ�ˣ�����������ڻ��׳��쳣�����Ƿ���null�����ڵĻ��᷵��ֵ���Ƴ����Ԫ�أ��ڵ㣩
	    public E remove() {
	        return removeFirst();
	    }
	    //��ӣ��Ӻ�ˣ���ʼ�շ���true
	    public boolean offer(E e) {
	        return add(e);
	    }
	  //��ӣ���ǰ�ˣ���ʼ�շ���true
	    public boolean offerFirst(E e) {
	        addFirst(e);
	        return true;
	    }
	    //��ӣ��Ӻ�ˣ���ʼ�շ���true
	    public boolean offerLast(E e) {
	        addLast(e);//linkLast(e)
	        return true;
	    }
	    //���ӣ���ǰ�ˣ�����õ�һ��Ԫ�أ������ڻ᷵��null������ɾ��Ԫ�أ��ڵ㣩
	    public E peekFirst() {
	        final Node<E> f = first;
	        return (f == null) ? null : f.item;
	     }
	    //���ӣ��Ӻ�ˣ���������һ��Ԫ�أ������ڻ᷵��null������ɾ��Ԫ�أ��ڵ㣩
	    public E peekLast() {
	        final Node<E> l = last;
	        return (l == null) ? null : l.item;
	    }
	    //���ӣ���ǰ�ˣ�����õ�һ��Ԫ�أ������ڻ᷵��null����ɾ��Ԫ�أ��ڵ㣩
	    public E pollFirst() {
	        final Node<E> f = first;
	        return (f == null) ? null : unlinkFirst(f);
	    }
	  //���ӣ��Ӻ�ˣ���������һ��Ԫ�أ������ڻ᷵��null����ɾ��Ԫ�أ��ڵ㣩
	    public E pollLast() {
	        final Node<E> l = last;
	        return (l == null) ? null : unlinkLast(l);
	    }
	  //��ջ����ǰ�����
	    public void push(E e) {
	        addFirst(e);
	    }
	  //��ջ������ջ��Ԫ�أ���ǰ���Ƴ�����ɾ����
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
	    	// ���һ�η��صĽڵ㣬Ҳ�ǵ�ǰ���еĽڵ�
	        private Node<E> lastReturned;
	        // ����һ��Ԫ�ص�����
	        private Node<E> next;
	        // ��һ���ڵ��index
	        private int nextIndex;
	        private int expectedModCount = modCount;
	        // ���췽��������һ��index����������һ��ListItr����
	        ListItr(int index) {
	            // assert isPositionIndex(index);
	            next = (index == size) ? null : node(index);
	            nextIndex = index;
	        }
	        // ����nextIndex�Ƿ����size�ж�ʱ������һ���ڵ㣨Ҳ�������Ϊ�Ƿ��������LinkedList��
	        public boolean hasNext() {
	            return nextIndex < size;
	        }
	     // ��ȡ��һ��Ԫ��
	        public E next() {
	            checkForComodification();
	            if (!hasNext())//��������һ���ڵ㣬�״�
	                throw new NoSuchElementException();
	            lastReturned = next;// �������һ�η��صĽڵ�Ϊnext�ڵ�
	            next = next.next;// ��next������ƶ�һλ��
	            nextIndex++;
	            return lastReturned.item;// ����lastReturned��Ԫ��
	        }
	        //�ж�ǰ���Ƿ���ǰ���ڵ�
	        public boolean hasPrevious() {
	            return nextIndex > 0;
	        }
	        //���ǰһ���ڵ�
	        public E previous() {
	            checkForComodification();
	            if (!hasPrevious())
	                throw new NoSuchElementException();
	            lastReturned = next = (next == null) ? last : next.prev;
	            nextIndex--;
	            return lastReturned.item;
	        }
	        //��������λ��
	        public int nextIndex() {
	            return nextIndex;
	        }
	        //ǰһ��λ��
	        public int previousIndex() {
	            return nextIndex - 1;
	        }
	     // �Ƴ���ǰIterator���еĽڵ�
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
	        // �޸ĵ�ǰ�ڵ������
	        public void set(E e) {
	            if (lastReturned == null)
	                throw new IllegalStateException();
	            checkForComodification();
	            lastReturned.item = e;
	        }
	        // �ڵ�ǰ���нڵ��������½ڵ�
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
	  //���ص�����
	    public Iterator<E> descendingIterator() {
	        return new DescendingIterator();
	    }
	    //��Ϊ��������ʵ�֣����Ե������ܼ�,���Ǹ����������
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
	    // ���ø����clone()������ʼ����������clone����clone�����һ���յ�˫��ѭ������
	    //��֮��header����һ���ڵ㿪ʼ������ڵ���ӵ�clone�С���󷵻ؿ�¡��clone����
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
	    //������С��LinkedList��ȵ�����result������������ÿ���ڵ��Ԫ��element���Ƶ������У��������顣
	    public Object[] toArray() {
	        Object[] result = new Object[size];
	        int i = 0;
	        for (Node<E> x = first; x != null; x = x.next)
	            result[i++] = x.item;
	        return result;
	    }
	    @SuppressWarnings("unchecked")
	    /*���жϳ��������a�Ĵ�С�Ƿ��㹻������С��������չ�������õ��˷���ķ���������ʵ������һ����СΪsize�����顣֮������a��ֵ������result������������result����ӵ�Ԫ�ء�
	    ����ж�����a�ĳ����Ƿ����size����������sizeλ�õ���������Ϊnull������a��
	    �Ӵ����п��Կ���������a��lengthС�ڵ���sizeʱ��a������Ԫ�ر����ǣ�����չ���Ŀռ�洢�����ݶ���null��
	    ������a��length��length����size����0��size-1λ�õ����ݱ����ǣ�sizeλ�õ�Ԫ�ر�����Ϊnull��size֮���Ԫ�ز��䡣*/
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

package com.liu.List;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class myArraylisttest{
	public static void main(String[] args) {
//		UserLog userLog=new UserLog();
//		userLog.setUserid("123");
//		userLog.setId(1);
//		userLog.setDetail("1234");
//		String s1=new String();
//		s1="asdadsa";
//		String s2=new String();
//		s2="asddd";
//		String s3=new String();
//		s3="fsd";
//		Object[] strings=new String[10];
//		List<Object> list1=new myArraylist<Object>(10);
//		List<String> list2=new myArraylist<String>(10);
//		list2.add(s1);
//		list2.add(s2);
//		list2.add(s3);
//		list1.add(s1);
//		list1.add(s2);
//		list1.add(s3);
//		list1.addAll(list2);
//		list1.add(userLog);
//		System.out.println(list1.size());
//		UserLog userLog2;
//		Iterator<Object> iterator=list1.iterator();
//		while(iterator.hasNext())
//		{
//			Object s=iterator.next();
//			if(s.getClass().equals(userLog.getClass()))
//				{
//				userLog2=(UserLog) s;
//				System.out.println(userLog2.getDetail());
//				}
//				System.out.println(s.getClass());
//		}
//		list1.remove(2);
//		System.out.println(list1.size());
//		System.out.println(list1.indexOf("asddd"));
//		System.out.println(list1.get(0));
		LinkedList<String> linkedList=new LinkedList<String>();
		linkedList.add("aaa");
		linkedList.add("bbb");
		linkedList.add("ddd");
		linkedList.add("eee");
		System.out.println(linkedList.peek());
//		Iterator<String> iterator=linkedList.iterator();
//		while(iterator.hasNext())
//		{
//			iterator.
//			System.out.println(iterator.next());
//		}
		Iterator<String> iterator1=linkedList.descendingIterator();
		ListIterator<String> iterator=linkedList.listIterator();
		while(iterator1.hasNext())
			{
				System.out.println(iterator1.next());
			}
		while(iterator.hasNext())
		{
			System.out.println(iterator.next());
		}
//		System.out.println(iterator.next());
//			System.out.println(iterator.previous());
	
//		System.out.println(iterator.next());
//		iterator.set("xxx");
//		System.out.println(iterator.previous());
		
		
	}
}

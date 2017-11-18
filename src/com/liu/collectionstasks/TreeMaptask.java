package com.liu.collectionstasks;

import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class TreeMaptask {
	public static void main(String[] args) {
		TreeMap<String, String> treeMap=new TreeMap<String,String>();
		Scanner scanner=new Scanner(System.in);
		treeMap.put("2936430061000101", "谢伟");
		treeMap.put("2936430061000126", "向雪苗");
		treeMap.put("3536430061000101", "邱婉婷");
		Iterator<Map.Entry<String, String>> it=treeMap.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry<String, String> map=it.next();
			System.out.println(map.getKey());
			System.out.println(map.getValue());
		}
		while(true)
		{
		System.out.println("请输入学号:");
		String id=scanner.nextLine();
		
		if(treeMap.containsKey(id))
		{
			System.out.println("姓名:"+treeMap.get(id));
			break;
		}
		else
		{
			System.out.println("学号不存在,请重新输入!");
		}
		}
		
	}
}

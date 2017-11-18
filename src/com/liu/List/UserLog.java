package com.liu.List;

public class UserLog {
		private int id;      //日志编号
	    private String userid;  //用户名
	    private String time;    //时间
	    private String type;    //类型
	    private String detail;  //详情
	    private String ip;      //ip地址
		public int getId() {
			return id;
		}
		public void setId(Integer id) {
			this.id = id;
		}
		public String getUserid() {
			return userid;
		}
		public void setUserid(String userid) {
			this.userid = userid;
		}
		public String getTime() {
			return time;
		}
		public void setTime(String time) {
			this.time = time;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getDetail() {
			return detail;
		}
		public void setDetail(String detail) {
			this.detail = detail;
		}
		public String getIp() {
			return ip;
		}
		public void setIp(String ip) {
			this.ip = ip;
		}
	    
}

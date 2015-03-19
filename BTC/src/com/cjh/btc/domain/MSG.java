package com.cjh.btc.domain;

import android.annotation.SuppressLint;
import java.text.SimpleDateFormat;

public class MSG {

	private String content;
	private String time;
	private int isMine;//1是自己发送的消息，0是接收的消息
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public int isMine() {
		return isMine;
	}
	public void setMine(int isMine) {
		this.isMine = isMine;
	}
	@SuppressLint("SimpleDateFormat")
	public MSG(String content,int isMine) {
		this.content=content;
		this.isMine=isMine;
		SimpleDateFormat sDateFormat=new  SimpleDateFormat("yyyy-MM-dd   HH:MM:ss");
		time=sDateFormat.format(new   java.util.Date());
	}

}

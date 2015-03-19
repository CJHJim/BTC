/**
 * 
 */
package com.cjh.btc.domain;

/**
 * @author CJH
 *
 */
public class MSGListEnity {

	private String name;
	private String address;
	private String content;
	private String time;
	/**
	 * 
	 */
	public MSGListEnity(String name, String address,String content,String time) {
		this.name=name;
		this.address=address;
		this.content=content;
		this.time=time;
		// TODO Auto-generated constructor stub
	}
	
	public MSGListEnity(){
		
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}

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

}

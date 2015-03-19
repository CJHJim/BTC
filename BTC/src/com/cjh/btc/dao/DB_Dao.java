/**
 * 
 */
package com.cjh.btc.dao;

import java.util.ArrayList;
import java.util.List;

import com.cjh.btc.domain.DataBase;
import com.cjh.btc.domain.MSG;
import com.cjh.btc.domain.MSGListEnity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

/**
 * @author CJH
 *
 */

public class DB_Dao {

	private DataBase myBTCdata;
	/**
	 * 
	 */
	public DB_Dao(Context context) {
		myBTCdata=new DataBase(context);
	}
	
	
	
	
	public long addContact(String name,String address){
		//获得数据库写入权限
		SQLiteDatabase db=myBTCdata.getWritableDatabase();
		//利用系统api对数据库进行写操作（也可使用sql语句）
		ContentValues values=new ContentValues();
		values.put("cjh_name", name);
		values.put("cjh_MAC", address);
		
		long id=db.insert("cjh_contacts", null, values);
		//关闭
		Log.e("添加设备到数据库成功了吗？", Long.toString(id));
		db.close();
		return id;
	}

	/**
	 * 逐条添加聊天记录
	 * @param id
	 * @param msg
	 */
	public long addRecord(String Address,MSG msg){
		int id=getId(Address);
		SQLiteDatabase db=myBTCdata.getWritableDatabase();
		ContentValues values=new ContentValues();
		values.put("cjh_id", id);
		values.put("cjh_content", msg.getContent());
		values.put("cjh_time", msg.getTime());
		values.put("cjh_ismine", msg.isMine());
		System.out.println(msg.getContent()+"##"+msg.getTime()+"##"+msg.isMine());
		long r_id=db.insert("cjh_records", null, values);
		
		db.close();
		
		return r_id;
	}
	
	/**
	 * 获取与该设备的聊天记录
	 * @param Address
	 * @return 聊天记录
	 */
	public List<MSG> getMsgRecord(String Address){
		List<MSG> msgList=new ArrayList<MSG>();
		SQLiteDatabase db=myBTCdata.getReadableDatabase();
		int id=getId(Address);
		String idstr=Integer.toString(id);
		
		Cursor cursor=db.query("cjh_records", null, "cjh_id=?", new String[]{idstr}, null, null, null);
		
		while(cursor.moveToNext())
		{
			String content=cursor.getString(3);
			
			String time=cursor.getString(2);
			int ismine=cursor.getInt(4);
			System.out.println(content+"##"+time+"##"+ismine);
			MSG msg=new MSG(content,ismine);
			msg.setTime(time);
			msgList.add(msg);
			
		}
		db.close();
		cursor.close();
		return msgList;
	}
	
	/**
	 * @param Address
	 * @return r=0删除失败，否则成功
	 */
	public int deleteRecords(String Address){
		int id=getId(Address);
		int r=0;
		String idstr=Integer.toString(id);
		SQLiteDatabase db=myBTCdata.getWritableDatabase();
		if(db.inTransaction())
			db.endTransaction();
		db.beginTransaction();
		try{
		r=db.delete("cjh_msgList", "cjh_MAC=?", new String[]{Address});
		r=db.delete("cjh_records", "cjh_id=?", new String[]{idstr});
		db.setTransactionSuccessful();
		}
		catch (SQLException e) {  
            System.out.println("删除数据库失败");
		}
		finally
		{
			db.endTransaction();
		}
		db.close();
		return r;
	}
	
	/**
	 * @param Address 设备地址
	 * @return 返回设备id
	 * 获取设备id
	 */
	public int getId(String Address){
		
        SQLiteDatabase db=myBTCdata.getReadableDatabase();
		Cursor cursor=db.query("cjh_contacts", null, "cjh_MAC=?", new String[]{Address}, null, null, null);
		cursor.moveToNext();
		int id=cursor.getInt(0);
		cursor.close();
		return id;
	}
	
	
	/**
	 * @param Address  设备地址
	 * @return 如果存在则返回true,否则false
	 * 查询是否存在这个设备
	 */
	public boolean QueryContact(String Address){
		SQLiteDatabase db=myBTCdata.getReadableDatabase();
		Cursor cursor=db.query("cjh_contacts", null, "cjh_MAC=?", new String[]{Address}, null, null, null);
		boolean result=cursor.moveToNext();
		cursor.close();
		db.close();
		return result;
		
		
	}
	
	/**
	 * @param Address
	 * @return true 删除成功，否则失败
	 * 因为两张表的关联性，鉴于数据库的完整性约束，所以在删除某个联系人的同时，还要删除其在cjh_records表里面的聊天记录
	 */
	public boolean deleteContact(String Address){ 
		int r1=0,r2=0;
		boolean result=QueryContact(Address);;
		if(false==result)
			{r1=1;r2=1;}
		else
		{
			SQLiteDatabase db=myBTCdata.getWritableDatabase();
			//db.execSQL("DELETE FROM　cjh_records WHERE cjh_id="+id);
			//由于外键约束所以先删除聊天记录
			r1=deleteRecords(Address);
			r2=db.delete("cjh_contacts", "cjh_MAC=?", new String[]{Address});
			db.close();
			
		}
		if(r1!=0&&r2!=0)
		   return false;
		return true;
				
	}
	
	/**
	 * 查询是否存在该设备的聊天记录
	 * @param Address
	 * @return
	 */
	public boolean isRecordExist(String Address){
		boolean result=false;
		
		int id=getId(Address);
		SQLiteDatabase db=myBTCdata.getReadableDatabase();
		Cursor cursor=db.query("cjh_records", null, "cjh_id=?", new String[]{Integer.toString(id)}, null, null, null);
		result=cursor.moveToNext();
		cursor.close();
		db.close();
		return result;
		
	}
	
	public List<MSGListEnity> getMsgList(){
		List<MSGListEnity> msgList=new ArrayList<MSGListEnity>();
		List<String> idlist=new ArrayList<String>();
		String[] searchC={"cjh_id","cjh_content","cjh_time"};
		SQLiteDatabase db=myBTCdata.getReadableDatabase();
		Cursor cursor=db.query("cjh_msgList", null, null,null, null, null, null);
		//cursor.moveToFirst();
		while (cursor.moveToNext())
			idlist.add(Integer.toString(cursor.getInt(0)));
		String[] idlistStr=idlist.toArray(new String[idlist.size()]);
		for(String x:idlistStr){
			cursor=db.query("cjh_records", searchC, "r_id=?", new String[]{x}, null, null, null);
			if(cursor.moveToNext()){
			MSGListEnity temp=new MSGListEnity();
			String cjh_id=cursor.getString(0);
			temp.setContent(cursor.getString(1));
			temp.setTime(cursor.getString(2));
			cursor=db.query("cjh_contacts", new String[]{"cjh_name","cjh_MAC"},"cjh_id=?", new String[]{cjh_id}, null, null, null);
			if(cursor.moveToNext()){
			temp.setName(cursor.getString(0));
			temp.setAddress(cursor.getString(1));
			}
			else
				break;
			msgList.add(temp);
			}

		}
		cursor.close();
		db.close();
		return msgList;
	}
	
	public long addLastMsgId(long id,String address){
		SQLiteDatabase db=myBTCdata.getReadableDatabase();
		ContentValues values=new ContentValues();
		Cursor cursor=db.query("cjh_msgList", null, "cjh_MAC=?", new String[]{address}, null, null, null);
		if(cursor.moveToNext())
			db.delete("cjh_msgList", "cjh_MAC=?",new String[]{address} );
		values.put("r_id", id);
		values.put("cjh_MAC", address);
		long i=db.insert("cjh_msgList", null, values);
		cursor.close();
		db.close();
		
		return i;
	}
	
	
	
}

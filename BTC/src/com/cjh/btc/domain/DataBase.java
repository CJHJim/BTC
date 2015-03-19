/**
 * 建立数据库，存储消息和联系人
 */
package com.cjh.btc.domain;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author CJH
 *
 */
public class DataBase extends SQLiteOpenHelper {

	
	/**
	 * @param context
	 * @param name
	 * @param factory
	 * @param version
	 */
	public DataBase(Context context) {
		super(context, "BTC_data", null, 1);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase BTCdb) {
		// TODO Auto-generated method stub

		BTCdb.execSQL(
				"CREATE TABLE cjh_contacts"
		         +"("
		         +"cjh_id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, "//主键，自增长
		         +"cjh_MAC VARCHAR(18),"
				 +"cjh_name VARCHAR(20)"
				 +")"
		 		 );
		BTCdb.execSQL(
				"CREATE TABLE cjh_records"
				+"("		
				+" r_id INTEGER PRIMARY KEY,"
				+" cjh_id INTEGER NOT NULL,"
				+" cjh_time TIMESTAMP,"
				+" cjh_content TEXT,"
		        +" cjh_ismine INTEGER CHECK(cjh_ismine>=0 AND cjh_ismine <=1),"
		        +" CONSTRAINT fk FOREIGN KEY(cjh_id) REFERENCES cjh_contacts"
				+")"
		        );
		
		BTCdb.execSQL(
				"CREATE TABLE cjh_msgList"
				+"("
				+"r_id INTEGER,"
				+"cjh_MAC TEXT UNIQUE PRIMARY KEY,"
				+"CONSTRAINT FK_msgList FOREIGN KEY(r_id) REFERENCES cjh_records,"
				+"CONSTRAINT FK_ID FOREIGN KEY(cjh_MAC) REFERENCES cjh_contacts"
				+")"
				);
		}

	/* (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}

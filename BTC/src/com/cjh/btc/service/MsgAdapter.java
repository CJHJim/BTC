/**
 * 
 */
package com.cjh.btc.service;

import java.util.List;

import com.cjh.btc.domain.MSG;
import com.cjh.btc.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * @author CJH
 *
 */
public class MsgAdapter extends BaseAdapter {

	private List<MSG> msgList;
	private LayoutInflater myInflater;
	private Context context;
	private int isMine;
	/**
	 * 
	 */
	public MsgAdapter(Context context,List<MSG> msgList) {
		this.context=context;
		myInflater=LayoutInflater.from(context);
		this.msgList=msgList;
	}

	public static interface IMsgViewType {
		int  YOURS= 0;
		int MINE = 1;
	}
	public int getItemViewType(int position) {
		MSG entity = msgList.get(position);

		if (entity.isMine()==0) {
			return IMsgViewType.YOURS;
		} else {
			return IMsgViewType.MINE;
		}

	}
	
	public int getViewTypeCount() {
		return 2;
	}
	@Override
	public int getCount() {
		return msgList.size();
	}

	@Override
	public Object getItem(int position) {
		return msgList.get(position);
	}

	
	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MSG msg=msgList.get(position);
		isMine=msg.isMine();
		System.out.println("ÏûÏ¢ÄÚÈÝ£º"+msg.getContent()+"##isMine##"+isMine);
		ViewHolder viewHolder = null;
		if(convertView==null){
			if(isMine==1)
				convertView = myInflater.inflate(R.layout.chatting_item_msg_text_right, null);
			else
				convertView=myInflater.inflate(R.layout.chatting_item_msg_text_left, null);
			viewHolder=new ViewHolder();
			viewHolder.tvSendTime = (TextView) convertView
					.findViewById(R.id.tv_sendtime);
			viewHolder.tvContent = (TextView) convertView
					.findViewById(R.id.tv_chatcontent);
			viewHolder.isMine=isMine;
			convertView.setTag(viewHolder);
		}
		else
			viewHolder = (ViewHolder) convertView.getTag();
		viewHolder.tvSendTime.setText(msg.getTime());
		viewHolder.tvContent.setText(msg.getContent());
		
		return convertView;
	}
	
	class ViewHolder {
		public TextView tvSendTime;
		public TextView tvContent;
		public int isMine = 1;
	}

}

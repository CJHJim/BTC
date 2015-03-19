/**
 * 
 */
package com.cjh.btc.service;

import java.util.List;

import com.cjh.btc.R;
import com.cjh.btc.domain.MSGListEnity;
import com.cjh.btc.service.MsgAdapter.ViewHolder;

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
public class MSGListAdapter extends BaseAdapter {

	
	private List<MSGListEnity> myMsglist;
	private LayoutInflater myInflater;
	private Context context;
	
	/**
	 * 
	 */
	public MSGListAdapter(Context context,List<MSGListEnity> myMsg_list) {
		this.context=context;
		this.myMsglist=myMsg_list;
		myInflater=LayoutInflater.from(context);
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		return myMsglist.size();
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		return myMsglist.get(position);
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		MSGListEnity mylist=myMsglist.get(position);
		
		ViewHolder myholder=null;
		
		if(convertView==null){
			convertView=myInflater.inflate(R.layout.lv_item, null);
			myholder=new ViewHolder();
			myholder.NA=(TextView) convertView.findViewById(R.id.tv_NA);
			myholder.content=(TextView) convertView.findViewById(R.id.tv_content);
			myholder.time=(TextView) convertView.findViewById(R.id.tv_time);
			convertView.setTag(myholder);
		}
		else
			myholder = (ViewHolder) convertView.getTag();
		myholder.NA.setText(mylist.getName()+mylist.getAddress());
		myholder.content.setText(mylist.getContent());
		myholder.time.setText(mylist.getTime().substring(5, 18));
		return convertView;
	}
	class ViewHolder {
		public TextView time;
		public TextView content;
		public TextView NA;
	}

}

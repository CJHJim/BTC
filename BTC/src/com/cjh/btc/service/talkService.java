/*
 * ���õ����������ģʽ
 */

package com.cjh.btc.service;


import java.io.IOException;


import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.cjh.btc.MainActivity;
import com.cjh.btc.Talk_Activity;
import com.cjh.btc.domain.MSG;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


public class talkService {

	
	
	private static final UUID MY_UUID =UUID.fromString("cf620832-f9d5-42c5-8e5f-6d72d60aa1dc");
	
	
	public final static int STATE_LOST=1;     //û�н����κβ���
	public final static int STATE_LISTEN=2;   //��Ϊ��������ڼ���
	public final static int STATE_CONNECTED=3;//�Ѿ�����
	public final static int STATE_CONNECTING=4;//������
	public final static int STATE_STOP=5;
	private int myState;//��ǰ״̬
	
	
	private final BluetoothAdapter myAdapter;//�û��Լ��������豸
	private static Handler myHandler;        //
	private AcceptRequestThread myAcceptThread;    //������Ϊ���������������߳�
    private ConnectRequestThread myConnectThread;  //������Ϊ�ͻ�������������߳�
    private talkingThread mytalkingThread;//���������ӵ��ŵ����߳�
	
	public talkService(Handler handler) {
		myAdapter=BluetoothAdapter.getDefaultAdapter();
		myState=STATE_LOST;
		myHandler=handler;
		
	}

	private static class talkServiceHolder{
		static final talkService INSTANCE=new talkService(myHandler);
	}
	
	public static talkService getInstance(Handler handler){
		talkServiceHolder.INSTANCE.setHandler(handler);
		return talkServiceHolder.INSTANCE;
		
	}
	public void setHandler(Handler handler){
		myHandler=handler;
	}
	
	public int getMyState() {
		return myState;
	}


	public synchronized void setMyState(int myState) {
		this.myState = myState;
	}


	public static UUID getMyUuid() {
		return MY_UUID;
	}

	
	
	 public synchronized void start() {
		 
	        // ȡ�����ڵ���������
	        if (myConnectThread != null) {
	        	myConnectThread.cancel();
	        	myConnectThread = null;
	        	}

	        // �Ͽ������Ѿ����ӵ�����
	        if (mytalkingThread != null) {
	        	mytalkingThread.cancel(); 
	        	mytalkingThread = null;
	        	}

	        // ��ʼһ���µļ���
	        if (myAcceptThread == null) {
	        	myAcceptThread = new AcceptRequestThread();
	        	myAcceptThread.start();
	        }
	        setMyState(STATE_LISTEN);

	        myHandler.obtainMessage(Talk_Activity.CONNECT_STATE, myState, -1).sendToTarget();
	    }
	 
	 
	    public synchronized void connect(BluetoothDevice device) {
	    	
	       
	        if (myState == STATE_CONNECTING) {
	            if (myConnectThread != null) {
	            	     myConnectThread.cancel();
	            	     myConnectThread = null;
	            	}
	        }

	        if(myAcceptThread!=null){
	        	myAcceptThread.cancel();
	        	myAcceptThread=null;
	        }
	        if (mytalkingThread != null) {
	        	mytalkingThread.cancel(); 
	        	mytalkingThread = null;
	        	}

	       
	        myConnectThread = new ConnectRequestThread(device);
	        Log.i("myConnectThread", "��ʼ���������߳�"+device.getName().toString());
	        myConnectThread.start();
	        
	        setMyState(STATE_CONNECTING);
	        myHandler.obtainMessage(Talk_Activity.CONNECT_STATE, myState, -1).sendToTarget();
	    }
	 
		public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {

			// ȡ�����ڵ���������
	        if (myConnectThread != null) {
	        	myConnectThread.cancel(); 
	        	myConnectThread = null;
	        	}

	        // �Ͽ������Ѿ����ӵ�����
	        if (mytalkingThread != null) {
	        	mytalkingThread.cancel();
	        	mytalkingThread = null;
	        	}

	        // ȡ��������������
	        if (myAcceptThread != null) {
	        	myAcceptThread.cancel(); 
	        	myAcceptThread = null;
	        	}

	        // ��ʼһ���µ�����
	        mytalkingThread = new talkingThread(socket);
	        mytalkingThread.start();
	        setMyState(STATE_CONNECTED);
	        myHandler.obtainMessage(Talk_Activity.CONNECT_STATE, myState, -1).sendToTarget();
	    }
		
		 public synchronized void stop() {
		        if (myConnectThread != null) {myConnectThread.cancel(); myConnectThread = null;}
		        if (mytalkingThread != null) {mytalkingThread.cancel(); mytalkingThread = null;}
		        if (myAcceptThread != null) {myAcceptThread.cancel(); myAcceptThread = null;}
		        setMyState(STATE_STOP);
		        myHandler.obtainMessage(Talk_Activity.CONNECT_STATE, myState, -1).sendToTarget();
		    }
		 
		 
		 private void connectionFailed() {
		        setMyState(STATE_LISTEN);
		        Message msg = myHandler.obtainMessage(Talk_Activity.MSG_TOAST);
		        Bundle bundle = new Bundle();
		        bundle.putString(Talk_Activity.TOAST, "��������ʧ��");
		        msg.setData(bundle);
		        myHandler.sendMessage(msg);
		        myHandler.obtainMessage(Talk_Activity.CONNECT_STATE, myState, -1).sendToTarget();
		    }
		 
		 
		 public void write(byte [] msg) {
			  
			    talkingThread temp;
			    
		        synchronized (this) {
		            if (myState != STATE_CONNECTED) 
		            	return;//�����ǰ״̬�����Ѿ����Ӿ�ֱ�ӽ�������
		            temp = mytalkingThread;
		            
		        }
			    temp.write(msg);
		    }
		  
		  private void connectionLost() {
		        setMyState(STATE_LOST);

		        Message msg = myHandler.obtainMessage(Talk_Activity.MSG_TOAST);
		        Bundle bundle = new Bundle();
		        bundle.putString(Talk_Activity.TOAST, "�Ͽ�����");
		        msg.setData(bundle);
		        myHandler.sendMessage(msg);
		        myHandler.obtainMessage(Talk_Activity.CONNECT_STATE, myState, -1).sendToTarget();
		        
		    }
	//����������������
	private class AcceptRequestThread extends Thread {
        //��Ϊ��������socket
        private final BluetoothServerSocket myServerSocket;

		public AcceptRequestThread() {
			
			//��ʱsocket����
            BluetoothServerSocket temp = null;
            try {
                temp = myAdapter.listenUsingRfcommWithServiceRecord("LET'S TALK!", MY_UUID);
            } catch (IOException e) {
            	
            }
            myServerSocket = temp;
        }

 
		//ʵ���̵߳�run����
		public void run() {
			//���߳���������
            setName("TalkToMe");
            
            BluetoothSocket socket = null;

            System.out.println("UUID:"+MY_UUID);
            // ��û��������ʱ����������ֱ����ʱ
            while (myState != STATE_CONNECTED) {
                try {
                    socket = myServerSocket.accept();
                    Log.i("myAcceptThread", "�����߳�����");
                } catch (IOException e) {
                    //
                    break;
                }

                // �������ź�ʱ
                if (socket != null) {
                	//����������һ��һ��ͨ�ţ�����һ��BTC������ĳ��ʱ��ֻ����һ��talkService����
                	
                    synchronized (talkService.this) {
                        switch (myState) {
                        case STATE_LISTEN:
                        case STATE_CONNECTING:
                        	Message msg = myHandler.obtainMessage(MainActivity.REQUEST_TALK);
            		        Bundle bundle = new Bundle();
            		        bundle.putString(MainActivity.DEVICE_ADDRESS, socket.getRemoteDevice().getAddress());
            		        bundle.putString(MainActivity.DEVICE_NAME, socket.getRemoteDevice().getName());
            		        Log.i("myAcceptThread",socket.getRemoteDevice().getAddress().toString()+"##"+ socket.getRemoteDevice().getName().toString());
            		        msg.setData(bundle);
            		        myHandler.sendMessage(msg);
                            connected(socket, socket.getRemoteDevice());
                            break;
                        case STATE_LOST:
                        case STATE_CONNECTED:
                            //������Ϣ��ر��ŵ�
                            try {
                                socket.close();
                            } catch (IOException e) {
                                //
                            }
                            break;
                        }
                    }
                    //cancel();
                }
            }
        }

		public void cancel() {
            try {
                myServerSocket.close();//�ر�ͨ��
           
            } catch (IOException e) {
                
            }
        }
    }
	
	/*
	 * ������������緢������������߳�
	 */
	private class ConnectRequestThread extends Thread{
		private final BluetoothSocket mySocket;
		private final BluetoothDevice myDevice;
		
		public ConnectRequestThread(BluetoothDevice device){
			myDevice=device;
			
			BluetoothSocket tempSocket=null;
			try{
				//����һ��RFCOMM����
		         	tempSocket=device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
			}
             catch (IOException e) {
            	 Log.i("myConnectThread", "�����߳�ʧ��");
             }
			Log.i("myConnectThread", tempSocket.getRemoteDevice().getAddress().toString());
			mySocket=tempSocket;
		}
		public void run(){
			//�����߳�����
			setName("TalkToU");
			//����豸������������ֹͣ�����������˷�ϵͳ��Դ
			if(myAdapter.isDiscovering())
        	{
            	System.out.println("����������ֹͣ����");
        	    myAdapter.cancelDiscovery();
        	}
			//if(myDevice.)
			Log.i("myConnectThread", "�������������߳�");
			try{
				//��������Ŀ���豸
				mySocket.connect();
			}
			catch(IOException e){
				connectionFailed();
				try {
					mySocket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				talkService.this.start();
				return;
				
			}
			 synchronized (talkService.this) {
	                myConnectThread = null;
	            }
			 connected(mySocket, myDevice);

		}
		
		public void cancel(){
			
			try {
                mySocket.close();//�ر�ͨ��
           
            } catch (IOException e) {
                
            }
		}
		
	}
	
	private class talkingThread extends Thread{
		
		private final BluetoothSocket mySocket;
		private final InputStream inputInfo;
		private final OutputStream outputInfo;
		
		public talkingThread(BluetoothSocket socket){
			mySocket=socket;
			InputStream tempIn = null;
            OutputStream tempOut = null;

            // ����ŵ������������
            try {
                tempIn = socket.getInputStream();
                tempOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            inputInfo = tempIn;
            outputInfo = tempOut;
		}
		
		public void run(){
			byte[] buffer = new byte[1024];
            int bytes;
            //���������ŵ�����
           while(true){
        	   try {
        		   myHandler.obtainMessage(talkService.STATE_CONNECTED).sendToTarget();//ͬʱ����һ����Ϣ
                   bytes = inputInfo.read(buffer);
                   // ��ʾ���Ի�������ȥ
                   
                String content=new String(buffer,0,bytes);
                
                System.out.print(content);
               	Message msg = myHandler.obtainMessage(Talk_Activity.MSGACTION_READ);
               	Bundle bundle=new Bundle();
               	bundle.putInt(Talk_Activity.ISMY_MSG, 0);
               	bundle.putString(Talk_Activity.MSG_CONTENT, content);
               	msg.setData(bundle);
               	myHandler.sendMessage(msg);
               } catch (IOException e) {
                   connectionLost();
                   break;
               }
           }
		}
		public void write(byte[] buffer) {
            try {
            	
                
                
               
            	outputInfo.write(buffer);
            	outputInfo.flush();

                // ��ʾ���͵���Ϣ���Ի�������
            	String content=new String(buffer,0,buffer.length);
            	System.out.print(content);
               	Message msg = myHandler.obtainMessage(Talk_Activity.MSGACTION_WRITE);
               	Bundle bundle=new Bundle();
               	bundle.putInt(Talk_Activity.ISMY_MSG, 1);
               	bundle.putString(Talk_Activity.MSG_CONTENT, content);
               	msg.setData(bundle);
               	myHandler.sendMessage(msg);
            } catch (IOException e) {
            	myHandler.obtainMessage(Talk_Activity.MSG_TOAST,"����ʧ��").sendToTarget();
            }
        }

		public void cancel() {
            try {
                mySocket.close();
            } catch (IOException e) {
            	myHandler.obtainMessage(Talk_Activity.MSG_TOAST,"����ʧ��").sendToTarget();
            }
        }
    }
		
}
	

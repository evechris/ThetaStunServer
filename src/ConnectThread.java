import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;


public class ConnectThread implements Runnable{

	/**
	 * @param args
	 */
	
	Thread thread;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new ConnectThread();
	}
	
	public ConnectThread(){
		thread = new Thread(this);
		thread.start();
	}
	
	public void run(){
		try{
			for( ; ; ){
				//�������ݿ�
				final Connection connection;
				try {
					Class.forName("com.mysql.jdbc.Driver");
			    	
					connection = DriverManager.getConnection
					("jdbc:mysql://localhost/test","root","root");
					
					Statement statement=connection.createStatement();
			    	ResultSet resultSet=statement.executeQuery("select * from pmws where ALIVE=1");
			    	
			    	while(resultSet.next()){
			    		
			    		final String clientname=resultSet.getString(1);
			    		final String clientip=resultSet.getString(2);
			    		final String clientport=resultSet.getString(3);
			    		final String clientconnectport=resultSet.getString(4);
			    		final String clientalive=resultSet.getString(5);
			    		System.out.println("�����������ӵ��ͻ��� " + clientname+" -- "+clientip + ":"+clientconnectport +"\n");
			    		
			    		//CONNECT����
			    		try {
			    			//System.out.println(clientip+" " + Integer.parseInt(clientconnectport));
			    			final Socket socket = new Socket(clientip, Integer.parseInt(clientconnectport));
			    			
			    		   	//SocketAddress remoteAddr = new InetSocketAddress(clientip, Integer.parseInt(clientconnectport)); 
			    			//socket.connect(remoteAddr);
			    			
			    			final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(),"utf-8"));
			            	PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"utf-8")), true);
			            	if (socket.isConnected()) {
			    	        	if (!socket.isOutputShutdown()) {
			    	        		out.println("REQUEST:CONNECT\r\n");
			    	        	}
			            	}
			            	else {
			            		Statement statement2=connection.createStatement();
			            		int resultdelete = statement2.executeUpdate("update pmws set ALIVE = 0" +
			            				"where NAME = '" + clientname + "' AND IP = '" + clientip + "' AND PORT = '" + 
			            				clientport + "'");
			            		break;
			            	}
			            	
			            	//��ȡע�᷵�ؽ��
					        new Thread(new Runnable(){
					        	public void run(){
					                try {
					                	Date d = new Date();
					                	long longtime = d.getTime();
					                	boolean flag = false;
					                	//System.out.println(longtime);
					                    while (true && (((Date) new Date()).getTime()<=longtime+5000)) {
					                        if (socket.isConnected()) {
					                            if (!socket.isInputShutdown()) {
					                            	String content;
					                                if ((content = in.readLine()) != null) {
					                                    content += "\n";
					                                    //handler.obtainMessage(1,content).sendToTarget();
					                                    if(content.indexOf("RESPONSE:CONNECT")!=-1){
					                                    	socket.close();
					                                    	flag = true;
					                                    	System.out.println("�ͻ���"  + clientname + " - " + clientip + ":" + 
					        			            				clientconnectport + "��Ӧ�ɹ�" +"\n");
					                                    }
					                                    else if(content.indexOf("FAIL")!=-1){
					                                    	socket.close();
					                                    	flag = false;
					                                    	System.out.println("�ͻ�����Ӧʧ��" +"\n");
					                                    }
					                                } else {
					                                	flag = false;
					                                }
					                            }
					                        }
					                    }
					                    
					                    if(flag==false){
					                    	System.out.println("�������ӵ��ͻ���:"+clientname+" -- "+clientip+":"+clientconnectport+" 5s��δ�յ��ظ�" +"\n");
							    			//ALIVE��Ϊ0
							    			Statement statement2=connection.createStatement();
						            		int resultdelete = statement2.executeUpdate("update pmws set ALIVE = 0" +
						            				" where NAME = '" + clientname + "' AND IP = '" + clientip + "' AND PORT = '" + 
						            				clientport + "'");
					                    }
					                } catch (Exception e) {
					                    e.printStackTrace();
					                }
					            }
					        }).start();
					        
					        
			    		} catch (IOException e) {
			    			// TODO Auto-generated catch block
			    			System.out.println("�������ӵ��ͻ���ʱ��������" +"\n");
			    			//ALIVE��Ϊ0
			    			Statement statement2=connection.createStatement();
		            		int resultdelete = statement2.executeUpdate("update pmws set ALIVE = 0" +
		            				" where NAME = '" + clientname + "' AND IP = '" + clientip + "' AND PORT = '" + 
		            				clientport + "'");
			    			e.printStackTrace();
			    			break;
			    		}
			    	}
				} catch (Exception e) {//mysql error
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//���ü������ʱ��
				thread.sleep(1000*30);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}

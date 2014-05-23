import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;


public class stunsocket {
	int httpport=8088;
	
	public static void main(String[] args){
		new stunsocket();
	}
	
	public stunsocket() {
		try{
			ServerSocket httpserver=new ServerSocket(httpport); //����HTTP�����׽��� 
			System.out.println("�м������������"+ InetAddress.getLocalHost() + ":" + httpserver.getLocalPort() +"\n"); 
			httpdaemon httpproxy=new httpdaemon(httpserver); //����HTTP�����߳� 
			new ConnectThread();
		}
		catch(IOException e){
		}
	}
	
	class httpdaemon extends Thread{
		private ServerSocket server;
		
		public httpdaemon(ServerSocket _server){
			server=_server;
			start();
		}
		
		public void run(){
			//�߳����к���
			Socket connection;
			while(true){
				try{
					connection=server.accept(); 
					HTTPServerThread handler =new HTTPServerThread(connection); 
				}
				catch(Exception e){}
			}
		}
	}
	
	class HTTPServerThread extends Thread{
		private Socket connection;
		
		public HTTPServerThread(Socket _connection){
			connection=_connection;
			start();
		}
		
		public void run(){
			//�߳����к��� 
			byte buf[]=new byte[10000]; 
			int readbytes=0;
			String s=null;
			DataInputStream in=null;
			PrintWriter out =null;
			
			try{
				in=new DataInputStream(connection.getInputStream());
				out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(),"utf-8")), true);
				//out=new DataOutputStream(connection.getOutputStream());
				String clientip=connection.getInetAddress().getHostAddress();
				SocketAddress clientport=connection.getRemoteSocketAddress();
				//System.out.println(clientip + ":" + clientport);
				
				if(in!=null&&out!=null){
					readbytes=in.read(buf,0,10000);
					//System.out.println(readbytes);//511
					//System.out.println(new String(buf)); //�ӿͻ��˶�����
					if(readbytes>0){
						//��������
						s=new String(buf); 
						if(s.indexOf("\r\n")!=-1)
							s=s.substring(0,s.indexOf("\r\n"));
						
						//�����REGISTER����
						if(s.indexOf("REGISTER")!=-1){
							InetAddress ip=connection.getInetAddress();
							int port=connection.getPort();
							System.out.println("�յ�����ע������: " + s + "---" + ip + ":" + port +"\n");
							String name=s.split("##")[1].split(":")[1];
							String connectport=s.split("##")[2].split(":")[1];
							
							//�洢�����ݿ�
							Connection connection;
							int resultSet = 0;
							try {
								Class.forName("com.mysql.jdbc.Driver");
								
								connection = DriverManager.getConnection
								("jdbc:mysql://localhost/test","root","root");
								
								Statement statement = connection.createStatement();
								resultSet = statement.executeUpdate("insert into pmws(NAME,IP,PORT,CONNECTPORT,ALIVE) " +
						    			"values('" + name + "','" + ip.toString().split("/")[1] + "','" + port + "','" + connectport + "','1')");
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							if(resultSet==1){
								System.out.println("�ɹ���������"+name+" - "+ip.toString().split("/")[1]+":"+port+" ��ע������" +"\n");
								out.println("REGISTER SUCCESS");
							}
							else{
								System.out.println("����"+name+" - "+ip.toString().split("/")[1]+":"+port+" ��ע������������" +"\n");
								out.println("REGISTER FAIL");
							}
							out.flush();
						}
						
						//�����CONNECT����
						else if(s.indexOf("CONNECT")!=-1){
							
						}
					}
				}
				
				//ִ�йرղ��� 
				if(in!=null)
					in.close();
				if(out!=null)
					out.close(); 
				if(connection!=null)
					connection.close();
			}
			catch(IOException e){}
		}
	}
	
}

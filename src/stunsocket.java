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
			ServerSocket httpserver=new ServerSocket(httpport); //建立HTTP侦听套接字 
			System.out.println("中间服务器启动："+ InetAddress.getLocalHost() + ":" + httpserver.getLocalPort() +"\n"); 
			httpdaemon httpproxy=new httpdaemon(httpserver); //建立HTTP侦听线程 
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
			//线程运行函数
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
			//线程运行函数 
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
					//System.out.println(new String(buf)); //从客户端读数据
					if(readbytes>0){
						//读到数据
						s=new String(buf); 
						if(s.indexOf("\r\n")!=-1)
							s=s.substring(0,s.indexOf("\r\n"));
						
						//如读到REGISTER请求
						if(s.indexOf("REGISTER")!=-1){
							InetAddress ip=connection.getInetAddress();
							int port=connection.getPort();
							System.out.println("收到服务注册请求: " + s + "---" + ip + ":" + port +"\n");
							String name=s.split("##")[1].split(":")[1];
							String connectport=s.split("##")[2].split(":")[1];
							
							//存储至数据库
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
								System.out.println("成功处理来自"+name+" - "+ip.toString().split("/")[1]+":"+port+" 的注册请求" +"\n");
								out.println("REGISTER SUCCESS");
							}
							else{
								System.out.println("来自"+name+" - "+ip.toString().split("/")[1]+":"+port+" 的注册请求发生错误" +"\n");
								out.println("REGISTER FAIL");
							}
							out.flush();
						}
						
						//如读到CONNECT请求
						else if(s.indexOf("CONNECT")!=-1){
							
						}
					}
				}
				
				//执行关闭操作 
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

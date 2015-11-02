package caiqiqi.sdnserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MyServer {
	/**
	 * 负责接收客户端Socket的连接请求。每当客户端Socket连接到该ServerSocket之后，
	 * 程序将对应的Socket加入到socketList集合中保存，并为该Socket启动一条线程，该线程负责处理该Socket所有的通信任务。
	 **/

	// 定义所有保存Socket的ArrayList
	public static ArrayList<Socket> socketList = new ArrayList<Socket>();

	public static void main(String[] args) throws IOException {

		ServerSocket ss = null;
		try {
			// 服务器端在默认端口30000号端口监听
			ss = new ServerSocket(Constants.DEFAULT_BIND_PORT);
			System.out.println("Server started,listening at port ..." + Constants.DEFAULT_BIND_PORT);
			while (true) {
				// 此行代码会阻塞，直到客户端的连接出现
				Socket s = ss.accept();
				socketList.add(s);
				// 每当客户端连接后，启动一条ServerThread线程为该客户端服务
				new Thread(new ServerThread1(s)).start();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}

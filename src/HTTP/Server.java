package HTTP;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import com.sun.net.httpserver.HttpServer;

import HTTP.requestHandler;
import HTTP.responseHandler;



public class Server {

	public static int port = 80;
	public static String request = new String();
	public static byte[] response;
	public static int requestLength = -1;
	public static int serverCount = 0;
	public static char[] cbuf = new char[8000];
	
	
	public static void main(String args[]) throws IOException, InterruptedException
	{
		
		while(true)
		{
			ServerSocket server = new ServerSocket(port);
			System.out.println("Server "+(++serverCount)+" now listening on port "+ port);
			Socket socket = server.accept();
			System.out.println("accepted: "+socket.getInetAddress()+":"+socket.getPort());
            DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
			
			while(!br.ready());
			request = "";
			requestLength = br.read(cbuf);
			request = String.copyValueOf(cbuf, 0, requestLength);
			response = HTTP.requestHandler.responseGenerator(request);
			sendResponse(response, dout);
			System.out.println("Response sent");
			
			br.close();
			dout.close();
			socket.close();
			server.close();
			System.out.println("Server closed\n");
		}
	}
	
	public static void sendResponse(byte[] response, DataOutputStream dout) throws IOException
	{
		dout.write(response);
		dout.flush();
	}
}
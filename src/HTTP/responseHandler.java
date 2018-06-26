package HTTP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;


public class responseHandler {
	public static byte[] getResponse(String[] parsedRequest) throws IOException
	{
		byte[] response;
		String responseStartLine = "HTTP/1.1 ";			//固定响应头
		String urlHeader = "C:/Users/15761/Desktop/HTTP";		//服务器根目录
		String responseHeader = "Content-Type: ";		//响应类型
		String get = new String();
		String extension = new String();
		
		if(parsedRequest[1].indexOf('?')>-1)		//从url分离出get请求
		{
			get = parsedRequest[1].substring(parsedRequest[1].indexOf('?')+1);
			parsedRequest[1]=parsedRequest[1].substring(0,parsedRequest[1].indexOf('?'));
		}
		
		if(parsedRequest[1].endsWith("/")) parsedRequest[1]+="index.html";			//自动补全默认页index.html
		
		//控制台调试信息
		System.out.println("method: "+parsedRequest[0]+"\nurl: "+parsedRequest[1]);
		System.out.println("get: " + get);
		System.out.println("Accept: "+parsedRequest[2]+"\nbody: "+parsedRequest[3]);
		
		//判断请求文件是否存在并补全响应头的状态码，ie. 200 OK, 404 NotFound ...
		File requestedFile = new File( urlHeader + parsedRequest[1]);
		if(!requestedFile.exists()||requestedFile.isDirectory())
		{
			requestedFile = new File(urlHeader + "/404.html");
			responseStartLine+="404 NotFound\r\n";
		}
		else responseStartLine+="200 OK\r\n";
		
		//通过请求的url后缀名判断MIMEType，补全Content-Type
		extension = requestedFile.getPath().substring(requestedFile.getPath().lastIndexOf('.')+1);
		responseHeader+=getMIMEType(extension);
		
		response = getResponseByteArray(requestedFile.toPath(), responseStartLine, responseHeader);
		//若响应为html页面，打印到控制台
		if(getMIMEType(extension).indexOf("text")>-1)System.out.println(new String(response, "UTF-8"));
		response = getHandler(response, get);
		
		return response;
	}

	
	

	private static String getMIMEType(String extension) {
		String MIMEType = new String();
		
		//查找mime文件中后缀名对应的MIMEType
		try (InputStream in = Files.newInputStream((new File("C:/Users/15761/Desktop/HTTP/mime.txt")).toPath());
				BufferedReader reader = new BufferedReader(new InputStreamReader(in,"UTF-8")))
		{
		    String line = null;
		    reader.read();
		    while ((line = reader.readLine()) != null)
		    {
		    	if(line.startsWith(extension)) MIMEType = line.split("\t")[1];
		    }
		} catch (IOException x) {
		    System.err.println(x);
		}
		if(MIMEType.isEmpty()) MIMEType = "none";
		return MIMEType;
	}
	
	private static byte[] getResponseByteArray(Path requestedFilePath, String responseStartLine, String responseHeader)
	{
		byte[] response = null;
		System.out.println("opening file: "+requestedFilePath.toString());
		byte[] content = null;
		byte[] header = (responseStartLine + responseHeader +"\r\n\r\n").getBytes();		//响应头和响应内容之间增加空行
		try {
			content = Files.readAllBytes(requestedFilePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//初始化response的大小并将响应head和body组合
		response = new byte[content.length+ header.length];
		System.arraycopy(header, 0, response, 0, header.length);
		System.arraycopy(content, 0, response, header.length, content.length);
		return response;
	}

	private static byte[] getHandler(byte[] response, String get) {
		//TODO: 处理get请求
		
		return response;
		
	}
}



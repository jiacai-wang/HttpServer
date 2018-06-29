/*
 * 0，从request解析出Method，url，Accept，body字段
 * 1，调用HTTP.responseHandler.getResponse(parsedRequest)获取response
 * 
 */

package HTTP;

import java.io.IOException;
import java.util.Scanner;

public class requestHandler {
	public static byte[] responseGenerator(String request) throws IOException
	{
		request = java.net.URLDecoder.decode(request, "UTF-8");				//将url编码的字符串转为UTF-8编码
		
		String[] parsedRequest=new String[4];
		//字符串数组 parsedRequest[4]分别用于存放
		//[0]: method（ie. GET, POST, CONNECT ...）
		//[1]: 资源路径(含get的?foo1=bar1&foo2=bar2字段)
		//[2]: 接受的响应类型(ie. text/html, image/jpeg, */*, ...)
		//[3]: body内容
		
		Scanner scanner=new Scanner(String.valueOf(request));
		String line=scanner.nextLine();
		parsedRequest[0]=line.split(" ")[0];
		parsedRequest[1]=line.split(" ")[1];
		parsedRequest[3]="";
		//遍历head查找Accept: 字段
		//实际后面没用到。按照mime.txt获取后缀名对应的ContentType后未检查是否满足Accept
		while(line.length()!=0 && scanner.hasNextLine())
		{
			if((line=scanner.nextLine()).startsWith("Accept:")) parsedRequest[2]=line.split(" ")[1] ;
//			System.out.println((++i)+"\t"+line+"\t"+line.length());
		}
		
		//获取body字段
		while (scanner.hasNextLine())
		{
			line = scanner.nextLine();
			parsedRequest[3]=parsedRequest[3]+line+"\r\n";
		}
		scanner.close();
		
		//调用getRespons方法获取response
		return HTTP.responseHandler.getResponse(parsedRequest);			
	}
}

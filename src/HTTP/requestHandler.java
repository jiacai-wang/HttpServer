/*
 * 0����request������Method��url��Accept��body�ֶ�
 * 1������HTTP.responseHandler.getResponse(parsedRequest)��ȡresponse
 * 
 */

package HTTP;

import java.io.IOException;
import java.util.Scanner;

public class requestHandler {
	public static byte[] responseGenerator(String request) throws IOException
	{
		request = java.net.URLDecoder.decode(request, "UTF-8");				//��url������ַ���תΪUTF-8����
		
		String[] parsedRequest=new String[4];
		//�ַ������� parsedRequest[4]�ֱ����ڴ��
		//[0]: method��ie. GET, POST, CONNECT ...��
		//[1]: ��Դ·��(��get��?foo1=bar1&foo2=bar2�ֶ�)
		//[2]: ���ܵ���Ӧ����(ie. text/html, image/jpeg, */*, ...)
		//[3]: body����
		
		Scanner scanner=new Scanner(String.valueOf(request));
		String line=scanner.nextLine();
		parsedRequest[0]=line.split(" ")[0];
		parsedRequest[1]=line.split(" ")[1];
		parsedRequest[3]="";
		//����head����Accept: �ֶ�
		//ʵ�ʺ���û�õ�������mime.txt��ȡ��׺����Ӧ��ContentType��δ����Ƿ�����Accept
		while(line.length()!=0 && scanner.hasNextLine())
		{
			if((line=scanner.nextLine()).startsWith("Accept:")) parsedRequest[2]=line.split(" ")[1] ;
//			System.out.println((++i)+"\t"+line+"\t"+line.length());
		}
		
		//��ȡbody�ֶ�
		while (scanner.hasNextLine())
		{
			line = scanner.nextLine();
			parsedRequest[3]=parsedRequest[3]+line+"\r\n";
		}
		scanner.close();
		
		//����getRespons������ȡresponse
		return HTTP.responseHandler.getResponse(parsedRequest);			
	}
}

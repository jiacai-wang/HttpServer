package HTTP;

import java.io.IOException;
import java.util.Scanner;

public class requestHandler {
	public static byte[] responseGenerator(String request) throws IOException
	{
		request = java.net.URLDecoder.decode(request, "UTF-8");
		byte[] response;
		String[] parsedRequest=new String[4];
		Scanner scanner=new Scanner(String.valueOf(request));
		String line=scanner.nextLine();
		parsedRequest[0]=line.split(" ")[0];
		parsedRequest[1]=line.split(" ")[1];
		parsedRequest[3]="";
		while(line.length()!=0 && scanner.hasNextLine())
		{
			if((line=scanner.nextLine()).startsWith("Accept:")) parsedRequest[2]=line.split(" ")[1] ;
//			System.out.println((++i)+"\t"+line+"\t"+line.length());
		}
		while (scanner.hasNextLine())
		{
			line = scanner.nextLine();
			parsedRequest[3]=parsedRequest[3]+line+"\r\n";
		}
		scanner.close();
		response = HTTP.responseHandler.getResponse(parsedRequest);
		return response;
		
		
	}
}

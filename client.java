import java.io.*;
import java.net.*;
import java.util.Date;
import java.text.SimpleDateFormat;

public class Client  //Creates a socket and connects with the Server
{
	public void startClient(InetAddress address, int portNumber)
	{
		try
		{
			InetAddress socketAddress = address;
			int socketPortNumber = portNumber;
			Socket clientSocket =new Socket(socketAddress,socketPortNumber);
			System.out.println("\n CONNECTED WITH SERVER \n");
			ClientUtility.printHelpClient();

			FromServer inputObject = new FromServer(clientSocket);
			Thread inputThread = new Thread(inputObject); //spawns a thread that handles all the operations that are received from the Server

			ToServer outputObject = new ToServer(clientSocket);
			Thread outputThread = new Thread(outputObject); //spawns a thread that handles all the operations that are sent to the Server

			inputThread.start();
			outputThread.start();
			inputThread.join();
			System.exit(0);
			//outputThread.join();
		}

		catch(SocketException error)
		{
			System.out.println(" No Server found on this port number ");
		}

		catch(Exception e)
		{
			System.out.println(" Enter valid IP address and port number ");
		}
	}

	public static void main(String []args)
	{
		try
		{
			if(args.length<2)
			{
				System.out.println("Enter IP address and portnumber ");
			}
			else
			{
				InetAddress address = InetAddress.getByName(args[0]);
				int portNumber = Integer.parseInt(args[1]);
				new Client().startClient(address, portNumber);
			}
		}

		catch(Exception e)
		{
			System.out.println("Enter IP address first and then port number ");
		}
	}
}

class ToServer implements Runnable // handles operation that are sent from the Client( by the User) to the Server
{
	Socket clientSocket;
	public static String fileToRead = null;

	public ToServer(Socket clientSocket)
	{
		this.clientSocket = clientSocket;
	}
	
	public void run()
	{
		while(true)
		{
			try
			{
				String sendToServer;
				String currentTimestamp = new java.text.SimpleDateFormat("MM/dd/yyyy hh:mm:ss a").format(new Date());

				BufferedReader inputFromUser = new BufferedReader(new InputStreamReader(System.in));
				DataOutputStream bufferToServer = new DataOutputStream(clientSocket.getOutputStream());
				sendToServer = inputFromUser.readLine(); //input from the user is saved in a String

				String splitArray[] = sendToServer.split(" ",2);
				String firstWord =  splitArray.length >= 1? splitArray[0] : " ";//saves the first word of the input String
				String lastWord = splitArray.length >= 2? splitArray[1] : " ";//saves the remaining words in  a String
				try
				{
					if(sendToServer.equalsIgnoreCase("Exit"))
					{
						inputFromUser.close();
						bufferToServer.close();
						clientSocket.close();
						break;
					}

					else if(firstWord.equalsIgnoreCase("Chat"))
					{
						long currentNanoTime = System.nanoTime();
						bufferToServer.writeBytes(sendToServer + '\t' + currentNanoTime + '\n');
						bufferToServer.flush();
					}
		
					else if(firstWord.equalsIgnoreCase("put"))
					{
						String checkArray[] = lastWord.split(" ",2);
						if((checkArray.length >= 2) && (lastWord != null))
						{
							long currentNanoTime = System.nanoTime();
							bufferToServer.writeBytes(sendToServer + '\t' + currentNanoTime + '\n');
							bufferToServer.flush();
						}
						else
						{
							System.out.println(" Enter key and value ");
						}
					}

					else if(firstWord.equalsIgnoreCase("get"))
					{
						String checkArray[] = lastWord.split(" ",2);
						if((checkArray.length >=1) && (lastWord != null))
						{
							long currentNanoTime = System.nanoTime();
							bufferToServer.writeBytes(sendToServer + '\t' + currentNanoTime + '\n');
							bufferToServer.flush();
						}
						else
						{
							System.out.println(" Enter key after get ");
						}
					}

					else if(firstWord.equalsIgnoreCase("remove") || firstWord.equalsIgnoreCase("delete"))
					{
						String checkArray[] = lastWord.split(" ",2);
						if((checkArray.length >=1) && (lastWord != null))
						{
							long currentNanoTime = System.nanoTime();
							bufferToServer.writeBytes(sendToServer + '\t' + currentNanoTime + '\n');
							bufferToServer.flush();
						}
						else
						{
							System.out.println(" Enter key after delete/remove ");
						}
					}

					else if(firstWord.equalsIgnoreCase("read"))
					{
						String checkArray[] = lastWord.split(" ",2);
						String copyFileName = checkArray[1];
						fileToRead = copyFileName;
						if((checkArray.length >=1) && (lastWord != null))
						{
							long currentNanoTime = System.nanoTime();
							bufferToServer.writeBytes(sendToServer + '\t' + currentNanoTime + '\n');
							bufferToServer.flush();
						}
						else
						{
							System.out.println(" Enter file name after read ");
						}
					}

	

					else if(firstWord.equalsIgnoreCase("Help"))
					{
						ClientUtility.printHelpClient();
					}

					else
					{
						System.out.println(" Enter chat/put/get/remove/help as first word or exit to terminate ");
					}
				}

				catch(Exception e)
				{
					System.out.println("Exception ");
				}
			}

			catch (Exception e)
			{
				System.out.println(" Closing Connection ");
				break;
			}
		}
	}
}

class FromServer implements Runnable //handles operations that are sent from the Server to the Client
{
	Socket clientSocket;

	public FromServer(Socket clientSocket)
	{
		this.clientSocket = clientSocket;

	}

	public void run()
	{
		while(true)
		{
			try
			{
				BufferedReader bufferFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				String currentTimestamp = new java.text.SimpleDateFormat("MM/dd/yyyy hh:mm:ss a").format(new Date());
				String receivedFromServer = bufferFromServer.readLine();
				FileOutputStream writeFileContents = null;
				InputStream fileInput = clientSocket.getInputStream();
				BufferedOutputStream writeFileBuffer = null;
				int FileSize = 6000000;
				
				/*String getLine = receivedFromServer;
				StringBuilder receivedFromServerLines = new StringBuilder();
				while(getLine != null)
				{
				//	System.out.println("foo");
					if(getLine == null)
				        { break; }
					receivedFromServerLines.append(getLine);
					receivedFromServerLines.append(System.lineSeparator());
					getLine = bufferFromServer.readLine();					
				}
				
				System.out.println(" Server says : " + receivedFromServerLines);
				//bufferFromServer.flush();
				//receivedFromServer = bufferFromServer.readLine(); */

				String splitArray[] = receivedFromServer.split(" ",2);
				String firstWord = splitArray.length >=1 ?splitArray[0] : "";
				String lastWord = splitArray.length >=2 ?splitArray[1] : "";

				if(receivedFromServer.equalsIgnoreCase("Exit"))
				{
					bufferFromServer.close();
					clientSocket.close();
					break;
				}

				/*else if(firstWord.equalsIgnoreCase("Chat"))
				{
					String chatArray[] = lastWord.split("\t",2);
					String chatMessage = chatArray.length >=1 ? chatArray[0] : "";
					long pastNanoTime = Long.parseLong(chatArray[1].trim());
					long currentNanoTime = System.nanoTime();
					long nanoDelay = (currentNanoTime - pastNanoTime)/1000;
					System.out.println("Server says : "+chatMessage +'\t' +"Delay : "+nanoDelay + "ns" +'\t' +currentTimestamp);
				} */

				else if(firstWord.equalsIgnoreCase("get"))
				{
					String getArray[] = lastWord.split("\t",2);
					String getMessage = getArray[0];
					long pastNanoTime = Long.parseLong(getArray[1].trim());
					long currentNanoTime = System.nanoTime();
					long nanoDelay = (currentNanoTime - pastNanoTime)/1000; //calculates throughput
					System.out.println(getMessage + '\t' + "Throughput : " +nanoDelay +"ns" + '\t' +currentTimestamp);
				}

				else if(firstWord.equalsIgnoreCase("key"))
				{
					String getArray[] = receivedFromServer.split("\t",2);
					String printMessage = getArray[0];
					long pastNanoTime = Long.parseLong(getArray[1].trim());
					long currentNanoTime = System.nanoTime();
					long nanoDelay = (currentNanoTime - pastNanoTime)/1000;
					System.out.println(printMessage + '\t' + "Throughput : " +nanoDelay +"ns" + '\t' +currentTimestamp);
				}
				
				/*else //if(firstWord.equalsIgnoreCase("file"))
				{
					int readBytes;
					String fileName = ToServer.fileToRead;
					byte []fileByteArray = new byte[FileSize];
					writeFileContents = new FileOutputStream(fileName);
					writeFileBuffer = new BufferedOutputStream(writeFileContents);
					readBytes = fileInput.read(fileByteArray,0,fileByteArray.length);
					int numberOfBytesNow = readBytes;

					do {
						readBytes = fileInput.read(fileByteArray,numberOfBytesNow,(fileByteArray.length-numberOfBytesNow));
						if(readBytes >= 0) numberOfBytesNow += readBytes;
					}while(readBytes > -1);

					writeFileBuffer.write(fileByteArray,0,numberOfBytesNow);
					writeFileBuffer.flush();
					System.out.println (" File Received ");



					//String getArray[] = lastWord.split("\t",2);
					//String printFileContents = getArray[0];
					//long pastNanoTime = Long.parseLong(getArray[1].trim());
					//long currentNanoTime = System.nanoTime();
					//long nanoDelay = (currentNanoTime - pastNanoTime)/1000;
					//System.out.println("Throughput : " +nanoDelay +"ns" + '\t' +currentTimestamp);


					System.out.println(" FILE CONTENTS \n\n" + lastWord + "\n\n");
				} */


			}

			catch (Exception e)
			{
				System.out.println("Closing Connection ");
				e.printStackTrace();
				break;
			}
		}
	}
}

class ClientUtility 
{
	public static void printHelpClient()
	{
		System.out.println(" \n Available Commands ");
		//System.out.println(" 1. chat argument1 - to communicate with server (Eg., chat hello server)");
		System.out.println(" 1. put argument1 argument2 - to store key/value pait in the server (Eg., put 1 foo )");
		System.out.println(" 2. get argument1 - to retrieve value for the key from the server (Eg., get 1)");
		System.out.println(" 3. remove argument1 - to delete the key value pair stored in the server (Eg., remove 1)");
		System.out.println(" 4. delete argument1 - to delete the key value pair stored in the server (Eg., delete 1)");	
		System.out.println(" 5. Exit - to close connection ");
		System.out.println(" 6. Help - to get Help \n ");
	}
}



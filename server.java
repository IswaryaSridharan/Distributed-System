import java.io.*;
import java.net.*;
import javax.net.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.lang.Object;
import java.sql.Timestamp;
import javax.net.ssl.*;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Server//Creates a Server Socket, waits for a client and connects with it
{
	public void startServer(int portNumber) 
	{

		try
		{
			int socketPortNumber = portNumber;
			int clientNumber=1; int i=1;

		        ServerSocketFactory serverSocketFactory = ServerSocketFactory.getDefault();
			ServerSocket listeningSocket = serverSocketFactory.createServerSocket(socketPortNumber);
			
			System.out.println(" Waiting for Client .......... ");


			Thread inputThread[] = new Thread[5];
			Thread outputThread[] = new Thread[5];
			Socket[] connectingSocketArray = new Socket[6];
			while(true)
			{
				Socket connectingSocket = listeningSocket.accept();
				connectingSocketArray[i] = connectingSocket;
				System.out.println(" CONNECTED WITH CLIENT " +clientNumber);
				ServerUtility.printHelpServer();
				ToClient outputObject = new ToClient(connectingSocketArray,listeningSocket,clientNumber);
				outputThread[clientNumber] = new Thread(outputObject); //spawns a thread that handles all the operations that are sent to the Client from Server
				FromClient inputObject = new FromClient(connectingSocket,listeningSocket,clientNumber);
				inputThread[clientNumber] = new Thread(inputObject); //spawns a thread that handles all the operations that are received from the Client to Server
				inputThread[clientNumber].setPriority(Thread.MAX_PRIORITY);
				inputThread[clientNumber].start();
				outputThread[clientNumber].setPriority(Thread.MAX_PRIORITY);
				outputThread[clientNumber].start();
				clientNumber++; i++;


				//inputThread[i].join();
				//System.exit(0);
			}
		}		
		catch(Exception e)
		{
			System.out.println(" Enter port number between 2000 to 65535 ");
		}
	}

	public static void main(String []args)
	{
		try
		{
			if(args.length < 1)
			{
				System.out.println(" Enter port number ");
			}
			else
			{
				int portNumber = Integer.parseInt(args[0]);
				new Server().startServer(portNumber);
			}
		}

		catch(Exception error)
		{
			System.out.println(" Exception occured : Closing Connection ");
		}
	} 
}

class ToClient implements Runnable//handles all the operations that are sent to the Client from the Server
{
	Socket[] connectingSocketArray = new Socket[6];
	ServerSocket listeningSocket;
	int keyGet = 0;
	String valueGet = null;
	int clientNumber;


	public ToClient(Socket[] connectingSocketArray, ServerSocket listeningSocket, int clientNumber)
	{
		this.connectingSocketArray = connectingSocketArray;
		this.listeningSocket = listeningSocket;
		this.clientNumber = clientNumber;

	}

	public void run()
	{
		while(true)
		{
			try
			{
				String currentTimestamp = new java.text.SimpleDateFormat("MM/dd/yyyy hh:mm:ss a").format(new Date());
				String sendToClient;

				BufferedReader inputFromUser = new BufferedReader(new InputStreamReader(System.in));
				DataOutputStream bufferToClient = new DataOutputStream(connectingSocketArray[clientNumber].getOutputStream());
				sendToClient = inputFromUser.readLine();

				String splitArray[] = sendToClient.split(" ",2);
				String firstWord = splitArray.length >= 1 ? splitArray[0] : "";
				String lastWord = splitArray.length >=2 ? splitArray[1] : "";
				try
				{
					if(sendToClient.equalsIgnoreCase("Exit"))
					{
						bufferToClient.close();
						inputFromUser.close();
						//connectingSocket.close();
						listeningSocket.close();
						for(int i=1; i<=connectingSocketArray.length;i++)
						{
							connectingSocketArray[i].close();
						}
						break;
					}
		
					/*else if(firstWord.equalsIgnoreCase("Chat"))
					{
						//while(connectingSocketArray.hasMoreElements())
						//{
						System.out.println("length : " + connectingSocketArray.length);
						for(int i=1; i<=connectingSocketArray.length; i++)
						{
						//Socket client = clientList.nextElement();
						long currentNanoTime= System.nanoTime();
						bufferToClient.writeBytes(sendToClient + '\t' +currentNanoTime + '\n');
						bufferToClient.flush();
						}
					} */
			
					else if(firstWord.equalsIgnoreCase("Help"))
					{
						ServerUtility.printHelpServer();
					}

					else
					{
						System.out.println(" Enter exit to terminate or help to get help");
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

class FromClient implements Runnable //parses the incoming command from the Client and handles the necessary operations
{
	Socket connectingSocket;
	ServerSocket listeningSocket;
	int clientNumber;
	static HashMap<String,String> hashMapObject = new HashMap<String,String>();
	private final Lock mutex = new ReentrantLock(true);
	
	public FromClient(Socket connectingSocket, ServerSocket listeningSocket, int clientNumber)
	{
		this.connectingSocket = connectingSocket;
		this.listeningSocket = listeningSocket;
		this.clientNumber = clientNumber;
	}
				
	void hashPutFunction(String putKey, String putValueOnly, String currentTimestamp, long pastNanoTime)
	{
		try
		{
			mutex.lock();
			System.out.println("Acquired Lock \n");
			hashMapObject.put(putKey,putValueOnly);
			System.out.println(" ADDED Key : " + putKey + " Value : " +putValueOnly +'\t' + currentTimestamp);
			DataOutputStream sendAck = new DataOutputStream(connectingSocket.getOutputStream());
			sendAck.writeBytes("Key Value Added " +'\t' +pastNanoTime +'\n');
			mutex.unlock();
			System.out.println ("\nReleased Lock ");
		}

		catch(Exception e)
		{
			System.out.println(" Exception ");
		}
	}


	public void run()
	{
		while(true)
		{
			try
			{
		        	String receivedFromClient;
				String currentTimestamp = new java.text.SimpleDateFormat("MM/dd/yyyy hh:mm:ss a").format(new Date());

				BufferedReader bufferFromClient = new BufferedReader(new InputStreamReader(connectingSocket.getInputStream()));
				receivedFromClient = bufferFromClient.readLine();
				String storeArray[] = receivedFromClient.split(" ",2);
				String firstWord = storeArray.length >=1 ?storeArray[0] : "";
				String lastWord = storeArray.length >=2 ?storeArray[1] : "";
				FileInputStream readFileContents =null;

				if(receivedFromClient.equalsIgnoreCase("Exit"))
				{
					bufferFromClient.close();
					connectingSocket.close();
					listeningSocket.close();
					break;
				} 

				/*if(firstWord.equalsIgnoreCase("Chat"))
				{
					String chatArray[] = lastWord.split("\t",2);
					String chatMessage = chatArray.length >=1 ? chatArray[0] : "";
					long pastNanoTime = Long.parseLong(chatArray[1].trim());
					long currentNanoTime = System.nanoTime();
					long nanoDelay = (currentNanoTime - pastNanoTime)/1000;
					System.out.println("Client[" +clientNumber +"] says : "+chatMessage +'\t' +"Delay : "+nanoDelay +"ns" +'\t' +currentTimestamp);
				} */

				if(firstWord.equalsIgnoreCase("Put"))
				{
					String putArray[] = lastWord.split(" ",2);
					String putKey = putArray.length >=1 ? putArray[0] : "";
					String putValue = putArray.length >=2 ? putArray[1] : "";
					String valueArray[] = putValue.split("\t",2);
					String putValueOnly = valueArray[0];
					long pastNanoTime = Long.parseLong(valueArray[1].trim());
					DataOutputStream sendAck = new DataOutputStream(connectingSocket.getOutputStream());

					if(hashMapObject.containsKey(putKey))
					{
						sendAck.writeBytes("Key already exists " +'\t' + pastNanoTime + '\n');
					}
					else
					{
						hashPutFunction(putKey,putValueOnly,currentTimestamp,pastNanoTime);    


					}
				}

				if(firstWord.equalsIgnoreCase("get"))
				{
					String getArray[] = lastWord.split("\t",2);
					String getValue = getArray.length >=1 ?getArray[0] : " ";
					long pastNanoTime = Long.parseLong(getArray[1].trim());				
					DataOutputStream sendGetValue = new DataOutputStream(connectingSocket.getOutputStream());
					if(hashMapObject.containsKey(getValue))
					{
						String valueOfKey = hashMapObject.get(getValue).toString();
						sendGetValue.writeBytes("Get " + "key : " +getValue +" Value : " +valueOfKey +'\t' +pastNanoTime + '\n' );
					}
					else
					{
						sendGetValue.writeBytes("Key not found in HashMap " + '\t' +pastNanoTime +'\n');
					}
				}
			
				if(firstWord.equalsIgnoreCase("remove") || firstWord.equalsIgnoreCase("delete"))
				{
					String getArray[] = lastWord.split("\t",2);
					String getValue = getArray.length >=1 ?getArray[0] : "";
					long pastNanoTime = Long.parseLong(getArray[1].trim());
					DataOutputStream sendRemovedValue = new DataOutputStream(connectingSocket.getOutputStream());
					if(hashMapObject.containsKey(getValue))
					{
						hashMapObject.remove(getValue);
						long currentNanoTime = System.nanoTime();
						long nanoDelay = (currentNanoTime - pastNanoTime)/1000;
						sendRemovedValue.writeBytes("Key Value pair deleted " + '\t' + pastNanoTime +'\n');
					}
					else
					{
						sendRemovedValue.writeBytes("Key not found in HashMap " + '\t' + pastNanoTime +'\n');
					}
				}

			/*	if(firstWord.equalsIgnoreCase("read"))
				{
					String getArray[] = lastWord.split("\t",2);
					String readFileNamePath = getArray.length >= 1 ?getArray[0] : "";
					Path savePath = Paths.get(readFileNamePath);
					long pastNanoTime = Long.parseLong(getArray[1].trim());
					File checkFileName = new File(readFileNamePath);

					if(Files.exists(savePath))
					{
						try
						{
							byte [] storeFileLength = new byte[(int) checkFileName.length()];
							//FileReader fileContents = new FileReader(checkFileName);
							readFileContents = new FileInputStream(checkFileName);
							BufferedInputStream readFileBuffer = new BufferedInputStream(readFileContents);
							//String getLine = readFileContents.readLine();
							//StringBuilder allLines = new StringBuilder();
							DataOutputStream sendFileContents = new DataOutputStream(connectingSocket.getOutputStream());
							//PrintStream sendFileContents = new PrintStream(connectingSocket.getOutputStream());
                                                        //System.out.println(getLine);
							readFileBuffer.read(storeFileLength,0,storeFileLength.length);
							/*if(getLine == null)
							{
								sendFileContents.println("file No contents " + '\t' +pastNanoTime + '\n');
							}
							else
							{
								while(getLine != null)
								{
														                    
									allLines.append(getLine);
									allLines.append(System.lineSeparator());
									getLine = readFileContents.readLine();
								}
								sendFileContents.println("file " + allLines.toString() +'\t'+  pastNanoTime);
								//sendFileContents.close();
								//System.out.println(allLines);

							} **
							sendFileContents.write(storeFileLength,0,storeFileLength.length);
							sendFileContents.flush();
						}
						catch(Exception e)
						{
							System.out.println(" File Exception ");
						}
					}
					else
					{
					        DataOutputStream sendFileContents = new DataOutputStream(connectingSocket.getOutputStream());
						sendFileContents.writeBytes("File No such file exists " +'\t' +pastNanoTime +'\n');
					}
				} */
			}

			catch (Exception e)
			{
				System.out.println("Closing Connection ");
				break;
			}
		}
	}
}

class ServerUtility 
{
	public static void printHelpServer()
	{
		System.out.println(" \n Available Commands ");
		//System.out.println(" 1. chat argument1 - to communicate with client (Eg., chat hello client)");
		System.out.println(" 1. Exit - to close connection ");
		System.out.println(" 2. Help - to get Help \n ");
	}
}




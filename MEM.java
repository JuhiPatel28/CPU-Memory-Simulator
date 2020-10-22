import java.util.Scanner;
import java.io.*;

public class MEM 
{
	static int m[] = new int[2000];
	
	public static void main(String[] args)
	{
		Scanner CPUin = new Scanner(System.in);
		File f = null;
		
		if(CPUin.hasNext())
		{
			f = new File(CPUin.nextLine()); //create file to be read
		}
		else
		{
			//System.out.println("Didn't read anything from CPU.");
			System.exit(-1);
		}
		

		try
		{
			readFile(f); //read the file to memory
			//System.out.println("Successful.");
		}
		catch (FileNotFoundException error)
		{
			//System.out.println("File path not found.");
			System.exit(-1);
		}
		
		
		while(CPUin.hasNextLine()) //loop while cpu sends data
		{
			//store and split data
			String l = CPUin.nextLine();
			String [] iCPU = l.split(" ");
			
			//variables for address and data
			int addr;
			int data;
			
			switch(iCPU[0])
			{
				case "exit": //if the command is exit
				{
					CPUin.close(); //close the input stream
					System.exit(0); //close process
					break; 
				}
				case "read": //if the command is read
				{
					addr = Integer.parseInt(iCPU[1]); //get address
					int a = read(addr); //read data at address
					System.out.println(a); //send back data
					break;
				}
				case "write": //if the command is write
				{
					addr = Integer.parseInt(iCPU[1]); //get address
					data = Integer.parseInt(iCPU[2]); //get data
					write(addr, data); //write data at address
					break;
				}
			}
		}
	}
	
	private static int read(int a)
	{
		return m[a]; //return data at address
	}
	
	private static void write(int a, int d)
	{
		m[a] = d; //store data at address
	}
	
	private static void readFile(File f) throws FileNotFoundException
	{
		try
		{
			//create file input stream
			Scanner file = new Scanner(f);
			int temp = 0;
			
			while(file.hasNextLine())
			{
				String l = file.nextLine().trim();
				
				if(l.isEmpty() || l.startsWith("//")) //if the read line is empty
				{
					continue; //do not store anything in memory
				}
				
				String [] command = l.split(" ");
				if(l.startsWith(".")) //if the read line starts with a "."
				{
					temp = Integer.parseInt(command[0].substring(1, command[0].length())); //set address to the number that follows
					continue; //do not store that number
				}
				
				m[temp] = Integer.parseInt(command[0]); //store instruction at address
				temp++;	//increment address		
			}
			file.close(); //close input stream
		}
		catch(FileNotFoundException exp)
		{
			System.out.println("File does not exist.");
			System.exit(-1);
		}
	}

}

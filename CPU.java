import java.util.Scanner;
import java.io.*;
import java.util.Random;
import java.lang.Runtime;


public class CPU 
{
	public static void main(String[] args)
	{
		
		if(args.length < 2) //check if user entered enough arguments
		{
			System.out.println("Not enough arguments.");
			System.exit(-1);
		}
		
		try
		{		
			//store arguments
			String filename = args[0];
			int timer = Integer.parseInt(args[1]);
			
			//create runtime and processes
			Runtime rt = Runtime.getRuntime();
			Process mem = rt.exec("java MEM");
			
			//create input and output streams
			InputStream is = mem.getInputStream();
			OutputStream os = mem.getOutputStream();
		    PrintWriter pw = new PrintWriter(os); 
 		    Scanner sc = new Scanner(is);
		    
 		    //send filename to memory
		    pw.printf(filename + "\n");
	        pw.flush();
	        
	        //create and start execution of cpu object
	        cpu c = new cpu(sc, pw, timer);
	        c.execute();
	        
	        System.exit(0);      	        	   
		}
		catch (Throwable t)
	    {
			t.printStackTrace();
	    }
	}

	public static class cpu
	{
		//Register variables
		private int PC;
		private int SP;
		private int IR;
		private int AC;
		private int X;
		private int Y;
		
		//Interrupt variables
		private int iCount;
		private int timer;
		
		//Flags
		private boolean kMode;
		private boolean exitFlag;
		
		//Memory output and input
		private Scanner sc;
		private PrintWriter pw;
		
		public cpu(Scanner sc, PrintWriter pw, int timer)
		{
			PC = IR = AC = X = Y = 0; //set registers to 0
			SP = 1000; //set stack pointer to 1000
			iCount = 0; //set instruction counter to 0
			
			//set flags to false
			kMode = false;
			exitFlag = false;
		
			//set input and output streams
			this.sc = sc;
			this.pw = pw;
			this.timer = timer;
		}	
		
		public void execute()
		{
			while(!exitFlag) //while not finished
			{
				loadIR(); //load an instruction to IR
			
				if(IR == -1) //if nothing is read from memory
				{
					break; //break loop
				}
				
				iProcess(IR); //process the instruction
				iCount++; //increment instruction counter
				
				if(iCount % timer == 0 && kMode == false) //check if timer interrupt should be called
				{
					enterKMode(); //enter kernel mode
					PC = 1000; //set program counter to 1000
				}
				
				
			}
		}
		
		private void enterKMode()
		{
			kMode = true; //set kernel mode flag
			
			int saveSP = SP; //save old stack pointer
			SP = 2000;
			
			//push all registers
			push(saveSP);
			push(PC);
			push(IR);
			push(AC);
			push(X);
			push(Y);
		}
		
		private void leaveKMode()
		{	
			//pop all registers
			Y = pop();
			X = pop();
			AC = pop();
			IR = pop();
			PC = pop();
			SP = pop();

			kMode = false; //set kernel mode flag
		}
		
		private void push(int d)
		{
			SP--; //decrement stack pointer
			write(SP, d); //write data
		}
		
		private int pop()
		{
			int r = read(SP); //get data at stack pointer
			SP++; //increment stack pointer
			return r; //return retrieved data
		}
		
		private void write(int a, int d)
		{
			pw.printf("write " + a + " " + d + "\n"); //send write command to memory
			pw.flush();
		}
		
		private int read(int a)
		{
			memVio(a); //check if memory violation
			
			pw.printf("read " + a + "\n"); //send read command to memory
			pw.flush();
			
			if(sc.hasNextLine()) //check if there is something to read
			{
				String line = sc.nextLine(); //read instruction
				int r = Integer.parseInt(line);
				return r; //return instruction
			}
			else //if there is nothing to read
			{
				return -1; //return invalid number
			}
			
		}
		
		private void loadIR()
		{
			IR = read(PC); //read value at program counter
			PC++; //increment program counter
		}
		
		private void memVio(int a)
		{
			if(!kMode && a >= 1000) //if not in kmode and the address is greater than 1000
			{
				System.out.println("Can't access this memory in user mode"); //output error
				System.exit(-1);				
			}
		}
		
		
		private void iProcess(int i)
		{
			switch(IR)
			{
				case 1: //Load the value into the AC
				{
					loadIR();
					AC = IR;
					break;
				}
				case 2: //Load the value at the address into the AC
				{
					loadIR();
					AC = read(IR);
					break;
				}
				case 3: //Load the value from the address found in the given address into the AC
				{
					loadIR();
					int temp = read(IR);
					AC = read(temp);
					break;
				}
				case 4: //Load the value at (address+X) into the AC
				{
					loadIR();
					AC = (read(IR + X));
					break;
				}
				case 5: //Load the value at (address+Y) into the AC
				{
					loadIR();
					AC = (read(IR + Y));
					break;
				}
				case 6: //Load from (Sp+X) into the AC (if SP is 990, and X is 1, load from 991).
				{
					AC = read(SP + X);
					break;
				}
				case 7: //Store the value in the AC into the address
				{
					loadIR();
					write(IR, AC);
					break;
				}
				case 8: //Gets a random int from 1 to 100 into the AC
				{
					Random generate = new Random();
					int r = generate.nextInt(100) + 1;
					AC = r;
					break;
				}
				case 9: //If port=1, writes AC as an int to the screen If port=2, writes AC as a char to the screen
				{
					loadIR();
					
					if(IR == 1)
					{
						int out = (int)AC;
						System.out.print(out);
					}
					else if(IR == 2)
					{
						char out = (char)AC;
						System.out.print(out);
					}
					else
					{
						System.out.println("Invalid port.");
					}
					
					break;
				}
				case 10: //Add the value in X to the AC
				{
					AC = AC + X;
					break;
				}
				case 11: //Add the value in Y to the AC
				{
					AC = AC + Y;
					break;
				}
				case 12: //Subtract the value in X from the AC
				{ 
					AC = AC - X;
					break;
				}
				case 13: //Subtract the value in Y from the AC
				{
					AC = AC - Y;
					break;
				}
				case 14: //Copy the value in the AC to X
				{
					X = AC;
					break;
				}
				case 15: //Copy the value in X to the AC
				{
					AC = X;
					break;
				}
				case 16: //Copy the value in the AC to Y
				{
					Y = AC;
					break;
				}
				case 17: //Copy the value in Y to the AC
				{
					AC = Y;
					break;
				}
				case 18: //Copy the value in AC to the SP
				{
					SP = AC;
					break;
				}
				case 19: //Copy the value in SP to the AC 
				{
					AC = SP;
					break;
				}
				case 20: //Jump to the address
				{
					loadIR();
					PC = IR;
					break;
				} 
				case 21: //Jump to the address only if the value in the AC is zero
				{
					loadIR();
					
					if(AC == 0)
					{
						PC = IR;
					}
					
					break;
				}
				case 22: //Jump to the address only if the value in the AC is not zero
				{
					loadIR();
					
					if(AC != 0)
					{
						PC = IR;
					}
					
					break;
				}
				case 23: //Push return address onto stack, jump to the address
				{
					loadIR();
					push(PC);
					PC = IR;
					break;
				}
				case 24: //Pop return address from the stack, jump to the address
				{
					int RA = pop();
					PC = RA;
					break;
				}
				case 25: //Increment the value in X
				{
					X++;
					break;
				}
				case 26: //Decrement the value in X
				{
					X--;
					break;
				}
				case 27: //Push AC onto stack
				{
					push(AC);
					break;
				}
				case 28: //Pop from stack into AC
				{
					AC = pop();
					break;
				}
				case 29: //Perform system call
				{
					if(!kMode)
					{
						enterKMode();
						PC = 1500;
					}
					
					break;
				}
				case 30: //Return from system call
				{
					leaveKMode();
					break;
				}
				case 50: //End execution
				{
					pw.printf("exit");
					exitFlag = true;
					break;
				}
				default: //Invalid instruction
				{
					System.out.println("Invalid instruction.");
					System.exit(-1);
					break;
				}
			}
			
		}
	}
	
	
	
}

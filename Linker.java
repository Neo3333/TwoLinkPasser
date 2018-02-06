import java.io.*;
import java.util.*;
public class Linker {
	static HashMap<String, Integer> symTable = new HashMap<String, Integer>();//symbol table
	static HashMap<String, Integer> checkTable = new HashMap<String, Integer>();// check whether all the defined symbols are used
	static ArrayList<Integer> modBaseAdd = new ArrayList<Integer>();//stores the base address of each module
	static ArrayList<ArrayList<String>> useList = new ArrayList<ArrayList<String>>();//2d array, containing all the useLists
	static ArrayList<ArrayList<String>> defList = new ArrayList<ArrayList<String>>();//2d array, containing all the defLists
	static ArrayList<ArrayList<String>> instructions = new ArrayList<ArrayList<String>>();// 2d array, containing all the instructions
	static int mod_count = 0;
	static int add_offset;
	static int totalAdd;
	
	public static Scanner scan(String filename){
		 try{
			 Scanner input = new Scanner(new BufferedReader(new FileReader(filename)));
			 return input;
		 }
		 catch(Exception e) {
			 System.out.println("no such file" + filename);
			 System.exit(0);
		 }
		 return null;
	}
	
	public static void firstpass(Scanner input){
		input.nextLine();
		int lineNum = 0;
		int addOffset = 0;
		String[] words = null;
		ArrayList<String> wordList = new ArrayList<String>();
		while (input.hasNext()){
			ArrayList<String> useList1 = new ArrayList<String>();
			ArrayList<String> defList1 = new ArrayList<String>();
			ArrayList<String> instList1 = new ArrayList<String>();
			String currentLine = input.nextLine();
			words = currentLine.split("\\s+");
			for (int i = 0; i < words.length; i++){
				if (!"".equals(words[i])){
					wordList.add(words[i]);
				}
			}
			if ((lineNum % 3) == 0){
				int defSize =Integer.parseInt(wordList.get(0));
				for (int i = 0; i < defSize; i++){
					String temp1 = wordList.get(1 + 2 * i);
					int temp2 = Integer.parseInt(wordList.get(2 + 2 * i));
					if (symTable.get(temp1) == null){
						defList1.add(temp1);
						symTable.put(temp1, addOffset + temp2);
						checkTable.put(temp1, 1);
					}else{
						System.out.println("Error: the symbol " + temp1 + " has already been defined" );
					}
				}
				defList.add(defList1);
			}
			else if((lineNum % 3) == 1){
				int useSize = Integer.parseInt(wordList.get(0));
				for (int i = 0; i < useSize; i++){
					String temp = wordList.get(1 + i);
					useList1.add(temp);
				}
				useList.add(useList1);
			}
			else if((lineNum % 3) == 2){
				int instSize = Integer.parseInt(wordList.get(0));
				for (int i = 0; i < instSize; i++){
					String temp1 = wordList.get(1 + 2 * i);
					String temp2 = wordList.get(2 + 2 * i);
					instList1.add(temp1);
					instList1.add(temp2);
				}
				instructions.add(instList1);
				modBaseAdd.add(addOffset);
				addOffset += instSize;
				mod_count ++;
			}
			lineNum++;
			wordList.clear();
			totalAdd = addOffset - 1;
		}

	}
	public static void secondpass(){
		ArrayList<String> useList1 = new ArrayList<String>();
		ArrayList<String> defList1 = new ArrayList<String>();
		ArrayList<String> instList1 = new ArrayList<String>();
		System.out.println("Symbol Table");
		for (String k : symTable.keySet()){
			System.out.println(k + " = " + symTable.get(k));
		}
		System.out.println("\t" + "\t" + "Memory Map");
		for (int i = 0; i < mod_count; i++){
			System.out.println("+" + modBaseAdd.get(i));
			useList1 = useList.get(i);
			defList1 = defList.get(i);
			instList1 = instructions.get(i);
			int instSize = instList1.size() / 2;
			int[] checkuse = new int[useList1.size()];
			for (int q = 0; q < checkuse.length; q++){
				checkuse[q] = 1;
			}
			String[] def = new String[instSize];
			for(int t = 0; t < defList1.size(); t++){
				if (symTable.get(defList1.get(t)) - modBaseAdd.get(i) < instSize){
					def[symTable.get(defList1.get(t)) - modBaseAdd.get(i)] = defList1.get(t);
				}else{
					System.out.println("Error: symbol " + defList1.get(t) + " exceeds the module "
							+ "size" );
				}
			}
			for(int j = 0; j < instSize; j++){
				if (def[j] != null) System.out.print(j + ":" + def[j]+ "\t");
				else System.out.print(j + ":" + "\t");
				System.out.print(instList1.get(2 * j) + " " + instList1.get(2 * j + 1) + " ");
				String temp1 = instList1.get(2 * j);
				int temp2 = Integer.parseInt(instList1.get(2 * j + 1));
				switch (temp1){
				case "R":
					int result = temp2 + modBaseAdd.get(i);
					int check = (result % 100);
					if (check <= totalAdd){
						System.out.print("\t" + "\t"+ temp2 + "+" + modBaseAdd.get(i) + " =" + result + "\n");
					}else{
						int temp = result - check;
						System.out.print("\t" + "\t"+ "\t" + temp + "\n");
						System.out.println("Error: the relative address exceeds total address");
					}
					break;
				case "I":
					System.out.print("\t" + "\t"+"\t"+ temp2 + "\n");
					break;
				case "E":
					int temp3 = (temp2 % 10);
					if (temp3 >= useList1.size()){
						System.out.print("\t" + "\t" + "\t" + temp2 + "\n");
						System.out.println("Error: the address exceeds the uselist");
					}
					else if (symTable.get(useList1.get(temp3)) != null){
						int checkNum = checkTable.get(useList1.get(temp3));
						checkNum --;
						checkTable.remove(useList1.get(temp3));
						checkTable.put(useList1.get(temp3), checkNum);
						int temp4 = temp2 - temp3 + symTable.get(useList1.get(temp3));
						int temp5 = checkuse[temp3];
						checkuse[temp3] = temp5 - 1;
						System.out.print("->" + useList1.get(temp3) + "\t" +"\t" + temp4 + "\n");
					}else{
						int temp4 = temp2 -temp3;
						System.out.print("->" + useList1.get(temp3) + "\t" + "\t" + temp4 + "\n" );
						System.out.println("Error: symbol " + useList1.get(temp3) + " hasn't been "
								+ "defined, initialize as zero");
						int temp5 = checkuse[temp3];
						checkuse[temp3] = temp5 - 1;
					}
						break;
				case "A":
					int temp = temp2 % 10 + temp2 % 100;
					if (temp > totalAdd){
						temp3 = temp2 - temp;
						System.out.print("\t" + "\t" + "\t" + temp3 + "\n");
						System.out.println("Error: the absolute address exceeds the total address"
								+ " ,initialize to zero");
					}else{
						System.out.print("\t" + "\t" + "\t" + temp2 + "\n");
					}
					break;
				default:
					System.out.print("no such command" + "\n");
					break;
				}
			}
			for (int k = 0; k < checkuse.length; k++){
				if (checkuse[k] > 0) System.out.println("Warning: symbol " + useList1.get(k) + " "
						+ "is not used in this module");
			}
		}
		System.out.println();
		for (String k : checkTable.keySet()){
			if (checkTable.get(k) > 0) System.out.println("Error: the symbol " + k + " is defined"
					+ "but not used");
		}
	}
	public static void main(String args[]){
		System.out.println("Please enter the input file to be test");
		Scanner s = new Scanner(System.in);
		String filename =  s.nextLine();
		Scanner input = scan(filename);
		firstpass(input);
		secondpass();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}

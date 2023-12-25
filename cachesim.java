//initalize memory as array, memory address as index 
//initialize cache as hashmap with key as set and values as value bit 

import java.util.ArrayDeque;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class cachesim{

    public static void main (String[]args) throws IOException{
        //initializing variables 
        String filename = args[0];
        int size = Integer.parseInt(args[1]);
        int associativity = Integer.parseInt(args[2]);
        int block_size = Integer.parseInt(args[3]);

        //Calculating number of sets 
        int block_exponent_size = findExponent(block_size);
        int associativity_size = findExponent(associativity);


        int sets = (size *1024/block_size)/associativity;
    


        //Initalizing cache 
        String[] memory = new String[17123412]; //memory array index value corresponding to memory address //store in 2 bits at a time 
        //change to string array, split up by 2 hex each time 
        HashMap<Integer,ArrayDeque<Integer>> cache= new HashMap <>(); //HashMap with set number as value and tags stored as array
        for(int i=0;i<sets;i++){
            cache.put(i, new ArrayDeque<Integer>());
        }

        processFile(filename,memory,cache,sets,block_size,associativity);
    
    }




    private static int findExponent(int n) {
        int r=0;
        while (n>1) {
        n=n>>1;
        r++;
    };
        return r;
    }
    

    private static void processFile(String filename,String[]memory,HashMap<Integer,ArrayDeque<Integer>>cache, int sets, int block_size, int associativity) throws IOException{
        int index_bits = findExponent(sets);
        int block_size_bits = findExponent(block_size);
        int tag_bits = 24-index_bits - block_size_bits;

        try(BufferedReader reader = new BufferedReader(new FileReader(filename))){
            String line;
            while((line=reader.readLine())!=null){
                processLine(line,memory,cache,index_bits, block_size_bits, associativity, block_size, sets);
            }
            System.exit(0);
        }
    }

    private static int ones (int offset_bits){
            return (1<<offset_bits)-1;
    }

    private static void processLine(String line,String[]memory,HashMap<Integer,ArrayDeque<Integer>>cache,int index_bits, int block_size_bits, int associativity, int block_size, int sets){
        
        String[] arr = line.split(" "); //contains diff instructions
        String[] fin = new String[4]; //final string to be returned 
        fin[1] = arr[1];
        //parse 0x out of string 
        if(arr[1].startsWith("0x")||arr[1].startsWith("0X")){
            arr[1]=arr[1].substring(2);
        }

        int address = Integer.parseInt(arr[1],16); //hexadecimal address in line 
      
        int index = (address>> block_size_bits)& ones(index_bits); //index = specific number of sets in java
        int tag = address>> (index_bits+block_size_bits); //tag-bits = shift address to the right by this amount 

            if(cache.get(index).contains(tag)){ //if cache contains tag 
                // System.out.println("contained");
                fin[2] = "hit";

                //updates in priority queue 
                cache.get(index).remove(tag);
                cache.get(index).offer(tag);                     
                String yum = "";

                if (arr[0].equals("load")){ //load and hit 
                    fin[0] = "load";
                    //if contains tag and we want to load
                    
                      for(int i =0;i<Integer.parseInt(arr[2]);i++){ //for loop to receive from memory 
                        if(memory[address+i]==null){
                            yum = yum + "00";
                        }
                        else{
                            yum = yum + memory[address+i]; //do we need to search for tag in address or is the value just stored in the memory address 
                        }
                }   



                }
                else{ //load and miss 
                    fin[0] = "store";

                    char[] split = new char[arr[3].length()];
                    for(int i=0;i<arr[3].length();i++){
                       split[i]= arr[3].charAt(i);
                       //creates a char array of characters in arr[3];
                    }

                    String addition = "";
                    String[] hexchar = new String[arr[3].length()/2];

                    int counter = 0;
                    for (int i=0;i<split.length;i++){ //creates string array with two char as one value 
                        addition = Character.toString(split[i]) + Character.toString(split[i+1]);
                        hexchar[counter] = addition;
                        counter+=1;
                        i+=1;
                    }
                    for(int i=0;i<hexchar.length;i++){ //places into memory 
                        memory[address+i] = hexchar[i];
                    }

                }
                fin[3] = yum; //places number stored in load 

            }
            else{ //not in priority queue = miss
                fin[2] = "miss"; //priority queue does not include cache 
                
               
            if(arr[0].equals("load")){
                fin[0]= "load"; //if load and miss

                if(cache.get(index).size()>=associativity){ //check if size is greater, if greater  then remove -> CHECK SIZE OF CACHE 
                    cache.get(index).poll();
                }
                cache.get(index).offer(tag); //add to cache, need to check if limit greater than needed 
                //check that this is correct method 
                
                String yum = "";
              
                for(int i =0;i<Integer.parseInt(arr[2]);i++){ //for loop to receive from memory 
                        if(memory[address+i]==null){
                            yum = yum + "00";
                        }
                        else{
                            yum = yum + memory[address+i]; //do we need to search for tag in address or is the value just stored in the memory address 
                        }
                }                
            
            fin[3] = yum;
            }
            else{

                fin[0] = "store";

                    char[] split = new char[arr[3].length()];
                    for(int i=0;i<arr[3].length();i++){
                       split[i]= arr[3].charAt(i);
                       //creates a char array of characters in arr[3];
                    }

                    String addition = "";
                    String[] hexchar = new String[arr[3].length()/2];

                    int counter = 0;
                    for (int i=0;i<split.length;i++){ //creates string array with two char as one value 
                        addition = Character.toString(split[i]) + Character.toString(split[i+1]);
                        hexchar[counter] = addition;
                        counter+=1;
                        i+=1;
                    }
                    for(int i=0;i<hexchar.length;i++){ //places into memory 
                        memory[address+i] = hexchar[i];
                    }
                    fin[3] = ""; 
            }

        }
        
        String mergedString ="";
        for (String str:fin){
            mergedString+=" " + str;
        }

        System.out.println(mergedString);

    }

}
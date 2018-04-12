import java.io.*;
import java.util.*;
/**
 * Baby-DES Encryptor/Decryptor 
 * 
 * The program prompts the user for a filename, a key, whether to encrypt or decrypt, and the encryption mode (electronic codebook or cipher block chain). 
 * User responses are read from the keyboard. The program output should be written to a file corresponding to the input file: if the input filename was 
 * mymsg.txt the output file should be either mymsg_enc.txt or mymsg_dec.txt, depending upon whether we are encrypting or decrypting. 
 * 
 * note: could use some more organization and some code is repetitive and could be combined into one method
 *       but should function correctly
 *      
 * 
 * @author (Wyatt Sawyer)
 * @version (April 1st, 2018)
 */
public class BabyDES
{

    private String crypt;
    private ArrayList<String> pt;
    private String key;
    private String mode; 
    private String[] Sbox1 = {"101","010","001","110","011","100","111","000","001","100","110","010","000","111","101","011"};
    private String[] Sbox2 = {"100","000","110","101","111","001","011","010","101","011","000","111","110","010","001","100"};
    private final boolean DEBUG = false;

    /**
     * User will be prompted for a filename, a key, whether to encrypt or decrypt, and the encryption mode.
     * Then output will be printed to a filename that will be shown
     * 
     */
    public void main() 
    {
        String args[] = new String[4];
        String file = "";
        Scanner input = new Scanner(System.in);  
        System.out.print("Enter e to encrypt, or d to decrypt: ");
        args[0] = input.next();
        System.out.print("\nEnter the file name (ex: mymsg.txt): ");
        args[1] = input.next(); 
        System.out.print("\nEnter the 9 binary digit key (ex: 101010101): ");
        args[2] = input.next(); 
        System.out.print("\nEnter e for electronic codebook, or c for cipher block chain: ");
        args[3] = input.next(); 

        if(DEBUG)
        {
            for(String a: args)
            {
                System.out.println(a);
            }
        }
        input.close();

        //fill in variables
        crypt = args[0];
        key = args[2];
        mode = args[3];
        //initialize s-boxes

        //read in the file and fill in plaintext into arraylist pt
        pt = new ArrayList<String>();
        try {

            FileReader fr = new FileReader(args[1]);
            BufferedReader br = new BufferedReader(fr);
            String line = null;

            while((line = br.readLine()) != null) {
                pt.add(line);

            }   
            br.close(); 
        }
        catch(FileNotFoundException e)
        {
            System.err.println(e);
        }
        catch(IOException e)
        {
            System.err.println(e);
        }

        try {
            String method = "";
            if(crypt.equals("e"))method = "enc";
            else method = "dec";
            int endName = args[1].indexOf(".");
            file = (args[1].substring(0,endName) + "_" + method + ".txt");
            PrintWriter newFile = new PrintWriter(file, "UTF-8");
            //now check which method needs to be executed
            if(crypt.equals("e") && mode.equals("e"))//encrypt EBC
            {
                ArrayList<String> done = encryptEBC(4);
                for ( String a : done)
                {
                    newFile.println(a);
                }

            }
            else if(crypt.equals("e") && mode.equals("c"))//encrypt CBC
            {
                ArrayList<String> done = encryptCBC(4);
                for ( String a : done)
                {
                    newFile.println(a);
                }
            }
            else if(crypt.equals("d") && mode.equals("e"))//decrypt EBC
            {
                ArrayList<String> done = decryptEBC(4);
                for ( String a : done)
                {
                    newFile.println(a);
                }
            }
            else if(crypt.equals("d") && mode.equals("c"))//decrypt CBC
            {
                ArrayList<String> done = decryptCBC(4);
                for ( String a : done)
                {
                    newFile.println(a);
                }
            }
            else //uesr didn't fill in arguments correctly
            {
                newFile.println("Input(s) were not correct");
            }

            newFile.close(); 
            System.out.println("\nOutput was printed to new file : " + file);
        }
        catch(FileNotFoundException e)
        {
            System.err.println(e);
        }
        catch(IOException e)
        {
            System.err.println(e);
        }

    }

    /**
     * A method that returns a string that is the product of the XOR of two input strings
     * created because of an issue using the int operator for XOR "^", because leading zeros when parsing Strings to ints would be lost
     */
    public String XOR(String a, String b)
    {
        String temp = "";
        for(int i = 0; i<a.length(); i++)
        {
            if(a.charAt(i) == b.charAt(i))
            {
                temp = temp + "0";
            }
            else
            {
                temp = temp + "1";
            }

        }
        return temp;
    }

    /**
     * decryptEBC decrypts an array list of plaintext blocks using electronic codebook
     * 
     * @return : arraylist of the decrypted lines
     * @param : numRounds - number of rounds of encryption
     */
    public ArrayList<String> decryptEBC(int numRounds)
    {
        ArrayList<String> origPlainText = new ArrayList<String>();
        for(int i = 0; i<pt.size(); i++)
        {

            for(int j = numRounds-1; j>=0; j--)//in decryption you use last rounds
            {
                String a = "";
                String b = "";
                if(j == numRounds-1)//if first round of decryption
                {
                    a = pt.get(i);                    
                    b = roundReverse(a, j); //will return the new left
                    origPlainText.add(b);
                }
                else
                {
                    a = origPlainText.get(i);
                    b = roundReverse(a, j); //will return the new left
                    origPlainText.set(i,b);
                }

            }

        }
        return (origPlainText);
    }

    /**
     * encryptEBC encypts an array list of plaintext blocks using electronic codebook
     * 
     * @return : arraylist of the encrypted lines
     * @param : numRounds - number of rounds of encryption
     */
    public ArrayList<String> encryptEBC(int numRounds)
    {

        ArrayList<String> finalPlainText = new ArrayList<String>();
        for(int i = 0; i<pt.size(); i++)
        {
            for(int j = 0; j<numRounds; j++)
            {
                String a = "";
                String b = "";
                if(j == 0)//if first round
                {
                    a = pt.get(i);
                    b = round(a, j);
                    finalPlainText.add(b);
                }
                else
                {
                    a = finalPlainText.get(i);
                    b = round(a, j);
                    finalPlainText.set(i, b);
                }

            }
        }
        return (finalPlainText);
    }

    /**
     * decryptCBC decrypts an array list of plaintext blocks using cipher block chain
     * 
     * @return : arraylist of the decrypted lines
     * @param : numRounds - number of rounds of encryption
     */
    public ArrayList<String> decryptCBC(int numRounds)
    {
        ArrayList<String> origPlainText = pt;
        String iv = "111111111111";
        for(int i = pt.size()-1; i>=0; i--)
        {
            String a = "";
            String b = "";
            for(int j = numRounds-1; j>=0; j--)//in decryption you use last rounds
            {

                a = origPlainText.get(i);
                b = roundReverse(a, j); //will return the new left
                origPlainText.set(i,b);

            }
            if(i==0)
            {
                a = origPlainText.get(i);
                b = XOR(a,iv);
            }
            else
            {
                a = origPlainText.get(i);
                b = XOR(a,origPlainText.get(i-1));
            }
            origPlainText.set(i,b);

        }
        return (origPlainText);
    }

    /**
     * encryptCBC encypts an array list of plaintext blocks using cipher block chain
     * 
     * @return : arraylist of the encrypted lines
     * @param : numRounds - number of rounds of encryption
     */
    public ArrayList<String> encryptCBC(int numRounds)
    {

        String iv = "111111111111"; //initialization vector of all 1's
        ArrayList<String> finalPlainText = new ArrayList<String>();
        for(int i = 0; i<pt.size(); i++)
        {
            String a = "";
            String b = "";
            String newA = "";
            if(i==0) //if first line
            {
                a = pt.get(i);
                newA = XOR(a,iv);
                b = round(newA, 0);//first round is done
                for(int j=1; j<numRounds; j++)
                {
                    b = round(b, j);
                }

            }
            else
            {
                a = pt.get(i);
                newA = XOR(a,finalPlainText.get(i-1));
                b = round(newA, 0);
                for(int j=1; j<numRounds; j++)
                {
                    b = round(b, j);
                }

            }
            finalPlainText.add(b);
        }

        return (finalPlainText);
    }

    /**
     * round does one round of the Feistel process
     * 
     * @return : ciphertext of one String after one round
     * @param : roundNumber - int of the round number so the key of the round can be determined
     */
    public String round(String plainText, int roundNumber)
    {
        if (DEBUG)System.out.println("ENCRYPT " + plainText);
        String txt = plainText;
        String left = txt.substring(0,6);
        String right = txt.substring(6);
        if (DEBUG)System.out.println("1st left->" + left);
        if (DEBUG)System.out.println("1st right->" + right);

        //expand right
        String a = right.substring(2,3);
        String b = right.substring(3,4);
        right = (right.substring(0,2) + b + a + b + a + right.substring(4));
        if (DEBUG)System.out.println("expanded right->" + right);

        //develop the key depending on which round it is
        String newKey = null;
        if(roundNumber == 0)
        {
            newKey = key.substring(roundNumber,8);
        }
        else
        {
            newKey = key.substring(roundNumber) + key.substring(0,roundNumber-1);
        }
        if (DEBUG)System.out.println("KEY : " + newKey);
        //XOR the right with the key
        String s = XOR(right,newKey);
        if (DEBUG)System.out.println("s->" + s);

        //search S boxes
        int c = Integer.parseInt(s.substring(0,4),2);
        int d = Integer.parseInt(s.substring(4),2);

        //resusing the temporary values 
        String tempC = Sbox1[c];
        String tempD = Sbox2[d];

        if (DEBUG)System.out.println("Spot in sbox1->" + c);
        if (DEBUG)System.out.println("Spot in sbox2->" + d);
        if (DEBUG)System.out.println("value1->" + tempC);
        if (DEBUG)System.out.println("value2->" + tempD);
        s = (tempC + tempD);
        if (DEBUG)System.out.println("newS->" + s);

        //XOR with left
        String newR = XOR(s,left);
        if (DEBUG)System.out.println("newS XOR w/ left" + newR);

        //left becomes original right before the round & right is updated after the round
        left = txt.substring(6);
        right = newR;
        if (DEBUG)System.out.println("final left->" + left);
        if (DEBUG)System.out.println("final right->" + right + "\n");

        return(left + right);

        //System.out.println(left + " " + right);
    }

    /**
     * roundReverse does one round of the Feistel process with some things changed for decryption
     * 
     * note: this method could be combined into the round method for more organization
     * 
     * @return : plaintext of one String after one round of decryption
     * @param : roundNumber - int of the round number so the key needed can be determined
     */
    public String roundReverse(String plainText, int roundNumber)
    {
        if (DEBUG)System.out.println("DECRYPT " + plainText);
        String txt = plainText;
        String left = txt.substring(0,6); //RETURN THIS AS FINAL RIGHT
        String right = txt.substring(6); 
        if (DEBUG) System.out.println("1st left->" + left);
        if (DEBUG)System.out.println("1st right->" + right);

        //expand left
        String a = left.substring(2,3);
        String b = left.substring(3,4);
        left = (left.substring(0,2) + b + a + b + a + left.substring(4));
        if (DEBUG)System.out.println("expanded left->" + left);

        //develop the key depending on which round it is
        String newKey = null;
        if(roundNumber == 0)
        {
            newKey = key.substring(roundNumber,8);
        }
        else
        {
            newKey = key.substring(roundNumber) + key.substring(0,roundNumber-1);
        }
        if (DEBUG)System.out.println("KEY : " + newKey);
        //XOR the right with the key
        String s = XOR(left,newKey);
        if (DEBUG)System.out.println("s->" + s);

        //search S boxes
        int c = Integer.parseInt(s.substring(0,4),2);
        int d = Integer.parseInt(s.substring(4),2);

        //resusing the temporary values 
        String tempC = Sbox1[c];
        String tempD = Sbox2[d];

        if (DEBUG)System.out.println("Spot in sbox1->" + c);
        if (DEBUG)System.out.println("Spot in sbox2->" + d);
        if (DEBUG)System.out.println("value1->" + tempC);
        if (DEBUG)System.out.println("value2->" + tempD);
        s = (tempC + tempD);
        if (DEBUG)System.out.println("newS->" + s);

        //XOR with left
        if (DEBUG)System.out.println("XOR s " + s + " with right " + right);
        String newL = XOR(s,right);
        if (DEBUG)System.out.println("newS XOR w/ right" + newL);

        //right becomes original left before the round & left is updated after the round
        right = txt.substring(0,6);
        left = newL;
        if (DEBUG)System.out.println("final left->" + left);
        if (DEBUG)System.out.println("final right->" + right + "\n");

        return(left + right);
    }

}

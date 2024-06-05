import CodeGeneration.IMCGenerator;
import Lexer.Lexer;
import Lexer.LinkedList;
import Node.TreeNode;
import Parser.Parser;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.CoderMalfunctionError;
import java.util.Scanner;
import Semantics.Scoping;
import Semantics.Naming.VariableAnalysis;
import java.util.regex.*;

public class Main {
    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        String directory = "";
        int count = 1;

        try{
////////////////////////////START///////////////////////////////////////////////////////
//..........................Lexer.......................................................
            Lexer lex = new Lexer("src/test/test2.txt");
            System.out.println("Lexing.......................");
            LinkedList lst = lex.start();
            System.out.println("Done Lexing..................");

//..........................Parser......................................................
            Parser parser = new Parser(lst);
            System.out.println("Parsing......................");
            TreeNode parsedTree = parser.start(true, false);
            String treeString = parser.printTree();
            writeToFile(treeString, count++);
            System.out.println("Done Parsing.................");

//..........................Type Checker................................................
            System.out.println("Type Checking................");
            System.out.println("Scoping......................");
            Scoping scoping = new Scoping(parsedTree);
            TreeNode scopedTree = scoping.start();
            treeString = scoping.printTree();
            writeToFile(treeString, count++);
            System.out.println("Done Scoping.................");
            System.out.println("Variable Checking............");
            VariableAnalysis varAnalysis = new VariableAnalysis(scopedTree);
            TreeNode namedTree = varAnalysis.start();
            treeString = varAnalysis.printTree();
            writeToFile(treeString, count++);
            System.out.println("Done Variable Checking.......");
            System.out.println("Done Type Checking...........");
            System.out.println("Generating Code..............");
            IMCGenerator codeGen = new IMCGenerator(namedTree);
            TreeNode GeneratedCode = codeGen.start();
            writeToFile(treeString, count++);
            System.out.println("Done generating code.........");
//..........................Code Generator...............................................


/////////////////////////////////DONE////////////////////////////////////////////////////
        }catch (Exception e){
            System.out.println("Error found");
            if(e.getMessage() == null)
                e.printStackTrace();
            else
                writeToFile(e.getMessage(), count++);
        }
    }

    private static void writeToFile(String str, int i)
    {
        try
        {
            String filepath = System.getProperty("user.dir")+"\\results"+i+".txt";
            File resultFile = new File(filepath);
            resultFile.createNewFile();

            FileWriter myWriter = new FileWriter(filepath, false);
            myWriter.write(str);
            myWriter.close();
            System.out.println("results saved to "+filepath);
        } catch (IOException e)
        {
            System.out.println("An error occurred when writing to file.");
//            e.printStackTrace();
        }
    }
}

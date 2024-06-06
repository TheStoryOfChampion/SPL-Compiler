package Lexer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.regex.*;
import Node.Node;
import Node.NodeSubType;
import Node.NodeType;

public class Lexer {

    private final LinkedList lst;
    private final String filePath;
    public Lexer(String pFilePath)
    {
        lst = new LinkedList();
        filePath = pFilePath;
    }
    public LinkedList start() throws Exception
    {
        String message;
        if(filePath.trim().isEmpty())
        {
            throw new Exception("No File path Specified");
        }

        File oFile = new File(filePath);
        FileReader oFileReader = new FileReader(oFile);
        BufferedReader oBufferReader = new BufferedReader(oFileReader);

        int iNextChar;

        while ((iNextChar = oBufferReader.read()) != -1)
        {
            char c = (char) iNextChar;

            // Check if valid Char
            if(!isValidChar(c))
            {
                System.out.println("is here......................Invalid Char");
                message = invalidTokenError()+ c +"(ascii: "+(int) c+"). Scanning aborted";
                lst.add(new Node( message, NodeType.Error));
                throw new Exception(message);
            }else
            {
//                System.out.println("is here......................");
                if (c == '"')   //TODO instead of listing all characters here manually, and risk making mistakes use the
                // isStartOf(enum, char) function to test if the character is a start symbol for
                // a specific non-Terminal
                {
                    // Short String
                    // System.out.println("+ short String");
                    shortString(oBufferReader, c);          //TODO you do not have to pass in the BufferedReader to each
                    // function as long as it is a class member

                    //TODO give each method a name related to the nonTerminal it
                    // is lexing, makes it easier to track

                    //TODO This implementation stop lexing when an invalid token
                    // is read or the string is not part of the SPL language
                }
                else if( (c >= 'a') && (c <= 'z'))
                {
                    // UDN or keyword
                    // System.out.println("+ UDN/Keyword");
                    readKeywordOrUDN(oBufferReader, c);
                }
                else if ( (c >= '0') && (c <= '9') ||  (c == '-') )
                {
                    // Number
                    // System.out.println("+ Number");
                    readNumber(oBufferReader, c);
                }
                else if((c == 'T') || (c == 'F')){
                    isTruth(c);
                }
                else if(c == '='){
                    insertAssignmentSymbol(c, oBufferReader);
                }
                else if(!isValidWhiteSpace(c))
                {
                    if(isGroupingSymbol(c))
                    {
                        insertGroupingSymbol(c);
                    }
                    else if (c ==':')
                    {
                        insertAssignmentSymbol(c, oBufferReader);
                    }
                    else {
                        message = classErrorName()+ c +"(ascii "+iNextChar+") Unidentified error. Scanning aborted";
                        lst.add(message, NodeType.Error);
                        throw new Exception(message);
                    }
                }

            }


        }
        return lst;
    }

    private void readNumber(BufferedReader bReader, char c) throws Exception
    {
        int iNextChar;
        String message;
        StringBuilder sNumber;

        if((iNextChar = bReader.read()) != -1)
        {
            if (c == '-' && (!isNumber((char) iNextChar) || ('0' == (char) iNextChar)))
            {
                // '-' can only be followed by a non-zero number
                // Error if iNextChar is anything other than a non-zero umber
                message = unexpectedTokenError()+" "+ (char) iNextChar + " (ascii "+iNextChar+") after '-' ";
                lst.add(new Node(message, NodeType.Error));
                throw new Exception(message);
            }
            else if(c == '0' && (isNumber((char) iNextChar) || isLetter((char) iNextChar)))
            {
                // '0' cannot be followed by a number or letter
                // Error if iNextChar is a letter of number
                message = unexpectedTokenError() + (char) iNextChar + " (ascii "+iNextChar+") after '0' ";
                lst.add(new Node(message, NodeType.Error));
                throw new Exception(message);
            }
            else
            {
                sNumber = new StringBuilder(String.valueOf(c));
            }
            do
            {
                c = (char) iNextChar;
                if(isNumber(c))
                {
                    // if c== '-' (when the function is called) this should always run on the first iteration
                    sNumber.append(c);
                }
                else if(isValidWhiteSpace(c))
                {
                    // white space after number
                    // will never run on the first iteration because of second 'if', when c == '-'
                    lst.add(new Node(sNumber.toString(), NodeType.Number));
                    return;
                }
                else if(isGroupingSymbol(c))
                {
                    // grouping symbol after number
                    // will never run on the first iteration because of second 'if', when c == '-'
                    lst.add(new Node(sNumber.toString(), NodeType.Number));
                    insertGroupingSymbol(c);
                    return;
                }
                else
                {
                    // Any other character besides a number, white space or grouping symbol is not valid
                    message = unexpectedTokenError()+" "+c+" (ascii "+(int)c+").";
                    lst.add(new Node(message, NodeType.Error));
                    throw new Exception(message);
                }
            } while (((iNextChar = bReader.read()) != -1));
        }
        else
        {
            if (c == '-')
            {
                // c== '-' and no number follows after
                message = classErrorName()+" Unexpected end of string after -";
                lst.add(new Node(message, NodeType.Error));
                throw new Exception(message);
            }
        }
        // Stops when:
        // 1. while loop condition is false
        // 2. first 'if' is false and c != '-'
        lst.add(new Node(String.valueOf(c), NodeType.Number));
    }

    private void readKeywordOrUDN(BufferedReader oReader, char c) throws Exception
    {
        StringBuilder sStringBuilder = new StringBuilder(String.valueOf(c));
        String message;
        int iNextChar;
        while(((iNextChar = oReader.read()) != -1) && isUDN((char) iNextChar))
        {
            c = (char) iNextChar;
            sStringBuilder.append(c);
        }

        if(!isKeyword(String.valueOf(sStringBuilder)))
        {
            // isKeyword Function automatically adds a new node to list if the string is a keyword
            // else it needs to be manually added as follows
            lst.add(String.valueOf(sStringBuilder), NodeType.UserDefinedName);
        }

        if(c != (char) iNextChar)
        {
            c = (char) iNextChar;
            if(isGroupingSymbol(c))
            {
                insertGroupingSymbol(c);
            }
            else if (c == ':')
            {
                // Reading Assignment Symbol
                insertAssignmentSymbol(c, oReader);
            }
            else if(c == '-')
            {
                readNumber(oReader, c);
            }
            else if(!isValidWhiteSpace(c))
            {
                message = unexpectedTokenError()+" "+c+" (ascii "+(int) c +")";
                lst.add(message, NodeType.Error);
                throw new Exception(message);
            }
        }
    }

    private void insertAssignmentSymbol(char c, BufferedReader oReader) throws Exception
    {
        int iNextChar;
        String message = "";
        lst.add("=", NodeType.Assignment);
        /*if((iNextChar = oReader.read()) != -1)
        {
            if((char)iNextChar == '=')
            {
                // Assignment symbol is correct (:=)
                lst.add("=", NodeType.Assignment);
            }
            else
            {
                // Invalid character after :
                // Was supposed to be an assignment symbol
                message = unexpectedTokenError()+c+ (char)iNextChar+" (ascii "+iNextChar+"+). Expected =";
                lst.add(message, NodeType.Error);
                throw new Exception(message);
            }
        }
        else
        {
            // Invalid character after :
            // Was supposed to be an assignment symbol
            message = classErrorName() + " Unexpected end of program after "+c;
            lst.add(message, NodeType.Error);
            throw new Exception(message);
        }*/
    }

    private void shortString(BufferedReader oReader, char c) throws Exception
    {
        System.out.println("C is "+ c);
        StringBuilder sShortString = new StringBuilder(String.valueOf(c));

        int iStringLength = 0;
        int iNextChar;
        String message;
        while ((iStringLength++ <= 8) && ((iNextChar = oReader.read()) != -1))
        {
            c = (char)iNextChar;
            if( isShortStringChar(c))
            {
                sShortString.append(c);

            }
            else if (c == '"')
            {
                sShortString.append(c);
                lst.add(new Node(sShortString.toString(), NodeType.ShortString));
                break;
            }
            else if (!isValidChar(c))
            {
                message = invalidTokenError()+ c +"(ascii: "+(int) c+"). Scanning aborted";
                lst.add(new Node(message, NodeType.Error));
                throw new Exception(message);
            }
            else
            {
                message = classErrorName() + sShortString+ c +". Invalid string name. Scanning aborted.";
                lst.add(new Node(message, NodeType.Error));
                throw new Exception(message);
            }
        }
        if (iStringLength <3 || iStringLength >10)
        {
            message = classErrorName() + sShortString + ". String is too short. Aborted";
            lst.add(new Node(message, NodeType.Error));
            throw new Exception(message);
        } else {
            return;
        }
        /*message = classErrorName() + sShortString + ". string too long. scanning aborted";
        lst.add(new Node(message, NodeType.Error));
        throw new Exception(message);*/
    }

    private void insertGroupingSymbol(char c) throws Exception
    {
        switch (c)
        {
            /*case '[':
                lst.add(String.valueOf(c), NodeType.Grouping, NodeSubType.LeftSquareBracket);
                break;
            case ']':
                lst.add(String.valueOf(c), NodeType.Grouping, NodeSubType.RightSquareBracket);
                break;*/
            case'{':
                lst.add(String.valueOf(c), NodeType.Grouping, NodeSubType.LeftCurlyBrace);
                break;
            case'}':
                lst.add(String.valueOf(c), NodeType.Grouping, NodeSubType.RightCurlyBrace);
                break;
            case'(':
                lst.add(String.valueOf(c), NodeType.Grouping, NodeSubType.LeftBracket);
                break;
            case')':
                lst.add(String.valueOf(c), NodeType.Grouping, NodeSubType.RightBracket);
                break;
            case';':
                lst.add(String.valueOf(c), NodeType.Grouping, NodeSubType.SemiColon);
                break;
            case',':
                lst.add(String.valueOf(c), NodeType.Grouping, NodeSubType.Comma);
                break;
            case':':
                lst.add(String.valueOf(c), NodeType.Grouping, NodeSubType.Colon);
                break;
            default:
                String message = unexpectedTokenError()+" "+c+" (ascii "+(int)c+"). Expected Grouping symbol";
                lst.add(message, NodeType.Error);
                throw new Exception(message);
        }
    }

    private boolean isShortStringChar(char c)
    {
        return ((c == ' ' || isNumber(c) || ((c >= 'a' && c <= 'z') ||((c >= 'A') && (c <= 'Z'))) ));
    }

    private boolean isUDN(char c)
    {
        return (isNumber(c) || ((c >= 'a') && (c <= 'z')) || c == '_' );
    }

    private boolean isLetter(char c)
    {
        return ((c >= 'A') && (c <= 'Z')) || ((c >= 'a') && (c <= 'z'));
    }

    private boolean isValidChar(char c)
    {
        System.out.println(c);
        if( isNumber(c)) return true;

        if((c >= 'A') && (c <= 'Z')) return true;

        if((c >= 'a') && (c <= 'z')) return true;

        // Carriage return or space
        if (isValidWhiteSpace(c)) return true;

        char[] oCharArray = {'{','}',';',',', ':', '=','(',')','-','"', '_', 'T', 'F'};
        for (char value : oCharArray)
        {
            if (c == value) return true;
        }
        return false;
    }

    private boolean isKeyword(String sUDN)
    {
        String[] boolOperators = {"T", "F", "not", "and", "or"};
        String[] compOperator = {"eq", "larger"};
        String[] numOperators = {"add", "mult"};
        String[] types ={"num", "bool", "proc","text"};
        String[] ioCommands = {"input", "print", "dummy"};
        String[] keywords = { "repeat", "if", "then", "else", "do", "until", "exec", "def"};

        if( stringWithinList( compOperator, sUDN ) )
            lst.add(new Node(sUDN, NodeType.Keyword, NodeSubType.Comparison));

        else if( stringWithinList( ioCommands, sUDN ))
            lst.add(new Node(sUDN, NodeType.Keyword, NodeSubType.IOCommand));

        /*else if( sUDN.equals("proc") )
            lst.add(new Node(sUDN, NodeType.Keyword, NodeSubType.PDKeyword));*/

        else if( stringWithinList( boolOperators, sUDN ) )
            lst.add(new Node(sUDN, NodeType.Keyword, NodeSubType.BooleanOp));

        else if( stringWithinList( numOperators, sUDN ) )
            lst.add(new Node(sUDN, NodeType.Keyword, NodeSubType.NumberOp));
        else if( stringWithinList( types, sUDN ))
            lst.add(new Node(sUDN, NodeType.Keyword, NodeSubType.VarType));

        else if( stringWithinList(keywords, sUDN))
            lst.add(new Node(sUDN, NodeType.Keyword));

        else
            return false;
        return true;
    }

    private boolean isGroupingSymbol(char c)
    {
        char[] symbols = {';',',','(',')',':','{','}'};
        for (char symbol: symbols)
        {
            if(symbol == c) return true;
        }
        return false;
    }

    private boolean stringWithinList(String[] pList, String pString)
    {
        for (String word : pList)
        {
            if(word.equals(pString))
                return true;
        }
        return false;
    }

    private boolean isNumber(char c)
    {
        return '0' <= c && c <= '9';
    }

    private boolean isValidWhiteSpace(char c)
    {
        return ((int) c == 10)|| ((int)c == 13) || ((int)c == 32);
    }

    private String classErrorName()
    {
        return "[Lexical Error] ";
    }

    private String unexpectedTokenError()
    {
        return classErrorName()+" Unexpected token ";
    }

    private String invalidTokenError()
    {
        return classErrorName() +" Invalid Character ";
    }

    private boolean isTruth(char c){
        if((c == 'T')){
            lst.add("T", NodeType.Keyword, NodeSubType.T);
            return true;
        }
        else if(c == 'F'){
            lst.add("F", NodeType.Keyword, NodeSubType.F);
            return false;
        }
        return false;
    }

}

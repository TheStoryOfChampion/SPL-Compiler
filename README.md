# SPL-Compiler
This is a compiler I built for a programming language named SPL (Student programming language) as an assignment

# Running the program

It is easiest to run in Intellij as that was the environment in which I coded this project. 

> On the top right side of the screen ensure the class it's selected is Main
> 
> Right next to that, there should be a green play button
> 
> click on that and the prigram should run

The example code is in the "Test" subdirectory, should you want to peruse it or change the code to run tests, go in the Test subdirectory and look for the "test.txt" file. This is the file to change according to the grammar that follows


Should you want to see the results, after running it, in the root of the prioject there will be 3 new text files (if ran successfully), each indicating the diferent steps of the compiler (Parsing, Scoping, Type Checking). Peruse these files.

# The Grammar is as follows:

PROG ::= DECL CODE PROCDEFS

DECL ::=

DECL ::= D, DECL

D ::= NAME:TYPE

CODE ::= 

CODE ::= INOUT; CODE

CODE ::= CALLP; CODE

TYPE ::= num

TYPE ::= bool 

TYPE ::= proc

TYPE ::= text

NAME ::= LETT_ID

LETT ::= a | b | c | d | e | f | g | h | i | j | k | l | m | n | o | p | q | r | s | t | u | v | w | x | y | z

ID ::=

ID ::= DIGIT ID

DIGIT ::= 0 | 1| 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9

INOUT ::= print CONST

INOUT ::= print NAME

INOUT ::= input NAME

CALLP ::= exec NAME 

PROCDEFS ::= 

PROCDEFS ::= def NAME { BODY } PROCDEFS

CONST ::= TRUTH

CONST ::= INTEG

CONST ::= STRNG

TRUTH ::= T

TRUTH ::= F

INTEG ::= (Any integer including null, negative integers and positive integers)

STRNG ::= (Any string of characters excluding Quotation marks with a length between and including 1 and 8, enclosed by "Quotation Marks")

BODY ::= DECL COMMANDS

COMMANDS ::= dummy

COMMANDS ::= INSTR; COMMANDS

INSTR ::= ASSGN

INSTR ::= LOOP

INSTR ::= BRANCH

ASSGN ::= NAME = EXPR

LOOP ::= repeat { COMMANDS } until EXPR

BRANCH ::= if EXPR then { COMMANDS } else { COMMANDS }

EXPR ::= NAME

EXPR ::= CONST

EXPR ::= add(EXPR ,EXPR )

EXPR ::= mult(EXPR ,EXPR )

EXPR ::= eq(EXPR ,EXPR )

EXPR ::= larger(EXPR ,EXPR )

EXPR ::= or(EXPR ,EXPR )

EXPR ::= and(EXPR ,EXPR )

EXPR ::= not(EXPR)
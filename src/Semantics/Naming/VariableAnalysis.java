package Semantics.Naming;
import Node.Scope;
import Node.TreeNode;
import Node.SymbolType;
import java.util.ArrayList;

public class VariableAnalysis {
    private TreeNode treeRoot;
    private ArrayList<TreeNode> names = new ArrayList<>();
    private ArrayList<TreeNode> ProcDefs = new ArrayList<>();
    private ArrayList<TreeNode> CallProcs = new ArrayList<>();
    private ArrayList<TreeNode> Decls = new ArrayList<>();
    private int nCount = 0;
    private int pCount = 0;

    public VariableAnalysis(TreeNode treeRoot)
    {
        this.treeRoot = treeRoot;
        // initialises all the lists
        initArrays(treeRoot);
    }

    public TreeNode start() throws Exception
    {
        if(treeRoot != null)
        {
            procedureCheck(treeRoot);

            // Rule: Procedure declaration without existence of a matching procedure call
            unusedProcedureCheck();
            // Rule: Procedure call without existence of a matching procedure declaration
            unusedCallPCheck();

            variableCheck(treeRoot);

            //  Variable declaration without existence of any matching variable usage
            unusedDeclarationCheck();
            // Rule: Variable usage without existence of any matching variable declaration
            unusedVarCheck();
//            unusedFieldCheck();
        }
        return treeRoot;
    }

    private void initArrays(TreeNode node)
    {
        if(node != null && !node.isTerminal())
        {
            if (node.isType(SymbolType.NAME) && !node.getParent().isType(SymbolType.DECL))
                names.add(node);
            else if (node.isType(SymbolType.PROCDEFS))
            {
                node.getChildren().get(1).setSemanticName("p"+pCount++);
                ProcDefs.add(node);
            }
            else if (node.isType(SymbolType.CALLP))
                CallProcs.add(node);
            else if (node.isType(SymbolType.DECL))
            {
                if(!node.firstChild().getValue().endsWith("arr"))
                {
                    node.lastChild().firstChild().setSemanticName("n"+nCount++);
                    Decls.add(node);
                }
            }
            for (TreeNode child : node.getChildren())
            {
                initArrays(child);
            }
        }
    }

//    .........................................Variable Checks..........................................................
    private void variableCheck(TreeNode node) throws Exception
    {

        //Rule: The declaration of some variable v can never be in any “offspring”-scope of v’s scope
        if(node != null && !node.isTerminal())
        {
            if(node.isType(SymbolType.NAME))
            {
                String name = node.firstChild().getValue();
                // parent not Dec node
                if( !node.getParent().isType(SymbolType.DECL))
                {


                    // Rule: The declaration of v can only be either in v’s own scope, or in some “ancestor”-scope
                    // This rule DOES NOT limit te declaration of a variable to one
                    if (incorrectScopeDeclaration(node))
                    {
                        // Rule: The declaration of some variable v can never be in any “offspring”-scope of v’s scope.
                        if (offspringDeclaration(node))
                            throw new Exception("[Declaration Error] variable \""+ name +"\" used before declaration");

                        throw new Exception("[Declaration Error] variable \""+name+"\" cannot be used before declaration");
                    }

                        /*
                          Rule:  If there is no declaration for v in its own scope, there must exist a matching
                                 declaration in any of v’s “ancestor”

                          Rule:   If there is a declaration for v in its own scope and another declaration for v in some
                                  “ancestor”-scope, then the declaration in v’s own scope is the “matching” declaration:
                                  local variable

                          Rule:   If there is no declaration for v in its own scope, and there are two declarations for
                                  v at two different hierarchy-levels in two different “ancestor”-scopes, then the
                                  “nearer” declaration is the “matching” declaration
                         */
                    if (hasNoDeclaration(node))
                        throw new Exception("[Declaration Error] no declaration for \""+name);
                }
            }
            if(node.isType(SymbolType.DECL))
            {
                String name = node.lastChild().firstChild().getValue();

                //  Rule:  Within any one scope, there cannot be more than one declaration for the same variable v
                if(multipleScopeDeclaration(node))
                    throw new Exception("[Sementics.Naming Error] variable \""+name+"\" has already been declared ");

            }

            for (TreeNode child : node.getChildren())
            {
                variableCheck(child);
            }
        }
    }

    private void unusedVarCheck() throws Exception
    {
        ArrayList<TreeNode> newVars = new ArrayList<>();
        for (TreeNode var: names)
        {
            if(isDeclared(var))
                newVars.add(var);
            else
            {
                String name = var.getValue();
                throw new Exception("[APPL-DECL Error] variable \""+name+"\" is not defined");
            }
        }
        names = newVars;
    }

    private void unusedDeclarationCheck() throws Exception
    {
        ArrayList<TreeNode> newVarDecs = new ArrayList<>();
        for (TreeNode varDec : Decls)
        {
            if(isUsed(varDec))
                newVarDecs.add(varDec);
            else
            {
                String name = varDec.lastChild().firstChild().getValue();
                throw new Exception("[DECL-APPL Error] variable \""+name+"\" is never used");
            }
        }
        Decls = newVarDecs;
    }

    private boolean hasNoDeclaration(TreeNode node) throws Exception
    {
        ArrayList<TreeNode> declarations;
        declarations = Decls;
        Scope currentScope = node.getScope();
        while (currentScope != null)
        {
            for (TreeNode varDec : declarations)
            {
                if(varDec.getScopeID().equals(currentScope.getID()))
                {
                    if(sameNameVars(varDec.lastChild(), node))
                    {
                        node.firstChild().setSemanticName(getSemanticName(varDec));
                        return false;    // return false because it does have a Declaration in scope or ancestor
                    }
                }
            }
            currentScope = currentScope.getParentScope();
        }
        return true;
    }

    private String getSemanticName(TreeNode node) throws Exception
    {
        if(node.isType(SymbolType.PROCDEFS) || node.isType(SymbolType.CALLP))
            return node.getChildren().get(1).getSemanticName();
        if(node.isType(SymbolType.DECL))
            return node.lastChild().firstChild().getSemanticName();
        if(node.isType(SymbolType.NAME))
            return node.firstChild().getSemanticName();
        else
            throw new Exception("Node has no semantic name "+node.getValue());
    }

    private boolean multipleScopeDeclaration(TreeNode node)
    {
        ArrayList<TreeNode> declarations;
        declarations = Decls;

        for (TreeNode dec :declarations)
        {
            if(dec.getScopeID().equals(node.getScopeID()) && node.getID() != dec.getID())
            {
                String name1 = dec.lastChild().firstChild().getValue();
                String name2 = node.lastChild().firstChild().getValue();
                if(name1.equals(name2))
                    return true;
            }
        }
        return false;
    }

    private boolean offspringDeclaration(TreeNode varNode) throws Exception
    {
        ArrayList<TreeNode> declarations;
        declarations = Decls;

        for (TreeNode varDec : declarations)
        {
            if (sameNameVars(varDec.lastChild(), varNode))
            {
                // if declaration happens in an offspring scope of variable
                // ALT: if variable is used in a scope where Declaration has not happened
                // ALT:  if varNode scope is ancestor of varDec

                if(isAncestorScope(varNode.getScope(), varDec.getScope().getParentScope()))
                {
                    return true;
                }
            }

        }
        return false;
    }

    private boolean incorrectScopeDeclaration(TreeNode varNode) throws Exception
    {
        ArrayList<TreeNode> declarations;
        declarations = Decls;

        // loop through each variable node in variables
        for (TreeNode varDec : declarations)
        {
            // if the varDec has the same name as varNode
            if(sameNameVars(varDec.lastChild(), varNode))
            {
                // if the varDec is parent of varNode
                if(varNode.getScopeID().equals(varDec.getScopeID()) || isAncestorScope(varDec.getScope(), varNode.getScope()))
                    return false;
            }
        }
        return true;
    }

    /** Checks whether the given parentScope is a parent of the given offspringScope
     *
     * @param parentScope scope given as ancestor
     * @param offspringScope scope given as offSpring
     * @return TRUE     if the parentScope is a parent of offspringScope
     *         FALSE    if the parentScope is not a parent of offspringScope
     */
    private boolean isAncestorScope(Scope parentScope, Scope offspringScope)
    {
        // parent of root scope is null
        // loop while parent of root scope has not been reached
        while (offspringScope != null && parentScope != null)
        {
            // if the parentScope is equal to offspringScope
            // since offspringScope keeps "getting older" (going up the tree), when parentScope = offspringScope then
            // parentScope is an ancestor of offspringScope.
            if(offspringScope.getID().equals(parentScope.getID()))
                return true;

            // go up in the tree
            offspringScope = offspringScope.getParentScope();
        }
        return false;
    }

    private boolean sameNameVars(TreeNode var1, TreeNode var2) throws Exception
    {

        if( var1 != null && var2 != null)
        {
            String name1 = var1.firstChild().getValue();
            String name2 = var2.firstChild().getValue();
            return name1.equals(name2);
        }
        else
        {
            if (var1 == null && var2 == null)
                throw new Exception("[Null Error] cannot compare var names for null objects");

            String error = null;
            if (var1 == null)
                error = var2.firstChild().getValue();
            if (var2 == null)
                error = var1.firstChild().getValue();

            throw new Exception("[Null Error] cannot compare variable \"" + error + "\" with null");
        }
    }

    private boolean isUsed(TreeNode dec) throws Exception
    {
        ArrayList<TreeNode> declarations;
        declarations = names;

        for (TreeNode var : declarations)
        {
            if(getSemanticName(var) == null)
                return false;
            if(getSemanticName(var).equals(getSemanticName(dec)))
                return true;
        }
        return false;
    }

    private boolean isDeclared(TreeNode node) throws Exception
    {
        ArrayList<TreeNode> varNodes;
        varNodes = Decls;

        for (TreeNode dec : varNodes)
        {
            if(getSemanticName(dec).equals(getSemanticName(node)))
                return true;
        }
        return false;
    }

//    .........................................Procedure checks......................................................
    private void procedureCheck(TreeNode node) throws Exception
    {
        if(node != null && !node.isTerminal())
        {
            if(node.isType(SymbolType.PROCDEFS))
            {
                String name = node.getChildren().get(1).getValue();

                // Rule: The unique “main” cannot be a UserDefinedName for any declared procedure in an SPL program
                if(name.equals("name"))
                    throw new Exception("Procedure name cannot be \"main\"");

                // Rule: Let S be a scope opened by a procedure declaration with some UserDefinedName=u. Let
                //S1, S2, ..., Sn be the Child-scopes in C of S. Then no procedure declaration in any of S1,
                //S2, ... Sn may have the same name u.
                if(hasSameNameChild(node))
                    throw new Exception("[Sementics.Naming Error] a parent procedure with the name \"" +name+"\" already exists");

                // Rule: Procedure declarations in Sibling-scopes must have different names
                if(hasSameNameSiblings(node))
                    throw new Exception("[Sementics.Naming Error] a sibling procedure with the name \""+name+"\" already exists");


                // if(!hasCorrespondingPCall(node))
                //     throw new Exception("[Sementics.Naming Error] procedure \""+name+"\" has no matching procedure call within range");


            }

            if(node.isType(SymbolType.CALLP))
            {
                String name = node.getChildren().get(1).getValue();

                // Rule: The unique “main” cannot be a UserDefinedName for any declared procedure in an SPL program
                if(name.equals("main"))
                    throw new Exception("Procedure name cannot be \"main\"");

                //Rule: Any procedure can only call itself or a sub-procedure that is declared in an immediate
                // Child-scope
                if(hasCorrespondingPD(node))
                {
                    TreeNode pd = getCorrespondingPD(node);
                    if(pd != null)
                        node.getChildren().get(1).setSemanticName(getSemanticName(pd));
                }else
                    throw new Exception("[Sementics.Naming Error] no procedure with the name \""+name+"\" was not found within procedure calling range");
            }

            for (TreeNode child : node.getChildren())
            {
                procedureCheck(child);
            }
        }
    }

    private void unusedProcedureCheck() throws Exception
    {
        ArrayList<TreeNode> newPDs = new ArrayList<>();
        for (TreeNode pd : ProcDefs)
        {
            if(isCalled(pd))
                newPDs.add(pd);
            else
            {
                String name = pd.getChildren().get(1).getValue();
                throw new Exception("[DECL-APPL Error] procedure \""+name+"\" is never called");
            }
        }
        ProcDefs = newPDs;
    }

    private void unusedCallPCheck() throws Exception
    {
        ArrayList<TreeNode> newPCalls = new ArrayList<>();
        for (TreeNode pCall : CallProcs)
        {
            if (isUsedPCall(pCall))
                newPCalls.add(pCall);
            else
            {
                String name = pCall.getChildren().get(1).getValue();
                throw new Exception("[APPL-DECL Error] no matching procedure for \""+name+"\"");
            }
        }
        CallProcs = newPCalls;
    }

    private boolean hasCorrespondingPD(TreeNode node) throws Exception
    {
        return getCorrespondingPD(node) != null;
    }

    private TreeNode getCorrespondingPD(TreeNode node) throws Exception
    {
        if(node != null )
        {
            Scope pScope = node.getScope();
            if(pScope != null )
            {
                for (TreeNode pd : ProcDefs)
                {
                    // if PD node's scope = node's scope or
                    // PD node's scope = node scope's parent scope
                    if(pd.getScope().getParentScope() == null)
                        throw new Exception("[Scoping Error] PD cannot have a cope of 0");
                    if (pd.getScope().getParentScope().getID().equals(node.getScopeID()) || pd.getScopeID().equals(node.getScopeID()))
                    {
                        if (sameNameProc(pd, node))
                            return pd;
                    }
                }
            }
        }
        return null;
    }

    /**
     * checks whether any procNodes within the child scope of the currentNode  have the same name as the currentNode
     *
     * @param currentNode node whose child scopes are to be checked
     * @return True if procNode with same name exists (in child scope)
     *         False if NO procNode with same name exists (in child scope)
     */
    private boolean hasSameNameChild(TreeNode currentNode)
    {
        // child scopes to be checked
        ArrayList<Scope> childScopes = currentNode.getScope().getChildScopes();

        //Loop through all PDs
        for (TreeNode proc : ProcDefs)
        {
            // If the proc's scope is within child scopes
            if(childScopes.contains(proc.getScope()))
            {
                // if the proc is has the same name as current node
                // Second Value should not be needed, theoretically since
                if (sameNameProc(proc, currentNode) /*&& proc.getID() != currentNode.getID()*/)
                    return true;
            }
        }
        // No node within child scopes was found to have the same name as currentNode
        return false;
    }

    /**
     *  check above  (hasSameNameChild() method)
     *
     */
    private boolean hasSameNameSiblings(TreeNode node) throws Exception
    {
        if(node.getScopeID().equals("0"))
        {
            throw new Exception("Proc cannot have scope zero");
        }else
        {
            ArrayList<Scope> siblingScopes = node.getScope().getParentScope().getChildScopes();
            for (TreeNode proc : ProcDefs )
            {
                if(siblingScopes.contains(proc.getScope()))
                {
                    if(sameNameProc(proc, node) && !proc.getScopeID().equals(node.getScopeID()))
                        return true;
                }
            }
        }
        return false;
    }

    private boolean sameNameProc(TreeNode node1, TreeNode node2)
    {
        String name1 = node1.getChildren().get(1).getValue();
        String name2 = node2.getChildren().get(1).getValue();
        return name1.equals(name2);
    }

    private boolean isCalled(TreeNode pd) throws Exception
    {
        for (TreeNode pcall : CallProcs)
        {
            if (getSemanticName(pcall).equals(getSemanticName(pd)))
                return true;
        }
        return  false;
    }

    private boolean isUsedPCall(TreeNode node) throws Exception
    {
        if(getSemanticName(node) != null && !getSemanticName(node).equals(""))
        {
            return hasCorrespondingPD(node);
        }
        return false;
    }

//    .......................................Actual Helpers................................................
    public String printTree()
    {
        if(treeRoot != null)
        {
            return printSubTree(treeRoot, "", 0);
        }
        return "tree root is null";
    }

    private String printSubTree(TreeNode treeNode, String tabs, int index)
    {
        String displayName = "";
        if(treeNode.getSemanticName() != null )
            displayName = ", vNum="+ treeNode.getSemanticName();
        String treeString = tabs +treeNode.getValue()+ "   [scope="+treeNode.getScopeID()+displayName +
                "]\n";
        tabs += treeNode.getChildren().size() > 1 ? "   |" : "    ";
        int i = 1;
        for (TreeNode child : treeNode.getChildren())
        {
            treeString += this.printSubTree(child, tabs, i++);
        }
        return treeString;
    }
}

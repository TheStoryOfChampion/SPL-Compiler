package Node;
import java.util.ArrayList;

public class Scope {
    private String scopeID;
    private Scope parentScope;
    private ArrayList<Scope> childScopes;

    public Scope()
    {
        this.scopeID = "0";
        this.parentScope = null;
        this.childScopes = new ArrayList<>();
    }

    public Scope(String scopeID)
    {
        this.scopeID = scopeID;
        this.childScopes = new ArrayList<>();
    }

    @Override
    public String toString()
    {
        String scope = "Scope{"+scopeID +",";
        scope += parentScope == null ? "null" : parentScope.getID();
        scope +=",[";
        for (Scope child : childScopes)
        {
            scope += child.getID() +",";
        }
        scope +="]}";
        return scope;
    }

    public String getID()
    {
        return scopeID;
    }

    public Scope getParentScope()
    {
        return parentScope;
    }

    public ArrayList<Scope> getChildScopes()
    {
        return childScopes;
    }

    public void setChildScopes(ArrayList<Scope> childScopes)
    {
        this.childScopes = new ArrayList<>();
        this.childScopes.addAll(childScopes);
    }

    public void addChildScope(Scope scope)
    {
        scope.setParentScope(this);
        this.childScopes.add(scope);
    }

    public void setParentScope(Scope scope)
    {
        this.parentScope = scope;
    }
}

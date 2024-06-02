package Node;

public class Node {
    static int m_iNumNodes =0;
    private Node m_oNext;
    private Node m_oPrev;
    private int m_iId;
    private NodeType m_eType;
    private String m_sValue;
    private NodeSubType m_eSubType;

    public Node(String pValue, NodeType pType)
    {
        m_iId = m_iNumNodes++;
        m_sValue = pValue;
        m_eType = pType;
        m_eSubType = null;
        m_oPrev = null;
        m_oNext = null;
    }

    public Node(String pValue, NodeType pType, NodeSubType pSubType)
    {
        m_iId = m_iNumNodes++;
        m_sValue = pValue;
        m_eType = pType;
        m_eSubType = pSubType;
        m_oPrev = null;
        m_oNext = null;
    }

//    ............................Getters......................................
    public int getId()
    {
        return m_iId;
    }

    public NodeType getType()
    {
        return m_eType;
    }

    public String getValue()
    {
        return m_sValue;
    }

    public NodeSubType getSubType()
    {
        return m_eSubType;
    }

//    ...............................Setters....................................
public void setId(int m_iId)
{
    this.m_iId = m_iId;
}

    public void setType(NodeType m_sType)
    {
        this.m_eType = m_sType;
    }

    public void setValue(String m_sValue)
    {
        this.m_sValue = m_sValue;
    }

    @Override
    public String toString()
    {
        String NodeDetails = "----------------------" +
                "\nID   : " + m_iId +
                "\nValue: " + m_sValue +
                "\nType : " + m_eType +
                "\nSubType :" + (m_eSubType != null ? String.valueOf(m_eSubType) : "null") +
                "\nNext :" + (m_oNext != null ? String.valueOf(m_oNext.m_iId) : "null") +
                "\nPrev :" + (m_oPrev != null ? String.valueOf(m_oPrev.m_iId) : "null");
        return NodeDetails;
    }

    public Node Prev()
    {
        return m_oPrev;
    }

    public Node next()
    {
        return m_oNext;
    }
    public void next(Node pNode)
    {
        m_oNext = pNode;
    }

    public void prev(Node pNode)
    {
        m_oPrev = pNode;
    }

    public String matchError()
    {
        return "["
                + " value=" + this.getValue()
                + " type=" + this.getType()
                +" ]";
    }
}

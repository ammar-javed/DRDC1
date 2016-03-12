import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.util.ArrayList;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

public class Instruction {
  private String text;
  private String warning;
  private String note; 
  private String subAssembly;
  
  public Instruction( String text, String warning, String note ){
    this.text = text;
    this.warning = warning;
    this.note = note;
  }

  public Instruction( String text ){
    this.text = text;
    this.warning = null;
    this.note = null;
    this.subAssembly = null;
  }

  public Instruction( Element step ){
    this.text = null;
    this.warning = null;
    this.note = null;
    this.subAssembly = null;
    NodeList infoList = step.getChildNodes();
    for (int y = 0; y < infoList.getLength(); y++) {
      Node info = infoList.item(y);
      if( info.getNodeName().equals( "instruction" ) )
        this.text = info.getTextContent();
      else if (info.getNodeName().equals( "warning" ) )
        this.warning = info.getTextContent();
      else if (info.getNodeName().equals( "note" ) )
        this.note = info.getTextContent();
      else if (info.getNodeName().equals( "assemblyFile" ) )
        this.subAssembly = info.getTextContent();
    }
    if( this.text == null ){
      System.out.println("Error: Instruction does not have text" );
    }
  }

  public String getText(){
    return this.text;
  }

  public String getNote(){
    return this.note;
  }

  public String getWarning(){
    return this.warning;
  }

  public boolean hasWarning(){
    return ( this.warning != null );
  }

  public boolean hasNote(){
    return ( this.note != null );
  }

  public String getSubAssembly(){
    return this.subAssembly;
  }

  public boolean hasSubAssembly(){
    return ( this.subAssembly != null );
  }
}


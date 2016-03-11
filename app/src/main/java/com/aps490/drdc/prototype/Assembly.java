package com.aps490.drdc.prototype;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.util.ArrayList;



public class Assembly {
  private File fXmlFile;
  private DocumentBuilderFactory dbFactory;
  private DocumentBuilder dBuilder;
  private Document doc;
  private Element module;
  private ArrayList<Instruction> instructions;
  public static final String ASSEMBLY_FILE_PATH = "";
  private String name;
  private int instrIndex;

  public Assembly( String assemblyPath ) {
    try {
      fXmlFile = new File( assemblyPath );
      dbFactory = DocumentBuilderFactory.newInstance();
      dBuilder = dbFactory.newDocumentBuilder();
      doc = dBuilder.parse(fXmlFile);
      doc.getDocumentElement().normalize();
      this.name = doc.getDocumentElement().getAttribute("name");

      //set default module
      selectModule( getModules().get(0) );

      this.instructions = getInstructionList();
      this.instrIndex = -1;

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  public Assembly( String assemblyPath, String moduleName ) {
    try {
      fXmlFile = new File( assemblyPath );
      dbFactory = DocumentBuilderFactory.newInstance();
      dBuilder = dbFactory.newDocumentBuilder();
      doc = dBuilder.parse(fXmlFile);
      doc.getDocumentElement().normalize();
      this.name = doc.getDocumentElement().getAttribute("name");

      //Try to use same module (in case of installation or removal)
      selectModule( moduleName );
      if( module ==null)
        selectModule( getModules().get(0) );

      this.instructions = getInstructionList();
      this.instrIndex = -1;

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
 
  public void printAll() {
    try {
      System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
      System.out.println("\nAssembly:" + doc.getDocumentElement().getAttribute("name"));
      NodeList nList = this.doc.getElementsByTagName("module");
      System.out.println("----------------------------");

      for (int temp = 0; temp < nList.getLength(); temp++) {
        Node nNode = nList.item(temp);
        System.out.println("\nCurrent Element:" + nNode.getNodeName());
        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
          Element eElement = (Element) nNode;
          System.out.println("module: " + eElement.getAttribute("name"));
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public String getName(){
    return this.name;
  }

  public ArrayList<String> getModules(){
    ArrayList<String> modules = new ArrayList<String>();
    NodeList nList = doc.getElementsByTagName("module");
    for (int temp = 0; temp < nList.getLength(); temp++) {
      Node nNode = nList.item(temp);
      if (nNode.getNodeType() == Node.ELEMENT_NODE) {
        Element eElement = (Element) nNode;
        modules.add( eElement.getAttribute("name") );
      }
    }
    return modules;
  }

  public void selectModule( String name ) {
    NodeList nList = doc.getElementsByTagName("module");
    for (int temp = 0; temp < nList.getLength(); temp++) {
      Node nNode = nList.item(temp);
      if (nNode.getNodeType() == Node.ELEMENT_NODE) {
        Element eElement = (Element) nNode;
        if( eElement.getAttribute("name").equals( name ) )
          this.module = eElement;
      }
    }
    if( this.module == null )
      System.out.println("Error Incorrect Module name");

    //Refresh Instruction
    this.instructions = getInstructionList();
    this.instrIndex = -1;
  }

  public String currentModule() {
    if( this.module == null ){
      System.out.println("Error: Module not yet selected");
      return null;
    }
    else
      return this.module.getAttribute("name");
  }

  public ArrayList<String> getFigures() {
    if( this.module == null ){
      System.out.println("Error: Module not yet selected");
      return null;
    }
    ArrayList<String> figures = new ArrayList<String>();

    NodeList nList = this.module.getElementsByTagName("figure");
    for (int temp = 0; temp < nList.getLength(); temp++) {
      Node nNode = nList.item(temp);
      Element figure = (Element) nNode;
      figures.add( figure.getAttribute( "path" ) );
    }
    return figures;
  }

  public ArrayList<Instruction> getInstructionList() {
    if( this.module == null ){
      System.out.println("Error: Module not yet selected");
      return null;
    }
    ArrayList<Instruction> instructions = new ArrayList<Instruction>();

    NodeList instr = this.module.getElementsByTagName("instructions");
    NodeList nList = this.module.getElementsByTagName("step");
    for (int temp = 0; temp < nList.getLength(); temp++) {
      instructions.add( new Instruction( (Element) nList.item(temp) ) ); 
    }
    return instructions;
  }

  public void printInstructions(){
    //Merely a debugging command to demonstrate functionality. 
    System.out.println("Instruction Set for assembly: " + this.name );
    System.out.println("--------------------------------------------" );
    Instruction instr = null;
    while( ( instr = getInstr() ) != null )
      printInstruction( instr );

    /*DEBUGGING TO check that print previous works
    while( ( instr = getPreviousInstr() ) != null )
      printInstruction( instr );
      */
  }
  
  public void printInstruction( Instruction instr ){
    if( instr == null ){
      System.out.println("Error: null instruction passed");
      return;
    }

    System.out.println( instr.getText() );

    if( instr.hasWarning() )
      System.out.println( "Warning: " + instr.getWarning() );  

    if( instr.hasNote() )
      System.out.println( "Note: " + instr.getNote() );  

    if( instr.hasSubAssembly() ){
      Assembly assembly = new Assembly( ASSEMBLY_FILE_PATH + instr.getSubAssembly(), this.currentModule() ); 
      System.out.println( "Now printing instructions for subAssembly: " + assembly.name );  
      assembly.printInstructions();
    }
  }

  public Instruction getInstr() {
    if(this.instrIndex < -1 || this.instrIndex >= (this.instructions.size() - 1 )){
      return null;
    }

    Instruction instr = this.instructions.get( ++this.instrIndex );
    if ( instr == null ){
      if( this.instrIndex == 0 )
        System.out.println("Error: No initial Instruction found" );
      this.instrIndex--;
    }
    return instr;
  }

  public Instruction getPreviousInstr() {
    if(this.instrIndex < 1 ){
      return null;
    }

    Instruction instr = this.instructions.get( --this.instrIndex );
    if ( instr == null ){
      System.out.println("Error: No Previous Instruction Found" );
      this.instrIndex++;
    }
    return instr;
  }


}



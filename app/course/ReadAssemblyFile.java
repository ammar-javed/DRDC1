import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.util.ArrayList;

public class ReadAssemblyFile {
  public static void main(String[] args) {
    try {
      //String assemblyPath = "mainWheelDoorActuator.xml";
      //String assemblyPath = Assembly.ASSEMBLY_FILE_PATH + args[0];
      String assemblyPath = CourseModules.map.get("Main Landing Gear Door"); 
      Assembly assembly = new Assembly( assemblyPath ); 
      ArrayList modules = assembly.getModules();
      for( int i=0; i<modules.size();i++ ){
        assembly.selectModule( (String) modules.get(i) );
        System.out.println( assembly.currentModule() );
        System.out.println( assembly.getFigures() );
        assembly.printInstructions();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}



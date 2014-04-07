/*
 * Recorrido.java
 *
 * Created on 4 de julio de 2007, 11:09 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package modelo;

import java.util.*;
import java.io.*;

/**
* @author javapractices.com
* @author Alex Wong
*/
public final class Recorrido {

  /**
  * Demonstrate use.
  */
  /*public static void main(String... aArguments) throws FileNotFoundException {
	aArguments = new String[1];
	aArguments[0] = "C:/Documents and Settings/admin/Escritorio/test/111";
	File tempDir = new File(aArguments[0]);
    List files = Recorrido.getFileListing( tempDir );

    //print out all file names, and display the order of File.compareTo
    Iterator filesIter = files.iterator();
    String carpeta ="";
    while( filesIter.hasNext() ){
    	File file = (File)filesIter.next();
    	if(file.isDirectory()){
    		carpeta = file.getName();
    	}else{
    		System.out.println( file.getAbsolutePath().replace('\\', '/') );
    		System.out.println("/opt/desarrollo/OLIB/"+carpeta+"/"+file.getName());
    	}
    }
  }*/

  /**
  * Recursively walk a directory tree and return a List of all
  * Files found; the List is sorted using File.compareTo.
  *
  * @param aStartingDir is a valid directory, which can be read.
  */
  static public List getFileListing( File aStartingDir ) throws FileNotFoundException{
    validateDirectory(aStartingDir);
    List<File> result = new ArrayList<File>();

    File[] filesAndDirs = aStartingDir.listFiles();
    List<File> filesDirs = Arrays.asList(filesAndDirs);
    Iterator filesIter = filesDirs.iterator();
    File file = null;
    while ( filesIter.hasNext() ) {
      file = (File)filesIter.next();
      result.add(file); //always add, even if directory
      if (!file.isFile()) {
        //must be a directory
        //recursive call!
        List<File> deeperList = getFileListing(file);
        result.addAll(deeperList);
      }

    }
    Collections.sort(result);
    return result;
  }

  /**
  * Directory is valid if it exists, does not represent a file, and can be read.
  */
  static private void validateDirectory (File aDirectory) throws FileNotFoundException {
    if (aDirectory == null) {
      throw new IllegalArgumentException("Directory should not be null.");
    }
    if (!aDirectory.exists()) {
      throw new FileNotFoundException("Directory does not exist: " + aDirectory);
    }
    if (!aDirectory.isDirectory()) {
      throw new IllegalArgumentException("Is not a directory: " + aDirectory);
    }
    if (!aDirectory.canRead()) {
      throw new IllegalArgumentException("Directory cannot be read: " + aDirectory);
    }
  }
} 


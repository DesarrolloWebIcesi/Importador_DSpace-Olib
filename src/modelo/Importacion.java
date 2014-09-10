package modelo;

import control.Ejecutable;
import java.io.*;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import vista.*;

public class Importacion {
	
	private File directorio;
	private String coleccion;
			
	public Importacion(File directorio, String coleccion){
		this.directorio = directorio;
		this.coleccion = coleccion;
	}
        
        public boolean crear(Object[] estructura) throws SecurityException{
            
                      
            String dir[]= (String[])estructura[0];
            String dublin[]= (String[])estructura[1];
            
            //try{
                // crear un log
                GregorianCalendar hoy = new GregorianCalendar();
                Ejecutable.getControl().imprimirLogFisico("-------------------------------");
                Ejecutable.getControl().imprimirLogFisico("LOG DE IMPORTACIÓN");
                Ejecutable.getControl().imprimirLogFisico("Fecha"+ GregorianCalendar.getInstance().getTime());              
                
                // seguir el proceso por cada archivo
                int cont = 10;              
                //crear directorio de la coleccion               
                File newDir = new File(directorio.getAbsolutePath()+"/"+coleccion);
                
                //borrar remanentes si existen
                if(newDir.exists()){
                    Ejecutable.getControl().deleteDir(newDir);
                }
                
                //Crea el directorio de la coleccion
                boolean success = newDir.mkdir();
                if (success) {
                    for (int i = 0; i < dir.length; i++) {
                        // creacion de estructura para dspace
                        //String url = dir[i];
                        String nombre= dir[i];
                        String dublinCore = dublin[i].trim();
                     
                        //comentar para producción
                        // tener en cuenta que la URL debe ser web o fisica y debe terminar en .pdf
                        
                        /*StringTokenizer token = new StringTokenizer(url,"/\\");
                        int val = token.countTokens();
                        String nombre = "";
                        while(val != 0){
                            nombre = token.nextToken().trim();
                            val--;
                        }*/
                        File elPdf = new File(directorio+"/"+nombre);
                        if(elPdf.exists()){
                            //Ejecutable.getControl().imprimirLogFisico("- Archivo "+elPdf.getName()+" encontrado");
                            newDir = new File(directorio.getAbsolutePath()+"/"+coleccion+"/"+cont++);
                            //Crea el directorio
                            success = newDir.mkdir();
                            if (success) {
                                Ejecutable.getControl().imprimirLogFisico("- Creando carpeta No."+(cont-1));
                                //Mueve el archivo al directorio
                                //success = elPdf.renameTo(new File(newDir, elPdf.getName()));
                                success = Ejecutable.getControl().copy(elPdf,new File(newDir.getAbsolutePath()+"/"+elPdf.getName()));
                                if (success) {
                                    //crea el archivo contents
                                    success = escribe(newDir.getPath()+"/contents",elPdf.getName()/*url*/);
                                    if (success) {
                                        Ejecutable.getControl().imprimirLogFisico("- El archivo contents fue creado.");
                                        //crea eldublin_core.xml
                                        success = escribe(newDir.getPath()+"/dublin_core.xml",dublinCore);
                                        if(success){
                                            Ejecutable.getControl().imprimirLogFisico("- EL archivo dublin_core fue creado.");
                                        } else {
                                            Ejecutable.getControl().imprimirLog("\nERROR: EL archivo dublin_core no fue creado.");
                                            Ejecutable.getControl().imprimirLogFisico("ERROR: EL archivo dublin_core no fue creado.");
                                            return false;
                                        }//if del dublin core
                                    } else {
                                        Ejecutable.getControl().imprimirLog("\nERROR: un archivo contents no fue creado.");
                                        Ejecutable.getControl().imprimirLogFisico("ERROR: un archivo contents no fue creado.");
                                        return false;
                                    }//if del contents
                                }else{
                                    Ejecutable.getControl().imprimirLog("\nERROR: El archivo "+elPdf.getName()+" no pudo ser movido.");
                                    Ejecutable.getControl().imprimirLogFisico("ERROR: El archivo "+elPdf.getName()+" no pudo ser movido.");
                                    return false;
                                }//if de mover el archivo
                            }else{
                                Ejecutable.getControl().imprimirLog("\nERROR: El Directorio "+(cont-1)+" no pudo ser creado.");
                                Ejecutable.getControl().imprimirLogFisico("ERROR: El Directorio "+(cont-1)+" no pudo ser creado.");
                                return false;
                            }//if de la creacion del directorio
                        }else{
                            Ejecutable.getControl().imprimirLog("\nERROR: El archivo "+elPdf.getName()+" no existe.");
                            Ejecutable.getControl().imprimirLogFisico("ERROR: El archivo "+elPdf.getName()+" no existe.");
                            return false;
                        }// if de buscar el pdf
                    }//for
                }else{                   
                    Ejecutable.getControl().imprimirLog("\nERROR: El Directorio "+coleccion+" no pudo ser creado. Verifique que tiene permisos sobre esa carpeta o que su disco no este lleno.");
                    Ejecutable.getControl().imprimirLogFisico("ERROR: El Directorio "+coleccion+" no pudo ser creado. Verifique que tiene permisos sobre esa carpeta o que su disco no este lleno.");
                    return false;
                }
            return true;
        }
	
	public boolean escribe(String nomarchivo, String texto){
		/* 
		 * este método escribe en disco en la locación que entra
		 * por parametro, la cadena que entra por parametro
		 */
		try{
			File archivo = new File(nomarchivo);
	        boolean success = archivo.createNewFile();
	        if (success) {
	            //Escribe en el archivo
	            BufferedWriter out = new BufferedWriter(new FileWriter(archivo.getAbsolutePath()));
	            out.write(texto);
	            out.close();
	            return true;
	        } else {
	        	return false;
	        }
		 } catch (IOException e) {
	    	return false;
	    }
	}
 
}
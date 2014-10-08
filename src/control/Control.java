/*
 * Control.java
 *
 * Created on 6 de julio de 2007, 09:22 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package control;

import java.sql.SQLException;
import modelo.*;
import vista.*;
import java.util.*;
import java.io.*;
import javax.swing.*;

/**
 *
 * @author admin
 */
public class Control {
    
    private GUI gui;
    private Importacion importacion;
    private String passOlib = "";
    private String passDspace = "";
    private String usuOlib = "";
    private String usuDspace = "";
    private String passSamba = "";
    private String usuSamba = "";
    private Object[] retorno;
    private String sambaProduccion = "";
    private String sambaDesarrollo = "";
    private String sambaPruebas = "";
    //Gavarela: Se agregó para no incluir una clase de vista en un objeto de control
    private AccesoServidores accesoServidores;
    //Gavarela: Se agragaron para dejarlos en memoria una vez elegidos y verlos en la ventana principal (gui)
    private String conexiónOlib;
    private String conexiónDSpace;
    private String urlSamba;
    
    public Control(){
        passDspace = "";
        passOlib = "";
        usuDspace = "";
        usuOlib = "";
    }
    
    public int validarDatos(String coleccion, String titleno, String ruta, String urlOlib, String urlDspace) throws ClassNotFoundException, SQLException{
        modelo.Conectar conn = new modelo.Conectar("","");
        // 1 = todo ok
        // 2 = ruta mala
        // 3 = colección mala
        // 4 = emisión mala
        if(!validarRuta(ruta)){
            return 2;
        }
        if(!conn.validarDatosDspace(coleccion,usuDspace,passDspace,urlDspace)){
            return 3;
        }
        if(!conn.validarDatosOlib(titleno,usuOlib,passOlib,urlOlib)){
            return 4;
        }
        return 1;
    }
    
    public int correrComando(String instruccion[]){
        // corre un solo comando en el RUNTIME en el caso se queman solo comandos para el CMD de windows 
        RunCommand comando = new RunCommand();
        String retorno = "";
        int exitVal = comando.comando(instruccion);
        retorno = comando.getSalida();
        imprimirLogFisico(retorno);
        return exitVal;
    }
    
    public boolean validarRuta(String ruta){
            // valida que los datos sean validos es decir que existan                                
            File carpeta = new File(ruta);
            if(!carpeta.exists()){
                return false;
            }    
            return true;
    }
    
    public boolean crearTXTimportacion(){
        
        String eperson = (String)gui.getEpersonDspace().get(gui.getUrlDspace().indexOf(conexiónDSpace));//.get(urlDspace.indexOf(cadenaDspace)));
        String coleccion = gui.getIdColeccion().getText();
        String emision = gui.getIdEmision().getText();
        
        try {
            File directorio = new File(gui.getRutaCarpeta().getText());
            File arcLog = new File(directorio.getAbsolutePath()+"/"+emision+"/import.txt");
            PrintWriter logout = new PrintWriter(new FileWriter(arcLog.getAbsolutePath(),true),true);
            logout.println(eperson);
            logout.println(coleccion);
            logout.close();
            return true;
        } catch (IOException ex) {
            imprimirLogFisico(ex.getMessage());
            imprimirLogFisico("Hay problemas al escribir el archivo de importacion, la importación ha fallado.");
            imprimirLog("Hay problemas al escribir el archivo de importación, la importación ha fallado.");
            return false;
        }
    }
    
    public void imprimirLogFisico(String cadena){
        try {
            File directorio = new File(gui.getRutaCarpeta().getText());
            File arcLog = new File(directorio.getAbsolutePath()+"/log.txt");
            PrintWriter logout = new PrintWriter(new FileWriter(arcLog.getAbsolutePath(),true),true);
            logout.println(cadena);
            //gavarela: para que imprima el salto de línea visible al Bloc de notas de Windows
            logout.println();
            //logout.print(cadena+"\r\n");
            logout.close();
        } catch (IOException ex) {
            imprimirLog("Hay problemas al escribir el LOG físico, no se podrá tener registro de las importaciones hechas en esta sesión.");
        }
    }
    
    public String cortarCadena(String servidor){
        StringTokenizer token = new StringTokenizer(servidor,"()");
	String carpeta = "";
	while(token.hasMoreTokens()){
            carpeta = token.nextToken().trim();
	}
        return carpeta;
    }
    
    public boolean crearCarpetasRemotas(String servidor, File directorio){
	
        String emision = gui.getIdEmision().getText();
        
        //String carpeta = cortarCadena(servidor);  //nombre de la instancia de Dsapce a usar (coincide con el nombre de la carpera en el servidor)
        String carpeta= "bibdigital";
        String[] tmp = (String[])retorno[0]; // lista de los nombres de archivos
        int valor = 10;  //las carpetas toman nombres 10, 11, 12, 13, etc con 10 como el valor inicial
        RunCommand comando = new RunCommand();
        String retorno = "";
        String instruccion[] = new String[1];
        int exitVal;
        
        // 1 borrar la capeta de la colección en el servidor(en caso de ya existir):  Rmdir /s /q [nombre carpeta]
        instruccion[0] = "Rmdir /s /q Y:\\"+carpeta+"\\OLIB\\"+emision;
        exitVal = comando.comando(instruccion);
        retorno = comando.getSalida();
        imprimirLogFisico(retorno);
        
        // 2 crear la carpeta de la coleccion: mkdir [nombre carpeta]
        instruccion[0] = "mkdir Y:\\"+carpeta+"\\OLIB\\"+emision;
        exitVal = comando.comando(instruccion);
        retorno = comando.getSalida();
        imprimirLogFisico(retorno);
        
        if(exitVal != 0){return false;}
        comando.setSalida("");
        
        // 3 copiar todo el contenido local al remoto: Xcopy /s /e [origen local] [destino remoto]
        //instruccion[0] = "Xcopy /s /e "+directorio.getAbsolutePath()+"\\"+emision+" Y:\\"+carpeta+"\\OLIB\\"+emision;
        instruccion[0] = "Xcopy /s /e "+directorio.getAbsolutePath()+"\\"+emision+" Y:\\"+carpeta+"\\OLIB\\"+emision;
        exitVal = comando.comando(instruccion);
        retorno = comando.getSalida();
        imprimirLogFisico(retorno);
        
        if(exitVal != 0){return false;}
        comando.setSalida("");
        
        return true;  
    }
    
    public boolean crearCarpetasLocales(File directorio, String articleno, String cadenaOlib) throws ClassNotFoundException, SQLException{

        String emision = gui.getIdEmision().getText();
        
        modelo.Conectar conectar = new modelo.Conectar(usuOlib, passOlib);
	conectar.setUrl(cadenaOlib);
        retorno = conectar.conectarOlib(cadenaOlib, articleno);
        importacion = new Importacion(directorio,emision);
        return importacion.crear(retorno);
    }
    
    public void imprimirLog(String cadena){
        gui.getLog().append(cadena);
    } 
    
    public boolean copy(File src, File dst){
        try {
            // Copies src file to dst file.
            // If the dst file does not exist, it is created
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);
    
            // Transfer bytes from in to out
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException ex) {
            imprimirLogFisico("Error al copiar el archivo "+src+" "+ex);
            return false;
        } catch (IOException ex) {
            imprimirLogFisico("Error al copiar el archivo "+src+" "+ex);
            return false;
        }
        return true;
    }
        
    public boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
    
        // The directory is now empty so delete it
        return dir.delete();
    }
    
    public void borrarTemporalesLocales(File ruta){

        String emision = gui.getIdEmision().getText();
        
        File borra = new File(ruta.getAbsolutePath()+"/"+emision);
        try{
            if(!deleteDir(borra)){
                Ejecutable.getControl().imprimirLogFisico("No se pudieron borrar los archivos temporales locales: "+borra.getAbsolutePath());
                imprimirLog("No se pudieron borrar los archivos temporales locales: "+borra.getAbsolutePath());
            }
        }catch(SecurityException ex){
            Ejecutable.getControl().imprimirLogFisico("No se pudieron borrar los archivos temporales locales: "+borra.getAbsolutePath());
            imprimirLog("No se pudieron borrar los archivos temporales locales: "+borra.getAbsolutePath());
        }
    }
    
    public void ayuda(JTextArea area){
        try {
            BufferedReader in = new BufferedReader(new FileReader("help.txt"));
            String cadena;
            while ((cadena = in.readLine()) != null) {
                area.append("\n"+cadena);
            }
            in.close();
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(null, "Faltan el archivo de ayuda: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error de lectura: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void comenzar() {
        List<String> olib = new ArrayList<String>();
        List<String> olibnames = new ArrayList<String>();
        List<String> dspace = new ArrayList<String>();
        List<String> dspacenames = new ArrayList<String>();
        List<String> epersons = new ArrayList<String>();
        List<String> ususDspace = new ArrayList<String>();
        List<String> dbususDspace = new ArrayList<String>();
        
        try {
            GUI.setNativeLookAndFeel(); //loock an feel del sistema operativo
            // las direcciones de los servidores quedan en archivos de configuración
            BufferedReader in = new BufferedReader(new FileReader("URLolib.txt"));
            String cadena;
            while ((cadena = in.readLine()) != null) {
                olib.add(cadena); //URL
                cadena = in.readLine();
                olibnames.add(cadena); //Nombre
            }
            in.close();            
            
            in = new BufferedReader(new FileReader("URLdspace.txt"));
            while ((cadena = in.readLine()) != null) {
                dspace.add(cadena); //URL
                cadena = in.readLine();
                dspacenames.add(cadena); //Nombre
                cadena = in.readLine(); 
                epersons.add(cadena); //eperson
                cadena = in.readLine(); 
                dbususDspace.add(cadena);//db user
            }
            in.close();
            
            /*for(int val = 0; val<epersons.size(); val++){
                String person = (String)epersons.get(val);
                StringTokenizer token = new StringTokenizer(person,"@");
		String nombre = "";
		if(token.hasMoreTokens()){
			nombre = token.nextToken().trim();
		}
                ususDspace.add(nombre);
            }*/
            
            
            //LEER DE LA PARTE DEL ARCHIVO DE SAMBA txt
            in = new BufferedReader(new FileReader("DIRsamba.txt"));
            sambaProduccion = in.readLine();
            sambaDesarrollo = in.readLine();
            in.close();
            
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(null, "Faltan archivos importantes: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
            //ex.printStackTrace();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error de lectura: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
            //ex.printStackTrace();
        }
        //Gavarela: ahora se piden las claves antes de mostrar la ventana principal (gui)
        //gui = new GUI(olib,dspace,olibnames, dspacenames, epersons, ususDspace);
        //gui.setVisible(true);
        accesoServidores = new AccesoServidores(dspace, dspacenames, olib, olibnames, epersons, dbususDspace);
        gui = new GUI(olib,dspace,olibnames, dspacenames, epersons, ususDspace);
        accesoServidores.setVisible(true);
    }
    
    /**
     * Gavarela:
     * Invoca el método errorOlib de AccesoServidores para mostrar el error, según el código, 
     * al usuario.
     * El 04 de mayo de 2009 se cambió para que mandara el mensaje de la excepción lanzada
     *
     * @param mensaje El mensaje de error contenido en la excepción.
     */
    public void errorAccesoOlib(/*int código*/String mensaje) {
        accesoServidores.errorOlib(mensaje);
    }
    
    /**
     * Gavarela:
     * Invoca el método errorDSpace de AccesoServidores para mostrar el error, según el código, 
     * al usuario.
     * El 04 de mayo de 2009 se cambió para que mandara el mensaje de la excepción lanzada
     *
     * @param mensaje El mensaje de error contenido en la excepción.
     */
    public void errorAccesoDSpace(/*int código*/String mensaje) {
        accesoServidores.errorDSpace(mensaje);
    }
    
    /**
     * Gavarela:
     * Invoca el método errorSamba de AccesoServidores para mostrar el error al usuario.
     */
    public void errorAccesoSamba() {
        accesoServidores.errorSamba();
    }
    
    /**
     * Gavarela:
     * Hace que se avance a la ventana principal.
     */
    public void servidoresAccedidos(){
        accesoServidores.setVisible(false);
        String nombreDSpace = (String) gui.getNomUrlDspace().get(gui.getUrlDspace().indexOf(conexiónDSpace));
        gui.getNombreDSpace().setText(nombreDSpace);
        String nombreOlib = (String) gui.getNomUrlOlib().get(gui.getUrlOlib().indexOf(conexiónOlib));
        gui.getNombreOlib().setText(nombreOlib);
        gui.setVisible(true);
    }
    
    public GUI getGui() {
        return gui;
    }

    public String getPassOlib() {
        return passOlib;
    }

    public void setPassOlib(String passOlib) {
        this.passOlib = passOlib;
    }

    public String getPassDspace() {
        return passDspace;
    }

    public void setPassDspace(String passDspace) {
        this.passDspace = passDspace;
    }

    public String getUsuOlib() {
        return usuOlib;
    }

    public void setUsuOlib(String usuOlib) {
        this.usuOlib = usuOlib;
    }

    public String getUsuDspace() {
        return usuDspace;
    }

    public void setUsuDspace(String usuDspace) {
        this.usuDspace = usuDspace;
    }

    public Importacion getImportacion() {
        return importacion;
    }

    public void setImportacion(Importacion importacion) {
        this.importacion = importacion;
    }

    public String getPassSamba() {
        return passSamba;
    }

    public void setPassSamba(String passSamba) {
        this.passSamba = passSamba;
    }

    public String getUsuSamba() {
        return usuSamba;
    }

    public void setUsuSamba(String usuSamba) {
        this.usuSamba = usuSamba;
    }

    public String getSambaProduccion() {
        return sambaProduccion;
    }

    public String getSambaDesarrollo() {
        return sambaDesarrollo;
    }
    
    public String getSambaPruebas() {
        return sambaPruebas;
    }

    public String getConexiónOlib() {
        return conexiónOlib;
    }

    public void setConexiónOlib(String conexiónOlib) {
        this.conexiónOlib = conexiónOlib;
    }

    public String getConexiónDSpace() {
        return conexiónDSpace;
    }

    public void setConexiónDSpace(String conexónDSpace) {
        this.conexiónDSpace = conexónDSpace;
    }

    public String getUrlSamba() {
        return urlSamba;
    }

    public void setUrlSamba(String urlSamba) {
        this.urlSamba = urlSamba;
    }
}

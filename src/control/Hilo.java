/*
 * Hilo.java
 *
 * Created on 12 de julio de 2007, 08:20 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package control;


import java.awt.Cursor;
import javax.swing.*;
import java.io.*;
import java.sql.*;
import vista.*;
import javax.swing.*;

/**
 *
 * @author mechever
 */
public class Hilo extends java.lang.Thread{
    
    /** Creates a new instance of Hilo */
    public Hilo() {
        super();
    }
    
    public void run(){
        String col = Ejecutable.getControl().getGui().getIdColeccion().getText();
        String em = Ejecutable.getControl().getGui().getIdEmision().getText();
        String rut = Ejecutable.getControl().getGui().getRutaCarpeta().getText();
        
        if(col == "" || em == "" || rut == ""){
            JOptionPane.showMessageDialog(null,"No puede dejar ningún campo en blanco","Campos en blanco",JOptionPane.INFORMATION_MESSAGE);
        }else{
            try {
                
                //String cadenaConeccionDspace = (String)Ejecutable.getControl().getGui().getUrlDspace().get(Ejecutable.getControl().getGui().getComboDspace().getSelectedIndex());
                //String servidor = (String)Ejecutable.getControl().getGui().getNomUrlDspace().get(Ejecutable.getControl().getGui().getComboDspace().getSelectedIndex());
                //String eperson = (String)Ejecutable.getControl().getGui().getEpersonDspace().get(Ejecutable.getControl().getGui().getComboDspace().getSelectedIndex());
                
                //String coleccion = Ejecutable.getControl().getGui().getIdColeccion().getText();
                
                //String cadenaConeccionOlib = (String)Ejecutable.getControl().getGui().getUrlOlib().get(Ejecutable.getControl().getGui().getComboOlib().getSelectedIndex());
                
                String instanciaDSpace = Ejecutable.getControl().getGui().getNombreDSpace().getText();
                String articleno = Ejecutable.getControl().getGui().getIdEmision().getText();
                String cadenaConeccionOlib = Ejecutable.getControl().getConexiónOlib();
                
                File ruta = new File(Ejecutable.getControl().getGui().getRutaCarpeta().getText());
                
                // comienza el proceso de importacion
                
                //1) crear las carpetas locales
                Ejecutable.getControl().getGui().getLog().append("\nComenzando la importación. Este proceso puede tardar algunos minutos");               
                if(!Ejecutable.getControl().crearCarpetasLocales(ruta, articleno, cadenaConeccionOlib) || !Ejecutable.getControl().crearTXTimportacion()){
                    Ejecutable.getControl().getGui().getLog().append("\n*** Han ocurrido errores durante la creacion archivos locales, verifique este el LOG ***");
                }else{
                    Ejecutable.getControl().getGui().getLog().append("\nCreación de archivos locales realizada exitosamente.");
                    
                    //2)copiar carpetas al servidor
                    if(!Ejecutable.getControl().crearCarpetasRemotas(instanciaDSpace, ruta)){
                        Ejecutable.getControl().getGui().getLog().append("\n*** Han ocurrido errores durante la copia de archivos al servidor, verifique este el LOG ***");
                    }else{
                        //Gavarela: ahora la conexión se cierra al cerrar la ventana principal (gui)
                        //3) cerrar las conecciones abiertas con el net use     
                        /*String instruccion[] = new String[1];
                        instruccion[0] = "net use /y Y: /delete";
                        int exitVal = Ejecutable.getControl().correrComando(instruccion);
                        if(exitVal != 0){
                            Ejecutable.getControl().getGui().getLog().append("\n*** No se pudieron cerrar algunas conecciones ***.");
                        }*/
                        
                        Ejecutable.getControl().getGui().getLog().append("\nCopia de archivos al servidor realizada exitosamente.");
                        
                        // 4) borrar los archivos temporales locales
                        Ejecutable.getControl().borrarTemporalesLocales(ruta);
                        JOptionPane.showMessageDialog(null, "El proceso de importación ha concluido. Recuerde que el proceso de importación no se corre en el servidor inmediatamente.","Fin de la importación local",JOptionPane.INFORMATION_MESSAGE);
                        
                    }
                }
            } catch (ClassNotFoundException ex) {
                Ejecutable.getControl().imprimirLogFisico("Existe un problema con la el driver de ORACLE favor comunicarse con soporte técnico");
                JOptionPane.showMessageDialog(null, "Existe un problema con la el driver de ORACLE favor comunicarse con soporte técnico","Error",JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Error de conexión: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
                Ejecutable.getControl().imprimirLogFisico("Error de conexión: "+ex.getMessage());
            } finally {
                //liberar la ventana              
                Ejecutable.getControl().getGui().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                Ejecutable.getControl().getGui().getBotonBuscaColeccion().setEnabled(true);
                Ejecutable.getControl().getGui().getBotonBuscarEmision().setEnabled(true);
                Ejecutable.getControl().getGui().getBotonExaminarCarpetas().setEnabled(true);
                Ejecutable.getControl().getGui().getBotonComenzarImportacion().setEnabled(true);
                Ejecutable.getControl().getGui().getIdColeccion().setEditable(true);
                Ejecutable.getControl().getGui().getIdEmision().setEditable(true);
                Ejecutable.getControl().getGui().getRutaCarpeta().setEditable(true);
                //Ejecutable.getControl().getGui().getComboDspace().setEnabled(true);
                //Ejecutable.getControl().getGui().getComboOlib().setEnabled(true);
            }
        }
    }
}

/*
 * HiloPruebaConexiones.java
 *
 * Created on 14 de agosto de 2008, 04:41 PM
 */

package control;

import java.sql.*;
/**
 *
 * @author gavarela
 */
public class HiloPruebaConexiones extends Thread{
    private String usuarioOlib;
    private char[] claveOlib;
    private String conexiónOlib;
    private String usuarioDSpace;
    private char[] claveDSpace;
    private String conexiónDSpace;
    private String usuarioSamba;
    private char[] claveSamba;
    private String urlSamba;
    
    public HiloPruebaConexiones(String usuarioOlib, char[] claveOlib, String conexiónOlib, String usuarioDSpace, char[] claveDSpace, String conexiónDSpace, String usuarioSamba, char[] claveSamba, String urlSamba) {
        super();
        this.usuarioOlib = usuarioOlib;
        this.claveOlib = claveOlib;
        this.conexiónOlib = conexiónOlib;
        this.usuarioDSpace = usuarioDSpace;
        this.claveDSpace = claveDSpace;
        this.conexiónDSpace = conexiónDSpace;
        this.usuarioSamba = usuarioSamba;
        this.claveSamba = claveSamba;
        this.urlSamba = urlSamba;
    }
    
    @Override
    public void run() {
        //Bandera para salir del hilo si una de las conexiones falla
        boolean continuar = false;

        /*Gavarela: se pasará la excepción para poner en pantalla en mensaje de esta
        switch(pruebaOlib()){
            case 0://Conexión Exitosa
                continuar = true;
                break;
            case 1://Falla debido a usuario/contraseña
                Ejecutable.getControl().errorAccesoOlib(1);
                break;
            case 2://Falla del Driver JDBC de Oracle
                Ejecutable.getControl().errorAccesoOlib(2);
        }
         */
        try {
            pruebaOlib();
            continuar = true;
        } catch (SQLException ex) {
            Ejecutable.getControl().errorAccesoOlib(ex.getMessage());
        } catch (ClassNotFoundException ex) {
            Ejecutable.getControl().errorAccesoOlib(ex.getMessage());
        }

        if (continuar) {
            continuar = false;
            /*Gavarela: se pasará la excepción para poner en pantalla en mensaje de esta
            switch (pruebaDSpace()) {
                case 0://Conexión Exitosa
                    continuar = true;
                    break;
                case 1://Falla debido a usuario/contraseña
                    Ejecutable.getControl().errorAccesoDSpace(1);
                    break;
                case 2://Falla del Driver JDBC de Oracle
                    Ejecutable.getControl().errorAccesoDSpace(2);
            }
            */
            try {
                pruebaDSpace();
                continuar = true;
            } catch (SQLException ex) {
                Ejecutable.getControl().errorAccesoDSpace(ex.getMessage());
            } catch (ClassNotFoundException ex) {
                Ejecutable.getControl().errorAccesoDSpace(ex.getMessage());
            }
        }
        
        if (continuar) {
            if (pruebaSamba()) {//Conexión Exitósa
                Ejecutable.getControl().setUsuOlib(usuarioOlib);
                Ejecutable.getControl().setPassOlib(new String(claveOlib));
                Ejecutable.getControl().setConexiónOlib(conexiónOlib);
                Ejecutable.getControl().setUsuDspace(usuarioDSpace);
                Ejecutable.getControl().setPassDspace(new String(claveDSpace));
                Ejecutable.getControl().setConexiónDSpace(conexiónDSpace);
                Ejecutable.getControl().setUsuSamba(usuarioSamba);
                Ejecutable.getControl().setPassSamba(new String(claveSamba));
                Ejecutable.getControl().setUrlSamba(urlSamba);
                
                Ejecutable.getControl().servidoresAccedidos();
            } else {//Falla en la conexión
                Ejecutable.getControl().errorAccesoSamba();
            }
        }
    }
    
    private /*int*/ void pruebaOlib() throws SQLException, ClassNotFoundException{
        /*Gavarela: se pasará la excepción para poner en pantalla en mensaje de esta
        try {*/
            Class.forName("oracle.jdbc.driver.OracleDriver");
            String clave = new String (claveOlib);
            Connection conexión = DriverManager.getConnection(conexiónOlib, usuarioOlib, clave);
            conexión.close();
            
            /*Gavarela: como se pasan las excepciones, ya no es necesario retornar un número
             return 0;
        } catch (SQLException ex) {
            return 1;
        } catch (ClassNotFoundException ex) {
            return 2;
        }*/
    }
    
    private /*int*/void pruebaDSpace() throws SQLException, ClassNotFoundException{
        /*Gavarela: se pasará la excepción para poner en pantalla en mensaje de esta
        try {*/
            Class.forName("oracle.jdbc.driver.OracleDriver");
            String clave = new String (claveDSpace);
            Connection conexión = DriverManager.getConnection(conexiónDSpace, usuarioDSpace, clave);
            conexión.close();

            /*Gavarela: como se pasan las excepciones, ya no es necesario retornar un número
            return 0;
        } catch (SQLException ex) {System.out.println(ex.getMessage());
            return 1;
        } catch (ClassNotFoundException ex) {
            return 2;
        }*/
    }
    
    private boolean pruebaSamba(){
        //Primero se debe cerrar cualquier conexión en la unidad Y
        String cmd[] = {"net use /y Y: /delete"};
        int resultado = Ejecutable.getControl().correrComando(cmd);
        
        //Luego se abre la seleccionada
        //Ejemplo:  net use Y: \\servidor.icesi.edu.co\dspace [pass] /user:[usu]
        String clave = new String (claveSamba);
        cmd[0] = "net use Y: "+urlSamba+" "+clave+" /user:"+usuarioSamba;
        resultado = Ejecutable.getControl().correrComando(cmd);
        
        if(resultado == 0){//Conexión Exitósa
            return true;
        }else{//Falla en la conexión
            return false;
        }
    }
}

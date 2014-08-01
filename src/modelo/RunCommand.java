/*
 * RunCommand.java
 *
 * Created on 10 de diciembre de 2007, 10:30 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package modelo;

/**
 *
 * @author 16944030
 */
public class RunCommand {
    
    //Mechever: clase modificada de los ejemplo de "When Runtime.exec() won't"
    // Autor: Michael C. Daconta, JavaWorld.com, 12/29/00
    // mas info: http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=1
    
    private String salida;
    
    public RunCommand(){
        setSalida("");
    }
    
    public int comando(String args[])
    {
       String ret = "";
       int exitVal = -1;
        
        try
        {            
            String osName = System.getProperty("os.name" );
            String[] cmd = new String[2 + args.length];
            if( osName.equals( "Windows NT" ) )
            {
                cmd[0] = "cmd.exe" ;
                cmd[1] = "/C" ;
            }
            else if( osName.equals( "Windows 95" ) )
            {
                cmd[0] = "command.com" ;
                cmd[1] = "/C" ;
            }else{
            	cmd[0] = "cmd.exe" ;
                cmd[1] = "/C" ;
            }
            for(int a= 2; a<cmd.length; a++){
            	cmd[a] = args[a-2];
            }
            
            /*gavarela: Innecesrio que imprima los comandos en la consola de Java
            for(int a= 0; a<cmd.length; a++){
            	System.out.println(cmd[a]);
            }*/
            
            Runtime rt = Runtime.getRuntime();
            System.out.println("Execing " + cmd[0] + " " + cmd[1] + " " + cmd[2]);
            ret += "Execing " + cmd[0] + " " + cmd[1] + " " + cmd[2] +"\r\n";
            
            Process proc = rt.exec(cmd);
            // any error message?
            StreamGobbler errorGobbler = new 
                StreamGobbler(proc.getErrorStream(), "\r\nERROR");            
            
            // any output?
            StreamGobbler outputGobbler = new 
                StreamGobbler(proc.getInputStream(), "\r\nOUTPUT");
                
            // kick them off
            errorGobbler.start();
            outputGobbler.start();
            
            // any error???
            exitVal = proc.waitFor();
            
            ret += errorGobbler.getRetorno();
            ret += outputGobbler.getRetorno();
            
            System.out.println("ExitValue: " + exitVal);
            ret += "ExitValue: " + exitVal + "\r\n";
            
            
        }catch (Throwable t){
            //t.printStackTrace();
        	ret += t.getMessage();
        }
        setSalida(ret);
        return exitVal;
    }

    public String getSalida() {
        return salida;
    }

    public void setSalida(String salida) {
        this.salida = salida;
    }
    
}

//Mechever: clase modificada de los ejemplo de "When Runtime.exec() won't"
// Autor: Michael C. Daconta, JavaWorld.com, 12/29/00
// mas info: http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=1

package modelo;

import java.io.*;

class StreamGobbler extends Thread
{
    private InputStream is;
    private String type;
    private String retorno;
    
    StreamGobbler(InputStream is, String type)
    {
        this.is = is;
        this.type = type;
        retorno = "";
    }
    
    public void run()
    {
        try
        {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null)
                //System.out.println(type + ">" + line);
            	retorno += type + ">" + line + "\n";
            
            } catch (IOException ioe)
              {
                //ioe.printStackTrace();
            	retorno += ioe.getMessage();
              }
    }

    public String getRetorno() {
        return retorno;
    }
}

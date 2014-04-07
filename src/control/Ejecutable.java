/*
 * Ejecutable.java
 *
 * Created on 6 de julio de 2007, 05:51 PM
 *
 *Punto de partida de toda la aplicación
 *
 */

package control;
import java.io.*;
/**
 *
 * @author admin
 */
public class Ejecutable {
    
    private static Control control;
    
    public static void main (String arg[]){
        control = new Control();
        getControl().comenzar();
    }

    public static Control getControl() {
        return control;
    }
    
}

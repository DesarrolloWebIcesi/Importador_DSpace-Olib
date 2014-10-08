/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package vista;

import java.io.*;

/**
 *
 * @author Christian david criollo <cdcriollo>
 */
public class Filtro implements FilenameFilter {
    
   String extension;
    Filtro(String extension){
        this.extension=extension;
    }
    public boolean accept(File dir, String name){
        return name.endsWith(extension);
    }   
}

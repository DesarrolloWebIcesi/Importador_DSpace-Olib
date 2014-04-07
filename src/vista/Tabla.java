package vista;

import javax.swing.*;
import javax.swing.JTable.*;
import java.awt.*; 
import java.sql.*;

public class Tabla {
    private String cadena;
    private String consulta;
    private JFrame frame;
    private JTable table;
    private String usuario;
    private String contraseña;
    private String url;

    public Tabla(String cadena, String usuario, String contraseña, String url, String tipo) throws ClassNotFoundException, SQLException{
        this.cadena = cadena;
        if(tipo.equals("DSPACE")){
            // consulta en DSpace
            consulta = "select c.collection_id ID, c.name NOMBRE, c.short_description DESCRIPCION, t.name COMUNIDAD from collection c, community t, community2collection cc where t.community_ID = cc.community_ID and c.collection_id = cc.collection_id and (lower(c.short_description) like '%"+cadena+"%' or lower(c.name) like '%"+cadena+"%' or lower(t.name) like '%"+cadena+"%')";
        }else{
            // consulta en Olib
            //Gavarela: se están subiendo artículos de seriadas a DSpace, por lo que hay que comentar la condición que las excluye
            //consulta = "select art.title ARTICULO, emi.titleno ID, emi.title EMISION from titles art, titles emi, titleobjs t_o, objects o, mediatps m  where art.articleno = emi.titleno and emi.articleno is not null and lower(emi.title) like '%"+cadena+"%' and t_o.objectno = o.objectno and art.mediatp = m.mediatp and t_o.titleno = art.titleno and lower(o.locator) like '%.pdf' union select t1.title, t1.titleno, '' from titles t1, titleobjs tio, objects ob where lower(t1.title) like '%"+cadena+"%'and tio.objectno = ob.objectno  and tio.titleno = t1.titleno and t1.mediatp not in ('SART','SISS')and lower(ob.locator) like '%.pdf' order by 2";
            consulta = "SELECT art.title ARTICULO, emi.titleno ID, emi.title EMISION " +
                    "FROM TITLES art, TITLES emi, TITLEOBJS t_o, OBJECTS o, MEDIATPS m " +
                    "WHERE art.articleno = emi.titleno " +
                    "AND emi.articleno IS NOT NULL " +
                    "AND LOWER(emi.title) LIKE LOWER('%" + cadena + "%') " +
                    "AND t_o.objectno = o.objectno " +
                    "AND art.mediatp = m.mediatp " +
                    "AND t_o.titleno = art.titleno " +
                    "AND LOWER(o.locator) LIKE '%.pdf' " +
                    "UNION " +
                    "SELECT t1.title, t1.titleno, '' " +
                    "FROM TITLES t1, TITLEOBJS tio, OBJECTS ob " +
                    "WHERE LOWER(t1.title) LIKE '%" + cadena + "%' " +
                    "AND tio.objectno = ob.objectno " +
                    "AND tio.titleno = t1.titleno " +
                    "--AND t1.mediatp NOT IN('SART',   'SISS') " +
                    "AND LOWER(ob.locator) LIKE '%.pdf' " +
                    "ORDER BY 2";
        }
        this.usuario=usuario;
        this.contraseña=contraseña;
        this.url = url;
        JDBCAdapter jjbca = new JDBCAdapter(url,"oracle.jdbc.driver.OracleDriver",usuario,contraseña);
	jjbca.executeQuery(consulta);
	table = new JTable(jjbca);
    }

    /*public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try{
                    Tabla newContentPane = new Tabla("no","","","jdbc:oracle:thin:@200.3.192.29:1521:FILES","DSPACE");
                } catch (ClassNotFoundException ex) {
			System.err.println("Cannot find the database driver classes.");
			System.err.println(ex);
		} catch (SQLException ex) {
			System.err.println("Cannot connect to this database.");
			System.err.println(ex);
		}    
            }
        });
    }*/

	public String getCadena() {
		return cadena;
	}

	public void setCadena(String cadena) {
		this.cadena = cadena;
	}

	public JFrame getFrame() {
		return frame;
	}

	public void setFrame(JFrame frame) {
		this.frame = frame;
	}

	public JTable getTable() {
		return table;
	}

	public void setTable(JTable table) {
		this.table = table;
	}
}

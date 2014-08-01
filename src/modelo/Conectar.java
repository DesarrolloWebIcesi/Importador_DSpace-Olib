package modelo;

import java.sql.*;

public class Conectar {

    private String usuario;
    private String contraseña;
    private String url;

    public Conectar(String usuario, String contraseña) {
        this.contraseña = contraseña;
        this.usuario = usuario;
    }
    //TODO: poner en la documetacion técnica que en la tabla DCVALUE el campo TEXT_VALUE se debe agrandar a 4000 caracteres por seguridad.
    public boolean validarDatosDspace(String coleccion, String usuDspace, String passDspace, String urlDspace) throws ClassNotFoundException, SQLException {
        // valida que los datos sean validos es decir que existan

        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection conn = DriverManager.getConnection(urlDspace, usuDspace, passDspace);
        conn.setAutoCommit(false);
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        String consulta = "select count(*) from COLLECTION where collection_ID = " + coleccion;
        System.out.println("validar datos dspace"+" "+consulta);
        ResultSet rsetA = stmt.executeQuery(consulta);
        rsetA.next();
        int filas = rsetA.getInt(1);
        rsetA.close();
        if (filas == 0) {
            return false;
        }
        return true;
    }

    public boolean validarDatosOlib(String titleno, String usuOlib, String passOlib, String urlOlib) throws ClassNotFoundException, SQLException {
        // valida que los datos sean validos es decir que existan           

        Connection conn = DriverManager.getConnection(urlOlib, usuOlib, passOlib);
        conn.setAutoCommit(false);
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        String consulta = "select count(*) from titles where titleno = " + titleno;
        System.out.println("validar datos olib"+" "+consulta);
        ResultSet rsetA = stmt.executeQuery(consulta);
        rsetA.next();
        int filas = rsetA.getInt(1);
        rsetA.close();
        if (filas == 0) {
            return false;
        }
        return true;
    }

    public Object[] conectarDspace(String url, String name) throws ClassNotFoundException, SQLException {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection conn = DriverManager.getConnection(url, usuario, contraseña);
        conn.setAutoCommit(false);
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        String consulta = "select count(*) from collection where name like '%" + name + "%'";
        System.out.println("conectar dspace"+" "+consulta);
        ResultSet rsetA = stmt.executeQuery(consulta);
        rsetA.next();
        int filas = rsetA.getInt(1);
        rsetA.close();

        consulta = "select * from collection where name like '%" + name + "%'";
        System.out.println("conectar dspace"+" "+consulta);
        ResultSet rset = stmt.executeQuery(consulta);


        //System.out.println(filas);
        int id[] = new int[filas];
        String names[] = new String[filas];
        int cont = 0;
        while (rset.next()) {
            id[cont] = rset.getInt(1);
            names[cont] = rset.getString(2);
            cont++;
        }
        stmt.close();


        Object[] resultado = new Object[2];
        resultado[0] = id;
        resultado[1] = names;
        return resultado;
    }

    private static String convertToUnicodeString(String hexString) {
        System.out.println("hexstring"+ hexString);
        StringBuffer output = new StringBuffer();
        String subStr = null;
        System.out.println(hexString.length());
        for (int i = 0; i < hexString.length(); i = i + 2) {
            subStr = hexString.substring(i, i + 2);
            System.out.println("substring"+subStr );
            char c = (char) Integer.parseInt(subStr, 16);
            output.append(c);
        }
        return output.toString();
    }

    public Object[] conectarOlib(String url, String articleno) throws ClassNotFoundException, SQLException {
        // retorna un arreglo con 2 arreglos en su interior [  [arreglo 1] , [arreglo 2]  ]
        // arreglo 1: arreglo de cadenas [nombres de archivo]
        // arreglo 2: arreglo de cadenas [cadenas con dublin core]

        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection conn = DriverManager.getConnection(url, usuario, contraseña);
        conn.setAutoCommit(false);

        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        //consulta (restringida a pdfs por politica de biblioteca)
        //String consulta = "select count ('a') from titleobjs t_o, titles t, objects o, mediatps m where t_o.titleno = t.titleno and t_o.objectno = o.objectno and t.mediatp = m.mediatp and t.articleno = " + articleno + " and lower(o.locator) like '%.pdf' UNION select count(*) from titleobjs t_o, titles t, objects o, mediatps m where t_o.titleno = t.titleno and t_o.objectno = o.objectno and t.mediatp = m.mediatp and t.titleno = " + articleno + " and lower(o.locator) like '%.pdf'";
        String consulta= "select count ('a') from titleobjs t_o, titles t, objects o, mediatps m where t_o.titleno = t.titleno and t_o.objectno = o.objectno and t.mediatp = m.mediatp and t.titleno = " + articleno + " and lower(o.locator) like '%.pdf' UNION select count(*) from titleobjs t_o, titles t, objects o, mediatps m where t_o.titleno = t.titleno and t_o.objectno = o.objectno and t.mediatp = m.mediatp and t.titleno = " + articleno + " and lower(o.locator) like '%.pdf'";
        System.out.println("conectar olib"+" "+consulta);
        ResultSet rsetA = stmt.executeQuery(consulta);
        int filas = 0;
        while (rsetA.next()) {
            filas += rsetA.getInt(1);
        }
        rsetA.close();

        //consulta (restringida a pdfs politica de biblioteca)
        //consulta = "select distinct replace(o.locator,'http://www.icesi.edu.co/esn/contenido/pdfs/',''), '<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?><dublin_core>' ||' <dcvalue element=\"title\" qualifier=\"none\">'||t.title||decode(t.subtitle,null,null,' : '||t.subtitle)||'</dcvalue>' ||decode(t.isbn,null,null,' <dcvalue element=\"identifier\" qualifier=\"isbn\">'||t.isbn||'</dcvalue>') ||'<dcvalue element=\"identifier\" qualifier=\"other\">'||t.titleno||'</dcvalue>' ||fbib_dc_autores(t.titleno) ||fbib_dc_abstract(t.titleno) ||' <dcvalue element=\"language\" qualifier=\"iso\">es</dcvalue>' ||' <dcvalue element=\"date\" qualifier=\"accessioned\">'||to_char(nvl(fbibbus_fecha_pub(t.titleno),sysdate),'yyyy-mm-dd')||'</dcvalue>' ||' <dcvalue element=\"date\" qualifier=\"available\">'||to_char(nvl(fbibbus_fecha_pub(t.titleno),sysdate),'yyyy-mm-dd')||'</dcvalue>'||' <dcvalue element=\"date\" qualifier=\"issued\">'||to_char(nvl(fbibbus_fecha_pub(t.titleno),sysdate),'yyyy-mm-dd')||'</dcvalue>' ||fbib_dc_altertitulo(t.titleno) ||fbib_dc_publicador(t.titleno) ||fbib_dc_subject(t.titleno) ||fbib_dc_sponsors(t.titleno) ||fbib_dc_type(t.titleno) ||' </dublin_core>' dc_info from titleobjs t_o, titles t, objects o, mediatps m where t_o.titleno = t.titleno and t_o.objectno = o.objectno and t.mediatp = m.mediatp and t.articleno = "+ articleno +" and lower(o.locator) like '%.pdf' UNION select distinct replace(o.locator,'http://www.icesi.edu.co/esn/contenido/pdfs/',''), '<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?><dublin_core>' ||' <dcvalue element=\"title\" qualifier=\"none\">'||t.title||decode(t.subtitle,null,null,' : '||t.subtitle)||'</dcvalue>' ||decode(t.isbn,null,null,' <dcvalue element=\"identifier\" qualifier=\"isbn\">'||t.isbn||'</dcvalue>') ||'<dcvalue element=\"identifier\" qualifier=\"other\">'||t.titleno||'</dcvalue>' ||fbib_dc_autores(t.titleno) ||fbib_dc_abstract(t.titleno) ||' <dcvalue element=\"language\" qualifier=\"iso\">es</dcvalue>' ||' <dcvalue element=\"date\" qualifier=\"accessioned\">'||to_char(nvl(fbibbus_fecha_pub(t.titleno),sysdate),'yyyy-mm-dd')||'</dcvalue>' ||' <dcvalue element=\"date\" qualifier=\"available\">'||to_char(nvl(fbibbus_fecha_pub(t.titleno),sysdate),'yyyy-mm-dd')||'</dcvalue>'||' <dcvalue element=\"date\" qualifier=\"issued\">'||to_char(nvl(fbibbus_fecha_pub(t.titleno),sysdate),'yyyy-mm-dd')||'</dcvalue>' ||fbib_dc_altertitulo(t.titleno) ||fbib_dc_publicador(t.titleno) ||fbib_dc_subject(t.titleno) ||fbib_dc_sponsors(t.titleno) ||fbib_dc_type(t.titleno) ||' </dublin_core>' dc_info from titleobjs t_o, titles t, objects o, mediatps m where t_o.titleno = t.titleno and t_o.objectno = o.objectno and t.mediatp = m.mediatp and t.titleno = "+ articleno +" and lower(o.locator) like '%.pdf'";
        //consulta = "select distinct replace(o.locator,'http://www.icesi.edu.co/esn/contenido/pdfs/',''), '<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?><dublin_core>' ||' <dcvalue element=\"title\" qualifier=\"none\">'||t.title||decode(t.subtitle,null,null,' : '||t.subtitle)||'</dcvalue>' ||decode(t.isbn,null,null,' <dcvalue element=\"identifier\" qualifier=\"isbn\">'||t.isbn||'</dcvalue>') ||'<dcvalue element=\"identifier\" qualifier=\"other\">'||t.titleno||'</dcvalue>' ||olib.fbib_dc_autores(t.titleno) ||olib.fbib_dc_abstract(t.titleno) ||' <dcvalue element=\"language\" qualifier=\"iso\">es</dcvalue>' ||' <dcvalue element=\"date\" qualifier=\"accessioned\">'||to_char(nvl(olib.fbibbus_fecha_pub(t.titleno),sysdate),'yyyy-mm-dd')||'</dcvalue>' ||' <dcvalue element=\"date\" qualifier=\"available\">'||to_char(nvl(olib.fbibbus_fecha_pub(t.titleno),sysdate),'yyyy-mm-dd')||'</dcvalue>'||' <dcvalue element=\"date\" qualifier=\"issued\">'||to_char(nvl(olib.fbibbus_fecha_pub(t.titleno),sysdate),'yyyy-mm-dd')||'</dcvalue>' ||olib.fbib_dc_altertitulo(t.titleno) ||olib.fbib_dc_publicador(t.titleno) ||olib.fbib_dc_subject(t.titleno) ||olib.fbib_dc_sponsors(t.titleno) ||olib.fbib_dc_type(t.titleno) ||' </dublin_core>' dc_info from titleobjs t_o, titles t, objects o, mediatps m where t_o.titleno = t.titleno and t_o.objectno = o.objectno and t.mediatp = m.mediatp and t.articleno = "+ articleno +" and lower(o.locator) like '%.pdf' UNION select distinct replace(o.locator,'http://www.icesi.edu.co/esn/contenido/pdfs/',''), '<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?><dublin_core>' ||' <dcvalue element=\"title\" qualifier=\"none\">'||t.title||decode(t.subtitle,null,null,' : '||t.subtitle)||'</dcvalue>' ||decode(t.isbn,null,null,' <dcvalue element=\"identifier\" qualifier=\"isbn\">'||t.isbn||'</dcvalue>') ||'<dcvalue element=\"identifier\" qualifier=\"other\">'||t.titleno||'</dcvalue>' ||olib.fbib_dc_autores(t.titleno) ||olib.fbib_dc_abstract(t.titleno) ||' <dcvalue element=\"language\" qualifier=\"iso\">es</dcvalue>' ||' <dcvalue element=\"date\" qualifier=\"accessioned\">'||to_char(nvl(olib.fbibbus_fecha_pub(t.titleno),sysdate),'yyyy-mm-dd')||'</dcvalue>' ||' <dcvalue element=\"date\" qualifier=\"available\">'||to_char(nvl(olib.fbibbus_fecha_pub(t.titleno),sysdate),'yyyy-mm-dd')||'</dcvalue>'||' <dcvalue element=\"date\" qualifier=\"issued\">'||to_char(nvl(olib.fbibbus_fecha_pub(t.titleno),sysdate),'yyyy-mm-dd')||'</dcvalue>' ||olib.fbib_dc_altertitulo(t.titleno) ||olib.fbib_dc_publicador(t.titleno) ||olib.fbib_dc_subject(t.titleno) ||olib.fbib_dc_sponsors(t.titleno) ||olib.fbib_dc_type(t.titleno) ||' </dublin_core>' dc_info from titleobjs t_o, titles t, objects o, mediatps m where t_o.titleno = t.titleno and t_o.objectno = o.objectno and t.mediatp = m.mediatp and t.titleno = "+ articleno +" and lower(o.locator) like '%.pdf'";
        
        /*Gavarela: La consulta original sacaba todo el dublin core en una sola columna,
         * esto generaba problemas ya que el límite de retorno de un VARCHAR2 es de 4000,
         * por eso se partió el dublin core en 3 columnas, dejando la descripción como columna aparte*/
        consulta = /*"SELECT DISTINCT REPLACE(o.locator, 'http://www.icesi.edu.co/esn/contenido/pdfs/', '')" +
                ",'<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><dublin_core>' || "+
                "' <dcvalue element=\"title\" qualifier=\"none\">' || t.title || decode(t.subtitle,   NULL,   NULL,   ' : ' || t.subtitle) || '</dcvalue>' ||"+
                " decode(t.isbn,   NULL,   NULL,   ' <dcvalue element=\"identifier\" qualifier=\"isbn\">' || t.isbn || '</dcvalue>') || "+
                "'<dcvalue element=\"identifier\" qualifier=\"other\">' || t.titleno || '</dcvalue>' || "+"olib.fbib_dc_autores(t.titleno)" +
                ", olib.fbib_dc_abstract(t.titleno)|| "+
                "' <dcvalue element=\"identifier\" qualifier=\"OLIB\">http://biblioteca2.icesi.edu.co/cgi-olib?infile=details.glu&loid=' || t.titleno || '</dcvalue>' ||" +
                "' <dcvalue element=\"language\" qualifier=\"iso\">spa</dcvalue>' || "+
                "' <dcvalue element=\"date\" qualifier=\"available\">' || "+
                " to_char(nvl(olib.fbibbus_fecha_pub(t.titleno),   sysdate),   'yyyy-mm-dd') || '</dcvalue>' || "+
                "' <dcvalue element=\"date\" qualifier=\"issued\">' || to_char(nvl(olib.fbibbus_fecha_pub(t.titleno),   sysdate),   'yyyy-mm-dd') || '</dcvalue>' ||"+
                " olib.fbib_dc_altertitulo(t.titleno) || decode(t.isbn,   NULL,   NULL,   '<dcvalue element=\"pubplace\" qualifier=\"none\">' || tp.pubplace || '</dcvalue>') ||"+
                " olib.fbib_dc_publicador(t.titleno) || olib.fbib_dc_subject(t.titleno) || olib.fbib_dc_sponsors(t.titleno) || olib.fbib_dc_type(t.titleno) || "+
                "' </dublin_core>' dc_info" +
                " FROM titleobjs t_o, titles t, objects o, mediatps m, titlepub tp" +
                " WHERE t_o.titleno = t.titleno" +
                " AND t_o.objectno = o.objectno" +
                " AND t.mediatp = m.mediatp" +
                //" AND t.articleno = "+ articleno +
                " AND t.titleno = "+ articleno +
                " AND tp.titleno = "+ articleno +
                " AND LOWER(o.locator) LIKE '%.pdf'" +
                " UNION" +*/
                " SELECT DISTINCT REPLACE(o.locator, 'http://www.icesi.edu.co/esn/contenido/pdfs/', '')" +
                ", '<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><dublin_core>' || ' <dcvalue element=\"title\" qualifier=\"none\">' || t.title || decode(t.subtitle,   NULL,   NULL,   ' : ' || t.subtitle) || '</dcvalue>' || decode(t.isbn,   NULL,   NULL,   ' <dcvalue element=\"identifier\" qualifier=\"isbn\">' || t.isbn || '</dcvalue>') || '<dcvalue element=\"identifier\" qualifier=\"other\">' || t.titleno || '</dcvalue>' || olib.fbib_dc_autores(t.titleno)" +
                ", olib.fbib_dc_abstract(t.titleno) || ' <dcvalue element=\"identifier\" qualifier=\"OLIB\">http://biblioteca2.icesi.edu.co/cgi-olib?infile=details.glu&loid=' || t.titleno||'</dcvalue>' ||" +
                "'<dcvalue element=\"language\" qualifier=\"iso\">es</dcvalue>' || '</dcvalue>' || ' <dcvalue element=\"date\" qualifier=\"available\">' || to_char(nvl(olib.fbibbus_fecha_pub(t.titleno),   sysdate),   'yyyy-mm-dd') || '</dcvalue>' || ' <dcvalue element=\"date\" qualifier=\"issued\">' || to_char(nvl(olib.fbibbus_fecha_pub(t.titleno),   sysdate),   'yyyy-mm-dd') || '</dcvalue>' || olib.fbib_dc_altertitulo(t.titleno) || decode(t.isbn,   NULL,   NULL,   '<dcvalue element=\"pubplace\" qualifier=\"none\">' || tp.pubplace || '</dcvalue>') || olib.fbib_dc_publicador(t.titleno) || olib.fbib_dc_subject(t.titleno) || olib.fbib_dc_sponsors(t.titleno) || olib.fbib_dc_type(t.titleno) || ' </dublin_core>' dc_info" +
                " FROM titleobjs t_o, titles t, objects o, mediatps m, titlepub tp" +
                " WHERE t_o.titleno = t.titleno" +
                " AND t_o.objectno = o.objectno" +
                " AND t.mediatp = m.mediatp" +
                " AND t.titleno = "+ articleno +
                " AND tp.titleno = "+ articleno +
                " AND LOWER(o.locator) LIKE '%.pdf'";
        
        System.out.println(consulta);

        ResultSet rset = stmt.executeQuery(consulta);

        System.out.println("filas"+" "+filas);
        String dir[] = new String[filas];
        String dublin[] = new String[filas];
        int cont = 0;
        while (rset.next()) {
          
            //Reemplasar caracter '%' por 'Porciento' y '&' por 'y' (causan problemas en la importacion)
            /*String dc = rset.getString(2);
            dc += rset.getString(3);
            dc += rset.getString(4);*/
            
            String dc = rset.getString(1);
            dc += rset.getString(2);
            dc += rset.getString(3);
            

            //AQUI MACHETE! :)  EN CASO DE QUE LA CONSULTA RETORNE UNA CADENA HEXADECIMAL
            /*if (!dc.startsWith("<")) {
                int a = dc.indexOf("x");
                if (a != -1) {
                    dc = dc.substring(a + 1);
                    dc = convertToUnicodeString(dc);
                }
            }*/
            
            System.out.println("continuar dublin core");
            
            dc = dc.replace('&', 'y');
            dc = dc.replace("%", "porciento");

            dir[cont] = rset.getString(1);
            dublin[cont] = dc;
            cont++;
        }
        stmt.close();

        Object[] resultado = new Object[2];
        resultado[0] = dir;
        resultado[1] = dublin;
        return resultado;
    }

    /*public static void main(String[] args){
    // este main es solo para probar :)
    // test de olib
    Conectar conectar =  new Conectar("olib", "cobit05");
    try{
    conectar.setUrl("jdbc:oracle:thin:@192.168.220.20:1521:dbreg4"); // olib pruebas
    String articleno = "142886"; // un ejemplo
    Object retorno[] = conectar.conectarOlib(conectar.getUrl(), articleno);			
    //secuencia de impresión
    String dir[]= (String[])retorno[0];
    String dublin[]= (String[])retorno [1];
    for(int cont=0; cont<dir.length; cont++){
    // hay que validar los nulls
    System.out.println("---------------------");
    System.out.println(dir[cont]);
    System.out.println(dublin[cont]);
    }		
    }catch(SQLException e){
    e.printStackTrace();
    }catch (ClassNotFoundException e){
    e.printStackTrace();
    }
    // test de dspace bibdigital
    System.out.println("\n\n TEST DSPACE \n");
    conectar =  new Conectar("dspace", "icdspace");
    try{
    conectar.setUrl("jdbc:oracle:thin:@200.3.192.29:1521:FILES");
    String name= "Despidos y conflictos"; // un ejemplo
    Object retorno[] = conectar.conectarDspace(conectar.getUrl(), name);
    //secuencia de impresión
    int id[]= (int[])retorno[0];
    String names[]= (String[])retorno [1];
    for(int cont=0; cont<id.length; cont++){
    // hay que validar los nulls
    System.out.println(id[cont]+"\t"+names[cont]);
    }		
    }catch(SQLException e){
    e.printStackTrace();
    }catch (ClassNotFoundException e){
    e.printStackTrace();
    }
    }*/
    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }
}

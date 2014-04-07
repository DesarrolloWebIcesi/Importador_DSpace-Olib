/*
 * JDBCAdapter.java
 *
 * Created on 4 de julio de 2007, 08:55 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package vista;

/*
 * @(#)JDBCAdapter.java 1.12 01/12/03
 *
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/**
 * An adaptor, transforming the JDBC interface to the TableModel interface.
 *
 * @version 1.20 09/25/97
 * @author Philip Milne
 */
import java.util.Vector;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;

// import app_info.classes.db.*;

public class JDBCAdapter extends AbstractTableModel {
	protected Connection connection;

	protected Statement statement;

	protected ResultSet resultSet;

	protected String[] columnNames = {};

	protected Vector<Vector> rows = new Vector<Vector>();

	protected ResultSetMetaData metaData;

	public JDBCAdapter(String url, String driverName, String user, String passwd) throws ClassNotFoundException, SQLException{
		//try { //manejo de escepciones eliminado para propagarlo hasta la vista 
			Class.forName(driverName);
			/*gavarela: Innecesrio que imprima en la consola de Java
                        System.out.println("Opening db connection");*/

			connection = DriverManager.getConnection(url, user, passwd);
			statement = connection.createStatement();
		/*} catch (ClassNotFoundException ex) {
			System.err.println("Cannot find the database driver classes.");
			System.err.println(ex);
		} catch (SQLException ex) {
			System.err.println("Cannot connect to this database.");
			System.err.println(ex);
		}*/
	}

	public void executeQuery(String query) {
		boolean notRows = true;
		if (connection == null || statement == null) {
			System.err.println("There is no database to execute the query.");
			return;
		}
		try {
			/*gavarela: Innecesrio que imprima en la consola de Java
                    System.out.println("JDBCAdapter: Procesando consulta");*/
			resultSet = statement.executeQuery(query);
			/*gavarela: Innecesrio que imprima en la consola de Java
                        System.out.println("JDBCAdapter: Consulta obtenida");*/
			metaData = resultSet.getMetaData();

			int numberOfColumns = metaData.getColumnCount();
			columnNames = new String[numberOfColumns];
			// Get the column names and cache them.
			// Then we can close the connection.
			for (int column = 0; column < numberOfColumns; column++) {
				columnNames[column] = metaData.getColumnLabel(column + 1);
			}

			// Get all rows.
			rows = new Vector<Vector>();
			/*gavarela: Innecesrio que imprima en la consola de Java
                        System.out.println("JDBCAdapter: Construyendo DataModel");*/
			while (resultSet.next()) {
				Vector<String> newRow = new Vector<String>();
				for (int i = 0; i < numberOfColumns; i++) {
					newRow.addElement(resultSet.getString(i + 1));
				}
				rows.addElement(newRow);
				notRows = false;
			}
			/*gavarela: Innecesrio que imprima en la consola de Java
                        System.out.println("JDBCAdapter: DataModel creado");*/
			if (notRows) {
				Vector<String> newRow = new Vector<String>();
				for (int i = 0; i < numberOfColumns; i++) {
					newRow.addElement("");
				}
				rows.addElement(newRow);
			}
			// close(); Need to copy the metaData, bug in jdbc:odbc driver.
			fireTableChanged(null); // Tell the listeners a new table has
									// arrived.
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	public void close() throws SQLException {
		/*gavarela: Innecesrio que imprima en la consola de Java
            System.out.println("JDBCAdapter: Closing db connection");*/
		resultSet.close();
		statement.close();
		// connection.close();
	}

	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

	// ////////////////////////////////////////////////////////////////////////
	//
	// Implementation of the TableModel Interface
	//
	// ////////////////////////////////////////////////////////////////////////

	// MetaData

	public String getColumnName(int column) {
		if (columnNames[column] != null) {
			return columnNames[column];
		} else {
			return "";
		}
	}

	public Class getColumnClass(int column) {
		int type;
		try {
			type = metaData.getColumnType(column + 1);
		} catch (SQLException e) {
			return super.getColumnClass(column);
		}

		switch (type) {
		case Types.CHAR:
		case Types.VARCHAR:
		case Types.LONGVARCHAR:
			return String.class;

		case Types.BIT:
			return Boolean.class;

		case Types.TINYINT:
		case Types.SMALLINT:
		case Types.INTEGER:
			return Integer.class;

		case Types.BIGINT:
			return Long.class;

		case Types.FLOAT:
		case Types.DOUBLE:
			return Double.class;

		case Types.DATE:
			return java.sql.Date.class;

		default:
			return Object.class;
		}
	}

    @Override
	public boolean isCellEditable(int row, int column) {
		/*try {
			return metaData.isWritable(column + 1);
		} catch (SQLException e) {
			return false;
		}*/
        //gavarela: Para no permitir la edición de datos en la tabla que debe ser sólo de consulta
            return false;
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	// Data methods

	public int getRowCount() {
		return rows.size();
	}

	public Object getValueAt(int aRow, int aColumn) {
		Vector row = (Vector) rows.elementAt(aRow);
		return row.elementAt(aColumn);
	}

	public String dbRepresentation(int column, Object value) {
		int type;

		if (value == null) {
			return "null";
		}

		try {
			type = metaData.getColumnType(column + 1);
		} catch (SQLException e) {
			return value.toString();
		}

		switch (type) {
		case Types.INTEGER:
		case Types.DOUBLE:
		case Types.FLOAT:
			return value.toString();
		case Types.BIT:
			return ((Boolean) value).booleanValue() ? "1" : "0";
		case Types.DATE:
			return value.toString(); // This will need some conversion.
		default:
			return "\"" + value.toString() + "\"";
		}

	}
	
	/*public static void main(String arg[]){
		//String url, String driverName, String user, String passwd
                try {
                    JDBCAdapter jjbca = new JDBCAdapter("jdbc:oracle:thin:@200.3.192.29:1521:FILES","oracle.jdbc.driver.OracleDrive","","");
                    jjbca.executeQuery("select c.collection_id ID, c.name NOMBRE, c.short_description DESCRIPCION, t.name COMUNIDAD from collection c, community t, community2collection cc where t.community_ID = cc.community_ID and c.collection_id = cc.collection_id and (lower(c.short_description) like '%co%' or lower(c.name) like '%co%' or lower(t.name) like '%co%')");
                    JTable table = new JTable(jjbca);
                } catch (ClassNotFoundException ex) {
			System.err.println("Cannot find the database driver classes.");
			System.err.println(ex);
		} catch (SQLException ex) {
			System.err.println("Cannot connect to this database.");
			System.err.println(ex);
		}
	}*/

	public void setValueAt(Object value, int row, int column) {
		try {
			String tableName = metaData.getTableName(column + 1);
			// Some of the drivers seem buggy, tableName should not be null.
			if (tableName == null) {
				/*gavarela: Innecesrio que imprima en la consola de Java
                            System.out.println("Table name returned null.");*/
			}
			String columnName = getColumnName(column);
			String query = "update " + tableName + " set " + columnName + " = "
					+ dbRepresentation(column, value) + " where ";
			// We don't have a model of the schema so we don't know the
			// primary keys or which columns to lock on. To demonstrate
			// that editing is possible, we'll just lock on everything.
			for (int col = 0; col < getColumnCount(); col++) {
				String colName = getColumnName(col);
				if (colName.equals("")) {
					continue;
				}
				if (col != 0) {
					query = query + " and ";
				}
				query = query + colName + " = "
						+ dbRepresentation(col, getValueAt(row, col));
			}
			/*gavarela: Innecesrio que imprima en la consola de Java
                        System.out.println(query);
			System.out.println("Not sending update to database");*/
			// statement.executeQuery(query);
		} catch (SQLException e) {
			// e.printStackTrace();
			System.err.println("Update failed");
		}
		Vector dataRow = (Vector) rows.elementAt(row);
		dataRow.setElementAt(value, column);

	}
}
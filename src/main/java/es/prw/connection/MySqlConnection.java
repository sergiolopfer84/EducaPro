package es.prw.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import org.springframework.stereotype.Controller;

import io.github.cdimascio.dotenv.Dotenv;

@Controller
public class MySqlConnection {

	private String host;
	private String puerto;
	private String nameDB;
	private String usuario;
	private String password;

	// Atributo que indica si las operaciones se realizarán con autocomit. De lo
	// contrario, en la capa de datos (DAO's) se
	// debe controlar las transacciones.
	private boolean autocomit;

	// Bandera de error
	private boolean flagError;
	// Mensaje de error
	private String msgError;

	// Objeto de conexión
	private Connection connection;

	// inicialización de parámetros por defecto
	private void _initialize() {
		this.flagError = false;
		this.msgError = "";
		this.connection = null;
	}

	// Inicialización de la bandera y mensaje de error
	private void _initializeError() {
		this.flagError = false;
		this.msgError = "";
	}

	// Constructor implícito
	public MySqlConnection() {
		this._initialize();
		this.autocomit = true;
		Dotenv dotenv = Dotenv.load();
		this.host = dotenv.get("DB_HOST");
		this.puerto = dotenv.get("DB_PORT");
		this.nameDB = dotenv.get("DB_NAME");
		this.usuario = dotenv.get("DB_USER");
		this.password = dotenv.get("DB_PASSWORD");

	}

	public MySqlConnection(boolean _autocomit) {
		this._initialize();
		this.autocomit = _autocomit;
		Dotenv dotenv = Dotenv.load();
		this.host = dotenv.get("DB_HOST");
		this.puerto = dotenv.get("DB_PORT");
		this.nameDB = dotenv.get("DB_NAME");
		this.usuario = dotenv.get("DB_USER");
		this.password = dotenv.get("DB_PASSWORD");

	}

	// Constructor que permite indicar esquema por defecto
	public MySqlConnection(String _nameDB) {
		this._initialize();
		this.nameDB = _nameDB;
		this.autocomit = true;
	}

	public MySqlConnection(String _nameDB, boolean _autocomit) {
		this._initialize();
		this.nameDB = _nameDB;
		this.autocomit = _autocomit;
	}

	// Constructor que permite conectar a cualquier base de datos MySql
	public MySqlConnection(String _host, String _puerto, String _nameDB, String _usuario, String _password) {
		this._initialize();
		this.host = _host;
		this.puerto = _puerto;
		this.nameDB = _nameDB;
		this.usuario = _usuario;
		this.password = _password;
		this.autocomit = true;

	}

	public MySqlConnection(String _host, String _puerto, String _nameDB, String _usuario, String _password,
			boolean _autocomit) {
		this._initialize();
		this.host = _host;
		this.puerto = _puerto;
		this.nameDB = _nameDB;
		this.usuario = _usuario;
		this.password = _password;
		this.autocomit = _autocomit;

	}

	// Método que abre la conexión y en caso de error, activa la bandera de error.
	public void open() {

		try {
			this._initializeError();
			if ((this.connection == null) || (this.connection != null && this.connection.isClosed())) {
				Class.forName("com.mysql.cj.jdbc.Driver");
				this.connection = DriverManager.getConnection(
						"jdbc:mysql://" + this.host + ":" + this.puerto + "/" + this.nameDB, this.usuario,
						this.password);
			}
		} catch (ClassNotFoundException ex) {
			this.flagError = true;
			this.msgError = "Error al registrar el driver. +Info: " + ex.getMessage();
		} catch (SQLException ex) {
			this.flagError = true;
			this.msgError = "Error en Open. +Info: " + ex.getMessage();
		}
	}

	// Método que cierra la conexión si estaba abierta
	public void close() {
		try {
			this._initializeError();
			if ((this.connection != null) && (!this.connection.isClosed()))
				this.connection.close();
		} catch (SQLException ex) {
			this.flagError = true;
			this.msgError = "Error en close. +Info: " + ex.getMessage();
		}

	}

	// Método que retorna el resultado de cualquier consulta sql en un ResultSet
	public ResultSet executeSelect(String sql, Object... params) {
		try {
			this._initializeError(); // Inicializar cualquier error previo
			if (this.connection != null && !this.connection.isClosed()) {
				// Crear el PreparedStatement
				PreparedStatement stmt = this.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

				// Establecer los parámetros si existen
				if (params != null) {
					for (int i = 0; i < params.length; i++) {
						stmt.setObject(i + 1, params[i]);
					}
				}
				// Ejecutar la consulta
				return stmt.executeQuery();
			} else {
				this.flagError = true;
				this.msgError = "Error en ExecuteSelect. +Info: Conexión cerrada.";
			}
		} catch (SQLException ex) {
			this.flagError = true;
			this.msgError = "Error en ExecuteSelect. +Info: " + ex.getMessage();
		}

		return null; // Retornar null si hubo un error
	}

	// Método para ejecutar un insert. Devuelve la/s claves primarias generadas en
	// un record set.
	public ResultSet executeInsert(String sql, Object... params) {
		try {
			this._initializeError();
			if (this.connection != null && !this.connection.isClosed()) {
				PreparedStatement stmt = this.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				if (params != null) {
					// Establecer los parámetros
					for (int i = 0; i < params.length; i++) {
						stmt.setObject(i + 1, params[i]);
					}
				}
				/// stmt.executeUpdate();
				int rowsAffected = stmt.executeUpdate();
				// Verifica si hay claves generadas
				ResultSet rs = stmt.getGeneratedKeys();

				return rs;
			} else {
				this.flagError = true;
				this.msgError = "Error en ExecuteInsert. +Info: Conexión cerrada.";
			}
		} catch (SQLException ex) {
			this.flagError = true;
			this.msgError = "Error en ExecuteInsert. +Info: " + ex.getMessage();
		}

		return null;
	}

	// Método para ejecutar un update o delete. Devuelve el número de registros
	// afectados.
	public int executeUpdateOrDelete(String sql, Object... params) {
		int numRows = 0;
		try {
			this._initializeError();
			if (this.connection != null && !this.connection.isClosed()) {
				PreparedStatement stmt = this.connection.prepareStatement(sql);
				if (params != null) {
					// Establecer los parámetros
					for (int i = 0; i < params.length; i++) {
						stmt.setObject(i + 1, params[i]);
					}
				}
				numRows = stmt.executeUpdate();
			} else {
				this.flagError = true;
				this.msgError = "Error en executeUpdateOrDelete. +Info: Conexión cerrada.";
			}
		} catch (SQLException ex) {
			this.flagError = true;
			this.msgError = "Error en executeUpdateOrDelete. +Info: " + ex.getMessage();
		}

		return numRows;
	}

	// Método que fuerza un commit. Solo se realizará si el autocomit está
	// deshabilitado. (autocomit = false).
	public void commit() {
		try {
			this._initializeError();
			if (!this.connection.getAutoCommit()) {
				this.connection.commit();
			}
		} catch (SQLException ex) {
			this.flagError = true;
			this.msgError = "Error en commit. +Info: " + ex.getMessage();
		}
	}

	// Método que fuerza un rollback. Solo se realizará si el autocomit está
	// deshabilitado. (autocomit = false).
	public void rollback() {
		try {
			this._initializeError();
			if (!this.connection.getAutoCommit()) {
				this.connection.rollback();
			}
		} catch (SQLException ex) {
			this.flagError = true;
			this.msgError = "Error en rollback. +Info: " + ex.getMessage();
		}
	}

	// Devuelve el valor de la bandera de error
	public boolean isError() {
		return this.flagError;
	}

	// Devuelve la descripción del error
	public String msgError() {
		return this.msgError;
	}

}

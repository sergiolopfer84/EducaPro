package es.prw.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.stereotype.Controller;

import io.github.cdimascio.dotenv.Dotenv;

@Controller
public class MySqlConnection {

    private String host;
    private String puerto;
    private String nameDB;
    private String usuario;
    private String password;

    // Variables para OpenAI
    private String openAiApiKey;
    private String openAiApiUrl;

    // Atributo que indica si las operaciones se realizarán con autocommit
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

    // -----------------------------
    // CARGA DEL .env CON DOTENV
    // -----------------------------
    private void loadEnvVariables() {
        // Configuramos Dotenv para que busque el archivo .env en el directorio raíz
        Dotenv dotenv = Dotenv.configure()
            .directory(System.getProperty("user.dir")) // raíz del proyecto
            .filename(".env")                         // nombre del archivo
            .ignoreIfMissing()                        // no lanzará error si no lo encuentra
            .load();

        // Variables de la base de datos
        this.host = dotenv.get("DB_HOST", "localhost");
        this.puerto = dotenv.get("DB_PORT", "3306");
        this.nameDB = dotenv.get("DB_NAME", "default_db");
        this.usuario = dotenv.get("DB_USER", "root");
        this.password = dotenv.get("DB_PASSWORD", "");

        // Variables de OpenAI
        this.openAiApiKey = dotenv.get("OPENAI_API_KEY", "");
        this.openAiApiUrl = dotenv.get("OPENAI_API_URL", "");

  
    }

    // -----------------------------
    // CONSTRUCTORES
    // -----------------------------
    // Constructor implícito
    public MySqlConnection() {
        this._initialize();
        this.autocomit = true;
        loadEnvVariables();
    }

    public MySqlConnection(boolean _autocomit) {
        this._initialize();
        this.autocomit = _autocomit;
        loadEnvVariables();
    }

    // Constructor que permite indicar esquema por defecto
    public MySqlConnection(String _nameDB) {
        this._initialize();
        this.autocomit = true;
        loadEnvVariables();
        this.nameDB = _nameDB;  // Sobrescribimos el DB_NAME si queremos
    }

    public MySqlConnection(String _nameDB, boolean _autocomit) {
        this._initialize();
        this.nameDB = _nameDB;
        this.autocomit = _autocomit;
        loadEnvVariables();
    }

    // Constructor que permite conectar a cualquier base de datos MySQL
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

    // -----------------------------
    // GETTERS PARA OPENAI
    // -----------------------------
    public String getOpenAiApiKey() {
        return openAiApiKey;
    }

    public String getOpenAiApiUrl() {
        return openAiApiUrl;
    }

    // -----------------------------
    // MÉTODOS DE CONEXIÓN
    // -----------------------------
    public void open() {
        try {
            this._initializeError();
            if ((this.connection == null) || (this.connection != null && this.connection.isClosed())) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                this.connection = DriverManager.getConnection(
                        "jdbc:mysql://" + this.host + ":" + this.puerto + "/" + this.nameDB,
                        this.usuario,
                        this.password
                );
            }
        } catch (ClassNotFoundException ex) {
            this.flagError = true;
            this.msgError = "Error al registrar el driver. +Info: " + ex.getMessage();
        } catch (SQLException ex) {
            this.flagError = true;
            this.msgError = "Error en Open. +Info: " + ex.getMessage();
        }
    }

    public void close() {
        try {
            this._initializeError();
            if ((this.connection != null) && (!this.connection.isClosed())) {
                this.connection.close();
            }
        } catch (SQLException ex) {
            this.flagError = true;
            this.msgError = "Error en close. +Info: " + ex.getMessage();
        }
    }

    // -----------------------------
    // MÉTODOS DE CONSULTA
    // -----------------------------
    public ResultSet executeSelect(String sql, Object... params) {
        try {
            this._initializeError();
            if (this.connection != null && !this.connection.isClosed()) {
                PreparedStatement stmt = this.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                if (params != null) {
                    for (int i = 0; i < params.length; i++) {
                        stmt.setObject(i + 1, params[i]);
                    }
                }
                return stmt.executeQuery();
            } else {
                this.flagError = true;
                this.msgError = "Error en ExecuteSelect. +Info: Conexión cerrada.";
            }
        } catch (SQLException ex) {
            this.flagError = true;
            this.msgError = "Error en ExecuteSelect. +Info: " + ex.getMessage();
        }
        return null;
    }

    public ResultSet executeInsert(String sql, Object... params) {
        try {
            this._initializeError();
            if (this.connection != null && !this.connection.isClosed()) {
                PreparedStatement stmt = this.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                if (params != null) {
                    for (int i = 0; i < params.length; i++) {
                        stmt.setObject(i + 1, params[i]);
                    }
                }
                int rowsAffected = stmt.executeUpdate();
                return stmt.getGeneratedKeys();
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

    public int executeUpdateOrDelete(String sql, Object... params) {
        int numRows = 0;
        try {
            this._initializeError();
            if (this.connection != null && !this.connection.isClosed()) {
                PreparedStatement stmt = this.connection.prepareStatement(sql);
                if (params != null) {
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

    // -----------------------------
    // TRANSACCIONES
    // -----------------------------
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

    // -----------------------------
    // GETTERS DE ERRORES
    // -----------------------------
    public boolean isError() {
        return this.flagError;
    }

    public String msgError() {
        return this.msgError;
    }
}

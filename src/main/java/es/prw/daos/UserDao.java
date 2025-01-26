package es.prw.daos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import es.prw.connection.MySqlConnection;
import es.prw.models.Usuario;

@Repository
public class UserDao {

    private MySqlConnection objMySqlConnection;

   
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    public UserDao(PasswordEncoder passwordEncoder) {
        objMySqlConnection = new MySqlConnection(false);
        this.passwordEncoder = passwordEncoder;
    }

    // Método para registrar un nuevo usuario
    public Optional<Usuario> registerUser (String nombre, String email, String pass) throws SQLException {
        // Validar que el email no esté ya registrado
    
        if (isEmailTaken(email)) {
            return Optional.empty(); // Retorna vacío si el email ya está en uso
        }

        // Codificar la contraseña
        String encodedPassword = passwordEncoder.encode(pass);

        // Crear un nuevo objeto Usuario
        Usuario newUser  = new Usuario();
        newUser.setNombre(nombre);
        newUser.setEmail(email);
        newUser.setPass(encodedPassword);
    
        // Guardar el usuario en la base de datos
        saveUser(newUser);

        return Optional.of(newUser ); // Retorna el nuevo usuario registrado
    }

    // Método para guardar el usuario en la base de datos
    private void saveUser (Usuario user) throws SQLException {
    	
        String sql = "INSERT INTO usuario (nombre, email, pass) VALUES (?, ?, ?)";
     
        objMySqlConnection.open();

        if (!objMySqlConnection.isError()) {
            try {
                // Imprimir la consulta SQL y los parámetros
              

                ResultSet generatedKeys = objMySqlConnection.executeInsert(sql, user.getNombre(), user.getEmail(), user.getPass());
                if (generatedKeys != null && generatedKeys.next()) {
                    user.setIdUsuario(generatedKeys.getInt(1)); // Asignar el ID generado al usuario
                } 
                    objMySqlConnection.commit();
               
            } catch (SQLException e) {
                e.printStackTrace();
                objMySqlConnection.rollback();
                throw new SQLException("Error al guardar el usuario: " + e.getMessage());
            } finally {
                objMySqlConnection.close();
            }
        } else {
            throw new SQLException("Error al abrir la conexión: " + objMySqlConnection.msgError());
        }
    }

    // Método para verificar si el email ya está en uso
    private boolean isEmailTaken(String email) {
    	boolean emailExists = false;
        objMySqlConnection.open();
        

        if (!objMySqlConnection.isError()) {
            String sql = "SELECT COUNT(*) FROM usuario WHERE email = ?";
            ResultSet result = objMySqlConnection.executeSelect(sql, email); // Llama al método de MySqlConnection
            try {
                if (result != null && result.next()) {
                    emailExists = result.getInt(1) > 0; // Si el conteo es mayor que 0, el email ya está en uso
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                objMySqlConnection.close();
            }
        }
        return emailExists;
    }

    // Método para buscar un usuario por email
    public Optional<Usuario> findByEmail(String email) {
        Usuario user = null;
        objMySqlConnection.open();

        if (!objMySqlConnection.isError()) {
            String sql = "SELECT * FROM usuario WHERE email = ?";
            ResultSet result = objMySqlConnection.executeSelect(sql, email); // Llama al método de MySqlConnection
            try {
                if (result != null && result.next()) {
                    user = new Usuario();
                    user.setIdUsuario(result.getInt("id_usuario"));
                    user.setPass(result.getString("pass"));
                    user.setNombre(result.getString("nombre"));
                    user.setEmail(result.getString("email"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                objMySqlConnection.close();
            }
        }
        return Optional.ofNullable(user);
    }
}
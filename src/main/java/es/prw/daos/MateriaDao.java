package es.prw.daos;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import es.prw.connection.MySqlConnection;
import es.prw.models.Materia;

public class MateriaDao {

	private MySqlConnection objMySqlConnection;

	@Autowired
	public MateriaDao() {
		objMySqlConnection = new MySqlConnection();

	}

	public List<Materia> getMaterias() {
		List<Materia> materias = new ArrayList();
		objMySqlConnection.open();
		String sql = "Select * from materia";
		ResultSet result = objMySqlConnection.executeSelect(sql, null);
		try {
			while (result != null && result.next()) {
				Materia materia = new Materia();
				materia.setIdMateria(result.getInt("id_materia"));
				materia.setMateria(result.getString("nombre_materia"));
				materias.add(materia);

			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			objMySqlConnection.close();
		}

		return materias;
	}

}

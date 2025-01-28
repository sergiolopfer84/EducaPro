package es.prw.daos;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import es.prw.connection.MySqlConnection;
import es.prw.models.Pregunta;
import es.prw.models.Respuesta;

public class PreguntaDao {
	private MySqlConnection objMySqlConnection;

	@Autowired
	public PreguntaDao() {
		objMySqlConnection = new MySqlConnection();

	}

	public List<Pregunta> getPreguntasConRespuestas(int idTest) {
		List<Pregunta> preguntas = new ArrayList<>();
		objMySqlConnection.open();

		String sqlPreguntas = "SELECT id_pregunta, texto_pregunta, id_test FROM pregunta WHERE id_test = ?";
		
		ResultSet rsPreguntas = objMySqlConnection.executeSelect(sqlPreguntas, new Object[]{idTest});

		try {
			while (rsPreguntas != null && rsPreguntas.next()) {
				Pregunta pregunta = new Pregunta();
				pregunta.setIdPregunta(rsPreguntas.getInt("id_pregunta"));
				pregunta.setPregunta(rsPreguntas.getString("texto_pregunta"));
				pregunta.setIdTest(rsPreguntas.getInt("id_test"));

				// Obtener respuestas asociadas a la pregunta
				RespuestaDao respuestaDao = new RespuestaDao();
				List<Respuesta> respuestas = respuestaDao.getRespuestasByPregunta(pregunta.getIdPregunta());
				pregunta.setRespuestas(respuestas);
				preguntas.add(pregunta);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			objMySqlConnection.close();
		}
		return preguntas;
	}

}

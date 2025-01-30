package es.prw.daos;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import es.prw.connection.MySqlConnection;
import es.prw.models.Respuesta;

@Repository
public class RespuestaDao {

	private MySqlConnection objMySqlConnection;

	@Autowired
	public RespuestaDao() {
		objMySqlConnection = new MySqlConnection();

	}

	public List<Respuesta> getRespuestasByPregunta(int idPregunta) {
		List<Respuesta> respuestas = new ArrayList<>();
		objMySqlConnection.open();

		String sqlRespuestas = "SELECT id_respuesta, texto_respuesta, texto_explicacion, nota, id_pregunta FROM respuesta WHERE id_pregunta = ?";
		ResultSet rsRespuestas = objMySqlConnection.executeSelect(sqlRespuestas, new Object[]{idPregunta});
		try {
			while (rsRespuestas != null && rsRespuestas.next()) {
				Respuesta respuesta = new Respuesta();
				respuesta.setIdRespuesta(rsRespuestas.getInt("id_respuesta"));
				respuesta.setRespuesta(rsRespuestas.getString("texto_respuesta"));
				respuesta.setExplicacion(rsRespuestas.getString("texto_explicacion"));
				respuesta.setNota(rsRespuestas.getFloat("nota"));
				respuesta.setIdPregunta(rsRespuestas.getInt("id_pregunta"));

				respuestas.add(respuesta);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			objMySqlConnection.close();
		}

		return respuestas;
	}
	
	
	public List<Respuesta> getRespuestasByIds(List<Integer> idsRespuestas) {
	    List<Respuesta> respuestas = new ArrayList<>();

	    if (idsRespuestas.isEmpty()) {
	        return respuestas;
	    }

	    objMySqlConnection.open();
	    if (!objMySqlConnection.isError()) {
	        try {
	            // Crear una consulta SQL dinámica con la cantidad de IDs
	            String placeholders = String.join(",", idsRespuestas.stream().map(id -> "?").toArray(String[]::new));
	            String sql = "SELECT id_respuesta, texto_respuesta, texto_explicacion, nota, id_pregunta FROM respuesta WHERE id_respuesta IN (" + placeholders + ")";

	            ResultSet rsRespuestas = objMySqlConnection.executeSelect(sql, idsRespuestas.toArray());

	            while (rsRespuestas != null && rsRespuestas.next()) {
	                Respuesta respuesta = new Respuesta();
	                respuesta.setIdRespuesta(rsRespuestas.getInt("id_respuesta"));
	                respuesta.setRespuesta(rsRespuestas.getString("texto_respuesta"));
	                respuesta.setExplicacion(rsRespuestas.getString("texto_explicacion"));
	                respuesta.setNota(rsRespuestas.getFloat("nota"));
	                respuesta.setIdPregunta(rsRespuestas.getInt("id_pregunta"));

	                respuestas.add(respuesta);
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            objMySqlConnection.close();
	        }
	    }

	    return respuestas;
	}


}

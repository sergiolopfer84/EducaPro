package es.prw.daos;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import es.prw.connection.MySqlConnection;
import es.prw.models.Respuesta;
import jakarta.servlet.http.HttpSession;

@Repository
public class RespuestaDao {

	private MySqlConnection objMySqlConnection;

	@Autowired
	public RespuestaDao() {
		objMySqlConnection = new MySqlConnection();

	}

	public List<Respuesta> getRespuestasByPregunta(int idPregunta, HttpSession session) {
	    List<Respuesta> respuestas = (List<Respuesta>) session.getAttribute("respuestas_" + idPregunta);
	    
	    if (respuestas == null) { // Solo consulta si no están en sesión
	        respuestas = new ArrayList<>();
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

	        // Almacenar respuestas en sesión
	        session.setAttribute("respuestas_" + idPregunta, respuestas);
	    }

	    return respuestas;
	}

	public List<Respuesta> getRespuestasByIds(List<Integer> idsRespuestas, HttpSession session) {
	    if (idsRespuestas == null || idsRespuestas.isEmpty()) {
	        System.err.println("Error: La lista de IDs de respuestas es nula o vacía.");
	        return new ArrayList<>();
	    }

	    List<Respuesta> respuestas = (List<Respuesta>) session.getAttribute("respuestas_" + idsRespuestas.hashCode());

	    if (respuestas != null) {
	        System.out.println("Respuestas recuperadas desde sesión.");
	        return respuestas; // Si ya están en sesión, retornarlas directamente
	    }

	    respuestas = new ArrayList<>();
	    objMySqlConnection.open();
	    
	    if (!objMySqlConnection.isError()) {
	        try {
	            // Construimos la consulta con placeholders dinámicos
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

	            session.setAttribute("respuestas_" + idsRespuestas.hashCode(), respuestas); // Guardar en sesión

	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            objMySqlConnection.close();
	        }
	    }

	    return respuestas;
	}



}

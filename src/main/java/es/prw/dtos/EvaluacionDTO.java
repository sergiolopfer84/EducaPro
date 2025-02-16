package es.prw.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class EvaluacionDTO {

    @NotNull(message = "El ID del test no puede ser nulo.")
    @Min(value = 1, message = "El ID del test debe ser mayor que 0.")
    private Integer idTest;

    @NotEmpty(message = "La lista de respuestas no puede estar vacía.")
    private List<Integer> respuestas;

    public EvaluacionDTO() {
    }

    public EvaluacionDTO(Integer idTest, List<Integer> respuestas) {
        this.idTest = idTest;
        this.respuestas = respuestas;
    }

    public Integer getIdTest() {
        return idTest;
    }

    public void setIdTest(Integer idTest) {
        this.idTest = idTest;
    }

    public List<Integer> getRespuestas() {
        return respuestas;
    }

    public void setRespuestas(List<Integer> respuestas) {
        this.respuestas = respuestas;
    }
}

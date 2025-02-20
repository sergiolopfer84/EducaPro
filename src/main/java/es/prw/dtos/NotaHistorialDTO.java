package es.prw.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

public class NotaHistorialDTO {

    @NotNull(message = "El nombre del test no puede ser nulo.")
    @NotEmpty(message = "El nombre del test no puede estar vacío.")
    private String test;

    @NotNull(message = "La lista de notas no puede ser nula.")
    private List<Double> notas;

    public NotaHistorialDTO() {
        this.notas = Collections.emptyList(); // Evita valores nulos
    }

    public NotaHistorialDTO(String test, List<Double> notas) {
        this.test = test;
        this.notas = (notas != null) ? notas : Collections.emptyList(); // Asegura que la lista nunca sea nula
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public List<Double> getNotas() {
        return notas;
    }

    public void setNotas(List<Double> notas) {
        this.notas = (notas != null) ? notas : Collections.emptyList();
    }
}

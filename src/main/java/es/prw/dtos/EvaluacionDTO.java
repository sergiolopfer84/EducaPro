package es.prw.dtos;



import java.util.List;

public class EvaluacionDTO {
    private Integer idTest;
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
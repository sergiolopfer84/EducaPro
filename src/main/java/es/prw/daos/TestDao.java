package es.prw.daos;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import es.prw.connection.MySqlConnection;
import es.prw.models.Test;

@Repository // 🟢 Agrega esta anotación para que Spring lo reconozca como Bean
public class TestDao {
    private MySqlConnection objMySqlConnection;

    @Autowired
    public TestDao() {
        objMySqlConnection = new MySqlConnection();
    }

    public List<Test> getTests(int idMateria) {
        List<Test> tests = new ArrayList<>();
        objMySqlConnection.open();
        String sql = "SELECT * FROM test WHERE id_materia = ?";
        ResultSet result = objMySqlConnection.executeSelect(sql, new Object[]{idMateria});

        try {
            while (result != null && result.next()) {
                Test test = new Test();
                test.setIdTest(result.getInt("id_test"));
                test.setTest(result.getString("nombre_test"));
                test.setIdMateria(result.getInt("id_materia"));
                tests.add(test);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            objMySqlConnection.close();
        }
        return tests;
    }
}

import java.io.Serializable;

public class RespostaLogin implements Serializable {
    private Boolean resposta;
    private String directoryAtual;


    public RespostaLogin(Boolean resposta, String directoryAtual) {
        this.resposta = resposta;
        this.directoryAtual = directoryAtual;
    }

    @Override
    public String toString() {
        return "respostaTESTE{" +
                "resposta=" + resposta +
                ", directoryAtual='" + directoryAtual + '\'' +
                '}';
    }

    public Boolean getResposta() {
        return resposta;
    }

    public void setResposta(Boolean resposta) {
        this.resposta = resposta;
    }

    public String getDirectoryAtual() {
        return directoryAtual;
    }

    public void setDirectoryAtual(String directoryAtual) {
        this.directoryAtual = directoryAtual;
    }
}

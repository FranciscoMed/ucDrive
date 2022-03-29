import java.io.Serializable;

public class RespostaServidor implements Serializable
{
    private String resposta;
    private String mensagemCompleta;


    public RespostaServidor(String resposta, String mensagemCompleta) {
        this.resposta = resposta;
        this.mensagemCompleta = mensagemCompleta;
    }

    @Override
    public String toString() {
        return "RespostaServidor{" +
                "resposta='" + resposta + '\'' +
                ", mensagemCompleta='" + mensagemCompleta + '\'' +
                '}';
    }

    public String getResposta() {
        return resposta;
    }

    public void setResposta(String resposta) {
        this.resposta = resposta;
    }

    public String getMensagemCompleta() {
        return mensagemCompleta;
    }

    public void setMensagemCompleta(String mensagemCompleta) {
        this.mensagemCompleta = mensagemCompleta;
    }
}

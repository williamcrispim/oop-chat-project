import java.util.ArrayList;
import java.util.logging.Logger;
import java.io.BufferedWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.util.logging.Level;

public class ServidorSocketChat extends Thread {
    static Logger LOGGER = Logger.getLogger(ServidorSocketChat.class.getName());

    private final static String INICIANDO = "Chat iniciando...\n";
    private final static String DESCRICAO = "Projeto de POO - UFABC\n";
    private final static String AUTOR = "Elaborado por: William Crispim (RA: 11201722180)\n";
    private final static String INFORMACOES = "Desenvolvedor fullstack Jr. na IBM (Node.js/Vue.js)\n";
    private final static String SAIR = "Sair";

    private static ServerSocket servidor;
    private Socket socket;
    private static ArrayList<BufferedWriter> mensagemClienteBuffer;
    private String nomeDoCliente;
    private InputStream entradaDoCliente;
    private InputStreamReader inputStreamReader;
    private BufferedReader bufferedReader;

    public ServidorSocketChat(Socket socket) {
        this.socket = socket;

        try {
            entradaDoCliente = socket.getInputStream();
            inputStreamReader = new InputStreamReader(entradaDoCliente);
            bufferedReader = new BufferedReader(inputStreamReader);
        } catch (IOException erro) {
            LOGGER.log(Level.SEVERE, erro.getMessage());
        }
    }

    public void run() {
        try {
            String mensagemDoCliente;
            OutputStream outputStream = this.socket.getOutputStream();
            Writer writer = new OutputStreamWriter(outputStream);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            mensagemClienteBuffer.add(bufferedWriter);
            nomeDoCliente = mensagemDoCliente = bufferedReader.readLine();

            while (!SAIR.equalsIgnoreCase(mensagemDoCliente) && mensagemDoCliente != null) {
                mensagemDoCliente = bufferedReader.readLine();
                enviarMensagemUsuarios(bufferedWriter, mensagemDoCliente);
                LOGGER.log(Level.INFO, nomeDoCliente + " " + "digitou: " + mensagemDoCliente);
            }

        } catch (Exception erro) {
            LOGGER.log(Level.SEVERE, erro.getMessage());
        }
    }

    public void enviarMensagemUsuarios(BufferedWriter bufferedWriter, String mensagemDoCliente) throws IOException {
        BufferedWriter bufferedWriterParaComparar;

        for (BufferedWriter cliente : mensagemClienteBuffer) {
            bufferedWriterParaComparar = (BufferedWriter) cliente;
            if (!(bufferedWriter == bufferedWriterParaComparar)) {
                cliente.write(nomeDoCliente + ":      " + mensagemDoCliente + "\r\n");
                cliente.flush();
            }
        }
    }

    public static void main(String[] args) {
        LOGGER.log(Level.INFO, "\n\n" + INICIANDO + DESCRICAO + AUTOR + INFORMACOES + "\n\n");

        try {
            JTextField portaDoServidor = new JTextField();
            Object[] informacoesDoServidor = { new JLabel("Em qual o servidor deve rodar?"), portaDoServidor };

            // Mostra a janela para o usuario digitar a porta
            JOptionPane.showMessageDialog(null, informacoesDoServidor);
            // Cria o servidor na porta que o usuario escolheu
            servidor = new ServerSocket(Integer.parseInt(portaDoServidor.getText()));
            mensagemClienteBuffer = new ArrayList<BufferedWriter>();
            JOptionPane.showMessageDialog(null, "O servidor foi iniciado na porta: " + portaDoServidor.getText());
            LOGGER.log(Level.INFO, "O servidor foi iniciado na porta: " + portaDoServidor.getText());

            while (true) {
                LOGGER.log(Level.INFO, "Aguardando algum usuario conectar...");
                Socket socket = servidor.accept();
                LOGGER.log(Level.INFO, "Alguem entrou no servidor...");
                Thread thread = new ServidorSocketChat(socket);
                thread.start();
                LOGGER.log(Level.INFO, "Uma nova thread foi criada");
            }

        } catch (Exception erro) {
            LOGGER.log(Level.SEVERE, erro.getMessage());
        }
    }
}

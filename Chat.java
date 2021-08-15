
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import javax.swing.*;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Chat extends JFrame implements ActionListener, KeyListener {
    static Logger LOGGER = Logger.getLogger(ServidorSocketChat.class.getName());

    private Socket socket;
    private OutputStream outputStream;
    private Writer writer;
    private BufferedWriter bufferedWriter;
    private JPanel jPanelConteudo;
    private JTextArea jTextArea;
    private JTextField jTextFieldMensagem;
    private JTextField jTextFieldIp;
    private JTextField jTextFieldPorta;
    private JTextField jTextFieldNome;
    private JButton jButtonEnviar;
    private JButton jButtonSair;

    private final static String SAIR = "Sair";

    public Chat() throws IOException {
        // Painel de Conexao ao chat
        JLabel jLabelMessage = new JLabel("Entrar no Chat");
        jTextFieldIp = new JTextField("127.0.0.1");
        jTextFieldPorta = new JTextField("8001");
        jTextFieldNome = new JTextField("Digite seu nome aqui");
        Object[] objeto = { jLabelMessage, jTextFieldIp, jTextFieldPorta, jTextFieldNome };
        JOptionPane.showMessageDialog(null, objeto);

        // Configurando o painel
        jPanelConteudo = new JPanel();
        jTextArea = new JTextArea(10, 20);
        jTextArea.setEditable(false);
        jTextFieldMensagem = new JTextField(20);
        jButtonEnviar = new JButton("Enviar");
        jButtonSair = new JButton("Sair");
        jButtonEnviar.addActionListener(this);
        jButtonSair.addActionListener(this);
        jButtonEnviar.addKeyListener(this);
        jTextFieldMensagem.addKeyListener(this);
        JScrollPane rolagem = new JScrollPane(jTextArea);
        jTextArea.setLineWrap(true);

        // Renderizando o painel
        jPanelConteudo.add(rolagem);
        jPanelConteudo.add(jTextFieldMensagem);
        jPanelConteudo.add(jButtonSair);
        jPanelConteudo.add(jButtonEnviar);

        // Criando o painel
        setTitle(jTextFieldNome.getText());
        setContentPane(jPanelConteudo);
        setLocationRelativeTo(null);
        setResizable(false);
        setSize(250, 300);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    /* Conecta no servidor socket */
    public void conestarServidorSocket() throws IOException {
        socket = new Socket(jTextFieldIp.getText(), Integer.parseInt(jTextFieldPorta.getText()));
        outputStream = socket.getOutputStream();
        writer = new OutputStreamWriter(outputStream);
        bufferedWriter = new BufferedWriter(writer);
        bufferedWriter.write(jTextFieldNome.getText() + "\r\n");
        bufferedWriter.flush();
    }

    /* Envia mensagem para o servidor socket */
    public void enviarMensagemServidorSocket(String mensagem) throws IOException {
        if (mensagem.equals(SAIR)) {
            bufferedWriter.write("Desconectou-se. \r\n");
            jTextArea.append("Desconectou-se. \r\n");
        } else {
            bufferedWriter.write(mensagem + "\r\n");
            jTextArea.append(jTextFieldNome.getText() + ":      " + jTextFieldMensagem.getText() + "\r\n");
        }
        bufferedWriter.flush();
        jTextFieldMensagem.setText("");
    }

    /* Recebe a mensagem do servidor socket */
    public void esperarNovasMensagens() throws IOException {
        InputStream inputStream = socket.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String mensagem = "";

        while (!SAIR.equalsIgnoreCase(mensagem)) {
            if (bufferedReader.ready()) {
                mensagem = bufferedReader.readLine();
                jTextArea.append(mensagem + "\r\n");
            }
        }
    }

    public void desconectarServidorSocket() throws IOException {
        enviarMensagemServidorSocket(SAIR);
        bufferedWriter.close();
        writer.close();
        outputStream.close();
        socket.close();
    }

    /* Sobrescreve a acao original do panel */
    @Override
    public void actionPerformed(ActionEvent acao) {
        try {
            if (acao.getActionCommand().equals(jButtonEnviar.getActionCommand()))
                enviarMensagemServidorSocket(jTextFieldMensagem.getText());
            else if (acao.getActionCommand().equals(jButtonSair.getActionCommand()))
                desconectarServidorSocket();
        } catch (IOException erro) {
            LOGGER.log(Level.SEVERE, erro.getMessage());
        }
    }

    /* Sobrescreve a acao original do panel */
    @Override
    public void keyPressed(KeyEvent evento) {
        if (evento.getKeyCode() == KeyEvent.VK_ENTER) {
            try {
                enviarMensagemServidorSocket(jTextFieldMensagem.getText());
            } catch (IOException erro) {
                LOGGER.log(Level.SEVERE, erro.getMessage());
            }
        }
    }

    /* Código necessario para fugir de um erro específico */
    @Override
    public void keyReleased(KeyEvent evento) {
    }

    /* Código necessario para fugir de um erro específico */
    @Override
    public void keyTyped(KeyEvent evento) {
    }

    public static void main(String[] args) throws IOException {
        Chat chat = new Chat();
        chat.conestarServidorSocket();
        chat.esperarNovasMensagens();
    }
}
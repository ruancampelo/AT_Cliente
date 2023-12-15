package org.example;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        Cliente();
    }


    private static void Cliente() {

        String path = "http://localhost:8080/api";

        JFrame frame = new JFrame("Cliente");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1));

        JTextField dados = new JTextField();
        JPanel result = new JPanel();

        result.setSize(100,400);
        dados.setFont((new Font("Arial", Font.PLAIN, 35)));

        JLabel label1 = new JLabel("Dados do RFID: ");
        label1.setFont((new Font("Arial", Font.BOLD, 35)));

        panel.add(label1);
        panel.add(dados);
        panel.add(result);

        dados.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateLabel();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateLabel();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateLabel();
            }

            private void updateLabel() {
                try{
                    HttpURLConnection connection = HttpConnection(path, dados);

                    // Ler a resposta da requisição
                    String resultResponse = LerResultado(connection);

                    if(resultResponse.equals("NACK")) {
                        result.setBackground(Color.RED);
                        Temporizador(result);
                    } else {
                        result.setBackground(Color.GREEN);
                        Temporizador(result);
                    }
                    // Fechar a conexão
                    connection.disconnect();
                }catch (Exception ex) {
                }
            }
        });
        frame.add(panel);
        frame.setVisible(true);
    }

    private static String LerResultado(HttpURLConnection connection) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            JsonElement jsonElement = JsonParser.parseString(String.valueOf(response));
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String responseResult = jsonObject.get("resultado").getAsString();
            return  responseResult;
        }
    }

    private static HttpURLConnection HttpConnection(String path, JTextField dados) throws IOException {
        URL url = new URL(path);

        // Abrir conexão HttpURLConnection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Configurar a conexão para requisição POST
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");

        // Habilitar envio de dados na requisição
        connection.setDoOutput(true);

        // Dados a serem enviados no corpo da requisição
        String requestBody = "{\"dados\": "+ dados.getText()+"}";

        // Escrever os dados no corpo da requisição
        try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
            byte[] postData = requestBody.getBytes(StandardCharsets.UTF_8);
            wr.write(postData);
        }
        return connection;
    }

    private static void Temporizador(JPanel result) {
        Timer timer = new Timer(3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result.setBackground(Color.WHITE);
            }
        });
        timer.setRepeats(false);
        timer.start();
    }
}
package com.muc;

import com.mongodb.MongoClientURI;
import org.apache.commons.lang3.StringUtils;
import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;


public class ServerWorker extends Thread {

    private final Socket clientSocket;
    private final Server server;
    private String login = null;
    MongoClientURI uri = new MongoClientURI("mongodb+srv://prateek:test1234@cluster0.fyuen.mongodb.net/<dbname>?retryWrites=true&w=majority");


    public ServerWorker(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClientSocket() throws IOException {
        InputStream inputStream = clientSocket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ( (line = reader.readLine()) != null) {

            String[] tokens = StringUtils.split(line);
            if (tokens != null && tokens.length > 0) {
                String cmd = tokens[0];
                if ("logoff".equals(cmd) || "quit".equalsIgnoreCase(cmd)) {
                    handleLogoff();
                    break;
                } else if ("login".equalsIgnoreCase(cmd)) {
                    handleLogin(tokens);
                } else if ("msg".equalsIgnoreCase(cmd)) {
                    String[] tokensMsg = StringUtils.split(line, null, 3);
                    handleMessage(tokensMsg);
                } else if ("server".equalsIgnoreCase(cmd)) {
                    String[] tokensMsg = StringUtils.split(line, null, 2);
                    ChatWithServer(tokensMsg);
                }else if ("file".equalsIgnoreCase(cmd)) {
                    String[] tokensMsg = StringUtils.split(line, null, 2);
                    SendFile(tokensMsg);
                }else if ("upload".equalsIgnoreCase(cmd)) {
                    String[] tokensMsg = StringUtils.split(line, null, 3);
                    Uploadfile(tokensMsg);
                }else if ("download".equalsIgnoreCase(cmd)) {
                    String[] tokensMsg = StringUtils.split(line, null, 3);
                    Downloadfile(tokensMsg);

                }
                else {
                    String msg = "unknown " + cmd + "\n";

                    PrintWriter pr  = new PrintWriter(clientSocket.getOutputStream(),true);
                    pr.println(msg);
                }
            }
        }

        //clientSocket.close();
    }

    private void Downloadfile(String[] tokensMsg) throws IOException {
        String dbName = "dbone"; String BucketName = tokensMsg[1]; String Filename = tokensMsg[2];
        MongoServer mg = new MongoServer(uri);
        boolean flag = mg.name_of_files(dbName,BucketName,Filename);
        if(!flag) Filename = "file dosnt exists";
        if((flag)){
            send("Search_Found");
            send(Filename);
        }

    }

   private void Uploadfile(String[] tokensMsg) throws IOException {
        if(this.login.equalsIgnoreCase("prateek")){
            MongoServer mg = new MongoServer(uri);
            String dbName = "dbone"; String BucketName = tokensMsg[1]; String name = tokensMsg[2];
            String path = "E:\\toupload.pdf";
            mg.UploadFiles(dbName, BucketName,name, path);
        }
        else {
            send("you cannot upload this file");
        }
    }

    private void SendFile(String[] tokensMsg) throws IOException {

        String nameOfFile = tokensMsg[1];
        List<ServerWorker> workerList = server.getWorkerList();
        String filename = "E:\\" + nameOfFile;
        File myfile = new File(filename);
       // int length = (int) myfile.length();

        PrintWriter pr = new PrintWriter(clientSocket.getOutputStream(), true);

        try {
            FileInputStream fr = new FileInputStream(myfile);
            byte[] b = new byte[20002];
            fr.read(b,0,b.length);

            OutputStream os = clientSocket.getOutputStream();
            for (ServerWorker worker : workerList) {
                if (login.equalsIgnoreCase(worker.getLogin())) {
                    pr.println("file");
                    os.write(b,0,b.length);
                    os.flush();
                    //os.close();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            String str = "file not found";
            send(str);
        }
    }

    private void ChatWithServer(String[] tokensMsg) throws IOException {
        String msg = tokensMsg[1];
        msg = this.login + " : " + msg;
        System.out.println(msg);
        Scanner sc = new Scanner(System.in);
            msg = sc.nextLine();
            msg = "server : " + msg;
        send(msg);

    }

    private void handleMessage(String[] tokens) throws IOException {
        String sendTo = tokens[1];
        String body = tokens[2];

        List<ServerWorker> workerList = server.getWorkerList();
        if(sendTo.equalsIgnoreCase("all")){
            for(ServerWorker worker : workerList){
                String outMsg = "msg from " + login + " = " + body + "\n";
                worker.send(outMsg);
            }
        }
        else {
            for (ServerWorker worker : workerList) {
                if (sendTo.equalsIgnoreCase(worker.getLogin())) {
                    String outMsg = "msg from " + login + " = " + body + "\n";
                    worker.send(outMsg);
                }
            }
        }

    }

    private void handleLogoff() throws IOException {
        server.removeWorker(this);
        List<ServerWorker> workerList = server.getWorkerList();

        // send other online users current user's status
        String onlineMsg = "offline " + login + "\n";
        for(ServerWorker worker : workerList) {
            if (!login.equals(worker.getLogin())) {
                worker.send(onlineMsg);
            }
        }
        clientSocket.close();
    }

    public String getLogin() {
        return login;
    }

    private void handleLogin(String[] tokens) throws IOException {
        if (tokens.length == 3) {
            String login = tokens[1];
            String password = tokens[2];

            if ((login.equals("guest") && password.equals("guest")) || (login.equals("prateek") && password.equals("prateek")) ) {
                String msg = "ok login\n";
                PrintWriter pr  = new PrintWriter(clientSocket.getOutputStream(),true);
                pr.println(msg);
                this.login = login;
                System.out.println("User logged in succesfully: " + login);

                List<ServerWorker> workerList = server.getWorkerList();

                // send current user all other online logins
                for(ServerWorker worker : workerList) {
                    if (worker.getLogin() != null) {
                        if (!login.equals(worker.getLogin())) {
                            String msg2 = "online " + worker.getLogin() + "\n";
                            send(msg2);
                        }
                    }
                }

                // send other online users current user's status
                String onlineMsg = "online " + login + "\n";
                for(ServerWorker worker : workerList) {
                    if (!login.equals(worker.getLogin())) {
                        worker.send(onlineMsg);
                    }
                }

            } else {
                String msg = "error login\n";
                PrintWriter pr  = new PrintWriter(clientSocket.getOutputStream(),true);
                pr.println(msg);
            }
        }
    }

    private void send(String msg) throws IOException {
        if (login != null) {
            PrintWriter pr  = new PrintWriter(clientSocket.getOutputStream(),true);
            pr.println(msg);
        }
    }
}

package com.muc;

import com.mongodb.MongoClientURI;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class clientWorker extends  Thread{

    private final int serverPort;
    private final String servername;
    private Socket socket;
    private InputStream istream;
    private BufferedReader bufferedIn;


    public clientWorker(String servername,int serverPort) {
        this.serverPort = serverPort;
        this.servername = servername;
    }

    @Override
    public void run() {
        try {
            this.socket = new Socket(servername, serverPort);
            System.out.println("Client port is " + socket.getLocalPort());
            this.istream = socket.getInputStream();
            this.bufferedIn = new BufferedReader(new InputStreamReader(istream));

            login("guest" , "guest");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void login(String name, String password)  throws IOException {

        String cmd = "login " + name + " " + password + "\n";
        PrintWriter pr  = new PrintWriter(socket.getOutputStream(),true);
        pr.println(cmd);

        String response = bufferedIn.readLine();
        System.out.println("Response Line:" + response);

        if ("ok login".equalsIgnoreCase(response)) {
            System.out.println("login successful");
            startMessageReader();


        } else {
            System.out.println("not able to login");
        }
    }

    private void startMessageReader() throws IOException {

       Thread t = new Thread(() -> readMessageLoop());
        t.start();
        while(true){
        Scanner scanner = new Scanner(System.in);
        String inputString = scanner.nextLine();

        try {

            PrintWriter pr  = new PrintWriter(socket.getOutputStream(),true);
            pr.println(inputString);

        } catch (IOException e) {
            e.printStackTrace();
        }}
    }

    private void readMessageLoop() {
        try {
            String line;
            while ((line = bufferedIn.readLine()) != null) {
               if(line.equalsIgnoreCase("file")){

                   byte[] b = new byte[20002];
                   System.out.println("file is coming");
                   FileOutputStream fr = new FileOutputStream("D:\\recieved.txt");
                   istream.read(b,0,b.length);
                   fr.write(b,0, b.length);
                   System.out.println("received");
               }
               else if(line.equalsIgnoreCase("Search_Found")){
                   String name = bufferedIn.readLine();
                   System.out.println(name);
                   fileDownload(name);
               }

               else System.out.println(line);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void fileDownload(String name) {
        MongoClientURI uri = new MongoClientURI("mongodb+srv://prateek:test1234@cluster0.fyuen.mongodb.net/<dbname>?retryWrites=true&w=majority");
        String dbName = "dbone"; String BucketName = "testfiles"; String path = "D:\\downloadfile1.pdf";
        MongoClientt mg = new MongoClientt(uri);
        mg.DownloadFiles(dbName, BucketName, name, path);
        System.out.println("download complete");
    }


}

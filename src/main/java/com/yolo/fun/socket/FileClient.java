package com.yolo.fun.socket;

import java.io.*;
import java.net.Socket;

/**
 * @author ravi
 */
public class FileClient {

    public class UserCommandLineInput implements Runnable {

        private DataOutputStream dataOutputStream;
        private Socket socket;

        public UserCommandLineInput(DataOutputStream dataOutputStream, Socket socket) {
            this.dataOutputStream = dataOutputStream;
            this.socket = socket;
        }

        @Override public void run() {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
                while (true) {
                    String command = br.readLine();
                    System.out.println("Data to be written to server: " + command);
                    dataOutputStream.writeUTF(command);
                    if (command.equalsIgnoreCase("CMD:SOCK_CLOSE")) {
                        socket.close();
                        System.exit(0);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class ServerDataHandler implements Runnable {

        private DataInputStream dataInputStream;

        public ServerDataHandler(DataInputStream dataInputStream) {
            this.dataInputStream = dataInputStream;
        }

        @Override public void run() {
            while (true) {
                try {
                    if (dataInputStream.available() > 0) {
                        System.out.println(dataInputStream.readUTF());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Socket socket;

    public FileClient(String host, int port) {
        try {
            socket = new Socket(host, port);
            new Thread(new UserCommandLineInput(new DataOutputStream(socket.getOutputStream()), socket)).start();
            new Thread(new ServerDataHandler(new DataInputStream(socket.getInputStream()))).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        FileClient fc = new FileClient("localhost", 1988);
    }
}

package com.yolo.fun.socket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author ravi
 */
public class FileServer {

    private ServerSocket serverSocket;

    public FileServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server Socket started..");

            listenForClientConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listenForClientConnection() {
        while (true) {
            try {
                System.out.println("Listening for client socket connection..");

                Socket clientSock = serverSocket.accept();

                System.out.println("A new client connection has been accepted..");

                new Thread(new ClientRequestHandler(clientSock)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class ClientRequestHandler implements Runnable {

        private Socket socket;

        public ClientRequestHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            RandomAccessFile fileStore = null;
            File file = null;
            String newLine = System.getProperty("line.separator");
            try (DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());//
                    DataOutputStream dataOutPutStream = new DataOutputStream(socket.getOutputStream());) {

                boolean fileOpen = false;
                boolean writeFile = false;
                while (true) {
                    if (dataInputStream.available() > 0) {
                        String data = dataInputStream.readUTF().trim().replaceAll("[^\\u0000-\\uFFFF]", "");
                        System.out.println("Data received from client socket: " + data);
                        try {
                            if (writeFile) {
                                if (data.equalsIgnoreCase("CMD:WRITE_CLOSE")) {
                                    writeFile = false;
                                } else {
                                    fileStore.writeUTF(newLine + data);
                                }

                            } else if (data.startsWith("CMD:OPEN|")) {
                                file = new File(data.split("\\|")[1]);
                                fileStore = new RandomAccessFile(file, "rw");
                                fileOpen = true;

                            } else if (data.startsWith("CMD:READ|")) {
                                if (fileOpen) {
                                    int offSet = Integer.parseInt(data.split("\\|")[1]);
                                    fileStore.seek(0);

                                    int seekCounter = 0;
                                    String line = "";
                                    while ((line = fileStore.readLine()) != null) {
                                        if (++seekCounter > offSet) {
                                            dataOutPutStream.writeUTF(line);
                                        }
                                    }
                                } else {
                                    dataOutPutStream.writeUTF("ERROR|FileNotOpenError");
                                }
                            } else if (data.trim().startsWith("CMD:WRITE")) {
                                if (fileOpen) {
                                    fileStore.seek(file.length());
                                    writeFile = true;
                                } else {
                                    dataOutPutStream.writeUTF("ERROR|FileNotOpenError");
                                }

                            } else if (data.equalsIgnoreCase("CMD:SOCK_CLOSE")) {
                                System.out.println("SOCK:CLOSE command has been received from client.");
                                break;

                            } else {
                                dataOutPutStream.writeUTF("FileWrite is false, Invalid command received:" + data);
                            }
                        } catch (Exception e) {
                            System.out.println("Exception came for data:" + data + ", " + e.toString());
                            dataOutPutStream.writeUTF("Exception came for data:" + data + ", " + e.toString());
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {

                if (socket != null) {
                    try {
                        System.out.println("Closing the client socket.");
                        socket.close();
                    } catch (IOException e) {
                    }
                }

                if (fileStore != null) {
                    try {
                        System.out.println("Closing the file.");
                        fileStore.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        FileServer fs = new FileServer(1988);
    }
}

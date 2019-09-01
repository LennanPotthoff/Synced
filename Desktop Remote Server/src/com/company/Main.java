package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import java.net.*;
import java.util.*;
@SuppressWarnings("ALL")
public class Main {
    static boolean useAudio = true;
    static boolean usePause = true;
    static String directory;
    static Boolean processed = false;
    static DatagramSocket ds;
    static int port;
    static ServerSocket serverSocketPublic;
    static Socket socketPublic;
    static InetAddress address;
    static ServerSocket transferServerSocket;
    static Socket transferSocket;
    static int packagesSent = 0;
    static Runnable uploadRunnable;
    static Thread uploadThread;

    public static void setPort(int portNumber) {
        port = portNumber;
    }

    public static void setDir(String msg) {
        directory = msg;
    }

    public static void setDatagramSocket(DatagramSocket socket) {
        ds = socket;
    }

    public static void setSocket(Socket socket) {
        socketPublic = socket;
    }

    public static void setServerSocket(ServerSocket serverSocket) {
        serverSocketPublic = serverSocket;
    }

    public static void uploadFile(String chosenFile) {
        try {
            System.out.println("Waiting for client...");
            transferServerSocket = new ServerSocket(8889);
            transferSocket = transferServerSocket.accept();
            System.out.println("Connected");
            OutputStream outputStream = transferSocket.getOutputStream();
            File file = new File(chosenFile);
            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            byte[] buffer = new byte[4194];
            int byteAmounts;
            while ((byteAmounts = bufferedInputStream.read(buffer)) > 0) {
                outputStream.write(buffer);
            }
            outputStream.flush();
            transferSocket.close();
            transferServerSocket.close();
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            try {
                transferSocket.close();
                transferServerSocket.close();
            } catch (Exception el) {
                System.out.println(el);
            }
            System.out.println(e);

        }

    }
        public static void main(String[] args) {

            if (SystemTray.isSupported()) {
                try {
                    String addressRaw = InetAddress.getLocalHost().toString();
                    int step = 1;
                    while (true) {
                        if (addressRaw.substring(0, step).contains("/")) {
                            addressRaw = addressRaw.substring(step, addressRaw.length());
                            PopupMenu popup = new PopupMenu();
                            TrayIcon trayIcon = new TrayIcon(new ImageIcon(Main.class.getResource("/resources/icon.png")).getImage()) {};;
                            SystemTray tray = SystemTray.getSystemTray();
                            trayIcon.setPopupMenu(popup);
                            tray.add(trayIcon);
                            MenuItem ipItem = new MenuItem("IP: " + addressRaw);
                            MenuItem exitItem = new MenuItem("Exit");

                            exitItem.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    System.exit(0);
                                }
                            });

                            popup.add(ipItem);
                            popup.add(exitItem);

                            break;
                        } else if (!addressRaw.substring(0, step).contains("/")) {
                            step++;
                        }
                    }
                } catch (Exception e) {
                    System.out.println("TrayIcon could not be added.");
                }

            }

            try {
                DatagramSocket ds = new DatagramSocket(8888);
                byte[] bytes = new byte[6];
                DatagramPacket dp = new DatagramPacket(bytes, bytes.length);
                System.out.println("Waiting for wake message...");
                ds.receive(dp);
                String message = new String(dp.getData());
                System.out.println(message);
                if (message.contains("hello?")) {
                    DatagramPacket dp2 = new DatagramPacket("hello!".getBytes(), "hello!".length(), dp.getAddress(), 9999);
                    ds.send(dp2);
                }
            } catch (Exception e) {
                System.out.println(e);
            }

            try (ServerSocket serverSocket = new ServerSocket(8888)) {
                Socket socket = serverSocket.accept();
                System.out.println("Connected");

                setSocket(socket);
                setServerSocket(serverSocket);

            } catch (Exception e) {
                System.out.println(e);
                System.exit(0);
            }

            String oldDir = "C:\\Users";
            System.out.println("Working Directory = " +
                    System.getProperty("user.dir"));
            HashMap<String, File> fileMap = new HashMap<>();
            ArrayList<String> fileArray = new ArrayList<>();

            while (true) {

                if (processed == false) {
                    try {
                        System.out.println("Waiting for Request");

                        InputStreamReader inputStreamReader = new InputStreamReader(socketPublic.getInputStream());
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        setDir(directory = bufferedReader.readLine());

                        processed = true;

                    } catch (Exception e) {
                        System.out.println(e);
                        System.exit(1);
                    }
                }

                if (processed == true) {
                    String dir = directory;
                    if (fileMap.containsKey(dir) == true) {

                        try {
                            File file = fileMap.get(dir);
                            if (file.isFile() == true) {

                                uploadRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        uploadFile(fileMap.get(dir).getAbsolutePath());
                                    }
                                };
                                uploadThread = new Thread(uploadRunnable);
                                uploadThread.start();
                            }
                            if (file.isDirectory() == true) {
                                oldDir = file.getPath();
                                File folder = new File(file.getPath());
                                File[] listOfFiles = folder.listFiles();
                                for (File files : listOfFiles) {
                                    if (files.isFile() && files.isHidden() == false) {
                                        fileMap.put(files.getName(), files);
                                        fileArray.add("File " + files.getName());
                                        //System.out.println("File " + files.getName());
                                    }
                                    if (files.isDirectory() && files.isHidden() == false) {
                                        fileMap.put(files.getName(), files);
                                        fileArray.add("Folder " + files.getName());
                                        //System.out.println("Folder " + files.getName());
                                    }
                                }

                                if (fileArray.size() == 0) {
                                    fileArray.add("File " + "This folder is empty.");
                                }

                                try {
                                    final String[] strings1 = fileArray.toArray(new String[0]);
                                    ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(socketPublic.getOutputStream()));
                                    out.writeObject(strings1);
                                    out.flush();
                                } catch (Exception e) {
                                    System.out.println(e);
                                    System.exit(13);
                                }


                                String subString = "";
                                int index = file.getPath().length() - 1;
                                while (subString.contains("\\") == false) {
                                    subString = file.getPath().substring(index);
                                    index--;
                                }
                                System.out.println(oldDir.replace(subString.substring(0), ""));

                            }

                        } catch (Exception e) {
                            System.out.println(e);
                            System.exit(8);
                        }

                        fileArray.clear();

                        processed = false;
                        continue;
                    }

                    if (directory.contains("back") != true) {

                        fileMap.clear();
                        oldDir = directory;
                        File folder = new File(directory);
                        File[] listOfFiles = folder.listFiles();
                        for (File file : listOfFiles) {
                            if (file.isFile() && file.isHidden() == false) {
                                //System.out.println(file.getName() + "(File)");
                                fileMap.put(file.getName(), file);
                                fileArray.add("File " + file.getName());
                            }

                            if (file.isDirectory() && file.isHidden() == false) {
                                //System.out.println(file.getName() + "(Folder)");
                                fileMap.put(file.getName(), file);
                                fileArray.add("Folder " + file.getName());
                            }
                        }

                        try {
                            final String[] strings1 = fileArray.toArray(new String[0]);
                            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(socketPublic.getOutputStream()));
                            out.writeObject(strings1);
                            out.flush();
                        } catch (Exception e) {
                            System.out.println(e);
                            System.exit(13);
                        }

                        fileArray.clear();
                        processed = false;
                        continue;

                    }

                    if (dir.contains("back") == true) {


                        //fileMap.clear();

                        if (!oldDir.equals("C:\\Users")) {
                            System.out.println("C:\\Users");

                            String subString = "";
                            try {
                                System.out.println(oldDir + " Before Cut");
                                int index = oldDir.length() - 1;
                                while (subString.contains("\\") == false) {
                                    subString = oldDir.substring(index);
                                    index--;
                                }
                            } catch (StringIndexOutOfBoundsException e) {
                                System.out.println(e);
                            }
                            oldDir = oldDir.replace(subString.substring(0), "");
                            System.out.println(oldDir.replace(subString.substring(0), "") + " After cut!");
                            File folder = new File(oldDir);
                            File[] listOfFiles = folder.listFiles();
                            for (File file : listOfFiles) {
                                if (file.isFile() && file.isHidden() == false) {
                                    fileMap.put(file.getName(), file);
                                    fileArray.add("File " + file.getName());
                                }
                                if (file.isDirectory() && file.isHidden() == false) {
                                    fileMap.put(file.getName(), file);
                                    fileArray.add("Folder " + file.getName());
                                }
                            }
                            try {
                                final String[] strings1 = fileArray.toArray(new String[0]);
                                ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(socketPublic.getOutputStream()));
                                out.writeObject(strings1);
                                out.flush();
                            } catch (Exception e) {
                                System.out.println(e);
                                System.exit(13);
                            }
                        } else if (oldDir.equals("C:\\Users")) {

                            oldDir = "C:\\Users";
                            File folder = new File("C:\\Users");
                            File[] listOfFiles = folder.listFiles();
                            for (File file : listOfFiles) {
                                if (file.isFile() && file.isHidden() == false) {
                                    fileMap.put(file.getName(), file);
                                    fileArray.add("File " + file.getName());
                                }
                                if (file.isDirectory() && file.isHidden() == false) {
                                    fileMap.put(file.getName(), file);
                                    fileArray.add("Folder " + file.getName());
                                }
                            }
                            System.out.println(fileArray.size() + " Size!");
                            try {
                                final String[] strings1 = fileArray.toArray(new String[0]);

                                ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(socketPublic.getOutputStream()));
                                out.writeObject(strings1);
                                out.flush();
                            } catch (Exception e) {
                                System.out.println(e);
                                System.exit(13);
                            }
                        }

                        fileArray.clear();

                        processed = false;
                        continue;

                    }
                    processed = false;
                    continue;
                }


            }
        }
}


import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
public class Main {
    public static void main(String[] args)
            throws Exception {
        InetAddress host = InetAddress.getByName("localhost");
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel =
                ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(host, 1234));
        serverSocketChannel.register(selector, SelectionKey.
                OP_ACCEPT);
        SelectionKey key = null;
        int i=0;
        while (true) {
            if (selector.select() <= 0)
                continue;
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();
            while (iterator.hasNext()) {
                key = (SelectionKey) iterator.next();
                iterator.remove();
            }
                if (key.isAcceptable()) {
                    SocketChannel sc = serverSocketChannel.accept();
                    sc.configureBlocking(false);
                    sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                    System.out.println("Connection Accepted: "
                            + sc.getLocalAddress() + "n");
                }
                if (key.isReadable()) {
                    SocketChannel sc = (SocketChannel) key.channel();
                    ByteBuffer bb = ByteBuffer.allocate(1024);
                    sc.read(bb);
                    String result = new String(bb.array()).trim();
                    System.out.println("Message received: "
                            + result
                            + " Message length= " + result.length());

                    if (result.length() <= 0) {
                        sc.close();
                        System.out.println("Connection closed...");
                        System.out.println(
                                "Server will keep running. " +
                                        "Try running another client to " +
                                        "re-establish connection");
                    }
                }
                if (key.isWritable()){
                    SocketChannel sc = (SocketChannel) key.channel();
                    ByteBuffer b1 = ByteBuffer.wrap("sssssss".getBytes());
                    sc.write(b1);
                    sc.close();
                    i++;
                    System.out.printf("Сообщение отправлено %d\n", i);
                }
            }
        }
    }

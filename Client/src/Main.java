import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
public class Main {
    private static BufferedReader input = null;
    public static void main(String[] args) throws Exception {
        InetSocketAddress addr = new InetSocketAddress(
                InetAddress.getByName("localhost"), 1234);
        Selector selector = Selector.open();
        SocketChannel sc = SocketChannel.open();
        sc.configureBlocking(false);
        sc.connect(addr);
        sc.register(selector, SelectionKey.OP_CONNECT |
                SelectionKey.OP_READ | SelectionKey.
                OP_WRITE);
        input = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            if (selector.select() > 0) {
                Boolean doneStatus = processReadySet
                        (selector.selectedKeys());
                if (doneStatus) {
                    break;
                }
            }
        }
        sc.close();
    }

    public static Boolean processReadySet(Set readySet)
            throws Exception {
        SelectionKey key = null;
        Iterator iterator = null;
        iterator = readySet.iterator();
        while (iterator.hasNext()) {
            key = (SelectionKey) iterator.next();
            iterator.remove();
        }
        if (key.isConnectable()) {
            Boolean connected = processConnect(key);
            if (!connected) {
                return true;
            }
        }
        if (key.isReadable()) {
            SocketChannel sc = (SocketChannel) key.channel();
            ByteBuffer bb = ByteBuffer.allocate(1024);
            sc.read(bb);
            String result = new String(bb.array()).trim();
            System.out.println("Message received from Server: " + result + " Message length= "
                    + result.length());
        }
        if (key.isWritable()) {
            System.out.print("Type a message (type quit to stop): ");
            String msg = input.readLine();
            if (msg.equalsIgnoreCase("quit")) {
                return true;
            }

            if (msg.equalsIgnoreCase("command1")) {
                Command1 command1 = new Command1();
                ByteArrayOutputStream str = new ByteArrayOutputStream();
                ObjectOutputStream obj = new ObjectOutputStream(str);
                obj.writeObject(command1);
                String serCommand1 = Base64.getEncoder().encodeToString(str.toByteArray());
                SocketChannel sc = (SocketChannel) key.channel();
                ByteBuffer bb = ByteBuffer.wrap(serCommand1.getBytes());
                sc.write(bb);
                return false;
            }
            SocketChannel sc = (SocketChannel) key.channel();
            ByteBuffer bb = ByteBuffer.wrap(msg.getBytes());
            sc.write(bb);
        }
        return false;
    }
    public static Boolean processConnect(SelectionKey key) {
        SocketChannel sc = (SocketChannel) key.channel();
        try {
            while (sc.isConnectionPending()) {
                sc.finishConnect();
            }
        } catch (IOException e) {
            key.cancel();
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
package ballistickemu;
 
import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.net.InetSocketAddress;
 
import ballistickemu.Tools.StickNetworkHandler;
import ballistickemu.Lobby.LobbyServer;
import ballistickemu.Tools.DatabaseTools;
import ballistickemu.Tools.QuickplayTool;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.apache.mina.filter.executor.ExecutorFilter;
 
/**
 *
 * @author Simon
 */
public class Main {
    public static String IP = "";
    private static int PORT = 1138;
    public static int maxPlayers = 100; // 100 default, but configureable via props
    private static LobbyServer LS;
 
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Welcome to BallistickEMU - Improved by andre_jar");
        NioSocketAcceptor SocketAcceptor = new NioSocketAcceptor();
        SocketAcceptor.getSessionConfig().setIdleTime( IdleStatus.BOTH_IDLE, 10 );
        SocketAcceptor.getSessionConfig().setReadBufferSize( 2048 );
        SocketAcceptor.setHandler(new StickNetworkHandler());
        ExecutorFilter executor = new ExecutorFilter();
        SocketAcceptor.getFilterChain().addLast( "codec", new ProtocolCodecFilter( new TextLineCodecFactory( Charset.forName( "UTF-8" ), "\0", "\0")));
        SocketAcceptor.getFilterChain().addLast("threadPool", executor);
        LS = new LobbyServer();
 
        Properties ConfigProps = new Properties();
        try {
        ConfigProps.load(new FileInputStream("config.properties"));
        } catch (FileNotFoundException fnf)
        {
            System.out.println("Unable to start server: config.properties was not found.");
            return;
        } catch (IOException e)
        {
            System.out.println("Unable to start server: Error reading from config.properties.");
            return;
        }
 
        Main.IP = ConfigProps.getProperty("server_IP");
        Main.PORT = Integer.parseInt(ConfigProps.getProperty("server_Port"));
        Main.maxPlayers = Integer.parseInt(ConfigProps.getProperty("server_maxPlayers"));
        DatabaseTools.user = ConfigProps.getProperty("db_user");
        DatabaseTools.pass = ConfigProps.getProperty("db_pass");
        DatabaseTools.server = ConfigProps.getProperty("db_server");
        DatabaseTools.database = ConfigProps.getProperty("db_database");
 
        DatabaseTools.dbConnect();
 
        if (!LS.getShop().PopulateShop())
        {
            System.out.println("There was an error reading shop info from the database.");
            return;
        }
 
        if (!QuickplayTool.PopulateNameList())
        {
            System.out.println("There was an error reading quickplay names from the database.");
            return;
        }
 
        try
        {
            SocketAcceptor.bind(new InetSocketAddress(PORT));
 
            System.out.printf("Server started on port %s \n", PORT);
            System.out.println();
        }
 
        catch (IOException e)
        {
            System.out.printf("Unable to bind to port %s, Exception thrown: %s \n", PORT, e);
            System.out.println();
        }
    }
 
    public static LobbyServer getLobbyServer()
    {
        return LS;
    }
 
}

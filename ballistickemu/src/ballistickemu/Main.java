package ballistickemu;
 
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ballistickemu.Lobby.LobbyServer;
import ballistickemu.Tools.DatabaseTools;
import ballistickemu.Tools.QuickplayTool;
import ballistickemu.Tools.StickNetworkHandler;
 
/**
 *
 * @author Simon
 */
public class Main {
    public static String IP = "";
    private static int PORT = 1138;
    public static int maxPlayers = 100; // 100 default, but configureable via props
    private static LobbyServer LS;
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
	private static boolean chatLogEnabled = false;	
	private static boolean isPromptEnabled = false;
 
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) { 
    	LoggerContext context = (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
    	File file = new File("log4j2.xml");
    	// this will force a reconfiguration
    	context.setConfigLocation(file.toURI());
    	Properties ConfigProps = new Properties();
        try {
        ConfigProps.load(new FileInputStream("config.properties"));
        } catch (FileNotFoundException fnf)
        {
            LOGGER.error("Unable to start server: config.properties was not found.");
            return;
        } catch (IOException e)
        {
            LOGGER.error("Unable to start server: Error reading from config.properties.");
            return;
        }
		if ("true".equalsIgnoreCase(ConfigProps.getProperty("logchat"))) {
			chatLogEnabled = true;
		}
        LOGGER.info("Welcome to BallistickEMU - Improved by andre_jar");
        NioSocketAcceptor SocketAcceptor = new NioSocketAcceptor();
        SocketAcceptor.getSessionConfig().setIdleTime( IdleStatus.BOTH_IDLE, 10 );
        SocketAcceptor.getSessionConfig().setReadBufferSize( 2048 );
        SocketAcceptor.setHandler(new StickNetworkHandler());
        ExecutorFilter executor = new ExecutorFilter();
        SocketAcceptor.getFilterChain().addLast( "codec", new ProtocolCodecFilter( new TextLineCodecFactory( Charset.forName( "UTF-8" ), "\0", "\0")));
        SocketAcceptor.getFilterChain().addLast("threadPool", executor);
        LS = new LobbyServer();
       
 
        Main.IP = ConfigProps.getProperty("server_IP");
        Main.PORT = Integer.parseInt(ConfigProps.getProperty("server_Port"));
        Main.maxPlayers = Integer.parseInt(ConfigProps.getProperty("server_maxPlayers"));
        DatabaseTools.user = ConfigProps.getProperty("db_user");
        DatabaseTools.pass = ConfigProps.getProperty("db_pass");
        DatabaseTools.server = ConfigProps.getProperty("db_server");
        DatabaseTools.database = ConfigProps.getProperty("db_database");
		String enablePrompt = ConfigProps.getProperty("enable_prompt");
		if ("true".equalsIgnoreCase(enablePrompt)) {
			isPromptEnabled = true;
			Thread t = new Thread(new Runnable() {
				Scanner scanner = new Scanner(System.in);

				@Override
				public void run() {
					while (true) {
						if (scanner.hasNext()) {
							ConsoleCommandHandler.handle(scanner.nextLine());
						}
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							LOGGER.warn("There was an exception waiting for scanner thread: ", e);
						}
					}
				}

			});
			t.start();
		}
        
        DatabaseTools.dbConnect();
 
        if (!LS.getShop().PopulateShop())
        {
            LOGGER.error("There was an error reading shop info from the database.");
            return;
        }
 
        if (!QuickplayTool.PopulateNameList())
        {
            LOGGER.error("There was an error reading quickplay names from the database.");
            return;
        }
 
        try
        {
            SocketAcceptor.bind(new InetSocketAddress(PORT));
 
            LOGGER.info("Server started on port {} \n", PORT);
        }
 
        catch (IOException e)
        {
            LOGGER.error("Unable to bind to port {}, Exception thrown: {} \n", PORT, e);
        }
    }
    
	public static boolean isChatLogEnabled() {
		return chatLogEnabled;
	}
 
    public static LobbyServer getLobbyServer()
    {
        return LS;
    }
	public static boolean isPromptEnabled() {
		return isPromptEnabled;
	}
	public static void setChatLogEnabled(boolean enabled) {
		chatLogEnabled = enabled;
	}
}

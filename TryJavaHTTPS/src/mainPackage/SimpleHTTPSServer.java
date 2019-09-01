package mainPackage;

import com.sun.net.httpserver.*;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;

public class SimpleHTTPSServer {

    public static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "This is the response";
            HttpsExchange httpsExchange = (HttpsExchange) t;
            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {

        try {
            // setup the socket address
            InetSocketAddress address = new InetSocketAddress(8000);

            // initialise the HTTPS server
            HttpsServer httpsServer = HttpsServer.create(address, 0);
            SSLContext sslContext = SSLContext.getInstance("TLS");

            // initialise the keystore
            char[] password = "password".toCharArray();
            KeyStore ks = KeyStore.getInstance("JKS");
            FileInputStream fis = new FileInputStream("testkey.jks");
            ks.load(fis, password);

            // setup the key manager factory
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, password);

            // setup the trust manager factory
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);

            // setup the HTTPS context and parameters
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {
                    try {
                        // initialise the SSL context
                        SSLContext context = getSSLContext();
                        SSLEngine engine = context.createSSLEngine();
                        params.setNeedClientAuth(false);
                        params.setCipherSuites(engine.getEnabledCipherSuites());
                        params.setProtocols(engine.getEnabledProtocols());

                        // Set the SSL parameters
                        SSLParameters sslParameters = context.getSupportedSSLParameters();
                        params.setSSLParameters(sslParameters);

                    } catch (Exception ex) {
                        System.out.println("Failed to create HTTPS port");
                    }
                }
            });
            httpsServer.createContext("/test", new MyHandler());
            httpsServer.setExecutor(null); // creates a default executor
            httpsServer.start();

        } catch (Exception exception) {
            System.out.println("Failed to create HTTPS server on port " + 8000 + " of localhost");
            exception.printStackTrace();

        }
    }

}

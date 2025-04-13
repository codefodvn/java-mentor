package codefod.com.networkingdemo.basicwebserver.https;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

/*
Bước 1: Trích xuất certificate từ server.jks
> keytool -exportcert -alias myserver -keystore server.jks -file server.crt -storepass password

Bước 2: Import server.crt vào TrustStore riêng (để client tin tưởng)
> keytool -importcert -alias myserver -file server.crt \
 -keystore truststore.jks -storepass password -noprompt

* */

public class VerifyServerCertificate {
    public static void main(String[] args) throws Exception {
        String host = "localhost";
        int port = 8443;

        // 1. Load TrustStore chứa chứng chỉ server
        KeyStore trustStore = KeyStore.getInstance("JKS");
        try (InputStream ts = new FileInputStream("truststore.jks")) {
            trustStore.load(ts, "password".toCharArray());
        }

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(trustStore);

        // 2. Tạo SSLContext từ truststore
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);

        // 3. Tạo SSLSocket kết nối tới server
        SSLSocketFactory factory = sslContext.getSocketFactory();
        SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
        socket.startHandshake(); // thực hiện bắt tay TLS

        // 4. Lấy thông tin chứng chỉ từ session
        SSLSession session = socket.getSession();
        System.out.println("🔐 TLS version: " + session.getProtocol());
        System.out.println("🔒 Cipher suite: " + session.getCipherSuite());

        System.out.println("📄 Certificate chain:");
        for (java.security.cert.Certificate cert : session.getPeerCertificates()) {
            if (cert instanceof X509Certificate x509Cert) {
                System.out.println("---------------------------------------------------");
                System.out.println("Subject: " + x509Cert.getSubjectDN());
                System.out.println("Issuer:  " + x509Cert.getIssuerDN());
                System.out.println("Valid from: " + x509Cert.getNotBefore());
                System.out.println("Valid until: " + x509Cert.getNotAfter());
                System.out.println("Serial number: " + x509Cert.getSerialNumber());
            }
        }

        socket.close();
        System.out.println("✅ Chứng chỉ đã được xác thực thành công.");
    }
}


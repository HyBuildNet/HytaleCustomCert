package net.hybuild.customcert;

import com.hypixel.hytale.plugin.early.ClassTransformer;
import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Early Plugin ClassTransformer that patches certificate-related classes at load time.
 *
 * Configuration: earlyplugins/CustomCert.json
 * {
 *   "privateKeyPath": "certificates/server.key",
 *   "publicKeyPath": "certificates/server.crt",
 *   "experimentalBypassClientCertificateBinding": true
 * }
 */
public class CertificatePatchTransformer implements ClassTransformer {

    private static final String CERT_UTIL = "com.hypixel.hytale.server.core.auth.CertificateUtil";
    private static final String QUIC_TRANSPORT = "com.hypixel.hytale.server.core.io.transport.QUICTransport";

    private final ClassPool classPool;

    // Config with defaults
    private String privateKeyPath = "certificates/server.key";
    private String publicKeyPath = "certificates/server.crt";
    private boolean bypassClientCertificateBinding = false;

    public CertificatePatchTransformer() {
        this.classPool = ClassPool.getDefault();
        loadConfig();
    }

    private void loadConfig() {
        File configFile = new File("earlyplugins/CustomCert.json");

        if (!configFile.exists()) {
            createDefaultConfig(configFile);
        }

        System.out.println("[CustomCert] Loading config from " + configFile.getAbsolutePath());
        try (FileReader reader = new FileReader(configFile)) {
            com.google.gson.JsonObject json = com.google.gson.JsonParser.parseReader(reader).getAsJsonObject();

            if (json.has("privateKeyPath")) {
                privateKeyPath = json.get("privateKeyPath").getAsString();
            }
            if (json.has("publicKeyPath")) {
                publicKeyPath = json.get("publicKeyPath").getAsString();
            }
            if (json.has("experimental")) {
                com.google.gson.JsonObject experimental = json.getAsJsonObject("experimental");
                if (experimental.has("bypassClientCertificateBinding")) {
                    bypassClientCertificateBinding = experimental.get("bypassClientCertificateBinding").getAsBoolean();
                }
            }

            System.out.println("[CustomCert] Config loaded:");
            System.out.println("[CustomCert]   privateKeyPath: " + privateKeyPath);
            System.out.println("[CustomCert]   publicKeyPath: " + publicKeyPath);
            System.out.println("[CustomCert]   experimental.bypassClientCertificateBinding: " + bypassClientCertificateBinding);

        } catch (Exception e) {
            System.err.println("[CustomCert] Failed to load config: " + e.getMessage());
            System.out.println("[CustomCert] Using defaults");
        }
    }

    private void createDefaultConfig(File configFile) {
        try {
            if (configFile.getParentFile() != null && !configFile.getParentFile().exists()) {
                configFile.getParentFile().mkdirs();
            }

            com.google.gson.JsonObject experimental = new com.google.gson.JsonObject();
            experimental.addProperty("bypassClientCertificateBinding", bypassClientCertificateBinding);

            com.google.gson.JsonObject json = new com.google.gson.JsonObject();
            json.addProperty("privateKeyPath", privateKeyPath);
            json.addProperty("publicKeyPath", publicKeyPath);
            json.add("experimental", experimental);

            com.google.gson.Gson gson = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
            try (FileWriter writer = new FileWriter(configFile)) {
                gson.toJson(json, writer);
            }

            System.out.println("[CustomCert] Created default config at " + configFile.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("[CustomCert] Failed to create default config: " + e.getMessage());
        }
    }

    @Override
    public byte[] transform(String name, String path, byte[] bytes) {
        try {
            if (CERT_UTIL.equals(name) && bypassClientCertificateBinding) {
                System.out.println("[CustomCert] Found CertificateUtil, patching...");
                return patchCertificateUtil(bytes);
            }
            if (QUIC_TRANSPORT.equals(name)) {
                System.out.println("[CustomCert] Found QUICTransport, patching...");
                return patchQUICTransport(bytes);
            }
        } catch (Exception e) {
            System.err.println("[CustomCert] ERROR patching " + name + ": " + e.getMessage());
            e.printStackTrace();
        }
        return bytes;
    }

    /**
     * Patches CertificateUtil.validateCertificateBinding() to always return true.
     */
    private byte[] patchCertificateUtil(byte[] bytes) throws Exception {
        System.out.println("[CustomCert] Patching CertificateUtil...");

        CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(bytes));
        CtMethod method = ctClass.getDeclaredMethod("validateCertificateBinding");
        method.setBody(buildValidationBypassCode());

        byte[] result = ctClass.toBytecode();
        ctClass.detach();

        System.out.println("[CustomCert] CertificateUtil patched successfully");
        return result;
    }

    /**
     * Patches QUICTransport to load certificates from disk.
     */
    private byte[] patchQUICTransport(byte[] bytes) throws Exception {
        System.out.println("[CustomCert] Patching QUICTransport...");

        CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(bytes));

        // Add STATIC fields to store loaded certificate (avoids 'this' before super() issue)
        ctClass.addField(CtField.make(
            "private static java.security.PrivateKey customPrivateKey;", ctClass));
        ctClass.addField(CtField.make(
            "private static java.security.cert.X509Certificate customCertificate;", ctClass));

        // Get constructor and inject certificate loading
        CtConstructor constructor = ctClass.getDeclaredConstructor(new CtClass[]{});
        constructor.insertBefore(buildCertificateLoadingCode());

        // Replace SelfSignedCertificate.key() and .cert() calls
        constructor.instrument(new ExprEditor() {
            @Override
            public void edit(NewExpr e) throws CannotCompileException {
                if (e.getClassName().equals("io.netty.handler.ssl.util.SelfSignedCertificate")) {
                    System.out.println("[CustomCert] Skipping SelfSignedCertificate instantiation");
                    e.replace("{ $_ = null; }");
                }
            }

            @Override
            public void edit(MethodCall m) throws CannotCompileException {
                try {
                    String className = m.getClassName();
                    String methodName = m.getMethodName();
                    if (className != null && className.equals("io.netty.handler.ssl.util.SelfSignedCertificate")) {
                        if ("key".equals(methodName)) {
                            System.out.println("[CustomCert] Replacing .key() call");
                            m.replace("{ $_ = " + QUIC_TRANSPORT + ".customPrivateKey; }");
                        }
                        if ("cert".equals(methodName)) {
                            System.out.println("[CustomCert] Replacing .cert() call");
                            m.replace("{ $_ = " + QUIC_TRANSPORT + ".customCertificate; }");
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("[CustomCert] Warning: Could not analyze method call: " + ex.getMessage());
                }
            }
        });

        byte[] result = ctClass.toBytecode();
        ctClass.detach();

        System.out.println("[CustomCert] QUICTransport patched successfully");
        return result;
    }

    /**
     * Builds the code that bypasses certificate validation.
     */
    private String buildValidationBypassCode() {
        StringBuilder code = new StringBuilder();
        code.append("{");
        code.append("  System.out.println(\"[CustomCert] Certificate binding validation bypassed\");");
        code.append("  return true;");
        code.append("}");
        return code.toString();
    }

    /**
     * Builds the code that loads or generates certificates.
     * Uses java.io.File instead of java.nio.file to avoid Javassist varargs issues.
     * Uses try-finally for proper resource cleanup.
     */
    private String buildCertificateLoadingCode() {
        String escapedKeyPath = privateKeyPath.replace("\\", "\\\\").replace("\"", "\\\"");
        String escapedCertPath = publicKeyPath.replace("\\", "\\\\").replace("\"", "\\\"");

        StringBuilder code = new StringBuilder();

        code.append("{");
        code.append("  java.io.File keyFile = new java.io.File(\"").append(escapedKeyPath).append("\");");
        code.append("  java.io.File certFile = new java.io.File(\"").append(escapedCertPath).append("\");");

        code.append("  try {");
        code.append("    if (keyFile.exists() && certFile.exists()) {");
        code.append("      System.out.println(\"[CustomCert] Loading certificates:\");");
        code.append("      System.out.println(\"[CustomCert]   Private key: \" + keyFile.getAbsolutePath());");
        code.append("      System.out.println(\"[CustomCert]   Public cert: \" + certFile.getAbsolutePath());");

        // Load private key with try-finally
        code.append("      org.bouncycastle.openssl.PEMParser keyParser = null;");
        code.append("      try {");
        code.append("        keyParser = new org.bouncycastle.openssl.PEMParser(new java.io.FileReader(keyFile));");
        code.append("        Object keyObj = keyParser.readObject();");
        code.append("        org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter keyConv = ");
        code.append("          new org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter();");
        code.append("        if (keyObj instanceof org.bouncycastle.openssl.PEMKeyPair) {");
        code.append("          com.hypixel.hytale.server.core.io.transport.QUICTransport.customPrivateKey = keyConv.getPrivateKey(");
        code.append("            ((org.bouncycastle.openssl.PEMKeyPair)keyObj).getPrivateKeyInfo());");
        code.append("        } else {");
        code.append("          com.hypixel.hytale.server.core.io.transport.QUICTransport.customPrivateKey = keyConv.getPrivateKey(");
        code.append("            (org.bouncycastle.asn1.pkcs.PrivateKeyInfo)keyObj);");
        code.append("        }");
        code.append("      } finally { if (keyParser != null) keyParser.close(); }");

        // Load certificate with try-finally
        code.append("      org.bouncycastle.openssl.PEMParser certParser = null;");
        code.append("      try {");
        code.append("        certParser = new org.bouncycastle.openssl.PEMParser(new java.io.FileReader(certFile));");
        code.append("        Object certObj = certParser.readObject();");
        code.append("        com.hypixel.hytale.server.core.io.transport.QUICTransport.customCertificate = ");
        code.append("          new org.bouncycastle.cert.jcajce.JcaX509CertificateConverter()");
        code.append("            .getCertificate((org.bouncycastle.cert.X509CertificateHolder)certObj);");
        code.append("      } finally { if (certParser != null) certParser.close(); }");

        code.append("      System.out.println(\"[CustomCert] Loaded certificate: \" + ");
        code.append("        com.hypixel.hytale.server.core.io.transport.QUICTransport.customCertificate.getSubjectX500Principal().getName());");

        code.append("    } else {");

        // Generate new certificate
        code.append("      System.out.println(\"[CustomCert] Certificate files not found, generating new ones...\");");
        code.append("      io.netty.handler.ssl.util.SelfSignedCertificate ssc = ");
        code.append("        new io.netty.handler.ssl.util.SelfSignedCertificate(\"localhost\");");
        code.append("      com.hypixel.hytale.server.core.io.transport.QUICTransport.customPrivateKey = ssc.key();");
        code.append("      com.hypixel.hytale.server.core.io.transport.QUICTransport.customCertificate = ssc.cert();");

        // Create parent directories
        code.append("      if (keyFile.getParentFile() != null && !keyFile.getParentFile().exists()) {");
        code.append("        keyFile.getParentFile().mkdirs();");
        code.append("      }");
        code.append("      if (certFile.getParentFile() != null && !certFile.getParentFile().exists()) {");
        code.append("        certFile.getParentFile().mkdirs();");
        code.append("      }");

        // Save private key with try-finally
        code.append("      org.bouncycastle.openssl.jcajce.JcaPEMWriter keyWriter = null;");
        code.append("      try {");
        code.append("        keyWriter = new org.bouncycastle.openssl.jcajce.JcaPEMWriter(new java.io.FileWriter(keyFile));");
        code.append("        keyWriter.writeObject(com.hypixel.hytale.server.core.io.transport.QUICTransport.customPrivateKey);");
        code.append("      } finally { if (keyWriter != null) keyWriter.close(); }");

        // Save certificate with try-finally
        code.append("      org.bouncycastle.openssl.jcajce.JcaPEMWriter certWriter = null;");
        code.append("      try {");
        code.append("        certWriter = new org.bouncycastle.openssl.jcajce.JcaPEMWriter(new java.io.FileWriter(certFile));");
        code.append("        certWriter.writeObject(com.hypixel.hytale.server.core.io.transport.QUICTransport.customCertificate);");
        code.append("      } finally { if (certWriter != null) certWriter.close(); }");

        code.append("      System.out.println(\"[CustomCert] Saved certificates:\");");
        code.append("      System.out.println(\"[CustomCert]   Private key: \" + keyFile.getAbsolutePath());");
        code.append("      System.out.println(\"[CustomCert]   Public cert: \" + certFile.getAbsolutePath());");

        code.append("    }");

        // Exception handling - fallback to in-memory
        code.append("  } catch (Exception e) {");
        code.append("    System.err.println(\"[CustomCert] Failed to load/save certificate: \" + e.getMessage());");
        code.append("    e.printStackTrace();");
        code.append("    try {");
        code.append("      io.netty.handler.ssl.util.SelfSignedCertificate ssc = ");
        code.append("        new io.netty.handler.ssl.util.SelfSignedCertificate(\"localhost\");");
        code.append("      com.hypixel.hytale.server.core.io.transport.QUICTransport.customPrivateKey = ssc.key();");
        code.append("      com.hypixel.hytale.server.core.io.transport.QUICTransport.customCertificate = ssc.cert();");
        code.append("      System.out.println(\"[CustomCert] Using fallback in-memory certificate\");");
        code.append("    } catch (Exception e2) {");
        code.append("      throw new RuntimeException(\"[CustomCert] Failed to create fallback certificate\", e2);");
        code.append("    }");
        code.append("  }");

        code.append("}");

        return code.toString();
    }

    @Override
    public int priority() {
        return 100;
    }
}

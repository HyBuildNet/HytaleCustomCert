# CustomCert - Hytale Early Plugin

## Purpose

Enables running a Hytale server behind a reverse proxy by patching two classes at load time:

1. **CertificateUtil** - Bypasses certificate binding validation
2. **QUICTransport** - Loads custom TLS certificates from configurable paths

### Why is this needed?

Hytale uses QUIC with client certificates. The server generates a new self-signed cert on every startup and validates that client certs are "bound" to it - making proxies impossible. This plugin:
- Skips certificate binding validation (configurable)
- Loads persistent certificates from disk
- Auto-generates and saves certs on first startup

## Configuration

Config file: `earlyplugins/CustomCert.json` (auto-created on first start)

```json
{
  "privateKeyPath": "certificates/server.key",
  "publicKeyPath": "certificates/server.crt",
  "experimental": {
    "bypassClientCertificateBinding": false
  }
}
```

| Option | Default | Description |
|--------|---------|-------------|
| `privateKeyPath` | `certificates/server.key` | Path to private key (PEM) |
| `publicKeyPath` | `certificates/server.crt` | Path to certificate (PEM) |
| `experimental.bypassClientCertificateBinding` | `false` | Bypass client cert binding validation |

## How it Works

This plugin uses **Javassist** for runtime bytecode manipulation. No Hytale code is distributed - only transformation instructions.

**CertificateUtil Patch:**
```java
// validateCertificateBinding() is modified to always return true
```

**QUICTransport Patch:**
```java
// Constructor is modified to load/save certificates from disk
// instead of generating a new SelfSignedCertificate every time
```

## Project Structure

```
CustomCert/
├── src/net/hybuild/customcert/
│   └── CertificatePatchTransformer.java   # Bytecode transformer (Javassist)
├── resources/META-INF/services/
│   └── com.hypixel.hytale.plugin.early.ClassTransformer
├── lib/
│   ├── javassist.jar                      # Included in repo
│   └── HytaleServer.jar                   # NOT included (add manually)
├── build.sh
└── clean.sh
```

## Build & Deploy

```bash
./build.sh    # Compiles and creates build/latest/CustomCert.jar (fat-jar with Javassist)
./clean.sh    # Removes all build artifacts
```

**Requirements:**
- `lib/HytaleServer.jar` - Place manually (not distributed)
- `lib/javassist.jar` - Included in repo

On the server:
1. Place `CustomCert.jar` in the `earlyplugins/` folder
2. Optionally create `earlyplugins/CustomCert.json` for custom paths
3. Start with `java -jar HytaleServer.jar --allow-early-plugins`

## Certificate Requirements

Format: **PEM-encoded X.509**

```
certificates/
├── server.key    # Private key (PKCS#8 or PKCS#1 PEM)
└── server.crt    # X.509 certificate (PEM)
```

Generate manually:
```bash
openssl req -x509 -newkey rsa:2048 -keyout server.key -out server.crt -days 365 -nodes
```

Or let the plugin auto-generate on first startup.

## Update Workflow

For each new Hytale version:

```bash
./clean.sh
rm lib/HytaleServer.jar
cp /path/to/new/HytaleServer.jar lib/
./build.sh
```

## Troubleshooting

| Problem | Solution |
|---------|----------|
| Build fails | Ensure HytaleServer.jar is in lib/ |
| Plugin not loaded | Check --allow-early-plugins flag |
| Certificates not loading | Check file permissions and PEM format |
| Config not loading | Ensure valid JSON in earlyplugins/CustomCert.json |

## Legal Note

This plugin distributes only:
- Our own transformation code (MIT licensed)
- Javassist library (Apache/MPL licensed)

No Hytale binaries or decompiled code is included.

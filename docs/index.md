# CustomCert

Hytale Plugin for custom/persistent TLS certificates (+ reverse proxy support). It uses Hytales special `Early Plugins` to modify the certificate-loading process before the actual server boots up.

The plugin works with both 'Online' and 'Offline' mode. In order to put your server behind a reverse proxy you have to set `bypassClientCertificateBinding` to `true`. This will prevent the server from checking the client certificate (which in a reverse-proxy scenario has a different key than the Hytale installation on your computer) - otherwise the connection will fail. Make sure that the server and the proxy use the same certificate.

## Download

[CustomCert.jar](https://raw.githubusercontent.com/HyBuildNet/HytaleCustomCert/main/build/latest/CustomCert.jar)

## Installation

1. Place `CustomCert.jar` in the `earlyplugins/` folder
2. Start the server with the early plugins flag:

```bash
java -jar HytaleServer.jar --allow-early-plugins
```

On first startup, the plugin generates certificates in `certificates/` and creates a config file at `earlyplugins/CustomCert.json`.

## What it does

Hytale uses QUIC with client certificates. The server generates a new self-signed certificate on every startup, which breaks reverse proxy setups. This plugin:

- Loads certificates from disk instead of generating new ones
- Saves auto-generated certificates for reuse
- Optionally bypasses client certificate binding validation

Can be used with any QUIC-compatible reverse proxy, such as [quic-relay](https://github.com/HyBuildNet/quic-relay).

## Building from source

Requires `lib/HytaleServer.jar` (not distributed).

### Using Gradle

```bash
./gradlew build
```

Output: `build/latest/CustomCert.jar`

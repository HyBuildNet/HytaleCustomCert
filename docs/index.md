# CustomCert

Hytale Early Plugin that enables persistent TLS certificates and reverse proxy support.

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

## Building from source

Requires `lib/HytaleServer.jar` (not distributed).

```bash
./build.sh
```

Output: `build/latest/CustomCert.jar`

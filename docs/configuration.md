# Configuration

Config file: `earlyplugins/CustomCert.json`

Created automatically on first startup with default values.

```json
{
  "privateKeyPath": "certificates/server.key",
  "publicKeyPath": "certificates/server.crt",
  "experimental": {
    "bypassClientCertificateBinding": false
  }
}
```

## Options

| Option | Default | Description |
|--------|---------|-------------|
| `privateKeyPath` | `certificates/server.key` | Path to private key (PEM format) |
| `publicKeyPath` | `certificates/server.crt` | Path to certificate (PEM format) |
| `experimental.bypassClientCertificateBinding` | `false` | Skip client certificate binding validation |

## Certificate format

PEM-encoded X.509 certificates.

```
certificates/
├── server.key    # Private key (PKCS#8 or PKCS#1)
└── server.crt    # X.509 certificate
```

To generate manually:

```bash
openssl req -x509 -newkey rsa:2048 -keyout server.key -out server.crt -days 365 -nodes
```

Or let the plugin auto-generate on first startup.

## Troubleshooting

| Problem | Solution |
|---------|----------|
| Plugin not loaded | Verify `--allow-early-plugins` flag is set |
| Certificates not loading | Check file permissions and PEM format |
| Config not loading | Validate JSON syntax in `CustomCert.json` |

# CustomCert

Hytale Plugin for custom/persistent TLS certificates (+ reverse proxy support). It uses Hytales special `Early Plugins` to modify
the certificate-loading process before the actual server boots up.

> The plugin works with both 'Online' and 'Offline' mode. In order to put your server behind a reverse proxy (e.g. [quic-relay](https://github.com/HyBuildNet/quic-relay)) you have to set `bypassClientCertificateBinding` to `true`. This will prevent the server from checking the client certificate (which in a reverse-proxy scenario has a different key than the Hytale installation on your computer) - otherwise the connection will fail. Make sure that the server and the proxy use the same certificate. 

**Documentation:** [hybuildnet.github.io/HytaleCustomCert](https://hybuildnet.github.io/HytaleCustomCert/)

**Download:** [CustomCert.jar](https://raw.githubusercontent.com/HyBuildNet/HytaleCustomCert/main/build/latest/CustomCert.jar)

## License

[MIT](LICENSE)

This project bundles [Javassist](https://www.javassist.org/) (Apache 2.0 / MPL).

[!] This project is neither related to nor affiliated with HYPIXEL STUDIOS CANADA INC. or any other Trademark owner of Hytale.

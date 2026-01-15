# CustomCert

Hytale Plugin for custom/persistent TLS certificates (+ reverse proxy support). It uses Hytales special `Early Plugins` to modify
the certificate-loading process before the actual server boots up.

The plugin works with both 'Online' and 'Offline' mode. In order to put your server behind the reverse proxy you have to set the 
`bypassClientCertificateBinding` to `true`. This will prevent the Server from checking the client (which in a reverse-proxy scenario has a 
different key then the Hytale-installation on your computer) else the connection will fail. Make sure that the server and the proxy use the same 
certificate. 

**Documentation:** [hybuildnet.github.io/HytaleCustomCert](https://hybuildnet.github.io/HytaleCustomCert/)

**Download:** [CustomCert.jar](https://raw.githubusercontent.com/HyBuildNet/HytaleCustomCert/main/build/latest/CustomCert.jar)

## License

[MIT](LICENSE)

This project bundles [Javassist](https://www.javassist.org/) (Apache 2.0 / MPL).

[!] This project is neither related to nor affiliated with HYPIXEL STUDIOS CANADA INC. or any other Trademark owner of Hytale.

import { defineConfig } from 'vitepress'

export default defineConfig({
  title: 'CustomCert',
  description: 'Hytale Early Plugin for persistent TLS certificates and reverse proxy support',

  base: '/HytaleCustomCert/',

  head: [
    ['meta', { name: 'google-site-verification', content: '-T2K0pWwX_CIpTMvP-RrEmr0nCenG4Nhw1YIl5NcjDQ' }],
    ['meta', { name: 'robots', content: 'index, follow' }],
    ['meta', { name: 'keywords', content: 'Hytale, plugin, TLS, certificate, reverse proxy, QUIC' }],
    ['link', { rel: 'canonical', href: 'https://hybuildnet.github.io/HytaleCustomCert/' }]
  ],

  themeConfig: {
    nav: [
      { text: 'Getting Started', link: '/' },
      { text: 'Configuration', link: '/configuration' }
    ],

    sidebar: [
      {
        text: 'Documentation',
        items: [
          { text: 'Getting Started', link: '/' },
          { text: 'Configuration', link: '/configuration' }
        ]
      }
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/HyBuildNet/HytaleCustomCert' }
    ],

    footer: {
      message: 'Released under the MIT License.',
      copyright: 'This project bundles Javassist (Apache 2.0 / MPL).'
    }
  }
})

import { defineConfig } from 'vitepress'

const siteUrl = 'https://hybuildnet.github.io/HytaleCustomCert/'
const siteTitle = 'CustomCert - Hytale TLS Certificate Plugin'
const siteDescription = 'Hytale Early Plugin for persistent TLS certificates and reverse proxy support. Use custom certificates with QUIC transport.'

export default defineConfig({
  title: 'CustomCert',
  description: siteDescription,
  lang: 'en-US',

  base: '/HytaleCustomCert/',

  // SEO
  lastUpdated: true,
  sitemap: {
    hostname: siteUrl
  },

  head: [
    // Verification
    ['meta', { name: 'google-site-verification', content: '-T2K0pWwX_CIpTMvP-RrEmr0nCenG4Nhw1YIl5NcjDQ' }],

    // Basic SEO
    ['meta', { name: 'robots', content: 'index, follow' }],
    ['meta', { name: 'keywords', content: 'Hytale, plugin, TLS, SSL, certificate, reverse proxy, QUIC, server, early plugin, custom cert' }],
    ['link', { rel: 'canonical', href: siteUrl }],

    // Open Graph
    ['meta', { property: 'og:type', content: 'website' }],
    ['meta', { property: 'og:locale', content: 'en_US' }],
    ['meta', { property: 'og:site_name', content: 'CustomCert' }],
    ['meta', { property: 'og:title', content: siteTitle }],
    ['meta', { property: 'og:description', content: siteDescription }],
    ['meta', { property: 'og:url', content: siteUrl }],

    // Twitter Card
    ['meta', { name: 'twitter:card', content: 'summary' }],
    ['meta', { name: 'twitter:title', content: siteTitle }],
    ['meta', { name: 'twitter:description', content: siteDescription }],

    // Additional
    ['meta', { name: 'author', content: 'HyBuild' }],
    ['meta', { name: 'theme-color', content: '#3eaf7c' }]
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

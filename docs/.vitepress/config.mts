import { defineConfig } from "vitepress";

// https://vitepress.dev/reference/site-config
export default defineConfig({
  title: "p2p Shipyard",
  description: "Build, ship and maintain",
  base: "/p2p-shipyard",
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    nav: [
      {
        text: "Guides",
        link: "/guides/getting-started",
      },
      {
        text: "Blog",
        link: "/blog/how-to-create-an-end-user-happ",
      },
    ],

    sidebar: {
      "/guides/": [
        {
          text: "Build",
          items: [
            {
              text: "Getting Started",
              link: "/guides/getting-started",
            },
            {
              text: "Modules",
              items: [
                {
                  text: "Profiles",
                  link: "https://darksoil.studio/profiles-zome",
                },
                {
                  text: "Linked Devices",
                  link: "https://darksoil.studio/-zome",
                },
                {
                  text: "File Storage",
                  link: "https://darksoil.studio/file-storage",
                },
                {
                  text: "Friends",
                  link: "https://darksoil.studio/friends-zome",
                },
                {
                  text: "Messenger",
                  link: "https://darksoil.studio/messenger-zome",
                },
                {
                  text: "Roles",
                  link: "https://darksoil.studio/roles-zome",
                },
                {
                  text: "Notifications",
                  link: "https://darksoil.studio/notifications-zome",
                },
                {
                  text: "Collaborative Sessions",
                  link: "https://darksoil.studio/collaborative-sessions",
                },
                {
                  text: "Notes",
                  link: "https://darksoil.studio/notes-zome",
                },
                {
                  text: "Comments",
                  link: "https://darksoil.studio/comments-zome",
                },
                {
                  text: "Tags",
                  link: "https://darksoil.studio/tags-zome",
                },
                {
                  text: "Chain of Custody",
                  link: "https://darksoil.studio/chain-of-custody-zome",
                },
              ],
            },
          ],
        },
      ],
    },

    socialLinks: [
      {
        icon: "github",
        link: "https://github.com/darksoil-studio/p2p-shipyard",
      },
    ],
  },
});
